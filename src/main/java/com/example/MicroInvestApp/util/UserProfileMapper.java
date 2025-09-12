package com.example.MicroInvestApp.util;
import com.example.MicroInvestApp.domain.user.UserProfile;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.UserProfileResponseDTO;
import com.example.MicroInvestApp.dto.user.RequestDTOs.UserProfileUpdateRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class UserProfileMapper {
    public UserProfile toEntity(UserProfileUpdateRequestDTO dto) {
        UserProfile profile = new UserProfile();

        profile.setExperienceLevel(dto.getExperienceLevel());
        profile.setInvestmentGoal(dto.getInvestmentGoal());
        profile.setPersonalFinancialGoal(dto.getPersonalFinancialGoal());
        profile.setPreferredTypes(dto.getPreferredTypes());

        if (dto.getInvestmentGoalTargetAmount() != null) {
            profile.setInvestmentGoalTargetAmount(dto.getInvestmentGoalTargetAmount());
        }
        if (dto.getInvestmentGoalTargetDate() != null) {
            profile.setInvestmentGoalTargetDate(dto.getInvestmentGoalTargetDate());
        }
        if (dto.getPersonalFinancialGoalTargetAmount() != null) {
            profile.setPersonalFinancialGoalTargetAmount(dto.getPersonalFinancialGoalTargetAmount());
        }
        if (dto.getPersonalFinancialGoalDescription() != null) {
            profile.setPersonalFinancialGoalDescription(dto.getPersonalFinancialGoalDescription());
        }

        return profile;
    }

    public UserProfileResponseDTO toResponseDTO(UserProfile profile) {
        UserProfileResponseDTO dto = new UserProfileResponseDTO();

        dto.setProfileId(profile.getProfileId());
        dto.setUserId(profile.getUserAccount().getUserId());
        dto.setExperienceLevel(profile.getExperienceLevel());
        dto.setInvestmentGoal(profile.getInvestmentGoal());
        dto.setPersonalFinancialGoal(profile.getPersonalFinancialGoal());
        dto.setPreferredTypes(profile.getPreferredTypes());
        dto.setInvestmentGoalTargetAmount(profile.getInvestmentGoalTargetAmount());
        dto.setInvestmentGoalTargetDate(profile.getInvestmentGoalTargetDate());
        dto.setPersonalFinancialGoalTargetAmount(profile.getPersonalFinancialGoalTargetAmount());
        dto.setPersonalFinancialGoalDescription(profile.getPersonalFinancialGoalDescription());
        dto.setLearningProgress(profile.getLearningProgress());

        // Set calculated fields
        dto.setProgressPercentage(profile.calculateProgressPercentage());
        dto.setDaysUntilGoal(profile.getDaysUntilGoal());
        dto.setGoalOverdue(profile.isGoalOverdue());
        dto.setExperienced(profile.isExperienced());

        return dto;
    }

    public void updateEntityFromDTO(UserProfileUpdateRequestDTO dto, UserProfile profile) {
        if (dto.getExperienceLevel() != null) {
            profile.setExperienceLevel(dto.getExperienceLevel());
        }
        if (dto.getInvestmentGoal() != null) {
            profile.setInvestmentGoal(dto.getInvestmentGoal());
        }
        if (dto.getPersonalFinancialGoal() != null) {
            profile.setPersonalFinancialGoal(dto.getPersonalFinancialGoal());
        }
        if (dto.getPreferredTypes() != null) {
            profile.setPreferredTypes(dto.getPreferredTypes());
        }
        if (dto.getInvestmentGoalTargetAmount() != null) {
            profile.setInvestmentGoalTargetAmount(dto.getInvestmentGoalTargetAmount());
        }
        if (dto.getInvestmentGoalTargetDate() != null) {
            profile.setInvestmentGoalTargetDate(dto.getInvestmentGoalTargetDate());
        }
        if (dto.getPersonalFinancialGoalTargetAmount() != null) {
            profile.setPersonalFinancialGoalTargetAmount(dto.getPersonalFinancialGoalTargetAmount());
        }
        if (dto.getPersonalFinancialGoalDescription() != null) {
            profile.setPersonalFinancialGoalDescription(dto.getPersonalFinancialGoalDescription());
        }
    }
}
