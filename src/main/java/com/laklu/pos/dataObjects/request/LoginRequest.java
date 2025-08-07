package com.laklu.pos.dataObjects.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequest {
    @NotNull
    public String username;
    @NotNull
    public String password;
}
