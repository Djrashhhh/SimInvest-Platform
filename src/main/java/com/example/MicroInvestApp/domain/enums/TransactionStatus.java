package com.example.MicroInvestApp.domain.enums;

public enum TransactionStatus {
    PENDING("Transaction pending"),    // Transaction is pending and not yet completed
    COMPLETED("Transaction complete"),  // Transaction has been successfully completed
    FAILED("Transaction failed"),     // Transaction failed due to an error or insufficient funds
    CANCELED("Transaction canceled, Try again later.");   // Transaction was canceled by the user or system

    // Additional methods or properties can be added if needed
    private final String description;

    TransactionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == CANCELED;
    }
}
