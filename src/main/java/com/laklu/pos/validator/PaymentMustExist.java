package com.laklu.pos.validator;

import com.laklu.pos.repositories.PaymentRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@AllArgsConstructor
public class PaymentMustExist extends BaseRule{
    private final Integer paymentId;
    private final PaymentRepository paymentRepository;

    @Override
    public String getValidateField() {
        return "paymentId";
    }

    @Override
    public boolean isValid() {
        if (paymentRepository == null || paymentId == null || paymentId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID thanh toán không hợp lệ");
        }

        if (!paymentRepository.existsById(paymentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thanh toán với ID: " + paymentId);
        }

        return true;
    }

    @Override
    public String getMessage() {
        return "Payment " + paymentId + " không tìm thấy";
    }
}

