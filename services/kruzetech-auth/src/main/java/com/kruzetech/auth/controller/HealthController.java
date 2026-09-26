package com.kruzetech.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "health")
@RestController
@RequestMapping("/health")
public class HealthController {

    private final Instant startTime = Instant.now();
    private final JdbcTemplate jdbc;

    public HealthController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    @Operation(summary = "Health check endpoint")
    public Map<String, Object> check() {
        String database;
        try {
            jdbc.queryForObject("SELECT 1", Integer.class);
            database = "connected";
        } catch (Exception e) {
            database = "disconnected";
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "ok");
        body.put("timestamp", Instant.now().toString());
        body.put("uptime", Instant.now().getEpochSecond() - startTime.getEpochSecond());
        body.put("database", database);
        return body;
    }
}
