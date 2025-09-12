//This DTO would be designed to capture historical price movements over a specific time interval (e.g., 1 minute, 1 hour, 1 day).
// Each "candle" summarizes the trading activity within that interval
package com.example.MicroInvestApp.dto.finnhub;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public class FinnhubCandleDTO {

    @JsonProperty("c") //  Maps JSON "c" to Java 'currentPrice'
    private List<BigDecimal> closePrices;   //List of close prices,

    @JsonProperty("h") // List of high prices
    private List<BigDecimal> highPrices;

    @JsonProperty("l") // List of low prices
    private List<BigDecimal> lowPrices;

    @JsonProperty("o") // List of open prices
    private List<BigDecimal> openPrices;

    @JsonProperty("t") // List of timestamps
    private List<Long> timestamps;

    @JsonProperty("v") // List of volumes
    private List<Long> volumes;

    @JsonProperty("s") // Status
    private String status;

    // Constructors
    public FinnhubCandleDTO() {}

    // Getters and Setters
    public List<BigDecimal> getClosePrices() { return closePrices; }
    public void setClosePrices(List<BigDecimal> closePrices) { this.closePrices = closePrices; }

    public List<BigDecimal> getHighPrices() { return highPrices; }
    public void setHighPrices(List<BigDecimal> highPrices) { this.highPrices = highPrices; }

    public List<BigDecimal> getLowPrices() { return lowPrices; }
    public void setLowPrices(List<BigDecimal> lowPrices) { this.lowPrices = lowPrices; }

    public List<BigDecimal> getOpenPrices() { return openPrices; }
    public void setOpenPrices(List<BigDecimal> openPrices) { this.openPrices = openPrices; }

    public List<Long> getTimestamps() { return timestamps; }
    public void setTimestamps(List<Long> timestamps) { this.timestamps = timestamps; }

    public List<Long> getVolumes() { return volumes; }
    public void setVolumes(List<Long> volumes) { this.volumes = volumes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
