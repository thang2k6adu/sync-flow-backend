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
            @Email String email, @Size(min = 6) String password, String firstName, String lastName,
            String avatar) {}

    public record UpdateProgressionRequest(
            Integer expGained,
            Boolean cardStudied,
            Boolean wordMastered,
            Boolean correctExercise) {}

    public record ProgressionResponse(
            int level,
            int currentExp,
            int totalExp,
            int expToNextLevel,
            int streak,
            int wordsMastered,
            int totalReviews,
            String rankTitle,
            Instant lastStudyDate) {

        public static ProgressionResponse from(User u) {
            return new ProgressionResponse(
                    Math.max(u.getLevel(), 1),
                    Math.max(u.getCurrentExp(), 0),
                    Math.max(u.getTotalExp(), 0),
                    Math.max(u.getLevel(), 1) * 100,
                    Math.max(u.getStreak(), 0),
                    Math.max(u.getWordsMastered(), 0),
                    Math.max(u.getTotalReviews(), 0),
                    u.rankTitle(),
                    u.getLastStudyDate());
        }
    }

    public record LeaderboardEntryResponse(
            int rank,
            String id,
            String name,
            String avatar,
            int exp,
            int masteredWords,
            int streak,
            String rankTitle,
            boolean isCurrentUser) {}

    public record LeaderboardResponse(
            java.util.List<LeaderboardEntryResponse> topThree,
            LeaderboardEntryResponse myStanding,
            java.util.List<LeaderboardEntryResponse> restList,
            int totalMembers) {}

    public record UserResponse(
            String id,
            String email,
            String firstName,
            String lastName,
            String name,
            String avatar,
            String role,
            boolean isActive,
            int level,
            int currentExp,
            int totalExp,
            int streak,
            int wordsMastered,
            int totalReviews,
            String rankTitle,
            Instant createdAt,
            Instant updatedAt) {

        public static UserResponse from(User u) {
            return new UserResponse(
                    u.getId(),
                    u.getEmail(),
                    u.getFirstName(),
                    u.getLastName(),
                    u.displayName(),
                    u.getAvatar(),
                    u.getRole().name(),
                    u.isActive(),
                    Math.max(u.getLevel(), 1),
                    Math.max(u.getCurrentExp(), 0),
                    Math.max(u.getTotalExp(), 0),
                    Math.max(u.getStreak(), 0),
                    Math.max(u.getWordsMastered(), 0),
                    Math.max(u.getTotalReviews(), 0),
                    u.rankTitle(),
                    u.getCreatedAt(),
                    u.getUpdatedAt());
        }
    }
}
