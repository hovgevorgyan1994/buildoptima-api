package com.vecondev.buildoptima.parameters.actions;

import static com.vecondev.buildoptima.util.TestUtil.asJsonString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.vecondev.buildoptima.dto.filter.FetchRequestDto;
import com.vecondev.buildoptima.manager.JwtTokenManager;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.endpoints.EndpointUris;
import java.util.UUID;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

public abstract class EntityResultActions<T extends EndpointUris> {

  protected static final String AUTHORIZATION_HEADER = "Authorization";
  protected static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";

  protected abstract T getEndpointUris();

  protected abstract MockMvc getMockMvc();

  protected abstract JwtTokenManager getTokenManager();

  public ResultActions deleteByIdResultActions(UUID idToDelete, User user) throws Exception {
    return getMockMvc()
        .perform(
            delete(getEndpointUris().getDeleteByIdUri(), idToDelete)
                .header(AUTHORIZATION_HEADER, getAccessToken(user))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE));
  }

  public ResultActions updateResultActions(UUID entityId, User user, Object requestDto)
      throws Exception {
    return getMockMvc()
        .perform(
            put(getEndpointUris().getUpdateUri(), entityId.toString())
                .header(AUTHORIZATION_HEADER, getAccessToken(user))
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE));
  }

  public ResultActions creationResultActions(User user, Object requestDto) throws Exception {
    return getMockMvc()
        .perform(
            post(getEndpointUris().getCreationUri())
                .header(AUTHORIZATION_HEADER, getAccessToken(user))
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE));
  }

  public ResultActions fetchingResultActions(FetchRequestDto requestDto, User user)
      throws Exception {
    return getMockMvc()
        .perform(
            post(getEndpointUris().getFetchUri())
                .header(AUTHORIZATION_HEADER, getAccessToken(user))
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));
  }

  public ResultActions getByIdResultActions(UUID entityId, User user) throws Exception {
    return getMockMvc()
        .perform(
            get(getEndpointUris().getRetrieveByIdUri(), entityId.toString())
                .header(AUTHORIZATION_HEADER, getAccessToken(user))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE));
  }

  public ResultActions getAllResultActions(User user) throws Exception {
    return getMockMvc()
        .perform(
            get(getEndpointUris().getAllUri())
                .header(AUTHORIZATION_HEADER, getAccessToken(user))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE));
  }

  public ResultActions getAllInCsvResultActions(User user) throws Exception {
    return getMockMvc()
        .perform(
            get(getEndpointUris().getExportInCsvUri())
                .header(AUTHORIZATION_HEADER, getAccessToken(user))
                .contentType(APPLICATION_JSON)
                .accept("application/csv"));
  }

  public ResultActions getMetadataResultActions(User user) throws Exception {
    return getMockMvc()
        .perform(
            get(getEndpointUris().getMetadataUri())
                .header(AUTHORIZATION_HEADER, getAccessToken(user))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE));
  }

  public String getAccessToken(User user) {
    return AUTHORIZATION_HEADER_PREFIX + getTokenManager().generateAccessToken(user);
  }
}
