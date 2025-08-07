package com.laklu.pos.controllers;


import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.auth.policies.OrderPolicy;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.request.MergeOrderRequest;
import com.laklu.pos.dataObjects.request.NewOrderRequest;
import com.laklu.pos.dataObjects.request.OrderSplitRequest;
import com.laklu.pos.dataObjects.request.UpdateStatusOrderRequest;
import com.laklu.pos.dataObjects.response.*;
import com.laklu.pos.entities.*;
import com.laklu.pos.enums.OrderItemStatus;
import com.laklu.pos.exceptions.httpExceptions.ForbiddenException;
import com.laklu.pos.mapper.DishMapper;
import com.laklu.pos.mapper.OrderMapper;
import com.laklu.pos.repositories.ScheduleRepository;
import com.laklu.pos.services.*;
import com.laklu.pos.uiltis.Ultis;
import com.laklu.pos.validator.RuleValidator;
import com.laklu.pos.validator.UserMustHaveScheduleAndAttendance;
import com.laklu.pos.valueObjects.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/order")
@Tag(name = "Order Controller", description = "Quản lý thông tin đặt món")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final OrderPolicy orderPolicy;
    private final DishMapper dishMapper;
    private final MenuItemService menuItemService;
    private final TableService tableService;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleService scheduleService;
    private final ReservationService reservationService;

    @Operation(summary = "Lấy thông tin tất cả đơn đặt món", description = "API này dùng để lấy danh sách tất cả mục đặt món")
    @GetMapping("/")
    public ApiResponseEntity getAllOrder(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "desc") String sort
    ) throws Exception {
        Ultis.throwUnless(orderPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());
        LocalDate localDate = (date != null && !date.isEmpty()) ? LocalDate.parse(date) : null;
        String dbStatus = (status != null && !status.isEmpty()) ? mapStatusToDb(status) : null;
        List<Order> orders = orderService.getAllOrders(localDate, dbStatus, sort);

        // Chuyển đổi sang OrderResponseDTO cho calendar view
        List<OrderResponseDTO> orderResponseDTOs = orders.stream()
                .map(this::convertOrderResponseDTO)
                .collect(Collectors.toList());

        // Comment lại phần cũ không cần thiết
        /*
        return ApiResponseEntity.success(orderMapper.toOrderResponses(orders));
        */

        return ApiResponseEntity.success(orderResponseDTOs);
    }

    @Operation(summary = "Tạo một mục trong đặt món", description = "API này dùng để tạo một mục trong đặt món mới")
    @PostMapping("/")
    public ApiResponseEntity createOrder(
            @Valid @RequestBody NewOrderRequest newOrderRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) throws Exception {
        Ultis.throwUnless(orderPolicy.canCreate(JwtGuard.userPrincipal()), new ForbiddenException());
        User staff = userPrincipal.getPersitentUser(); // Lấy staffId từ User đang đăng nhập
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);
        Order order = orderService.createOrder(newOrderRequest, staff);
        return ApiResponseEntity.success(orderMapper.toOrderResponse(order));
    }

    @Operation(summary = "Lấy thông tin mục trong đặt món theo ID", description = "API này dùng để lấy thông tin mục trong đặt món theo ID")
    @GetMapping("/{id}")
    public ApiResponseEntity getOrderById(@PathVariable Integer id) throws Exception {
        Order order = orderService.findOrFail(id);
        Ultis.throwUnless(orderPolicy.canView(JwtGuard.userPrincipal(), order), new ForbiddenException());

        // Comment lại phần cũ không cần thiết
        /*
        OrderResponse orderResponse = orderMapper.toOrderResponse(order);
        // Lấy danh sách menuItemId từ tất cả OrderItemResponse
        List<Integer> menuItemIds = orderResponse.getOrderItems().stream()
                .map(OrderItemResponse::getMenuItemId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // Truy vấn toàn bộ MenuItem một lần duy nhất
        Map<Integer, MenuItem> menuItemMap = menuItemService.findAllByIds(menuItemIds).stream()
                .collect(Collectors.toMap(MenuItem::getId, Function.identity()));

        for (OrderItemResponse orderItemResponse : orderResponse.getOrderItems()) {
            MenuItem menuItem = menuItemMap.get(orderItemResponse.getMenuItemId());
            if (menuItem != null && menuItem.getDish() != null) {
                DishResponse dishResponse = dishMapper.toDishResponse(menuItem.getDish());
                orderItemResponse.setDish(dishResponse);
            }
        }
        Table table = tableService.getTableNumberByReservationId(order.getReservation().getId());
        orderResponse.setTableNumber(table.getTableNumber());
        orderResponse.setTableId(table.getId());
        return ApiResponseEntity.success(orderResponse);
        */

        // Sử dụng convertOrderResponseDTO để chuyển đổi
        OrderResponseDTO orderResponseDTO = convertOrderResponseDTO(order);
        return ApiResponseEntity.success(orderResponseDTO);
    }

    @Operation(summary = "Cập nhật một phần thông tin trạng thái trong đặt món", description = "API này dùng để cập nhật một phần thông tin mục trong đặt món theo ID")
    @PutMapping("/status/{id}")
    public ApiResponseEntity updateOrderStatus(@PathVariable Integer id, @Valid @RequestBody UpdateStatusOrderRequest request) throws Exception {
        Order order = orderService.findOrFail(id);
        Ultis.throwUnless(orderPolicy.canEdit(JwtGuard.userPrincipal(), order), new ForbiddenException());
        User staff = JwtGuard.userPrincipal().getPersitentUser();
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);
        Order updatedOrder = orderService.updateOrderStatus(order, request);
        return ApiResponseEntity.success(orderMapper.toOrderResponse(updatedOrder));
    }

    private String mapStatusToDb(String status) {
        return switch (status) {
            case "Đang chờ" -> "PENDING";
            case "Đã xác nhận" -> "CONFIRMED";
            case "Đã hoàn thành" -> "COMPLETED";
            case "Đã hủy" -> "CANCELLED";
            default -> null;
        };
    }

    @Operation(summary = "Lấy đặt món theo mã đặt bàn", description = "API này dùng để lấy đặt món theo mã đặt bàn")
    @GetMapping("/reservation/{id}")
    public ApiResponseEntity getOrderByReservationId(@PathVariable Integer id) throws Exception {
        Reservation reservation = reservationService.findOrFail(id);
        Ultis.throwUnless(orderPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());
        List<Order> orders = orderService.getOrderByReservationId(reservation.getId());
        List<OrderResponse> orderResponses = orderMapper.toOrderResponses(orders);
        for (OrderResponse orderResponse : orderResponses) {
            // Lấy danh sách menuItemId từ tất cả OrderItemResponse
            List<Integer> menuItemIds = orderResponse.getOrderItems().stream()
                    .map(OrderItemResponse::getMenuItemId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            // Truy vấn toàn bộ MenuItem một lần duy nhất
            Map<Integer, MenuItem> menuItemMap = menuItemService.findAllByIds(menuItemIds).stream()
                    .collect(Collectors.toMap(MenuItem::getId, Function.identity()));

            for (OrderItemResponse orderItemResponse : orderResponse.getOrderItems()) {
                MenuItem menuItem = menuItemMap.get(orderItemResponse.getMenuItemId());
                if (menuItem != null && menuItem.getDish() != null) {
                    DishResponse dishResponse = dishMapper.toDishResponse(menuItem.getDish());
                    orderItemResponse.setDish(dishResponse);
                }
            }
        }
        return ApiResponseEntity.success(orderResponses);
    }

    @Operation(
            summary = "Lấy đặt món theo mã đặt bàn ngoại trừ các món bị cancel",
            description = "API này dùng để lấy đặt món theo mã đặt bàn ngoại trừ các món bị cancel"
    )
    @GetMapping("/active/{id}")
    public ApiResponseEntity getOrderNotCancelByReservationId(@PathVariable Integer id) throws Exception {
        Reservation reservation = reservationService.findOrFail(id);
        Ultis.throwUnless(orderPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());
        List<Order> orders = orderService.getOrderNotCancelByReservationId(reservation.getId());
        List<OrderResponse> orderResponses = orderMapper.toOrderResponses(orders);
        for (OrderResponse orderResponse : orderResponses) {
            // Lấy danh sách menuItemId từ tất cả OrderItemResponse
            List<Integer> menuItemIds = orderResponse.getOrderItems().stream()
                    .map(OrderItemResponse::getMenuItemId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            // Truy vấn toàn bộ MenuItem một lần duy nhất
            Map<Integer, MenuItem> menuItemMap = menuItemService.findAllByIds(menuItemIds).stream()
                    .collect(Collectors.toMap(MenuItem::getId, Function.identity()));

            for (OrderItemResponse orderItemResponse : orderResponse.getOrderItems()) {
                MenuItem menuItem = menuItemMap.get(orderItemResponse.getMenuItemId());
                if (menuItem != null && menuItem.getDish() != null) {
                    DishResponse dishResponse = dishMapper.toDishResponse(menuItem.getDish());
                    orderItemResponse.setDish(dishResponse);
                }
            }
        }
        return ApiResponseEntity.success(orderResponses);
    }

    @Operation(summary = "Tách đơn hàng để thanh toán riêng", description = "API này dùng để tách một đơn hàng thành hai đơn để hỗ trợ thanh toán riêng")
    @PostMapping("/{id}/split")
    public ApiResponseEntity splitOrder(
            @PathVariable Integer id,
            @Valid @RequestBody OrderSplitRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) throws Exception {
        Order order = orderService.findOrFail(id);
        Ultis.throwUnless(orderPolicy.canEdit(JwtGuard.userPrincipal(), order), new ForbiddenException());
        User staff = userPrincipal.getPersitentUser();
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);
        if (!request.getOrderId().equals(id)) {
            throw new IllegalArgumentException("Order ID trong path và body phải trùng khớp");
        }

        List<OrderResponse> responses = orderService.splitOrder(request, staff);
        return ApiResponseEntity.success(responses);
    }

    @Operation(summary = "Gộp nhiều đơn hàng thành một", description = "API này dùng để gộp nhiều đơn hàng thành một đơn hàng mới")
    @PostMapping("/merge")
    public ApiResponseEntity mergeOrders(
            @Valid @RequestBody MergeOrderRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) throws Exception {
        // Ultis.throwUnless(orderPolicy.canCreate(JwtGuard.userPrincipal()), new ForbiddenException());
        User staff = userPrincipal.getPersitentUser();
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);
        OrderResponse response = orderService.mergeOrders(request, staff);
        return ApiResponseEntity.success(response);
    }

    @Operation(summary = "Lấy đơn hàng từ 4h chiều đến 3h sáng hôm sau", description = "API này lấy các đơn hàng với trạng thái 'Đang chờ' từ 4h chiều đến 3h sáng hôm sau, sắp xếp theo thời gian tạo. Khi gọi API vào rạng sáng (0h-3h), hệ thống tự động lấy đơn từ 4h chiều ngày hôm trước đến 3h sáng ngày hiện tại.")
    @GetMapping("/evening-to-dawn")
    public ApiResponseEntity getEveningToDawnOrders(
            @RequestParam(required = false) String date
    ) throws Exception {
        Ultis.throwUnless(orderPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());

        // Mặc định sử dụng ngày hiện tại nếu không có tham số date
        LocalDate today = (date != null && !date.isEmpty()) ? LocalDate.parse(date) : LocalDate.now();

        List<Order> orders = orderService.getEveningToDawnOrders(today);

        // Chuyển đổi sang OrderResponseDTO
        List<OrderResponseDTO> orderResponseDTOs = orders.stream()
                .map(this::convertOrderResponseDTO)
                .collect(Collectors.toList());

        return ApiResponseEntity.success(orderResponseDTOs);
    }

    @Operation(summary = "Xoá đơn không mong muốn", description = "API xoá đơn hàng có các món bên trong là 'đang chờ' và 'đã huỷ'")
    @DeleteMapping("/{id}")
    public ApiResponseEntity deleteOrder(@PathVariable Integer id) throws Exception {
        Order orderExit = orderService.findOrFail(id);
        Ultis.throwUnless(orderPolicy.canDelete(JwtGuard.userPrincipal(), orderExit), new ForbiddenException());
        User staff = JwtGuard.userPrincipal().getPersitentUser();
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);
        Order order = orderService.deleteOrder(orderExit);
        return ApiResponseEntity.success(orderMapper.toOrderResponse(order));
    }

    private OrderResponseDTO convertOrderResponseDTO(Order order) {
        try {
            List<TableInfo> tables = order.getReservation().getReservationTables().stream()
                    .map(reservationTable -> TableInfo.builder()
                            .id(reservationTable.getTable().getId())
                            .tableNumber(reservationTable.getTable().getTableNumber())
                            .build())
                    .collect(Collectors.toList());

            List<OrderItemResponse> orderItems = order.getOrderItems().stream()
                    .map(orderItem -> {
                        var orderItemResponse = OrderItemResponse.builder()
                                .orderItemId(orderItem.getId())
                                .orderId(orderItem.getOrder().getId())
                                .menuItemId(orderItem.getMenuItem().getId())
                                .quantity(orderItem.getQuantity())
                                .statusLabel(orderItem.getStatus().getLabel())
                                .createdAt(orderItem.getCreatedAt())
                                .updatedAt(orderItem.getUpdatedAt())
                                .build();

                        if (orderItem.getMenuItem().getDish() != null) {
                            DishResponse dishResponse = dishMapper.toDishResponse(orderItem.getMenuItem().getDish());
                            orderItemResponse.setDish(dishResponse);
                        }
                        return orderItemResponse;
                    })
                    .toList();

            return new OrderResponseDTO(
                    order.getId(),
                    order.getReservation().getId(),
                    order.getStaff().getId(),
                    order.getStatus().getLabel(),
                    order.getCreatedAt(),
                    order.getUpdatedAt(),
                    tables,
                    orderItems
            );
        } catch (Exception e) {
            throw e;
        }
    }
}
