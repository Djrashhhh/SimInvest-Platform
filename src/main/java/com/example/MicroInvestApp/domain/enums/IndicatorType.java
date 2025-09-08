package com.example.MicroInvestApp.domain.enums;

public enum IndicatorType {
    MOVING_AVERAGE_50_DAY,
    MOVING_AVERAGE_200_DAY,
    RSI, // Relative Strength Index
    MACD, // Moving Average Convergence Divergence
    BOLLINGER_BANDS, // Bollinger Bands
    STOCHASTIC_OSCILLATOR, // Stochastic Oscillator
    ATR, // Average True Range
    VOLUME_WEIGHTED_AVERAGE_PRICE; // Volume Weighted Average Price

    @Override
    public String toString() {
        return name().replace("_", " ").toLowerCase();
    }
}
