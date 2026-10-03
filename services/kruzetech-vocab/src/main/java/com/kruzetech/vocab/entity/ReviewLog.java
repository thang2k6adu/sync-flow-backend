package com.kruzetech.vocab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "review_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewLog {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "card_id", nullable = false, length = 36)
    private String cardId;

    @Column(nullable = false, length = 20)
    private String rating;

    @Column(name = "interval_before", nullable = false)
    private int intervalBefore;

    @Column(name = "interval_after", nullable = false)
    private int intervalAfter;

    @Column(name = "time_spent_ms", nullable = false)
    private int timeSpentMs;

    @Column(name = "mistakes_count", nullable = false)
    private int mistakesCount;

    @Column(name = "used_hint", nullable = false)
    private boolean usedHint;

    @Column(name = "reviewed_at", nullable = false, updatable = false)
    private Instant reviewedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (reviewedAt == null) {
            reviewedAt = Instant.now();
        }
    }
}
