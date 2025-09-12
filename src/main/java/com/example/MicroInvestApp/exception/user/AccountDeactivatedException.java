package com.example.MicroInvestApp.exception.user;

public class AccountDeactivatedException extends RuntimeException {
    public AccountDeactivatedException(String message) {
        super(message);
    }
    public AccountDeactivatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
