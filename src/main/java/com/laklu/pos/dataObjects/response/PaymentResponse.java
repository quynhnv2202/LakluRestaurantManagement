package com.laklu.pos.dataObjects.response;

import com.laklu.pos.enums.PaymentMethod;
import com.laklu.pos.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private Integer paymentId;
    private Integer orderId;
    private BigDecimal amountPaid;
    private BigDecimal receivedAmount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;
    private BigDecimal voucherValue;
    private BigDecimal vat;
    private List<OrderItemsResponse> orderItems;
}
