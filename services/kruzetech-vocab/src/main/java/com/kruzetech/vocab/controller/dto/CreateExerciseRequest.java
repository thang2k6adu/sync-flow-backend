package com.kruzetech.vocab.controller.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateExerciseRequest(
        @NotBlank String exerciseType,
        String meaningHint,
        @NotBlank String targetSentence,
        @NotBlank String vietnameseTranslation,
        List<String> tokens,
        List<String> distractorTokens,
        int targetIndex,
        String audioUrl) {}
