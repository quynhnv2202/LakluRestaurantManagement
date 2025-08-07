package com.laklu.pos.dataObjects.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSplitRequest {
    private Integer orderId;
    private List<OrderItemSplitRequest> orderItems;
}
