package com.kruzetech.auth.repository;

import com.kruzetech.auth.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    @Query("select t from RefreshToken t join fetch t.user where t.token = :token")
    Optional<RefreshToken> findByTokenWithUser(@Param("token") String token);

    @Modifying
    @Query("delete from RefreshToken t where t.user.id = :userId")
    void deleteAllByUserId(@Param("userId") String userId);

    @Modifying
    @Query("delete from RefreshToken t where t.user.id = :userId and t.token = :token")
    void deleteByUserIdAndToken(@Param("userId") String userId, @Param("token") String token);
}
