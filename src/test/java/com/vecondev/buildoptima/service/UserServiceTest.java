package com.vecondev.buildoptima.service;

import com.vecondev.buildoptima.dto.request.ChangePasswordRequestDto;
import com.vecondev.buildoptima.dto.request.ConfirmEmailRequestDto;
import com.vecondev.buildoptima.dto.request.FetchRequestDto;
import com.vecondev.buildoptima.dto.request.RefreshTokenRequestDto;
import com.vecondev.buildoptima.dto.request.RestorePasswordRequestDto;
import com.vecondev.buildoptima.dto.request.UserRegistrationRequestDto;
import com.vecondev.buildoptima.dto.response.FetchResponseDto;
import com.vecondev.buildoptima.dto.response.RefreshTokenResponseDto;
import com.vecondev.buildoptima.dto.response.UserResponseDto;
import com.vecondev.buildoptima.exception.AuthenticationException;
import com.vecondev.buildoptima.filter.converter.PageableConverter;
import com.vecondev.buildoptima.manager.JwtTokenManager;
import com.vecondev.buildoptima.mapper.user.UserMapper;
import com.vecondev.buildoptima.model.user.ConfirmationToken;
import com.vecondev.buildoptima.model.user.RefreshToken;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.repository.UserRepository;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import com.vecondev.buildoptima.service.impl.ConfirmationTokenServiceImpl;
import com.vecondev.buildoptima.service.impl.ImageServiceImpl;
import com.vecondev.buildoptima.service.impl.MailService;
import com.vecondev.buildoptima.service.impl.RefreshTokenServiceImpl;
import com.vecondev.buildoptima.service.impl.UserServiceImpl;
import com.vecondev.buildoptima.util.RestPreconditions;
import com.vecondev.buildoptima.util.UserServiceTestParameters;
import com.vecondev.buildoptima.validation.UserValidator;
import com.vecondev.buildoptima.validation.validator.FieldNameValidator;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.AlreadyBuiltException;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
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
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  private final UserServiceTestParameters userServiceTestParameters =
      new UserServiceTestParameters();

  @InjectMocks private UserServiceImpl userService;
  @Mock private MailService mailService;
  @Mock private ImageServiceImpl imageService;
  @Mock private ConfirmationTokenServiceImpl confirmationTokenService;
  @Mock private RefreshTokenServiceImpl refreshTokenService;
  @Mock private UserValidator userValidator;
  @Mock private UserMapper userMapper;
  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder encoder;
  @Mock private JwtTokenManager tokenManager;
  @Mock private PageableConverter pageableConverter;

  @Test
  void failedRegistrationAsPhoneIsDuplicated() {
    Locale locale = new Locale("en");
    UserRegistrationRequestDto requestDto =
        userServiceTestParameters.getUserRegistrationRequestDto();
    User user = userServiceTestParameters.getUserFromRegistrationDto(requestDto);

    when(userMapper.mapToEntity(requestDto)).thenReturn(user);
    doThrow(AlreadyBuiltException.class).when(userValidator).validateUserRegistration(user);

    assertThrows(AlreadyBuiltException.class, () -> userService.register(requestDto, locale));
    verify(userMapper).mapToEntity(requestDto);
  }

  @Test
  void successfulRegistration() throws MessagingException {
    UserRegistrationRequestDto requestDto =
        userServiceTestParameters.getUserRegistrationRequestDto();
    User user = userServiceTestParameters.getUserFromRegistrationDto(requestDto);
    User savedUser = userServiceTestParameters.getSavedUser(user);
    UserResponseDto responseDto = userServiceTestParameters.getUserResponseDto(savedUser);

    when(userMapper.mapToEntity(requestDto)).thenReturn(user);
    when(userRepository.saveAndFlush(user)).thenReturn(savedUser);
    when(userMapper.mapToResponseDto(savedUser)).thenReturn(responseDto);

    UserResponseDto registrationResponseDto = userService.register(requestDto, new Locale("en"));
    assertEquals(requestDto.getEmail(), registrationResponseDto.getEmail());
    assertEquals(savedUser.getCreationDate(), registrationResponseDto.getCreationDate());
    verify(mailService).sendConfirm(any(), any());
    verify(confirmationTokenService).create(savedUser);
    verify(userValidator).validateUserRegistration(user);
  }

  @Test
  void failedRegistrationAsThrowsExceptionWhileSendingEmail() throws MessagingException {
    Locale locale = new Locale("en");
    UserRegistrationRequestDto requestDto =
        userServiceTestParameters.getUserRegistrationRequestDto();
    User user = userServiceTestParameters.getUserFromRegistrationDto(requestDto);
    User savedUser = userServiceTestParameters.getSavedUser(user);

    when(userMapper.mapToEntity(requestDto)).thenReturn(user);
    when(userRepository.saveAndFlush(user)).thenReturn(savedUser);
    doThrow(MessagingException.class).when(mailService).sendConfirm(any(), any());

    assertThrows(AuthenticationException.class, () -> userService.register(requestDto, locale));
    verify(confirmationTokenService).create(savedUser);
    verify(userValidator).validateUserRegistration(user);
  }

  @Test
  void failedActivationAsTokenIsInvalid() {
    String token = UUID.randomUUID().toString();

    when(confirmationTokenService.isNotValid(any())).thenReturn(true);

    assertThrows(AuthenticationException.class, () -> userService.activate(token));
    verify(confirmationTokenService).getByToken(token);
  }

  @Test
  void successfulActivation() {
    String token = UUID.randomUUID().toString();
    User user = userServiceTestParameters.getSavedUser();
    ConfirmationToken confirmationToken =
        userServiceTestParameters.getConfirmationToken(token, user);
    UserResponseDto responseDto = userServiceTestParameters.getUserResponseDto(user);

    when(confirmationTokenService.isNotValid(any())).thenReturn(false);
    when(confirmationTokenService.getByToken(token)).thenReturn(confirmationToken);
    when(userRepository.getReferenceById(user.getId())).thenReturn(user);
    when(userMapper.mapToResponseDto(user)).thenReturn(responseDto);

    userService.activate(token);
    verify(confirmationTokenService).remove(user.getId());
  }

  @Test
  void successfulRefreshingOfTokens() {
    RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto(UUID.randomUUID().toString());
    User user = userServiceTestParameters.getSavedUser();
    RefreshToken refreshToken =
        userServiceTestParameters.getRefreshTokenWithRefreshTokenId(
            requestDto.getRefreshToken(), user.getId(), false);

    when(refreshTokenService.findByRefreshToken(requestDto.getRefreshToken()))
        .thenReturn(refreshToken);
    when(userRepository.findById(refreshToken.getUserId())).thenReturn(Optional.of(user));
    when(tokenManager.generateAccessToken(user)).thenReturn(UUID.randomUUID().toString());

    RefreshTokenResponseDto response = userService.refreshToken(requestDto);
    assertNotNull(response.getAccessToken());
    assertNotNull(response.getRefreshToken());
  }

  @Test
  void failedRefreshingOfTokensAsTokenIsExpired() {
    RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto(UUID.randomUUID().toString());
    User user = userServiceTestParameters.getSavedUser();
    RefreshToken refreshToken =
        userServiceTestParameters.getRefreshTokenWithRefreshTokenId(
            requestDto.getRefreshToken(), user.getId(), false);
    refreshToken.setExpiresAt(LocalDateTime.now().minusDays(1));

    when(refreshTokenService.findByRefreshToken(requestDto.getRefreshToken()))
        .thenReturn(refreshToken);

    assertThrows(AuthenticationException.class, () -> userService.refreshToken(requestDto));
  }

  @Test
  void successfulFetchingOfUsers() {
    FetchRequestDto requestDto = userServiceTestParameters.getFetchRequest();
    Pageable pageable = userServiceTestParameters.getPageable(requestDto);
    Page<User> result = new PageImpl<>(userServiceTestParameters.getFetchResponse());

    try (MockedStatic<FieldNameValidator> validator =
        Mockito.mockStatic(FieldNameValidator.class)) {
      validator
          .when(() -> FieldNameValidator.validateFieldNames(any(), any()))
          .thenAnswer((Answer<Void>) invocation -> null);
    }
    when(pageableConverter.convert(requestDto)).thenReturn(pageable);
    when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(result);
    when(userMapper.mapToResponseList(result))
        .thenReturn(userServiceTestParameters.getUserResponseDtoList(result.stream().toList()));

    FetchResponseDto responseDto = userService.fetchUsers(requestDto);
    assertEquals(2, responseDto.getTotalElements());
  }

  @Test
  void successfulFetchingWithDefaultSortDirectory() {
    FetchRequestDto requestDto = userServiceTestParameters.getFetchRequest();
    requestDto.setSort(null);
    Pageable pageable = userServiceTestParameters.getPageable(requestDto);
    Page<User> result = new PageImpl<>(userServiceTestParameters.getFetchResponse());

    try (MockedStatic<FieldNameValidator> validator =
        Mockito.mockStatic(FieldNameValidator.class)) {
      validator
          .when(() -> FieldNameValidator.validateFieldNames(any(), any()))
          .thenAnswer((Answer<Void>) invocation -> null);
    }
    when(pageableConverter.convert(requestDto)).thenReturn(pageable);
    when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(result);
    when(userMapper.mapToResponseList(result))
        .thenReturn(userServiceTestParameters.getUserResponseDtoList(result.stream().toList()));

    FetchResponseDto responseDto = userService.fetchUsers(requestDto);
    assertEquals(2, responseDto.getTotalElements());
  }

  @Test
  void successfulChangingOfPassword() {
    String oldPassword = "oldPassword";
    String newPassword = "newPassword";
    String encodedNewPassword = userServiceTestParameters.getPasswordEncoded(newPassword);
    ChangePasswordRequestDto requestDto =
        userServiceTestParameters.getChangePasswordRequestDto(oldPassword, newPassword);
    User user = userServiceTestParameters.getSavedUser();
    user.setPassword(userServiceTestParameters.getPasswordEncoded(oldPassword));
    AppUserDetails userDetails = new AppUserDetails(user);

    when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));
    when(encoder.matches(any(), any())).thenReturn(true);
    when(encoder.encode(any())).thenReturn(encodedNewPassword);

    assertDoesNotThrow(() -> userService.changePassword(requestDto, userDetails));
    verify(encoder).encode(any());
  }

  @Test
  void failedChangingOfPasswordAsPasswordIsInvalid() {
    String oldPassword = "oldPassword";
    String newPassword = "newPassword";
    ChangePasswordRequestDto requestDto =
        userServiceTestParameters.getChangePasswordRequestDto(oldPassword, newPassword);
    User user = userServiceTestParameters.getSavedUser();
    user.setPassword(userServiceTestParameters.getPasswordEncoded(oldPassword + 1));
    AppUserDetails userDetails = new AppUserDetails(user);

    when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));
    when(encoder.matches(any(), any())).thenReturn(false);

    assertThrows(
        AuthenticationException.class, () -> userService.changePassword(requestDto, userDetails));
  }

  @Test
  void failedChangingOfPasswordAsPasswordsAreTheSame() {
    String password = "oldPassword";
    ChangePasswordRequestDto requestDto =
        userServiceTestParameters.getChangePasswordRequestDto(password, password);
    User user = userServiceTestParameters.getSavedUser();
    user.setPassword(userServiceTestParameters.getPasswordEncoded(password));
    AppUserDetails userDetails = new AppUserDetails(user);

    when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));
    when(encoder.matches(any(), any())).thenReturn(true);

    assertThrows(
        AuthenticationException.class, () -> userService.changePassword(requestDto, userDetails));
  }

  @Test
  void successfulUserFetching() {
    User user = userServiceTestParameters.getSavedUser();

    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(userMapper.mapToResponseDto(user))
        .thenReturn(userServiceTestParameters.getUserResponseDto(user));

    UserResponseDto responseDto = userService.getUser(user.getId());
    assertEquals(user.getId(), responseDto.getId());
    assertEquals(user.getUpdateDate(), responseDto.getUpdateDate());
  }

  @Test
  void failedUserFetching() {
    UUID userId = UUID.randomUUID();

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(AuthenticationException.class, () -> userService.getUser(userId));
  }

  @Test
  void successfulVerifyingUser() throws MessagingException {
    User user = userServiceTestParameters.getSavedUser();
    ConfirmEmailRequestDto requestDto = new ConfirmEmailRequestDto(user.getEmail());

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    userService.verifyUserAndSendEmail(requestDto, new Locale("en"));

    verify(confirmationTokenService).create(user);
    verify(mailService).sendVerify(any(), any());
  }

  @Test
  void failedUserVerifyingAsMailCantSend() throws MessagingException {
    User user = userServiceTestParameters.getSavedUser();
    ConfirmEmailRequestDto requestDto = new ConfirmEmailRequestDto(user.getEmail());
    Locale locale = new Locale("en");

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    doThrow(MessagingException.class).when(mailService).sendVerify(any(), any());

    assertThrows(
        AuthenticationException.class,
        () -> userService.verifyUserAndSendEmail(requestDto, locale));
    verify(confirmationTokenService).create(user);
  }

  @Test
  void successfulPasswordRestoring() {
    UUID token = UUID.randomUUID();
    User savedUser = userServiceTestParameters.getSavedUser();
    RestorePasswordRequestDto requestDto = new RestorePasswordRequestDto(token.toString(), "newPassword");
    ConfirmationToken confirmationToken =
        userServiceTestParameters.getSavedConfirmationToken(savedUser, token);

    when(confirmationTokenService.getByToken(requestDto.getConfirmationToken()))
        .thenReturn(confirmationToken);
    when(confirmationTokenService.isNotValid(confirmationToken)).thenReturn(false);
    when(encoder.encode(requestDto.getNewPassword()))
        .thenReturn(userServiceTestParameters.getPasswordEncoded(requestDto.getNewPassword()));

    assertDoesNotThrow(() -> userService.restorePassword(requestDto));
  }

  @Test
  void failedRestoringPassword(){
    UUID token = UUID.randomUUID();
    User savedUser = userServiceTestParameters.getSavedUser();
    RestorePasswordRequestDto requestDto = new RestorePasswordRequestDto(token.toString(), "newPassword");
    ConfirmationToken confirmationToken =
        userServiceTestParameters.getSavedConfirmationToken(savedUser, token);

    when(confirmationTokenService.getByToken(requestDto.getConfirmationToken()))
        .thenReturn(confirmationToken);
    when(confirmationTokenService.isNotValid(confirmationToken)).thenReturn(true);

    assertThrows(AuthenticationException.class,() -> userService.restorePassword(requestDto));
  }

  @Test
  void successfulImageUploading() {
    UUID userId = UUID.randomUUID();

    try (MockedStatic<RestPreconditions> restPreconditions =
                 Mockito.mockStatic(RestPreconditions.class)) {
      restPreconditions
              .when(() -> RestPreconditions.checkNotNull(any(), any(), any()))
              .thenAnswer((Answer<Void>) invocation -> null);
      userService.uploadImage(userId, null);
    }

    verify(imageService).uploadUserImagesToS3(userId, null);
  }

  @Test
  void successfulImageDownloading() {
    UUID userId = UUID.randomUUID();
    UUID ownerId = UUID.randomUUID();
    String contentType = IMAGE_JPEG_VALUE;
    boolean isOriginal = true;

    when(imageService.downloadUserImage(ownerId, isOriginal)).thenReturn(new byte[] {});
    when(imageService.getContentTypeOfObject(userId, isOriginal)).thenReturn(contentType);
    ResponseEntity<byte[]> response = userService.downloadImage(userId, ownerId, isOriginal);

    assertEquals(contentType, Objects.requireNonNull(response.getHeaders().get("Content-type")).get(0));
    verify(imageService).downloadUserImage(ownerId, isOriginal);
  }

  @Test
  void successfulImageDeleting() {
    UUID userId = UUID.randomUUID();

    userService.deleteImage(userId);

    verify(imageService).deleteUserImagesFromS3(userId);
  }
}