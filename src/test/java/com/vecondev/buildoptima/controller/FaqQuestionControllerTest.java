package com.vecondev.buildoptima.controller;

import com.vecondev.buildoptima.config.properties.JwtConfigProperties;
import com.vecondev.buildoptima.dto.request.faq.FaqQuestionRequestDto;
import com.vecondev.buildoptima.manager.JwtTokenManager;
import com.vecondev.buildoptima.model.faq.FaqQuestion;
import com.vecondev.buildoptima.model.user.Role;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.faq.question.FaqQuestionControllerTestParameters;
import com.vecondev.buildoptima.repository.faq.FaqCategoryRepository;
import com.vecondev.buildoptima.repository.faq.FaqQuestionRepository;
import com.vecondev.buildoptima.repository.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static com.vecondev.buildoptima.model.user.Role.CLIENT;
import static com.vecondev.buildoptima.model.user.Role.MODERATOR;
import static com.vecondev.buildoptima.util.TestUtil.asJsonString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
@ActiveProfiles("test")
class FaqQuestionControllerTest {

  @Autowired private MockMvc mvc;

  @Autowired private UserRepository userRepository;
  @Autowired private FaqCategoryRepository faqCategoryRepository;
  @Autowired private FaqQuestionRepository faqQuestionRepository;

  @Autowired private PasswordEncoder encoder;
  @Autowired private JwtConfigProperties jwtConfigProperties;
  @Autowired private JwtTokenManager tokenManager;
  private FaqQuestionControllerTestParameters testParameters;

  @BeforeEach
  void setUp() {
    testParameters = new FaqQuestionControllerTestParameters(userRepository, faqCategoryRepository);
    List<User> users = testParameters.users();
    users.forEach(user -> user.setPassword(encoder.encode(user.getPassword())));
    userRepository.saveAll(users);

    faqCategoryRepository.saveAll(testParameters.faqCategories());
    faqQuestionRepository.saveAll(testParameters.faqQuestions());
  }

  @AfterEach
  void tearDown() {
    faqQuestionRepository.deleteAll();
    faqCategoryRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void successfulRetrievalOfAllCategories() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);

    mvc.perform(
            get("/faq/question")
                .header(jwtConfigProperties.getAuthorizationHeader(), getAccessToken(moderatorUser))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.*", hasSize(faqQuestionRepository.findAll().size())));
  }

  @Test
  void failedRetrievalOfAllCategoriesAsAccessDenied() throws Exception {
    User clientUser = getUserByRole(CLIENT);
    assumeFalse(clientUser == null);

    mvc.perform(
            get("/faq/question")
                .header(jwtConfigProperties.getAuthorizationHeader(), getAccessToken(clientUser))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isForbidden());
  }

  @Test
  void successfulRetrievalOfQuestionById() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqQuestion faqQuestion = faqQuestionRepository.findAll().stream().findAny().orElse(null);
    assumeFalse(faqQuestion == null);

    mvc.perform(
            get("/faq/question/{id}", faqQuestion.getId())
                .header(jwtConfigProperties.getAuthorizationHeader(), getAccessToken(moderatorUser))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.category.id").value(faqQuestion.getCategory().getId().toString()))
        .andExpect(jsonPath("$.question").value(faqQuestion.getQuestion()));
  }

  @Test
  void failedRetrievalOfQuestionByIdAsQuestionNotFound() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);

    mvc.perform(
            get("/faq/question/{id}", UUID.randomUUID())
                .header(jwtConfigProperties.getAuthorizationHeader(), getAccessToken(moderatorUser))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isNotFound());
  }

  @Test
  void successfulCreationOfFaqQuestion() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);

    FaqQuestionRequestDto requestDto = testParameters.getFaqQuestionToSave();

    mvc.perform(
            post("/faq/question")
                .header(jwtConfigProperties.getAuthorizationHeader(), getAccessToken(moderatorUser))
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNotEmpty());
  }

  @Test
  void failedCreationOfFaqQuestionAsQuestionAlreadyExists() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);

    FaqQuestionRequestDto requestDto = testParameters.getFaqQuestionWithDuplicatedQuestion();

    mvc.perform(
            post("/faq/question")
                .header(jwtConfigProperties.getAuthorizationHeader(), getAccessToken(moderatorUser))
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isConflict());
  }

  @Test
  void failedCreationOfFaqQuestionAsQuestionIsInvalid() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);

    FaqQuestionRequestDto requestDto = new FaqQuestionRequestDto();

    mvc.perform(
            post("/faq/question")
                .header(jwtConfigProperties.getAuthorizationHeader(), getAccessToken(moderatorUser))
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }

  @Test
  void successfulUpdateOfFaqQuestion() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqQuestion faqQuestion = faqQuestionRepository.findAll().stream().findAny().orElse(null);
    assumeFalse(faqQuestion == null);
    FaqQuestionRequestDto requestDto = testParameters.getFaqQuestionToSave();

    mvc.perform(
            put("/faq/question/{id}", faqQuestion.getId().toString())
                .header(jwtConfigProperties.getAuthorizationHeader(), getAccessToken(moderatorUser))
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.question").value(requestDto.getQuestion()));
  }

  @Test
  void failedUpdateOfFaqQuestionAsQuestionNotFound() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqQuestionRequestDto requestDto = testParameters.getFaqQuestionToSave();

    mvc.perform(
            put("/faq/question/{id}", UUID.randomUUID())
                .header(jwtConfigProperties.getAuthorizationHeader(), getAccessToken(moderatorUser))
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isNotFound());
  }

  @Test
  void successfulDeletionOfFaqQuestion() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqQuestion faqQuestion = faqQuestionRepository.findAll().stream().findAny().orElse(null);
    assumeFalse(faqQuestion == null);

    mvc.perform(
            delete("/faq/question/{id}", faqQuestion.getId().toString())
                .header(jwtConfigProperties.getAuthorizationHeader(), getAccessToken(moderatorUser))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());

    assertFalse(faqQuestionRepository.existsById(faqQuestion.getId()));
  }

  @Test
  void failedDeletionOfFaqQuestionAsQuestionNotFound() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);

    mvc.perform(
            delete("/faq/question/{id}", UUID.randomUUID())
                .header(jwtConfigProperties.getAuthorizationHeader(), getAccessToken(moderatorUser))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isNotFound());
  }

  private User getUserByRole(Role role) {
    return userRepository.findByRole(role).orElse(null);
  }

  private String getAccessToken(User user) {
    return jwtConfigProperties.getAuthorizationHeaderPrefix()
        + tokenManager.generateAccessToken(user);
  }
}
