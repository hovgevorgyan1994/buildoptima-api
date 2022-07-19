package com.vecondev.buildoptima.controller;

import com.vecondev.buildoptima.dto.request.FetchRequestDto;
import com.vecondev.buildoptima.dto.request.faq.FaqQuestionRequestDto;
import com.vecondev.buildoptima.manager.JwtTokenManager;
import com.vecondev.buildoptima.model.faq.FaqQuestion;
import com.vecondev.buildoptima.model.user.Role;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.endpoints.FaqQuestionEndpointUris;
import com.vecondev.buildoptima.parameters.faq.question.FaqQuestionControllerTestParameters;
import com.vecondev.buildoptima.parameters.result_actions.FaqQuestionResultActions;
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
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
@ActiveProfiles("test")
class FaqQuestionControllerTest extends FaqQuestionResultActions {

  @Autowired private MockMvc mvc;

  @Autowired private UserRepository userRepository;
  @Autowired private FaqCategoryRepository faqCategoryRepository;
  @Autowired private FaqQuestionRepository faqQuestionRepository;

  @Autowired private PasswordEncoder encoder;
  @Autowired private JwtTokenManager tokenManager;
  private FaqQuestionControllerTestParameters testParameters;

  @BeforeEach
  void setUp() {
    setResultActionsParameters(new FaqQuestionEndpointUris(), mvc, tokenManager);
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

    getAllResultActions(moderatorUser)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.*", hasSize(faqQuestionRepository.findAll().size())));
  }

  @Test
  void failedRetrievalOfAllCategoriesAsAccessDenied() throws Exception {
    User clientUser = getUserByRole(CLIENT);
    assumeFalse(clientUser == null);

    getAllResultActions(clientUser).andExpect(status().isForbidden());
  }

  @Test
  void successfulRetrievalOfQuestionById() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqQuestion faqQuestion = faqQuestionRepository.findAll().stream().findAny().orElse(null);
    assumeFalse(faqQuestion == null);

    getByIdResultActions(faqQuestion.getId(), moderatorUser)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.category.id").value(faqQuestion.getCategory().getId().toString()))
        .andExpect(jsonPath("$.question").value(faqQuestion.getQuestion()));
  }

  @Test
  void failedRetrievalOfQuestionByIdAsQuestionNotFound() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);

    getByIdResultActions(UUID.randomUUID(), moderatorUser).andExpect(status().isNotFound());
  }

  @Test
  void successfulCreationOfFaqQuestion() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqQuestionRequestDto requestDto = testParameters.getFaqQuestionToSave();

    creationResultActions(moderatorUser, requestDto)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNotEmpty());
  }

  @Test
  void failedCreationOfFaqQuestionAsQuestionAlreadyExists() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqQuestionRequestDto requestDto = testParameters.getFaqQuestionWithDuplicatedQuestion();

    creationResultActions(moderatorUser, requestDto).andExpect(status().isConflict());
  }

  @Test
  void failedCreationOfFaqQuestionAsQuestionIsInvalid() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqQuestionRequestDto requestDto = new FaqQuestionRequestDto();

    creationResultActions(moderatorUser, requestDto).andExpect(status().isBadRequest());
  }

  @Test
  void successfulUpdateOfFaqQuestion() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqQuestion faqQuestion = faqQuestionRepository.findAll().stream().findAny().orElse(null);
    assumeFalse(faqQuestion == null);
    FaqQuestionRequestDto requestDto = testParameters.getFaqQuestionToSave();

    updateResultActions(faqQuestion.getId(), moderatorUser, requestDto)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.question").value(requestDto.getQuestion()));
  }

  @Test
  void failedUpdateOfFaqQuestionAsQuestionNotFound() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqQuestionRequestDto requestDto = testParameters.getFaqQuestionToSave();

    updateResultActions(UUID.randomUUID(), moderatorUser, requestDto)
        .andExpect(status().isNotFound());
  }

  @Test
  void successfulDeletionOfFaqQuestion() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqQuestion faqQuestion = faqQuestionRepository.findAll().stream().findAny().orElse(null);
    assumeFalse(faqQuestion == null);

    deleteByIdResultActions(faqQuestion.getId(), moderatorUser).andExpect(status().isOk());
    assertFalse(faqQuestionRepository.existsById(faqQuestion.getId()));
  }

  @Test
  void failedDeletionOfFaqQuestionAsQuestionNotFound() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);

    deleteByIdResultActions(UUID.randomUUID(), moderatorUser).andExpect(status().isNotFound());
  }

  @Test
  void successfulFetchingOfQuestions() throws Exception {
    FetchRequestDto requestDto = testParameters.getFetchRequest();
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);

    fetchingResultActions(requestDto, moderatorUser).andExpect(status().isOk());
  }

  @Test
  void failedFetchingOfQuestionsAsPermissionDenied() throws Exception {
    FetchRequestDto requestDto = testParameters.getFetchRequest();
    User clientUser = getUserByRole(CLIENT);

    fetchingResultActions(requestDto, clientUser).andExpect(status().isForbidden());
  }

  @Test
  void failedFetchingOfQuestionsAsRequestDtoIsInvalid() throws Exception {
    FetchRequestDto requestDto = testParameters.getInvalidFetchRequest();
    User adminUser = getUserByRole(MODERATOR);

    fetchingResultActions(requestDto, adminUser).andExpect(status().isBadRequest());
  }

  private User getUserByRole(Role role) {
    return userRepository.findByRole(role).orElse(null);
  }
}
