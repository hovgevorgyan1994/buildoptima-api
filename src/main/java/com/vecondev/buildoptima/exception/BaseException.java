package com.vecondev.buildoptima.exception;

import com.vecondev.buildoptima.error.AuthErrorCode;
import com.vecondev.buildoptima.error.ErrorCode;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

  private final AuthErrorCode errorCode;

  private final Integer statusCode;

  private final String message;

  public BaseException(AuthErrorCode errorCode, String message) {
    this(errorCode, null, message);
  }

  public BaseException(AuthErrorCode errorCode, Integer statusCode, String message) {
    this.errorCode = errorCode;
    this.statusCode = statusCode;
    this.message = message;
  }
}
