package com.example.MicroInvestApp.exception.portfolio;

// Exception thrown when trying to create a watchlist with a name that already exists
public class WatchlistNotFoundException extends RuntimeException {
    public WatchlistNotFoundException(String message) {
        super(message);
    }
}
