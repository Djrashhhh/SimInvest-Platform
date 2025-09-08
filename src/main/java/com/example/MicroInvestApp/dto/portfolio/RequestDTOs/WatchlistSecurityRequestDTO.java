package com.example.MicroInvestApp.dto.portfolio.RequestDTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

// Request DTO for adding/removing securities from watchlist
public class WatchlistSecurityRequestDTO {

    @NotEmpty(message = "Security IDs cannot be empty")
    @JsonProperty("security_ids")
    private Set<Long> securityIds;

    // Constructors
    public WatchlistSecurityRequestDTO() {}

    public WatchlistSecurityRequestDTO(Set<Long> securityIds) {
        this.securityIds = securityIds;
    }

    // Getters and Setters
    public Set<Long> getSecurityIds() { return securityIds; }
    public void setSecurityIds(Set<Long> securityIds) { this.securityIds = securityIds; }
}
