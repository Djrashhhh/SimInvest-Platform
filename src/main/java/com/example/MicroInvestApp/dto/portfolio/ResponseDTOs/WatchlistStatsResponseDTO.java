package com.example.MicroInvestApp.dto.portfolio.ResponseDTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WatchlistStatsResponseDTO {

    @JsonProperty("total_watchlists")
    private Long totalWatchlists;

    @JsonProperty("public_watchlists")
    private Long publicWatchlists;

    @JsonProperty("private_watchlists")
    private Long privateWatchlists;

    @JsonProperty("total_securities")
    private Long totalSecurities;

    @JsonProperty("average_securities_per_watchlist")
    private Double averageSecuritiesPerWatchlist;

    // Constructors
    public WatchlistStatsResponseDTO() {}

    public WatchlistStatsResponseDTO(Long totalWatchlists, Long publicWatchlists,
                                     Long privateWatchlists, Long totalSecurities) {
        this.totalWatchlists = totalWatchlists;
        this.publicWatchlists = publicWatchlists;
        this.privateWatchlists = privateWatchlists;
        this.totalSecurities = totalSecurities;
        this.averageSecuritiesPerWatchlist = totalWatchlists > 0 ?
                (double) totalSecurities / totalWatchlists : 0.0;
    }

    // Getters and Setters
    public Long getTotalWatchlists() { return totalWatchlists; }
    public void setTotalWatchlists(Long totalWatchlists) {
        this.totalWatchlists = totalWatchlists;
        updateAverage();
    }

    public Long getPublicWatchlists() { return publicWatchlists; }
    public void setPublicWatchlists(Long publicWatchlists) {
        this.publicWatchlists = publicWatchlists;
    }

    public Long getPrivateWatchlists() { return privateWatchlists; }
    public void setPrivateWatchlists(Long privateWatchlists) {
        this.privateWatchlists = privateWatchlists;
    }

    public Long getTotalSecurities() { return totalSecurities; }
    public void setTotalSecurities(Long totalSecurities) {
        this.totalSecurities = totalSecurities;
        updateAverage();
    }

    public Double getAverageSecuritiesPerWatchlist() { return averageSecuritiesPerWatchlist; }
    public void setAverageSecuritiesPerWatchlist(Double averageSecuritiesPerWatchlist) {
        this.averageSecuritiesPerWatchlist = averageSecuritiesPerWatchlist;
    }

    private void updateAverage() {
        if (totalWatchlists != null && totalSecurities != null && totalWatchlists > 0) {
            this.averageSecuritiesPerWatchlist = (double) totalSecurities / totalWatchlists;
        } else {
            this.averageSecuritiesPerWatchlist = 0.0;
        }
    }
}
