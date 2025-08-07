package com.laklu.pos.validator;

import com.laklu.pos.entities.Order;
import com.laklu.pos.enums.OrderStatus;

public class OrderExistRule extends BaseRule {
    private final Order order;

    public OrderExistRule(Order order) {
        this.order = order;
    }

    @Override
    public String getValidateField() {
        return "orderStatus";
    }

    @Override
    public boolean isValid() {
        return !(order.getStatus().equals(OrderStatus.CANCELLED) || order.getStatus().equals(OrderStatus.COMPLETED));
    }

    @Override
    public String getMessage() {
        return "Order ID " + order.getId() + " đã bị hủy hoặc hoàn thành, không thể thực hiện thao tác này.";
    }
}
