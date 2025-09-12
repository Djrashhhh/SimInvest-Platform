package com.example.MicroInvestApp.exception.portfolio;

public class PositionCalculationException extends RuntimeException {
    private final String symbol;
    private final String portfolioId;

    // Constructor with symbol and portfolio ID
    public PositionCalculationException(String message, String symbol, String portfolioId, Throwable cause) {
        super(message, cause);
        this.symbol = symbol;
        this.portfolioId = portfolioId;
    }

    // Constructor without symbol and portfolio ID (for general errors)
    public PositionCalculationException(String message, Throwable cause) {
        super(message, cause);
        this.symbol = null;
        this.portfolioId = null;
    }

    // Constructor with just message
    public PositionCalculationException(String message) {
        super(message);
        this.symbol = null;
        this.portfolioId = null;
    }

    // Constructor with message, symbol, and portfolio ID (no cause)
    public PositionCalculationException(String message, String symbol, String portfolioId) {
        super(message);
        this.symbol = symbol;
        this.portfolioId = portfolioId;
    }

    // Getters
    public String getSymbol() {
        return symbol;
    }

    public String getPortfolioId() {
        return portfolioId;
    }
}