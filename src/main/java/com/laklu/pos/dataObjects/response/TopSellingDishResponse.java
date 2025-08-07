package com.laklu.pos.dataObjects.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class TopSellingDishResponse {
    private Integer dishId;
    private String dishName;
    private String dishDescription;
    private BigDecimal dishPrice;
    private Long totalQuantity;
}
