package com.kruzetech.auth.controller;

import com.kruzetech.auth.controller.dto.AuthDtos.AuthResponse;
import com.kruzetech.auth.controller.dto.AuthDtos.LoginRequest;
import com.kruzetech.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "dev-auth", description = "Development & Swagger Testing (Email/Password Login)")
@RestController
@RequestMapping("/auth")
public class DevAuthController {

    private final AuthService authService;

    public DevAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login with Email & Password (Dành riêng cho Swagger / Test)",
            description = "Endpoint đăng nhập nhanh bằng email và mật khẩu trực tiếp trên Swagger mà không cần qua Firebase.")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }
}
