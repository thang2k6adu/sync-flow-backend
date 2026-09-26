package com.kruzetech.auth.security;

import com.kruzetech.auth.entity.Role;

/** Principal đặt vào SecurityContext; dùng qua @AuthenticationPrincipal (tương ứng @CurrentUser bên Nest). */
public record AuthUser(String id, String email, String firstName, String lastName, Role role) {}
