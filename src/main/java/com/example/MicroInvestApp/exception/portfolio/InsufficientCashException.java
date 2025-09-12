package com.example.MicroInvestApp.exception.portfolio;

// This exception is thrown when a user attempts to perform an operation that requires more cash than they have available in their portfolio.
public class InsufficientCashException extends RuntimeException {
    public InsufficientCashException(String message) {
        super(message);
    }
}
