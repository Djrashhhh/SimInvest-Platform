package com.example.MicroInvestApp.dto.portfolio.RequestDTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;



// Request DTO for updating portfolio
public class UpdatePortfolioRequestDTO {

    @Size(min = 1, max = 100, message = "Portfolio name must be between 1 and 100 characters")
    @JsonProperty("portfolio_name")
    private String portfolioName;

    @JsonProperty("is_active")
    private Boolean isActive;

    // Constructors
    public UpdatePortfolioRequestDTO() {}

    public UpdatePortfolioRequestDTO(String portfolioName, Boolean isActive) {
        this.portfolioName = portfolioName;
        this.isActive = isActive;
    }

    // Getters and Setters
    public String getPortfolioName() { return portfolioName; }
    public void setPortfolioName(String portfolioName) { this.portfolioName = portfolioName; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
