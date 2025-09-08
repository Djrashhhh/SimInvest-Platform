package com.example.MicroInvestApp.service.user;

import com.example.MicroInvestApp.domain.enums.ExperienceLevel;
import com.example.MicroInvestApp.dto.user.RequestDTOs.UserProfileUpdateRequestDTO;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.UserProfileResponseDTO;

import java.util.List;
import java.util.Optional;

public interface UserProfileService {

    UserProfileResponseDTO createProfile(Long userId, UserProfileUpdateRequestDTO profileRequest);
    Optional<UserProfileResponseDTO> getProfileByUserId(Long userId);
    UserProfileResponseDTO updateProfile(Long userId, UserProfileUpdateRequestDTO updateRequest);
    void deleteProfile(Long userId);

    List<UserProfileResponseDTO> getProfilesByExperienceLevel(ExperienceLevel level);
    void incrementLearningProgress(Long userId);

    List<UserProfileResponseDTO> getProfilesWithUpcomingGoals(int daysAhead);
    List<UserProfileResponseDTO> getOverdueGoals();

    double calculateAverageGoalAmount();
}
