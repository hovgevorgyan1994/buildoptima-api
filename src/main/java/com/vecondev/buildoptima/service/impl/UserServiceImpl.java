package com.vecondev.buildoptima.service.impl;

import com.vecondev.buildoptima.dto.request.AuthRequestDto;
import com.vecondev.buildoptima.dto.request.RefreshTokenRequestDto;
import com.vecondev.buildoptima.dto.request.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.request.UsersViewRequest;
import com.vecondev.buildoptima.dto.response.AuthResponseDto;
import com.vecondev.buildoptima.dto.response.RefreshTokenResponseDto;
import com.vecondev.buildoptima.dto.response.UserResponseDto;
import com.vecondev.buildoptima.dto.response.UsersViewResponse;
import com.vecondev.buildoptima.error.AuthErrorCode;
import com.vecondev.buildoptima.exception.AuthException;
import com.vecondev.buildoptima.mapper.user.UserMapper;
import com.vecondev.buildoptima.model.user.ConfirmationToken;
import com.vecondev.buildoptima.model.user.RefreshToken;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.repository.RefreshTokenRepository;
import com.vecondev.buildoptima.repository.UserRepository;
import com.vecondev.buildoptima.security.JwtConfigProperties;
import com.vecondev.buildoptima.security.JwtTokenManager;
import com.vecondev.buildoptima.service.ConfirmationTokenService;
import com.vecondev.buildoptima.service.UserService;
import com.vecondev.buildoptima.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenManager jwtAuthenticationUtil;
  private final JwtConfigProperties jwtConfigProperties;
  private final UserMapper userMapper;
  private final UserValidator userValidator;
  private final ConfirmationTokenService confirmationTokenService;
  private final MailService mailService;

  @Override
  @SneakyThrows
  public UserResponseDto register(UserRegistrationRequestDto dto, Locale locale) {
    User user = userMapper.mapToEntity(dto);
    userValidator.validateUserRegistration(user);
    user = userRepository.save(user);
    ConfirmationToken confirmationToken = confirmationTokenService.create(user);
    log.info("New user registered.");
    mailService.send(locale, confirmationToken);
    log.info("Verification email was sent to user {}", user.getEmail());
    return userMapper.mapToResponseDto(user);
  }

  @Override
  public UserResponseDto activate(String token) {
    ConfirmationToken confirmationToken = confirmationTokenService.getByToken(token);
    if (!isValid(confirmationToken)) {
      log.warn("The email confirmation token is not valid");
      throw new AuthException(
          AuthErrorCode.AUTH_CONFIRM_TOKEN_NOT_FOUND,
          AuthErrorCode.AUTH_CONFIRM_TOKEN_NOT_FOUND.getMessage());
    }
    return activateUserAccount(confirmationToken);
  }

  @Override
  @Transactional
  public AuthResponseDto authenticate(final AuthRequestDto authRequestDto) {
    log.info("Request from user {} to get authenticated", authRequestDto.getUsername());
    final User user =
        userRepository
            .findByEmail(authRequestDto.getUsername())
            .orElseThrow(
                () ->
                    new AuthException(
                        AuthErrorCode.AUTH_BAD_CREDENTIALS,
                        AuthErrorCode.AUTH_BAD_CREDENTIALS.getMessage()));

    if (!passwordEncoder.matches(authRequestDto.getPassword(), user.getPassword())) {
      log.warn("User {} provided wrong credentials", authRequestDto.getUsername());
      throw new AuthException(
          AuthErrorCode.AUTH_BAD_CREDENTIALS, AuthErrorCode.AUTH_BAD_CREDENTIALS.getMessage());
    }
    return buildAuthDto(user);
  }

  @Override
  @Transactional
  public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto request) {
    log.info("Request to refresh the access token");

    final RefreshToken refreshToken =
        refreshTokenRepository
            .findById(UUID.fromString(request.getRefreshTokenId()))
            .orElseThrow(
                () ->
                    new AuthException(
                        AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID,
                        AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID.getMessage()));

    if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
      log.warn("Refresh token is expired");
      throw new AuthException(
          AuthErrorCode.AUTH_REFRESH_TOKEN_EXPIRED,
          AuthErrorCode.AUTH_REFRESH_TOKEN_EXPIRED.getMessage());
    }

    User user =
        userRepository
            .findById(refreshToken.getUserId())
            .orElseThrow(
                () ->
                    new AuthException(
                        AuthErrorCode.AUTH_CREDENTIALS_NOT_FOUND,
                        AuthErrorCode.AUTH_CREDENTIALS_NOT_FOUND.getMessage()));

    refreshTokenRepository.deleteById(refreshToken.getId());
    final RefreshToken newRefreshToken = createRefreshToken(user.getId());
    log.info("New access token is created for user {}", user.getEmail());
    return RefreshTokenResponseDto.builder()
        .accessToken(generateAccessToken(user))
        .refreshTokenId(newRefreshToken.getId().toString())
        .build();
  }

  @Override
  public UsersViewResponse getUsers(UsersViewRequest viewRequest) {
    Sort sort =
        viewRequest.getSortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
            ? Sort.by(viewRequest.getSortBy()).ascending()
            : Sort.by(viewRequest.getSortBy()).descending();

    Pageable pageable = PageRequest.of(viewRequest.getPageNo(), viewRequest.getPageSize(), sort);

    Page<User> users = userRepository.findAll(pageable);

    List<UserResponseDto> content =
        users.getContent().stream()
            .map(userMapper::mapToResponseDto)
            .toList();

    return UsersViewResponse.builder()
        .content(content)
        .pageNo(users.getNumber())
        .pageSize(users.getSize())
        .totalElements(users.getTotalElements())
        .totalPages(users.getTotalPages())
        .last(users.isLast())
        .build();
  }

  private UserResponseDto activateUserAccount(ConfirmationToken confirmationToken) {
    User user = userRepository.getReferenceById(confirmationToken.getUser().getId());
    user.setEnabled(true);
    confirmationTokenService.remove(user.getId());
    log.info("User {} account was verified by email", user.getEmail());
    return userMapper.mapToResponseDto(user);
  }

  private boolean isValid(ConfirmationToken confirmationToken) {
    Optional<User> user = userRepository.findById(confirmationToken.getUser().getId());
    return user.isPresent() && user.get().getEnabled().equals(false);
  }

  private AuthResponseDto buildAuthDto(final User user) {
    log.info("User {} provided credentials to receive an access token", user.getEmail());
    final String accessToken = generateAccessToken(user);
    final RefreshToken refreshToken;
    Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByUserId(user.getId());
    if (optionalRefreshToken.isEmpty()) {
      refreshToken = createRefreshToken(user.getId());
    } else if (optionalRefreshToken.get().getExpiresAt().isBefore(LocalDateTime.now())) {
      refreshTokenRepository.deleteById(optionalRefreshToken.get().getId());
      refreshToken = createRefreshToken(user.getId());
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

  private String generateAccessToken(User user) {
    final Authentication authentication =
        new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
    return jwtAuthenticationUtil.generateAccessToken(authentication);
  }
}