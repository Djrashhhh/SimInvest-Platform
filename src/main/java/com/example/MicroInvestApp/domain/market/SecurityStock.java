package com.example.MicroInvestApp.domain.market;

import com.example.MicroInvestApp.domain.enums.Exchange;
import com.example.MicroInvestApp.domain.enums.SecuritySector;
import com.example.MicroInvestApp.domain.enums.SecurityType;
import com.example.MicroInvestApp.domain.portfolio.Dividend;
import com.example.MicroInvestApp.domain.portfolio.Position;
import com.example.MicroInvestApp.domain.portfolio.Watchlist;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
// REMOVED: Snake case strategy to use camelCase for frontend compatibility
// import com.fasterxml.jackson.databind.PropertyNamingStrategies;
// import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@SuppressWarnings({ "serial", "deprecation" })
@Entity
@Table(name = "SecurityStock", indexes = {
        @Index(name = "idx_symbol", columnList = "security_symbol", unique = true),
        @Index(name = "idx_sector", columnList = "sector"),
        @Index(name = "idx_exchange", columnList = "exchange")
})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
// REMOVED: JsonNaming annotation to use default camelCase
// @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class SecurityStock implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "security_id")
    private Long securityId;

    @NotBlank(message = "Symbol cannot be blank")
    @Size(min = 1, max = 10, message = "Symbol must be between 1 and 10 characters")
    @Pattern(regexp = "^[A-Z0-9.-]+$", message = "Symbol must contain only uppercase letters, numbers, dots and hyphens")
    @Column(name = "security_symbol", nullable = false, unique = true, length = 10)
    private String symbol;

    @NotBlank(message = "Company name cannot be blank")
    @Size(max = 200, message = "Company name cannot exceed 200 characters")
    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @NotNull(message = "Sector cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "sector", nullable = false)
    private SecuritySector sector;

    @Positive(message = "Market Cap must be a positive value")
    @Digits(integer = 27, fraction = 2, message = "Invalid market cap format")
    @Column(name = "market_cap", nullable = false, precision = 25, scale = 2)
    private BigDecimal marketCap;

    @Positive(message = "Current Price must be a positive value")
    @Digits(integer = 10, fraction = 4, message = "Invalid price format")
    @Column(name = "current_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentPrice;

    @NotNull(message = "Security type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "security_type", nullable = false)
    private SecurityType securityType;

    @NotNull(message = "Exchange cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "exchange", nullable = false)
    private Exchange exchange;

    @Column(name = "security_active", nullable = false)
    private boolean isActive;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private Instant updatedDate;

    // Optional fields for enhanced functionality
    @Column(name = "previous_close", precision = 19, scale = 4)
    private BigDecimal previousClose;

    @Column(name = "price_change", precision = 19, scale = 4)
    private BigDecimal priceChange;

    @Column(name = "price_change_percent", precision = 8, scale = 4)
    private BigDecimal priceChangePercent;

    // Relationships with proper JSON handling
    @OneToMany(mappedBy = "securityStock", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore // Ignore this to prevent circular references and serialization issues
    private List<MarketData> marketData;

    @OneToMany(mappedBy = "securityStock", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore // Ignore this to prevent circular references and serialization issues
    private List<Dividend> dividends;

    @OneToMany(mappedBy = "securityStock", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore // Ignore this to prevent circular references and serialization issues
    private List<Position> positions;

    @ManyToMany(mappedBy = "securities", fetch = FetchType.LAZY)
    @JsonIgnore // Ignore this to prevent circular references and serialization issues
    private Set<Watchlist> watchlists;

    // Constructors
    public SecurityStock() {
        // Default constructor for JPA
    }

    public SecurityStock(String symbol, String companyName, SecuritySector sector,
                         BigDecimal marketCap, BigDecimal currentPrice, SecurityType securityType,
                         Exchange exchange) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.sector = sector;
        this.marketCap = marketCap;
        this.currentPrice = currentPrice;
        this.securityType = securityType;
        this.exchange = exchange;
        this.isActive = true; // Default to active when created
    }

    // Getters and Setters
    public Long getSecurityId() {
        return securityId;
    }
    public void setSecurityId(Long securityId) {
        this.securityId = securityId;
    }
    public String getSymbol() {
        return symbol;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    public String getCompanyName() {
        return companyName;
    }
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    public SecuritySector getSector() {
        return sector;
    }
    public void setSector(SecuritySector sector) {
        this.sector = sector;
    }
    public BigDecimal getMarketCap() {
        return marketCap;
    }
    public void setMarketCap(BigDecimal marketCap) {
        this.marketCap = marketCap;
    }
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }
    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }
    public SecurityType getSecurityType() {
        return securityType;
    }
    public void setSecurityType(SecurityType securityType) {
        this.securityType = securityType;
    }
    public Exchange getExchange() {
        return exchange;
    }
    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        isActive = active;
    }
    public Instant getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }
    public Instant getUpdatedDate() {
        return updatedDate;
    }
    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }
    public BigDecimal getPreviousClose() {
        return previousClose;
    }
    public void setPreviousClose(BigDecimal previousClose) {
        this.previousClose = previousClose;
    }
    public BigDecimal getPriceChange() {
        return priceChange;
    }
    public void setPriceChange(BigDecimal priceChange) {
        this.priceChange = priceChange;
    }
    public BigDecimal getPriceChangePercent() {
        return priceChangePercent;
    }
    public void setPriceChangePercent(BigDecimal priceChangePercent) {
        this.priceChangePercent = priceChangePercent;
    }
    public List<MarketData> getMarketData() {
        return marketData;
    }
    public void setMarketData(List<MarketData> marketData) {
        this.marketData = marketData;
    }
    public List<Dividend> getDividends() {
        return dividends;
    }
    public void setDividends(List<Dividend> dividends) {
        this.dividends = dividends;
    }
    public List<Position> getPositions() {
        return positions;
    }
    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }
    public Set<Watchlist> getWatchlists() {
        return watchlists;
    }
    public void setWatchlists(Set<Watchlist> watchlists) {
        this.watchlists = watchlists;
    }

    // Business logic methods

    /**
     * Update price with automatic change calculation
     */
    public void updatePrice(BigDecimal newPrice) {
        if (this.currentPrice != null) {
            this.previousClose = this.currentPrice;
            this.priceChange = newPrice.subtract(this.currentPrice);

            if (this.currentPrice.compareTo(BigDecimal.ZERO) > 0) {
                this.priceChangePercent = this.priceChange
                        .divide(this.currentPrice, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }
        }

        this.currentPrice = newPrice;
        this.updatedDate = Instant.now();
    }

    /**
     * Check if the security data is stale
     */
    public boolean isDataStale(int staleThresholdHours) {
        if (this.updatedDate == null) {
            return true;
        }

        Instant staleThreshold = Instant.now().minusSeconds(staleThresholdHours * 3600L);
        return this.updatedDate.isBefore(staleThreshold);
    }

    /**
     * Get market capitalization in millions for display
     */
    public BigDecimal getMarketCapInMillions() {
        if (marketCap == null) {
            return null;
        }
        return marketCap.divide(BigDecimal.valueOf(1_000_000), 2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "SecurityStock{" +
                "securityId=" + securityId +
                ", symbol='" + symbol + '\'' +
                ", companyName='" + companyName + '\'' +
                ", sector=" + sector +
                ", marketCap=" + marketCap +
                ", currentPrice=" + currentPrice +
                ", securityType=" + securityType +
                ", exchange='" + exchange + '\'' +
                ", isActive=" + isActive +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SecurityStock)) return false;
        SecurityStock that = (SecurityStock) o;
        return symbol != null && symbol.equals(that.symbol);
    }

    @Override
    public int hashCode() {
        return symbol != null ? symbol.hashCode() : 0;
    }
}