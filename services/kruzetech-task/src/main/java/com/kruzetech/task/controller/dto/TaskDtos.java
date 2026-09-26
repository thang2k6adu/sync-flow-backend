package com.kruzetech.task.controller.dto;

import com.kruzetech.task.entity.Task;
import com.kruzetech.task.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class TaskDtos {

    private TaskDtos() {}

    public record CreateTaskRequest(
            @NotBlank @Size(max = 200) String title, String description, TaskStatus status, Instant dueDate) {}

    /** Mọi field đều tuỳ chọn; field null thì giữ nguyên. */
    public record UpdateTaskRequest(
            @Size(min = 1, max = 200) String title, String description, TaskStatus status, Instant dueDate) {}

    public record TaskResponse(
            String id,
            String title,
            String description,
            String status,
            Instant dueDate,
            String userId,
            Instant createdAt,
            Instant updatedAt) {

        public static TaskResponse from(Task t) {
            return new TaskResponse(
                    t.getId(),
                    t.getTitle(),
                    t.getDescription(),
                    t.getStatus().name(),
                    t.getDueDate(),
                    t.getUserId(),
                    t.getCreatedAt(),
                    t.getUpdatedAt());
        }
    }

    public record MessageResponse(String message) {}
}
