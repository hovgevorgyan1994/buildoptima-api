package com.vecondev.buildoptima.actions;

import com.vecondev.buildoptima.endpoints.FaqCategoryEndpointUris;
import com.vecondev.buildoptima.manager.JwtTokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@TestConfiguration
@RequiredArgsConstructor
public class FaqCategoryResultActions extends EntityResultActions<FaqCategoryEndpointUris> {

  private final FaqCategoryEndpointUris endpointUris;
  private final MockMvc mvc;

  @Override
  protected FaqCategoryEndpointUris getEndpointUris() {
    return endpointUris;
  }

  @Override
  protected MockMvc getMockMvc() {
    return mvc;
  }

}
