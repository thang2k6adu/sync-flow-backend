package com.kruzetech.vocab.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kruzetech.vocab.entity.UserCardProgress;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SrsEngineTest {

    private SrsEngine srsEngine;

    @BeforeEach
    void setUp() {
        srsEngine = new SrsEngine();
    }

    @Test
    @DisplayName("Telemetry: 2 lỗi trở lên tự động xếp loại AGAIN")
    void testTelemetryAgainOnMistakes() {
        SrsEngine.Telemetry telemetry = SrsEngine.Telemetry.builder()
                .timeSpentMs(4000)
                .mistakesCount(2)
                .usedHint(false)
                .build();
        assertEquals(SrsEngine.Rating.AGAIN, srsEngine.evaluateTelemetry(telemetry));
    }

    @Test
    @DisplayName("Telemetry: 1 lỗi hoặc có bấm gợi ý xếp loại HARD")
    void testTelemetryHardOnHintOrOneMistake() {
        SrsEngine.Telemetry t1 = SrsEngine.Telemetry.builder()
                .timeSpentMs(4000)
                .mistakesCount(1)
                .usedHint(false)
                .build();
        assertEquals(SrsEngine.Rating.HARD, srsEngine.evaluateTelemetry(t1));

        SrsEngine.Telemetry t2 = SrsEngine.Telemetry.builder()
                .timeSpentMs(2000)
                .mistakesCount(0)
                .usedHint(true)
                .build();
        assertEquals(SrsEngine.Rating.HARD, srsEngine.evaluateTelemetry(t2));
    }

    @Test
    @DisplayName("Telemetry: Đúng 100% và thời gian <= 3000ms xếp loại EASY")
    void testTelemetryEasyOnFastSpeed() {
        SrsEngine.Telemetry telemetry = SrsEngine.Telemetry.builder()
                .timeSpentMs(2500)
                .mistakesCount(0)
                .usedHint(false)
                .build();
        assertEquals(SrsEngine.Rating.EASY, srsEngine.evaluateTelemetry(telemetry));
    }

    @Test
    @DisplayName("Telemetry: Đúng 100% thời gian tiêu chuẩn xếp loại GOOD")
    void testTelemetryGoodOnNormalSpeed() {
        SrsEngine.Telemetry telemetry = SrsEngine.Telemetry.builder()
                .timeSpentMs(4500)
                .mistakesCount(0)
                .usedHint(false)
                .build();
        assertEquals(SrsEngine.Rating.GOOD, srsEngine.evaluateTelemetry(telemetry));
    }

    @Test
    @DisplayName("SRS SM-2: Xử lý nhánh AGAIN - Phạt EF, reset interval về 0, giáng cấp Level 1, tăng lapses")
    void testCalculateAgainBranch() {
        UserCardProgress progress = UserCardProgress.builder()
                .masteryLevel(2)
                .easeFactor(new BigDecimal("2.50"))
                .intervalDays(6)
                .repetitionCount(2)
                .lapsesCount(0)
                .dueDate(Instant.now())
                .build();

        SrsEngine.Telemetry telemetry = SrsEngine.Telemetry.builder()
                .manualRating(SrsEngine.Rating.AGAIN)
                .build();

        SrsEngine.SrsResult result = srsEngine.calculateNextState(progress, telemetry);

        assertEquals(SrsEngine.Rating.AGAIN, result.getRating());
        assertEquals(1, result.getNewLevel());
        assertEquals(new BigDecimal("2.30"), result.getNewEaseFactor());
        assertEquals(0, result.getNewIntervalDays());
        assertEquals(0, result.getNewRepetitionCount());
        assertEquals(1, result.getNewLapsesCount());
        assertFalse(result.isLeech());
    }

    @Test
    @DisplayName("SRS SM-2: Xử lý Leech Card khi số lần quên >= 1")
    void testLeechCardDetection() {
        UserCardProgress progress = UserCardProgress.builder()
                .masteryLevel(1)
                .easeFactor(new BigDecimal("1.70"))
                .intervalDays(1)
                .repetitionCount(1)
                .lapsesCount(0)
                .dueDate(Instant.now())
                .build();

        SrsEngine.Telemetry telemetry = SrsEngine.Telemetry.builder()
                .manualRating(SrsEngine.Rating.AGAIN)
                .build();

        SrsEngine.SrsResult result = srsEngine.calculateNextState(progress, telemetry);

        assertEquals(1, result.getNewLapsesCount());
        assertTrue(result.isLeech());
    }

    @Test
    @DisplayName("SRS SM-2: Nhánh GOOD chuỗi đầu tiên (Rep 0 -> 1 ngày, Rep 1 -> 6 ngày)")
    void testCalculateGoodBranchGraduation() {
        // Lần đầu nhớ
        UserCardProgress p0 = UserCardProgress.builder()
                .masteryLevel(1)
                .easeFactor(new BigDecimal("2.50"))
                .intervalDays(0)
                .repetitionCount(0)
                .lapsesCount(0)
                .dueDate(Instant.now())
                .build();

        SrsEngine.Telemetry t = SrsEngine.Telemetry.builder().manualRating(SrsEngine.Rating.GOOD).build();
        SrsEngine.SrsResult r0 = srsEngine.calculateNextState(p0, t);
        assertEquals(1, r0.getNewIntervalDays());
        assertEquals(2, r0.getNewLevel());
        assertEquals(1, r0.getNewRepetitionCount());

        // Lần 2 nhớ
        UserCardProgress p1 = UserCardProgress.builder()
                .masteryLevel(2)
                .easeFactor(new BigDecimal("2.50"))
                .intervalDays(1)
                .repetitionCount(1)
                .lapsesCount(0)
                .dueDate(Instant.now())
                .build();

        SrsEngine.SrsResult r1 = srsEngine.calculateNextState(p1, t);
        // Interval = 6 (có thể dao động chút do fuzz)
        assertTrue(r1.getNewIntervalDays() >= 5 && r1.getNewIntervalDays() <= 7);
        assertEquals(3, r1.getNewLevel());
        assertEquals(2, r1.getNewRepetitionCount());
    }

    @Test
    @DisplayName("SRS SM-2: Nhánh EASY - Thưởng EF, nhảy thẳng Level 3")
    void testCalculateEasyBranch() {
        UserCardProgress progress = UserCardProgress.builder()
                .masteryLevel(1)
                .easeFactor(new BigDecimal("2.50"))
                .intervalDays(0)
                .repetitionCount(0)
                .lapsesCount(0)
                .dueDate(Instant.now())
                .build();

        SrsEngine.Telemetry t = SrsEngine.Telemetry.builder().manualRating(SrsEngine.Rating.EASY).build();
        SrsEngine.SrsResult result = srsEngine.calculateNextState(progress, t);

        assertEquals(3, result.getNewLevel());
        assertEquals(new BigDecimal("2.65"), result.getNewEaseFactor());
        // Đối với Rep 0, Easy interval = 4 (+/- fuzz)
        assertTrue(result.getNewIntervalDays() >= 3 && result.getNewIntervalDays() <= 5);
        assertEquals(1, result.getNewRepetitionCount());
    }
}
