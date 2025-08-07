package com.laklu.pos.dataObjects.response;

import com.laklu.pos.enums.PaymentMethod;
import com.laklu.pos.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CashResponse {
    private Integer orderId;
    private BigDecimal amountPaid;
    private BigDecimal vat;
    private BigDecimal receivedAmount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;
    private BigDecimal change;
}
