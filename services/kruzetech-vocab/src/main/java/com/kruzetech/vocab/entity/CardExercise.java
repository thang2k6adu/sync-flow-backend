package com.kruzetech.vocab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "card_exercises")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardExercise {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "card_id", nullable = false, length = 36)
    private String cardId;

    @Column(name = "exercise_type", nullable = false, length = 50)
    private String exerciseType;

    @Column(name = "meaning_hint", length = 255)
    private String meaningHint;

    @Column(name = "target_sentence", nullable = false, columnDefinition = "text")
    private String targetSentence;

    @Column(name = "vietnamese_translation", nullable = false, columnDefinition = "text")
    private String vietnameseTranslation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tokens", columnDefinition = "jsonb", nullable = false)
    private List<String> tokens;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "distractor_tokens", columnDefinition = "jsonb")
    private List<String> distractorTokens;

    @Column(name = "target_index", nullable = false)
    private int targetIndex;

    @Column(name = "audio_url", length = 255)
    private String audioUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
