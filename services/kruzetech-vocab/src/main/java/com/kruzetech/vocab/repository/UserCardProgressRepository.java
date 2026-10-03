package com.kruzetech.vocab.repository;

import com.kruzetech.vocab.entity.UserCardProgress;
import com.kruzetech.vocab.entity.UserCardProgressId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCardProgressRepository extends JpaRepository<UserCardProgress, UserCardProgressId> {

    Optional<UserCardProgress> findByIdUserIdAndIdCardId(String userId, String cardId);

    @Query("SELECT p FROM UserCardProgress p WHERE p.id.userId = :userId AND p.dueDate <= :now ORDER BY p.dueDate ASC")
    List<UserCardProgress> findDueCards(@Param("userId") String userId, @Param("now") Instant now, Pageable pageable);

    @Query("SELECT p FROM UserCardProgress p JOIN VocabCard c ON p.id.cardId = c.id WHERE p.id.userId = :userId AND c.deckId = :deckId AND p.dueDate <= :now ORDER BY p.dueDate ASC")
    List<UserCardProgress> findDueCardsByDeck(
            @Param("userId") String userId,
            @Param("deckId") String deckId,
            @Param("now") Instant now,
            Pageable pageable);

    @Query("SELECT COUNT(p) FROM UserCardProgress p WHERE p.id.userId = :userId AND p.dueDate <= :now")
    long countDueCards(@Param("userId") String userId, @Param("now") Instant now);

    @Query("SELECT COUNT(p) FROM UserCardProgress p JOIN VocabCard c ON p.id.cardId = c.id WHERE p.id.userId = :userId AND c.deckId = :deckId AND p.dueDate <= :now")
    long countDueCardsByDeck(@Param("userId") String userId, @Param("deckId") String deckId, @Param("now") Instant now);
}
