package com.vecondev.buildoptima.exception;

public class UserNotFoundException extends BaseException {

  public UserNotFoundException(ErrorCode errorCode) {
    super(errorCode);
  }
}
