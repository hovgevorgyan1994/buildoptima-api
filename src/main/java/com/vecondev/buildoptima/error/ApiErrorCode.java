package com.vecondev.buildoptima.error;

import antlr.MismatchedCharException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ApiErrorCode implements ErrorCode {
  BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, 200, "Bad Credentials"),
  REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, 201, "Invalid Refresh Token"),
  REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 202, "Expired Refresh Token"),
  CREDENTIALS_ALREADY_EXIST(HttpStatus.CONFLICT, 204, "Credentials Already Exist"),
  CREDENTIALS_USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, 205, "Username Already Exists"),
  CREDENTIALS_NOT_FOUND(HttpStatus.NOT_FOUND, 206, "Credentials Not Found"),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, 206, "User Not Found"),
  ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 207, "Expired Access Token"),
  CONFIRM_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, 208, "Confirmation Token Not Found"),
  PROVIDED_SAME_PASSWORD(
      HttpStatus.CONFLICT, 209, "Provided The Same Password In Change Password Request"),
  PROVIDED_WRONG_PASSWORD(
      HttpStatus.BAD_REQUEST, 210, "Provided Wrong Password In Change Password Request"),

  SEND_EMAIL_FAILED(HttpStatus.EXPECTATION_FAILED, 211, "Failed To Send An Email");

  private final HttpStatus httpStatus;
  private final int errorCode;
  private final String message;

  @Override
  public int getErrorCode() {
    return 0;
  }
}
