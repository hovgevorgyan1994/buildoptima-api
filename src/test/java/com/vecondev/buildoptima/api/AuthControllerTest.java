package com.vecondev.buildoptima.api;

import static com.vecondev.buildoptima.exception.Error.USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.vecondev.buildoptima.actions.UserResultActions;
import com.vecondev.buildoptima.config.AmazonS3Config;
import com.vecondev.buildoptima.dto.user.request.AuthRequestDto;
import com.vecondev.buildoptima.dto.user.request.ConfirmEmailRequestDto;
import com.vecondev.buildoptima.dto.user.request.RefreshTokenRequestDto;
import com.vecondev.buildoptima.dto.user.request.RestorePasswordRequestDto;
import com.vecondev.buildoptima.dto.user.request.UserRegistrationRequestDto;
import com.vecondev.buildoptima.endpoints.UserEndpointUris;
import com.vecondev.buildoptima.exception.UserNotFoundException;
import com.vecondev.buildoptima.model.user.ConfirmationToken;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.user.UserControllerTestParameters;
import com.vecondev.buildoptima.repository.user.ConfirmationTokenRepository;
import com.vecondev.buildoptima.repository.user.RefreshTokenRepository;
import com.vecondev.buildoptima.repository.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
@ActiveProfiles("test")
@Import({AmazonS3Config.class, UserEndpointUris.class, UserResultActions.class})
class AuthControllerTest {

  private static final String MAIL_EXAMPLE = "Example@mail.ru";
  private static final String GREENMAIL_EMAIL_ADDRESS = "buildoptima-test@gmail.com";
  private static final String GREENMAIL_EMAIL_PASSWORD = "buildoptima";

  @RegisterExtension
  private static final GreenMailExtension greenMail =
      new GreenMailExtension(ServerSetupTest.SMTP)
          .withConfiguration(
              GreenMailConfiguration.aConfig()
                  .withUser(GREENMAIL_EMAIL_ADDRESS, GREENMAIL_EMAIL_PASSWORD))
          .withPerMethodLifecycle(true);

  private UserControllerTestParameters userControllerTestParameters;
  @Autowired private UserRepository userRepository;
  @Autowired private ConfirmationTokenRepository confirmationTokenRepository;
  @Autowired private RefreshTokenRepository refreshTokenRepository;
  @Autowired private PasswordEncoder encoder;
  @Autowired private UserResultActions resultActions;


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
    UserRegistrationRequestDto requestDto = userControllerTestParameters.getUserToSave();

    resultActions
        .register(requestDto)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.firstName").exists());

    MimeMessage[] messages = greenMail.getReceivedMessages();
    assertEquals(1, messages.length);
    assertEquals(requestDto.getEmail(), messages[0].getAllRecipients()[0].toString());
  }

  @Test
  void failedRegistrationAsBodyValuesAreInvalid() throws Exception {
    UserRegistrationRequestDto requestDto =
        userControllerTestParameters.getUserToSaveWithInvalidFields();

    resultActions
        .register(requestDto)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").exists());
  }

  @Test
  void failedRegistrationAsEmailAlreadyExist() throws Exception {
    UserRegistrationRequestDto requestDto =
        userControllerTestParameters.getUserToSaveWithDuplicatedEmail();

    resultActions.register(requestDto).andExpect(status().isConflict());
  }

  @Test
  void successfulActivation() throws Exception {
    ConfirmationToken confirmationToken =
        userControllerTestParameters.getConfirmationTokenToConfirmAccount();
    UUID userId = confirmationToken.getUser().getId();
    assumeTrue(confirmationToken.getExpiresAt().isAfter(LocalDateTime.now()));

    resultActions
        .activate(confirmationToken.getToken())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.toString()));

    assertTrue(
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND))
            .isEnabled());
  }

  @Test
  void failedActivationAsTokenDoesntExist() throws Exception {
    String token = UUID.randomUUID().toString();

    resultActions.activate(token).andExpect(status().isNotFound());
  }

  @Test
  void successfulLogin() throws Exception {
    AuthRequestDto requestDto = userControllerTestParameters.getUserCredentialsToLogin();

    resultActions
        .login(requestDto)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.refreshToken").exists());
  }

  @Test
  void failedLoginAsUserDoesntExist() throws Exception {
    AuthRequestDto requestDto = userControllerTestParameters.getUserInvalidCredentialsToLogin();
    assumeFalse(userRepository.existsByEmailIgnoreCase(requestDto.getUsername()));

    resultActions.login(requestDto).andExpect(status().isUnauthorized());
  }

  @Test
  void successfulRefreshmentOfTokens() throws Exception {
    RefreshTokenRequestDto requestDto = userControllerTestParameters.getRefreshToken();

    resultActions.refresh(requestDto).andExpect(status().isOk());
  }

  @Test
  void successfulPasswordVerification() throws Exception {
    User user = userControllerTestParameters.getSavedUser();
    ConfirmEmailRequestDto requestDto = new ConfirmEmailRequestDto(user.getEmail());

    resultActions.verify(requestDto).andExpect(status().isOk());
    MimeMessage[] messages = greenMail.getReceivedMessages();
    assertEquals(1, messages.length);
    assertEquals(user.getEmail(), messages[0].getAllRecipients()[0].toString());
  }

  @Test
  void failedPasswordVerificationAsEmailDoesntExist() throws Exception {
    ConfirmEmailRequestDto requestDto = new ConfirmEmailRequestDto(MAIL_EXAMPLE);
    assumeFalse(userRepository.existsByEmailIgnoreCase(requestDto.getEmail()));

    resultActions.verify(requestDto).andExpect(status().isNotFound());
  }

  @Test
  void successfulPasswordRestoring() throws Exception {
    ConfirmationToken confirmationToken =
        userControllerTestParameters.getConfirmationTokenToConfirmAccount();
    User userWithPasswordEncoded =
        userControllerTestParameters.getSavedUserWithId(confirmationToken.getUser().getId());
    User userWithoutEncodedPassword =
        userControllerTestParameters.getUserByEmail(userWithPasswordEncoded.getEmail());
    RestorePasswordRequestDto requestDto =
        new RestorePasswordRequestDto(
            confirmationToken.getToken(), userWithoutEncodedPassword.getPassword() + ".a");

    resultActions.restorePassword(requestDto).andExpect(status().isOk());
    assertTrue(
        encoder.matches(
            userWithoutEncodedPassword.getPassword() + ".a",
            userControllerTestParameters
                .getSavedUserWithId(confirmationToken.getUser().getId())
                .getPassword()));
  }
}
