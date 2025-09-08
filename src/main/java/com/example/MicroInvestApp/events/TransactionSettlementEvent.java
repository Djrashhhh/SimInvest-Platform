package com.example.MicroInvestApp.events;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Event triggered when a transaction needs to be scheduled for settlement
 */
public class TransactionSettlementEvent extends ApplicationEvent {

    private final Long transactionId;
    private final LocalDateTime settlementDateTime;

    public TransactionSettlementEvent(Object source, Long transactionId, LocalDateTime settlementDateTime) {
        super(source);
        this.transactionId = transactionId;
        this.settlementDateTime = settlementDateTime;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getSettlementDateTime() {
        return settlementDateTime;
    }
}