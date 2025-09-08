package com.example.MicroInvestApp.domain.market;

import com.example.MicroInvestApp.domain.enums.Exchange;
import com.example.MicroInvestApp.domain.enums.SessionType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@SuppressWarnings({ "serial", "deprecation",  })
@Entity
@Table(name = "MarketSession")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class MarketSession implements Serializable {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long sessionId; // Unique identifier for the market session

    @NotNull(message = "Exchange cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "exchange", nullable = false)
    private Exchange exchange; // Name of the stock exchange (e.g., NYSE, NASDAQ,TSX)

    @NotNull(message = "Session date cannot be null")
    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate; // Date of the market session

    @NotNull(message = "Market open time cannot be null")
    @Column(name = "market_open_time", nullable = false)
    private LocalTime marketOpenTime; // Opening time of the market session

    @NotNull(message = "Market close time cannot be null")
    @Column(name = "market_close_time", nullable = false)
    private LocalTime marketCloseTime; // Closing time of the market session

    @NotNull(message = "Market open status cannot be null")
    @Column(name = "is_market_open", nullable = false)
    private boolean isMarketOpen; // Indicates if the market is currently open

    @NotNull(message = "Holiday Flag cannot be null")
    @Column(name = "is_holiday", nullable = false)
    private boolean isHoliday; // Indicates if the market session is a holiday

    @NotNull(message = "Session type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false)
    private SessionType sessionType; // Type of the market session (e.g., Regular, Pre-Market, After-Hours)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt; // Timestamp when the record was created (UTC)

    @UpdateTimestamp
    @NotNull(message = "Last updated timestamp cannot be null")
    private Instant lastUpdated; // Timestamp of the last update to the market session information

    public MarketSession() {
        // Default constructor
    }

    public MarketSession(Exchange exchange, LocalDate sessionDate, LocalTime marketOpenTime,
                         LocalTime marketCloseTime, boolean isMarketOpen, boolean isHoliday,
                         SessionType sessionType) {
        this.exchange = exchange;
        this.sessionDate = sessionDate;
        this.marketOpenTime = marketOpenTime;
        this.marketCloseTime = marketCloseTime;
        this.isMarketOpen = isMarketOpen;
        this.isHoliday = isHoliday;
        this.sessionType = sessionType;
    }

    //GETTERS AND SETTERS
    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public LocalTime getMarketOpenTime() {
        return marketOpenTime;
    }

    public void setMarketOpenTime(LocalTime marketOpenTime) {
        this.marketOpenTime = marketOpenTime;
    }

    public LocalTime getMarketCloseTime() {
        return marketCloseTime;
    }

    public void setMarketCloseTime(LocalTime marketCloseTime) {
        this.marketCloseTime = marketCloseTime;
    }

    public boolean isMarketOpen() {
        return isMarketOpen;
    }

    public void setMarketOpen(boolean marketOpen) {
        isMarketOpen = marketOpen;
    }

    public boolean isHoliday() {
        return isHoliday;
    }

    public void setHoliday(boolean holiday) {
        isHoliday = holiday;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
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
        return "MarketSession{" +
                "sessionId=" + sessionId +
                ", exchange=" + exchange +
                ", sessionDate=" + sessionDate +
                ", marketOpenTime=" + marketOpenTime +
                ", marketCloseTime=" + marketCloseTime +
                ", isMarketOpen=" + isMarketOpen +
                ", isHoliday=" + isHoliday +
                ", sessionType=" + sessionType +
                ", createdAt=" + createdAt +
                ", lastUpdated=" + lastUpdated +
                '}';
    }

}
