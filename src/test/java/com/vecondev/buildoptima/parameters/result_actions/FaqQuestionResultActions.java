package com.vecondev.buildoptima.parameters.result_actions;

import com.vecondev.buildoptima.manager.JwtTokenManager;
import com.vecondev.buildoptima.parameters.endpoints.FaqQuestionEndpointUris;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@TestConfiguration
@RequiredArgsConstructor
public class FaqQuestionResultActions extends EntityResultActions<FaqQuestionEndpointUris> {

  private final FaqQuestionEndpointUris endpointUris;
  private final MockMvc mvc;
  private final JwtTokenManager tokenManager;

  @Override
  protected FaqQuestionEndpointUris getEndpointUris() {
    return endpointUris;
  }

  @Override
  protected MockMvc getMockMvc() {
    return mvc;
  }

  @Override
  protected JwtTokenManager getTokenManager() {
    return tokenManager;
  }
}
