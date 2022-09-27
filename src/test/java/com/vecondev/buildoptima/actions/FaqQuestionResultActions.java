package com.vecondev.buildoptima.actions;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.vecondev.buildoptima.endpoints.FaqQuestionEndpointUris;
import com.vecondev.buildoptima.filter.model.DictionaryField;
import com.vecondev.buildoptima.manager.JwtTokenManager;
import com.vecondev.buildoptima.model.Status;
import com.vecondev.buildoptima.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@TestConfiguration
@RequiredArgsConstructor
public class FaqQuestionResultActions extends EntityResultActions<FaqQuestionEndpointUris> {

  private final FaqQuestionEndpointUris endpointUris;
  private final MockMvc mvc;

  @Override
  protected FaqQuestionEndpointUris getEndpointUris() {
    return endpointUris;
  }

  @Override
  protected MockMvc getMockMvc() {
    return mvc;
  }


  public ResultActions lookup(Status status, DictionaryField dictionary, User user)
      throws Exception {
    return getMockMvc()
        .perform(
            addAuthorizationHeaders(get(getEndpointUris().getLookupUri(), status, dictionary), user)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON_VALUE));
  }
}
