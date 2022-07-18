package com.vecondev.buildoptima.exception;

import lombok.Getter;

@Getter
public class WrongFieldException extends BaseException {

  public WrongFieldException(ErrorCode errorCode) {
    super(errorCode);
  }
}
