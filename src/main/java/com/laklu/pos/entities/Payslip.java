package com.laklu.pos.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payslip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
    private User staff;

    @Column(name = "salary_month", nullable = false)
    private String salaryMonth;

    @Column(name = "total_working_days", nullable = false)
    private int totalWorkingDays;

    @Column(name = "total_working_hours", nullable = false, columnDefinition = "DOUBLE DEFAULT 0.0")
    private double totalWorkingHours;

    @Column(name = "total_salary", nullable = false)
    private BigDecimal totalSalary;

    @Column(name = "late_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int lateCount;

    @Column(name = "late_hours", nullable = false, columnDefinition = "DOUBLE DEFAULT 0.0")
    private double lateHours;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @PrePersist
    public void OnCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public YearMonth getSalaryMonth() {
        return YearMonth.parse(salaryMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    public void setSalaryMonth(YearMonth salaryMonth) {
        this.salaryMonth = salaryMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }
}