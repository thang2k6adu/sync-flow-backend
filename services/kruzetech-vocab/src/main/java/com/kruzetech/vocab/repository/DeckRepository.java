package com.kruzetech.vocab.repository;

import com.kruzetech.vocab.entity.Deck;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeckRepository extends JpaRepository<Deck, String> {

    List<Deck> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<Deck> findByIdAndUserId(String id, String userId);

    @Query("SELECT d FROM Deck d WHERE d.userId = :userId OR d.userId = 'system' ORDER BY (CASE WHEN d.userId = 'system' THEN 0 ELSE 1 END) ASC, d.createdAt DESC")
    List<Deck> findAccessibleDecks(@Param("userId") String userId);

    @Query("SELECT d FROM Deck d WHERE d.id = :id AND (d.userId = :userId OR d.userId = 'system')")
    Optional<Deck> findAccessibleById(@Param("id") String id, @Param("userId") String userId);
}
