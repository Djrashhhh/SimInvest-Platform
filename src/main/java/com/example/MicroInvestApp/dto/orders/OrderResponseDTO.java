package com.example.MicroInvestApp.dto.orders;

import com.example.MicroInvestApp.domain.enums.OrderStatus;
import com.example.MicroInvestApp.domain.enums.OrderType;
import com.example.MicroInvestApp.domain.enums.OrderSide;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public class OrderResponseDTO {

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("portfolio_id")
    private Long portfolioId;

    @JsonProperty("portfolio_name")
    private String portfolioName;

    @JsonProperty("stock_symbol")
    private String stockSymbol;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("quantity")
    private BigDecimal quantity;

    @JsonProperty("order_price")
    private BigDecimal orderPrice;

    @JsonProperty("estimated_total")
    private BigDecimal estimatedTotal;

    @JsonProperty("filled_quantity")
    private BigDecimal filledQuantity;

    @JsonProperty("average_fill_price")
    private BigDecimal averageFillPrice;

    @JsonProperty("total_fees")
    private BigDecimal totalFees;

    @JsonProperty("order_type")
    private OrderType orderType;

    @JsonProperty("order_side")
    private OrderSide orderSide;

    @JsonProperty("order_status")
    private OrderStatus orderStatus;

    @JsonProperty("order_placed_date")
    private LocalDateTime orderPlacedDate;

    @JsonProperty("order_executed_date")
    private LocalDateTime orderExecutedDate;

    @JsonProperty("order_cancelled_date")
    private LocalDateTime orderCancelledDate;

    @JsonProperty("expiry_date")
    private LocalDateTime expiryDate;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("cancellation_reason")
    private String cancellationReason;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("last_updated")
    private Instant lastUpdated;

    // Calculated fields
    @JsonProperty("remaining_quantity")
    private BigDecimal remainingQuantity;

    @JsonProperty("can_be_cancelled")
    private boolean canBeCancelled;

    @JsonProperty("is_fully_filled")
    private boolean isFullyFilled;

    @JsonProperty("is_partially_filled")
    private boolean isPartiallyFilled;

    @JsonProperty("is_buy_order")
    private boolean isBuyOrder;

    @JsonProperty("is_sell_order")
    private boolean isSellOrder;

    // Constructors
    public OrderResponseDTO() {}

    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getPortfolioId() { return portfolioId; }
    public void setPortfolioId(Long portfolioId) { this.portfolioId = portfolioId; }

    public String getPortfolioName() { return portfolioName; }
    public void setPortfolioName(String portfolioName) { this.portfolioName = portfolioName; }

    public String getStockSymbol() { return stockSymbol; }
    public void setStockSymbol(String stockSymbol) { this.stockSymbol = stockSymbol; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

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

    public BigDecimal getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(BigDecimal remainingQuantity) { this.remainingQuantity = remainingQuantity; }

    public boolean isCanBeCancelled() { return canBeCancelled; }
    public void setCanBeCancelled(boolean canBeCancelled) { this.canBeCancelled = canBeCancelled; }

    public boolean isFullyFilled() { return isFullyFilled; }
    public void setFullyFilled(boolean fullyFilled) { this.isFullyFilled = fullyFilled; }

    public boolean isPartiallyFilled() { return isPartiallyFilled; }
    public void setPartiallyFilled(boolean partiallyFilled) { this.isPartiallyFilled = partiallyFilled; }

    public boolean isBuyOrder() { return isBuyOrder; }
    public void setBuyOrder(boolean buyOrder) { this.isBuyOrder = buyOrder; }

    public boolean isSellOrder() { return isSellOrder; }
    public void setSellOrder(boolean sellOrder) { this.isSellOrder = sellOrder; }
}