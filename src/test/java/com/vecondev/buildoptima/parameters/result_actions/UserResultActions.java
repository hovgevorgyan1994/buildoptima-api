package com.vecondev.buildoptima.parameters.result_actions;

import com.vecondev.buildoptima.dto.request.AuthRequestDto;
import com.vecondev.buildoptima.dto.request.ChangePasswordRequestDto;
import com.vecondev.buildoptima.dto.request.ConfirmEmailRequestDto;
import com.vecondev.buildoptima.dto.request.RefreshTokenRequestDto;
import com.vecondev.buildoptima.dto.request.RestorePasswordRequestDto;
import com.vecondev.buildoptima.dto.request.UserRegistrationRequestDto;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.endpoints.UserEndpointUris;
import org.springframework.mock.web.MockMultipartFile;
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

public abstract class UserResultActions extends EntityResultActions<UserEndpointUris> {

  public ResultActions imageDeletionResultActions(UUID ownerId, User user) throws Exception {
    return mvc.perform(
        delete(endpointUris.imageDeletionUri(), ownerId.toString())
            .header(AUTHORIZATION_HEADER, getAccessToken(user))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON));
  }

  public ResultActions imageDownloadingResultActions(String imageType, UUID ownerId, User user)
      throws Exception {
    return mvc.perform(
        get(endpointUris.imageDownloadingUri() + imageType, ownerId.toString())
            .header(AUTHORIZATION_HEADER, getAccessToken(user))
            .contentType(APPLICATION_JSON)
            .accept("*/*"));
  }

  public ResultActions imageUploadingResultActions(MockMultipartFile file, UUID ownerId, User user)
      throws Exception {
    return mvc.perform(
        multipart(endpointUris.imageUploadingUri(), ownerId)
            .file(file)
            .header(AUTHORIZATION_HEADER, getAccessToken(user))
            .contentType(MULTIPART_FORM_DATA)
            .accept("*/*"));
  }

  public ResultActions passwordRestoringResultActions(RestorePasswordRequestDto requestDto)
      throws Exception {
    return mvc.perform(
        put(endpointUris.passwordRestoringUri())
            .content(asJsonString(requestDto))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON));
  }

  public ResultActions passwordVerificationResultActions(ConfirmEmailRequestDto requestDto)
      throws Exception {
    return mvc.perform(
        post(endpointUris.passwordVerificationUri())
            .content(asJsonString(requestDto))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON));
  }

  public ResultActions passwordChangingResultActions(ChangePasswordRequestDto requestDto, User user)
      throws Exception {
    return mvc.perform(
        put(endpointUris.changePasswordUri())
            .header(AUTHORIZATION_HEADER, getAccessToken(user))
            .content(asJsonString(requestDto))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON));
  }

  public ResultActions refreshTokenResultActions(RefreshTokenRequestDto requestDto)
      throws Exception {
    return mvc.perform(
        post(endpointUris.refreshTokenUri())
            .content(asJsonString(requestDto))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON));
  }

  public ResultActions loginResultActions(AuthRequestDto requestDto) throws Exception {
    return mvc.perform(
        post(endpointUris.loginUri())
            .content(asJsonString(requestDto))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON));
  }

  public ResultActions activationResultActions(String token) throws Exception {
    return mvc.perform(
        put(endpointUris.activationUri())
            .param("token", token)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON));
  }

  public ResultActions registrationResultActions(UserRegistrationRequestDto requestDto)
      throws Exception {
    return mvc.perform(
        post(endpointUris.registrationUri())
            .content(asJsonString(requestDto))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON));
  }
}
