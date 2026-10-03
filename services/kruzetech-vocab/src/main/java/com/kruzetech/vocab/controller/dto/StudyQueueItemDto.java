package com.kruzetech.vocab.controller.dto;

import com.kruzetech.vocab.entity.Meaning;
import java.util.List;

public record StudyQueueItemDto(
        String cardId,
        String term,
        String phonetic,
        String audioUrl,
        int masteryLevel,
        String state,
        int lapsesCount,
        boolean isLeech,
        List<Meaning> meanings,
        CardExerciseDto currentExercise) {}
