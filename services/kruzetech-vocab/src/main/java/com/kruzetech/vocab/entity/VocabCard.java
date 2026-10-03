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
@Table(name = "vocab_cards")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabCard {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "deck_id", nullable = false, length = 36)
    private String deckId;

    @Column(nullable = false, length = 100)
    private String term;

    @Column(length = 100)
    private String phonetic;

    @Column(name = "audio_url", length = 255)
    private String audioUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meanings", columnDefinition = "jsonb", nullable = false)
    private List<Meaning> meanings;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "collocations", columnDefinition = "jsonb")
    private List<String> collocations;

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
