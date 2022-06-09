package com.vecondev.buildoptima.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("There is no user registered with such id!");
    }
}
