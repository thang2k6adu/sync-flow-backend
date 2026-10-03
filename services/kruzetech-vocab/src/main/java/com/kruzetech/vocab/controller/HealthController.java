package com.kruzetech.vocab.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Healthcheck endpoint")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public Map<String, Object> health() {
        return Map.of(
                "status", "ok",
                "service", "kruzetech-vocab",
                "timestamp", Instant.now().toString());
    }
}
