package com.vecondev.buildoptima.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Bad Credentials"),
  ACCESS_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "Access Token Missing"),
  REFRESH_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "Invalid Refresh Token"),
  REFRESH_TOKEN_EXPIRED(HttpStatus.FORBIDDEN, "Expired Refresh Token"),
  CREDENTIALS_NOT_FOUND(HttpStatus.NOT_FOUND, "Credentials Not Found"),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User Not Found"),
  ACCESS_TOKEN_EXPIRED(HttpStatus.FORBIDDEN, "Expired Access Token"),
  CONFIRM_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "Confirmation Token Not Found"),
  PROVIDED_SAME_PASSWORD(
      HttpStatus.CONFLICT, "Provided The Same Password In Change Password Request"),
  PROVIDED_WRONG_PASSWORD(
      HttpStatus.BAD_REQUEST, "Provided Wrong Password In Change Password Request"),

  SEND_EMAIL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed To Send An Email"),
  INVALID_PAGEABLE(HttpStatus.BAD_REQUEST, "The `skip` Value Should Be Divisible To `take`Value"),
  INVALID_FILTER_STRUCTURE(HttpStatus.BAD_REQUEST, "Invalid Filter Structure");

  private final HttpStatus httpStatus;
  private final String message;
}
