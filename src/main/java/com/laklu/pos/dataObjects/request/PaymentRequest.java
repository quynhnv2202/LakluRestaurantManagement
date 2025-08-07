package com.laklu.pos.dataObjects.request;

import com.laklu.pos.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    @NotNull(message = "Order ID không được để trống!")
    private Integer orderId;

    @NotNull(message = "Phương thức thanh toán không được để trống!")
    private PaymentMethod paymentMethod;

    private String voucherCode;

    @DecimalMin(value = "0.0", inclusive = true, message = "VAT không được nhỏ hơn 0!")
    private BigDecimal vat;
}
