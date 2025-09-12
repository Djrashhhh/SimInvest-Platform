package com.example.MicroInvestApp.dto.user.RequestDTOs;

import com.example.MicroInvestApp.domain.enums.RiskTolerance;
import com.example.MicroInvestApp.domain.enums.SecurityQuestion;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UserUpdateRequestDTO {

    @Email(message = "Email should be valid")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @JsonProperty("security_question")
    private SecurityQuestion securityQuestion;

    @JsonProperty("security_answer")
    private String securityAnswer;

    @JsonProperty("risk_tolerance")
    private RiskTolerance riskTolerance;

    // Constructors
    public UserUpdateRequestDTO() {}

    public UserUpdateRequestDTO(String email, String password, SecurityQuestion securityQuestion, String securityAnswer) {
        this.email = email;
        this.password = password;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public SecurityQuestion getSecurityQuestion() {
        return securityQuestion;
    }
    public void setSecurityQuestion(SecurityQuestion securityQuestion) {
        this.securityQuestion = securityQuestion;
    }
    public String getSecurityAnswer() {
        return securityAnswer;
    }
    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }
    public RiskTolerance getRiskTolerance() {
        return riskTolerance;
    }
    public void setRiskTolerance(RiskTolerance riskTolerance) {
        this.riskTolerance = riskTolerance;}
}
