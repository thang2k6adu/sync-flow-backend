package com.kruzetech.vocab.security;

import com.kruzetech.vocab.entity.Role;

/** Danh tính lấy thẳng từ claim của access token do kruzetech-auth cấp (không truy vấn DB). */
public record AuthUser(String id, String email, Role role) {

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
