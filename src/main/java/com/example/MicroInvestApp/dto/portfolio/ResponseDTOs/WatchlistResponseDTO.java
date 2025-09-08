package com.example.MicroInvestApp.dto.portfolio.ResponseDTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;


// Response DTO for watchlist information
public class WatchlistResponseDTO {

    @JsonProperty("watchlist_id")
    private Long watchlistId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;


    @JsonProperty("security_count")
    private Integer securityCount;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    @JsonProperty("securities")
    private List<WatchlistSecurityResponseDTO> securities;

    // Constructors
    public WatchlistResponseDTO() {}

    public WatchlistResponseDTO(Long watchlistId, Long userId, String name, String description,
                              Integer securityCount, Instant createdAt, Instant updatedAt) {
        this.watchlistId = watchlistId;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.securityCount = securityCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getWatchlistId() { return watchlistId; }
    public void setWatchlistId(Long watchlistId) { this.watchlistId = watchlistId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }


    public Integer getSecurityCount() { return securityCount; }
    public void setSecurityCount(Integer securityCount) { this.securityCount = securityCount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<WatchlistSecurityResponseDTO> getSecurities() { return securities; }
    public void setSecurities(List<WatchlistSecurityResponseDTO> securities) { this.securities = securities; }
}
