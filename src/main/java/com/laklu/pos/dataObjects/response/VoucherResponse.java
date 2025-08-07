package com.laklu.pos.dataObjects.response;

import com.laklu.pos.enums.DiscountType;
import com.laklu.pos.enums.VoucherStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoucherResponse {
    private Integer id;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private VoucherStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
