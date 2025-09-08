package com.example.MicroInvestApp.domain.enums;

public enum OrderSide {

    BUY("Buy order - purchase securities"),
    SELL("Sell order - sell securities");

    private final String description;

    OrderSide(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isBuy() {
        return this == BUY;
    }

    public boolean isSell() {
        return this == SELL;
    }
}
