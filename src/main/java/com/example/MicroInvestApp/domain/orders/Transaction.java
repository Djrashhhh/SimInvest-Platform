package com.example.MicroInvestApp.domain.orders;

import com.example.MicroInvestApp.domain.market.SecurityStock;
import com.example.MicroInvestApp.domain.enums.OrderType;
import com.example.MicroInvestApp.domain.enums.TransactionStatus;
import com.example.MicroInvestApp.domain.enums.TransactionType;
import com.example.MicroInvestApp.domain.portfolio.Portfolio;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;

@SuppressWarnings({"serial", "deprecation"})
@Entity
@Table(name = "Transactions", indexes = {
        @Index(name = "idx_transaction_portfolio", columnList = "portfolio_id"),
        @Index(name = "idx_transaction_security", columnList = "security_id"),
        @Index(name = "idx_transaction_order", columnList = "order_id"),
        @Index(name = "idx_transaction_type", columnList = "transaction_type"),
        @Index(name = "idx_transaction_status", columnList = "transaction_status"),
        @Index(name = "idx_transaction_date", columnList = "transaction_date")
})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

public class Transaction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "security_id", nullable = false)
    private SecurityStock securityStock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order; // Optional - not all transactions come from orders (e.g., dividends)

    @Positive(message = "Quantity must be positive")
    @Digits(integer = 15, fraction = 4, message = "Invalid quantity format")
    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Positive(message = "Price per share must be positive")
    @Digits(integer = 10, fraction = 4, message = "Invalid price format")
    @Column(name = "price_per_share", nullable = false, precision = 19, scale = 4)
    private BigDecimal pricePerShare;

    @Positive(message = "Total amount must be positive")
    @Digits(integer = 15, fraction = 2, message = "Invalid total amount format")
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @PositiveOrZero(message = "Fees cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Invalid fees format")
    @Column(name = "fees", nullable = false, precision = 19, scale = 2)
    private BigDecimal fees = BigDecimal.ZERO;

    @PositiveOrZero(message = "Tax amount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Invalid tax format")
    @Column(name = "tax_amount", precision = 19, scale = 2, columnDefinition = "DECIMAL(19,2) DEFAULT 0.00")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @NotNull(message = "Net amount cannot be null")
    @Digits(integer = 15, fraction = 2, message = "Invalid net amount format")
    @Column(name = "net_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal netAmount; // Total amount +/- fees and taxes

    @NotNull(message = "Transaction date cannot be null")
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "settlement_date")
    private LocalDateTime settlementDate; // T+2 for stocks, date when funds/shares are settled

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Column(name = "notes", length = 1000)
    private String notes;

    @NotNull(message = "Transaction type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type")
    private OrderType orderType; // Can be null for non-order transactions

    @NotNull(message = "Transaction status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status", nullable = false)
    private TransactionStatus transactionStatus;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    public Transaction() {
        this.transactionDate = LocalDateTime.now();
        this.transactionStatus = TransactionStatus.PENDING;
    }

    public Transaction(Portfolio portfolio, SecurityStock securityStock, Order order,
                       BigDecimal quantity, BigDecimal pricePerShare, TransactionType transactionType) {
        this();
        this.portfolio = portfolio;
        this.securityStock = securityStock;
        this.order = order;
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
        this.transactionType = transactionType;

        // Calculate amounts
        this.totalAmount = quantity.multiply(pricePerShare).setScale(2, RoundingMode.HALF_UP);
        this.netAmount = calculateNetAmount();

        // Set settlement date (T+2 for stocks)
        this.settlementDate = transactionDate.plusDays(2);
    }

    // Business Methods
    public BigDecimal calculateNetAmount() {
        BigDecimal net = totalAmount;

        // For BUY transactions: add fees and taxes
        // For SELL transactions: subtract fees and taxes
        if (transactionType == TransactionType.BUY) {
            net = net.add(fees).add(taxAmount);
        } else if (transactionType == TransactionType.SELL) {
            net = net.subtract(fees).subtract(taxAmount);
        }

        return net.setScale(2, RoundingMode.HALF_UP);
    }

    public void updateNetAmount() {
        this.netAmount = calculateNetAmount();
    }

    public boolean isSettled() {
        return settlementDate != null && LocalDateTime.now().isAfter(settlementDate)
                && transactionStatus == TransactionStatus.COMPLETED;
    }

    public boolean affectsPortfolioBalance() {
        return transactionType == TransactionType.BUY ||
                transactionType == TransactionType.SELL ||
                transactionType == TransactionType.DIVIDEND ||
                transactionType == TransactionType.DEPOSIT ||
                transactionType == TransactionType.WITHDRAWAL;
    }

    public boolean affectsPosition() {
        return transactionType == TransactionType.BUY ||
                transactionType == TransactionType.SELL ||
                transactionType == TransactionType.STOCK_SPLIT ||
                transactionType == TransactionType.STOCK_DIVIDEND;
    }

    public void markAsCompleted() {
        this.transactionStatus = TransactionStatus.COMPLETED;
    }

    public void markAsFailed(String reason) {
        this.transactionStatus = TransactionStatus.FAILED;
        if (reason != null) {
            this.notes = (this.notes != null ? this.notes + "; " : "") + "Failed: " + reason;
        }
    }

    public void markAsCancelled(String reason) {
        this.transactionStatus = TransactionStatus.CANCELED;
        if (reason != null) {
            this.notes = (this.notes != null ? this.notes + "; " : "") + "Cancelled: " + reason;
        }
    }

    // Getters and Setters
    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public Portfolio getPortfolio() { return portfolio; }
    public void setPortfolio(Portfolio portfolio) { this.portfolio = portfolio; }

    public SecurityStock getSecurityStock() { return securityStock; }
    public void setSecurityStock(SecurityStock securityStock) { this.securityStock = securityStock; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getPricePerShare() { return pricePerShare; }
    public void setPricePerShare(BigDecimal pricePerShare) { this.pricePerShare = pricePerShare; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) {
        this.fees = fees;
        updateNetAmount();
    }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
        updateNetAmount();
    }

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

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", portfolio=" + (portfolio != null ? portfolio.getPortfolioId() : null) +
                ", securityStock=" + (securityStock != null ? securityStock.getSymbol() : null) +
                ", order=" + (order != null ? order.getOrderId() : null) +
                ", quantity=" + quantity +
                ", pricePerShare=" + pricePerShare +
                ", totalAmount=" + totalAmount +
                ", netAmount=" + netAmount +
                ", transactionType=" + transactionType +
                ", transactionStatus=" + transactionStatus +
                ", transactionDate=" + transactionDate +
                '}';
    }
}