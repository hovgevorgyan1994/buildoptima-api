package com.vecondev.buildoptima.service.impl;

import com.vecondev.buildoptima.dto.request.*;
import com.vecondev.buildoptima.dto.response.AuthResponseDto;
import com.vecondev.buildoptima.dto.response.FetchResponse;
import com.vecondev.buildoptima.dto.response.RefreshTokenResponseDto;
import com.vecondev.buildoptima.dto.response.UserResponseDto;
import com.vecondev.buildoptima.error.ApiErrorCode;
import com.vecondev.buildoptima.exception.ApiException;
import com.vecondev.buildoptima.mapper.user.UserMapper;
import com.vecondev.buildoptima.model.user.ConfirmationToken;
import com.vecondev.buildoptima.model.user.RefreshToken;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.repository.RefreshTokenRepository;
import com.vecondev.buildoptima.repository.UserRepository;
import com.vecondev.buildoptima.security.JwtConfigProperties;
import com.vecondev.buildoptima.security.JwtTokenManager;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import com.vecondev.buildoptima.service.ConfirmationTokenService;
import com.vecondev.buildoptima.service.UserService;
import com.vecondev.buildoptima.validation.UserValidator;
import lombok.RequiredArgsConstructor;
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

import javax.mail.MessagingException;
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
  public UserResponseDto register(UserRegistrationRequestDto dto, Locale locale) {
    User user = userMapper.mapToEntity(dto);
    userValidator.validateUserRegistration(user);
    user = userRepository.save(user);
    ConfirmationToken confirmationToken = confirmationTokenService.create(user);
    log.info("New user registered.");
    try {
      mailService.send(locale, confirmationToken);
    } catch (MessagingException e) {
      throw new ApiException(ApiErrorCode.SEND_EMAIL_FAILED.getMessage());
    }
    log.info("Verification email was sent to user {}", user.getEmail());
    return userMapper.mapToResponseDto(user);
  }

  @Override
  public UserResponseDto activate(String token) {
    ConfirmationToken confirmationToken = confirmationTokenService.getByToken(token);
    if (!isValid(confirmationToken)) {
      log.warn("The email confirmation token is not valid");
      throw new ApiException(ApiErrorCode.CONFIRM_TOKEN_NOT_FOUND.getMessage());
    }
    return activateUserAccount(confirmationToken);
  }

  @Override
  public AuthResponseDto authenticate(final AuthRequestDto authRequestDto) {
    log.info("Request from user {} to get authenticated", authRequestDto.getUsername());
    final User user =
        userRepository
            .findByEmail(authRequestDto.getUsername())
            .orElseThrow(() -> new ApiException(ApiErrorCode.BAD_CREDENTIALS.getMessage()));

    if (!passwordEncoder.matches(authRequestDto.getPassword(), user.getPassword())) {
      log.warn("User {} provided wrong credentials", authRequestDto.getUsername());
      throw new ApiException(ApiErrorCode.BAD_CREDENTIALS.getMessage());
    }
    return buildAuthDto(user);
  }

  @Override
  public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto request) {
    log.info("Request to refresh the access token");

    final RefreshToken refreshToken =
        refreshTokenRepository
            .findById(UUID.fromString(request.getRefreshTokenId()))
            .orElseThrow(() -> new ApiException(ApiErrorCode.REFRESH_TOKEN_INVALID.getMessage()));

    if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
      log.warn("Refresh token is expired");
      throw new ApiException(ApiErrorCode.REFRESH_TOKEN_EXPIRED.getMessage());
    }

    User user =
        userRepository
            .findById(refreshToken.getUserId())
            .orElseThrow(() -> new ApiException(ApiErrorCode.CREDENTIALS_NOT_FOUND.getMessage()));

    refreshTokenRepository.deleteById(refreshToken.getId());
    final RefreshToken newRefreshToken = createRefreshToken(user.getId());
    log.info("New access token is created for user {}", user.getEmail());
    return RefreshTokenResponseDto.builder()
        .accessToken(generateAccessToken(user))
        .refreshTokenId(newRefreshToken.getId().toString())
        .build();
  }

  @Override
  public FetchResponse fetchUsers(FetchRequest viewRequest) {
    Sort sort =
        Sort.Direction.ASC.name().equalsIgnoreCase(viewRequest.getSortDir())
            ? Sort.by(viewRequest.getSortBy()).ascending()
            : Sort.by(viewRequest.getSortBy()).descending();

    Pageable pageable = PageRequest.of(viewRequest.getPage(), viewRequest.getSize(), sort);

    Page<User> users = userRepository.findAll(pageable);

    List<UserResponseDto> content = userMapper.mapToResponseList(users);

    return FetchResponse.builder()
        .content(content)
        .page(users.getNumber())
        .size(users.getSize())
        .totalElements(users.getTotalElements())
        .totalPages(users.getTotalPages())
        .last(users.isLast())
        .build();
  }

  @Override
  public void changePassword(ChangePasswordRequest request, AppUserDetails userDetails) {
    log.info("Request from user {} to change the password", userDetails.getUsername());
    User user =
        userRepository
            .findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND.getMessage()));
    if (!isValidRequest(request, user)) {
      log.warn("User {} had provided wrong credentials to change the password", user.getEmail());
      throw new ApiException(ApiErrorCode.PROVIDED_WRONG_PASSWORD.getMessage());
    }
    if (request.getOldPassword().equals(request.getNewPassword())) {
      log.warn("In change password request user {} provided the same password", user.getEmail());
      throw new ApiException(ApiErrorCode.PROVIDED_SAME_PASSWORD.getMessage());
    }
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    log.info("User {} password was successfully changed", user.getEmail());
  }

  private boolean isValidRequest(ChangePasswordRequest request, User user) {
    return passwordEncoder.matches(request.getOldPassword(), user.getPassword());
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
