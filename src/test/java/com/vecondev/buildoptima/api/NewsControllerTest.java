package com.vecondev.buildoptima.api;

import com.vecondev.buildoptima.config.AmazonS3Config;
import com.vecondev.buildoptima.dto.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.news.request.NewsCreateRequestDto;
import com.vecondev.buildoptima.dto.news.request.NewsUpdateRequestDto;
import com.vecondev.buildoptima.model.news.News;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.endpoints.NewsEndpointUris;
import com.vecondev.buildoptima.parameters.news.NewsControllerTestParameters;
import com.vecondev.buildoptima.parameters.news.NewsServiceTestParameters;
import com.vecondev.buildoptima.parameters.result_actions.NewsResultActions;
import com.vecondev.buildoptima.repository.news.NewsRepository;
import com.vecondev.buildoptima.repository.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
@ActiveProfiles("test")
@Import({AmazonS3Config.class, NewsEndpointUris.class, NewsResultActions.class})
class NewsControllerTest {
  @Autowired private UserRepository userRepository;
  @Autowired private NewsRepository newsRepository;
  @Autowired private NewsResultActions newsResultActions;
  private NewsControllerTestParameters newsControllerTestParameters;

  private NewsServiceTestParameters newsServiceTestParameters;

  private User admin;
  private User client;
  private News news;

  @BeforeEach
  void init() {
    newsControllerTestParameters = new NewsControllerTestParameters();
    newsServiceTestParameters = new NewsServiceTestParameters();
    admin = newsControllerTestParameters.getRightUser();
    client = newsControllerTestParameters.getWrongUser();
    userRepository.saveAllAndFlush(Set.of(admin, client));
    NewsCreateRequestDto createNewsRequestDto = newsControllerTestParameters.getRequestToSave();
    News newsToSave = newsControllerTestParameters.getSavedNewsItem(createNewsRequestDto);
    newsToSave.setCreatedBy(admin.getId());
    newsToSave.setUpdatedBy(admin.getId());
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
    newsResultActions
        .creationResultActions(admin, createRequestDto)
        .andExpect(status().isCreated());
  }

  @Test
  void failedCreateNewsAsContainsInvalidFields() throws Exception {
    NewsCreateRequestDto createRequestDto =
        newsControllerTestParameters.createRequestDtoWithInvalidFields();
    newsResultActions
        .creationResultActions(admin, createRequestDto)
        .andExpect(status().isBadRequest());
  }

  @Test
  void failedCreateNewsAsPermissionDenied() throws Exception {
    NewsCreateRequestDto createRequestDto = newsControllerTestParameters.createRequestDto();
    newsResultActions
        .creationResultActions(client, createRequestDto)
        .andExpect(status().isForbidden());
  }

  @Test
  void updateNewsSuccess() throws Exception {
    NewsUpdateRequestDto updateRequestDto = newsControllerTestParameters.updateRequestDto();
    newsResultActions
        .updateResultActions(news.getId(), admin, updateRequestDto)
        .andExpect(status().isOk());
  }

  @Test
  void failedUpdateAsInvalidFields() throws Exception {
    NewsUpdateRequestDto updateRequestDto =
        newsControllerTestParameters.updateRequestDtoWithInvalidFields();
    newsResultActions
        .updateResultActions(news.getId(), admin, updateRequestDto)
        .andExpect(status().isBadRequest());
  }

  @Test
  void failedUpdateAsNewsItemNotFound() throws Exception {
    NewsUpdateRequestDto updateRequestDto = newsControllerTestParameters.updateRequestDto();
    newsResultActions
        .updateResultActions(UUID.randomUUID(), admin, updateRequestDto)
        .andExpect(status().isNotFound());
  }

  @Test
  void deleteAsNewsItemSuccess() throws Exception {
    newsResultActions.deleteByIdResultActions(news.getId(), admin).andExpect(status().isOk());
  }

  @Test
  void failedDeleteAsNewsItemNotFound() throws Exception {
    newsResultActions
        .deleteByIdResultActions(UUID.randomUUID(), admin)
        .andExpect(status().isNotFound());
  }

  @Test
  void failedDeleteAsAccessDenied() throws Exception {
    newsResultActions
        .deleteByIdResultActions(news.getId(), client)
        .andExpect(status().isForbidden());
  }

  @Test
  void getByIdSuccess() throws Exception {
    newsResultActions.getByIdResultActions(news.getId(), admin).andExpect(status().isOk());
  }

  @Test
  void failedGetByIdAsNotFound() throws Exception {
    newsResultActions
        .getByIdResultActions(UUID.randomUUID(), admin)
        .andExpect(status().isNotFound());
  }

  @Test
  void getMetadataSuccess() throws Exception {
    newsResultActions.getMetadataResultActions(admin).andExpect(status().isOk());
  }

  @Test
  void exportCsvSuccess() throws Exception {
    FetchRequestDto fetchRequest = newsServiceTestParameters.getFetchRequest();
    newsResultActions.getExportCsvResultActions(fetchRequest, admin).andExpect(status().isOk());
  }

  @Test
  void fetchNewsSuccess() throws Exception {
    FetchRequestDto fetchRequest = newsServiceTestParameters.getFetchRequest();
    newsResultActions.fetchingResultActions(fetchRequest, admin).andExpect(status().isOk());
  }

  @Test
  void archiveNewsSuccess() throws Exception {
    newsResultActions.getArchiveResultActions(news.getId(), admin).andExpect(status().isOk());
  }

  @Test
  void failedArchiveNewsAsNotFound() throws Exception {
    newsResultActions
        .getArchiveResultActions(UUID.randomUUID(), admin)
        .andExpect(status().isNotFound());
  }

  @Test
  void failedArchiveNewsAsAccessDenied() throws Exception {
    newsResultActions
        .getArchiveResultActions(news.getId(), client)
        .andExpect(status().isForbidden());
  }
}
