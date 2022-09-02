package com.vecondev.buildoptima.actions;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.vecondev.buildoptima.endpoints.PropertyEndpointUris;
import com.vecondev.buildoptima.filter.model.PropertySearchCriteria;
import com.vecondev.buildoptima.manager.JwtTokenManager;
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

  public ResultActions search(String value, PropertySearchCriteria by) throws Exception {
    return mockMvc.perform(
        get(endpointUris.getSearchUri())
            .param("value", value)
            .param("by", by.name())
            .accept(APPLICATION_JSON_VALUE));
  }

  public ResultActions getByAin(String ain) throws Exception {
    return mockMvc.perform(get(endpointUris.getFindByAinUri(), ain).accept(APPLICATION_JSON_VALUE));
  }
}
