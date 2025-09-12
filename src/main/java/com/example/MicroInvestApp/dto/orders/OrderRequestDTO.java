package com.example.MicroInvestApp.dto.orders;

import com.example.MicroInvestApp.domain.enums.OrderSide;
import com.example.MicroInvestApp.domain.enums.OrderType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderRequestDTO {

@NotNull(message = "Portfolio ID is required")
    @JsonProperty("portfolio_id")
    private Long portfolioId;

    @NotBlank(message = "Stock symbol is required")
    @JsonProperty("stock_symbol")
    private String stockSymbol;

    @NotNull(message = "Quantity is required")
@Positive(message = "Quantity must be a positive number")
    @JsonProperty("quantity")
    private BigDecimal quantity;


    @JsonProperty("order_price")
    private BigDecimal orderPrice; // Optional for MARKET orders

   @NotNull(message = "Order type is required (MARKET, LIMIT, STOP)")
    @JsonProperty("order_type")
    private OrderType orderType;

    @NotNull(message = "Order side is required (BUY or SELL)")
    @JsonProperty("order_side")
    private OrderSide orderSide;

    @JsonProperty("expiry_date")
    private LocalDateTime expiryDate; // Optional for GTC orders


    @JsonProperty("notes")
    private String notes;

    // Constructors
    public OrderRequestDTO() {}

    public OrderRequestDTO(Long portfolioId, String stockSymbol, BigDecimal quantity,
                           BigDecimal orderPrice, OrderType orderType, OrderSide orderSide) {
        this.portfolioId = portfolioId;
        this.stockSymbol = stockSymbol;
        this.quantity = quantity;
        this.orderPrice = orderPrice;
        this.orderType = orderType;
        this.orderSide = orderSide;
    }

    // Getters and Setters
    public Long getPortfolioId() { return portfolioId; }
    public void setPortfolioId(Long portfolioId) { this.portfolioId = portfolioId; }

    public String getStockSymbol() { return stockSymbol; }
    public void setStockSymbol(String stockSymbol) { this.stockSymbol = stockSymbol; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getOrderPrice() { return orderPrice; }
    public void setOrderPrice(BigDecimal orderPrice) { this.orderPrice = orderPrice; }

    public OrderType getOrderType() { return orderType; }
    public void setOrderType(OrderType orderType) { this.orderType = orderType; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public OrderSide getOrderSide() { return orderSide; }
    public void setOrderSide(OrderSide orderSide) { this.orderSide = orderSide;}

    // Helper methods
    public boolean isBuyOrder() {
        return OrderSide.BUY.equals(this.orderSide);
    }

    public boolean isSellOrder() {
        return OrderSide.SELL.equals(this.orderSide);
    }

    @AssertTrue(message = "Limit orders must specify order price")
    private boolean isValidLimitOrder() {
        return orderType != OrderType.LIMIT || orderPrice != null;
    }

    @AssertTrue(message = "Market orders cannot specify order price")
    private boolean isValidMarketOrder() {
        return orderType != OrderType.MARKET || orderPrice == null;
    }
}
