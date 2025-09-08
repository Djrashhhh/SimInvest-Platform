// SecuritySummaryDTO.java - Lightweight DTO for performance queries
package com.example.MicroInvestApp.dto;

import com.example.MicroInvestApp.domain.enums.Exchange;
import com.example.MicroInvestApp.domain.enums.SecuritySector;

import java.math.BigDecimal;

/**
 * Lightweight DTO for security summary information
 * Used in performance-critical queries where full SecurityStock entity is not needed
 */
public class SecuritySummaryDTO {

    private String symbol;
    private String companyName;
    private BigDecimal currentPrice;
    private SecuritySector sector;
    private Exchange exchange;

    // Constructors
    public SecuritySummaryDTO() {}

    public SecuritySummaryDTO(String symbol, String companyName, BigDecimal currentPrice,
                              SecuritySector sector, Exchange exchange) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
        this.sector = sector;
        this.exchange = exchange;
    }

    // Getters and Setters
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

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public SecuritySector getSector() {
        return sector;
    }

    public void setSector(SecuritySector sector) {
        this.sector = sector;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public String toString() {
        return "SecuritySummaryDTO{" +
                "symbol='" + symbol + '\'' +
                ", companyName='" + companyName + '\'' +
                ", currentPrice=" + currentPrice +
                ", sector=" + sector +
                ", exchange=" + exchange +
                '}';
    }
}