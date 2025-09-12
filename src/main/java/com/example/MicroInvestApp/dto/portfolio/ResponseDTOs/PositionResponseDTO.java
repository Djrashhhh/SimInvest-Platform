package com.example.MicroInvestApp.dto.portfolio.ResponseDTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Enhanced Response DTO for position information with comprehensive metrics
 */
public class PositionResponseDTO {

    @JsonProperty("position_id")
    private Long positionId;

    @JsonProperty("portfolio_id")
    private Long portfolioId;

    @JsonProperty("portfolio_name")
    private String portfolioName;

    @JsonProperty("security_id")
    private Long securityId;

    @JsonProperty("security_symbol")
    private String securitySymbol;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("quantity")
    private BigDecimal quantity;

    @JsonProperty("avg_cost_per_share")
    private BigDecimal avgCostPerShare;

    @JsonProperty("current_price")
    private BigDecimal currentPrice;

    @JsonProperty("current_value")
    private BigDecimal currentValue;

    @JsonProperty("cost_basis")
    private BigDecimal costBasis;

    @JsonProperty("unrealized_gain_loss")
    private BigDecimal unrealizedGainLoss;

    @JsonProperty("unrealized_gain_loss_percentage")
    private BigDecimal unrealizedGainLossPercentage;

    @JsonProperty("realized_gain_loss")
    private BigDecimal realizedGainLoss;

    @JsonProperty("total_gain_loss")
    private BigDecimal totalGainLoss;

    // Enhanced: Performance tracking
    @JsonProperty("day_change")
    private BigDecimal dayChange;

    @JsonProperty("day_change_percent")
    private BigDecimal dayChangePercent;

    @JsonProperty("portfolio_weight")
    private BigDecimal portfolioWeight;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("performance_status")
    private String performanceStatus;

    // Enhanced: Timestamps
    @JsonProperty("open_date")
    private Instant openDate;


    @JsonProperty("last_updated")
    private Instant lastUpdated;

    // Enhanced: Risk and analysis fields
    @JsonProperty("break_even_price")
    private BigDecimal breakEvenPrice;

    @JsonProperty("annualized_return")
    private BigDecimal annualizedReturn;

    @JsonProperty("holding_period_days")
    private Long holdingPeriodDays;

    // Constructors
    public PositionResponseDTO() {
    }

    public PositionResponseDTO(Long positionId, Long portfolioId, String portfolioName, Long securityId,
                               String securitySymbol, String companyName, BigDecimal quantity,
                               BigDecimal avgCostPerShare, BigDecimal currentPrice, BigDecimal currentValue,
                               BigDecimal unrealizedGainLoss, BigDecimal realizedGainLoss,
                               Boolean isActive, Instant openDate, Instant lastUpdated) {
        this.positionId = positionId;
        this.portfolioId = portfolioId;
        this.portfolioName = portfolioName;
        this.securityId = securityId;
        this.securitySymbol = securitySymbol;
        this.companyName = companyName;
        this.quantity = quantity;
        this.avgCostPerShare = avgCostPerShare;
        this.currentPrice = currentPrice;
        this.currentValue = currentValue;
        this.unrealizedGainLoss = unrealizedGainLoss;
        this.realizedGainLoss = realizedGainLoss;
        this.isActive = isActive;
        this.openDate = openDate;
        this.lastUpdated = lastUpdated;

        // Calculate derived values
        this.calculateDerivedValues();
    }

    /**
     * Enhanced: Calculate all derived values
     */
    public void calculateDerivedValues() {
        // Calculate cost basis first
        if (quantity != null && avgCostPerShare != null) {
            this.costBasis = quantity.multiply(avgCostPerShare);
        } else {
            this.costBasis = BigDecimal.ZERO;
        }

        // Ensure current value is consistent
        if (quantity != null && currentPrice != null) {
            this.currentValue = quantity.multiply(currentPrice);
        }

        // Calculate unrealized gain/loss: currentValue - costBasis
        if (currentValue != null && costBasis != null) {
            this.unrealizedGainLoss = currentValue.subtract(costBasis);
        } else {
            this.unrealizedGainLoss = BigDecimal.ZERO;
        }

        // Calculate total gain/loss
        if (unrealizedGainLoss != null && realizedGainLoss != null) {
            this.totalGainLoss = unrealizedGainLoss.add(realizedGainLoss);
        } else {
            this.totalGainLoss = unrealizedGainLoss != null ? unrealizedGainLoss : BigDecimal.ZERO;
        }

        // Calculate unrealized gain/loss percentage
        if (costBasis != null && costBasis.compareTo(BigDecimal.ZERO) > 0 && unrealizedGainLoss != null) {
            this.unrealizedGainLossPercentage = unrealizedGainLoss
                    .divide(costBasis, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.unrealizedGainLossPercentage = BigDecimal.ZERO;
        }

        // Calculate break-even price (this was incorrect)
        if (avgCostPerShare != null) {
            this.breakEvenPrice = avgCostPerShare; // Break-even is simply the average cost per share
        }

        // Update performance status
        updatePerformanceStatus();

        // Calculate holding period
        calculateHoldingPeriod();

        // Calculate annualized return
        calculateAnnualizedReturn();
    }

    /**
     * Enhanced: Set portfolio weight
     */
    public void setPortfolioWeightFromTotal(BigDecimal portfolioTotalValue) {
        if (portfolioTotalValue != null && portfolioTotalValue.compareTo(BigDecimal.ZERO) > 0 &&
                currentValue != null && currentValue.compareTo(BigDecimal.ZERO) >= 0) {
            this.portfolioWeight = currentValue.divide(portfolioTotalValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.portfolioWeight = BigDecimal.ZERO;
        }
    }

    /**
     * Enhanced: Calculate holding period in days
     */
    private void calculateHoldingPeriod() {
        if (openDate != null) {
            Instant now = Instant.now();
            this.holdingPeriodDays = (now.getEpochSecond() - openDate.getEpochSecond()) / (24 * 60 * 60);
        }else{
            this.holdingPeriodDays = 0L;   // Default to 0 if openDate is null
        }
    }

    /**
     * Enhanced: Calculate annualized return
     */
    private void calculateAnnualizedReturn() {
        if (holdingPeriodDays != null && holdingPeriodDays > 0 && costBasis != null &&
                costBasis.compareTo(BigDecimal.ZERO) > 0 && totalGainLoss != null) {

            double days = holdingPeriodDays.doubleValue();
            if (days >= 1) {
                // Calculate total return as a multiplier
                BigDecimal totalReturn = totalGainLoss.divide(costBasis, 6, RoundingMode.HALF_UP);
                double totalReturnDouble = totalReturn.doubleValue();

                // Use CAGR formula: (1 + totalReturn)^(365/days) - 1
                double annualizedReturnDouble = Math.pow(1 + totalReturnDouble, 365.0 / days) - 1;

                this.annualizedReturn = BigDecimal.valueOf(annualizedReturnDouble * 100)
                        .setScale(4, RoundingMode.HALF_UP);
            } else {
                this.annualizedReturn = BigDecimal.ZERO;
            }
        } else {
            this.annualizedReturn = BigDecimal.ZERO;
        }
    }

    /**
     * Enhanced: Update performance status with more granular categories
     */
    private void updatePerformanceStatus() {
        if (totalGainLoss == null) {
            performanceStatus = "NEUTRAL";
            return;
        }

        int comparison = totalGainLoss.compareTo(BigDecimal.ZERO);
        if (comparison > 0) {
            // Check for significant gains
            if (unrealizedGainLossPercentage != null &&
                    unrealizedGainLossPercentage.compareTo(BigDecimal.valueOf(10)) > 0) {
                performanceStatus = "STRONG_GAIN";
            } else {
                performanceStatus = "GAIN";
            }
        } else if (comparison < 0) {
            // Check for significant losses
            if (unrealizedGainLossPercentage != null &&
                    unrealizedGainLossPercentage.compareTo(BigDecimal.valueOf(-10)) < 0) {
                performanceStatus = "SIGNIFICANT_LOSS";
            } else {
                performanceStatus = "LOSS";
            }
        } else {
            performanceStatus = "NEUTRAL";
        }
    }

    /**
     * Enhanced: Check if position is considered long-term (>1 year)
     */
    public boolean isLongTermHolding() {
        return holdingPeriodDays != null && holdingPeriodDays >= 365;
    }

    // All getters and setters
    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public Long getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(Long portfolioId) {
        this.portfolioId = portfolioId;
    }

    public String getPortfolioName() {
        return portfolioName;
    }

    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
    }

    public Long getSecurityId() {
        return securityId;
    }

    public void setSecurityId(Long securityId) {
        this.securityId = securityId;
    }

    public String getSecuritySymbol() {
        return securitySymbol;
    }

    public void setSecuritySymbol(String securitySymbol) {
        this.securitySymbol = securitySymbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        this.calculateDerivedValues();
    }

    public BigDecimal getAvgCostPerShare() {
        return avgCostPerShare;
    }

    public void setAvgCostPerShare(BigDecimal avgCostPerShare) {
        this.avgCostPerShare = avgCostPerShare;
        this.calculateDerivedValues();
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public BigDecimal getCostBasis() {
        return costBasis;
    }

    public void setCostBasis(BigDecimal costBasis) {
        this.costBasis = costBasis;
    }

    public BigDecimal getUnrealizedGainLoss() {
        return unrealizedGainLoss;
    }

    public void setUnrealizedGainLoss(BigDecimal unrealizedGainLoss) {
        this.unrealizedGainLoss = unrealizedGainLoss;
        this.calculateDerivedValues();
    }

    public BigDecimal getUnrealizedGainLossPercentage() {
        return unrealizedGainLossPercentage;
    }

    public void setUnrealizedGainLossPercentage(BigDecimal unrealizedGainLossPercentage) {
        this.unrealizedGainLossPercentage = unrealizedGainLossPercentage;
    }

    public BigDecimal getRealizedGainLoss() {
        return realizedGainLoss;
    }

    public void setRealizedGainLoss(BigDecimal realizedGainLoss) {
        this.realizedGainLoss = realizedGainLoss;
    }

    public BigDecimal getTotalGainLoss() {
        return totalGainLoss;
    }

    public void setTotalGainLoss(BigDecimal totalGainLoss) {
        this.totalGainLoss = totalGainLoss;
    }

    public BigDecimal getDayChange() {
        return dayChange;
    }

    public void setDayChange(BigDecimal dayChange) {
        this.dayChange = dayChange;
    }

    public BigDecimal getDayChangePercent() {
        return dayChangePercent;
    }

    public void setDayChangePercent(BigDecimal dayChangePercent) {
        this.dayChangePercent = dayChangePercent;
    }

    public BigDecimal getPortfolioWeight() {
        return portfolioWeight;
    }

    public void setPortfolioWeight(BigDecimal portfolioWeight) {
        this.portfolioWeight = portfolioWeight;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getPerformanceStatus() {
        return performanceStatus;
    }

    public void setPerformanceStatus(String performanceStatus) {
        this.performanceStatus = performanceStatus;
    }

    public Instant getOpenDate() {
        return openDate;
    }

    public void setOpenDate(Instant openDate) {
        this.openDate = openDate;
    }


    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public BigDecimal getBreakEvenPrice() {
        return breakEvenPrice;
    }

    public void setBreakEvenPrice(BigDecimal breakEvenPrice) {
        this.breakEvenPrice = breakEvenPrice;
    }

    public BigDecimal getAnnualizedReturn() {
        return annualizedReturn;
    }

    public void setAnnualizedReturn(BigDecimal annualizedReturn) {
        this.annualizedReturn = annualizedReturn;
    }

    public Long getHoldingPeriodDays() {
        return holdingPeriodDays;
    }

    public void setHoldingPeriodDays(Long holdingPeriodDays) {
        this.holdingPeriodDays = holdingPeriodDays;
    }

    @Override
    public String toString() {
        return "PositionResponseDTO{" +
                "positionId=" + positionId +
                ", portfolioId=" + portfolioId +
                ", securitySymbol='" + securitySymbol + '\'' +
                ", quantity=" + quantity +
                ", currentValue=" + currentValue +
                ", totalGainLoss=" + totalGainLoss +
                ", performanceStatus='" + performanceStatus + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}