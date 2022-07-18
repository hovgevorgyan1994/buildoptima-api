package com.vecondev.buildoptima.controller;

import com.vecondev.buildoptima.config.properties.JwtConfigProperties;
import com.vecondev.buildoptima.dto.request.news.NewsCreateRequestDto;
import com.vecondev.buildoptima.dto.request.news.NewsUpdateRequestDto;
import com.vecondev.buildoptima.manager.JwtTokenManager;
import com.vecondev.buildoptima.model.news.News;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.news.NewsControllerTestParameters;
import com.vecondev.buildoptima.repository.news.NewsRepository;
import com.vecondev.buildoptima.repository.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static com.vecondev.buildoptima.util.TestUtil.asJsonString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
@ActiveProfiles("test")
class NewsControllerTest {

  @Autowired
  private MockMvc mvc;
  @Autowired private UserRepository userRepository;
  @Autowired private NewsRepository newsRepository;
  @Autowired private JwtTokenManager tokenManager;
  @Autowired private JwtConfigProperties jwtConfigProperties;

  private final NewsControllerTestParameters newsControllerTestParameters =
      new NewsControllerTestParameters();

  private User admin;
  private User client;
  private News news;
  private NewsCreateRequestDto createNewsRequestDto;
  private NewsCreateRequestDto invalidCreateRequest;
  private NewsUpdateRequestDto updateNewsRequestDto;
  private NewsUpdateRequestDto invalidUpdateRequest;

  @BeforeEach
  void init() {
    admin = newsControllerTestParameters.getRightUser();
    client = newsControllerTestParameters.getWrongUser();
    userRepository.saveAll(Set.of(admin, client));
    createNewsRequestDto = newsControllerTestParameters.getRequestToSave();
    invalidCreateRequest = newsControllerTestParameters.getRequestToSaveWithInvalidFields();
    updateNewsRequestDto = newsControllerTestParameters.getRequestToUpdate();
    invalidUpdateRequest = newsControllerTestParameters.getRequestToUpdateWithInvalidFields();
    news =
        newsRepository.saveAndFlush(
            newsControllerTestParameters.getSavedNewsItem(createNewsRequestDto));
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
    newsRepository.deleteAll();
  }

  @Test
  void createNewsSuccess() throws Exception {
    mvc.perform(
            post("/news")
                .header(
                    jwtConfigProperties.getAuthorizationHeader(),
                    jwtConfigProperties.getAuthorizationHeaderPrefix()
                        + tokenManager.generateAccessToken(admin))
                .content(asJsonString(createNewsRequestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isCreated());
  }

  @Test
  void failedCreateNewsAsContainsInvalidFields() throws Exception {
    mvc.perform(
            post("/news")
                .header(
                    jwtConfigProperties.getAuthorizationHeader(),
                    jwtConfigProperties.getAuthorizationHeaderPrefix()
                        + tokenManager.generateAccessToken(client))
                .content(asJsonString(invalidCreateRequest))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }

  @Test
  void failedCreateNewsAsPermissionDenied() throws Exception {
    mvc.perform(
            post("/news")
                .header(
                    jwtConfigProperties.getAuthorizationHeader(),
                    jwtConfigProperties.getAuthorizationHeaderPrefix()
                        + tokenManager.generateAccessToken(client))
                .content(asJsonString(createNewsRequestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isForbidden());
  }

  @Test
  void failedCreateNewsAsAccessTokenIsMissing() throws Exception {
    mvc.perform(
            post("/news")
                .content(asJsonString(createNewsRequestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void updateNewsSuccess() throws Exception {
    mvc.perform(
            put("/news/{id}/update", news.getId())
                .header(
                    jwtConfigProperties.getAuthorizationHeader(),
                    jwtConfigProperties.getAuthorizationHeaderPrefix()
                        + tokenManager.generateAccessToken(admin))
                .content(asJsonString(updateNewsRequestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  void failedUpdateAsInvalidFields() throws Exception {
    mvc.perform(
            put("/news/{id}/update", news.getId())
                .header(
                    jwtConfigProperties.getAuthorizationHeader(),
                    jwtConfigProperties.getAuthorizationHeaderPrefix()
                        + tokenManager.generateAccessToken(admin))
                .content(asJsonString(invalidUpdateRequest))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());
  }

  @Test
  void failedUpdateAsNewsItemNotFound() throws Exception {
    mvc.perform(
            put("/news/{id}/update", UUID.randomUUID())
                .header(
                    jwtConfigProperties.getAuthorizationHeader(),
                    jwtConfigProperties.getAuthorizationHeaderPrefix()
                        + tokenManager.generateAccessToken(admin))
                .content(asJsonString(updateNewsRequestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE))
        .andExpect(status().isNotFound());
  }

  @Test
  void failedDeleteAsNewsItemNotFound() throws Exception {
    mvc.perform(
            delete("/news/{id}/delete", UUID.randomUUID())
                .header(
                    jwtConfigProperties.getAuthorizationHeader(),
                    jwtConfigProperties.getAuthorizationHeaderPrefix()
                        + tokenManager.generateAccessToken(admin)))
        .andExpect(status().isNotFound());
  }

  @Test
  void failedDeleteAsAccessDenied() throws Exception {
    mvc.perform(
            delete("/news/{id}/delete", UUID.randomUUID())
                .header(
                    jwtConfigProperties.getAuthorizationHeader(),
                    jwtConfigProperties.getAuthorizationHeaderPrefix()
                        + tokenManager.generateAccessToken(client)))
        .andExpect(status().isForbidden());
  }

  @Test
  void failedDeleteAsAccessTokenMissing() throws Exception {
    mvc.perform(
            delete("/news/{id}/delete", UUID.randomUUID()))
        .andExpect(status().isUnauthorized());
  }
}