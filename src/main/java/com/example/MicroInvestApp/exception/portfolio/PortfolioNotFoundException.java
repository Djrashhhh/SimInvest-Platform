package com.example.MicroInvestApp.exception.portfolio;

// Exception thrown when a portfolio is not found
public class PortfolioNotFoundException extends RuntimeException {
    public PortfolioNotFoundException(String message) {
        super(message);
    }
}
