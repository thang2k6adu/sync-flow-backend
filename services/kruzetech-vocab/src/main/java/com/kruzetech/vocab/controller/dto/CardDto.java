package com.kruzetech.vocab.controller.dto;

import com.kruzetech.vocab.entity.Meaning;
import java.util.List;

public record CardDto(
        String id,
        String deckId,
        String term,
        String phonetic,
        String audioUrl,
        List<Meaning> meanings,
        List<String> collocations,
        List<CardExerciseDto> exercises) {}
