package com.example.MicroInvestApp.exception.portfolio;

// Exception thrown when trying to create a duplicate position in a portfolio
public class PositionAlreadyExistsException extends RuntimeException {
    public PositionAlreadyExistsException(String message) {
        super(message);
    }
}
