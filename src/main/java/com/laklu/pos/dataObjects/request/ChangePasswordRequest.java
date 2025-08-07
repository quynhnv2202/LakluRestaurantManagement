package com.laklu.pos.dataObjects.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotNull
    private String currentPassword;
    
    @NotNull
    private String newPassword;
} 