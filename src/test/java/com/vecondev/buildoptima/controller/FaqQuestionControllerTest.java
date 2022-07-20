package com.vecondev.buildoptima.controller;

import com.vecondev.buildoptima.config.AmazonS3Config;
import com.vecondev.buildoptima.dto.request.faq.FaqQuestionRequestDto;
import com.vecondev.buildoptima.dto.request.filter.FetchRequestDto;
import com.vecondev.buildoptima.model.Status;
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
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static com.vecondev.buildoptima.filter.model.DictionaryField.CATEGORY;
import static com.vecondev.buildoptima.filter.model.DictionaryField.UPDATED_BY;
import static com.vecondev.buildoptima.model.Status.ACTIVE;
import static com.vecondev.buildoptima.model.Status.ARCHIVED;
import static com.vecondev.buildoptima.model.user.Role.CLIENT;
import static com.vecondev.buildoptima.model.user.Role.MODERATOR;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
@ActiveProfiles("test")
@Import({FaqQuestionEndpointUris.class, FaqQuestionResultActions.class, AmazonS3Config.class})
class FaqQuestionControllerTest {

  @Autowired private UserRepository userRepository;
  @Autowired private FaqCategoryRepository faqCategoryRepository;
  @Autowired private FaqQuestionRepository faqQuestionRepository;
  @Autowired private PasswordEncoder encoder;
  @Autowired private FaqQuestionResultActions resultActions;

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

    resultActions
        .getAllResultActions(moderatorUser)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.*", hasSize(faqQuestionRepository.findAll().size())));
  }

  @Test
  void failedRetrievalOfAllCategoriesAsAccessDenied() throws Exception {
    User clientUser = getUserByRole(CLIENT);
    assumeFalse(clientUser == null);

    resultActions.getAllResultActions(clientUser).andExpect(status().isForbidden());
  }

  @Test
  void successfulRetrievalOfQuestionById() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqQuestion faqQuestion = faqQuestionRepository.findAll().stream().findAny().orElse(null);
    assumeFalse(faqQuestion == null);

    resultActions
        .getByIdResultActions(faqQuestion.getId(), moderatorUser)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.category.id").value(faqQuestion.getCategory().getId().toString()))
        .andExpect(jsonPath("$.question").value(faqQuestion.getQuestion()));
  }

  @Test
  void failedRetrievalOfQuestionByIdAsQuestionNotFound() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);

    resultActions
        .getByIdResultActions(UUID.randomUUID(), moderatorUser)
        .andExpect(status().isNotFound());
  }

  @Test
  void successfulCreationOfFaqQuestion() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqQuestionRequestDto requestDto = testParameters.getFaqQuestionToSave();

    resultActions
        .creationResultActions(moderatorUser, requestDto)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNotEmpty());
  }

  @Test
  void failedCreationOfFaqQuestionAsQuestionAlreadyExists() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqQuestionRequestDto requestDto = testParameters.getFaqQuestionWithDuplicatedQuestion();

    resultActions.creationResultActions(moderatorUser, requestDto).andExpect(status().isConflict());
  }

  @Test
  void failedCreationOfFaqQuestionAsQuestionIsInvalid() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqQuestionRequestDto requestDto = new FaqQuestionRequestDto();

    resultActions
        .creationResultActions(moderatorUser, requestDto)
        .andExpect(status().isBadRequest());
  }

  @Test
  void successfulUpdateOfFaqQuestion() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqQuestion faqQuestion = faqQuestionRepository.findAll().stream().findAny().orElse(null);
    assumeFalse(faqQuestion == null);
    FaqQuestionRequestDto requestDto = testParameters.getFaqQuestionToSave();

    resultActions
        .updateResultActions(faqQuestion.getId(), moderatorUser, requestDto)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.question").value(requestDto.getQuestion()));
  }

  @Test
  void failedUpdateOfFaqQuestionAsQuestionNotFound() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqQuestionRequestDto requestDto = testParameters.getFaqQuestionToSave();

    resultActions
        .updateResultActions(UUID.randomUUID(), moderatorUser, requestDto)
        .andExpect(status().isNotFound());
  }

  @Test
  void successfulDeletionOfFaqQuestion() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqQuestion faqQuestion = faqQuestionRepository.findAll().stream().findAny().orElse(null);
    assumeFalse(faqQuestion == null);

    resultActions
        .deleteByIdResultActions(faqQuestion.getId(), moderatorUser)
        .andExpect(status().isOk());
    assertFalse(faqQuestionRepository.existsById(faqQuestion.getId()));
  }

  @Test
  void failedDeletionOfFaqQuestionAsQuestionNotFound() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);

    resultActions
        .deleteByIdResultActions(UUID.randomUUID(), moderatorUser)
        .andExpect(status().isNotFound());
  }

  @Test
  void successfulFetchingOfQuestions() throws Exception {
    FetchRequestDto requestDto = testParameters.getFetchRequest();
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);

    resultActions.fetchingResultActions(requestDto, moderatorUser).andExpect(status().isOk());
  }

  @Test
  void failedFetchingOfQuestionsAsPermissionDenied() throws Exception {
    FetchRequestDto requestDto = testParameters.getFetchRequest();
    User clientUser = getUserByRole(CLIENT);

    resultActions.fetchingResultActions(requestDto, clientUser).andExpect(status().isForbidden());
  }

  @Test
  void failedFetchingOfQuestionsAsRequestDtoIsInvalid() throws Exception {
    FetchRequestDto requestDto = testParameters.getInvalidFetchRequest();
    User adminUser = getUserByRole(MODERATOR);

    resultActions.fetchingResultActions(requestDto, adminUser).andExpect(status().isBadRequest());
  }

  @Test
  void successfulExportingOfQuestionsInCsv() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);

    resultActions
        .getAllInCsvResultActions(moderatorUser)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", notNullValue()));
  }

  @Test
  void successfulGettingMetadata() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    FaqQuestion lastUpdatedQuestion = faqQuestionRepository.findTopByOrderByUpdatedAtDesc().orElse(null);
    assumeFalse(lastUpdatedQuestion == null);

    resultActions.getMetadataResultActions(moderatorUser)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lastUpdatedAt").value(lastUpdatedQuestion.getUpdatedAt().toString()))
            .andExpect(jsonPath("$.allActiveCount").value(faqQuestionRepository.countByStatus(ACTIVE)))
            .andExpect(jsonPath("$.allArchivedCount").value(faqQuestionRepository.countByStatus(ARCHIVED)));
  }

  @Test
  void successfulLookupByModifiers() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    Status status = ACTIVE;

    resultActions
        .lookupResultActions(status, UPDATED_BY, moderatorUser)
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$", hasSize(faqQuestionRepository.findDistinctModifiers(status).size())));
  }

  @Test
  void successfulLookupByCategories() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    Status status = ARCHIVED;

    resultActions
        .lookupResultActions(status, CATEGORY, moderatorUser)
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$", hasSize(faqQuestionRepository.findDistinctCategories(status).size())));
  }

  private User getUserByRole(Role role) {
    return userRepository.findByRole(role).orElse(null);
  }
}
