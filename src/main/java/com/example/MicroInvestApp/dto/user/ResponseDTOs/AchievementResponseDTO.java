package com.example.MicroInvestApp.dto.user.ResponseDTOs;
import com.example.MicroInvestApp.domain.enums.AchievementType;
import com.example.MicroInvestApp.domain.enums.AchievementTier;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public class AchievementResponseDTO {
    @JsonProperty("achievement_id")
    private Long achievementId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("achievement_name")
    private String achievementName;

    @JsonProperty("achievement_description")
    private String achievementDescription;

    @JsonProperty("date_earned")
    private LocalDate dateEarned;

    @JsonProperty("achievement_type")
    private AchievementType achievementType;

    @JsonProperty("achievement_tier")
    private AchievementTier achievementTier;

    @JsonProperty("icon_url")
    private String iconUrl;

    @JsonProperty("requirements_threshold")
    private Double requirementsThreshold;

    private Integer points;

    @JsonProperty("is_recent")
    private boolean isRecent;

    @JsonProperty("is_high_value")
    private boolean isHighValue;

    // Constructors, getters, and setters
    public AchievementResponseDTO() {}

    // All getters and setters...
    public Long getAchievementId() { return achievementId; }
    public void setAchievementId(Long achievementId) { this.achievementId = achievementId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getAchievementName() { return achievementName; }
    public void setAchievementName(String achievementName) { this.achievementName = achievementName; }

    public String getAchievementDescription() { return achievementDescription; }
    public void setAchievementDescription(String achievementDescription) { this.achievementDescription = achievementDescription; }

    public LocalDate getDateEarned() { return dateEarned; }
    public void setDateEarned(LocalDate dateEarned) { this.dateEarned = dateEarned; }

    public AchievementType getAchievementType() { return achievementType; }
    public void setAchievementType(AchievementType achievementType) { this.achievementType = achievementType; }

    public AchievementTier getAchievementTier() { return achievementTier; }
    public void setAchievementTier(AchievementTier achievementTier) { this.achievementTier = achievementTier; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public Double getRequirementsThreshold() { return requirementsThreshold; }
    public void setRequirementsThreshold(Double requirementsThreshold) { this.requirementsThreshold = requirementsThreshold; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public boolean isRecent() { return isRecent; }
    public void setRecent(boolean recent) { isRecent = recent; }

    public boolean isHighValue() { return isHighValue; }
    public void setHighValue(boolean highValue) { isHighValue = highValue; }
}
