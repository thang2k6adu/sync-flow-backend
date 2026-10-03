package com.kruzetech.vocab.service;

import com.kruzetech.vocab.entity.UserCardProgress;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
public class SrsEngine {

    public enum Rating {
        AGAIN,
        HARD,
        GOOD,
        EASY;

        public static Rating fromString(String val) {
            if (val == null) return null;
            return switch (val.trim().toLowerCase()) {
                case "again" -> AGAIN;
                case "hard" -> HARD;
                case "good" -> GOOD;
                case "easy" -> EASY;
                default -> null;
            };
        }
    }

    @Data
    @Builder
    public static class Telemetry {
        private int timeSpentMs;
        private int mistakesCount;
        private boolean usedHint;
        private Rating manualRating;
    }

    @Data
    @Builder
    public static class SrsResult {
        private Rating rating;
        private int newLevel;
        private BigDecimal newEaseFactor;
        private int newIntervalDays;
        private int newRepetitionCount;
        private int newLapsesCount;
        private Instant newDueDate;
        private boolean isLeech;
    }

    private final Random random = new Random();

    /**
     * Tự động ánh xạ telemetry sang Rating (SM-2 Rating).
     */
    public Rating evaluateTelemetry(Telemetry t) {
        if (t.getManualRating() != null) {
            return t.getManualRating();
        }
        if (t.getMistakesCount() >= 2) {
            return Rating.AGAIN;
        }
        if (t.getMistakesCount() == 1 || t.isUsedHint()) {
            return Rating.HARD;
        }
        if (t.getTimeSpentMs() > 0 && t.getTimeSpentMs() <= 3000 && t.getMistakesCount() == 0) {
            return Rating.EASY;
        }
        return Rating.GOOD;
    }

    /**
     * Tính toán chu kỳ và trạng thái tiếp theo của thẻ học theo thuật toán Modified SM-2.
     */
    public SrsResult calculateNextState(UserCardProgress progress, Telemetry telemetry) {
        Rating rating = evaluateTelemetry(telemetry);

        BigDecimal ef = progress.getEaseFactor() != null ? progress.getEaseFactor() : new BigDecimal("2.50");
        if (ef.compareTo(new BigDecimal("1.30")) < 0) {
            ef = new BigDecimal("2.50");
        }

        int currentLevel = progress.getMasteryLevel() > 0 ? progress.getMasteryLevel() : 1;
        int currentInterval = progress.getIntervalDays();
        int currentReps = progress.getRepetitionCount();
        int currentLapses = progress.getLapsesCount();

        int newLevel;
        BigDecimal newEf;
        int newInterval;
        int newReps;
        int newLapses = currentLapses;
        Instant now = Instant.now();
        Instant newDueDate;

        switch (rating) {
            case AGAIN -> {
                newEf = ef.subtract(new BigDecimal("0.20")).max(new BigDecimal("1.30"));
                newInterval = 0;
                newReps = 0;
                newLevel = 1;
                newLapses = currentLapses + 1;
                newDueDate = now.plus(10, ChronoUnit.MINUTES);
            }
            case HARD -> {
                newEf = ef.subtract(new BigDecimal("0.15")).max(new BigDecimal("1.30"));
                if (currentInterval == 0) {
                    newInterval = 1;
                } else {
                    newInterval = Math.max(1, (int) Math.round(currentInterval * 1.20));
                }
                newReps = currentReps + 1;
                newLevel = currentLevel;
                newDueDate = now.plus(newInterval, ChronoUnit.DAYS);
            }
            case GOOD -> {
                newEf = ef;
                if (currentReps == 0) {
                    newInterval = 1;
                } else if (currentReps == 1) {
                    newInterval = 6;
                } else {
                    newInterval = (int) Math.round(currentInterval * ef.doubleValue());
                }
                newInterval = applyFuzz(newInterval);
                newReps = currentReps + 1;
                newLevel = Math.min(3, currentLevel + 1);
                newDueDate = now.plus(newInterval, ChronoUnit.DAYS);
            }
            case EASY -> {
                newEf = ef.add(new BigDecimal("0.15")).min(new BigDecimal("3.50"));
                if (currentReps == 0) {
                    newInterval = 4;
                } else {
                    newInterval = (int) Math.round(currentInterval * ef.doubleValue() * 1.30);
                }
                newInterval = applyFuzz(newInterval);
                newReps = currentReps + 1;
                newLevel = 3;
                newDueDate = now.plus(newInterval, ChronoUnit.DAYS);
            }
            default -> throw new IllegalStateException("Unexpected rating: " + rating);
        }

        return SrsResult.builder()
                .rating(rating)
                .newLevel(newLevel)
                .newEaseFactor(newEf.setScale(2, RoundingMode.HALF_UP))
                .newIntervalDays(newInterval)
                .newRepetitionCount(newReps)
                .newLapsesCount(newLapses)
                .newDueDate(newDueDate)
                .isLeech(newLapses >= 5)
                .build();
    }

    /**
     * Thêm độ lệch ngẫu nhiên +/- 5% khi interval >= 3 để chống hiện tượng dồn toa (clustering).
     */
    private int applyFuzz(int interval) {
        if (interval < 3) {
            return interval;
        }
        double fuzzRange = interval * 0.05;
        double delta = (random.nextDouble() * 2 - 1) * fuzzRange;
        int res = (int) Math.round(interval + delta);
        return Math.max(1, res);
    }
}
