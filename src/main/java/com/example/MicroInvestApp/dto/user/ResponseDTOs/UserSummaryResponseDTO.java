// Purpose: Lightweight user data for lists and summaries. Reduces payload size for bulk operations.
package com.example.MicroInvestApp.dto.user.ResponseDTOs;

import com.example.MicroInvestApp.domain.enums.AccountStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class UserSummaryResponseDTO {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("full_name")
    private String fullName;

    private String email;
    private String username;

    @JsonProperty("account_status")
    private AccountStatus accountStatus;

    @JsonProperty("created_at")
    private LocalDate createdAt;

    @JsonProperty("net_worth")
    private double netWorth;

    // Constructors
    public UserSummaryResponseDTO() {}

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public double getNetWorth() { return netWorth; }
    public void setNetWorth(double netWorth) { this.netWorth = netWorth; }
}
