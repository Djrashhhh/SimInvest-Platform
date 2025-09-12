package com.example.MicroInvestApp.exception.portfolio;

// Exception thrown when there's insufficient quantity for a position operation
public class InsufficientQuantityException extends RuntimeException {
    public InsufficientQuantityException(String message) {
        super(message);
    }
}
