package com.example.MicroInvestApp.dto.portfolio.RequestDTOs;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

// Request DTO for creating a new position
public class CreatePositionRequestDTO {

    @NotNull(message = "Security ID cannot be null")
    @JsonProperty("security_id")
    private Long securityId;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be positive")
    @JsonProperty("quantity")
    private BigDecimal quantity;

    @NotNull(message = "Average cost per share cannot be null")
    @Positive(message = "Average cost per share must be positive")
    @JsonProperty("avg_cost_per_share")
    private BigDecimal avgCostPerShare;

    // Constructors
    public CreatePositionRequestDTO() {}

    public CreatePositionRequestDTO(Long securityId, BigDecimal quantity, BigDecimal avgCostPerShare) {
        this.securityId = securityId;
        this.quantity = quantity;
        this.avgCostPerShare = avgCostPerShare;
    }

    // Getters and Setters
    public Long getSecurityId() { return securityId; }
    public void setSecurityId(Long securityId) { this.securityId = securityId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getAvgCostPerShare() { return avgCostPerShare; }
    public void setAvgCostPerShare(BigDecimal avgCostPerShare) { this.avgCostPerShare = avgCostPerShare; }
}
