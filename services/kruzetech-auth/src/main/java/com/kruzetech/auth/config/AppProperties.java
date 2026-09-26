package com.kruzetech.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Firebase firebase, Cors cors, Seed seed, boolean swaggerEnabled) {

    public record Jwt(String secret, String refreshSecret, Duration expiresIn, Duration refreshExpiresIn) {}

    public record Firebase(String projectId, String clientEmail, String privateKey) {}

    public record Cors(String origins) {}

    public record Seed(String adminEmail, String adminPassword) {}
}
