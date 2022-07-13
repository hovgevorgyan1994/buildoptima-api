package com.vecondev.buildoptima.exception;

import lombok.Getter;

@Getter
public class AuthenticationException extends RuntimeException {

  private final ErrorCode errorCode;

  public AuthenticationException(ErrorCode errorCode) {
    this.errorCode = errorCode;
  }

}
