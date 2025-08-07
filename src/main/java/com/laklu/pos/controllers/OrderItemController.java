package com.laklu.pos.controllers;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.auth.policies.OrderItemPolicy;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.request.NewOrderItemRequest;
import com.laklu.pos.dataObjects.request.UpdateOrderItemQuantity;
import com.laklu.pos.dataObjects.request.UpdateStatusOrderItemRequest;
import com.laklu.pos.dataObjects.request.BatchUpdateOrderItemStatusRequest;
import com.laklu.pos.dataObjects.response.DishResponse;
import com.laklu.pos.dataObjects.response.OrderItemResponse;
import com.laklu.pos.entities.MenuItem;
import com.laklu.pos.entities.Order;
import com.laklu.pos.entities.OrderItem;
import com.laklu.pos.entities.User;
import com.laklu.pos.enums.OrderItemStatus;
import com.laklu.pos.exceptions.httpExceptions.ForbiddenException;
import com.laklu.pos.mapper.DishMapper;
import com.laklu.pos.mapper.OrderItemMapper;
import com.laklu.pos.repositories.ScheduleRepository;
import com.laklu.pos.services.OrderItemService;
import com.laklu.pos.services.OrderService;
import com.laklu.pos.services.ScheduleService;
import com.laklu.pos.uiltis.Ultis;
import com.laklu.pos.validator.RuleValidator;
import com.laklu.pos.validator.UserMustHaveScheduleAndAttendance;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/order_items")
@Tag(name = "OrderItem Controller", description = "Quản lý thông tin món đã đặt")
@AllArgsConstructor
public class OrderItemController {

    private final OrderItemService orderItemService;
    private final OrderService orderService;
    private final OrderItemMapper orderItemMapper;
    private final OrderItemPolicy orderItemPolicy;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleService scheduleService;
    private final DishMapper dishMapper;

    @Operation(summary = "Lấy thông tin món ăn đã đặt theo ID", description = "API này dùng để lấy thông tin món ăn đã đặt theo ID")
    @GetMapping("/{id}")
    public ApiResponseEntity getOrderItemById(@PathVariable Integer id) throws Exception {
        OrderItem orderItem = orderItemService.findOrFail(id);
        Ultis.throwUnless(orderItemPolicy.canView(JwtGuard.userPrincipal(), orderItem), new ForbiddenException());
        OrderItemResponse orderItemResponse = orderItemMapper.toResponse(orderItem);
        MenuItem menuItem = orderItem.getMenuItem();
        if (menuItem != null && menuItem.getDish() != null) {
            DishResponse dishResponse = dishMapper.toDishResponse(menuItem.getDish());
            orderItemResponse.setDish(dishResponse);
        }
        return ApiResponseEntity.success(orderItemResponse);

    }

    @Operation(summary = "Thêm thông tin món ăn mới theo orderid có sẵn", description = "API này dùng để thêm thông tin món ăn mới theo orderid có sẵn")
    @PostMapping("/{order_id}")
    public ApiResponseEntity createNewItemByOrderId(@PathVariable("order_id") Integer id, @Valid @RequestBody NewOrderItemRequest newOrderItemRequest) throws Exception {
        Order order = orderService.getOrderExist(id);
        Ultis.throwUnless(orderItemPolicy.canCreate(JwtGuard.userPrincipal()), new ForbiddenException());
        User staff = JwtGuard.userPrincipal().getPersitentUser();
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);
        OrderItem newOrderItem = orderItemService.createNewItemByOrderId(order, newOrderItemRequest);
        return ApiResponseEntity.success(orderItemMapper.toResponse(newOrderItem));
    }

    @Operation(summary = "Cập nhật thông tin trạng thái món ăn đang đặt", description = "API này dùng để cập nhật trạng thái món ăn đang đặt theo ID")
    @PutMapping("/status/{id}")
    public ApiResponseEntity updateOrderItemStatus(@PathVariable Integer id, @Valid @RequestBody UpdateStatusOrderItemRequest updateStatusOrderItemRequest) throws Exception {
        OrderItem orderItem = orderItemService.findOrFail(id);
        Ultis.throwUnless(orderItemPolicy.canEdit(JwtGuard.userPrincipal(), orderItem), new ForbiddenException());
        User staff = JwtGuard.userPrincipal().getPersitentUser();
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);
        OrderItem updatedOrder = orderItemService.updateOrderItemStatus(orderItem, updateStatusOrderItemRequest);
        return ApiResponseEntity.success(orderItemMapper.toResponse(updatedOrder));
    }

    @Operation(summary = "Cập nhật thông tin số lượng món ăn đang đặt", description = "API này dùng để cập nhật thông tin số lượng món ăn đang đặt theo ID")
    @PutMapping("/{id}")
    public ApiResponseEntity updateOrderItemQuantity(@PathVariable Integer id, @Valid @RequestBody UpdateOrderItemQuantity updateOrderItemQuantity) throws Exception{
        OrderItem orderItem = orderItemService.findOrFail(id);
        Ultis.throwUnless(orderItemPolicy.canEdit(JwtGuard.userPrincipal(), orderItem), new ForbiddenException());
        User staff = JwtGuard.userPrincipal().getPersitentUser();
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);
        OrderItem updatedOrder = orderItemService.updateOrderItemQuantity(orderItem, updateOrderItemQuantity);
        return ApiResponseEntity.success(orderItemMapper.toResponse(updatedOrder));
    }

    @Operation(summary = "Xoá món ăn đã đặt", description = "API này dùng để xoá thông tin món ăn đã đặt theo ID, đồng thời lưu lại thông tin người xoá và món ăn đã xoá")
    @DeleteMapping("/{id}")
    public ApiResponseEntity deleteOrderItem(@PathVariable Integer id) throws Exception {
        OrderItem orderItem = orderItemService.findOrFail(id);
        Ultis.throwUnless(orderItemPolicy.canDelete(JwtGuard.userPrincipal(), orderItem), new ForbiddenException());
        User staff = JwtGuard.userPrincipal().getPersitentUser();
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);

        // Lưu thông tin trước khi xoá để trả về
        OrderItemResponse response = orderItemMapper.toResponse(orderItem);

        // Tiến hành xoá với log
        orderItemService.deleteOrderItem(orderItem);

        return ApiResponseEntity.success(response);
    }

    @Operation(summary = "Cập nhật trạng thái hàng loạt cho các món ăn đã đặt",
              description = "API này dùng để cập nhật trạng thái cho nhiều món ăn đã đặt cùng lúc")
    @PutMapping("/status/batch")
    public ApiResponseEntity updateOrderItemsStatus(@Valid @RequestBody BatchUpdateOrderItemStatusRequest request) throws Exception {

        User staff = JwtGuard.userPrincipal().getPersitentUser();
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);

        // Kiểm tra quyền cho từng món ăn
        for (Integer orderItemId : request.getOrderItemIds()) {
            OrderItem orderItem = orderItemService.findOrFail(orderItemId);
            Ultis.throwUnless(orderItemPolicy.canEdit(JwtGuard.userPrincipal(), orderItem), new ForbiddenException());
        }

        List<OrderItem> updatedOrderItems = orderItemService.updateOrderItemsStatus(
            request.getOrderItemIds(),
            OrderItemStatus.valueOf(request.getStatus())
        );

        List<OrderItemResponse> responses = updatedOrderItems.stream()
                .map(orderItemMapper::toResponse)
                .collect(Collectors.toList());

        return ApiResponseEntity.success(responses);
    }
    @Operation(summary = "Chuyển sang trạng thái cancel bỏ qua kiểm tra trạng thái món", description = "API này dùng để chuyển sang trạng thái cancel bỏ qua kiểm tra trạng thái món theo ID")
    @PutMapping("/{id}/cancel-status")
    public ApiResponseEntity cancelIgnoreStatus(@PathVariable Integer id) throws Exception{
        OrderItem orderItem = orderItemService.findOrFail(id);
        Ultis.throwUnless(orderItemPolicy.canEdit(JwtGuard.userPrincipal(), orderItem), new ForbiddenException());
        User staff = JwtGuard.userPrincipal().getPersitentUser();
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);
        OrderItem updatedOrder = orderItemService.cancelIgnoreStatus(orderItem);
        return ApiResponseEntity.success(orderItemMapper.toResponse(updatedOrder));
    }
}
