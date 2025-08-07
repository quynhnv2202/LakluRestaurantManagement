package com.laklu.pos.dataObjects.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawRequest {
    
    @NotNull(message = "Số tiền rút không được để trống")
    @Positive(message = "Số tiền rút phải lớn hơn 0")
    private BigDecimal amount;
    
    private String notes;
} 