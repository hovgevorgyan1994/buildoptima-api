package com.vecondev.buildoptima.service.auth.impl;

import static com.vecondev.buildoptima.exception.Error.BAD_CREDENTIALS;
import static com.vecondev.buildoptima.exception.Error.SEND_EMAIL_FAILED;
import static com.vecondev.buildoptima.exception.Error.USER_NOT_FOUND;

import com.vecondev.buildoptima.dto.user.request.AuthRequestDto;
import com.vecondev.buildoptima.dto.user.request.ConfirmEmailRequestDto;
import com.vecondev.buildoptima.dto.user.request.RefreshTokenRequestDto;
import com.vecondev.buildoptima.dto.user.request.RestorePasswordRequestDto;
import com.vecondev.buildoptima.dto.user.request.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.user.response.AuthResponseDto;
import com.vecondev.buildoptima.dto.user.response.RefreshTokenResponseDto;
import com.vecondev.buildoptima.dto.user.response.UserResponseDto;
import com.vecondev.buildoptima.exception.AuthenticationException;
import com.vecondev.buildoptima.exception.Error;
import com.vecondev.buildoptima.manager.JwtTokenManager;
import com.vecondev.buildoptima.mapper.user.UserMapper;
import com.vecondev.buildoptima.model.user.ConfirmationToken;
import com.vecondev.buildoptima.model.user.RefreshToken;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.repository.user.UserRepository;
import com.vecondev.buildoptima.service.auth.AuthService;
import com.vecondev.buildoptima.service.auth.ConfirmationTokenService;
import com.vecondev.buildoptima.service.auth.RefreshTokenService;
import com.vecondev.buildoptima.service.mail.MailService;
import com.vecondev.buildoptima.validation.UserValidator;
import java.util.Locale;
import java.util.Optional;
import javax.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final UserValidator userValidator;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenManager tokenManager;
  private final ConfirmationTokenService confirmationTokenService;
  private final RefreshTokenService refreshTokenService;
  private final MailService mailService;

  @Override
  public UserResponseDto register(UserRegistrationRequestDto dto, Locale locale) {
    User user = userMapper.mapToEntity(dto);
    userValidator.validateUserRegistration(user);
    user = userRepository.saveAndFlush(user);
    sendEmail(locale, user);
    return userMapper.mapToResponseDto(user);
  }

  public void sendEmail(Locale locale, User user) {
    ConfirmationToken confirmationToken = confirmationTokenService.create(user);
    try {
      mailService.sendConfirm(locale, confirmationToken);
    } catch (MessagingException e) {
      throw new AuthenticationException(SEND_EMAIL_FAILED);
    }
    log.info("Verification email was sent to user {}", user.getEmail());
  }

  @Override
  public UserResponseDto activate(String token) {
    ConfirmationToken confirmationToken = confirmationTokenService.getByToken(token);

    return activateUserAccount(confirmationToken);
  }

  @Override
  public AuthResponseDto login(final AuthRequestDto authRequestDto) {
    log.info("Request from user {} to get authenticated", authRequestDto.getUsername());
    Optional<User> optionalUser = userRepository.findByEmail(authRequestDto.getUsername());
    if (optionalUser.isEmpty()
        || !passwordEncoder.matches(
            authRequestDto.getPassword(), optionalUser.get().getPassword())) {
      log.warn("{}: Provided wrong credentials for authentication", authRequestDto.getUsername());
      throw new AuthenticationException(BAD_CREDENTIALS);
    }
    User user = optionalUser.get();
    if (!user.isEnabled()) {
      throw new AuthenticationException(Error.NOT_ACTIVE_ACCOUNT);
    }
    return buildAuthDto(optionalUser.get());
  }

  @Override
  public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto request) {
    log.info("Request to refresh the access token");
    final RefreshToken refreshToken =
        refreshTokenService.findByRefreshToken(request.getRefreshToken());

    User user = userRepository.getReferenceById(refreshToken.getUserId());
    log.info("Access token is refreshed for user {}", user.getEmail());
    return RefreshTokenResponseDto.builder()
        .accessToken(tokenManager.generateAccessToken(user))
        .refreshToken(refreshToken.getPlainRefreshToken())
        .build();
  }

  @Override
  public void verify(ConfirmEmailRequestDto requestDto, Locale locale) {
    log.info(
        "Request from optional user {} to get a password restoring email", requestDto.getEmail());
    User user =
        userRepository
            .findByEmail(requestDto.getEmail())
            .orElseThrow(() -> new AuthenticationException(USER_NOT_FOUND));

    ConfirmationToken token = confirmationTokenService.create(user);
    try {
      log.info("Sending a password restoring email to user {}", user.getEmail());
      mailService.sendVerify(locale, token);
      log.info("Password restoring email was sent to user {}", user.getEmail());
    } catch (MessagingException e) {
      log.error("Something went wrong while sending email to user {}", user.getEmail());
      throw new AuthenticationException(SEND_EMAIL_FAILED);
    }
  }

  @Override
  public void restorePassword(RestorePasswordRequestDto restorePasswordRequestDto) {
    log.info("Request to restore a forgotten password");
    ConfirmationToken confirmationToken =
        confirmationTokenService.getByToken(restorePasswordRequestDto.getConfirmationToken());

    User user = confirmationToken.getUser();
    user.setPassword(passwordEncoder.encode(restorePasswordRequestDto.getNewPassword()));
    confirmationTokenService.deleteByUserId(confirmationToken.getUser().getId());
    log.info("User {} has successfully changed the password", user.getEmail());
  }

  private UserResponseDto activateUserAccount(ConfirmationToken confirmationToken) {
    User user = userRepository.getReferenceById(confirmationToken.getUser().getId());
    user.setEnabled(true);

    confirmationTokenService.remove(user.getId());
    log.info("User {} account was verified by email", user.getEmail());
    return userMapper.mapToResponseDto(user);
  }

  private AuthResponseDto buildAuthDto(final User user) {
    log.info("User {} provided credentials to receive an access token", user.getEmail());
    final String accessToken = tokenManager.generateAccessToken(user);
    RefreshToken refreshToken = refreshTokenService.findByUserId(user.getId());
    if (refreshToken == null) {
      refreshToken = refreshTokenService.create(user.getId());
    }
    log.info("Access token is created for user {}", user.getEmail());
    return AuthResponseDto.builder()
        .userId(user.getId())
        .imageVersion(user.getImageVersion())
        .accessToken(accessToken)
        .refreshToken(refreshToken.getPlainRefreshToken())
        .build();
  }
}
