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

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
@ActiveProfiles("test")
class NewsControllerTest {

  @Autowired private MockMvc mvc;
  @Autowired private UserRepository userRepository;
  @Autowired private NewsRepository newsRepository;
  @Autowired private JwtTokenManager tokenManager;
  @Autowired private JwtConfigProperties jwtConfigProperties;

  private final NewsControllerTestParameters newsControllerTestParameters =
      new NewsControllerTestParameters();

  private User admin;
  private User client;
  private News news;

  @BeforeEach
  void init() {
    admin = newsControllerTestParameters.getRightUser();
    client = newsControllerTestParameters.getWrongUser();
    userRepository.saveAllAndFlush(Set.of(admin, client));
    NewsCreateRequestDto createNewsRequestDto = newsControllerTestParameters.getRequestToSave();
    News newsToSave = newsControllerTestParameters.getSavedNewsItem(createNewsRequestDto);
    newsToSave.setCreatedBy(admin.getId());
    news = newsRepository.saveAndFlush(newsToSave);
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
    newsRepository.deleteAll();
  }

  @Test
  void createNewsSuccess() throws Exception {
    NewsCreateRequestDto createRequestDto = newsControllerTestParameters.createRequestDto();
    String header = jwtConfigProperties.getAuthorizationHeader();
    String token =
        jwtConfigProperties.getAuthorizationHeaderPrefix()
            + tokenManager.generateAccessToken(admin);
    mvc.perform(
            post("/news")
                .header(header, token)
                .content(createRequestDto.getImage().getBytes())
                .contentType(MULTIPART_FORM_DATA_VALUE)
                .param("title", createRequestDto.getTitle())
                .param("summary", createRequestDto.getSummary())
                .param("description", createRequestDto.getDescription())
                .param("category", createRequestDto.getCategory())
                .param("keywords", " "))
        .andExpect(status().isCreated());
  }

  @Test
  void failedCreateNewsAsContainsInvalidFields() throws Exception {
    NewsCreateRequestDto createRequestDto =
        newsControllerTestParameters.createRequestDtoWithInvalidFields();
    String header = jwtConfigProperties.getAuthorizationHeader();
    String token =
        jwtConfigProperties.getAuthorizationHeaderPrefix()
            + tokenManager.generateAccessToken(admin);
    mvc.perform(
            post("/news")
                .header(header, token)
                .content(createRequestDto.getImage().getBytes())
                .contentType(MULTIPART_FORM_DATA_VALUE)
                .param("title", createRequestDto.getTitle())
                .param("summary", createRequestDto.getSummary())
                .param("description", createRequestDto.getDescription())
                .param("category", createRequestDto.getCategory())
                .param("keywords", " "))
        .andExpect(status().isBadRequest());
  }

  @Test
  void failedCreateNewsAsPermissionDenied() throws Exception {
    NewsCreateRequestDto createRequestDto = newsControllerTestParameters.createRequestDto();
    String header = jwtConfigProperties.getAuthorizationHeader();
    String token =
        jwtConfigProperties.getAuthorizationHeaderPrefix()
            + tokenManager.generateAccessToken(client);
    mvc.perform(
            post("/news")
                .header(header, token)
                .content(createRequestDto.getImage().getBytes())
                .contentType(MULTIPART_FORM_DATA_VALUE)
                .param("title", createRequestDto.getTitle())
                .param("summary", createRequestDto.getSummary())
                .param("description", createRequestDto.getDescription())
                .param("category", createRequestDto.getCategory())
                .param("keywords", " "))
        .andExpect(status().isForbidden());
  }


  @Test
  void failedCreateNewsAsAccessTokenIsMissing() throws Exception {
    NewsCreateRequestDto createRequestDto = newsControllerTestParameters.createRequestDto();
    mvc.perform(
            post("/news")
                .content(createRequestDto.getImage().getBytes())
                .contentType(MULTIPART_FORM_DATA_VALUE)
                .param("title", createRequestDto.getTitle())
                .param("summary", createRequestDto.getSummary())
                .param("description", createRequestDto.getDescription())
                .param("category", createRequestDto.getCategory())
                .param("keywords", " "))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void updateNewsSuccess() throws Exception {
    NewsUpdateRequestDto updateRequestDto = newsControllerTestParameters.updateRequestDto();
    String header = jwtConfigProperties.getAuthorizationHeader();
    String token =
        jwtConfigProperties.getAuthorizationHeaderPrefix()
            + tokenManager.generateAccessToken(admin);
    mvc.perform(
            patch("/news/{id}", news.getId())
                .header(header, token)
                .content(updateRequestDto.getImage().getBytes())
                .contentType(MULTIPART_FORM_DATA_VALUE)
                .param("title", updateRequestDto.getTitle())
                .param("summary", updateRequestDto.getSummary())
                .param("description", updateRequestDto.getDescription())
                .param("category", updateRequestDto.getCategory())
                .param("keywords", " "))
        .andExpect(status().isOk());
  }

  @Test
  void failedUpdateAsInvalidFields() throws Exception {
    NewsUpdateRequestDto updateRequestDto =
        newsControllerTestParameters.updateRequestDtoWithInvalidFields();
    String header = jwtConfigProperties.getAuthorizationHeader();
    String token =
        jwtConfigProperties.getAuthorizationHeaderPrefix()
            + tokenManager.generateAccessToken(admin);
    mvc.perform(
            patch("/news/{id}", news.getId())
                .header(header, token)
                .content(updateRequestDto.getImage().getBytes())
                .contentType(MULTIPART_FORM_DATA_VALUE)
                .param("title", updateRequestDto.getTitle())
                .param("summary", updateRequestDto.getSummary())
                .param("description", updateRequestDto.getDescription())
                .param("category", updateRequestDto.getCategory())
                .param("keywords", " "))
        .andExpect(status().isBadRequest());
  }

  @Test
  void failedUpdateAsNewsItemNotFound() throws Exception {
    NewsUpdateRequestDto updateRequestDto = newsControllerTestParameters.updateRequestDto();
    String header = jwtConfigProperties.getAuthorizationHeader();
    String token =
        jwtConfigProperties.getAuthorizationHeaderPrefix()
            + tokenManager.generateAccessToken(admin);
    mvc.perform(
            patch("/news/{id}", UUID.randomUUID())
                .header(header, token)
                .content(updateRequestDto.getImage().getBytes())
                .contentType(MULTIPART_FORM_DATA_VALUE)
                .param("title", updateRequestDto.getTitle())
                .param("summary", updateRequestDto.getSummary())
                .param("description", updateRequestDto.getDescription())
                .param("category", updateRequestDto.getCategory())
                .param("keywords", " "))
        .andExpect(status().isNotFound());
  }

  @Test
  void deleteAsNewsItemSuccess() throws Exception {
    mvc.perform(
            delete("/news/{id}", news.getId())
                .header(
                    jwtConfigProperties.getAuthorizationHeader(),
                    jwtConfigProperties.getAuthorizationHeaderPrefix()
                        + tokenManager.generateAccessToken(admin)))
        .andExpect(status().isOk());
  }

  @Test
  void failedDeleteAsNewsItemNotFound() throws Exception {
    mvc.perform(
            delete("/news/{id}", UUID.randomUUID())
                .header(
                    jwtConfigProperties.getAuthorizationHeader(),
                    jwtConfigProperties.getAuthorizationHeaderPrefix()
                        + tokenManager.generateAccessToken(admin)))
        .andExpect(status().isNotFound());
  }

  @Test
  void failedDeleteAsAccessDenied() throws Exception {
    mvc.perform(
            delete("/news/{id}", UUID.randomUUID())
                .header(
                    jwtConfigProperties.getAuthorizationHeader(),
                    jwtConfigProperties.getAuthorizationHeaderPrefix()
                        + tokenManager.generateAccessToken(client)))
        .andExpect(status().isForbidden());
  }

  @Test
  void failedDeleteAsAccessTokenMissing() throws Exception {
    mvc.perform(delete("/news/{id}", UUID.randomUUID())).andExpect(status().isUnauthorized());
  }

  @Test
  void getByIdSuccess() throws Exception {
    mvc.perform(
            get("/news/{id}", news.getId())
                .header(
                    jwtConfigProperties.getAuthorizationHeader(),
                    jwtConfigProperties.getAuthorizationHeaderPrefix()
                        + tokenManager.generateAccessToken(admin)))
        .andExpect(status().isOk());
  }

  @Test
  void failedGetByIdAsNotFound() throws Exception {
    mvc.perform(
            get("/news/{id}", UUID.randomUUID())
                .header(
                    jwtConfigProperties.getAuthorizationHeader(),
                    jwtConfigProperties.getAuthorizationHeaderPrefix()
                        + tokenManager.generateAccessToken(admin)))
        .andExpect(status().isNotFound());
  }

  @Test
  void getMetadataSuccess() throws Exception {
    mvc.perform(
            get("/news/metadata")
                .header(
                    jwtConfigProperties.getAuthorizationHeader(),
                    jwtConfigProperties.getAuthorizationHeaderPrefix()
                        + tokenManager.generateAccessToken(admin)))
        .andExpect(status().isOk());
  }
}
