package com.laklu.pos.dataObjects.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CashRegisterSummaryResponse {
    private LocalDate date;
    private BigDecimal totalInitialAmount;
    private BigDecimal totalCurrentAmount;
    private BigDecimal diffAmount;
    private int totalRegisters;
    private List<CashRegisterResponse> registers;
} 