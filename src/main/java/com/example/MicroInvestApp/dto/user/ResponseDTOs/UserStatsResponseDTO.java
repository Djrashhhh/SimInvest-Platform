package com.example.MicroInvestApp.dto.user.ResponseDTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class UserStatsResponseDTO {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("total_achievements")
    private long totalAchievements;

    @JsonProperty("total_points")
    private int totalPoints;

    @JsonProperty("recent_achievements")
    private List<AchievementResponseDTO> recentAchievements;

    @JsonProperty("high_value_achievements")
    private List<AchievementResponseDTO> highValueAchievements;

    // Constructors, getters, and setters
    public UserStatsResponseDTO() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public long getTotalAchievements() { return totalAchievements; }
    public void setTotalAchievements(long totalAchievements) { this.totalAchievements = totalAchievements; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public List<AchievementResponseDTO> getRecentAchievements() { return recentAchievements; }
    public void setRecentAchievements(List<AchievementResponseDTO> recentAchievements) { this.recentAchievements = recentAchievements; }

    public List<AchievementResponseDTO> getHighValueAchievements() { return highValueAchievements; }
    public void setHighValueAchievements(List<AchievementResponseDTO> highValueAchievements) { this.highValueAchievements = highValueAchievements; }
}
