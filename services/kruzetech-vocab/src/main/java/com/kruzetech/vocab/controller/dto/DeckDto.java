package com.kruzetech.vocab.controller.dto;

import java.time.Instant;

public record DeckDto(
        String id,
        String userId,
        String name,
        String description,
        String category,
        String iconUrl,
        String cefrLevel,
        Instant createdAt) {}
