// Purpose: Exception for authentication failures. Security-focused error handling.
package com.example.MicroInvestApp.exception.user;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
