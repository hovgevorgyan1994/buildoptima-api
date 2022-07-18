//package com.vecondev.buildoptima.controller;
//
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.model.S3ObjectSummary;
//import com.icegreen.greenmail.configuration.GreenMailConfiguration;
//import com.icegreen.greenmail.junit5.GreenMailExtension;
//import com.icegreen.greenmail.util.ServerSetupTest;
//import com.vecondev.buildoptima.config.AmazonS3Config;
//import com.vecondev.buildoptima.config.properties.JwtConfigProperties;
//import com.vecondev.buildoptima.config.properties.S3ConfigProperties;
//import com.vecondev.buildoptima.dto.request.user.AuthRequestDto;
//import com.vecondev.buildoptima.dto.request.user.ChangePasswordRequestDto;
//import com.vecondev.buildoptima.dto.request.user.ConfirmEmailRequestDto;
//import com.vecondev.buildoptima.dto.request.filter.FetchRequestDto;
//import com.vecondev.buildoptima.dto.request.user.RefreshTokenRequestDto;
//import com.vecondev.buildoptima.dto.request.user.RestorePasswordRequestDto;
//import com.vecondev.buildoptima.dto.request.user.UserRegistrationRequestDto;
//import com.vecondev.buildoptima.exception.UserNotFoundException;
//import com.vecondev.buildoptima.manager.JwtTokenManager;
//import com.vecondev.buildoptima.model.user.ConfirmationToken;
//import com.vecondev.buildoptima.model.user.Role;
//import com.vecondev.buildoptima.model.user.User;
//import com.vecondev.buildoptima.parameters.user.UserControllerTestParameters;
//import com.vecondev.buildoptima.repository.user.ConfirmationTokenRepository;
//import com.vecondev.buildoptima.repository.user.RefreshTokenRepository;
//import com.vecondev.buildoptima.repository.user.UserRepository;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.jupiter.api.extension.RegisterExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.mail.internet.MimeMessage;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//
//import static com.vecondev.buildoptima.exception.ErrorCode.USER_NOT_FOUND;
//import static com.vecondev.buildoptima.model.user.Role.ADMIN;
//import static com.vecondev.buildoptima.model.user.Role.CLIENT;
//import static com.vecondev.buildoptima.util.FileUtil.convertMultipartFileToFile;
//import static com.vecondev.buildoptima.util.TestUtil.asJsonString;
//import static org.hamcrest.Matchers.containsString;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.junit.jupiter.api.Assumptions.assumeFalse;
//import static org.junit.jupiter.api.Assumptions.assumeTrue;
//import static org.springframework.http.MediaType.APPLICATION_JSON;
//import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
//import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
//import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@Slf4j
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
//@AutoConfigureMockMvc
//@ExtendWith({SpringExtension.class})
//@ActiveProfiles("test")
//@Import(AmazonS3Config.class)
//class UserControllerTest {
//
//  @RegisterExtension
//  private static GreenMailExtension greenMail =
//      new GreenMailExtension(ServerSetupTest.SMTP)
//          .withConfiguration(
//              GreenMailConfiguration.aConfig()
//                  .withUser("managementstaffing09@gmail.com", "buildoptima"))
//          .withPerMethodLifecycle(true);
//
//  @Autowired private MockMvc mvc;
//  @Autowired private AmazonS3 amazonS3;
//
//  @Autowired private UserRepository userRepository;
//  @Autowired private ConfirmationTokenRepository confirmationTokenRepository;
//  @Autowired private RefreshTokenRepository refreshTokenRepository;
//
//  @Autowired private JwtTokenManager tokenManager;
//  @Autowired private PasswordEncoder encoder;
//  @Autowired private JwtConfigProperties jwtConfigProperties;
//  @Autowired private S3ConfigProperties s3ConfigProperties;
//  ;
//
//  private UserControllerTestParameters userControllerTestParameters;
//
//  @BeforeEach
//  void setUp() {
//    userControllerTestParameters =
//        new UserControllerTestParameters(
//            userRepository, confirmationTokenRepository, refreshTokenRepository);
//
//    List<User> users = userControllerTestParameters.users();
//    users.forEach(user -> user.setPassword(encoder.encode(user.getPassword())));
//    userRepository.saveAll(users);
//
//    confirmationTokenRepository.saveAll(userControllerTestParameters.confirmationTokens());
//    refreshTokenRepository.saveAll(userControllerTestParameters.refreshTokens());
//  }
//
//  @AfterEach
//  void tearDown() {
//    refreshTokenRepository.deleteAll();
//    confirmationTokenRepository.deleteAll();
//    userRepository.deleteAll();
//  }
//
//  @Test
//  void successfulRegistration() throws Exception {
//    UserRegistrationRequestDto requestDto = userControllerTestParameters
//            .getUserToSave();
//
//    mvc.perform(
//            post("/user/auth/registration")
//                .content(asJsonString(requestDto))
//                .contentType(APPLICATION_JSON)
//                .accept(APPLICATION_JSON_VALUE))
//        .andExpect(status().isCreated())
//        .andExpect(jsonPath("$.firstName").exists());
//
//    MimeMessage[] messages = greenMail.getReceivedMessages();
//    assertEquals(1, messages.length);
//    assertEquals(requestDto.getEmail(), messages[0].getAllRecipients()[0].toString());
//  }
//
//  @Test
//  void failedRegistrationAsBodyValuesAreInvalid() throws Exception {
//    UserRegistrationRequestDto requestDto = userControllerTestParameters
//            .getUserToSaveWithInvalidFields();
//    mvc.perform(
//            post("/user/auth/registration")
//                .content(asJsonString(requestDto))
//                .contentType(APPLICATION_JSON)
//                .accept(APPLICATION_JSON))
//        .andExpect(status().isBadRequest())
//        .andExpect(jsonPath("$.errors").exists());
//  }
//
//  @Test
//  void failedRegistrationAsEmailAlreadyExist() throws Exception {
//    UserRegistrationRequestDto requestDto = userControllerTestParameters
//            .getUserToSaveWithDuplicatedEmail();
//    mvc.perform(
//            post("/user/auth/registration")
//                .content(asJsonString(requestDto))
//                .contentType(APPLICATION_JSON)
//                .accept(APPLICATION_JSON))
//        .andExpect(status().isConflict())
//        .andExpect(jsonPath("$.message").value(containsString("email")));
//  }
//
//  @Test
//  void successfulActivation() throws Exception {
//    ConfirmationToken confirmationToken = userControllerTestParameters
//            .getConfirmationTokenToConfirmAccount();
//    UUID userId = confirmationToken.getUser().getId();
//    assumeTrue(confirmationToken.getExpiresAt().isAfter(LocalDateTime.now()));
//    mvc.perform(
//            put("/user/auth/activate")
//                .param("token", confirmationToken.getToken())
//                .contentType(APPLICATION_JSON)
//                .accept(APPLICATION_JSON))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.id").value(userId.toString()));
//
//    assertEquals(
//        true,
//        userRepository
//            .findById(userId)
//            .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND))
//            .getEnabled());
//  }
//
//  @Test
//  void failedActivationAsTokenDoesntExist() throws Exception {
//    String token = UUID.randomUUID().toString();
//    mvc.perform(
//            put("/user/auth/activate")
//                .param("token", token)
//                .contentType(APPLICATION_JSON)
//                .accept(APPLICATION_JSON))
//        .andExpect(status().isNotFound());
//  }
//
//  @Test
//  void successfulLogin() throws Exception {
//    AuthRequestDto requestDto = userControllerTestParameters
//            .getUserCredentialsToLogin();
//    mvc.perform(
//            post("/user/auth/login")
//                .content(asJsonString(requestDto))
//                .contentType(APPLICATION_JSON)
//                .accept(APPLICATION_JSON))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.accessToken").exists())
//        .andExpect(jsonPath("$.refreshTokenId").exists());
//  }
//
//  @Test
//  void failedLoginAsUserDoesntExist() throws Exception {
//    AuthRequestDto requestDto = userControllerTestParameters
//            .getUserInvalidCredentialsToLogin();
//    assumeFalse(userRepository.existsByEmailIgnoreCase(requestDto.getUsername()));
//    mvc.perform(
//            post("/user/auth/login")
//                .content(asJsonString(requestDto))
//                .contentType(APPLICATION_JSON)
//                .accept(APPLICATION_JSON))
//        .andExpect(status().isUnauthorized());
//  }
//
//  @Test
//  void successfulRefreshmentOfTokens() throws Exception {
//    RefreshTokenRequestDto requestDto = userControllerTestParameters
//            .getRefreshToken();
//
//    mvc.perform(
//            post("/user/auth/refreshToken")
//                .content(asJsonString(requestDto))
//                .contentType(APPLICATION_JSON)
//                .accept(APPLICATION_JSON))
//        .andExpect(status().isOk());
//  }
//
//  @Test
//  void failedRefreshmentOfTokensAsRefreshTokenExpired() throws Exception {
//    RefreshTokenRequestDto requestDto = userControllerTestParameters
//            .getExpiredRefreshToken();
//
//    mvc.perform(
//            post("/user/auth/refreshToken")
//                .content(asJsonString(requestDto))
//                .contentType(APPLICATION_JSON)
//                .accept(APPLICATION_JSON))
//        .andExpect(status().isForbidden())
//        .andExpect(jsonPath("$.message").value(containsString("Expired")));
//  }
//
//  @Test
//  void successfulFetchingOfUsers() throws Exception {
//    FetchRequestDto requestDto = userControllerTestParameters
//            .getFetchRequest();
//    User adminUser = userControllerTestParameters
//            .getUser(ADMIN);
//    String json = asJsonString(requestDto);
//
//    mvc.perform(
//            post("/user/fetch")
//                .header(
//                    jwtConfigProperties.getAuthorizationHeader(),
//                    jwtConfigProperties.getAuthorizationHeaderPrefix()
//                        + tokenManager.generateAccessToken(adminUser))
//                .content(json)
//                .contentType(APPLICATION_JSON)
//                .accept(APPLICATION_JSON))
//        .andExpect(status().isOk());
//  }
//
//  @Test
//  void failedFetchingOfUsersAsPermissionDenied() throws Exception {
//    FetchRequestDto requestDto = userControllerTestParameters
//            .getFetchRequest();
//    User clientUser = userControllerTestParameters
//            .getUser(CLIENT);
//
//    mvc.perform(
//            post("/user/fetch")
//                .header(
//                    jwtConfigProperties.getAuthorizationHeader(),
//                    jwtConfigProperties.getAuthorizationHeaderPrefix()
//                        + tokenManager.generateAccessToken(clientUser))
//                .content(asJsonString(requestDto))
//                .contentType(APPLICATION_JSON)
//                .accept(APPLICATION_JSON))
//        .andExpect(status().isForbidden());
//  }
//
//  @Test
//  void failedFetchingOfUsersAsRequestDtoIsInvalid() throws Exception {
//    FetchRequestDto requestDto = userControllerTestParameters
//            .getInvalidFetchRequest();
//    User adminUser = userControllerTestParameters
//            .getUser(Role.ADMIN);
//
//    mvc.perform(
//            post("/user/fetch")
//                .header(
//                    jwtConfigProperties.getAuthorizationHeader(),
//                    jwtConfigProperties.getAuthorizationHeaderPrefix()
//                        + tokenManager.generateAccessToken(adminUser))
//                .content(asJsonString(requestDto))
//                .contentType(APPLICATION_JSON)
//                .accept(APPLICATION_JSON))
//        .andExpect(status().isBadRequest());
//  }
//
//  @Test
//  void successfulPasswordChanging() throws Exception {
//    User savedUser = userControllerTestParameters.getUser();
//    ChangePasswordRequestDto requestDto = userControllerTestParameters
//            .getChangePasswordRequestDto(savedUser);
//
//    mvc.perform(
//            put("/user/password/change")
//                .header(
//                    jwtConfigProperties.getAuthorizationHeader(),
//                    jwtConfigProperties.getAuthorizationHeaderPrefix()
//                        + tokenManager.generateAccessToken(savedUser))
//                .content(asJsonString(requestDto))
//                .contentType(APPLICATION_JSON)
//                .accept(APPLICATION_JSON))
//        .andExpect(status().isOk());
//  }
//
//  @Test
//  void successfulGettingUser() throws Exception {
//    User user = userControllerTestParameters
//            .getSavedUser();
//    User adminUser = userControllerTestParameters.getSavedUser(ADMIN);
//
//    mvc.perform(
//            get("/user/fetch/{id}", user.getId().toString())
//                .header(
//                    jwtConfigProperties.getAuthorizationHeader(),
//                    jwtConfigProperties.getAuthorizationHeaderPrefix()
//                        + tokenManager.generateAccessToken(adminUser))
//                .contentType(APPLICATION_JSON)
//                .accept(APPLICATION_JSON))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.id").value(user.getId().toString()));
//  }
//
//  @Test
//  void failedGettingUserAsPermissionDenied() throws Exception {
//    User user = userControllerTestParameters
//            .getSavedUser();
//
//    mvc.perform(
//            get("/user/fetch/{id}", UUID.randomUUID().toString())
//                .header(
//                    jwtConfigProperties.getAuthorizationHeader(),
//                    jwtConfigProperties.getAuthorizationHeaderPrefix()
//                        + tokenManager.generateAccessToken(user))
//                .contentType(APPLICATION_JSON)
//                .accept(APPLICATION_JSON))
//        .andExpect(status().isNotFound());
//  }
//
//  @Test
//  void successfulPasswordVerification() throws Exception {
//    User user = userControllerTestParameters.getSavedUser();
//    ConfirmEmailRequestDto requestDto = new ConfirmEmailRequestDto(user.getEmail());
//
//    mvc.perform(
//            post("/user/auth/password/verify")
//                .content(asJsonString(requestDto))
//                .contentType(APPLICATION_JSON)
//                .accept(APPLICATION_JSON))
//        .andExpect(status().isOk());
//
//    MimeMessage[] messages = greenMail.getReceivedMessages();
//    assertEquals(1, messages.length);
//    assertEquals(user.getEmail(), messages[0].getAllRecipients()[0].toString());
//  }
//
//  @Test
//  void failedPasswordVerificationAsEmailDoesntExist() throws Exception {
//    String email = "Example@mail.ru";
//    assumeFalse(userRepository.existsByEmailIgnoreCase(email));
//
//    mvc.perform(
//            post("/user/auth/password/verify")
//                .param("email", email)
//                .contentType(APPLICATION_JSON)
//                .accept(APPLICATION_JSON))
//        .andExpect(status().isBadRequest());
//  }
//
//  @Test
//  void successfulPasswordRestoring() throws Exception {
//    ConfirmationToken confirmationToken = userControllerTestParameters
//            .getConfirmationTokenToConfirmAccount();
//    User userWithPasswordEncoded =
//        userControllerTestParameters
//                .getSavedUserWithId(confirmationToken.getUser().getId());
//    User userWithoutEncodedPassword =
//        userControllerTestParameters
//                .getUserByEmail(userWithPasswordEncoded.getEmail());
//    RestorePasswordRequestDto requestDto =
//        new RestorePasswordRequestDto(
//            confirmationToken.getToken(), userWithoutEncodedPassword.getPassword() + ".a");
//
//    mvc.perform(
//            put("/user/auth/password/restore")
//                .content(asJsonString(requestDto))
//                .contentType(APPLICATION_JSON)
//                .accept(APPLICATION_JSON))
//        .andExpect(status().isOk());
//
//    assertTrue(
//        encoder.matches(
//            userWithoutEncodedPassword.getPassword() + ".a",
//            userControllerTestParameters
//                .getSavedUserWithId(confirmationToken.getUser().getId())
//                .getPassword()));
//  }
//
//  @Nested
//  class ImageTest {
//
//    private static final String[] TEST_IMAGES = {"valid_image.jpg", "invalid_image_size.jpg"};
//
//    @BeforeEach
//    void setUp() {
//      cleanS3Folder(s3ConfigProperties.getOriginalImagePath());
//      cleanS3Folder(s3ConfigProperties.getThumbnailImagePath());
//    }
//
//    @AfterAll
//    static void afterAll() throws IOException {
//      for (String image : TEST_IMAGES) {
//        Files.deleteIfExists(Paths.get(image));
//      }
//    }
//
//    @Test
//    void successfulImageUploading() throws Exception {
//      User user = userControllerTestParameters.getSavedUser();
//      UUID userId = user.getId();
//      MockMultipartFile file =
//          userControllerTestParameters.getMultiPartFile(TEST_IMAGES[0], IMAGE_JPEG_VALUE);
//
//      mvc.perform(
//              multipart("/user/{id}/image", userId.toString())
//                  .file(file)
//                  .header(
//                      jwtConfigProperties.getAuthorizationHeader(),
//                      jwtConfigProperties.getAuthorizationHeaderPrefix()
//                          + tokenManager.generateAccessToken(user))
//                  .contentType(MULTIPART_FORM_DATA)
//                  .accept(APPLICATION_JSON))
//          .andExpect(status().isNoContent());
//
//      assertTrue(
//          amazonS3.doesObjectExist(
//              s3ConfigProperties.getBucketName(),
//              s3ConfigProperties.getOriginalImagePath() + userId));
//      assertTrue(
//          amazonS3.doesObjectExist(
//              s3ConfigProperties.getBucketName(),
//              s3ConfigProperties.getThumbnailImagePath() + userId));
//    }
//
//    @Test
//    void failedImageUploadingAsImageIsInvalid() throws Exception {
//      User user = userControllerTestParameters.getSavedUser();
//      UUID userId = user.getId();
//      String filename = TEST_IMAGES[1];
//      MockMultipartFile file =
//          userControllerTestParameters.getMultiPartFile(filename, IMAGE_JPEG_VALUE);
//
//      mvc.perform(
//              multipart("/user/{id}/image", userId)
//                  .file(file)
//                  .header(
//                      jwtConfigProperties.getAuthorizationHeader(),
//                      jwtConfigProperties.getAuthorizationHeaderPrefix()
//                          + tokenManager.generateAccessToken(user))
//                  .contentType(MULTIPART_FORM_DATA)
//                  .accept("*/*"))
//          .andExpect(status().isPreconditionFailed());
//
//      assertFalse(
//          amazonS3.doesObjectExist(
//              s3ConfigProperties.getBucketName(),
//              s3ConfigProperties.getOriginalImagePath() + userId));
//      assertFalse(
//          amazonS3.doesObjectExist(
//              s3ConfigProperties.getBucketName(),
//              s3ConfigProperties.getThumbnailImagePath() + userId));
//      Files.delete(Paths.get(filename));
//    }
//
//    @Test
//    void successfulOriginalImageDownloading() throws Exception {
//      User savedUser = userControllerTestParameters.getSavedUser();
//      MultipartFile file =
//          userControllerTestParameters.getMultiPartFile(TEST_IMAGES[0], IMAGE_JPEG_VALUE);
//      amazonS3.putObject(
//          s3ConfigProperties.getBucketName(),
//          s3ConfigProperties.getOriginalImagePath() + savedUser.getId().toString(),
//          convertMultipartFileToFile(file));
//
//      mvc.perform(
//              get("/user/{id}/image", savedUser.getId())
//                  .header(
//                      jwtConfigProperties.getAuthorizationHeader(),
//                      jwtConfigProperties.getAuthorizationHeaderPrefix()
//                          + tokenManager.generateAccessToken(savedUser))
//                  .contentType(APPLICATION_JSON)
//                  .accept("*/*"))
//          .andExpect(status().isOk());
//    }
//
//    @Test
//    void failedOriginalImageDownloadingAsPermissionDenied() throws Exception {
//      mvc.perform(
//              get("/user/{id}/image", UUID.randomUUID().toString())
//                  .header(
//                      jwtConfigProperties.getAuthorizationHeader(),
//                      jwtConfigProperties.getAuthorizationHeaderPrefix()
//                          + tokenManager.generateAccessToken(
//                              userControllerTestParameters.getSavedUser()))
//                  .contentType(APPLICATION_JSON)
//                  .accept("*/*"))
//          .andExpect(status().isForbidden());
//    }
//
//    @Test
//    void successfulThumbnailImageDownloading() throws Exception {
//      User savedUser = userControllerTestParameters.getSavedUser();
//      MultipartFile file =
//          userControllerTestParameters.getMultiPartFile(TEST_IMAGES[0], IMAGE_JPEG_VALUE);
//      amazonS3.putObject(
//          s3ConfigProperties.getBucketName(),
//          s3ConfigProperties.getThumbnailImagePath() + savedUser.getId().toString(),
//          convertMultipartFileToFile(file));
//
//      mvc.perform(
//              get("/user/{id}/thumbnail_image", savedUser.getId())
//                  .header(
//                      jwtConfigProperties.getAuthorizationHeader(),
//                      jwtConfigProperties.getAuthorizationHeaderPrefix()
//                          + tokenManager.generateAccessToken(savedUser))
//                  .contentType(APPLICATION_JSON)
//                  .accept("*/*"))
//          .andExpect(status().isOk());
//    }
//
//    @Test
//    void successfulImageDeletion() throws Exception {
//      User savedUser = userControllerTestParameters.getSavedUser();
//      MultipartFile file =
//          userControllerTestParameters.getMultiPartFile(TEST_IMAGES[0], IMAGE_JPEG_VALUE);
//
//      amazonS3.putObject(
//          s3ConfigProperties.getBucketName(),
//          s3ConfigProperties.getOriginalImagePath() + savedUser.getId().toString(),
//          convertMultipartFileToFile(file));
//      amazonS3.putObject(
//          s3ConfigProperties.getBucketName(),
//          s3ConfigProperties.getThumbnailImagePath() + savedUser.getId().toString(),
//          convertMultipartFileToFile(file));
//
//      assumeTrue(
//          amazonS3.doesObjectExist(
//              s3ConfigProperties.getBucketName(),
//              s3ConfigProperties.getThumbnailImagePath() + savedUser.getId().toString()));
//      mvc.perform(
//              delete("/user/{id}/image", savedUser.getId().toString())
//                  .header(
//                      jwtConfigProperties.getAuthorizationHeader(),
//                      jwtConfigProperties.getAuthorizationHeaderPrefix()
//                          + tokenManager.generateAccessToken(savedUser))
//                  .contentType(APPLICATION_JSON)
//                  .accept(APPLICATION_JSON))
//          .andExpect(status().isNoContent());
//      assertFalse(
//          amazonS3.doesObjectExist(
//              s3ConfigProperties.getBucketName(),
//              s3ConfigProperties.getThumbnailImagePath() + savedUser.getId()));
//      assertFalse(
//          amazonS3.doesObjectExist(
//              s3ConfigProperties.getBucketName(),
//              s3ConfigProperties.getOriginalImagePath() + savedUser.getId()));
//    }
//
//    @Test
//    void failedImageDeletingAsImageDoesntExist() throws Exception {
//      User savedUser = userControllerTestParameters.getSavedUser();
//
//      assumeFalse(
//          amazonS3.doesObjectExist(
//              s3ConfigProperties.getBucketName(),
//              s3ConfigProperties.getThumbnailImagePath() + savedUser.getId()));
//      assumeFalse(
//          amazonS3.doesObjectExist(
//              s3ConfigProperties.getBucketName(),
//              s3ConfigProperties.getOriginalImagePath() + savedUser.getId()));
//      mvc.perform(
//              delete("/user/{id}/image", savedUser.getId())
//                  .header(
//                      jwtConfigProperties.getAuthorizationHeader(),
//                      jwtConfigProperties.getAuthorizationHeaderPrefix()
//                          + tokenManager.generateAccessToken(savedUser))
//                  .contentType(APPLICATION_JSON)
//                  .accept(APPLICATION_JSON))
//          .andExpect(status().isNotFound());
//    }
//
//    private void cleanS3Folder(String folderPath) {
//      String bucketName = s3ConfigProperties.getBucketName();
//      for (S3ObjectSummary file :
//          amazonS3.listObjects(bucketName, folderPath).getObjectSummaries()) {
//        amazonS3.deleteObject(bucketName, file.getKey());
//      }
//    }
//  }
//}
