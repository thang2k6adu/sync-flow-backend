package com.kruzetech.auth.controller;

import com.kruzetech.auth.controller.dto.AuthDtos.AuthResponse;
import com.kruzetech.auth.controller.dto.AuthDtos.FirebaseLoginRequest;
import com.kruzetech.auth.controller.dto.AuthDtos.LoginRequest;
import com.kruzetech.auth.controller.dto.AuthDtos.LogoutRequest;
import com.kruzetech.auth.controller.dto.AuthDtos.MessageResponse;
import com.kruzetech.auth.controller.dto.AuthDtos.RefreshTokenRequest;
import com.kruzetech.auth.controller.dto.AuthDtos.RegisterRequest;
import com.kruzetech.auth.controller.dto.AuthDtos.Tokens;
import com.kruzetech.auth.security.AuthUser;
import com.kruzetech.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "auth")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/firebase/login")
    @Operation(
            summary = "Login with Firebase ID token",
            description = "deviceId and platform are optional and can be omitted for testing.")
    public AuthResponse firebaseLogin(@Valid @RequestBody FirebaseLoginRequest req) {
        return authService.firebaseLogin(req);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public Tokens refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return authService.refresh(req.refreshToken());
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public MessageResponse logout(
            @AuthenticationPrincipal AuthUser user, @RequestBody(required = false) LogoutRequest body) {
        authService.logout(user.id(), body != null ? body.refreshToken() : null);
        return new MessageResponse("Logged out successfully");
    }
}
