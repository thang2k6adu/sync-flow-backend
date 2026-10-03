package com.kruzetech.vocab.controller;

import com.kruzetech.vocab.controller.dto.StudyQueueResponse;
import com.kruzetech.vocab.controller.dto.StudySubmitResponse;
import com.kruzetech.vocab.controller.dto.SubmitStudyRequest;
import com.kruzetech.vocab.security.AuthUser;
import com.kruzetech.vocab.service.StudyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "study", description = "Hàng đợi ôn tập SRS & Gửi telemetry bài tập")
@RestController
@RequestMapping("/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    @GetMapping("/queue")
    @Operation(summary = "Lấy danh sách các thẻ từ vựng tới hạn ôn tập (phân bổ bài tập xoay vòng Round-Robin)")
    public StudyQueueResponse getQueue(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(required = false) String deckId,
            @RequestParam(defaultValue = "20") int limit) {
        return studyService.getStudyQueue(user.id(), deckId, limit);
    }

    @PostMapping("/submit")
    @Operation(summary = "Gửi kết quả bài tập (telemetry) và tính toán chu kỳ SRS mới")
    public StudySubmitResponse submit(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody SubmitStudyRequest request) {
        return studyService.submitStudy(user.id(), request);
    }
}
