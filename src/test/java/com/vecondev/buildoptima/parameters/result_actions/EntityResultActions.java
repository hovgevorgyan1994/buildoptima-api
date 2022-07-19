package com.vecondev.buildoptima.parameters.result_actions;

import com.vecondev.buildoptima.dto.request.FetchRequestDto;
import com.vecondev.buildoptima.manager.JwtTokenManager;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.endpoints.EndpointUris;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static com.vecondev.buildoptima.util.TestUtil.asJsonString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public abstract class EntityResultActions<T extends EndpointUris> {

  protected static final String AUTHORIZATION_HEADER = "Authorization";
  protected static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";

  protected T endpointUris;
  protected MockMvc mvc;
  protected JwtTokenManager tokenManager;

  public void setResultActionsParameters(
      T endpointUris, MockMvc mvc, JwtTokenManager tokenManager) {
    this.endpointUris = endpointUris;
    this.mvc = mvc;
    this.tokenManager = tokenManager;
  }

  public ResultActions deleteByIdResultActions(UUID idToDelete, User user) throws Exception {
    return mvc.perform(
        delete(endpointUris.deleteByIdUri(), idToDelete)
            .header(AUTHORIZATION_HEADER, getAccessToken(user))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON_VALUE));
  }

  public ResultActions updateResultActions(UUID entityId, User user, Object requestDto)
      throws Exception {
    return mvc.perform(
        put(endpointUris.updateUri(), entityId.toString())
            .header(AUTHORIZATION_HEADER, getAccessToken(user))
            .content(asJsonString(requestDto))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON_VALUE));
  }

  public ResultActions creationResultActions(User user, Object requestDto) throws Exception {
    return mvc.perform(
        post(endpointUris.creationUri())
            .header(AUTHORIZATION_HEADER, getAccessToken(user))
            .content(asJsonString(requestDto))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON_VALUE));
  }

  public ResultActions fetchingResultActions(FetchRequestDto requestDto, User user)
      throws Exception {
    return mvc.perform(
        post(endpointUris.fetchUri())
            .header(AUTHORIZATION_HEADER, getAccessToken(user))
            .content(asJsonString(requestDto))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON));
  }

  public ResultActions getByIdResultActions(UUID entityId, User user) throws Exception {
    return mvc.perform(
            get(endpointUris.getByIdUri(), entityId.toString())
                    .header(AUTHORIZATION_HEADER, getAccessToken(user))
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON_VALUE));
  }

  public ResultActions getAllResultActions(User user) throws Exception {
    return mvc.perform(
            get(endpointUris.getAllUri())
                    .header(AUTHORIZATION_HEADER, getAccessToken(user))
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON_VALUE));
  }

  public String getAccessToken(User user) {
    return AUTHORIZATION_HEADER_PREFIX + tokenManager.generateAccessToken(user);
  }
}
