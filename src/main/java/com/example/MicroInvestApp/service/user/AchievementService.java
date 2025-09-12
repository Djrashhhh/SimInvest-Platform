package com.example.MicroInvestApp.service.user;

import com.example.MicroInvestApp.dto.user.ResponseDTOs.AchievementResponseDTO;
import com.example.MicroInvestApp.dto.user.RequestDTOs.AchievementCreateRequestDTO;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.UserStatsResponseDTO;
import com.example.MicroInvestApp.domain.enums.AchievementType;
import com.example.MicroInvestApp.domain.enums.AchievementTier;

import java.util.List;
import java.util.Optional;

public interface AchievementService {
    AchievementResponseDTO createAchievement(Long userId, AchievementCreateRequestDTO achievementRequest);
    Optional<AchievementResponseDTO> getAchievementById(Long achievementId);
    List<AchievementResponseDTO> getAchievementsByUserId(Long userId);
    List<AchievementResponseDTO> getAchievementsByUserIdAndType(Long userId, AchievementType type);

    void deleteAchievement(Long achievementId);

    List<AchievementResponseDTO> getRecentAchievements(Long userId, int days);
    List<AchievementResponseDTO> getHighValueAchievements(int minPoints);

    UserStatsResponseDTO getUserStats(Long userId);
    int getTotalPointsByUserId(Long userId);
    long getAchievementCountByUserId(Long userId);

    boolean hasAchievement(Long userId, String achievementName, AchievementType type);

    // Achievement trigger methods
    void checkAndAwardTradeAchievements(Long userId, int tradeCount);
    void checkAndAwardBalanceAchievements(Long userId, double balance);
    void checkAndAwardLearningAchievements(Long userId, int learningProgress);
}
