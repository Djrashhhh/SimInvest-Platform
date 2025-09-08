package com.example.MicroInvestApp.exception.user;

public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException(String message) {
        super(message);
    }
    public SessionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionNotFoundException(Long sessionId) {
        super("Session not found with ID: " + sessionId);
    }


}
