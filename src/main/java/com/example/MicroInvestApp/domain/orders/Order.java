package com.example.MicroInvestApp.domain.orders;

import com.example.MicroInvestApp.domain.enums.OrderStatus;
import com.example.MicroInvestApp.domain.enums.OrderType;
import com.example.MicroInvestApp.domain.enums.OrderSide;
import com.example.MicroInvestApp.domain.portfolio.Portfolio;
import com.example.MicroInvestApp.domain.market.SecurityStock;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
@Table(name = "Orders", indexes = {
        @Index(name = "idx_order_status", columnList = "order_status"),
        @Index(name = "idx_order_side", columnList = "order_side"),
        @Index(name = "idx_order_portfolio", columnList = "portfolio_id"),
        @Index(name = "idx_order_security", columnList = "security_id"),
        @Index(name = "idx_order_placed_date", columnList = "order_placed_date"),
        @Index(name = "idx_order_user_status", columnList = "portfolio_id,order_status"),
        @Index(name = "idx_order_symbol_date", columnList = "security_id,order_placed_date")
})
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Order implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "security_id", nullable = false)
    private SecurityStock securityStock;

    @Positive(message = "Quantity must be positive")
    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @Positive(message = "Order price must be positive")
    @Digits(integer = 10, fraction = 4, message = "Invalid price format")
    @Column(name = "order_price", precision = 19, scale = 4)
    private BigDecimal orderPrice; // Limit price for LIMIT orders, null for MARKET orders

    @Positive(message = "Estimated total must be positive")
    @Digits(integer = 15, fraction = 2, message = "Invalid total format")
    @Column(name = "estimated_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal estimatedTotal; // Estimated total amount

    @PositiveOrZero(message = "Filled quantity cannot be negative")
    @Column(name = "filled_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal filledQuantity = BigDecimal.ZERO; // Quantity that has been filled

    @PositiveOrZero(message = "Average fill price cannot be negative")
    @Digits(integer = 10, fraction = 4, message = "Invalid price format")
    @Column(name = "average_fill_price", precision = 19, scale = 4)
    private BigDecimal averageFillPrice; // Average price of filled portions

    @PositiveOrZero(message = "Total fees cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Invalid fees format")
    @Column(name = "total_fees", precision = 19, scale = 2, columnDefinition = "DECIMAL(19,2) DEFAULT 0.00")
    private BigDecimal totalFees = BigDecimal.ZERO; // Total fees charged

    @NotNull(message = "Order type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    @NotNull(message = "Order side cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "order_side", nullable = false)
    private OrderSide orderSide; // BUY or SELL

    @NotNull(message = "Order status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "order_placed_date", nullable = false)
    private LocalDateTime orderPlacedDate;

    @Column(name = "order_executed_date")
    private LocalDateTime orderExecutedDate;

    @Column(name = "order_cancelled_date")
    private LocalDateTime orderCancelledDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate; // For GTC (Good Till Cancelled) orders

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(name = "notes", length = 500)
    private String notes;

    @Size(max = 255, message = "Cancellation reason cannot exceed 255 characters")
    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    // Relationship with transactions
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private java.util.List<Transaction> transactions;

    public Order() {
        this.orderPlacedDate = LocalDateTime.now();
        this.orderStatus = OrderStatus.PENDING;
    }

    public Order(Portfolio portfolio, SecurityStock securityStock, BigDecimal quantity,
                 BigDecimal orderPrice, OrderType orderType, OrderSide orderSide, String notes) {
        this();
        this.portfolio = portfolio;
        this.securityStock = securityStock;
        this.quantity = quantity;
        this.orderPrice = orderPrice;
        this.orderType = orderType;
        this.orderSide = orderSide;
        this.notes = notes;

        // Calculate estimated total based on order type and side
        calculateEstimatedTotal();
    }

    // Business Methods
    public boolean isFullyFilled() {
        return filledQuantity != null && quantity.compareTo(filledQuantity) <= 0;
    }

    public boolean isPartiallyFilled() {
        return filledQuantity != null && filledQuantity.compareTo(BigDecimal.ZERO) > 0
                && quantity.compareTo(filledQuantity) > 0;
    }

    public BigDecimal getRemainingQuantity() {
        if (filledQuantity == null) {
            return quantity;
        }
        return quantity.subtract(filledQuantity);
    }

    public boolean canBeCancelled() {
        return orderStatus == OrderStatus.PENDING || orderStatus == OrderStatus.PARTIALLY_FILLED;
    }

    public boolean isActive() {
        return orderStatus == OrderStatus.PENDING || orderStatus == OrderStatus.PARTIALLY_FILLED;
    }

    public boolean isBuyOrder() {
        return OrderSide.BUY.equals(this.orderSide);
    }

    public boolean isSellOrder() {
        return OrderSide.SELL.equals(this.orderSide);
    }

    public void markAsExecuted() {
        this.orderStatus = OrderStatus.FILLED;
        this.orderExecutedDate = LocalDateTime.now();
    }

    public void markAsPartiallyFilled() {
        this.orderStatus = OrderStatus.PARTIALLY_FILLED;
    }

    public void markAsCancelled(String reason) {
        this.orderStatus = OrderStatus.CANCELLED;
        this.orderCancelledDate = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    public void markAsFailed(String reason) {
        this.orderStatus = OrderStatus.FAILED;
        this.cancellationReason = reason;
    }

    private void calculateEstimatedTotal() {
        if (securityStock == null) return;

        BigDecimal priceToUse;
        if (orderType.equals(OrderType.MARKET)) {
            priceToUse = securityStock.getCurrentPrice();
        } else {
            priceToUse = orderPrice;
        }

        if (priceToUse != null && quantity != null) {
            this.estimatedTotal = priceToUse.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
        }
    }

    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Portfolio getPortfolio() { return portfolio; }
    public void setPortfolio(Portfolio portfolio) { this.portfolio = portfolio; }

    public SecurityStock getSecurityStock() { return securityStock; }
    public void setSecurityStock(SecurityStock securityStock) { this.securityStock = securityStock; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getOrderPrice() { return orderPrice; }
    public void setOrderPrice(BigDecimal orderPrice) { this.orderPrice = orderPrice; }

    public BigDecimal getEstimatedTotal() { return estimatedTotal; }
    public void setEstimatedTotal(BigDecimal estimatedTotal) { this.estimatedTotal = estimatedTotal; }

    public BigDecimal getFilledQuantity() { return filledQuantity; }
    public void setFilledQuantity(BigDecimal filledQuantity) { this.filledQuantity = filledQuantity; }

    public BigDecimal getAverageFillPrice() { return averageFillPrice; }
    public void setAverageFillPrice(BigDecimal averageFillPrice) { this.averageFillPrice = averageFillPrice; }

    public BigDecimal getTotalFees() { return totalFees; }
    public void setTotalFees(BigDecimal totalFees) { this.totalFees = totalFees; }

    public OrderType getOrderType() { return orderType; }
    public void setOrderType(OrderType orderType) { this.orderType = orderType; }

    public OrderSide getOrderSide() { return orderSide; }
    public void setOrderSide(OrderSide orderSide) { this.orderSide = orderSide; }

    public OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }

    public LocalDateTime getOrderPlacedDate() { return orderPlacedDate; }
    public void setOrderPlacedDate(LocalDateTime orderPlacedDate) { this.orderPlacedDate = orderPlacedDate; }

    public LocalDateTime getOrderExecutedDate() { return orderExecutedDate; }
    public void setOrderExecutedDate(LocalDateTime orderExecutedDate) { this.orderExecutedDate = orderExecutedDate; }

    public LocalDateTime getOrderCancelledDate() { return orderCancelledDate; }
    public void setOrderCancelledDate(LocalDateTime orderCancelledDate) { this.orderCancelledDate = orderCancelledDate; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }

    public java.util.List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(java.util.List<Transaction> transactions) { this.transactions = transactions; }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", portfolio=" + (portfolio != null ? portfolio.getPortfolioId() : null) +
                ", securityStock=" + (securityStock != null ? securityStock.getSymbol() : null) +
                ", quantity=" + quantity +
                ", orderPrice=" + orderPrice +
                ", orderSide=" + orderSide +
                ", estimatedTotal=" + estimatedTotal +
                ", filledQuantity=" + filledQuantity +
                ", averageFillPrice=" + averageFillPrice +
                ", orderType=" + orderType +
                ", orderStatus=" + orderStatus +
                ", orderPlacedDate=" + orderPlacedDate +
                ", createdAt=" + createdAt +
                '}';
    }

    // Add to Order.java
    @PrePersist
    @PreUpdate
    private void validateOrderState() {
        if (orderType == OrderType.LIMIT && orderPrice == null) {
            throw new IllegalArgumentException("Limit orders must have an order price");
        }
        if (orderType == OrderType.MARKET && orderPrice != null) {
            throw new IllegalArgumentException("Market orders cannot have a fixed price");
        }
        if (filledQuantity != null && filledQuantity.compareTo(quantity) > 0) {
            throw new IllegalArgumentException("Filled quantity cannot exceed order quantity");
        }
    }
}

