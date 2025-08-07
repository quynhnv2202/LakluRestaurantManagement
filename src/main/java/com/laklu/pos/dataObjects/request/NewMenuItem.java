package com.laklu.pos.dataObjects.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class NewMenuItem {
    private Integer dishId;
    private Integer menuId;
    private Long categoryId;
    private BigDecimal price;
    private Boolean isActive = true;
}