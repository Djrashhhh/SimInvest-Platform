package com.example.MicroInvestApp.domain.enums;

public enum OrderStatus {
    PENDING("Order has been placed and is waiting to be processed"),
    PARTIALLY_FILLED("Order has been partially executed"),
    FILLED("Order has been completely executed"),
    CANCELLED("Order has been cancelled by user or system"),
    REJECTED("Order was rejected by the system"),
    EXPIRED("Order has expired"),
    FAILED("Order execution failed");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == PENDING || this == PARTIALLY_FILLED;
    }

    public boolean isFinal() {
        return this == FILLED || this == CANCELLED || this == REJECTED || this == EXPIRED || this == FAILED;
    }
}
