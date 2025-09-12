package com.example.MicroInvestApp.service.market;

import com.example.MicroInvestApp.domain.market.MarketData;
import com.example.MicroInvestApp.domain.market.PriceHistory;
import com.example.MicroInvestApp.domain.market.SecurityStock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Enhanced Market Data Service Interface
 * Provides comprehensive market data management capabilities
 */
public interface MarketDataService {

    // Core market data operations

    /**
     * Fetch and store current market data for a given symbol
     * @param symbol The stock symbol
     * @return The stored MarketData entity
     * @throws RuntimeException if data fetch or storage fails
     */
    MarketData fetchAndStoreCurrentMarketData(String symbol);

    /**
     * Fetch and store historical market data for a date range
     * @param symbol The stock symbol
     * @param from Start date (inclusive)
     * @param to End date (inclusive)
     * @return List of stored MarketData entities
     * @throws RuntimeException if data fetch or storage fails
     */
    List<MarketData> fetchAndStoreHistoricalMarketData(String symbol, LocalDate from, LocalDate to);

    /**
     * Update only the current price for a security (lightweight operation)
     * @param symbol The stock symbol
     * @return Updated SecurityStock entity
     * @throws RuntimeException if price update fails
     */
    SecurityStock updateCurrentPrice(String symbol);

    // Data retrieval methods

    /**
     * Get market data for a specific symbol and date
     * @param symbol The stock symbol
     * @param date The market date
     * @return Optional MarketData if found
     */
    Optional<MarketData> getMarketData(String symbol, LocalDate date);

    /**
     * Get the latest market data for a symbol
     * @param symbol The stock symbol
     * @return Optional MarketData if found
     */
    Optional<MarketData> getLatestMarketData(String symbol);

    /**
     * Get historical price data for a symbol within date range
     * @param symbol The stock symbol
     * @param from Start date (inclusive)
     * @param to End date (inclusive)
     * @return List of PriceHistory entities
     */
    List<PriceHistory> getHistoricalPrices(String symbol, LocalDate from, LocalDate to);

    // Bulk operations

    /**
     * Update current prices for multiple symbols efficiently
     * @param symbols List of stock symbols to update
     */
    default void bulkUpdateCurrentPrices(List<String> symbols) {
        for (String symbol : symbols) {
            try {
                updateCurrentPrice(symbol);
            } catch (Exception e) {
                // Log error but continue with other symbols
                System.err.println("Failed to update price for " + symbol + ": " + e.getMessage());
            }
        }
    }

    // Data validation and quality methods

    /**
     * Validate if market data exists and is current for a symbol
     * @param symbol The stock symbol
     * @return true if data is current and valid
     */
    default boolean isMarketDataCurrent(String symbol) {
        Optional<MarketData> latest = getLatestMarketData(symbol);
        if (latest.isEmpty()) {
            return false;
        }

        MarketData data = latest.get();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // Data is current if it's from today or yesterday (depending on market status)
        return data.getMarketDate().equals(today) || data.getMarketDate().equals(yesterday);
    }

    /**
     * Check if a security has sufficient historical data
     * @param symbol The stock symbol
     * @param requiredDays Minimum number of days of data required
     * @return true if sufficient data exists
     */
    default boolean hasSufficientHistoricalData(String symbol, int requiredDays) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(requiredDays);

        List<PriceHistory> history = getHistoricalPrices(symbol, startDate, endDate);
        return history.size() >= requiredDays * 0.8; // 80% coverage is sufficient
    }

    // Utility methods for market data analysis

    /**
     * Calculate simple moving average for a symbol
     * @param symbol The stock symbol
     * @param days Number of days for moving average
     * @return Moving average as BigDecimal, or null if insufficient data
     */
    default java.math.BigDecimal calculateMovingAverage(String symbol, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        List<PriceHistory> prices = getHistoricalPrices(symbol, startDate, endDate);

        if (prices.size() < days * 0.8) { // Need at least 80% of required data
            return null;
        }

        java.math.BigDecimal sum = prices.stream()
                .map(PriceHistory::getClosePrice)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        return sum.divide(java.math.BigDecimal.valueOf(prices.size()),
                4, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Get price volatility (standard deviation) for a symbol
     * @param symbol The stock symbol
     * @param days Number of days to analyze
     * @return Price volatility as percentage, or null if insufficient data
     */
    default java.math.BigDecimal calculateVolatility(String symbol, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        List<PriceHistory> prices = getHistoricalPrices(symbol, startDate, endDate);

        if (prices.size() < days * 0.8) {
            return null;
        }

        // Calculate daily returns
        List<java.math.BigDecimal> returns = new java.util.ArrayList<>();
        for (int i = 1; i < prices.size(); i++) {
            java.math.BigDecimal previousPrice = prices.get(i-1).getClosePrice();
            java.math.BigDecimal currentPrice = prices.get(i).getClosePrice();

            if (previousPrice.compareTo(java.math.BigDecimal.ZERO) > 0) {
                java.math.BigDecimal dailyReturn = currentPrice.subtract(previousPrice)
                        .divide(previousPrice, 6, java.math.RoundingMode.HALF_UP);
                returns.add(dailyReturn);
            }
        }

        if (returns.isEmpty()) {
            return null;
        }

        // Calculate standard deviation
        java.math.BigDecimal mean = returns.stream()
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                .divide(java.math.BigDecimal.valueOf(returns.size()), 6, java.math.RoundingMode.HALF_UP);

        java.math.BigDecimal variance = returns.stream()
                .map(ret -> ret.subtract(mean).pow(2))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                .divide(java.math.BigDecimal.valueOf(returns.size()), 6, java.math.RoundingMode.HALF_UP);

        // Return annualized volatility as percentage
        double volatility = Math.sqrt(variance.doubleValue()) * Math.sqrt(252) * 100; // 252 trading days per year
        return java.math.BigDecimal.valueOf(volatility).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}