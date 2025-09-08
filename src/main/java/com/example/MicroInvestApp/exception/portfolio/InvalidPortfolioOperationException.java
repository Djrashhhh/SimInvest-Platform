package com.example.MicroInvestApp.exception.portfolio;

// Exception thrown for invalid portfolio operations
public class InvalidPortfolioOperationException extends RuntimeException {
    public InvalidPortfolioOperationException(String message) {
        super(message);
    }
}
