package com.vecondev.buildoptima.exception;

import lombok.Getter;

@Getter
public class NewsException extends BaseException {

  public NewsException(ErrorCode errorCode) {
    super(errorCode);
  }
}
