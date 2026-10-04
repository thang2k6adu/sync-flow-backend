package com.kruzetech.vocab.repository;

import com.kruzetech.vocab.entity.CardTag;
import com.kruzetech.vocab.entity.CardTagId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardTagRepository extends JpaRepository<CardTag, CardTagId> {
    List<CardTag> findByCardId(String cardId);
    List<CardTag> findByCardIdIn(Collection<String> cardIds);
    void deleteByCardId(String cardId);
}
