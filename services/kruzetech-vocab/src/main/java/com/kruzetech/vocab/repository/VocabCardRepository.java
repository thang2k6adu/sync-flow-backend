package com.kruzetech.vocab.repository;

import com.kruzetech.vocab.entity.VocabCard;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VocabCardRepository extends JpaRepository<VocabCard, String> {

    List<VocabCard> findByDeckIdOrderByCreatedAtAsc(String deckId);

    @Query("SELECT c FROM VocabCard c WHERE c.deckId = :deckId ORDER BY c.createdAt ASC")
    List<VocabCard> findCardsByDeckPaginated(@Param("deckId") String deckId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM VocabCard c WHERE c.deckId = :deckId")
    long countCardsByDeck(@Param("deckId") String deckId);

    List<VocabCard> findByIdIn(Collection<String> ids);

    @Query("SELECT c FROM VocabCard c WHERE c.deckId = :deckId AND NOT EXISTS ("
            + "SELECT 1 FROM UserCardProgress p WHERE p.id.userId = :userId AND p.id.cardId = c.id"
            + ") ORDER BY c.createdAt ASC")
    List<VocabCard> findUnstudiedCardsByDeck(
            @Param("userId") String userId,
            @Param("deckId") String deckId,
            Pageable pageable);

    @Query("SELECT c FROM VocabCard c JOIN Deck d ON c.deckId = d.id "
            + "WHERE (d.userId = :userId OR d.userId = 'system') AND NOT EXISTS ("
            + "SELECT 1 FROM UserCardProgress p WHERE p.id.userId = :userId AND p.id.cardId = c.id"
            + ") ORDER BY c.createdAt ASC")
    List<VocabCard> findUnstudiedCards(
            @Param("userId") String userId,
            Pageable pageable);
}
