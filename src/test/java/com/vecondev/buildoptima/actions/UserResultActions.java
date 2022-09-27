package com.vecondev.buildoptima.actions;

import static com.vecondev.buildoptima.util.TestUtil.asJsonString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.vecondev.buildoptima.dto.user.request.AuthRequestDto;
import com.vecondev.buildoptima.dto.user.request.ChangePasswordRequestDto;
import com.vecondev.buildoptima.dto.user.request.ConfirmEmailRequestDto;
import com.vecondev.buildoptima.dto.user.request.EditUserDto;
import com.vecondev.buildoptima.dto.user.request.RefreshTokenRequestDto;
import com.vecondev.buildoptima.dto.user.request.RestorePasswordRequestDto;
import com.vecondev.buildoptima.dto.user.request.UserRegistrationRequestDto;
import com.vecondev.buildoptima.endpoints.UserEndpointUris;
import com.vecondev.buildoptima.manager.JwtTokenManager;
import com.vecondev.buildoptima.model.user.User;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@TestConfiguration
@RequiredArgsConstructor
public class UserResultActions extends EntityResultActions<UserEndpointUris> {

  private final UserEndpointUris endpointUris;
  private final MockMvc mvc;

  @Override
  protected UserEndpointUris getEndpointUris() {
    return endpointUris;
  }

  @Override
  protected MockMvc getMockMvc() {
    return mvc;
  }

  public ResultActions deleteImage(UUID ownerId, User user) throws Exception {
    return getMockMvc()
        .perform(
            addAuthorizationHeaders(
                    delete(getEndpointUris().getImageDeletionUri(), ownerId.toString()), user)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));
  }

  public ResultActions downloadImage(String imageType, UUID ownerId, User user) throws Exception {
    return getMockMvc()
        .perform(
            addAuthorizationHeaders(
                    get(getEndpointUris().getImageDownloadingUri() + imageType, ownerId.toString()),
                    user)
                .contentType(APPLICATION_JSON)
                .accept("*/*"));
  }

  public ResultActions uploadImage(MockMultipartFile file, UUID ownerId, User user)
      throws Exception {
    return getMockMvc()
        .perform(
            addAuthorizationHeaders(
                    multipart(getEndpointUris().getImageUploadingUri(), ownerId).file(file), user)
                .contentType(MULTIPART_FORM_DATA)
                .accept("*/*"));
  }

  public ResultActions restorePassword(RestorePasswordRequestDto requestDto) throws Exception {
    return getMockMvc()
        .perform(
            put(getEndpointUris().getPasswordRestoringUri())
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));
  }

  public ResultActions verify(ConfirmEmailRequestDto requestDto) throws Exception {
    return getMockMvc()
        .perform(
            post(getEndpointUris().getPasswordVerificationUri())
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));
  }

  public ResultActions changePassword(ChangePasswordRequestDto requestDto, User user)
      throws Exception {
    return getMockMvc()
        .perform(
            addAuthorizationHeaders(put(getEndpointUris().getChangePasswordUri()), user)
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));
  }

  public ResultActions refresh(RefreshTokenRequestDto requestDto) throws Exception {
    return getMockMvc()
        .perform(
            post(getEndpointUris().getRefreshTokenUri())
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));
  }

  public ResultActions login(AuthRequestDto requestDto) throws Exception {
    return getMockMvc()
        .perform(
            post(getEndpointUris().getLoginUri())
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));
  }

  public ResultActions activate(String token) throws Exception {
    return getMockMvc()
        .perform(
            put(getEndpointUris().getActivationUri())
                .param("token", token)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));
  }

  public ResultActions register(UserRegistrationRequestDto requestDto) throws Exception {
    return getMockMvc()
        .perform(
            post(getEndpointUris().getRegistrationUri())
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));
  }

  public ResultActions editUser(UUID id, EditUserDto requestDto, Locale locale, User user)
      throws Exception {
    return getMockMvc()
        .perform(
            addAuthorizationHeaders(patch(getEndpointUris().getEditUserUri(), id, locale), user)
                .content(asJsonString(requestDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));
  }
}
