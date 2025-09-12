package com.example.MicroInvestApp.dto.portfolio.RequestDTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

// Request DTO for updating an existing watchlist
public class UpdateWatchlistRequestDTO {

    @Size(max = 100, message = "Watchlist name must not exceed 100 characters")
    @JsonProperty("name")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @JsonProperty("description")
    private String description;


    // Constructors
    public UpdateWatchlistRequestDTO() {}

    public UpdateWatchlistRequestDTO(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

}
