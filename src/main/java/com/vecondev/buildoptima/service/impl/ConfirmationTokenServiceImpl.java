package com.vecondev.buildoptima.service.impl;

import com.vecondev.buildoptima.error.AuthErrorCode;
import com.vecondev.buildoptima.exception.AuthException;
import com.vecondev.buildoptima.model.user.ConfirmationToken;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.repository.ConfirmationTokenRepository;

import com.vecondev.buildoptima.service.ConfirmationTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmationTokenServiceImpl implements ConfirmationTokenService {

  private final ConfirmationTokenRepository confirmationTokenRepository;

  @Override
  public ConfirmationToken create(User user) {
    ConfirmationToken confirmationToken =
        ConfirmationToken.builder()
            .token(UUID.randomUUID().toString())
            .expiresAt(LocalDateTime.now().plusDays(1))
            .user(user)
            .build();
    return confirmationTokenRepository.save(confirmationToken);
  }

  @Override
  public ConfirmationToken getByToken(String token) {
    return confirmationTokenRepository
        .findByToken(token)
        .orElseThrow(() -> new AuthException(AuthErrorCode.AUTH_CONFIRM_TOKEN_NOT_FOUND));
  }

  @Override
  public void remove(UUID userId) {
    confirmationTokenRepository.deleteByUserId(userId);
    log.info("Confirmation token was deleted, because user has verified the email");
  }

  @Override
  @Scheduled(cron = "0 */12 * * * *")
  public void removeExpiredTokens() {
    confirmationTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    log.info("Expired confirmation tokens have been removed");
  }
}