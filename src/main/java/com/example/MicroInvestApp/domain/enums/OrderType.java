package com.example.MicroInvestApp.domain.enums;

public enum OrderType {
    MARKET("Execute immediately at current market price"),
    LIMIT("Execute only at specified price or better"),
    STOP_LOSS("Execute market order when price reaches stop price"),
    STOP_LIMIT("Execute limit order when price reaches stop price"),
    TRAILING_STOP("Stop price adjusts with favorable price movements"),
    GOOD_TILL_CANCELLED("Order remains active until filled or cancelled"),
    FILL_OR_KILL("Execute entire order immediately or cancel"),
    IMMEDIATE_OR_CANCEL("Execute as much as possible immediately, cancel remainder");

    private final String description;

    OrderType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresPrice() {
        return this == LIMIT || this == STOP_LIMIT;
    }

    public boolean isStopOrder() {
        return this == STOP_LOSS || this == STOP_LIMIT;
    }

    public boolean isMarketOrder() {
        return this == MARKET || this == STOP_LOSS;
    }

    public boolean isLimitOrder() {
        return this == LIMIT || this == STOP_LIMIT;
    }
}
