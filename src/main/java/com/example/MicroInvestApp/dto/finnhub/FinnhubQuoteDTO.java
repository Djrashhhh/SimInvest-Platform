// FinnhubQuoteDTO.java - Represents real-time quote/price data for a given stock at the moment of the request.
// It typically represents the current trading activity.
package com.example.MicroInvestApp.dto.finnhub;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class FinnhubQuoteDTO {

    @JsonProperty("c") // current price
    private BigDecimal currentPrice;

    @JsonProperty("h") // high price of the day
    private BigDecimal highPrice;

    @JsonProperty("l") // low price of the day
    private BigDecimal lowPrice;

    @JsonProperty("o") // open price of the day
    private BigDecimal openPrice;

    @JsonProperty("pc") // previous close price
    private BigDecimal previousClose;

    @JsonProperty("t") // timestamp
    private Long timestamp;

    // Constructors
    public FinnhubQuoteDTO() {}

    // Getters and Setters
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public BigDecimal getHighPrice() { return highPrice; }
    public void setHighPrice(BigDecimal highPrice) { this.highPrice = highPrice; }

    public BigDecimal getLowPrice() { return lowPrice; }
    public void setLowPrice(BigDecimal lowPrice) { this.lowPrice = lowPrice; }

    public BigDecimal getOpenPrice() { return openPrice; }
    public void setOpenPrice(BigDecimal openPrice) { this.openPrice = openPrice; }

    public BigDecimal getPreviousClose() { return previousClose; }
    public void setPreviousClose(BigDecimal previousClose) { this.previousClose = previousClose; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
