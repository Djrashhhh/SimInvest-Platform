package com.example.MicroInvestApp.dto.orders;

import com.example.MicroInvestApp.domain.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class TransactionRequestDTO {


    @JsonProperty("portfolio_id")
    private Long portfolioId;

    @JsonProperty("stock_symbol")
    private String stockSymbol;

    @JsonProperty("quantity")
    private BigDecimal quantity;


    @JsonProperty("price_per_share")
    private BigDecimal pricePerShare;


    @JsonProperty("transaction_type")
    private TransactionType transactionType;


    @JsonProperty("fees")
    private BigDecimal fees = BigDecimal.ZERO;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("order_id")
    private Long orderId;

    // Constructors
    public TransactionRequestDTO() {}

    // Getters and Setters
    public Long getPortfolioId() { return portfolioId; }
    public void setPortfolioId(Long portfolioId) { this.portfolioId = portfolioId; }

    public String getStockSymbol() { return stockSymbol; }
    public void setStockSymbol(String stockSymbol) { this.stockSymbol = stockSymbol; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getPricePerShare() { return pricePerShare; }
    public void setPricePerShare(BigDecimal pricePerShare) { this.pricePerShare = pricePerShare; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) { this.fees = fees; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId;}
}
