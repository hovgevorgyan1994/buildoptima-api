package com.vecondev.buildoptima.exception;

import lombok.Getter;

@Getter
public class WrongFieldException extends RuntimeException {

  private final String message;

  public WrongFieldException(String message) {
    this.message = message;
  }
}
