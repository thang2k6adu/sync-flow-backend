package com.kruzetech.auth.service;

import com.kruzetech.auth.controller.dto.UserDtos.CreateUserRequest;
import com.kruzetech.auth.controller.dto.UserDtos.UpdateUserRequest;
import com.kruzetech.auth.controller.dto.UserDtos.UserResponse;
import com.kruzetech.auth.core.PageResponse;
import com.kruzetech.auth.core.exception.ApiException;
import com.kruzetech.auth.entity.User;
import com.kruzetech.auth.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse create(CreateUserRequest req) {
        if (users.existsByEmail(req.email())) {
            throw ApiException.conflict("User with this email already exists");
        }
        User user = new User();
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        return UserResponse.from(users.save(user));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(int page, int limit, String search) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 1) - 1, limit > 0 ? limit : 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = StringUtils.hasText(search) ? users.search(search.trim(), pageable) : users.findAll(pageable);
        return PageResponse.of(result, UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserResponse findOne(String id) {
        return UserResponse.from(getOrThrow(id));
    }

    @Transactional
    public UserResponse update(String id, UpdateUserRequest req) {
        User user = getOrThrow(id);
        if (req.email() != null && !req.email().equals(user.getEmail())) {
            if (users.existsByEmail(req.email())) {
                throw ApiException.conflict("User with this email already exists");
            }
            user.setEmail(req.email());
        }
        if (req.firstName() != null) {
            user.setFirstName(req.firstName());
        }
        if (req.lastName() != null) {
            user.setLastName(req.lastName());
        }
        if (StringUtils.hasText(req.password())) {
            user.setPassword(passwordEncoder.encode(req.password()));
        }
        return UserResponse.from(users.saveAndFlush(user));
    }

    @Transactional
    public void remove(String id) {
        users.delete(getOrThrow(id));
    }

    private User getOrThrow(String id) {
        return users.findById(id).orElseThrow(() -> ApiException.notFound("User not found"));
    }
}
