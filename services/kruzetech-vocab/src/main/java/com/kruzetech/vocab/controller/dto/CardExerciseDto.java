package com.kruzetech.vocab.controller.dto;

import java.util.List;

public record CardExerciseDto(
        String id,
        String cardId,
        String exerciseType,
        String meaningHint,
        String targetSentence,
        String vietnameseTranslation,
        List<String> tokens,
        List<String> distractorTokens,
        int targetIndex,
        String audioUrl) {}
