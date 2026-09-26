package com.kruzetech.auth.security;

import com.kruzetech.auth.config.AppProperties;
import com.kruzetech.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/** Ký / kiểm tra JWT access + refresh (HS256, hai secret riêng như Nest). */
@Service
public class JwtService {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JwtService(AppProperties props) {
        AppProperties.Jwt jwt = props.jwt();
        this.accessKey = toKey(jwt.secret(), "JWT_SECRET");
        this.refreshKey = toKey(jwt.refreshSecret(), "JWT_REFRESH_SECRET");
        this.accessTtl = jwt.expiresIn();
        this.refreshTtl = jwt.refreshExpiresIn();
    }

    private static SecretKey toKey(String secret, String name) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(name + " must be set and at least 32 characters long");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(User user) {
        return build(user, accessKey, accessTtl);
    }

    public String createRefreshToken(User user) {
        return build(user, refreshKey, refreshTtl);
    }

    public long accessExpiresInSeconds() {
        return accessTtl.toSeconds();
    }

    public Instant refreshExpiry() {
        return Instant.now().plus(refreshTtl);
    }

    /** Trả về claims nếu access token hợp lệ, ngược lại empty. */
    public Optional<Claims> parseAccess(String token) {
        return parse(token, accessKey);
    }

    public boolean isValidRefresh(String token) {
        return parse(token, refreshKey).isPresent();
    }

    private String build(User user, SecretKey key, Duration ttl) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // jti: tránh hai refresh token trùng nhau trong cùng một giây
                .subject(user.getId())
                .claim("id", user.getId())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    private Optional<Claims> parse(String token, SecretKey key) {
        try {
            return Optional.of(
                    Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
