package com.kruzetech.auth.repository;

import com.kruzetech.auth.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByFirebaseUid(String firebaseUid);

    boolean existsByEmail(String email);

    @Query("""
            select u from User u
            where lower(u.email) like lower(concat('%', :search, '%'))
               or lower(coalesce(u.firstName, '')) like lower(concat('%', :search, '%'))
               or lower(coalesce(u.lastName, '')) like lower(concat('%', :search, '%'))
            """)
    Page<User> search(@Param("search") String search, Pageable pageable);
}
