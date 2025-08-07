package com.laklu.pos.validator;

import com.laklu.pos.entities.Order;
import com.laklu.pos.enums.OrderItemStatus;
import com.laklu.pos.enums.OrderStatus;

public class OrderStatusTransitionRule extends BaseRule {
    private final OrderStatus currentStatus;
    private final OrderStatus newStatus;
    private final Order order;

    public OrderStatusTransitionRule(OrderStatus currentStatus, OrderStatus newStatus, Order order) {
        this.currentStatus = currentStatus;
        this.newStatus = newStatus;
        this.order = order;
    }

    @Override
    public String getValidateField() {
        return "orderStatus";
    }

    @Override
    public boolean isValid() {
        if (currentStatus == OrderStatus.PENDING && newStatus == OrderStatus.CONFIRMED) {
            return true;
        }
        if (currentStatus == OrderStatus.CONFIRMED && newStatus == OrderStatus.COMPLETED) {
            return true;
        }
        if (currentStatus == OrderStatus.PENDING && newStatus == OrderStatus.CANCELLED) {
            return order.getOrderItems().stream()
                .allMatch(item -> item.getStatus() == OrderItemStatus.PENDING || item.getStatus() == OrderItemStatus.CANCELLED);
        }
        return false;
    }

    @Override
    public String getMessage() {
        return "Không thể chuyển trạng thái từ " + currentStatus + " sang " + newStatus;
    }
}
