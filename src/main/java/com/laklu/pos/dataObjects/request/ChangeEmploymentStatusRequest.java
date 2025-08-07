package com.laklu.pos.dataObjects.request;

import com.laklu.pos.enums.EmploymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeEmploymentStatusRequest {
    
    @NotNull(message = "Trạng thái làm việc không được để trống")
    private EmploymentStatus employmentStatus;
} 