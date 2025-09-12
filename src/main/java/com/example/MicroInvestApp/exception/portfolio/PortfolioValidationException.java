package com.example.MicroInvestApp.exception.portfolio;


// Exception thrown when portfolio validation fails
public class PortfolioValidationException extends RuntimeException {
    public PortfolioValidationException(String message) {
        super(message);
    }
}
