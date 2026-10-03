package com.kruzetech.vocab.controller.dto;

import com.kruzetech.vocab.entity.Meaning;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateCardRequest(
        @NotBlank String deckId,
        @NotBlank String term,
        String phonetic,
        String audioUrl,
        @NotEmpty List<@Valid Meaning> meanings,
        List<String> collocations,
        List<@Valid CreateExerciseRequest> exercises) {}
