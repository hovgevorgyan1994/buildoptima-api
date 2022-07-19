package com.vecondev.buildoptima.parameters.endpoints;

public class UserEndpointUris implements EndpointUris {

  @Override
  public String baseUri() {
    return "/user";
  }

  @Override
  public String deleteByIdUri() {
    return null;
  }

  @Override
  public String updateUri() {
    return null;
  }

  @Override
  public String creationUri() {
    return null;
  }

  @Override
  public String fetchUri() {
    return baseUri() + "/fetch";
  }

  @Override
  public String getByIdUri() {
    return baseUri() + "/{id}";
  }

  @Override
  public String getAllUri() {
    return null;
  }

  public String imageDeletionUri() {
    return baseUri() + "/{id}/image";
  }

  public String imageDownloadingUri() {
    return baseUri() + "/{id}/";
  }

  public String imageUploadingUri() {
    return baseUri() + "/{id}/image";
  }

  public String passwordRestoringUri() {
    return baseUri() + "/auth/password/restore";
  }

  public String passwordVerificationUri() {
    return baseUri() + "/auth/password/verify";
  }

  public String changePasswordUri() {
    return baseUri() + "/password/change";
  }

  public String refreshTokenUri() {
    return baseUri() + "/auth/refreshToken";
  }

  public String loginUri() {
    return baseUri() + "/auth/login";
  }

  public String activationUri() {
    return baseUri() + "/auth/activate";
  }

  public String registrationUri() {
    return baseUri() + "/auth/registration";
  }
}
