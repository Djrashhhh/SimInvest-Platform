package com.example.MicroInvestApp.domain.enums;

public enum TransactionType {
    BUY("Purchase of securities"),
    SELL("Sale of securities"),
    DIVIDEND("Dividend payment received"),
    INTEREST("Interest payment received"),
    DEPOSIT("Cash deposit to portfolio"),
    WITHDRAWAL("Cash withdrawal from portfolio"),
    FEE("Fee charged"),
    TAX("Tax payment"),
    STOCK_SPLIT("Stock split adjustment"),
    STOCK_DIVIDEND("Stock dividend received"),
    SPIN_OFF("Corporate spin-off"),
    MERGER("Corporate merger"),
    TRANSFER_IN("Securities transferred in"),
    TRANSFER_OUT("Securities transferred out");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean increasesPosition() {
        return this == BUY || this == STOCK_DIVIDEND || this == STOCK_SPLIT || this == TRANSFER_IN;
    }

    public boolean decreasesPosition() {
        return this == SELL || this == TRANSFER_OUT;
    }

    public boolean affectsCashBalance() {
        return this == BUY || this == SELL || this == DIVIDEND || this == INTEREST ||
                this == DEPOSIT || this == WITHDRAWAL || this == FEE || this == TAX;
    }

    public boolean isPositiveCashFlow() {
        return this == SELL || this == DIVIDEND || this == INTEREST || this == DEPOSIT;
    }

    public boolean isNegativeCashFlow() {
        return this == BUY || this == WITHDRAWAL || this == FEE || this == TAX;
    }

    public boolean affectsPosition() {
        return this == BUY || this == SELL || this == STOCK_SPLIT ||
                this == STOCK_DIVIDEND || this == TRANSFER_IN || this == TRANSFER_OUT;
    }
}
