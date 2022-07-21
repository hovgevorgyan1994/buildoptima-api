package com.vecondev.buildoptima.service;

import com.vecondev.buildoptima.dto.user.request.ConfirmEmailRequestDto;
import com.vecondev.buildoptima.dto.user.request.RefreshTokenRequestDto;
import com.vecondev.buildoptima.dto.user.request.RestorePasswordRequestDto;
import com.vecondev.buildoptima.dto.user.request.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.user.response.RefreshTokenResponseDto;
import com.vecondev.buildoptima.dto.user.response.UserResponseDto;
import com.vecondev.buildoptima.exception.AuthenticationException;
import com.vecondev.buildoptima.manager.JwtTokenManager;
import com.vecondev.buildoptima.mapper.user.UserMapper;
import com.vecondev.buildoptima.model.user.ConfirmationToken;
import com.vecondev.buildoptima.model.user.RefreshToken;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.user.UserServiceTestParameters;
import com.vecondev.buildoptima.repository.user.UserRepository;
import com.vecondev.buildoptima.service.auth.impl.AuthServiceImpl;
import com.vecondev.buildoptima.service.auth.impl.ConfirmationTokenServiceImpl;
import com.vecondev.buildoptima.service.auth.impl.RefreshTokenServiceImpl;
import com.vecondev.buildoptima.service.mail.MailService;
import com.vecondev.buildoptima.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.AlreadyBuiltException;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  private final UserServiceTestParameters testParameters = new UserServiceTestParameters();

  @InjectMocks private AuthServiceImpl authService;
  @Mock private MailService mailService;
  @Mock private ConfirmationTokenServiceImpl confirmationTokenService;
  @Mock private RefreshTokenServiceImpl refreshTokenService;
  @Mock private UserValidator userValidator;
  @Mock private UserMapper userMapper;
  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder encoder;
  @Mock private JwtTokenManager tokenManager;

  @Test
  void failedRegistrationAsPhoneIsDuplicated() {
    Locale locale = new Locale("en");
    UserRegistrationRequestDto requestDto = testParameters.getUserRegistrationRequestDto();
    User user = testParameters.getUserFromRegistrationDto(requestDto);

    when(userMapper.mapToEntity(requestDto)).thenReturn(user);
    doThrow(AlreadyBuiltException.class).when(userValidator).validateUserRegistration(user);

    assertThrows(AlreadyBuiltException.class, () -> authService.register(requestDto, locale));
    verify(userMapper).mapToEntity(requestDto);
  }

  @Test
  void successfulRegistration() throws MessagingException {
    UserRegistrationRequestDto requestDto = testParameters.getUserRegistrationRequestDto();
    User user = testParameters.getUserFromRegistrationDto(requestDto);
    User savedUser = testParameters.getSavedUser(user);
    UserResponseDto responseDto = testParameters.getUserResponseDto(savedUser);

    when(userMapper.mapToEntity(requestDto)).thenReturn(user);
    when(userRepository.saveAndFlush(user)).thenReturn(savedUser);
    when(userMapper.mapToResponseDto(savedUser)).thenReturn(responseDto);

    UserResponseDto registrationResponseDto = authService.register(requestDto, new Locale("en"));
    assertEquals(requestDto.getEmail(), registrationResponseDto.getEmail());
    assertEquals(savedUser.getCreatedAt(), registrationResponseDto.getCreatedAt());
    verify(mailService).sendConfirm(any(), any());
    verify(confirmationTokenService).create(savedUser);
    verify(userValidator).validateUserRegistration(user);
  }

  @Test
  void failedRegistrationAsThrowsExceptionWhileSendingEmail() throws MessagingException {
    Locale locale = new Locale("en");
    UserRegistrationRequestDto requestDto = testParameters.getUserRegistrationRequestDto();
    User user = testParameters.getUserFromRegistrationDto(requestDto);
    User savedUser = testParameters.getSavedUser(user);

    when(userMapper.mapToEntity(requestDto)).thenReturn(user);
    when(userRepository.saveAndFlush(user)).thenReturn(savedUser);
    doThrow(MessagingException.class).when(mailService).sendConfirm(any(), any());

    assertThrows(AuthenticationException.class, () -> authService.register(requestDto, locale));
    verify(confirmationTokenService).create(savedUser);
    verify(userValidator).validateUserRegistration(user);
  }

  @Test
  void failedActivationAsTokenNotFound() {
    String token = UUID.randomUUID().toString();

    doThrow(AuthenticationException.class).when(confirmationTokenService).getByToken(token);

    assertThrows(AuthenticationException.class, () -> authService.activate(token));
    verify(confirmationTokenService).getByToken(token);
  }

  @Test
  void successfulActivation() {
    String token = UUID.randomUUID().toString();
    User user = testParameters.getSavedUser();
    ConfirmationToken confirmationToken = testParameters.getConfirmationToken(token, user);
    UserResponseDto responseDto = testParameters.getUserResponseDto(user);

    when(confirmationTokenService.getByToken(token)).thenReturn(confirmationToken);
    when(userRepository.getReferenceById(user.getId())).thenReturn(user);
    when(userMapper.mapToResponseDto(user)).thenReturn(responseDto);

    authService.activate(token);
    verify(confirmationTokenService).remove(user.getId());
  }

  @Test
  void successfulRefreshingOfTokens() {
    RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto(UUID.randomUUID().toString());
    User user = testParameters.getSavedUser();
    RefreshToken refreshToken =
        testParameters.getRefreshTokenWithRefreshTokenId(
            requestDto.getRefreshToken(), user.getId(), false);

    when(refreshTokenService.findByRefreshToken(requestDto.getRefreshToken()))
        .thenReturn(refreshToken);
    when(userRepository.getReferenceById(refreshToken.getUserId())).thenReturn(user);
    when(tokenManager.generateAccessToken(user)).thenReturn(UUID.randomUUID().toString());

    RefreshTokenResponseDto response = authService.refreshToken(requestDto);
    assertNotNull(response.getAccessToken());
    assertNotNull(response.getRefreshToken());
  }

  @Test
  void failedRefreshingOfTokensAsTokenIsExpired() {
    RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto(UUID.randomUUID().toString());
    User user = testParameters.getSavedUser();
    RefreshToken refreshToken =
        testParameters.getRefreshTokenWithRefreshTokenId(
            requestDto.getRefreshToken(), user.getId(), false);
    refreshToken.setExpiresAt(LocalDateTime.now().minusDays(1));

    when(refreshTokenService.findByRefreshToken(requestDto.getRefreshToken()))
        .thenReturn(refreshToken);

    assertThrows(AuthenticationException.class, () -> authService.refreshToken(requestDto));
  }

  @Test
  void successfulVerifyingUser() throws MessagingException {
    User user = testParameters.getSavedUser();
    ConfirmEmailRequestDto requestDto = new ConfirmEmailRequestDto(user.getEmail());

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    authService.verify(requestDto, new Locale("en"));

    verify(confirmationTokenService).create(user);
    verify(mailService).sendVerify(any(), any());
  }

  @Test
  void failedUserVerifyingAsMailCantSend() throws MessagingException {
    User user = testParameters.getSavedUser();
    ConfirmEmailRequestDto requestDto = new ConfirmEmailRequestDto(user.getEmail());
    Locale locale = new Locale("en");

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    doThrow(MessagingException.class).when(mailService).sendVerify(any(), any());

    assertThrows(AuthenticationException.class, () -> authService.verify(requestDto, locale));
    verify(confirmationTokenService).create(user);
  }

  @Test
  void successfulPasswordRestoring() {
    UUID token = UUID.randomUUID();
    User savedUser = testParameters.getSavedUser();
    RestorePasswordRequestDto requestDto =
        new RestorePasswordRequestDto(token.toString(), "newPassword");
    ConfirmationToken confirmationToken =
        testParameters.getSavedConfirmationToken(savedUser, token);

    when(confirmationTokenService.getByToken(requestDto.getConfirmationToken()))
        .thenReturn(confirmationToken);
    when(encoder.encode(requestDto.getNewPassword()))
        .thenReturn(testParameters.getPasswordEncoded(requestDto.getNewPassword()));

    assertDoesNotThrow(() -> authService.restorePassword(requestDto));
  }

  @Test
  void failedRestoringPassword() {
    UUID token = UUID.randomUUID();
    User savedUser = testParameters.getSavedUser();
    RestorePasswordRequestDto requestDto =
        new RestorePasswordRequestDto(token.toString(), "newPassword");
    ConfirmationToken confirmationToken =
        testParameters.getSavedConfirmationToken(savedUser, token);

    doThrow(AuthenticationException.class)
        .when(confirmationTokenService)
        .getByToken(token.toString());

    assertThrows(AuthenticationException.class, () -> authService.restorePassword(requestDto));
  }
}
