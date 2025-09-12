package com.example.MicroInvestApp.domain.user;


import com.example.MicroInvestApp.domain.enums.AchievementTier;
import com.example.MicroInvestApp.domain.enums.AchievementType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;

import java.time.LocalDate;

@SuppressWarnings({ "serial", "deprecation",  })
@Entity
@Table(name = "Achievement",  indexes = {
        @Index(name = "idx_achievement_user_type", columnList = "userId, achievement_type"),
        @Index(name = "idx_achievement_date", columnList = "date_earned")
})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })


public class Achievement implements Serializable {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long achievementId; // Unique identifier for the achievement

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private UserAccount userAccount; // The user who earned the achievement

    @NotNull(message = "Achievement name cannot be null")
    @Column(name = "achievement_name", nullable = false)
    private String achievementName; // Name of the achievement

    @NotNull(message = "Achievement description cannot be null")
    @Column(name = "achievement_description", nullable = false)
    private String achievementDescription; // Description of the achievement

    @CreationTimestamp
    @Column(name = "date_earned", nullable = false, updatable = false)
    private LocalDate dateEarned; // The date when the achievement was earned

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Achievement type cannot be null")
    @Column(name = "achievement_type", nullable = false)
    private AchievementType achievementType; // Type of achievement (e.g., milestone, badge, etc.)

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Achievement tier cannot be null")
    @Column(name = "achievement_tier", nullable = false)
    private AchievementTier achievementTier; // Status of the achievement (e.g., active, inactive)

    //how the achievement is displayed in the app (e.g., icon, badge, etc.)
    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    //the requirements to earn the achievement numerical value (e.g., number of trades, portfolio value, etc.)
    @Min(value = 0, message = "Requirements threshold cannot be negative")
    @Column(name = "requirements_threshold")
    private Double requirementsThreshold;


    //Points/rewards associated with achievement
    @Min(value = 0, message = "Points cannot be negative")
    @Column(name = "points", nullable = false)
    private Integer points = 0;


    public Achievement(){

    }

    public Achievement(UserAccount userAccount, String achievementName, String achievementDescription, LocalDate dateEarned, AchievementType achievementType, AchievementTier achievementTier, Integer points) {
        this.userAccount = userAccount;
        this.achievementName = achievementName;
        this.achievementDescription = achievementDescription;
        this.dateEarned = dateEarned;
        this.achievementType = achievementType;
        this.achievementTier = achievementTier;
        this.points = points;
    }

    // Business logic methods
    public boolean isRecent() {
        return dateEarned != null && dateEarned.isAfter(LocalDate.now().minusDays(7));
    }

    public boolean isHighValue() {
        return points != null && points >= 100; // Configurable threshold
    }

    //Getters and Setters
    public Long getAchievementId() {
        return achievementId;
    }
    public void setAchievementId(Long achievementId) {
        this.achievementId = achievementId;
    }
    public UserAccount getUserAccount() {
        return userAccount;
    }
    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }
    public String getAchievementName() {
        return achievementName;
    }
    public void setAchievementName(String achievementName) {
        this.achievementName = achievementName;
    }
    public String getAchievementDescription() {
        return achievementDescription;
    }
    public void setAchievementDescription(String achievementDescription) {
        this.achievementDescription = achievementDescription;
    }
    public LocalDate getDateEarned() {
        return dateEarned;
    }
    public void setDateEarned(LocalDate dateEarned) {
        this.dateEarned = dateEarned;
    }
    public AchievementType getAchievementType() {
        return achievementType;
    }
    public void setAchievementType(AchievementType achievementType) {
        this.achievementType = achievementType;
    }
    public AchievementTier getAchievementTier() {
        return achievementTier;
    }
    public void setAchievementTier(AchievementTier achievementTier) {
        this.achievementTier = achievementTier;
    }
    public String getIconUrl() {
        return iconUrl;
    }
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
    public Double getRequirementsThreshold() {
        return requirementsThreshold;
    }
    public void setRequirementsThreshold(Double requirementsThreshold) {
        this.requirementsThreshold = requirementsThreshold;
    }
    public Integer getPoints() {
        return points;
    }
    public void setPoints(Integer points) {
        this.points = points;
    }


 @Override
    public String toString() {
        return "Achievement{" +
                "achievementId=" + achievementId +
                ", userAccount=" + userAccount +
                ", achievementName='" + achievementName + '\'' +
                ", achievementDescription='" + achievementDescription + '\'' +
                ", dateEarned=" + dateEarned +
                ", achievementType=" + achievementType +
                ", achievementTier=" + achievementTier +
                ", iconUrl='" + iconUrl + '\'' +
                ", requirementsThreshold=" + requirementsThreshold +
                ", points=" + points +
                '}';
    }
}
