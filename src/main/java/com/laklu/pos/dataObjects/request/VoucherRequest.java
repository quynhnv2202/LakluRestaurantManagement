package com.laklu.pos.dataObjects.request;

import com.laklu.pos.enums.DiscountType;
import com.laklu.pos.enums.VoucherStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoucherRequest {
    @NotBlank(message = "Mã voucher không được để trống")
    @Size(max = 50, message = "Mã voucher không được vượt quá 50 ký tự")
    private String code;

    @NotNull(message = "Loại Voucher không được để trống")
    private DiscountType discountType;

    @NotNull(message = "Giá trị Voucher không được để trống")
    @DecimalMin(value = "0.0", message = "Giá trị giảm giá phải lớn hơn 0")
    private BigDecimal discountValue;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime validFrom;

    @NotNull(message = "Ngày hết hạn không được để trống")
    private LocalDateTime validUntil;

    @NotNull(message = "Trạng thái voucher không được để trống")
    private VoucherStatus status;
}
