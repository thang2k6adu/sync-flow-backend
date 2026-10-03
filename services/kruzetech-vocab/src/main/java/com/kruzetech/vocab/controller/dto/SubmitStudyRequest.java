package com.kruzetech.vocab.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record SubmitStudyRequest(
        @NotBlank String cardId,
        int masteryLevel,
        int timeSpentMs,
        int mistakesCount,
        boolean usedHint,
        String manualRating) {}
