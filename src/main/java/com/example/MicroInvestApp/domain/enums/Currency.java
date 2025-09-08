package com.example.MicroInvestApp.domain.enums;

public enum Currency {
    USD("US Dollar", "$");

    private final String displayName; // The display name of the currency
    private final String symbol;       // The symbol of the currency

    Currency(String displayName, String symbol) {
        this.displayName = displayName;
        this.symbol = symbol;
    }

    public String getDisplayName() {
        return displayName;
    }
    public String getSymbol() {
        return symbol;
    }

}
