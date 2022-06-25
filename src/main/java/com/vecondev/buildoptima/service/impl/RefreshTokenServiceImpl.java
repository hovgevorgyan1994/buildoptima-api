package com.vecondev.buildoptima.service.impl;

import com.vecondev.buildoptima.config.properties.JwtConfigProperties;
import com.vecondev.buildoptima.error.ErrorCode;
import com.vecondev.buildoptima.exception.AuthenticationException;
import com.vecondev.buildoptima.model.user.RefreshToken;
import com.vecondev.buildoptima.repository.RefreshTokenRepository;
import com.vecondev.buildoptima.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

  private final JwtConfigProperties jwtConfigProperties;
  private final RefreshTokenRepository refreshTokenRepository;

  @Override
  public RefreshToken create(UUID userId) {
    final String refreshTokenPlain = UUID.randomUUID().toString();
    final RefreshToken refreshToken =
        RefreshToken.builder()
            .userId(userId)
            .refreshToken(refreshTokenPlain)
            .expiresAt(
                LocalDateTime.now().plusDays(jwtConfigProperties.getRefreshToken().getValidity()))
            .build();
    refreshTokenRepository.save(refreshToken);
    return refreshToken;
  }

  @Override
  public void deleteById(UUID id) {
    refreshTokenRepository.deleteById(id);
  }

  @Override
  public RefreshToken findByUserId(UUID id) {
    return refreshTokenRepository.findByUserId(id).orElse(null);
  }

  @Override
  public RefreshToken findByRefreshToken(String refreshToken) {
    return refreshTokenRepository
        .findByRefreshToken(refreshToken)
        .orElseThrow(() -> new AuthenticationException(ErrorCode.REFRESH_TOKEN_INVALID));
  }

  @Override
  @Scheduled(cron = "0 */12 * * * *")
  public void deleteExpiredOnes() {
    refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    log.info("Expired refresh tokens have been removed");
  }
}
