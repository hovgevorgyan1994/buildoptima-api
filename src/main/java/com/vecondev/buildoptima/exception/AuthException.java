package com.vecondev.buildoptima.exception;

import com.vecondev.buildoptima.error.ErrorCode;

public class AuthException extends BaseException {

  public AuthException(ErrorCode errorCode) {
    super(errorCode);
  }
}
