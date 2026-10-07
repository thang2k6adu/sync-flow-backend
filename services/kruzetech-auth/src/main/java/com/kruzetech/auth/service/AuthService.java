package com.kruzetech.auth.service;

import com.kruzetech.auth.controller.dto.AuthDtos.AuthResponse;
import com.kruzetech.auth.controller.dto.AuthDtos.FirebaseLoginRequest;
import com.kruzetech.auth.controller.dto.AuthDtos.LoginRequest;
import com.kruzetech.auth.controller.dto.AuthDtos.RegisterRequest;
import com.kruzetech.auth.controller.dto.AuthDtos.Tokens;
import com.kruzetech.auth.controller.dto.AuthDtos.UserInfo;
import com.kruzetech.auth.core.exception.ApiException;
import com.kruzetech.auth.entity.RefreshToken;
import com.kruzetech.auth.entity.User;
import com.kruzetech.auth.repository.RefreshTokenRepository;
import com.kruzetech.auth.repository.UserRepository;
import com.kruzetech.auth.security.JwtService;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final JwtService jwt;
    private final PasswordEncoder passwordEncoder;
    private final FirebaseService firebase;

    public AuthService(
            UserRepository users,
            RefreshTokenRepository refreshTokens,
            JwtService jwt,
            PasswordEncoder passwordEncoder,
            FirebaseService firebase) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.jwt = jwt;
        this.passwordEncoder = passwordEncoder;
        this.firebase = firebase;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (users.existsByEmail(req.email())) {
            throw ApiException.conflict("User with this email already exists");
        }
        User user = new User();
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        return issue(users.save(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = users.findByEmail(req.email())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));

        if (!user.isActive()) {
            throw ApiException.forbidden("User is disabled");
        }

        if (user.getPassword() == null || !passwordEncoder.matches(req.password(), user.getPassword())) {
            throw ApiException.unauthorized("Invalid email or password");
        }

        user.setLastLogin(Instant.now());
        user = users.save(user);

        return issue(user);
    }

    @Transactional
    public AuthResponse firebaseLogin(FirebaseLoginRequest req) {
        if (StringUtils.hasText(req.deviceId()) || StringUtils.hasText(req.platform())) {
            log.info("[Firebase Login] Device info: deviceId={} platform={}", req.deviceId(), req.platform());
        }

        FirebaseService.Identity identity = firebase.verifyIdToken(req.idToken());
        if (!StringUtils.hasText(identity.email())) {
            throw ApiException.unauthorized("Email is required");
        }

        User user = users.findByEmail(identity.email())
                .or(() -> users.findByFirebaseUid(identity.uid()))
                .orElseGet(User::new);

        String first = null;
        String last = null;
        if (StringUtils.hasText(identity.name())) {
            String[] parts = identity.name().trim().split("\\s+", 2);
            first = parts[0];
            last = parts.length > 1 ? parts[1] : null;
        }

        if (user.getId() == null) {
            user.setEmail(identity.email());
            user.setFirebaseUid(identity.uid());
            user.setFirstName(first);
            user.setLastName(last);
            user.setAvatar(identity.picture());
        } else {
            if (user.getFirebaseUid() == null) {
                user.setFirebaseUid(identity.uid());
            }
            if (identity.picture() != null && user.getAvatar() == null) {
                user.setAvatar(identity.picture());
            }
            if (first != null && (user.getFirstName() == null || user.getLastName() == null)) {
                user.setFirstName(first);
                if (last != null) {
                    user.setLastName(last);
                }
            }
            user.setLastLogin(Instant.now());
        }
        user = users.save(user);

        if (!user.isActive()) {
            throw ApiException.forbidden("User is disabled");
        }
        return issue(user);
    }

    /** Xoay refresh token: kiểm tra chữ ký + bản ghi DB, xoá token cũ, cấp cặp mới. */
    @Transactional
    public Tokens refresh(String refreshToken) {
        if (!jwt.isValidRefresh(refreshToken)) {
            throw ApiException.unauthorized("Invalid refresh token");
        }
        RefreshToken record = refreshTokens
                .findByTokenWithUser(refreshToken)
                .filter(r -> r.getExpiresAt().isAfter(Instant.now()))
                .filter(r -> r.getUser().isActive())
                .orElseThrow(() -> ApiException.unauthorized("Invalid refresh token"));

        User user = record.getUser();
        refreshTokens.delete(record);
        refreshTokens.flush();
        return issue(user).tokens();
    }

    @Transactional
    public void logout(String userId, String refreshToken) {
        if (StringUtils.hasText(refreshToken)) {
            refreshTokens.deleteByUserIdAndToken(userId, refreshToken);
        } else {
            refreshTokens.deleteAllByUserId(userId);
        }
    }

    private AuthResponse issue(User user) {
        String access = jwt.createAccessToken(user);
        String refresh = jwt.createRefreshToken(user);

        RefreshToken record = new RefreshToken();
        record.setToken(refresh);
        record.setUser(user);
        record.setExpiresAt(jwt.refreshExpiry());
        refreshTokens.save(record);

        return new AuthResponse(
                new UserInfo(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getAvatar(),
                        user.getRole().name()),
                new Tokens(access, refresh, jwt.accessExpiresInSeconds()));
    }
}
