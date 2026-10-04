package com.kruzetech.vocab.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDeckRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        @Size(max = 50) String category,
        @Size(max = 255) String iconUrl,
        @Size(max = 10) String cefrLevel) {}
