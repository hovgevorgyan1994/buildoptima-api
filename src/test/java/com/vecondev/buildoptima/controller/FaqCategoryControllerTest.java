package com.vecondev.buildoptima.controller;

import com.vecondev.buildoptima.config.AmazonS3Config;
import com.vecondev.buildoptima.dto.request.faq.FaqCategoryRequestDto;
import com.vecondev.buildoptima.dto.request.filter.FetchRequestDto;
import com.vecondev.buildoptima.manager.JwtTokenManager;
import com.vecondev.buildoptima.model.faq.FaqCategory;
import com.vecondev.buildoptima.model.user.Role;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.endpoints.FaqCategoryEndpointUris;
import com.vecondev.buildoptima.parameters.faq.category.FaqCategoryControllerTestParameters;
import com.vecondev.buildoptima.parameters.result_actions.FaqCategoryResultActions;
import com.vecondev.buildoptima.repository.faq.FaqCategoryRepository;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

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
@Import({FaqCategoryEndpointUris.class, FaqCategoryResultActions.class, AmazonS3Config.class})
class FaqCategoryControllerTest  {

  @Autowired private MockMvc mvc;

  @Autowired private UserRepository userRepository;
  @Autowired private FaqCategoryRepository faqCategoryRepository;
  @Autowired private PasswordEncoder encoder;
  @Autowired private FaqCategoryResultActions resultActions;

  private FaqCategoryControllerTestParameters testParameters;

  @BeforeEach
  void setUp() {
    testParameters = new FaqCategoryControllerTestParameters(userRepository);
    List<User> users = testParameters.users();
    users.forEach(user -> user.setPassword(encoder.encode(user.getPassword())));
    userRepository.saveAll(users);

    faqCategoryRepository.saveAll(testParameters.faqCategories());
  }

  @AfterEach
  void tearDown() {
    faqCategoryRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void successfulRetrievalOfAllCategories() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);

    resultActions.getAllResultActions(moderatorUser)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.*", hasSize(faqCategoryRepository.findAll().size())));
  }

  @Test
  void failedRetrievalOfAllCategoriesAsAccessDenied() throws Exception {
    User clientUser = getUserByRole(CLIENT);
    assumeFalse(clientUser == null);

    resultActions.getAllResultActions(clientUser).andExpect(status().isForbidden());
  }

  @Test
  void successfulRetrievalOfCategoryById() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqCategory faqCategory = faqCategoryRepository.findAll().stream().findAny().orElse(null);
    assumeFalse(faqCategory == null);

    resultActions.getByIdResultActions(faqCategory.getId(), moderatorUser)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(faqCategory.getId().toString()))
        .andExpect(jsonPath("$.name").value(faqCategory.getName()));
  }

  @Test
  void failedRetrievalOfCategoryByIdAsCategoryNotFound() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);

    resultActions.getByIdResultActions(UUID.randomUUID(), moderatorUser).andExpect(status().isNotFound());
  }

  @Test
  void successfulCreationOfFaqCategory() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqCategoryRequestDto requestDto = testParameters.getFaqCategoryToSave();

    resultActions.creationResultActions(moderatorUser, requestDto)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNotEmpty());
  }

  @Test
  void failedCreationOfFaqCategoryAsCategoryNameAlreadyExists() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqCategoryRequestDto requestDto = testParameters.getFaqCategoryWithDuplicatedNameToSave();

    resultActions.creationResultActions(moderatorUser, requestDto).andExpect(status().isConflict());
  }

  @Test
  void failedCreationOfFaqCategoryAsCategoryIsInvalid() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqCategoryRequestDto requestDto = new FaqCategoryRequestDto();

    resultActions.creationResultActions(moderatorUser, requestDto).andExpect(status().isBadRequest());
  }

  @Test
  void failedCreationOfFaqCategoryAsAccessDenied() throws Exception {
    User clientUser = getUserByRole(CLIENT);
    assumeFalse(clientUser == null);
    FaqCategoryRequestDto requestDto = testParameters.getFaqCategoryToSave();

    resultActions.creationResultActions(clientUser, requestDto).andExpect(status().isForbidden());
  }

  @Test
  void successfulUpdateOfFaqCategory() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqCategory faqCategory = faqCategoryRepository.findAll().stream().findAny().orElse(null);
    assumeFalse(faqCategory == null);
    FaqCategoryRequestDto requestDto =
        testParameters.getFaqCategoryToSave().toBuilder().name("Payment").build();

    resultActions.updateResultActions(faqCategory.getId(), moderatorUser, requestDto)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value(requestDto.getName()));
  }

  @Test
  void failedUpdateOfFaqCategoryAsCategoryNotFound() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqCategoryRequestDto requestDto =
        testParameters.getFaqCategoryToSave().toBuilder().name("Payment").build();


    resultActions.updateResultActions(UUID.randomUUID(), moderatorUser, requestDto)
        .andExpect(status().isNotFound());
  }

  @Test
  void successfulDeletionOfFaqCategory() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqCategory faqCategory = faqCategoryRepository.findAll().stream().findAny().orElse(null);
    assumeFalse(faqCategory == null);

    resultActions.deleteByIdResultActions(faqCategory.getId(), moderatorUser).andExpect(status().isOk());
    assertFalse(faqCategoryRepository.existsById(faqCategory.getId()));
  }

  @Test
  void failedDeletionOfFaqCategory() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);

    resultActions.deleteByIdResultActions(UUID.randomUUID(), moderatorUser).andExpect(status().isNotFound());
  }

  @Test
  void successfulFetchingOfCategories() throws Exception {
    FetchRequestDto requestDto = testParameters.getFetchRequest();
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);

    resultActions.fetchingResultActions(requestDto, moderatorUser).andExpect(status().isOk());
  }

  @Test
  void failedFetchingOfCategoriesAsPermissionDenied() throws Exception {
    FetchRequestDto requestDto = testParameters.getFetchRequest();
    User clientUser = getUserByRole(CLIENT);

    resultActions.fetchingResultActions(requestDto, clientUser).andExpect(status().isForbidden());
  }

  @Test
  void failedFetchingOfCategoriesAsRequestDtoIsInvalid() throws Exception {
    FetchRequestDto requestDto = testParameters.getInvalidFetchRequest();
    User moderatorUser = getUserByRole(MODERATOR);

    resultActions.fetchingResultActions(requestDto, moderatorUser).andExpect(status().isBadRequest());
  }

  @Test
  void successfulExportingOfCategoriesInCsv() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);

    resultActions.getAllInCsvResultActions(moderatorUser)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", notNullValue()));
  }

  private User getUserByRole(Role role) {
    return userRepository.findByRole(role).orElse(null);
  }
}
