package com.laklu.pos.dataObjects.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DishSummaryResponse {
    private Long totalDishSold;
    private Long totalDishTypes;
} 