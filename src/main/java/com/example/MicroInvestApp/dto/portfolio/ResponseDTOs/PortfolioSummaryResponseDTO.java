package com.example.MicroInvestApp.dto.portfolio.ResponseDTOs;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

// Summary DTO for portfolio overview
public class PortfolioSummaryResponseDTO {

    @JsonProperty("portfolio_id")
    private Long portfolioId;

    @JsonProperty("portfolio_name")
    private String portfolioName;

    @JsonProperty("total_value")
    private BigDecimal totalValue;

    @JsonProperty("cash_balance")
    private BigDecimal cashBalance;

    @JsonProperty("invested_amount")
    private BigDecimal investedAmount;

    @JsonProperty("total_gain_loss")
    private BigDecimal totalGainLoss;

    @JsonProperty("total_gain_loss_percentage")
    private BigDecimal totalGainLossPercentage;

    @JsonProperty("position_count")
    private Long positionCount;

    @JsonProperty("last_updated")
    private Instant lastUpdated;

    // Constructors
    public PortfolioSummaryResponseDTO() {}

    public PortfolioSummaryResponseDTO(Long portfolioId, String portfolioName, BigDecimal totalValue,
                                    BigDecimal cashBalance, BigDecimal investedAmount, BigDecimal totalGainLoss) {
        this.portfolioId = portfolioId;
        this.portfolioName = portfolioName;
        this.totalValue = totalValue;
        this.cashBalance = cashBalance;
        this.investedAmount = investedAmount;
        this.totalGainLoss = totalGainLoss;
        this.calculateGainLossPercentage();
    }

    // Business logic method
    private void calculateGainLossPercentage() {
        if (investedAmount != null && investedAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.totalGainLossPercentage = totalGainLoss
                    .divide(investedAmount, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.totalGainLossPercentage = BigDecimal.ZERO;
        }
    }

    // Getters and Setters
    public Long getPortfolioId() { return portfolioId; }
    public void setPortfolioId(Long portfolioId) { this.portfolioId = portfolioId; }

    public String getPortfolioName() { return portfolioName; }
    public void setPortfolioName(String portfolioName) { this.portfolioName = portfolioName; }

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }

    public BigDecimal getCashBalance() { return cashBalance; }
    public void setCashBalance(BigDecimal cashBalance) { this.cashBalance = cashBalance; }

    public BigDecimal getInvestedAmount() { return investedAmount; }
    public void setInvestedAmount(BigDecimal investedAmount) {
        this.investedAmount = investedAmount;
        this.calculateGainLossPercentage();
    }

    public BigDecimal getTotalGainLoss() { return totalGainLoss; }
    public void setTotalGainLoss(BigDecimal totalGainLoss) {
        this.totalGainLoss = totalGainLoss;
        this.calculateGainLossPercentage();
    }

    public BigDecimal getTotalGainLossPercentage() { return totalGainLossPercentage; }
    public void setTotalGainLossPercentage(BigDecimal totalGainLossPercentage) {
        this.totalGainLossPercentage = totalGainLossPercentage;
    }

    public Long getPositionCount() { return positionCount; }
    public void setPositionCount(Long positionCount) { this.positionCount = positionCount; }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
}
