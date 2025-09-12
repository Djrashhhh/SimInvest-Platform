package com.example.MicroInvestApp.domain.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;


@SuppressWarnings({ "serial", "deprecation",  })
@Entity
@Table(name = "MarketData")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })

public class MarketData implements Serializable {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long marketDataId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "security_id", nullable = false)
    private SecurityStock securityStock; // Reference to the associated SecurityStock entity

    @Positive(message = "Open Price must be a positive value")
    @Column(name = "open_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal openPrice;

    @Positive(message = "High Price must be a positive value")
    @Column(name = "high_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal highPrice;

    @Positive(message = "Low Price must be a positive value")
    @Column(name = "low_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal lowPrice;

    @Positive(message = "Close Price must be a positive value")
    @Column(name = "close_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal closePrice;

    @PositiveOrZero(message = "Volume must be a positive value")
    @Column(name = "volume", nullable = false)
    private Long volume; // Trading volume for the security

    @Positive(message = "Adjusted Close Price must be a positive value")
    @Column(name = "adjusted_close_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal adjustedClosePrice; // Adjusted close price for the security, accounting for dividends and stock splits


    @NotNull(message = "Market date cannot be null")
    @Column(name = "market_date", nullable = false)
    private LocalDate marketDate; // Date and time of the market data entry

    @NotBlank(message = "Data source cannot be blank")
    @Column(name = "data_source", nullable = false)
    private String dataSource; // Source of the market data (e.g., data provider name Finnhub)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt; // Timestamp when the record was created (UTC)

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated; // Timestamp of the last update to the market data

    public MarketData (){

    }

    public MarketData(SecurityStock securityStock, BigDecimal openPrice, BigDecimal highPrice,
                      BigDecimal lowPrice, BigDecimal closePrice, Long volume,
                      BigDecimal adjustedClosePrice, LocalDate marketDate, String dataSource) {
        this.securityStock = securityStock;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
        this.adjustedClosePrice = adjustedClosePrice;
        this.marketDate = marketDate;
        this.dataSource = dataSource;
    }

    // Getters and Setters
    public Long getMarketDataId() {
        return marketDataId;
    }
    public void setMarketDataId(Long marketDataId) {
        this.marketDataId = marketDataId;
    }
    public SecurityStock getSecurityStock() {
        return securityStock;
    }
    public void setSecurityStock(SecurityStock securityStock) {
        this.securityStock = securityStock;
    }
    public BigDecimal getOpenPrice() {
        return openPrice;
    }
    public void setOpenPrice(BigDecimal openPrice) {
        this.openPrice = openPrice;
    }
    public BigDecimal getHighPrice() {
        return highPrice;
    }
    public void setHighPrice(BigDecimal highPrice) {
        this.highPrice = highPrice;
    }
    public BigDecimal getLowPrice() {
        return lowPrice;
    }
    public void setLowPrice(BigDecimal lowPrice) {
        this.lowPrice = lowPrice;
    }
    public BigDecimal getClosePrice() {
        return closePrice;
    }
    public void setClosePrice(BigDecimal closePrice) {
        this.closePrice = closePrice;
    }
    public Long getVolume() {
        return volume;
    }
    public void setVolume(Long volume) {
        this.volume = volume;
    }
    public BigDecimal getAdjustedClosePrice() {
        return adjustedClosePrice;
    }
    public void setAdjustedClosePrice(BigDecimal adjustedClosePrice) {
        this.adjustedClosePrice = adjustedClosePrice;
    }
    public LocalDate getMarketDate() {
        return marketDate;
    }
    public void setMarketDate(LocalDate marketDate) {
        this.marketDate = marketDate;
    }
    public String getDataSource() {
        return dataSource;
    }
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }
    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "MarketData{" +
                "marketDataId=" + marketDataId +
                ", securityStock=" + securityStock +
                ", openPrice=" + openPrice +
                ", highPrice=" + highPrice +
                ", lowPrice=" + lowPrice +
                ", closePrice=" + closePrice +
                ", volume=" + volume +
                ", adjustedClosePrice=" + adjustedClosePrice +
                ", marketDate=" + marketDate +
                ", dataSource='" + dataSource + '\'' +
                ", createdAt=" + createdAt +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
