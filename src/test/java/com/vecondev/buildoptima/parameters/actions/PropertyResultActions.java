package com.vecondev.buildoptima.parameters.actions;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.vecondev.buildoptima.manager.JwtTokenManager;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.endpoints.PropertyEndpointUris;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@TestConfiguration
@RequiredArgsConstructor
public class PropertyResultActions extends EntityResultActions<PropertyEndpointUris> {

  private final PropertyEndpointUris endpointUris;
  private final MockMvc mockMvc;
  private final JwtTokenManager jwtTokenManager;

  @Override
  protected PropertyEndpointUris getEndpointUris() {
    return endpointUris;
  }

  @Override
  protected MockMvc getMockMvc() {
    return mockMvc;
  }

  @Override
  protected JwtTokenManager getTokenManager() {
    return jwtTokenManager;
  }

  public ResultActions migrationResultActions(User user) throws Exception {
    return getPostRequest(user, endpointUris.getMigrationUri());
  }

  public ResultActions reprocessResultActions(User user) throws Exception {
    return getPostRequest(user, endpointUris.getReprocessUri());
  }

  public ResultActions trackProgressResultActions(User user) throws Exception {
    return mockMvc.perform(
        get(endpointUris.getTrackProgressUri())
            .header(AUTHORIZATION_HEADER, getAccessToken(user))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON_VALUE));
  }

  public ResultActions getByAin(User user, String ain) throws Exception {
    return mockMvc.perform(
        get(endpointUris.getByAinUri(), ain)
            .header(AUTHORIZATION_HEADER, getAccessToken(user))
            .accept(APPLICATION_JSON_VALUE));
  }

  private ResultActions getPostRequest(User user, String uri) throws Exception {
    return mockMvc.perform(
        post(uri)
            .header(AUTHORIZATION_HEADER, getAccessToken(user))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON_VALUE));
  }
}
