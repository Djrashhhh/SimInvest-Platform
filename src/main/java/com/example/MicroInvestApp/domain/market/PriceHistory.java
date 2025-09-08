package com.example.MicroInvestApp.domain.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@SuppressWarnings({ "serial", "deprecation",  })
@Entity
@Table(name = "PriceHistory")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })

public class PriceHistory implements Serializable {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long priceHistoryId; // Unique identifier for the price history entry

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "securityId", nullable = false)
    private SecurityStock securityStock; // Reference to the associated SecurityStock entity

    @NotNull(message = "Date cannot be null")
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Positive(message = "Open Price must be a positive value")
    @Column(name = "open_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal openPrice; // Opening price of the security on the given date

    @Positive(message = "High Price must be a positive value")
    @Column(name = "high_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal highPrice; // Highest price of the security on the given date

    @Positive(message = "Low Price must be a positive value")
    @Column(name = "low_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal lowPrice; // Lowest price of the security on the given date

   @Positive(message = "Close Price must be a positive value")
    @Column(name = "close_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal closePrice; // Closing price of the security on the given date

    @Positive(message = "Volume must be a positive value")
    @Column(name = "volume", nullable = false)
    private Long volume; // Trading volume for the security on the given date

    @NotNull(message = "Price change cannot be null")
    @Column(name = "price_change", precision = 19, scale = 4)
    private BigDecimal priceChange; // Price change of the security on the given date

    @NotNull(message = "Percent change cannot be null")
    @Column(name = "percent_change", precision = 5, scale = 2)
    private BigDecimal percentChange; // Percentage change of the security on the given date

    @NotNull(message = "Moving average 50-day cannot be null")
    @Column(name = "moving_average_50_day", precision = 19, scale = 4)
    private BigDecimal movingAverage50Day; // 50-day moving average of the security price

    @NotNull(message = "Moving average 200-day cannot be null")
    @Column(name = "moving_average_200_day", precision = 19, scale = 4)
    private BigDecimal movingAverage200Day; // 200-day moving average of the security price

    public PriceHistory() {
        // Default constructor
    }

    public PriceHistory(SecurityStock securityStock, LocalDate date, BigDecimal openPrice, BigDecimal highPrice,
                        BigDecimal lowPrice, BigDecimal closePrice, Long volume, BigDecimal priceChange,
                        BigDecimal percentChange, BigDecimal movingAverage50Day, BigDecimal movingAverage200Day) {
        this.securityStock = securityStock;
        this.date = date;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
        this.priceChange = priceChange;
        this.percentChange = percentChange;
        this.movingAverage50Day = movingAverage50Day;
        this.movingAverage200Day = movingAverage200Day;
    }

    public Long getPriceHistoryId() {
        return priceHistoryId;
    }
    public void setPriceHistoryId(Long priceHistoryId) {
        this.priceHistoryId = priceHistoryId;
    }
    public SecurityStock getSecurityStock() {
        return securityStock;
    }
    public void setSecurityStock(SecurityStock securityStock) {
        this.securityStock = securityStock;
    }
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
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
    public BigDecimal getPriceChange() {
        return priceChange;
    }
    public void setPriceChange(BigDecimal priceChange) {
        this.priceChange = priceChange;
    }
    public BigDecimal getPercentChange() {
        return percentChange;
    }
    public void setPercentChange(BigDecimal percentChange) {
        this.percentChange = percentChange;
    }
    public BigDecimal getMovingAverage50Day() {
        return movingAverage50Day;
    }
    public void setMovingAverage50Day(BigDecimal movingAverage50Day) {
        this.movingAverage50Day = movingAverage50Day;
    }
    public BigDecimal getMovingAverage200Day() {
        return movingAverage200Day;
    }
    public void setMovingAverage200Day(BigDecimal movingAverage200Day) {
        this.movingAverage200Day = movingAverage200Day;
    }


}
