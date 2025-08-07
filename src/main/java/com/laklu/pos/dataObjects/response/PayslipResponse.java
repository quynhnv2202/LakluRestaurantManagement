package com.laklu.pos.dataObjects.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@AllArgsConstructor
public class PayslipResponse {
    private Integer payslipId;
    private Integer staffId;
    private String staffName;
    private YearMonth salaryMonth;
    private Integer totalWorkingDays;
    private Double totalWorkingHours;
    private BigDecimal totalSalary;
    private Integer lateCount;
    private Double lateHours;
}
