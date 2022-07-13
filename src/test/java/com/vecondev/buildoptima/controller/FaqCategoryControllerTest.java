package com.vecondev.buildoptima.controller;

import com.vecondev.buildoptima.config.properties.JwtConfigProperties;
import com.vecondev.buildoptima.dto.request.faq.FaqCategoryRequestDto;
import com.vecondev.buildoptima.manager.JwtTokenManager;
import com.vecondev.buildoptima.model.faq.FaqCategory;
import com.vecondev.buildoptima.model.user.Role;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.faq.category.FaqCategoryControllerTestParameters;
import com.vecondev.buildoptima.repository.faq.FaqCategoryRepository;
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
class FaqCategoryControllerTest {

  @Autowired private MockMvc mvc;

  @Autowired private UserRepository userRepository;
  @Autowired private FaqCategoryRepository faqCategoryRepository;

  @Autowired private PasswordEncoder encoder;
  @Autowired private JwtConfigProperties jwtConfigProperties;
  @Autowired private JwtTokenManager tokenManager;

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

    mvc.perform(
            get("/faq/category")
                .header(jwtConfigProperties.getAuthorizationHeader(), getAccessToken(moderatorUser))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.*", hasSize(faqCategoryRepository.findAll().size())));
  }

  @Test
  void failedRetrievalOfAllCategoriesAsAccessDenied() throws Exception {
    User clientUser = getUserByRole(CLIENT);
    assumeFalse(clientUser == null);

    mvc.perform(
            get("/faq/category")
                .header(jwtConfigProperties.getAuthorizationHeader(), getAccessToken(clientUser))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isForbidden());
  }

  @Test
  void successfulRetrievalOfCategoryById() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqCategory faqCategory = faqCategoryRepository.findAll().stream().findAny().orElse(null);
    assumeFalse(faqCategory == null);

    mvc.perform(
            get("/faq/category/{id}", faqCategory.getId())
                .header(jwtConfigProperties.getAuthorizationHeader(), getAccessToken(moderatorUser))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(faqCategory.getId().toString()))
        .andExpect(jsonPath("$.name").value(faqCategory.getName()));
  }

  @Test
  void failedRetrievalOfCategoryByIdAsCategoryNotFound() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);

    mvc.perform(
            get("/faq/category/{id}", UUID.randomUUID())
                .header(
                    jwtConfigProperties.getAuthorizationHeader(),
                    jwtConfigProperties.getAuthorizationHeaderPrefix()
                        + tokenManager.generateAccessToken(moderatorUser))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isNotFound());
  }

  @Test
  void successfulCreationOfFaqCategory() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);

    FaqCategoryRequestDto requestDto = testParameters.getFaqCategoryToSave();

    mvc.perform(
            post("/faq/category")
                .header(jwtConfigProperties.getAuthorizationHeader(), getAccessToken(moderatorUser))
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNotEmpty());
  }

  @Test
  void failedCreationOfFaqCategoryAsCategoryNameAlreadyExists() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);

    FaqCategoryRequestDto requestDto = testParameters.getFaqCategoryWithDuplicatedNameToSave();

    mvc.perform(
                    post("/faq/category")
                            .header(jwtConfigProperties.getAuthorizationHeader(), getAccessToken(moderatorUser))
                            .content(asJsonString(requestDto))
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON_VALUE))
            .andExpect(status().isConflict());
  }

  @Test
  void failedCreationOfFaqCategoryAsCategoryIsInvalid() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);

    FaqCategoryRequestDto requestDto = new FaqCategoryRequestDto();

    mvc.perform(
            post("/faq/category")
                .header(jwtConfigProperties.getAuthorizationHeader(), getAccessToken(moderatorUser))
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }

  @Test
  void failedCreationOfFaqCategoryAsAccessDenied() throws Exception {
    User clientUser = getUserByRole(CLIENT);
    assumeFalse(clientUser == null);

    FaqCategoryRequestDto requestDto = testParameters.getFaqCategoryToSave();

    mvc.perform(
            post("/faq/category")
                .header(jwtConfigProperties.getAuthorizationHeader(), getAccessToken(clientUser))
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isForbidden());
  }

  @Test
  void successfulUpdateOfFaqCategory() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqCategory faqCategory = faqCategoryRepository.findAll().stream().findAny().orElse(null);
    assumeFalse(faqCategory == null);
    FaqCategoryRequestDto requestDto =
        testParameters.getFaqCategoryToSave().toBuilder().name("Payment").build();

    mvc.perform(
            put("/faq/category/{id}", faqCategory.getId().toString())
                .header(jwtConfigProperties.getAuthorizationHeader(), getAccessToken(moderatorUser))
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value(requestDto.getName()));
  }

  @Test
  void failedUpdateOfFaqCategoryAsCategoryNotFound() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqCategoryRequestDto requestDto =
        testParameters.getFaqCategoryToSave().toBuilder().name("Payment").build();

    mvc.perform(
            put("/faq/category/{id}", UUID.randomUUID().toString())
                .header(jwtConfigProperties.getAuthorizationHeader(), getAccessToken(moderatorUser))
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isNotFound());
  }

  @Test
  void successfulDeletionOfFaqCategory() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);
    FaqCategory faqCategory = faqCategoryRepository.findAll().stream().findAny().orElse(null);
    assumeFalse(faqCategory == null);

    mvc.perform(
            delete("/faq/category/{id}", faqCategory.getId().toString())
                .header(jwtConfigProperties.getAuthorizationHeader(), getAccessToken(moderatorUser))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());

    assertFalse(faqCategoryRepository.existsById(faqCategory.getId()));
  }

  @Test
  void failedDeletionOfFaqCategory() throws Exception {
    User moderatorUser = getUserByRole(MODERATOR);
    assumeFalse(moderatorUser == null);

    mvc.perform(
            delete("/faq/category/{id}", UUID.randomUUID())
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
