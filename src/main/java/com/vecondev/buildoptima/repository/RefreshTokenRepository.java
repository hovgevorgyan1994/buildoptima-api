package com.vecondev.buildoptima.repository;

import com.vecondev.buildoptima.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  @Query(
      "UPDATE RefreshToken r SET "
          + "r.refreshToken = :refreshToken, "
          + "r.expiresAt = :expiresAt "
          + "WHERE r.userId = :userId")
  RefreshToken updateRefreshToken(
      @Param("refreshToken") String refreshToken,
      @Param("expiresAt") LocalDateTime expiresAt,
      @Param("userId") UUID userId);

  Optional<RefreshToken> findByUserId(UUID userID);
}
