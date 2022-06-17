package com.vecondev.buildoptima.exception;

import com.vecondev.buildoptima.error.AuthErrorCode;

public class AuthException extends BaseException {

  public AuthException(AuthErrorCode errorCode,String message) {
    super(errorCode, message);
  }

}
