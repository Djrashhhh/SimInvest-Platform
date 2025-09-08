package com.example.MicroInvestApp.dto.user.RequestDTOs;

import com.example.MicroInvestApp.domain.enums.AchievementType;
import com.example.MicroInvestApp.domain.enums.AchievementTier;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public class AchievementCreateRequestDTO {
    @NotBlank(message = "Achievement name is required")
    @JsonProperty("achievement_name")
    private String achievementName;

    @NotBlank(message = "Achievement description is required")
    @JsonProperty("achievement_description")
    private String achievementDescription;

    @NotNull(message = "Achievement type is required")
    @JsonProperty("achievement_type")
    private AchievementType achievementType;

    @NotNull(message = "Achievement tier is required")
    @JsonProperty("achievement_tier")
    private AchievementTier achievementTier;

    @JsonProperty("icon_url")
    private String iconUrl;

    @Min(value = 0, message = "Requirements threshold cannot be negative")
    @JsonProperty("requirements_threshold")
    private Double requirementsThreshold;

    @Min(value = 0, message = "Points cannot be negative")
    @NotNull(message = "Points are required")
    private Integer points;

    // Constructors, getters, and setters
    public AchievementCreateRequestDTO() {}

    public String getAchievementName() { return achievementName; }
    public void setAchievementName(String achievementName) { this.achievementName = achievementName; }

    public String getAchievementDescription() { return achievementDescription; }
    public void setAchievementDescription(String achievementDescription) { this.achievementDescription = achievementDescription; }

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
}
