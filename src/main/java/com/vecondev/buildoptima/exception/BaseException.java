package com.vecondev.buildoptima.exception;

import com.vecondev.buildoptima.error.ErrorCode;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

  private final ErrorCode errorCode;

  private final Integer statusCode;

  private final String message;

  public BaseException(ErrorCode errorCode) {
    this(errorCode, null, null);
  }

  public BaseException(ErrorCode errorCode, Integer statusCode) {
    this(errorCode, statusCode, null);
  }

  public BaseException(ErrorCode errorCode, Integer statusCode, String message) {
    this.errorCode = errorCode;
    this.statusCode = statusCode;
    this.message = message;
  }
}
