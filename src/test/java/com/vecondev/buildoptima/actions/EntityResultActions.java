package com.vecondev.buildoptima.actions;

import static com.vecondev.buildoptima.util.TestUtil.asJsonString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vecondev.buildoptima.dto.filter.FetchRequestDto;
import com.vecondev.buildoptima.endpoints.EndpointUris;
import com.vecondev.buildoptima.model.user.User;
import java.util.UUID;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public abstract class EntityResultActions<T extends EndpointUris> {

  protected static final String USER_ID_HEADER = "user_id";
  protected static final String USERNAME_HEADER = "username";
  protected static final String AUTHORITIES_HEADER = "authorities";

  protected abstract T getEndpointUris();

  protected abstract MockMvc getMockMvc();


  public ResultActions deleteById(UUID idToDelete, User user) throws Exception {
    return getMockMvc()
        .perform(
            addAuthorizationHeaders(delete(getEndpointUris().getDeleteByIdUri(), idToDelete), user)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE));
  }

  public ResultActions update(UUID entityId, User user, Object requestDto) throws Exception {
    return getMockMvc()
        .perform(
            addAuthorizationHeaders(
                    put(getEndpointUris().getUpdateUri(), entityId.toString()), user)
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE));
  }

  public ResultActions create(User user, Object requestDto) throws Exception {
    return getMockMvc()
        .perform(
            addAuthorizationHeaders(post(getEndpointUris().getCreationUri()), user)
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE));
  }

  public ResultActions fetch(FetchRequestDto requestDto, User user) throws Exception {
    return getMockMvc()
        .perform(
            addAuthorizationHeaders(post(getEndpointUris().getFetchUri()), user)
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));
  }

  public ResultActions getById(UUID entityId, User user) throws Exception {
    return getMockMvc()
        .perform(
            addAuthorizationHeaders(
                    get(getEndpointUris().getRetrieveByIdUri(), entityId.toString()), user)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE));
  }

  public ResultActions getAll(User user) throws Exception {
    return getMockMvc()
        .perform(
            addAuthorizationHeaders(get(getEndpointUris().getAllUri()), user)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE));
  }

  public ResultActions getAllInCsv(User user) throws Exception {
    return getMockMvc()
        .perform(
            addAuthorizationHeaders(get(getEndpointUris().getExportInCsvUri()), user)
                .contentType(APPLICATION_JSON)
                .accept("application/csv"));
  }

  public ResultActions getMetadata(User user) throws Exception {
    return getMockMvc()
        .perform(
            addAuthorizationHeaders(get(getEndpointUris().getMetadataUri()), user)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE));
  }

  public MockHttpServletRequestBuilder addAuthorizationHeaders(
      MockHttpServletRequestBuilder builder, User user) throws JsonProcessingException {
    return builder
        .header(USER_ID_HEADER, user.getId())
        .header(USERNAME_HEADER, user.getEmail())
        .header(
            AUTHORITIES_HEADER,
            new ObjectMapper()
                .writeValueAsString(
                    user.getRole().getAuthorities().stream()
                        .map(SimpleGrantedAuthority::toString)
                        .toList()));
  }
}
