package com.example.MicroInvestApp.dto.orders;

import com.example.MicroInvestApp.domain.enums.OrderType;
import com.example.MicroInvestApp.domain.enums.TransactionStatus;
import com.example.MicroInvestApp.domain.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;


public class TransactionResponseDTO {

    @JsonProperty("transaction_id")
    private Long transactionId;

    @JsonProperty("portfolio_id")
    private Long portfolioId;

    @JsonProperty("portfolio_name")
    private String portfolioName;

    @JsonProperty("stock_symbol")
    private String stockSymbol;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("quantity")
    private BigDecimal quantity;

    @JsonProperty("price_per_share")
    private BigDecimal pricePerShare;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("fees")
    private BigDecimal fees;

    @JsonProperty("tax_amount")
    private BigDecimal taxAmount;

    @JsonProperty("net_amount")
    private BigDecimal netAmount;

    @JsonProperty("transaction_date")
    private LocalDateTime transactionDate;

    @JsonProperty("settlement_date")
    private LocalDateTime settlementDate;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("transaction_type")
    private TransactionType transactionType;

    @JsonProperty("order_type")
    private OrderType orderType;

    @JsonProperty("transaction_status")
    private TransactionStatus transactionStatus;


    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("last_updated")
    private Instant lastUpdated;

    // Calculated fields
    @JsonProperty("is_settled")
    private boolean isSettled;

    @JsonProperty("affects_portfolio_balance")
    private boolean affectsPortfolioBalance;

    @JsonProperty("affects_position")
    private boolean affectsPosition;

    // Constructors
    public TransactionResponseDTO() {}

    // Getters and Setters
    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public Long getPortfolioId() { return portfolioId; }
    public void setPortfolioId(Long portfolioId) { this.portfolioId = portfolioId; }

    public String getPortfolioName() { return portfolioName; }
    public void setPortfolioName(String portfolioName) { this.portfolioName = portfolioName; }

    public String getStockSymbol() { return stockSymbol; }
    public void setStockSymbol(String stockSymbol) { this.stockSymbol = stockSymbol; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getPricePerShare() { return pricePerShare; }
    public void setPricePerShare(BigDecimal pricePerShare) { this.pricePerShare = pricePerShare; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) { this.fees = fees; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public LocalDateTime getSettlementDate() { return settlementDate; }
    public void setSettlementDate(LocalDateTime settlementDate) { this.settlementDate = settlementDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public OrderType getOrderType() { return orderType; }
    public void setOrderType(OrderType orderType) { this.orderType = orderType; }

    public TransactionStatus getTransactionStatus() { return transactionStatus; }
    public void setTransactionStatus(TransactionStatus transactionStatus) { this.transactionStatus = transactionStatus; }


    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }

    public boolean isSettled() { return isSettled; }
    public void setSettled(boolean settled) { this.isSettled = settled; }

    public boolean isAffectsPortfolioBalance() { return affectsPortfolioBalance; }
    public void setAffectsPortfolioBalance(boolean affectsPortfolioBalance) { this.affectsPortfolioBalance = affectsPortfolioBalance; }

    public boolean isAffectsPosition() { return affectsPosition; }
    public void setAffectsPosition(boolean affectsPosition) { this.affectsPosition = affectsPosition; }
}
