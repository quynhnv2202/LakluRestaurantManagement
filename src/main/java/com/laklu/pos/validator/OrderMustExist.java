package com.laklu.pos.validator;

import com.laklu.pos.repositories.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@AllArgsConstructor
public class OrderMustExist extends BaseRule{
    private final Integer orderId;
    private final OrderRepository orderRepository;

    @Override
    public String getValidateField() {
        return "orderId";
    }

    @Override
    public boolean isValid() {
        if (orderRepository == null || orderId == null || orderId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID đơn hàng không hợp lệ");
        }

        if (!orderRepository.existsById(orderId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với ID: " + orderId);
        }

        return true;
    }

    @Override
    public String getMessage() {
        return "Order " + orderId + " không tìm thấy";
    }
}
