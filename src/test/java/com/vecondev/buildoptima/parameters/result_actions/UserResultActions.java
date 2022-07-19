package com.vecondev.buildoptima.parameters.result_actions;

import com.vecondev.buildoptima.dto.request.user.AuthRequestDto;
import com.vecondev.buildoptima.dto.request.user.ChangePasswordRequestDto;
import com.vecondev.buildoptima.dto.request.user.ConfirmEmailRequestDto;
import com.vecondev.buildoptima.dto.request.user.RefreshTokenRequestDto;
import com.vecondev.buildoptima.dto.request.user.RestorePasswordRequestDto;
import com.vecondev.buildoptima.dto.request.user.UserRegistrationRequestDto;
import com.vecondev.buildoptima.manager.JwtTokenManager;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.endpoints.UserEndpointUris;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static com.vecondev.buildoptima.util.TestUtil.asJsonString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@TestConfiguration
@RequiredArgsConstructor
public class UserResultActions extends EntityResultActions<UserEndpointUris> {

  private final UserEndpointUris endpointUris;
  private final MockMvc mvc;
  private final JwtTokenManager tokenManager;

  @Override
  protected UserEndpointUris getEndpointUris() {
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

  public ResultActions imageDeletionResultActions(UUID ownerId, User user) throws Exception {
    return getMockMvc()
        .perform(
            delete(getEndpointUris().getImageDeletionUri(), ownerId.toString())
                .header(AUTHORIZATION_HEADER, getAccessToken(user))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));
  }

  public ResultActions imageDownloadingResultActions(String imageType, UUID ownerId, User user)
      throws Exception {
    return getMockMvc()
        .perform(
            get(getEndpointUris().getImageDownloadingUri() + imageType, ownerId.toString())
                .header(AUTHORIZATION_HEADER, getAccessToken(user))
                .contentType(APPLICATION_JSON)
                .accept("*/*"));
  }

  public ResultActions imageUploadingResultActions(MockMultipartFile file, UUID ownerId, User user)
      throws Exception {
    return getMockMvc()
        .perform(
            multipart(getEndpointUris().getImageUploadingUri(), ownerId)
                .file(file)
                .header(AUTHORIZATION_HEADER, getAccessToken(user))
                .contentType(MULTIPART_FORM_DATA)
                .accept("*/*"));
  }

  public ResultActions passwordRestoringResultActions(RestorePasswordRequestDto requestDto)
      throws Exception {
    return getMockMvc()
        .perform(
            put(getEndpointUris().getPasswordRestoringUri())
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));
  }

  public ResultActions passwordVerificationResultActions(ConfirmEmailRequestDto requestDto)
      throws Exception {
    return getMockMvc()
        .perform(
            post(getEndpointUris().getPasswordVerificationUri())
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));
  }

  public ResultActions passwordChangingResultActions(ChangePasswordRequestDto requestDto, User user)
      throws Exception {
    return getMockMvc()
        .perform(
            put(getEndpointUris().getChangePasswordUri())
                .header(AUTHORIZATION_HEADER, getAccessToken(user))
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));
  }

  public ResultActions refreshTokenResultActions(RefreshTokenRequestDto requestDto)
      throws Exception {
    return getMockMvc()
        .perform(
            post(getEndpointUris().getRefreshTokenUri())
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));
  }

  public ResultActions loginResultActions(AuthRequestDto requestDto) throws Exception {
    return getMockMvc()
        .perform(
            post(getEndpointUris().getLoginUri())
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));
  }

  public ResultActions activationResultActions(String token) throws Exception {
    return getMockMvc()
        .perform(
            put(getEndpointUris().getActivationUri())
                .param("token", token)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));
  }

  public ResultActions registrationResultActions(UserRegistrationRequestDto requestDto)
      throws Exception {
    return getMockMvc()
        .perform(
            post(getEndpointUris().getRegistrationUri())
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));
  }
}
