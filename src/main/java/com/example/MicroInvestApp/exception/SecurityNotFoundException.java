package com.example.MicroInvestApp.exception;

public class SecurityNotFoundException extends RuntimeException {
    public SecurityNotFoundException(String message) {

        super(message);
    }

    public SecurityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
