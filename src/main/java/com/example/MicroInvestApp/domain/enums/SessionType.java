package com.example.MicroInvestApp.domain.enums;

public enum SessionType {
    REGULAR("Regular Session"),
    PRE_MARKET("Pre-Market"),
    AFTER_HOURS("After-Hours"),
    EXTENDED_HOURS("Extended Hours"),
    HOLIDAY("Holiday Session"),
    WEEKEND("Weekend"),
    HALF_DAY("Half-Day Session");

    private final String description;

    SessionType(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
}
