package com.example.MicroInvestApp.exception.portfolio;

// Exception thrown when trying to create a portfolio for a user who already has one
public class PortfolioAlreadyExistsException extends RuntimeException {
    public PortfolioAlreadyExistsException(String message) {
        super(message);
    }
}
