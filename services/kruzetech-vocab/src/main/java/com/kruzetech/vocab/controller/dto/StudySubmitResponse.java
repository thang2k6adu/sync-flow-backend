package com.kruzetech.vocab.controller.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record StudySubmitResponse(
        String cardId,
        String evaluatedRating,
        int previousLevel,
        int newLevel,
        int previousInterval,
        int newInterval,
        BigDecimal easeFactor,
        Instant dueDate,
        boolean isLeech) {}
