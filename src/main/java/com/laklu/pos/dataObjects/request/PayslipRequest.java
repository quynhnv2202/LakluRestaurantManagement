package com.laklu.pos.dataObjects.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class PayslipRequest {
    @NotNull(message = "Mã nhân viên không được để trống")
    private Integer staffId;

    @NotBlank(message = "Tháng lương không được để trống")
    private String salaryMonth;
}
