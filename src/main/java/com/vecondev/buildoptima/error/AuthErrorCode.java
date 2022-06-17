package com.vecondev.buildoptima.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {
  AUTH_BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, 200,"Bad Credentials"),
  AUTH_REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, 201,"Invalid Refresh Token"),
  AUTH_REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 202,"Expired Refresh Token"),
  AUTH_CREDENTIALS_ALREADY_EXIST(HttpStatus.CONFLICT, 204,"Credentials Already Exist"),
  AUTH_CREDENTIALS_USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, 205,"Username Already Exists"),
  AUTH_CREDENTIALS_NOT_FOUND(HttpStatus.NOT_FOUND, 206,"Credentials Not Found"),
  AUTH_ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 207,"Expired Access Token"),
  AUTH_CONFIRM_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, 208,"Confirmation Token Not Found");

  private final HttpStatus httpStatus;
  private final int errorCode;
  private final String message;

  @Override
  public int getErrorCode() {
    return 0;
  }
}
