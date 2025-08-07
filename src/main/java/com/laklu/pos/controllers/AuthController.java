package com.laklu.pos.controllers;

import com.laklu.pos.auth.AuthService;
import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.request.LoginRequest;
import com.laklu.pos.dataObjects.response.AuthUserResponse;
import com.laklu.pos.dataObjects.response.TokenResponse;
import com.laklu.pos.valueObjects.UserCredentials;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Đăng nhập")
    @PostMapping("/login")
    public ApiResponseEntity login(@RequestBody @Validated LoginRequest loginRequest) {
        UserCredentials credentials = new UserCredentials(loginRequest.getUsername(), loginRequest.getPassword());
        return ApiResponseEntity.success(new TokenResponse(authService.login(credentials)), "Đăng nhập thành công");
    }

    @Operation(summary = "Lấy thông tin người đang đăng nhập")
    @GetMapping("/me")
    public ApiResponseEntity me() {
        return ApiResponseEntity.success(new AuthUserResponse(JwtGuard.userPrincipal()));
    }
}
