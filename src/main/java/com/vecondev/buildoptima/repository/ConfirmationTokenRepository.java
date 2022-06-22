package com.vecondev.buildoptima.repository;

import com.vecondev.buildoptima.model.user.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, UUID> {

  Optional<ConfirmationToken> findByToken(String token);

  void deleteByUserId(UUID userId);

  void deleteByExpiresAtBefore(LocalDateTime now);
}
