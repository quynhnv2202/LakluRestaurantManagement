package com.laklu.pos.dataObjects.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CashRegisterResponse {
    private Integer id;
    private Integer userId;
    private String userFullName;
    private Long scheduleId;
    private BigDecimal initialAmount;
    private BigDecimal currentAmount;
    private LocalDateTime shiftStart;
    private LocalDateTime shiftEnd;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 