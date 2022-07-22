package com.vecondev.buildoptima.service;

import com.vecondev.buildoptima.dto.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.filter.FetchResponseDto;
import com.vecondev.buildoptima.dto.user.request.ChangePasswordRequestDto;
import com.vecondev.buildoptima.dto.user.response.UserResponseDto;
import com.vecondev.buildoptima.exception.AuthenticationException;
import com.vecondev.buildoptima.exception.UserNotFoundException;
import com.vecondev.buildoptima.filter.converter.PageableConverter;
import com.vecondev.buildoptima.mapper.user.UserMapper;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.user.UserServiceTestParameters;
import com.vecondev.buildoptima.repository.user.UserRepository;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import com.vecondev.buildoptima.service.auth.SecurityContextService;
import com.vecondev.buildoptima.service.image.ImageService;
import com.vecondev.buildoptima.service.user.UserServiceImpl;
import com.vecondev.buildoptima.util.RestPreconditions;
import com.vecondev.buildoptima.validation.validator.FieldNameValidator;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  private final UserServiceTestParameters testParameters = new UserServiceTestParameters();

  @InjectMocks private UserServiceImpl userService;
  @Mock private ImageService imageService;
  @Mock private UserMapper userMapper;
  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder encoder;
  @Mock private PageableConverter pageableConverter;
  @Mock private SecurityContextService securityContextService;

  @Test
  void successfulFetchingOfUsers() {
    FetchRequestDto requestDto = testParameters.getFetchRequest();
    Pageable pageable = testParameters.getPageable(requestDto);
    Page<User> result = new PageImpl<>(testParameters.getUserList());

    when(securityContextService.getUserDetails()).thenReturn(testParameters.userDetails());
    try (MockedStatic<FieldNameValidator> validator =
        Mockito.mockStatic(FieldNameValidator.class)) {
      validator
          .when(() -> FieldNameValidator.validateFieldNames(any(), any()))
          .thenAnswer((Answer<Void>) invocation -> null);
    }
    when(pageableConverter.convert(requestDto)).thenReturn(pageable);
    when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(result);
    when(userMapper.mapToResponseList(result))
        .thenReturn(testParameters.getUserResponseDtoList(result.stream().toList()));

    FetchResponseDto responseDto =
        userService.fetch(requestDto);
    assertEquals(2, responseDto.getTotalElements());
  }

  @Test
  void successfulFetchingWithDefaultSortDirectory() {
    FetchRequestDto requestDto = testParameters.getFetchRequest();
    requestDto.setSort(null);
    Pageable pageable = testParameters.getPageable(requestDto);
    Page<User> result = new PageImpl<>(testParameters.getUserList());

    when(securityContextService.getUserDetails()).thenReturn(testParameters.userDetails());
    try (MockedStatic<FieldNameValidator> validator =
        Mockito.mockStatic(FieldNameValidator.class)) {
      validator
          .when(() -> FieldNameValidator.validateFieldNames(any(), any()))
          .thenAnswer((Answer<Void>) invocation -> null);
    }
    when(pageableConverter.convert(requestDto)).thenReturn(pageable);
    when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(result);
    when(userMapper.mapToResponseList(result))
        .thenReturn(testParameters.getUserResponseDtoList(result.stream().toList()));

    FetchResponseDto responseDto =
        userService.fetch(requestDto);
    assertEquals(2, responseDto.getTotalElements());
  }

  @Test
  void successfulChangingOfPassword() {
    String oldPassword = "oldPassword";
    String newPassword = "newPassword";
    String encodedNewPassword = testParameters.getPasswordEncoded(newPassword);
    ChangePasswordRequestDto requestDto =
        testParameters.getChangePasswordRequestDto(oldPassword, newPassword);
    User user = testParameters.getSavedUser();
    user.setPassword(testParameters.getPasswordEncoded(oldPassword));

    when(securityContextService.getUserDetails()).thenReturn(testParameters.userDetails());
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(encoder.matches(any(), any())).thenReturn(true);
    when(encoder.encode(any())).thenReturn(encodedNewPassword);

    assertDoesNotThrow(() -> userService.changePassword(requestDto));
    verify(encoder).encode(any());
  }

  @Test
  void failedChangingOfPasswordAsPasswordIsInvalid() {
    String oldPassword = "oldPassword";
    String newPassword = "newPassword";
    ChangePasswordRequestDto requestDto =
        testParameters.getChangePasswordRequestDto(oldPassword, newPassword);
    User user = testParameters.getSavedUser();
    user.setPassword(testParameters.getPasswordEncoded(oldPassword + 1));

    when(securityContextService.getUserDetails()).thenReturn(testParameters.userDetails());
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(encoder.matches(any(), any())).thenReturn(false);

    assertThrows(
        AuthenticationException.class, () -> userService.changePassword(requestDto));
  }

  @Test
  void failedChangingOfPasswordAsPasswordsAreTheSame() {
    String password = "oldPassword";
    ChangePasswordRequestDto requestDto =
        testParameters.getChangePasswordRequestDto(password, password);
    User user = testParameters.getSavedUser();
    user.setPassword(testParameters.getPasswordEncoded(password));

    when(securityContextService.getUserDetails()).thenReturn(testParameters.userDetails());
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(encoder.matches(any(), any())).thenReturn(true);

    assertThrows(
        AuthenticationException.class, () -> userService.changePassword(requestDto));
  }

  @Test
  void successfulUserFetching() {
    User user = testParameters.getSavedUser();

    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(userMapper.mapToResponseDto(user)).thenReturn(testParameters.getUserResponseDto(user));

    UserResponseDto responseDto = userService.getById(user.getId());
    assertEquals(user.getId(), responseDto.getId());
    assertEquals(user.getUpdatedAt(), responseDto.getUpdatedAt());
  }

  @Test
  void failedUserFetching() {
    UUID userId = UUID.randomUUID();

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.getById(userId));
  }

  @Test
  void successfulImageUploading() {
    UUID userId = UUID.randomUUID();

    when(securityContextService.getUserDetails()).thenReturn(testParameters.userDetails());
    try (MockedStatic<RestPreconditions> restPreconditions =
        Mockito.mockStatic(RestPreconditions.class)) {
      restPreconditions
          .when(() -> RestPreconditions.checkNotNull(any(), any()))
          .thenAnswer((Answer<Void>) invocation -> null);
      userService.uploadImage(userId, null);
    }

    verify(imageService).uploadImagesToS3(any(), any(), any(), any());
  }

  @Test
  void successfulImageDownloading() {
    UUID ownerId = UUID.randomUUID();
    String contentType = IMAGE_JPEG_VALUE;
    boolean isOriginal = true;

    when(userRepository.existsById(any())).thenReturn(true);
    when(imageService.downloadImage("user", ownerId, isOriginal)).thenReturn(new byte[] {});
    when(imageService.getContentTypeOfObject("user", ownerId, isOriginal)).thenReturn(contentType);
    ResponseEntity<byte[]> response = userService.downloadImage(ownerId, isOriginal);

    assertEquals(
        contentType, Objects.requireNonNull(response.getHeaders().get("Content-type")).get(0));
    verify(imageService).downloadImage("user", ownerId, isOriginal);
  }

  @Test
  void successfulImageDeleting() {
    when(userRepository.existsById(any())).thenReturn(true);
    userService.deleteImage(UUID.randomUUID());

    verify(imageService).deleteImagesFromS3(any(), any());
  }

  @Test
  void failedImageDeletingAsObjectDoesntExist() {
    UUID userId = UUID.randomUUID();
    when(userRepository.existsById(any())).thenReturn(false);

    assertThrows(UserNotFoundException.class, () -> userService.deleteImage(userId));
  }
}
