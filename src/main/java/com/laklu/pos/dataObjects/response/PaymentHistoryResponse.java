package com.laklu.pos.dataObjects.response;

import com.laklu.pos.enums.PaymentType;
import com.laklu.pos.enums.TransferType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentHistoryResponse {
    private Integer id;
    private Integer paymentId;
    private PaymentType paymentType;
    private TransferType transferType;
    private LocalDateTime transactionDate;
    private BigDecimal amount;
} 