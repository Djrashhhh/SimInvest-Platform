package com.example.MicroInvestApp.exception.user;

public class UserProfileNotFoundException extends RuntimeException {
    public UserProfileNotFoundException(String message) {
        super(message);
    }
    public UserProfileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserProfileNotFoundException(Long userId) {
        super("User profile not found for user ID: " + userId);
    }
}
