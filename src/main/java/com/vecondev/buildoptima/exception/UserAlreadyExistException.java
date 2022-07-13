package com.vecondev.buildoptima.exception;

public class UserAlreadyExistException extends BaseException {

  public UserAlreadyExistException(ErrorCode errorCode) {
    super(errorCode);
  }
}
