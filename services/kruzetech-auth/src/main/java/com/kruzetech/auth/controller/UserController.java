package com.kruzetech.auth.controller;

import com.kruzetech.auth.controller.dto.AuthDtos.MessageResponse;
import com.kruzetech.auth.controller.dto.UserDtos.CreateUserRequest;
import com.kruzetech.auth.controller.dto.UserDtos.UpdateUserRequest;
import com.kruzetech.auth.controller.dto.UserDtos.UserResponse;
import com.kruzetech.auth.core.PageResponse;
import com.kruzetech.auth.security.AuthUser;
import com.kruzetech.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "users")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new user (Admin only)")
    public UserResponse create(@Valid @RequestBody CreateUserRequest req) {
        return userService.create(req);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    @Operation(summary = "Get all users with pagination")
    public PageResponse<UserResponse> findAll(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit,
            @RequestParam(required = false) String search) {
        return userService.findAll(page, limit, search);
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile")
    public UserResponse profile(@AuthenticationPrincipal AuthUser user) {
        return userService.findOne(user.id());
    }

    @PatchMapping("/profile")
    @Operation(summary = "Update current user profile")
    public UserResponse updateProfile(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody UpdateUserRequest req) {
        return userService.update(user.id(), req);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    @Operation(summary = "Get user by id")
    public UserResponse findOne(@PathVariable String id) {
        return userService.findOne(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user (Admin only)")
    public UserResponse update(@PathVariable String id, @Valid @RequestBody UpdateUserRequest req) {
        return userService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user (Admin only)")
    public MessageResponse remove(@PathVariable String id) {
        userService.remove(id);
        return new MessageResponse("User deleted successfully");
    }
}
