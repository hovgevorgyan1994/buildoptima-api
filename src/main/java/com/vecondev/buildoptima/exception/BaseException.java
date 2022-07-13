package com.vecondev.buildoptima.exception;

import lombok.Data;

@Data
public class BaseException extends RuntimeException {

  protected final ErrorCode errorCode;

  public BaseException(ErrorCode errorCode) {
    this.errorCode = errorCode;
  }
}
