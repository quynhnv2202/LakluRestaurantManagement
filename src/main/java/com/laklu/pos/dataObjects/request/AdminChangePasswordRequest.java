package com.laklu.pos.dataObjects.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminChangePasswordRequest {
    @NotNull
    private String newPassword;
} 