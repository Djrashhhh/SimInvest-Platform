package com.example.MicroInvestApp.domain.portfolio;

import com.example.MicroInvestApp.domain.market.SecurityStock;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "Position", indexes = {
        @Index(name = "idx_position_portfolio", columnList = "portfolio_id"),
        @Index(name = "idx_position_security", columnList = "security_id"),
        @Index(name = "idx_position_portfolio_security", columnList = "portfolio_id,security_id", unique = true),
        @Index(name = "idx_position_active", columnList = "portfolio_id,is_active")
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Position implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long positionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    @JsonIgnore
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "security_id", nullable = false)
    private SecurityStock securityStock;

    @PositiveOrZero(message = "Quantity cannot be negative")
    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity = BigDecimal.ZERO;

    @PositiveOrZero(message = "Average cost per share cannot be negative")
    @Column(name = "avg_cost_per_share", nullable = false, precision = 19, scale = 4)
    private BigDecimal avgCostPerShare = BigDecimal.ZERO;

    @PositiveOrZero(message = "Current value cannot be negative")
    @Column(name = "current_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentValue = BigDecimal.ZERO;

    @NotNull(message = "Unrealized gain/loss cannot be null")
    @Column(name = "unrealized_gain_loss", nullable = false, precision = 19, scale = 2)
    private BigDecimal unrealizedGainLoss = BigDecimal.ZERO;

    @NotNull(message = "Realized gain/loss cannot be null")
    @Column(name = "realized_gain_loss", nullable = false, precision = 19, scale = 2)
    private BigDecimal realizedGainLoss = BigDecimal.ZERO;

    // Enhanced: Track position lifecycle
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;


    // Enhanced: Track performance metrics
    @Column(name = "day_change", precision = 19, scale = 2)
    private BigDecimal dayChange = BigDecimal.ZERO;

    @Column(name = "day_change_percent", precision = 8, scale = 4)
    private BigDecimal dayChangePercent = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant openDate;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    // Enhanced: Version for optimistic locking
    @Version
    @Column(name = "version")
    private Long version;

    // Constructors
    public Position() {
// Initialize all BigDecimal fields to prevent nulls
        this.quantity = BigDecimal.ZERO;
        this.avgCostPerShare = BigDecimal.ZERO;
        this.currentValue = BigDecimal.ZERO;
        this.unrealizedGainLoss = BigDecimal.ZERO;
        this.realizedGainLoss = BigDecimal.ZERO;
        this.dayChange = BigDecimal.ZERO;
        this.dayChangePercent = BigDecimal.ZERO;
        this.isActive = true;

    }

    public Position(Portfolio portfolio, SecurityStock securityStock, BigDecimal quantity, BigDecimal avgCostPerShare) {
        this.portfolio = portfolio;
        this.securityStock = securityStock;
        this.quantity = quantity;
        this.avgCostPerShare = avgCostPerShare;
        this.currentValue = avgCostPerShare.multiply(quantity);
        this.unrealizedGainLoss = BigDecimal.ZERO;
        this.realizedGainLoss = BigDecimal.ZERO;
        this.isActive = true;
        this.dayChange = BigDecimal.ZERO;
        this.dayChangePercent = BigDecimal.ZERO;
    }

    // Enhanced business logic methods
    public BigDecimal getCostBasis() {
        if (quantity == null || avgCostPerShare == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(avgCostPerShare);
    }

    public BigDecimal getCurrentMarketValue() {
        if (securityStock != null && securityStock.getCurrentPrice() != null && quantity.compareTo(BigDecimal.ZERO) > 0) {
            return quantity.multiply(securityStock.getCurrentPrice());
        }
        return currentValue;
    }

    /**
     * Updates current value and calculates unrealized gain/loss
     */
    public void updateCurrentValue() {
        if (quantity.compareTo(BigDecimal.ZERO) == 0) {
            this.currentValue = BigDecimal.ZERO;
            this.unrealizedGainLoss = BigDecimal.ZERO;
            this.dayChange = BigDecimal.ZERO;
            this.dayChangePercent = BigDecimal.ZERO;
            this.isActive = false;
            return;
        }

        if (securityStock != null && securityStock.getCurrentPrice() != null) {
            // Always recalculate current value from quantity * current price
            this.currentValue = quantity.multiply(securityStock.getCurrentPrice());

            // Calculate unrealized gain/loss
            BigDecimal costBasis = quantity.multiply(avgCostPerShare);
            this.unrealizedGainLoss = this.currentValue.subtract(costBasis);

            // Calculate day change using previous close
            calculateDayChangeFromSecurity();

            this.lastUpdated = Instant.now();
        }
    }

    /**
     * Calculate day change using SecurityStock's day change data
     */
    /**
     * Calculate day change using SecurityStock's day change data
     */
    public void calculateDayChangeFromSecurity() {
        if (securityStock.getCurrentPrice() != null && securityStock.getPreviousClose() != null &&
                quantity != null && quantity.compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal currentPositionValue = quantity.multiply(securityStock.getCurrentPrice());
            BigDecimal previousPositionValue = quantity.multiply(securityStock.getPreviousClose());

            this.dayChange = currentPositionValue.subtract(previousPositionValue);

            if (previousPositionValue.compareTo(BigDecimal.ZERO) > 0) {
                this.dayChangePercent = this.dayChange
                        .divide(previousPositionValue, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            } else {
                this.dayChangePercent = BigDecimal.ZERO;
            }
        } else {
            this.dayChange = BigDecimal.ZERO;
            this.dayChangePercent = BigDecimal.ZERO;
        }
    }

    /**
     * Manual day change calculation using previous closing price
     * Call this method when you have specific previous closing price data
     */
    public void calculateDayChangeFromPreviousClose(BigDecimal previousClosingPrice) {
        if (previousClosingPrice == null || previousClosingPrice.compareTo(BigDecimal.ZERO) <= 0 ||
                securityStock == null || securityStock.getCurrentPrice() == null || quantity == null) {
            this.dayChange = BigDecimal.ZERO;
            this.dayChangePercent = BigDecimal.ZERO;
            return;
        }

        BigDecimal previousValue = quantity.multiply(previousClosingPrice);
        BigDecimal currentValue = quantity.multiply(securityStock.getCurrentPrice());

        this.dayChange = currentValue.subtract(previousValue);

        if (previousValue.compareTo(BigDecimal.ZERO) > 0) {
            this.dayChangePercent = this.dayChange
                    .divide(previousValue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        } else {
            this.dayChangePercent = BigDecimal.ZERO;
        }
    }

    /**
     * Calculate the gain/loss percentage based on the cost basis
     */
    public BigDecimal getGainLossPercentage() {
        BigDecimal costBasis = getCostBasis();
        if (costBasis.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return unrealizedGainLoss.divide(costBasis, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * Enhanced: Check if position has shares
     */
    public boolean hasShares() {
        return quantity.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Enhanced: Get total gain/loss (realized + unrealized)
     */
    public BigDecimal getTotalGainLoss() {
        return realizedGainLoss.add(unrealizedGainLoss);
    }

    /**
     * Enhanced: Check if position is profitable
     */
    public boolean isProfitable() {
        return getTotalGainLoss().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Enhanced: Get position weight in portfolio (requires portfolio total value)
     */
    public BigDecimal getWeightInPortfolio(BigDecimal portfolioTotalValue) {
        if (portfolioTotalValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentValue.divide(portfolioTotalValue, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    public String getSecuritySymbol() {
        return securityStock != null ? securityStock.getSymbol() : null;
    }

    /**
     * Enhanced: Mark position as closed
     */
    public void closePosition() {
        this.isActive = false;
        this.quantity = BigDecimal.ZERO;
        this.currentValue = BigDecimal.ZERO;
        this.unrealizedGainLoss = BigDecimal.ZERO;
    }

    // Getters and Setters
    public Long getPositionId() { return positionId; }
    public void setPositionId(Long positionId) { this.positionId = positionId; }

    public Portfolio getPortfolio() { return portfolio; }
    public void setPortfolio(Portfolio portfolio) { this.portfolio = portfolio; }

    public SecurityStock getSecurityStock() { return securityStock; }
    public void setSecurityStock(SecurityStock securityStock) { this.securityStock = securityStock; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        if (quantity.compareTo(BigDecimal.ZERO) == 0) {
            this.isActive = false;
        }
    }

    public BigDecimal getAvgCostPerShare() { return avgCostPerShare; }
    public void setAvgCostPerShare(BigDecimal avgCostPerShare) { this.avgCostPerShare = avgCostPerShare; }

    public BigDecimal getCurrentValue() { return currentValue; }
    public void setCurrentValue(BigDecimal currentValue) { this.currentValue = currentValue; }

    public BigDecimal getUnrealizedGainLoss() { return unrealizedGainLoss; }
    public void setUnrealizedGainLoss(BigDecimal unrealizedGainLoss) { this.unrealizedGainLoss = unrealizedGainLoss; }

    public BigDecimal getRealizedGainLoss() { return realizedGainLoss; }
    public void setRealizedGainLoss(BigDecimal realizedGainLoss) { this.realizedGainLoss = realizedGainLoss; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }


    public BigDecimal getDayChange() { return dayChange; }
    public void setDayChange(BigDecimal dayChange) { this.dayChange = dayChange; }

    public BigDecimal getDayChangePercent() { return dayChangePercent; }
    public void setDayChangePercent(BigDecimal dayChangePercent) { this.dayChangePercent = dayChangePercent; }

    public Instant getOpenDate() { return openDate; }
    public void setOpenDate(Instant openDate) { this.openDate = openDate; }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    @PrePersist
    @PreUpdate
    private void validatePosition() {
        if (quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Position quantity cannot be negative");
        }
        if (avgCostPerShare.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Average cost per share cannot be negative");
        }


        // Auto-deactivate if quantity is zero
        if (quantity.compareTo(BigDecimal.ZERO) == 0) {
            this.isActive = false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return Objects.equals(positionId, position.positionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(positionId);
    }

    @Override
    public String toString() {
        return "Position{" +
                "positionId=" + positionId +
                ", securitySymbol=" + (securityStock != null ? securityStock.getSymbol() : "null") +
                ", quantity=" + quantity +
                ", avgCostPerShare=" + avgCostPerShare +
                ", currentValue=" + currentValue +
                ", unrealizedGainLoss=" + unrealizedGainLoss +
                ", isActive=" + isActive +
                '}';
    }
}