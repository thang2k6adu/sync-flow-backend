package com.kruzetech.vocab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "card_tags")
@IdClass(CardTagId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTag {

    @Id
    @Column(name = "card_id", length = 36)
    private String cardId;

    @Id
    @Column(name = "tag_id", length = 36)
    private String tagId;
}
