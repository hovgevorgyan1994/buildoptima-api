package com.vecondev.buildoptima.exception;

import com.vecondev.buildoptima.error.ApiErrorCode;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

  private final ApiErrorCode errorCode;

  public ApiException(ApiErrorCode errorCode) {
    this.errorCode = errorCode;
  }

}
