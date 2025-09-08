package com.example.MicroInvestApp.dto.portfolio.RequestDTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;


// Request DTO for creating a new portfolio
public class CreatePortfolioRequestDTO {

    @NotBlank(message = "Portfolio name cannot be blank")
    @Size(min = 1, max = 100, message = "Portfolio name must be between 1 and 100 characters")
    @JsonProperty("portfolio_name")
    private String portfolioName;

    @PositiveOrZero(message = "Initial cash balance cannot be negative")
    @Digits(integer = 15, fraction = 2, message = "Invalid cash balance format")
    @JsonProperty("initial_cash_balance")
    private BigDecimal initialCashBalance = BigDecimal.ZERO;

    // Constructors
    public CreatePortfolioRequestDTO() {}

    public CreatePortfolioRequestDTO(String portfolioName, BigDecimal initialCashBalance) {
        this.portfolioName = portfolioName;
        this.initialCashBalance = initialCashBalance;
    }

    // Getters and Setters
    public String getPortfolioName() { return portfolioName; }
    public void setPortfolioName(String portfolioName) { this.portfolioName = portfolioName; }

    public BigDecimal getInitialCashBalance() { return initialCashBalance; }
    public void setInitialCashBalance(BigDecimal initialCashBalance) { this.initialCashBalance = initialCashBalance; }
}

