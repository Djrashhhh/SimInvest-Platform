package com.example.MicroInvestApp.dto.user.RequestDTOs;

import com.example.MicroInvestApp.domain.enums.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public class UserProfileUpdateRequestDTO {

    @NotNull(message = "Experience level is required")
    @JsonProperty("experience_level")
    private ExperienceLevel experienceLevel;

    @NotNull(message = "Investment goal is required")
    @JsonProperty("investment_goal")
    private InvestmentGoalType investmentGoal;

    @NotNull(message = "Personal financial goal is required")
    @JsonProperty("personal_financial_goal")
    private PersonalFinancialGoalType personalFinancialGoal;

    @JsonProperty("preferred_investment_types")
    private List<InvestmentType> preferredTypes;

    @Positive(message = "Investment target amount must be positive")
    @JsonProperty("investment_goal_target_amount")
    private Double investmentGoalTargetAmount;

    @Future(message = "Investment target date must be in the future")
    @JsonProperty("investment_goal_target_date")
    private LocalDate investmentGoalTargetDate;

    @Positive(message = "Personal financial goal target amount must be positive")
    @JsonProperty("personal_financial_goal_target_amount")
    private Double personalFinancialGoalTargetAmount;

    @NotBlank(message = "Personal financial goal description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @JsonProperty("personal_financial_goal_description")
    private String personalFinancialGoalDescription;

    // Default constructor
    public UserProfileUpdateRequestDTO() {}

    // Getters and setters
    public ExperienceLevel getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(ExperienceLevel experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public InvestmentGoalType getInvestmentGoal() {
        return investmentGoal;
    }

    public void setInvestmentGoal(InvestmentGoalType investmentGoal) {
        this.investmentGoal = investmentGoal;
    }

    public PersonalFinancialGoalType getPersonalFinancialGoal() {
        return personalFinancialGoal;
    }

    public void setPersonalFinancialGoal(PersonalFinancialGoalType personalFinancialGoal) {
        this.personalFinancialGoal = personalFinancialGoal;
    }

    public List<InvestmentType> getPreferredTypes() {
        return preferredTypes;
    }

    public void setPreferredTypes(List<InvestmentType> preferredTypes) {
        this.preferredTypes = preferredTypes;
    }

    public Double getInvestmentGoalTargetAmount() {
        return investmentGoalTargetAmount;
    }

    public void setInvestmentGoalTargetAmount(Double investmentGoalTargetAmount) {
        this.investmentGoalTargetAmount = investmentGoalTargetAmount;
    }

    public LocalDate getInvestmentGoalTargetDate() {
        return investmentGoalTargetDate;
    }

    public void setInvestmentGoalTargetDate(LocalDate investmentGoalTargetDate) {
        this.investmentGoalTargetDate = investmentGoalTargetDate;
    }

    public Double getPersonalFinancialGoalTargetAmount() {
        return personalFinancialGoalTargetAmount;
    }

    public void setPersonalFinancialGoalTargetAmount(Double personalFinancialGoalTargetAmount) {
        this.personalFinancialGoalTargetAmount = personalFinancialGoalTargetAmount;
    }

    public String getPersonalFinancialGoalDescription() {
        return personalFinancialGoalDescription;
    }

    public void setPersonalFinancialGoalDescription(String personalFinancialGoalDescription) {
        this.personalFinancialGoalDescription = personalFinancialGoalDescription;
    }
}