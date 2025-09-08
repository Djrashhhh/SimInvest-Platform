package com.example.MicroInvestApp.exception.portfolio;

//When a user tries to create a watchlist that already exists, this exception is thrown
public class WatchlistDuplicateException extends RuntimeException {
    public WatchlistDuplicateException(String message) {
        super(message);
    }
}
