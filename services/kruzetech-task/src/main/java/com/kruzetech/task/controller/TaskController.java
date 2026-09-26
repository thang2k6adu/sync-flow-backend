package com.kruzetech.task.controller;

import com.kruzetech.task.controller.dto.TaskDtos.CreateTaskRequest;
import com.kruzetech.task.controller.dto.TaskDtos.MessageResponse;
import com.kruzetech.task.controller.dto.TaskDtos.TaskResponse;
import com.kruzetech.task.controller.dto.TaskDtos.UpdateTaskRequest;
import com.kruzetech.task.core.PageResponse;
import com.kruzetech.task.entity.TaskStatus;
import com.kruzetech.task.security.AuthUser;
import com.kruzetech.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
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

@Tag(name = "tasks")
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a task")
    public TaskResponse create(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody CreateTaskRequest req) {
        return taskService.create(user, req);
    }

    @GetMapping
    @Operation(summary = "List my tasks (ADMIN: all tasks)")
    public PageResponse<TaskResponse> list(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String search) {
        return taskService.list(user, page, limit, status, search);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a task")
    public TaskResponse get(@AuthenticationPrincipal AuthUser user, @PathVariable String id) {
        return taskService.get(user, id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a task")
    public TaskResponse update(
            @AuthenticationPrincipal AuthUser user, @PathVariable String id, @Valid @RequestBody UpdateTaskRequest req) {
        return taskService.update(user, id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    public MessageResponse delete(@AuthenticationPrincipal AuthUser user, @PathVariable String id) {
        taskService.delete(user, id);
        return new MessageResponse("Task deleted successfully");
    }
}
