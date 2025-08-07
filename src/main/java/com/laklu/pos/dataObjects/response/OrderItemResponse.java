package com.laklu.pos.dataObjects.response;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderItemResponse {
    private Integer orderItemId;
    private Integer orderId;
    private Integer menuItemId;
    private DishResponse dish;
    private int quantity;
    private String statusLabel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
