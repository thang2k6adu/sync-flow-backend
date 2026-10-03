package com.kruzetech.vocab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_card_progress")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCardProgress {

    @EmbeddedId
    private UserCardProgressId id;

    @Column(nullable = false, length = 20)
    private String state;

    @Column(name = "mastery_level", nullable = false)
    private int masteryLevel;

    @Column(name = "ease_factor", nullable = false, precision = 4, scale = 2)
    private BigDecimal easeFactor;

    @Column(name = "interval_days", nullable = false)
    private int intervalDays;

    @Column(name = "repetition_count", nullable = false)
    private int repetitionCount;

    @Column(name = "last_exercise_index", nullable = false)
    private int lastExerciseIndex;

    @Column(name = "due_date", nullable = false)
    private Instant dueDate;

    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;

    @Column(name = "lapses_count", nullable = false)
    private int lapsesCount;

    @PrePersist
    public void prePersist() {
        if (state == null) {
            state = "new";
        }
        if (masteryLevel <= 0) {
            masteryLevel = 1;
        }
        if (easeFactor == null) {
            easeFactor = new BigDecimal("2.50");
        }
        if (dueDate == null) {
            dueDate = Instant.now();
        }
    }
}
