package com.example.MicroInvestApp.exception.portfolio;

// Exception thrown when a dividend is not found
public class DividendNotFoundException extends RuntimeException {
    public DividendNotFoundException(String message) {
        super(message);
    }
    public DividendNotFoundException( Throwable cause) {
        super(cause);
    }
}
