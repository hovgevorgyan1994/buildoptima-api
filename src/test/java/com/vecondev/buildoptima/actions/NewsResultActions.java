package com.vecondev.buildoptima.actions;

import static com.vecondev.buildoptima.util.TestUtil.asJsonString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.vecondev.buildoptima.dto.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.news.request.NewsCreateRequestDto;
import com.vecondev.buildoptima.dto.news.request.NewsUpdateRequestDto;
import com.vecondev.buildoptima.endpoints.NewsEndpointUris;
import com.vecondev.buildoptima.manager.JwtTokenManager;
import com.vecondev.buildoptima.model.user.User;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@TestConfiguration
@RequiredArgsConstructor
public class NewsResultActions extends EntityResultActions<NewsEndpointUris> {

  private final NewsEndpointUris newsEndpointUris;
  private final MockMvc mockMvc;
  private final JwtTokenManager tokenManager;

  @Override
  protected NewsEndpointUris getEndpointUris() {
    return newsEndpointUris;
  }

  @Override
  protected MockMvc getMockMvc() {
    return mockMvc;
  }

  @Override
  protected JwtTokenManager getTokenManager() {
    return tokenManager;
  }

  @Override
  public ResultActions create(User user, Object requestDto) throws Exception {
    NewsCreateRequestDto createRequestDto = (NewsCreateRequestDto) requestDto;
    return mockMvc.perform(
        multipart(getEndpointUris().getCreationUri())
            .header(AUTHORIZATION_HEADER, getAccessToken(user))
            .content(createRequestDto.getImage().getBytes())
            .param("title", createRequestDto.getTitle())
            .param("summary", createRequestDto.getSummary())
            .param("description", createRequestDto.getDescription())
            .param("category", createRequestDto.getCategory())
            .param("keywords", " "));
  }

  @Override
  public ResultActions update(UUID entityId, User user, Object requestDto) throws Exception {
    NewsUpdateRequestDto updateRequestDto = (NewsUpdateRequestDto) requestDto;
    return mockMvc.perform(
        patch(getEndpointUris().getUpdateUri(), entityId)
            .header(AUTHORIZATION_HEADER, getAccessToken(user))
            .content(updateRequestDto.getImage().getBytes())
            .contentType(MULTIPART_FORM_DATA_VALUE)
            .param("title", updateRequestDto.getTitle())
            .param("summary", updateRequestDto.getSummary())
            .param("description", updateRequestDto.getDescription())
            .param("category", updateRequestDto.getCategory())
            .param("keywords", " "));
  }

  public ResultActions getMetadata(User user) throws Exception {
    return mockMvc.perform(
        get(getEndpointUris().getMetadataUri()).header(AUTHORIZATION_HEADER, getAccessToken(user)));
  }

  public ResultActions getAllInCsv(FetchRequestDto fetchRequest, User user) throws Exception {
    return mockMvc.perform(
        post(getEndpointUris().getExportInCsvUri())
            .header(AUTHORIZATION_HEADER, getAccessToken(user))
            .content(asJsonString(fetchRequest))
            .contentType(APPLICATION_JSON)
            .accept("application/csv"));
  }

  public ResultActions archive(UUID id, User user) throws Exception {
    return mockMvc.perform(
        patch(getEndpointUris().getArchiveUri(), id)
            .header(AUTHORIZATION_HEADER, getAccessToken(user)));
  }
}
