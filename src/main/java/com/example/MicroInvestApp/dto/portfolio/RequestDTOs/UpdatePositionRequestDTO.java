package com.example.MicroInvestApp.dto.portfolio.RequestDTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

/**
 * Enhanced Request DTO for updating position with flexible validation
 */
public class UpdatePositionRequestDTO {

    @PositiveOrZero(message = "Quantity cannot be negative")
    @JsonProperty("quantity")
    private BigDecimal quantity;

    @DecimalMin(value = "0.0", inclusive = false, message = "Average cost per share must be positive")
    @JsonProperty("avg_cost_per_share")
    private BigDecimal avgCostPerShare;

    @PositiveOrZero(message = "Current value cannot be negative")
    @JsonProperty("current_value")
    private BigDecimal currentValue;

    // Enhanced: Additional fields for comprehensive updates
    @JsonProperty("force_recalculate")
    private Boolean forceRecalculate = false;

    @JsonProperty("update_market_value")
    private Boolean updateMarketValue = true;

    @JsonProperty("notes")
    private String notes;

    // Constructors
    public UpdatePositionRequestDTO() {}

    public UpdatePositionRequestDTO(BigDecimal quantity, BigDecimal avgCostPerShare, BigDecimal currentValue) {
        this.quantity = quantity;
        this.avgCostPerShare = avgCostPerShare;
        this.currentValue = currentValue;
    }

    public UpdatePositionRequestDTO(BigDecimal quantity, BigDecimal avgCostPerShare) {
        this.quantity = quantity;
        this.avgCostPerShare = avgCostPerShare;
        this.updateMarketValue = true;
    }

    // Enhanced: Validation methods
    public boolean isQuantityZero() {
        return quantity != null && quantity.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean hasValidQuantityAndPrice() {
        return quantity != null && avgCostPerShare != null &&
                quantity.compareTo(BigDecimal.ZERO) >= 0 &&
                avgCostPerShare.compareTo(BigDecimal.ZERO) > 0;
    }

    // Getters and Setters
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getAvgCostPerShare() { return avgCostPerShare; }
    public void setAvgCostPerShare(BigDecimal avgCostPerShare) { this.avgCostPerShare = avgCostPerShare; }

    public BigDecimal getCurrentValue() { return currentValue; }
    public void setCurrentValue(BigDecimal currentValue) { this.currentValue = currentValue; }

    public Boolean getForceRecalculate() { return forceRecalculate; }
    public void setForceRecalculate(Boolean forceRecalculate) { this.forceRecalculate = forceRecalculate; }

    public Boolean getUpdateMarketValue() { return updateMarketValue; }
    public void setUpdateMarketValue(Boolean updateMarketValue) { this.updateMarketValue = updateMarketValue; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return "UpdatePositionRequestDTO{" +
                "quantity=" + quantity +
                ", avgCostPerShare=" + avgCostPerShare +
                ", currentValue=" + currentValue +
                ", forceRecalculate=" + forceRecalculate +
                ", updateMarketValue=" + updateMarketValue +
                '}';
    }
}