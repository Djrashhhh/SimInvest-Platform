package com.example.MicroInvestApp.exception.portfolio;

// Exception thrown when watchlist operation limits are exceeded
public class WatchlistLimitExceededException extends RuntimeException {
    public WatchlistLimitExceededException(String message) {
        super(message);
    }
}
