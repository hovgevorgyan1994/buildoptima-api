package com.vecondev.buildoptima.service.impl;

import com.vecondev.buildoptima.dto.request.AuthRequestDto;
import com.vecondev.buildoptima.dto.request.RefreshTokenRequestDto;
import com.vecondev.buildoptima.dto.response.AuthResponseDto;
import com.vecondev.buildoptima.dto.response.RefreshTokenResponseDto;
import com.vecondev.buildoptima.error.AuthErrorCode;
import com.vecondev.buildoptima.exception.AuthException;
import com.vecondev.buildoptima.model.RefreshToken;
import com.vecondev.buildoptima.model.User;
import com.vecondev.buildoptima.repository.RefreshTokenRepository;
import com.vecondev.buildoptima.repository.UserRepository;
import com.vecondev.buildoptima.security.JwtAuthenticationUtil;
import com.vecondev.buildoptima.security.JwtConfigProperties;
import com.vecondev.buildoptima.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service("jwt_auth_service")
@RequiredArgsConstructor
public class JwtAuthService implements AuthService {

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtAuthenticationUtil jwtAuthenticationUtil;
  private final JwtConfigProperties jwtConfigProperties;

  @Override
  @Transactional
  public AuthResponseDto authenticate(final AuthRequestDto authRequestDto) {
    log.info("Request from user {} to get authenticated", authRequestDto.getUsername());
    final User user =
        userRepository
            .findByEmail(authRequestDto.getUsername())
            .orElseThrow(() -> new AuthException(AuthErrorCode.AUTH_BAD_CREDENTIALS));

    if (!passwordEncoder.matches(authRequestDto.getPassword(), user.getPassword())) {
      log.warn("User {} provided wrong credentials", authRequestDto.getUsername());
      throw new AuthException(AuthErrorCode.AUTH_BAD_CREDENTIALS);
    }
    return buildAuthDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto request) {
    log.info("Request to refresh the access token");

    final RefreshToken refreshToken =
        refreshTokenRepository
            .findById(UUID.fromString(request.getRefreshTokenId()))
            .orElseThrow(() -> new AuthException(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID));

    if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
      log.warn("Refresh token is expired");
      throw new AuthException(AuthErrorCode.AUTH_REFRESH_TOKEN_EXPIRED);
    }

    User user =
        userRepository
            .findById(refreshToken.getUserId())
            .orElseThrow(() -> new AuthException(AuthErrorCode.AUTH_CREDENTIALS_NOT_FOUND));

    log.info("New access token is created for user {}", user.getEmail());
    return RefreshTokenResponseDto.builder().accessToken(generateAccessToken(user)).build();
  }

  private AuthResponseDto buildAuthDto(final User user) {
    log.info("User {} provided credentials to receive an access token", user.getEmail());
    final String accessToken = generateAccessToken(user);
    final RefreshToken refreshToken;
    Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByUserId(user.getId());
    if (optionalRefreshToken.isEmpty()) {
      refreshToken = createRefreshToken(user.getId());
    } else if (optionalRefreshToken.get().getExpiresAt().isBefore(LocalDateTime.now())) {
      refreshToken = updateRefreshToken(user.getId());
    } else {
      refreshToken = optionalRefreshToken.get();
    }
    log.info("Access token was created for user {}", user.getEmail());
    return AuthResponseDto.builder()
        .accessToken(accessToken)
        .refreshTokenId(refreshToken.getId().toString())
        .build();
  }

  private RefreshToken createRefreshToken(UUID userId) {
    final String refreshTokenPlain = UUID.randomUUID().toString();
    final String refreshTokenEncoded = passwordEncoder.encode(refreshTokenPlain);
    final RefreshToken refreshToken =
        RefreshToken.builder()
            .userId(userId)
            .refreshToken(refreshTokenEncoded)
            .expiresAt(
                LocalDateTime.now()
                    .plus(jwtConfigProperties.getRefreshToken().getValidity(), ChronoUnit.MONTHS))
            .build();
    refreshTokenRepository.save(refreshToken);
    return refreshToken;
  }

  private RefreshToken updateRefreshToken(UUID userId) {
    final String refreshTokenPlain = UUID.randomUUID().toString();
    final String refreshTokenEncoded = passwordEncoder.encode(refreshTokenPlain);
    LocalDateTime expiresAt =
        LocalDateTime.now()
            .plus(jwtConfigProperties.getRefreshToken().getValidity(), ChronoUnit.MONTHS);
    return refreshTokenRepository.updateRefreshToken(refreshTokenEncoded, expiresAt, userId);
  }

  private String generateAccessToken(User user) {
    final Authentication authentication =
        new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
    return jwtAuthenticationUtil.generateAccessToken(authentication);
  }
}
