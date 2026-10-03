package com.kruzetech.vocab.repository;

import com.kruzetech.vocab.entity.CardExercise;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardExerciseRepository extends JpaRepository<CardExercise, String> {

    List<CardExercise> findByCardIdOrderByTargetIndexAsc(String cardId);

    List<CardExercise> findByCardIdIn(Collection<String> cardIds);
}
