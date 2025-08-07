package com.laklu.pos.services;

import com.laklu.pos.dataObjects.request.*;
import com.laklu.pos.dataObjects.response.OrderResponse;
import com.laklu.pos.entities.*;
import com.laklu.pos.enums.OrderItemStatus;
import com.laklu.pos.enums.OrderStatus;
import com.laklu.pos.exceptions.RuleNotValidException;
import com.laklu.pos.exceptions.httpExceptions.NotFoundException;
import com.laklu.pos.mapper.OrderMapper;
import com.laklu.pos.repositories.OrderRepository;
import com.laklu.pos.validator.OrderExistRule;
import com.laklu.pos.validator.OrderStatusTransitionRule;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ReservationService reservationService;
    private final MenuItemService menuItemService;
    private final OrderItemService orderItemService;
    private final OrderMapper orderMapper;
    private final TableService tableService;

    public List<Order> getAllOrders(LocalDate date, String status, String sort) {
        Specification<Order> spec = Specification.where((root, query, cb) -> cb.conjunction());
        if (date != null) {
            // Thời gian bắt đầu (06:00 AM của ngày đó)
            LocalDateTime startTime = date.atTime(6, 0);
            // Thời gian kết thúc (03:00 AM của ngày hôm sau)
            LocalDateTime endTime = date.plusDays(1).atTime(3, 0);
            spec = spec.and((root, query, cb) -> cb.between(root.get("createdAt"), startTime, endTime));
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        Sort sortOrder = (sort != null && sort.equalsIgnoreCase("asc"))
                ? Sort.by("createdAt").ascending()
                : Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sortOrder); // Không giới hạn số lượng
        List<Order> orders = orderRepository.findAll(spec, pageable).getContent();
        return orders;
    }


    @Transactional
    public Order createOrder(NewOrderRequest newOrderRequest, User staff) {
        Reservation reservation = reservationService.findOrFail(newOrderRequest.getReservationId());
        reservationService.updateReservationStatus(reservation, Reservation.Status.CONFIRMED);
        Order order = Order.builder()
                .staff(staff)
                .reservation(reservation)
                .status(OrderStatus.PENDING)
                .build();

        orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (NewOrderItemRequest orderItemRequest : newOrderRequest.getOrderItems()) {
            MenuItem menuItem = menuItemService.findOrFail(orderItemRequest.getMenuItemId());
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .quantity(orderItemRequest.getQuantity())
                    .status(OrderItemStatus.PENDING)
                    .build();

            orderItems.add(orderItem);
        }
        orderItemService.saveAll(orderItems);

        return order;
    }

    @Transactional
    public List<OrderResponse> splitOrder(OrderSplitRequest request, User staff) {
        Order originalOrder = findOrFail(request.getOrderId());
        if (originalOrder.getStatus().equals(OrderStatus.CANCELLED) ||
                originalOrder.getStatus().equals(OrderStatus.COMPLETED)) {
            throw new RuleNotValidException("Không thể tách đơn đã hủy hoặc hoàn thành");
        }

        List<OrderItemSplitRequest> itemsToSplit = request.getOrderItems();
        if (itemsToSplit == null || itemsToSplit.isEmpty()) {
            throw new IllegalArgumentException("Không có món để tách đơn");
        }

        List<OrderItem> originalItems = originalOrder.getOrderItems();
        List<OrderItem> newOrderItems = new ArrayList<>();

        for (OrderItemSplitRequest splitRequest : itemsToSplit) {
            OrderItem originalItem = originalOrder.getOrderItems().stream()
                    .filter(item -> item.getId().equals(splitRequest.getOrderItemId()))
                    .findFirst()
                    .orElseThrow(() -> new RuleNotValidException("Không tìm thấy món với ID "
                            + splitRequest.getOrderItemId() + " trong đơn hàng"));

            int splitQuantity = splitRequest.getQuantity();
            if (splitQuantity > originalItem.getQuantity()) {
                throw new IllegalArgumentException("Số lượng món "
                        + originalItem.getMenuItem().getDish().getName()
                        + " muốn tách phải bằng hoặc nhỏ hơn"
                        + originalItem.getQuantity());
            }

            OrderItem newItem = OrderItem.builder()
                    .menuItem(originalItem.getMenuItem())
                    .quantity(splitQuantity)
                    .status(OrderItemStatus.DELIVERED)
                    .build();
            newOrderItems.add(newItem);

            if (splitQuantity == originalItem.getQuantity()) {
                originalItems.remove(originalItem);
            } else {
                originalItem.setQuantity(originalItem.getQuantity() - splitQuantity);
            }
        }

        if (newOrderItems.isEmpty()) {
            throw new RuleNotValidException("Không có món để tách đơn");
        }

        Order newOrder = Order.builder()
                .reservation(originalOrder.getReservation())
                .staff(staff)
                .status(OrderStatus.PENDING)
                .orderItems(newOrderItems)
                .build();
        newOrderItems.forEach(item -> item.setOrder(newOrder));

        orderRepository.save(originalOrder);
        orderRepository.save(newOrder);

        OrderResponse originalResponse = orderMapper.toOrderResponse(originalOrder);
        OrderResponse newResponse = orderMapper.toOrderResponse(newOrder);
        Table table = tableService.getTableNumberByReservationId(originalOrder.getReservation().getId());
        originalResponse.setTableNumber(table.getTableNumber());
        originalResponse.setTableId(table.getId());
        newResponse.setTableNumber(table.getTableNumber());
        newResponse.setTableId(table.getId());

        return List.of(originalResponse, newResponse);
    }

    @Transactional
    public OrderResponse mergeOrders(MergeOrderRequest request, User staff) {
        List<Integer> orderIds = request.getOrderIds();
        if (orderIds == null || orderIds.size() < 2) {
            throw new IllegalArgumentException("Để gộp đơn cần ít nhất 2 đơn");
        }

        List<Order> ordersToMerge = orderRepository.findAllById(orderIds);
        if (ordersToMerge.size() != orderIds.size()) {
            throw new NotFoundException();
        }

        boolean sameReservation = ordersToMerge.stream()
                .allMatch(order -> order.getReservation().getId().equals(request.getReservationId()));
        if (!sameReservation) {
            throw new IllegalArgumentException("Tất cả đơn đều phải chung 1 reservation");
        }

        boolean invalidStatus = ordersToMerge.stream()
                .anyMatch(order -> order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED);
        if (invalidStatus) {
            throw new RuleNotValidException("Không thể gộp đơn đã thanh toán hoặc hủy");
        }

        List<OrderItem> mergedItems = new ArrayList<>();
        for (Order order : ordersToMerge) {
            for (OrderItem item : order.getOrderItems()) {
                OrderItem existingItem = mergedItems.stream()
                        .filter(i -> i.getMenuItem().getId().equals(item.getMenuItem().getId()))
                        .findFirst()
                        .orElse(null);
                if (existingItem != null) {
                    existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                } else {
                    mergedItems.add(OrderItem.builder()
                            .menuItem(item.getMenuItem())
                            .quantity(item.getQuantity())
                            .status(OrderItemStatus.DELIVERED)
                            .build());
                }
            }
        }

        Order mergedOrder = Order.builder()
                .reservation(ordersToMerge.get(0).getReservation()) // Lấy reservation từ order đầu tiên
                .staff(staff) // Thu ngân thực hiện gộp
                .status(OrderStatus.PENDING) // Order mới ở trạng thái PENDING để chờ thanh toán
                .orderItems(mergedItems) // Danh sách món gộp
                .build();

        mergedItems.forEach(item -> item.setOrder(mergedOrder));
        ordersToMerge.forEach(order -> {
            order.setStatus(OrderStatus.CANCELLED); // Đặt trạng thái CANCELLED cho order cũ
            order.getOrderItems().forEach(item -> item.setStatus(OrderItemStatus.CANCELLED)); // Hủy các món trong order cũ
        });

        orderRepository.saveAll(ordersToMerge);
        orderRepository.save(mergedOrder);

        OrderResponse response = orderMapper.toOrderResponse(mergedOrder);
        Table table = tableService.getTableNumberByReservationId(mergedOrder.getReservation().getId());
        response.setTableNumber(table.getTableNumber());
        response.setTableId(table.getId());

        return response;
    }

    public Optional<Order> findById(Integer id) {
        return orderRepository.findById(id);
    }

    public Order findOrFail(Integer id) {
        return findById(id)
                .orElseThrow(NotFoundException::new);
    }

    public Order updateOrderStatus(Order order, UpdateStatusOrderRequest request) {
        OrderStatus newStatus = OrderStatus.valueOf(request.getStatus());
        OrderStatus currentStatus = order.getStatus();

        OrderStatusTransitionRule rule = new OrderStatusTransitionRule(currentStatus, newStatus, order);

        if (!rule.isValid()) {
            throw new RuleNotValidException(rule.getMessage());
        }

        if (newStatus == OrderStatus.CANCELLED) {
            order.getOrderItems().forEach(item -> item.setStatus(OrderItemStatus.CANCELLED));
            orderItemService.updateOrderItemsStatus(order.getOrderItems());
        }
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public Order getOrderExist(Integer id) {
        Order orderExist = findOrFail(id);
        OrderExistRule rule = new OrderExistRule(orderExist);
        if (!rule.isValid()) {
            throw new RuleNotValidException(rule.getMessage());
        }
        return orderExist;
    }

    public List<Order> getOrderByReservationId(Integer id) {
        List<Order> orders = orderRepository.findByReservationIdAndStatusNotCancelledOrCompleted(id);
        if (orders.isEmpty()) {
            throw new NotFoundException();
        }
        return orders;
    }
    
    public List<Order> getOrderNotCancelByReservationId(Integer id) {
        List<Order> orders = orderRepository.findByReservationIdAndStatusNotCancelledOrCompleted(id);
        if (orders.isEmpty()) {
            throw new NotFoundException();
        }
        // Với mỗi Order, loại bỏ các OrderItem có status = CANCEL
        orders.forEach(order -> {
            // Nếu order.getOrderItems() trả về List<OrderItem>
            List<OrderItem> filteredItems = order.getOrderItems().stream()
                .filter(item -> item.getStatus() != OrderItemStatus.CANCELLED)
                .collect(Collectors.toList());
            order.setOrderItems(filteredItems);
        });
        return orders;
    }

    /**
     * Lấy danh sách đơn hàng từ 4h chiều đến 3h sáng hôm sau với trạng thái PENDING
     * @param today Ngày hiện tại
     * @return Danh sách đơn hàng đã sắp xếp theo thời gian tạo tăng dần
     */
    public List<Order> getEveningToDawnOrders(LocalDate today) {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDate queryDate = today;

        /* Tạm thời comment đoạn này lại
        // Nếu thời gian hiện tại là từ 0h đến 3h sáng, cần lấy đơn hàng từ ngày hôm trước
        if (currentTime.getHour() >= 0 && currentTime.getHour() < 3) {
            // Nếu today là ngày hiện tại (không được chỉ định rõ), thì phải lấy đơn từ ngày hôm trước
            if (today.equals(LocalDate.now())) {
                queryDate = today.minusDays(1);
            }
        }

        // Thời gian bắt đầu (16:00 chiều của ngày được tính)
        LocalDateTime startTime = queryDate.atTime(16, 0);
        // Thời gian kết thúc (03:00 sáng của ngày hôm sau)
        LocalDateTime endTime = queryDate.plusDays(1).atTime(3, 0);
        */

        // Lấy toàn bộ orders trong ngày (từ 00:00 đến 23:59:59)
        LocalDateTime startTime = queryDate.atStartOfDay();
        LocalDateTime endTime = queryDate.atTime(23, 59, 59);

        Specification<Order> spec = Specification.where((root, query, cb) ->
            cb.and(
                cb.between(root.get("createdAt"), startTime, endTime),
                cb.equal(root.get("status"), OrderStatus.PENDING)
            )
        );

        // Sắp xếp theo thời gian tạo tăng dần (cái nào tạo trước đứng trước)
        Sort sortOrder = Sort.by("createdAt").ascending();
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sortOrder);

        return orderRepository.findAll(spec, pageable).getContent();
    }

    public Order deleteOrder(Order orderExist) {
        boolean canDelete = orderExist.getOrderItems().stream()
            .allMatch(item -> item.getStatus() == OrderItemStatus.PENDING || item.getStatus() == OrderItemStatus.CANCELLED);

        if (canDelete) {
            orderRepository.delete(orderExist);
            return orderExist;
        } else {
            throw new IllegalStateException("Đơn hàng chứa món không phải đang làm");
        }
    }
}
