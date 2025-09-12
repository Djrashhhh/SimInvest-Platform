package com.example.MicroInvestApp.impl.user;

import com.example.MicroInvestApp.domain.user.UserProfile;
import com.example.MicroInvestApp.domain.user.UserAccount;

import com.example.MicroInvestApp.dto.user.RequestDTOs.UserProfileUpdateRequestDTO;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.UserProfileResponseDTO;
import com.example.MicroInvestApp.repositories.user.UserProfileRepository;
import com.example.MicroInvestApp.repositories.user.UserAccountRepository;

import com.example.MicroInvestApp.service.user.UserProfileService;
import com.example.MicroInvestApp.util.UserProfileMapper;
import com.example.MicroInvestApp.exception.user.UserNotFoundException;
import com.example.MicroInvestApp.exception.user.UserProfileNotFoundException;
import com.example.MicroInvestApp.domain.enums.ExperienceLevel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserProfileMapper userProfileMapper;

    @Autowired
    public UserProfileServiceImpl(UserProfileRepository userProfileRepository,
                              UserAccountRepository userAccountRepository,
                              UserProfileMapper userProfileMapper) {
        this.userProfileRepository = userProfileRepository;
        this.userAccountRepository = userAccountRepository;
        this.userProfileMapper = userProfileMapper;
    }

    @Override
    public UserProfileResponseDTO createProfile(Long userId, UserProfileUpdateRequestDTO profileRequest) {
        UserAccount userAccount = userAccountRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        UserProfile profile = userProfileMapper.toEntity(profileRequest);
        profile.setUserAccount(userAccount);

        UserProfile savedProfile = userProfileRepository.save(profile);
        return userProfileMapper.toResponseDTO(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfileResponseDTO> getProfileByUserId(Long userId) {
        return userProfileRepository.findByUserAccountUserId(userId)
                .map(userProfileMapper::toResponseDTO);
    }

    @Override
    public UserProfileResponseDTO updateProfile(Long userId, UserProfileUpdateRequestDTO updateRequest) {
        UserProfile profile = userProfileRepository.findByUserAccountUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("Profile not found for user ID: " + userId));

        userProfileMapper.updateEntityFromDTO(updateRequest, profile);
        UserProfile updatedProfile = userProfileRepository.save(profile);

        return userProfileMapper.toResponseDTO(updatedProfile);
    }

    @Override
    public void deleteProfile(Long userId) {
        UserProfile profile = userProfileRepository.findByUserAccountUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("Profile not found for user ID: " + userId));

        userProfileRepository.delete(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponseDTO> getProfilesByExperienceLevel(ExperienceLevel level) {
        return userProfileRepository.findByExperienceLevel(level)
                .stream()
                .map(userProfileMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void incrementLearningProgress(Long userId) {
        UserProfile profile = userProfileRepository.findByUserAccountUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("Profile not found for user ID: " + userId));

        profile.incrementLearningProgress();
        userProfileRepository.save(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponseDTO> getProfilesWithUpcomingGoals(int daysAhead) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(daysAhead);

        return userProfileRepository.findByGoalTargetDateRange(startDate, endDate)
                .stream()
                .map(userProfileMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponseDTO> getOverdueGoals() {
        return userProfileRepository.findAll()
                .stream()
                .filter(UserProfile::isGoalOverdue)
                .map(userProfileMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateAverageGoalAmount() {
        Double average = userProfileRepository.getAverageGoalAmount();
        return average != null ? average : 0.0;
    }
}
