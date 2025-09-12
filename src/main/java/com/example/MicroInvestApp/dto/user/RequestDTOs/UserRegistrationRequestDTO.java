package com.example.MicroInvestApp.dto.user.RequestDTOs;

import com.example.MicroInvestApp.domain.enums.Currency;
import com.example.MicroInvestApp.domain.enums.RiskTolerance;
import com.example.MicroInvestApp.domain.enums.SecurityQuestion;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public class UserRegistrationRequestDTO {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @JsonProperty("first_name")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @JsonProperty("last_name")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotNull(message = "Risk tolerance is required")
    @JsonProperty("risk_tolerance")
    private RiskTolerance riskTolerance;

    @NotNull(message = "Security question is required")
    @JsonProperty("security_question")
    private SecurityQuestion securityQuestion;

    @NotBlank(message = "Security answer is required")
    @JsonProperty("security_answer")
    private String securityAnswer;

    @JsonProperty("account_currency")
    private Currency accountCurrency = Currency.USD;

    @Min(value = 1000, message = "Initial balance must be at least 1000")
    @Max(value = 100000, message = "Initial balance cannot exceed 100000")
    @JsonProperty("initial_virtual_balance")
    private Double initialVirtualBalance = 10000.0;

    // Constructors
    public UserRegistrationRequestDTO() {}

    // Getters and Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public RiskTolerance getRiskTolerance() { return riskTolerance; }
    public void setRiskTolerance(RiskTolerance riskTolerance) { this.riskTolerance = riskTolerance; }

    public SecurityQuestion getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(SecurityQuestion securityQuestion) { this.securityQuestion = securityQuestion; }

    public String getSecurityAnswer() { return securityAnswer; }
    public void setSecurityAnswer(String securityAnswer) { this.securityAnswer = securityAnswer; }

    public Currency getAccountCurrency() { return accountCurrency; }
    public void setAccountCurrency(Currency accountCurrency) { this.accountCurrency = accountCurrency; }

    public Double getInitialVirtualBalance() { return initialVirtualBalance; }
    public void setInitialVirtualBalance(Double initialVirtualBalance) { this.initialVirtualBalance = initialVirtualBalance; }
}
