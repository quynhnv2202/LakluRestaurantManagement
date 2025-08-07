package com.laklu.pos.dataObjects.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class HourlyTopSellingDishResponse {
    private Integer hour;
    private List<TopSellingDishResponse> topDishes;
    
    // Constructor để tạo từ giờ và danh sách TopSellingDishResponse
    public static HourlyTopSellingDishResponse of(Integer hour, List<TopSellingDishResponse> topDishes) {
        return new HourlyTopSellingDishResponse(hour, topDishes);
    }
} 