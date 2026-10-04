package com.kruzetech.vocab.repository;

import com.kruzetech.vocab.entity.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, String> {
    List<Tag> findByUserIdOrUserIdIsNullOrderByNameAsc(String userId);
    Optional<Tag> findByNameAndUserId(String name, String userId);
}
