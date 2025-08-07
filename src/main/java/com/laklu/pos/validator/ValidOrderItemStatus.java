package com.laklu.pos.validator;

import com.laklu.pos.enums.OrderItemStatus;

public class ValidOrderItemStatus extends BaseRule{
    private final OrderItemStatus currentStatus;
    private final OrderItemStatus newStatus;
    private final String field;

    public ValidOrderItemStatus(OrderItemStatus currentStatus, OrderItemStatus newStatus, String field) {
        this.currentStatus = currentStatus;
        this.newStatus = newStatus;
        this.field = field;
    }

    @Override
    public String getValidateField() {
        return this.field;
    }

    @Override
    public boolean isValid() {
        // Chỉ cho phép cập nhật từ PENDING -> DOING, DOING -> COMPLETED -> DELIVERED, hoặc PENDING -> CANCELLED
        return (currentStatus == OrderItemStatus.PENDING && (newStatus == OrderItemStatus.DOING || newStatus == OrderItemStatus.CANCELLED))
            || (currentStatus == OrderItemStatus.DOING && newStatus == OrderItemStatus.COMPLETED)
            || (currentStatus == OrderItemStatus.COMPLETED && newStatus == OrderItemStatus.DELIVERED);
    }

    @Override
    public String getMessage() {
        return "Không thể cập nhật trạng thái từ " + currentStatus + " sang " + newStatus;
    }
}
