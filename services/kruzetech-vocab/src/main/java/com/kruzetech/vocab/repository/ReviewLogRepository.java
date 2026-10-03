package com.kruzetech.vocab.repository;

import com.kruzetech.vocab.entity.ReviewLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewLogRepository extends JpaRepository<ReviewLog, String> {

    List<ReviewLog> findByUserIdAndCardIdOrderByReviewedAtDesc(String userId, String cardId);
}
