package com.vecondev.buildoptima.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.vecondev.buildoptima.config.AmazonS3Config;
import com.vecondev.buildoptima.config.properties.S3ConfigProperties;
import com.vecondev.buildoptima.dto.request.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.request.user.AuthRequestDto;
import com.vecondev.buildoptima.dto.request.user.ChangePasswordRequestDto;
import com.vecondev.buildoptima.dto.request.user.ConfirmEmailRequestDto;
import com.vecondev.buildoptima.dto.request.user.RefreshTokenRequestDto;
import com.vecondev.buildoptima.dto.request.user.RestorePasswordRequestDto;
import com.vecondev.buildoptima.dto.request.user.UserRegistrationRequestDto;
import com.vecondev.buildoptima.exception.UserNotFoundException;
import com.vecondev.buildoptima.model.user.ConfirmationToken;
import com.vecondev.buildoptima.model.user.Role;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.endpoints.UserEndpointUris;
import com.vecondev.buildoptima.parameters.result_actions.UserResultActions;
import com.vecondev.buildoptima.parameters.user.UserControllerTestParameters;
import com.vecondev.buildoptima.repository.user.ConfirmationTokenRepository;
import com.vecondev.buildoptima.repository.user.RefreshTokenRepository;
import com.vecondev.buildoptima.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.vecondev.buildoptima.exception.ErrorCode.USER_NOT_FOUND;
import static com.vecondev.buildoptima.model.user.Role.ADMIN;
import static com.vecondev.buildoptima.model.user.Role.CLIENT;
import static com.vecondev.buildoptima.util.FileUtil.convertMultipartFileToFile;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
@ActiveProfiles("test")
@Import({AmazonS3Config.class, UserEndpointUris.class, UserResultActions.class})
class UserControllerTest {

  @RegisterExtension
  private static final GreenMailExtension greenMail =
      new GreenMailExtension(ServerSetupTest.SMTP)
          .withConfiguration(
              GreenMailConfiguration.aConfig()
                  .withUser("managementstaffing09@gmail.com", "buildoptima"))
          .withPerMethodLifecycle(true);

  @Autowired private AmazonS3 amazonS3;
  @Autowired private UserRepository userRepository;
  @Autowired private ConfirmationTokenRepository confirmationTokenRepository;
  @Autowired private RefreshTokenRepository refreshTokenRepository;
  @Autowired private PasswordEncoder encoder;
  @Autowired private S3ConfigProperties s3ConfigProperties;
  @Autowired private UserResultActions resultActions;

  private UserControllerTestParameters userControllerTestParameters;



  @BeforeEach
  void setUp() {
    userControllerTestParameters =
        new UserControllerTestParameters(
            userRepository, confirmationTokenRepository, refreshTokenRepository);

    List<User> users = userControllerTestParameters.users();
    users.forEach(user -> user.setPassword(encoder.encode(user.getPassword())));
    userRepository.saveAll(users);

    confirmationTokenRepository.saveAll(userControllerTestParameters.confirmationTokens());
    refreshTokenRepository.saveAll(userControllerTestParameters.refreshTokens());
  }

  @AfterEach
  void tearDown() {
    refreshTokenRepository.deleteAll();
    confirmationTokenRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void successfulRegistration() throws Exception {
    UserRegistrationRequestDto requestDto = userControllerTestParameters
            .getUserToSave();

    resultActions
        .registrationResultActions(requestDto)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.firstName").exists());

    MimeMessage[] messages = greenMail.getReceivedMessages();
    assertEquals(1, messages.length);
    assertEquals(requestDto.getEmail(), messages[0].getAllRecipients()[0].toString());
  }

  @Test
  void failedRegistrationAsBodyValuesAreInvalid() throws Exception {
    UserRegistrationRequestDto requestDto = userControllerTestParameters
            .getUserToSaveWithInvalidFields();

    resultActions
        .registrationResultActions(requestDto)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").exists());
  }

  @Test
  void failedRegistrationAsEmailAlreadyExist() throws Exception {
    UserRegistrationRequestDto requestDto = userControllerTestParameters
            .getUserToSaveWithDuplicatedEmail();

    resultActions
        .registrationResultActions(requestDto)
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value(containsString("email")));
  }

  @Test
  void successfulActivation() throws Exception {
    ConfirmationToken confirmationToken = userControllerTestParameters
            .getConfirmationTokenToConfirmAccount();
    UUID userId = confirmationToken.getUser().getId();
    assumeTrue(confirmationToken.getExpiresAt().isAfter(LocalDateTime.now()));

    resultActions
        .activationResultActions(confirmationToken.getToken())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.toString()));

    assertEquals(
        true, userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND)).getEnabled());
  }

  @Test
  void failedActivationAsTokenDoesntExist() throws Exception {
    String token = UUID.randomUUID().toString();

    resultActions.activationResultActions(token).andExpect(status().isNotFound());
  }

  @Test
  void successfulLogin() throws Exception {
    AuthRequestDto requestDto = userControllerTestParameters
            .getUserCredentialsToLogin();

    resultActions
        .loginResultActions(requestDto)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.refreshTokenId").exists());
  }

  @Test
  void failedLoginAsUserDoesntExist() throws Exception {
    AuthRequestDto requestDto = userControllerTestParameters
            .getUserInvalidCredentialsToLogin();
    assumeFalse(userRepository.existsByEmailIgnoreCase(requestDto.getUsername()));

    resultActions.loginResultActions(requestDto).andExpect(status().isUnauthorized());
  }

  @Test
  void successfulRefreshmentOfTokens() throws Exception {
    RefreshTokenRequestDto requestDto = userControllerTestParameters
            .getRefreshToken();

    resultActions.refreshTokenResultActions(requestDto).andExpect(status().isOk());
  }

  @Test
  void failedRefreshmentOfTokensAsRefreshTokenExpired() throws Exception {
    RefreshTokenRequestDto requestDto = userControllerTestParameters
            .getExpiredRefreshToken();

    resultActions
        .refreshTokenResultActions(requestDto)
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value(containsString("Expired")));
  }

  @Test
  void successfulFetchingOfUsers() throws Exception {
    FetchRequestDto requestDto = userControllerTestParameters
            .getFetchRequest();
    User adminUser = userControllerTestParameters
            .getUser(ADMIN);

    resultActions.fetchingResultActions(requestDto, adminUser).andExpect(status().isOk());
  }

  @Test
  void failedFetchingOfUsersAsPermissionDenied() throws Exception {
    FetchRequestDto requestDto = userControllerTestParameters
            .getFetchRequest();
    User clientUser = userControllerTestParameters
            .getUser(CLIENT);

    resultActions.fetchingResultActions(requestDto, clientUser).andExpect(status().isForbidden());
  }

  @Test
  void failedFetchingOfUsersAsRequestDtoIsInvalid() throws Exception {
    FetchRequestDto requestDto = userControllerTestParameters
            .getInvalidFetchRequest();
    User adminUser = userControllerTestParameters
            .getUser(Role.ADMIN);

    resultActions.fetchingResultActions(requestDto, adminUser).andExpect(status().isBadRequest());
  }

  @Test
  void successfulPasswordChanging() throws Exception {
    User savedUser = userControllerTestParameters.getUser();
    ChangePasswordRequestDto requestDto = userControllerTestParameters
            .getChangePasswordRequestDto(savedUser);

    resultActions.passwordChangingResultActions(requestDto, savedUser).andExpect(status().isOk());
  }

  @Test
  void successfulGettingUser() throws Exception {
    User user = userControllerTestParameters
            .getSavedUser();
    User adminUser = userControllerTestParameters.getSavedUser(ADMIN);

    resultActions
        .getByIdResultActions(user.getId(), adminUser)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(user.getId().toString()));
  }

  @Test
  void failedGettingUserAsPermissionDenied() throws Exception {
    User user = userControllerTestParameters
            .getSavedUser();

    resultActions.getByIdResultActions(UUID.randomUUID(), user).andExpect(status().isNotFound());
  }

  @Test
  void successfulPasswordVerification() throws Exception {
    User user = userControllerTestParameters.getSavedUser();
    ConfirmEmailRequestDto requestDto = new ConfirmEmailRequestDto(user.getEmail());

    resultActions.passwordVerificationResultActions(requestDto).andExpect(status().isOk());
    MimeMessage[] messages = greenMail.getReceivedMessages();
    assertEquals(1, messages.length);
    assertEquals(user.getEmail(), messages[0].getAllRecipients()[0].toString());
  }

  @Test
  void failedPasswordVerificationAsEmailDoesntExist() throws Exception {
    ConfirmEmailRequestDto requestDto = new ConfirmEmailRequestDto("Example@mail.ru");
    assumeFalse(userRepository.existsByEmailIgnoreCase(requestDto.getEmail()));

    resultActions.passwordVerificationResultActions(requestDto).andExpect(status().isNotFound());
  }

  @Test
  void successfulPasswordRestoring() throws Exception {
    ConfirmationToken confirmationToken = userControllerTestParameters
            .getConfirmationTokenToConfirmAccount();
    User userWithPasswordEncoded =
        userControllerTestParameters
                .getSavedUserWithId(confirmationToken.getUser().getId());
    User userWithoutEncodedPassword =
        userControllerTestParameters
                .getUserByEmail(userWithPasswordEncoded.getEmail());
    RestorePasswordRequestDto requestDto =
        new RestorePasswordRequestDto(
            confirmationToken.getToken(), userWithoutEncodedPassword.getPassword() + ".a");

    resultActions.passwordRestoringResultActions(requestDto).andExpect(status().isOk());
    assertTrue(
        encoder.matches(
            userWithoutEncodedPassword.getPassword() + ".a",
            userControllerTestParameters

                .getSavedUserWithId(confirmationToken.getUser().getId())
                .getPassword()));
  }

  @Nested
  class ImageTest {

    private static final String ORIGINAL_IMAGES_PATH = "user/%s/original";
    private static final String THUMBNAIL_IMAGES_PATH = "user/%s/thumbnail";
    private static final String[] TEST_IMAGES = {"valid_image.jpg", "invalid_image_size.jpg"};

    @BeforeEach
    void setUp() {
      cleanS3Folder();
    }

    @AfterAll
    static void afterAll() throws IOException {
      for (String image : TEST_IMAGES) {
        Files.deleteIfExists(Paths.get(image));
      }
    }

    @Test
    void successfulImageUploading() throws Exception {
      User user = userControllerTestParameters.getSavedUser();
      UUID userId = user.getId();
      MockMultipartFile file =
          userControllerTestParameters.getMultiPartFile(TEST_IMAGES[0], IMAGE_JPEG_VALUE);

      resultActions
          .imageUploadingResultActions(file, userId, user)
          .andExpect(status().isNoContent());
      assertTrue(
          amazonS3.doesObjectExist(
              s3ConfigProperties.getBucketName(), String.format(ORIGINAL_IMAGES_PATH, userId)));
      assertTrue(
          amazonS3.doesObjectExist(
              s3ConfigProperties.getBucketName(), String.format(THUMBNAIL_IMAGES_PATH, userId)));
    }

    @Test
    void failedImageUploadingAsImageIsInvalid() throws Exception {
      User user = userControllerTestParameters.getSavedUser();
      UUID userId = user.getId();
      String filename = TEST_IMAGES[1];
      MockMultipartFile file =
          userControllerTestParameters.getMultiPartFile(filename, IMAGE_JPEG_VALUE);

      resultActions
          .imageUploadingResultActions(file, userId, user)
          .andExpect(status().isPreconditionFailed());
      assertFalse(
          amazonS3.doesObjectExist(
              s3ConfigProperties.getBucketName(), String.format(ORIGINAL_IMAGES_PATH, userId)));
      assertFalse(
          amazonS3.doesObjectExist(
              s3ConfigProperties.getBucketName(), String.format(THUMBNAIL_IMAGES_PATH, userId)));
      Files.delete(Paths.get(filename));
    }

    @Test
    void successfulOriginalImageDownloading() throws Exception {
      User savedUser = userControllerTestParameters.getSavedUser();
      UUID userId = savedUser.getId();
      MultipartFile file =
          userControllerTestParameters.getMultiPartFile(TEST_IMAGES[0], IMAGE_JPEG_VALUE);
      amazonS3.putObject(
          s3ConfigProperties.getBucketName(),
          String.format(ORIGINAL_IMAGES_PATH, userId),
          convertMultipartFileToFile(file));

      resultActions
          .imageDownloadingResultActions("image", userId, savedUser)
          .andExpect(status().isOk());
    }

    @Test
    void failedOriginalImageDownloadingAsPermissionDenied() throws Exception {
      resultActions
          .imageDownloadingResultActions(
              "image", UUID.randomUUID(), userControllerTestParameters.getSavedUser())
          .andExpect(status().isForbidden());
    }

    @Test
    void successfulThumbnailImageDownloading() throws Exception {
      User savedUser = userControllerTestParameters.getSavedUser();
      MultipartFile file =
          userControllerTestParameters.getMultiPartFile(TEST_IMAGES[0], IMAGE_JPEG_VALUE);
      amazonS3.putObject(
          s3ConfigProperties.getBucketName(),
          String.format(THUMBNAIL_IMAGES_PATH, savedUser.getId()),
          convertMultipartFileToFile(file));

      resultActions
          .imageDownloadingResultActions("thumbnail_image", savedUser.getId(), savedUser)
          .andExpect(status().isOk());
    }

    @Test

    void successfulImageDeletion() throws Exception {
      User savedUser = userControllerTestParameters.getSavedUser();
      UUID userId = savedUser.getId();
      MultipartFile file =
          userControllerTestParameters.getMultiPartFile(TEST_IMAGES[0], IMAGE_JPEG_VALUE);

      amazonS3.putObject(
          s3ConfigProperties.getBucketName(),
          String.format(ORIGINAL_IMAGES_PATH, userId),
          convertMultipartFileToFile(file));
      amazonS3.putObject(
          s3ConfigProperties.getBucketName(),
          String.format(THUMBNAIL_IMAGES_PATH, userId),
          convertMultipartFileToFile(file));

      assumeTrue(
          amazonS3.doesObjectExist(
              s3ConfigProperties.getBucketName(), String.format(THUMBNAIL_IMAGES_PATH, userId)));

      resultActions.imageDeletionResultActions(userId, savedUser).andExpect(status().isNoContent());
      assertFalse(
          amazonS3.doesObjectExist(
              s3ConfigProperties.getBucketName(), String.format(ORIGINAL_IMAGES_PATH, userId)));
      assertFalse(
          amazonS3.doesObjectExist(
              s3ConfigProperties.getBucketName(), String.format(THUMBNAIL_IMAGES_PATH, userId)));
    }

    @Test
    void failedImageDeletingAsImageDoesntExist() throws Exception {
      User savedUser = userControllerTestParameters.getSavedUser();
      UUID userId = savedUser.getId();

      assumeFalse(
          amazonS3.doesObjectExist(
              s3ConfigProperties.getBucketName(), String.format(ORIGINAL_IMAGES_PATH, userId)));
      assumeFalse(
          amazonS3.doesObjectExist(
              s3ConfigProperties.getBucketName(), String.format(THUMBNAIL_IMAGES_PATH, userId)));

      resultActions.imageDeletionResultActions(userId, savedUser).andExpect(status().isNotFound());
    }

    private void cleanS3Folder() {
      String bucketName = s3ConfigProperties.getBucketName();
      for (S3ObjectSummary file : amazonS3.listObjects(bucketName, "user").getObjectSummaries()) {
        amazonS3.deleteObject(bucketName, file.getKey());
      }
    }
  }
}
