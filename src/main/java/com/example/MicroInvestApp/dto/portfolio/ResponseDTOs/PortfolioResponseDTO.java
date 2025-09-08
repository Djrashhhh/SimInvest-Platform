package com.example.MicroInvestApp.dto.portfolio.ResponseDTOs;

import com.fasterxml.jackson.annotation.JsonProperty;


import java.math.BigDecimal;
import java.time.Instant;


// Response DTO for portfolio information
public class PortfolioResponseDTO {

    @JsonProperty("portfolio_id")
    private Long portfolioId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("portfolio_name")
    private String portfolioName;

    @JsonProperty("total_value")
    private BigDecimal totalValue;

    @JsonProperty("cash_balance")
    private BigDecimal cashBalance;

    @JsonProperty("created_date")
    private Instant createdDate;

    @JsonProperty("last_updated")
    private Instant lastUpdated;

    @JsonProperty("is_active")
    private boolean isActive;

    @JsonProperty("position_count")
    private Long positionCount;

    @JsonProperty("total_unrealized_gain_loss")
    private BigDecimal totalUnrealizedGainLoss;

    @JsonProperty("total_realized_gain_loss")
    private BigDecimal totalRealizedGainLoss;

    // Constructors
    public PortfolioResponseDTO() {}

    public PortfolioResponseDTO(Long portfolioId, Long userId, String portfolioName,
                             BigDecimal totalValue, BigDecimal cashBalance,
                             Instant createdDate, Instant lastUpdated, boolean isActive) {
        this.portfolioId = portfolioId;
        this.userId = userId;
        this.portfolioName = portfolioName;
        this.totalValue = totalValue;
        this.cashBalance = cashBalance;
        this.createdDate = createdDate;
        this.lastUpdated = lastUpdated;
        this.isActive = isActive;
    }

    // Getters and Setters
    public Long getPortfolioId() { return portfolioId; }
    public void setPortfolioId(Long portfolioId) { this.portfolioId = portfolioId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getPortfolioName() { return portfolioName; }
    public void setPortfolioName(String portfolioName) { this.portfolioName = portfolioName; }

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }

    public BigDecimal getCashBalance() { return cashBalance; }
    public void setCashBalance(BigDecimal cashBalance) { this.cashBalance = cashBalance; }

    public Instant getCreatedDate() { return createdDate; }
    public void setCreatedDate(Instant createdDate) { this.createdDate = createdDate; }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Long getPositionCount() { return positionCount; }
    public void setPositionCount(Long positionCount) { this.positionCount = positionCount; }

    public BigDecimal getTotalUnrealizedGainLoss() { return totalUnrealizedGainLoss; }
    public void setTotalUnrealizedGainLoss(BigDecimal totalUnrealizedGainLoss) {
        this.totalUnrealizedGainLoss = totalUnrealizedGainLoss;
    }

    public BigDecimal getTotalRealizedGainLoss() { return totalRealizedGainLoss; }
    public void setTotalRealizedGainLoss(BigDecimal totalRealizedGainLoss) {
        this.totalRealizedGainLoss = totalRealizedGainLoss;
    }
}
