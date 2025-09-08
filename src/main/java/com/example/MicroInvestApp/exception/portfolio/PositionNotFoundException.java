package com.example.MicroInvestApp.exception.portfolio;

//Exception thrown when a position is not found
public class PositionNotFoundException extends RuntimeException {
    public PositionNotFoundException(String message) {
        super(message);
    }
}
