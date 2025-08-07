package com.laklu.pos.dataObjects.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MonthlyRevenueResponse {
    private int year;                // Năm thống kê
    private int month;               // Tháng thống kê
    private BigDecimal totalRevenue; // Tổng doanh thu
    private LocalDateTime startDate; // Ngày bắt đầu của tháng
    private LocalDateTime endDate;   // Ngày kết thúc của tháng
}
