package com.vecondev.buildoptima.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {
  AUTH_BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, 200),
  AUTH_REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, 201),
  AUTH_REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 202),
  AUTH_CREDENTIALS_ALREADY_EXISTS(HttpStatus.CONFLICT, 204),
  AUTH_CREDENTIALS_USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, 205),
  AUTH_CREDENTIALS_NOT_FOUND(HttpStatus.NOT_FOUND, 206),
  AUTH_ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 207);

  private final HttpStatus httpStatus;
  private final int errorCode;

  @Override
  public int getErrorCode() {
    return 0;
  }
}
