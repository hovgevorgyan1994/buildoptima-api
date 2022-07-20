package com.vecondev.buildoptima.exception;


public class DataIntegrityViolationException extends BaseException{

    public DataIntegrityViolationException(ErrorCode errorCode) {
    super(errorCode);
    }
}
