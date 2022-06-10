package com.vecondev.buildoptima.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

  HttpStatus getHttpStatus();

  int getErrorCode();
}
