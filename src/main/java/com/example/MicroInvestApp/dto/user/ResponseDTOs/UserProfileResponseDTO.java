package com.example.MicroInvestApp.dto.user.ResponseDTOs;

import com.example.MicroInvestApp.domain.enums.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
public class UserProfileResponseDTO {

    @JsonProperty("profile_id")
    private Long profileId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("experience_level")
    private ExperienceLevel experienceLevel;

    @JsonProperty("investment_goal")
    private InvestmentGoalType investmentGoal;

    @JsonProperty("personal_financial_goal")
    private PersonalFinancialGoalType personalFinancialGoal;

    @JsonProperty("preferred_investment_types")
    private List<InvestmentType> preferredTypes;

    @JsonProperty("investment_goal_target_amount")
    private double investmentGoalTargetAmount;

    @JsonProperty("investment_goal_target_date")
    private LocalDate investmentGoalTargetDate;

    @JsonProperty("personal_financial_goal_target_amount")
    private double personalFinancialGoalTargetAmount;

    @JsonProperty("personal_financial_goal_description")
    private String personalFinancialGoalDescription;

    @JsonProperty("learning_progress")
    private int learningProgress;

    @JsonProperty("progress_percentage")
    private double progressPercentage;

    @JsonProperty("days_until_goal")
    private int daysUntilGoal;

    @JsonProperty("is_goal_overdue")
    private boolean isGoalOverdue;

    @JsonProperty("is_experienced")
    private boolean isExperienced;

    // Constructors, getters, and setters
    public UserProfileResponseDTO() {}

    // All getters and setters...
    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public ExperienceLevel getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(ExperienceLevel experienceLevel) { this.experienceLevel = experienceLevel; }

    public InvestmentGoalType getInvestmentGoal() { return investmentGoal; }
    public void setInvestmentGoal(InvestmentGoalType investmentGoal) { this.investmentGoal = investmentGoal; }

    public PersonalFinancialGoalType getPersonalFinancialGoal() { return personalFinancialGoal; }
    public void setPersonalFinancialGoal(PersonalFinancialGoalType personalFinancialGoal) { this.personalFinancialGoal = personalFinancialGoal; }

    public List<InvestmentType> getPreferredTypes() { return preferredTypes; }
    public void setPreferredTypes(List<InvestmentType> preferredTypes) { this.preferredTypes = preferredTypes; }

    public double getInvestmentGoalTargetAmount() { return investmentGoalTargetAmount; }
    public void setInvestmentGoalTargetAmount(double investmentGoalTargetAmount) { this.investmentGoalTargetAmount = investmentGoalTargetAmount; }

    public LocalDate getInvestmentGoalTargetDate() { return investmentGoalTargetDate; }
    public void setInvestmentGoalTargetDate(LocalDate investmentGoalTargetDate) { this.investmentGoalTargetDate = investmentGoalTargetDate; }

    public double getPersonalFinancialGoalTargetAmount() { return personalFinancialGoalTargetAmount; }
    public void setPersonalFinancialGoalTargetAmount(double personalFinancialGoalTargetAmount) { this.personalFinancialGoalTargetAmount = personalFinancialGoalTargetAmount; }

    public String getPersonalFinancialGoalDescription() { return personalFinancialGoalDescription; }
    public void setPersonalFinancialGoalDescription(String personalFinancialGoalDescription) { this.personalFinancialGoalDescription = personalFinancialGoalDescription; }

    public int getLearningProgress() { return learningProgress; }
    public void setLearningProgress(int learningProgress) { this.learningProgress = learningProgress; }

    public double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }

    public int getDaysUntilGoal() { return daysUntilGoal; }
    public void setDaysUntilGoal(int daysUntilGoal) { this.daysUntilGoal = daysUntilGoal; }

    public boolean isGoalOverdue() { return isGoalOverdue; }
    public void setGoalOverdue(boolean goalOverdue) { isGoalOverdue = goalOverdue; }

    public boolean isExperienced() { return isExperienced; }
    public void setExperienced(boolean experienced) { isExperienced = experienced; }
}
