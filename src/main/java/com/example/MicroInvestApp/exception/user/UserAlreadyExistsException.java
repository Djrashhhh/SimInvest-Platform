//Purpose: Exception thrown when attempting to create a user that already exists.
package com.example.MicroInvestApp.exception.user;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    }

