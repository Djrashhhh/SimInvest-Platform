package com.example.MicroInvestApp.util;

import com.example.MicroInvestApp.domain.user.Achievement;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.AchievementResponseDTO;
import com.example.MicroInvestApp.dto.user.RequestDTOs.AchievementCreateRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class AchievementMapper {

    public Achievement toEntity(AchievementCreateRequestDTO dto) {
        Achievement achievement = new Achievement();

        achievement.setAchievementName(dto.getAchievementName());
        achievement.setAchievementDescription(dto.getAchievementDescription());
        achievement.setAchievementType(dto.getAchievementType());
        achievement.setAchievementTier(dto.getAchievementTier());
        achievement.setIconUrl(dto.getIconUrl());
        achievement.setRequirementsThreshold(dto.getRequirementsThreshold());
        achievement.setPoints(dto.getPoints());

        return achievement;
    }

    public AchievementResponseDTO toResponseDTO(Achievement achievement) {
        AchievementResponseDTO dto = new AchievementResponseDTO();

        dto.setAchievementId(achievement.getAchievementId());
        dto.setUserId(achievement.getUserAccount().getUserId());
        dto.setAchievementName(achievement.getAchievementName());
        dto.setAchievementDescription(achievement.getAchievementDescription());
        dto.setDateEarned(achievement.getDateEarned());
        dto.setAchievementType(achievement.getAchievementType());
        dto.setAchievementTier(achievement.getAchievementTier());
        dto.setIconUrl(achievement.getIconUrl());
        dto.setRequirementsThreshold(achievement.getRequirementsThreshold());
        dto.setPoints(achievement.getPoints());

        // Set calculated fields
        dto.setRecent(achievement.isRecent());
        dto.setHighValue(achievement.isHighValue());

        return dto;
    }
}
