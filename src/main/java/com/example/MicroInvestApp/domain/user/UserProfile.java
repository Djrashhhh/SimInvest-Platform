package com.example.MicroInvestApp.domain.user;

import com.example.MicroInvestApp.domain.enums.ExperienceLevel;
import com.example.MicroInvestApp.domain.enums.InvestmentGoalType;
import com.example.MicroInvestApp.domain.enums.InvestmentType;
import com.example.MicroInvestApp.domain.enums.PersonalFinancialGoalType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;  // ADDED: Import for validation annotations
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@SuppressWarnings({ "serial", "deprecation",  })
@Entity
@Table(name = "UserProfile")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class UserProfile implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;

    @NotNull(message = "Experience level is required")
    @Enumerated(EnumType.STRING)
    @Column(name="experience_level", nullable = false)  // ADDED: Missing @Column annotation
    private ExperienceLevel experienceLevel; // User's experience level in investing

    @NotNull(message = "Investment goal is required")  // ADDED: Validation annotation
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvestmentGoalType investmentGoal; // User's primary investment goal (e.g., retirement, education, etc.)

    @NotNull(message = "Personal financial goal is required")  // ADDED: Validation annotation
    @Enumerated(EnumType.STRING)
    @Column(name="personal_financial_goal", nullable = false)
    private PersonalFinancialGoalType personalFinancialGoal; // User's personal financial goal (e.g., debt-free, emergency fund, etc.)

    @ElementCollection(targetClass = InvestmentType.class, fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_preferred_investment_types", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "investment_type")
    private List<InvestmentType> preferredTypes; // List of preferred investment types (e.g., ETFs, stocks, mutual funds)

    @Positive(message = "Investment target amount must be positive")  // ADDED: Validation annotation
    @Column(name = "investment_goal_target_amount", nullable = false)
    private double investmentGoalTargetAmount;

    @Future(message = "Investment target date must be in the future")  // ADDED: Validation annotation
    @Column(name = "investment_goal_target_date", nullable = false)
    private LocalDate investmentGoalTargetDate; // Target date for achieving the investment goal

    @Positive(message = "Personal financial goal target amount must be positive")  // ADDED: Validation annotation
    @Column(name = "personal_financial_goal_target_amount", nullable = false)
    private double personalFinancialGoalTargetAmount; // Target amount for the personal financial goal

    @NotBlank(message = "Personal financial goal description is required")  // ADDED: Validation annotation
    @Size(max = 500, message = "Description must not exceed 500 characters")  // ADDED: Size validation
    @Column(name = "personal_financial_goal_description", nullable = false)
    private String personalFinancialGoalDescription;

    @Min(value = 0, message = "Learning progress cannot be negative")  // ADDED: Validation annotation
    @Column(name = "learning_progress")
    private int learningProgress = 0; // Track completed tutorials, lessons, etc.

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserAccount userAccount; // Reference to the associated UserAccount entity

    //no argument constructor
    public UserProfile (){

    }

    // Constructor with UserAccount
    public UserProfile(UserAccount userAccount) {
        this.userAccount = userAccount;
        this.learningProgress = 0;
    }

    // Business logic methods

    public boolean isGoalAchievable() {
        if (investmentGoalTargetAmount <= 0 || investmentGoalTargetDate.isBefore(LocalDate.now())) {
            return false;
        }

        // Calculate if goal is realistic based on current portfolio value
        double currentPortfolioValue = userAccount.getTotalPortfolioValue();
        long daysUntilGoal = ChronoUnit.DAYS.between(LocalDate.now(), investmentGoalTargetDate);

        // Simple calculation - more sophisticated logic could be added
        return daysUntilGoal > 30; // At least 30 days to achieve goal
    }

    // ADDED: Additional business logic methods
    public double calculateProgressPercentage() {
        if (investmentGoalTargetAmount <= 0) return 0.0;
        double currentValue = userAccount.getTotalPortfolioValue();
        return Math.min((currentValue / investmentGoalTargetAmount) * 100, 100.0);
    }

    public int getDaysUntilGoal() {
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), investmentGoalTargetDate);
    }

    public boolean isGoalOverdue() {
        return investmentGoalTargetDate.isBefore(LocalDate.now());
    }

    public void incrementLearningProgress() {
        this.learningProgress++;
    }

    public boolean isExperienced() {
        return experienceLevel == ExperienceLevel.INTERMEDIATE || experienceLevel == ExperienceLevel.ADVANCED;
    }

    //Get and Set methods for each field

    public Long getProfileId() {
        return profileId;
    }
    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

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

    public double getInvestmentGoalTargetAmount() {
        return investmentGoalTargetAmount;
    }
    public void setInvestmentGoalTargetAmount(double investmentGoalTargetAmount) {
        this.investmentGoalTargetAmount = investmentGoalTargetAmount;
    }

    public LocalDate getInvestmentGoalTargetDate() {
        return investmentGoalTargetDate;
    }
    public void setInvestmentGoalTargetDate(LocalDate investmentGoalTargetDate) {
        this.investmentGoalTargetDate = investmentGoalTargetDate;
    }

    public double getPersonalFinancialGoalTargetAmount() {
        return personalFinancialGoalTargetAmount;
    }
    public void setPersonalFinancialGoalTargetAmount(double personalFinancialGoalTargetAmount) {
        this.personalFinancialGoalTargetAmount = personalFinancialGoalTargetAmount;
    }

    public String getPersonalFinancialGoalDescription() {
        return personalFinancialGoalDescription;
    }
    public void setPersonalFinancialGoalDescription(String personalFinancialGoalDescription) {
        this.personalFinancialGoalDescription = personalFinancialGoalDescription;
    }

    public int getLearningProgress() {
        return learningProgress;
    }
    public void setLearningProgress(int learningProgress) {
        this.learningProgress = learningProgress;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }
    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "profileId=" + profileId +
                ", experienceLevel=" + experienceLevel +
                ", investmentGoal=" + investmentGoal +
                ", personalFinancialGoal=" + personalFinancialGoal +
                ", investmentGoalTargetAmount=" + investmentGoalTargetAmount +
                ", investmentGoalTargetDate=" + investmentGoalTargetDate +
                ", learningProgress=" + learningProgress +
                '}';
    }
}