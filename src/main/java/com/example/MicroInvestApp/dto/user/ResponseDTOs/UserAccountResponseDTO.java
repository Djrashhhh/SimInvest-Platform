// Purpose: Represents user account data sent to clients. Excludes sensitive information like passwords.
package com.example.MicroInvestApp.dto.user.ResponseDTOs;

import com.example.MicroInvestApp.domain.enums.AccountStatus;
import com.example.MicroInvestApp.domain.enums.Currency;
import com.example.MicroInvestApp.domain.enums.RiskTolerance;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserAccountResponseDTO {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("username")
    private String username;

    @JsonProperty("risk_tolerance")
    private RiskTolerance riskTolerance;

    @JsonProperty("account_status")
    private AccountStatus accountStatus;

    @JsonProperty("is_active")
    private boolean active;

    @JsonProperty("email_verified")
    private boolean emailVerified;

    @JsonProperty("created_at")
    private LocalDate createdAt;

    @JsonProperty("last_updated")
    private LocalDateTime updatedAt;

    @JsonProperty("current_virtual_balance")
    private double currentVirtualBalance;

    @JsonProperty("total_invested_amount")
    private double totalInvestedAmount;

    @JsonProperty("total_returns")
    private double totalReturns;

    @JsonProperty("account_currency")
    private Currency accountCurrency;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("net_worth")
    private double netWorth;

    @JsonProperty("return_on_investment")
    private double returnOnInvestment;

    // Constructors
    public UserAccountResponseDTO() {}

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public RiskTolerance getRiskTolerance() { return riskTolerance; }
    public void setRiskTolerance(RiskTolerance riskTolerance) { this.riskTolerance = riskTolerance; }

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public double getCurrentVirtualBalance() { return currentVirtualBalance; }
    public void setCurrentVirtualBalance(double currentVirtualBalance) { this.currentVirtualBalance = currentVirtualBalance; }

    public double getTotalInvestedAmount() { return totalInvestedAmount; }
    public void setTotalInvestedAmount(double totalInvestedAmount) { this.totalInvestedAmount = totalInvestedAmount; }

    public double getTotalReturns() { return totalReturns; }
    public void setTotalReturns(double totalReturns) { this.totalReturns = totalReturns; }

    public Currency getAccountCurrency() { return accountCurrency; }
    public void setAccountCurrency(Currency accountCurrency) { this.accountCurrency = accountCurrency; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public double getNetWorth() { return netWorth; }
    public void setNetWorth(double netWorth) { this.netWorth = netWorth; }

    public double getReturnOnInvestment() { return returnOnInvestment; }
    public void setReturnOnInvestment(double returnOnInvestment) { this.returnOnInvestment = returnOnInvestment; }
}