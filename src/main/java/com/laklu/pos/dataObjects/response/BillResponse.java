package com.laklu.pos.dataObjects.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class BillResponse {
    private int orderId;
    private String tableNumber;
    private LocalDateTime date;
    private LocalDateTime timeIn;
    private LocalDateTime timeOut;
    private List<OrderItemsResponse> orderItems;
    private BigDecimal TotalAmount;
    private BigDecimal receivedAmount;
    private BigDecimal voucherValue;
    private BigDecimal change;
}
