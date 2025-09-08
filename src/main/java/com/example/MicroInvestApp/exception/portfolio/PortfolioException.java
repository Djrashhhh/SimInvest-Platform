package com.example.MicroInvestApp.exception.portfolio;

// Base exception for all portfolio-related errors
public class PortfolioException extends RuntimeException {
    public PortfolioException(String message) {
        super(message);
    }
  public PortfolioException(String message, Throwable cause) {
    super(message, cause);
  }
}
