package com.example.MicroInvestApp.dto.portfolio.ResponseDTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class WatchlistSecurityResponseDTO {

    @JsonProperty("security_id")
    private Long securityId;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("current_price")
    private java.math.BigDecimal currentPrice;

    @JsonProperty("last_updated")
    private Instant lastUpdated;

    // Constructors
    public WatchlistSecurityResponseDTO() {}

    public WatchlistSecurityResponseDTO(Long securityId, String symbol, String companyName,
                                     java.math.BigDecimal currentPrice, Instant lastUpdated) {
        this.securityId = securityId;
        this.symbol = symbol;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters
    public Long getSecurityId() { return securityId; }
    public void setSecurityId(Long securityId) { this.securityId = securityId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public java.math.BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(java.math.BigDecimal currentPrice) { this.currentPrice = currentPrice; }


    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
}
