package com.kruzetech.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO của module auth; tên field khớp với nest-boilerplate để flutter_boilerplate gọi được nguyên trạng. */
public final class AuthDtos {

    private AuthDtos() {}

    public record RegisterRequest(
            @Schema(example = "user@example.com") @NotBlank @Email String email,
            @Schema(example = "password123") @NotBlank @Size(min = 6) String password,
            @Schema(example = "John") String firstName,
            @Schema(example = "Doe") String lastName) {}

    public record RefreshTokenRequest(@NotBlank String refreshToken) {}

    public record FirebaseLoginRequest(
            @Schema(description = "Firebase ID token from client") @NotBlank String idToken,
            @Schema(description = "Device identifier (optional)", example = "web_chrome_123") String deviceId,
            @Schema(description = "web, ios, android (optional)", example = "web") String platform) {}

    public record LogoutRequest(String refreshToken) {}

    public record UserInfo(
            String id, String email, String firstName, String lastName, String avatar, String role) {}

    public record Tokens(String accessToken, String refreshToken, long expiresIn) {}

    /** Dùng chung cho register / firebase login. */
    public record AuthResponse(UserInfo user, Tokens tokens) {}

    public record MessageResponse(String message) {}
}
