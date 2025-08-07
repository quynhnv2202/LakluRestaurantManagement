package com.laklu.pos.services;

import com.laklu.pos.dataObjects.request.NewOrderItemRequest;
import com.laklu.pos.dataObjects.request.UpdateOrderItemQuantity;
import com.laklu.pos.dataObjects.request.UpdateStatusOrderItemRequest;
import com.laklu.pos.entities.MenuItem;
import com.laklu.pos.entities.Order;
import com.laklu.pos.entities.OrderItem;
import com.laklu.pos.entities.Profile;
import com.laklu.pos.entities.User;
import com.laklu.pos.entities.Menu;
import com.laklu.pos.enums.OrderItemStatus;
import com.laklu.pos.enums.TrackedResourceType;
import com.laklu.pos.exceptions.RuleNotValidException;
import com.laklu.pos.exceptions.httpExceptions.NotFoundException;
import com.laklu.pos.repositories.OrderItemRepository;
import com.laklu.pos.repositories.OrderRepository;
import com.laklu.pos.validator.ValidOrderItemStatus;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final MenuItemService menuItemService;
    private final ActivityLogService activityLogService;
    private final ProfileService profileService;
    private final OrderRepository orderRepository;

    @Transactional
    public void saveAll(List<OrderItem> orderItems) {
        orderItemRepository.saveAll(orderItems);
    }

    public void updateOrderItemsStatus(List<OrderItem> orderItems) {
        orderItems.forEach(item -> item.setUpdatedAt(LocalDateTime.now()));
        orderItemRepository.saveAll(orderItems);
    }

    public OrderItem findOrFail(Integer id) {
        return findById(id)
            .orElseThrow(NotFoundException::new);

    }

    public Optional<OrderItem> findById(Integer id) {
        return orderItemRepository.findById(id);
    }

    public OrderItem createNewItemByOrderId(Order order, NewOrderItemRequest newOrderItemRequest) {
        MenuItem menuItem = menuItemService.findOrFail(newOrderItemRequest.getMenuItemId());

        Optional<OrderItem> existingOrderItem = orderItemRepository.findByOrderAndMenuItem(order, menuItem);
        if (existingOrderItem.isPresent()) {
            OrderItem orderItem = existingOrderItem.get();
            int newQuantity = orderItem.getQuantity() + newOrderItemRequest.getQuantity();
            orderItem.setQuantity(newQuantity);
            orderItem.setUpdatedAt(LocalDateTime.now());
            return orderItemRepository.save(orderItem);
        }else{
            OrderItem orderItem = OrderItem.builder()
                    .menuItem(menuItem)
                    .order(order)
                    .quantity(newOrderItemRequest.getQuantity())
                    .status(OrderItemStatus.DELIVERED)
                    .build();

            return orderItemRepository.save(orderItem);
        }
    }

    public OrderItem updateOrderItemStatus(OrderItem orderItem, UpdateStatusOrderItemRequest updateStatusOrderItemRequest) {

        OrderItemStatus currentStatus = orderItem.getStatus();
        OrderItemStatus newStatus = OrderItemStatus.valueOf(updateStatusOrderItemRequest.getStatus());

        ValidOrderItemStatus rule = new ValidOrderItemStatus(currentStatus, newStatus, "status");
        if (!rule.isValid()) {
            throw new RuleNotValidException(rule.getMessage());
        }
        orderItem.setStatus(newStatus);

        return orderItemRepository.save(orderItem);
    }

    public OrderItem updateOrderItemQuantity(OrderItem orderItem, UpdateOrderItemQuantity updateOrderItemQuantity) {

        if (orderItem.getStatus() != OrderItemStatus.PENDING && orderItem.getStatus() != OrderItemStatus.DELIVERED) {
            throw new RuleNotValidException("Chỉ có thể cập nhật số lượng khi trạng thái là PENDING hoặc DELIVERED");
        }
        orderItem.setQuantity(updateOrderItemQuantity.getQuantity());
        return orderItemRepository.save(orderItem);
    }

    @Transactional
    public void deleteOrderItem(OrderItem orderItem) {
        String staffId = "N/A";
        String staffName = "không xác định";
        if (orderItem.getStatus() != OrderItemStatus.PENDING && orderItem.getStatus() != OrderItemStatus.CANCELLED && orderItem.getStatus() != OrderItemStatus.DELIVERED) {
            throw new IllegalStateException("Chỉ được phép xoá các món có trạng thái đang chờ hoặc đã huỷ");
        }
        
        // Lấy order từ orderItem
        Order order = orderItem.getOrder();
        
        // Đếm số lượng order items từ cơ sở dữ liệu
        long countOrderItems = orderItemRepository.countByOrder(order);

        if(countOrderItems == 1 && orderItem.getStatus() == OrderItemStatus.CANCELLED) {
            orderRepository.delete(order);
            return;
        }

        // Nếu chỉ có 1 order item, không cho phép xóa
        if (countOrderItems <= 1) {
            throw new IllegalStateException("Không thể xoá. Trong hóa đơn phải tồn tại ít nhất một món!");
        }
        
        try {
            // Lấy thông tin nhân viên hiện tại từ JwtGuard
            User user = com.laklu.pos.auth.JwtGuard.userPrincipal().getPersitentUser();
            staffId = String.valueOf(user.getId());

            // Lấy thông tin profile để có fullName
            Optional<Profile> profile = profileService.findByUserId(user.getId());
            if (profile.isPresent()) {
                staffName = profile.get().getFullName();
            } else {
                // Nếu không có profile, sử dụng username
                staffName = user.getUsername();
            }
        } catch (Exception ignored) {
            // Không làm gì nếu không lấy được thông tin
        }

        String itemName = orderItem.getMenuItem() != null && orderItem.getMenuItem().getDish() != null
                ? orderItem.getMenuItem().getDish().getName() : "không xác định";

        int quantity = orderItem.getQuantity();

        String logDetails = String.format("Xoá món %s với số lượng %d bởi nhân viên %s",
                itemName, quantity, staffName);

        activityLogService.logActivity(
                orderItem,
                TrackedResourceType.Action.DELETE,
                String.valueOf(orderItem.getId()),
                TrackedResourceType.ORDER,
                logDetails
        );

        orderItemRepository.delete(orderItem);
    }

    @Transactional
    public List<OrderItem> updateOrderItemsStatus(List<Integer> orderItemIds, OrderItemStatus status) {
        List<OrderItem> orderItems = orderItemRepository.findAllById(orderItemIds);
        if (orderItems.isEmpty()) {
            throw new NotFoundException();
        }

        for (OrderItem orderItem : orderItems) {
            orderItem.setStatus(status);
        }

        return orderItemRepository.saveAll(orderItems);
    }

    /**
     * Hủy tất cả OrderItem đang ở trạng thái PENDING liên quan đến một MenuItem
     * @param menuItem MenuItem cần hủy các OrderItem liên quan
     * @return Danh sách các OrderItem đã được hủy
     */
    @Transactional
    public List<OrderItem> cancelPendingOrderItemsByMenuItem(MenuItem menuItem) {
        // Lấy các OrderItem có trạng thái PENDING được tạo trong 1 ngày gần đây
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        List<OrderItem> pendingOrderItems = orderItemRepository.findByMenuItemAndStatusAndCreatedAtGreaterThanEqual(
            menuItem, OrderItemStatus.PENDING, oneDayAgo);

        for (OrderItem orderItem : pendingOrderItems) {
            orderItem.setStatus(OrderItemStatus.CANCELLED);
            orderItem.setUpdatedAt(LocalDateTime.now());

            // Ghi log hoạt động
            activityLogService.logActivity(
                orderItem,
                TrackedResourceType.Action.UPDATE,
                String.valueOf(orderItem.getId()),
                TrackedResourceType.ORDER,
                String.format("Hủy OrderItem do MenuItem '%s' đã bị vô hiệu hóa",
                    menuItem.getDish() != null ? menuItem.getDish().getName() : "Không xác định")
            );
        }

        return orderItemRepository.saveAll(pendingOrderItems);
    }

    /**
     * Kiểm tra xem có tồn tại OrderItem nào sử dụng MenuItem thuộc về menu cụ thể không
     *
     * @param menu Menu cần kiểm tra
     * @return true nếu có ít nhất một OrderItem sử dụng MenuItem thuộc về menu, ngược lại là false
     */
    public boolean existsByMenuItems_Menu(Menu menu) {
        return orderItemRepository.existsByMenuItems_Menu(menu);
    }

    public OrderItem cancelIgnoreStatus(OrderItem orderItem) {
        orderItem.setStatus(OrderItemStatus.CANCELLED);
        return orderItemRepository.save(orderItem);
    }
}
