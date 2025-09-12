package com.example.MicroInvestApp.dto.portfolio.ResponseDTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

// DTO for Watchlist Summary Response overview
public class WatchlistSummaryResponseDTO {

    @JsonProperty("watchlist_id")
    private Long watchlistId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("security_count")
    private Integer securityCount;


    @JsonProperty("updated_at")
    private Instant updatedAt;

    // Constructors
    public WatchlistSummaryResponseDTO() {}

    public WatchlistSummaryResponseDTO(Long watchlistId, String name, String description,
                                    Integer securityCount, Instant updatedAt) {
        this.watchlistId = watchlistId;
        this.name = name;
        this.description = description;
        this.securityCount = securityCount;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getWatchlistId() { return watchlistId; }
    public void setWatchlistId(Long watchlistId) { this.watchlistId = watchlistId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getSecurityCount() { return securityCount; }
    public void setSecurityCount(Integer securityCount) { this.securityCount = securityCount; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
