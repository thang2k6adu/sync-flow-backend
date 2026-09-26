package com.kruzetech.task.service;

import com.kruzetech.task.controller.dto.TaskDtos.CreateTaskRequest;
import com.kruzetech.task.controller.dto.TaskDtos.TaskResponse;
import com.kruzetech.task.controller.dto.TaskDtos.UpdateTaskRequest;
import com.kruzetech.task.core.PageResponse;
import com.kruzetech.task.core.exception.ApiException;
import com.kruzetech.task.entity.Task;
import com.kruzetech.task.entity.TaskStatus;
import com.kruzetech.task.repository.TaskRepository;
import com.kruzetech.task.security.AuthUser;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** User thường chỉ thao tác task của mình; ADMIN thao tác được mọi task. Task của người khác trả 404 để không lộ sự tồn tại. */
@Service
public class TaskService {

    private final TaskRepository tasks;

    public TaskService(TaskRepository tasks) {
        this.tasks = tasks;
    }

    @Transactional
    public TaskResponse create(AuthUser user, CreateTaskRequest req) {
        Task t = new Task();
        t.setTitle(req.title().trim());
        t.setDescription(req.description());
        if (req.status() != null) {
            t.setStatus(req.status());
        }
        t.setDueDate(req.dueDate());
        t.setUserId(user.id());
        return TaskResponse.from(tasks.save(t));
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> list(AuthUser user, int page, int limit, TaskStatus status, String search) {
        List<Specification<Task>> specs = new ArrayList<>();
        if (!user.isAdmin()) {
            specs.add((r, q, cb) -> cb.equal(r.get("userId"), user.id()));
        }
        if (status != null) {
            specs.add((r, q, cb) -> cb.equal(r.get("status"), status));
        }
        if (StringUtils.hasText(search)) {
            String like = "%" + search.trim().toLowerCase() + "%";
            specs.add((r, q, cb) -> cb.or(
                    cb.like(cb.lower(r.get("title")), like),
                    cb.like(cb.lower(cb.coalesce(r.<String>get("description"), "")), like)));
        }
        Specification<Task> spec = Specification.allOf(specs);
        var pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.of(tasks.findAll(spec, pageable), TaskResponse::from);
    }

    @Transactional(readOnly = true)
    public TaskResponse get(AuthUser user, String id) {
        return TaskResponse.from(getAccessible(user, id));
    }

    @Transactional
    public TaskResponse update(AuthUser user, String id, UpdateTaskRequest req) {
        Task t = getAccessible(user, id);
        if (req.title() != null) {
            t.setTitle(req.title().trim());
        }
        if (req.description() != null) {
            t.setDescription(req.description());
        }
        if (req.status() != null) {
            t.setStatus(req.status());
        }
        if (req.dueDate() != null) {
            t.setDueDate(req.dueDate());
        }
        return TaskResponse.from(tasks.saveAndFlush(t));
    }

    @Transactional
    public void delete(AuthUser user, String id) {
        tasks.delete(getAccessible(user, id));
    }

    private Task getAccessible(AuthUser user, String id) {
        return tasks.findById(id)
                .filter(t -> user.isAdmin() || t.getUserId().equals(user.id()))
                .orElseThrow(() -> ApiException.notFound("Task not found"));
    }
}
