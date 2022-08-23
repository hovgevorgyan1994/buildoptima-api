package com.vecondev.buildoptima.repository.user;

import com.vecondev.buildoptima.model.user.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

  Optional<RefreshToken> findByUserId(UUID userID);

  void deleteByExpiresAtBefore(LocalDateTime localDateTime);
}
