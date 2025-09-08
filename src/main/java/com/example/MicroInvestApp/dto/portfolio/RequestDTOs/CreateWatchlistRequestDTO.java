package com.example.MicroInvestApp.dto.portfolio.RequestDTOs;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

// Request DTO for creating a new watchlist
public class CreateWatchlistRequestDTO {

    @NotBlank(message = "Watchlist name cannot be blank")
    @Size(max = 100, message = "Watchlist name must not exceed 100 characters")
    @JsonProperty("name")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @JsonProperty("description")
    private String description;


    @JsonProperty("security_ids")
    private Set<Long> securityIds;

    // Constructors
    public CreateWatchlistRequestDTO() {}

    public CreateWatchlistRequestDTO(String name, String description, Set<Long> securityIds) {
        this.name = name;
        this.description = description;
        this.securityIds = securityIds;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Set<Long> getSecurityIds() { return securityIds; }
    public void setSecurityIds(Set<Long> securityIds) { this.securityIds = securityIds; }
}
