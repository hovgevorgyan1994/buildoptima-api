package com.vecondev.buildoptima.parameters.actions;

import com.vecondev.buildoptima.manager.JwtTokenManager;
import com.vecondev.buildoptima.parameters.endpoints.FaqCategoryEndpointUris;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@TestConfiguration
@RequiredArgsConstructor
public class FaqCategoryResultActions extends EntityResultActions<FaqCategoryEndpointUris> {

  private final FaqCategoryEndpointUris endpointUris;
  private final MockMvc mvc;
  private final JwtTokenManager tokenManager;

  @Override
  protected FaqCategoryEndpointUris getEndpointUris() {
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
