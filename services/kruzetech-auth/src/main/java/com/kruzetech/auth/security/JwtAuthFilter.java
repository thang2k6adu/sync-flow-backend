package com.kruzetech.auth.security;

import com.kruzetech.auth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Đọc Bearer token, nạp user từ DB (như JwtStrategy.validate bên Nest). Token sai/hết hạn thì bỏ qua,
 * việc trả 401 do lớp authorize quyết định, nên endpoint public vẫn chạy được dù client gửi kèm token cũ.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            jwtService.parseAccess(header.substring(7)).ifPresent(claims -> userRepository
                    .findById(claims.getSubject())
                    .filter(u -> u.isActive())
                    .ifPresent(u -> {
                        AuthUser principal =
                                new AuthUser(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(), u.getRole());
                        var auth = new UsernamePasswordAuthenticationToken(
                                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }));
        }
        chain.doFilter(req, res);
    }
}
