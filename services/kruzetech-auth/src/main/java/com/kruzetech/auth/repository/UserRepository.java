package com.kruzetech.auth.repository;

import com.kruzetech.auth.entity.User;
import java.util.List;
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

    /** Bảng xếp hạng toàn cục: sort theo tổng EXP giảm dần, rồi tới từ đã thuộc. */
    @Query("""
            select u from User u
            where u.active = true
            order by u.totalExp desc, u.wordsMastered desc, u.createdAt asc
            """)
    List<User> findLeaderboard(Pageable pageable);

    @Query("select count(u) from User u where u.active = true")
    long countActiveUsers();

    @Query("""
            select count(u) from User u
            where u.active = true
              and (u.totalExp > :totalExp
                   or (u.totalExp = :totalExp and u.wordsMastered > :wordsMastered)
                   or (u.totalExp = :totalExp and u.wordsMastered = :wordsMastered and u.createdAt < :createdAt))
            """)
    long countUsersAhead(
            @Param("totalExp") int totalExp,
            @Param("wordsMastered") int wordsMastered,
            @Param("createdAt") java.time.Instant createdAt);
}
