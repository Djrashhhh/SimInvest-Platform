package com.example.MicroInvestApp.domain.market;

import com.example.MicroInvestApp.domain.enums.IndicatorType;
import com.example.MicroInvestApp.domain.enums.Timeframe;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;


@SuppressWarnings({ "serial", "deprecation",  })
@Entity
@Table(name = "MarketIndicator")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })

public class MarketIndicator implements Serializable {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long indicatorId; // Unique identifier for the market indicator

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "securityId", nullable = false)
    private SecurityStock securityStock; // Reference to the associated SecurityStock entity

    @NotNull(message = "Indicator type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "indicator_type", nullable = false)
    private IndicatorType indicatorType; // Type of the market indicator (e.g., RSI, MACD, Bollinger Bands)

    @NotNull(message = "Indicator value cannot be null")
    @Column(name = "indicator_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal indicatorValue; // Value of the market indicator

    @NotNull(message = "Calculation date cannot be null")
    @Column(name = "calculation_date", nullable = false)
    private LocalDate calculationDate; // Date when the indicator was calculated

    @UpdateTimestamp
    @NotNull(message = "Last updated timestamp cannot be null")
    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated; // Timestamp of the last update to the market indicator

    @NotNull(message = "Timeframe cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "timeframe", nullable = false)
    private Timeframe timeframe; // Timeframe for the indicator (e.g., daily, weekly, monthly)

    public MarketIndicator() {
        // Default constructor
    }

    //Getters and Setters
    public Long getIndicatorId() {
        return indicatorId;
    }
    public void setIndicatorId(Long indicatorId) {
        this.indicatorId = indicatorId;
    }
    public SecurityStock getSecurityStock() {
        return securityStock;
    }
    public void setSecurityStock(SecurityStock securityStock) {
        this.securityStock = securityStock;
    }
    public IndicatorType getIndicatorType() {
        return indicatorType;
    }
    public void setIndicatorType(IndicatorType indicatorType) {
        this.indicatorType = indicatorType;
    }
    public BigDecimal getIndicatorValue() {
        return indicatorValue;
    }
    public void setIndicatorValue(BigDecimal indicatorValue) {
        this.indicatorValue = indicatorValue;
    }
    public LocalDate getCalculationDate() {
        return calculationDate;
    }
    public void setCalculationDate(LocalDate calculationDate) {
        this.calculationDate = calculationDate;
    }
    public Instant getLastUpdated() {
        return lastUpdated;
    }
    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    public Timeframe getTimeframe() {
        return timeframe;
    }
    public void setTimeframe(Timeframe timeframe) {
        this.timeframe = timeframe;
    }

    @Override
    public String toString() {
        return "MarketIndicator{" +
                "indicatorId=" + indicatorId +
                ", securityStock=" + securityStock +
                ", indicatorType=" + indicatorType +
                ", indicatorValue=" + indicatorValue +
                ", calculationDate=" + calculationDate +
                ", lastUpdated=" + lastUpdated +
                ", timeframe=" + timeframe +
                '}';
    }





}
