package com.vecondev.buildoptima.exception;

import com.vecondev.buildoptima.error.ErrorCode;
import lombok.Getter;

@Getter
public class AuthenticationException extends RuntimeException {

  private ErrorCode errorCode;
  private String message;

  public AuthenticationException(ErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  public AuthenticationException(String message) {
    this.message = message;
  }
}
