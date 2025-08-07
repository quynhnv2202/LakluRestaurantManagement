package com.laklu.pos.dataObjects.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class DailyRevenueResponse {
    private LocalDate date;
    private BigDecimal totalRevenue;
}