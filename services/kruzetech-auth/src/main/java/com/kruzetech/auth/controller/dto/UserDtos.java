package com.kruzetech.auth.controller.dto;

import com.kruzetech.auth.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class UserDtos {

    private UserDtos() {}

    public record CreateUserRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6) String password,
            String firstName,
            String lastName) {}

    /** Mọi field đều tuỳ chọn; field null thì giữ nguyên giá trị cũ (như Prisma undefined). */
    public record UpdateUserRequest(
            @Email String email, @Size(min = 6) String password, String firstName, String lastName) {}

    public record UserResponse(
            String id,
            String email,
            String firstName,
            String lastName,
            String avatar,
            String role,
            boolean isActive,
            Instant createdAt,
            Instant updatedAt) {

        public static UserResponse from(User u) {
            return new UserResponse(
                    u.getId(),
                    u.getEmail(),
                    u.getFirstName(),
                    u.getLastName(),
                    u.getAvatar(),
                    u.getRole().name(),
                    u.isActive(),
                    u.getCreatedAt(),
                    u.getUpdatedAt());
        }
    }
}
