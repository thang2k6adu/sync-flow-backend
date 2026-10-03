package com.kruzetech.vocab.repository;

import com.kruzetech.vocab.entity.VocabCard;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VocabCardRepository extends JpaRepository<VocabCard, String> {

    List<VocabCard> findByDeckIdOrderByCreatedAtAsc(String deckId);

    List<VocabCard> findByIdIn(Collection<String> ids);
}
