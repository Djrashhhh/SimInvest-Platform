// FinnhubCompanyProfileDTO.java - DTO for fetching company profile information from Finnhub API
// This allows us to get real company data (name, sector, exchange) when auto-creating securities
package com.example.MicroInvestApp.dto.finnhub;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class FinnhubCompanyProfileDTO {

    @JsonProperty("name")
    private String companyName;

    @JsonProperty("ticker")
    private String symbol;

    @JsonProperty("exchange")
    private String exchange;

    @JsonProperty("finnhubIndustry")
    private String industry;

    @JsonProperty("gicsSubIndustry")
    private String subIndustry;

    @JsonProperty("gicsSector")
    private String sector;

    @JsonProperty("marketCapitalization")
    private BigDecimal marketCap;

    @JsonProperty("country")
    private String country;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("weburl")
    private String website;

    @JsonProperty("logo")
    private String logoUrl;

    // Default constructor
    public FinnhubCompanyProfileDTO() {}

    // Getters and Setters
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getSubIndustry() { return subIndustry; }
    public void setSubIndustry(String subIndustry) { this.subIndustry = subIndustry; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public BigDecimal getMarketCap() { return marketCap; }
    public void setMarketCap(BigDecimal marketCap) { this.marketCap = marketCap; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    @Override
    public String toString() {
        return "FinnhubCompanyProfileDTO{" +
                "companyName='" + companyName + '\'' +
                ", symbol='" + symbol + '\'' +
                ", exchange='" + exchange + '\'' +
                ", sector='" + sector + '\'' +
                ", marketCap=" + marketCap +
                '}';
    }
}