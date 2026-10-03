package com.kruzetech.vocab.controller.dto;

import java.util.List;

public record StudyQueueResponse(
        long totalDue,
        List<StudyQueueItemDto> queue) {}
