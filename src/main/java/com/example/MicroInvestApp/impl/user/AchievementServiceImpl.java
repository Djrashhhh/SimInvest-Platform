package com.example.MicroInvestApp.impl.user;

import com.example.MicroInvestApp.domain.user.Achievement;
import com.example.MicroInvestApp.domain.user.UserAccount;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.AchievementResponseDTO;
import com.example.MicroInvestApp.dto.user.RequestDTOs.AchievementCreateRequestDTO;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.UserStatsResponseDTO;
import com.example.MicroInvestApp.repositories.user.AchievementRepository;
import com.example.MicroInvestApp.repositories.user.UserAccountRepository;
import com.example.MicroInvestApp.service.user.AchievementService;
import com.example.MicroInvestApp.util.AchievementMapper;
import com.example.MicroInvestApp.exception.user.UserNotFoundException;
import com.example.MicroInvestApp.exception.user.AchievementNotFoundException;
import com.example.MicroInvestApp.domain.enums.AchievementType;
import com.example.MicroInvestApp.domain.enums.AchievementTier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional

public class AchievementServiceImpl implements AchievementService {
    private final AchievementRepository achievementRepository;
    private final UserAccountRepository userAccountRepository;
    private final AchievementMapper achievementMapper;

    @Autowired
    public AchievementServiceImpl(AchievementRepository achievementRepository,
                              UserAccountRepository userAccountRepository,
                              AchievementMapper achievementMapper) {
        this.achievementRepository = achievementRepository;
        this.userAccountRepository = userAccountRepository;
        this.achievementMapper = achievementMapper;
    }

    @Override
    public AchievementResponseDTO createAchievement(Long userId, AchievementCreateRequestDTO achievementRequest) {
        UserAccount userAccount = userAccountRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Check if achievement already exists
        if (hasAchievement(userId, achievementRequest.getAchievementName(), achievementRequest.getAchievementType())) {
            throw new IllegalArgumentException("Achievement already exists for this user");
        }

        Achievement achievement = achievementMapper.toEntity(achievementRequest);
        achievement.setUserAccount(userAccount);
        achievement.setDateEarned(LocalDate.now());

        Achievement savedAchievement = achievementRepository.save(achievement);
        return achievementMapper.toResponseDTO(savedAchievement);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AchievementResponseDTO> getAchievementById(Long achievementId) {
        return achievementRepository.findById(achievementId)
                .map(achievementMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementResponseDTO> getAchievementsByUserId(Long userId) {
        return achievementRepository.findByUserAccountUserId(userId)
                .stream()
                .map(achievementMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementResponseDTO> getAchievementsByUserIdAndType(Long userId, AchievementType type) {
        return achievementRepository.findByUserAccountUserIdAndAchievementType(userId, type)
                .stream()
                .map(achievementMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAchievement(Long achievementId) {
        if (!achievementRepository.existsById(achievementId)) {
            throw new AchievementNotFoundException("Achievement not found with ID: " + achievementId);
        }
        achievementRepository.deleteById(achievementId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementResponseDTO> getRecentAchievements(Long userId, int days) {
        LocalDate fromDate = LocalDate.now().minusDays(days);
        return achievementRepository.findRecentAchievements(userId, fromDate)
                .stream()
                .map(achievementMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementResponseDTO> getHighValueAchievements(int minPoints) {
        return achievementRepository.findHighValueAchievements(minPoints)
                .stream()
                .map(achievementMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatsResponseDTO getUserStats(Long userId) {
        UserStatsResponseDTO stats = new UserStatsResponseDTO();
        stats.setUserId(userId);
        stats.setTotalAchievements(getAchievementCountByUserId(userId));
        stats.setTotalPoints(getTotalPointsByUserId(userId));
        stats.setRecentAchievements(getRecentAchievements(userId, 30));
        stats.setHighValueAchievements(
                getAchievementsByUserId(userId).stream()
                        .filter(a -> a.getPoints() >= 100)
                        .collect(Collectors.toList())
        );

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalPointsByUserId(Long userId) {
        Integer total = achievementRepository.getTotalPointsByUserId(userId);
        return total != null ? total : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public long getAchievementCountByUserId(Long userId) {
        return achievementRepository.countAchievementsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAchievement(Long userId, String achievementName, AchievementType type) {
        return achievementRepository.existsByUserAccountUserIdAndAchievementNameAndAchievementType(
                userId, achievementName, type);
    }

    @Override
    public void checkAndAwardTradeAchievements(Long userId, int tradeCount) {
        // First Trade Achievement
        if (tradeCount == 1 && !hasAchievement(userId, "First Trade", AchievementType.TRADING)) {
            AchievementCreateRequestDTO firstTrade = new AchievementCreateRequestDTO();
            firstTrade.setAchievementName("First Trade");
            firstTrade.setAchievementDescription("Completed your first trade!");
            firstTrade.setAchievementType(AchievementType.TRADING);
            firstTrade.setAchievementTier(AchievementTier.BRONZE);
            firstTrade.setPoints(10);
            firstTrade.setRequirementsThreshold(1.0);

            createAchievement(userId, firstTrade);
        }

        // 10 Trades Achievement
        if (tradeCount == 10 && !hasAchievement(userId, "Active Trader", AchievementType.TRADING)) {
            AchievementCreateRequestDTO activeTrade = new AchievementCreateRequestDTO();
            activeTrade.setAchievementName("Active Trader");
            activeTrade.setAchievementDescription("Completed 10 trades!");
            activeTrade.setAchievementType(AchievementType.TRADING);
            activeTrade.setAchievementTier(AchievementTier.SILVER);
            activeTrade.setPoints(50);
            activeTrade.setRequirementsThreshold(10.0);

            createAchievement(userId, activeTrade);
        }
    }

    @Override
    public void checkAndAwardBalanceAchievements(Long userId, double balance) {
        // High Balance Achievement
        if (balance >= 50000 && !hasAchievement(userId, "Big Saver", AchievementType.PORTFOLIO_OVER_5000)) {
            AchievementCreateRequestDTO bigSaver = new AchievementCreateRequestDTO();
            bigSaver.setAchievementName("Big Saver");
            bigSaver.setAchievementDescription("Reached $50,000 in portfolio value!");
            bigSaver.setAchievementType(AchievementType.PORTFOLIO_OVER_5000);
            bigSaver.setAchievementTier(AchievementTier.GOLD);
            bigSaver.setPoints(100);
            bigSaver.setRequirementsThreshold(50000.0);

            createAchievement(userId, bigSaver);
        }
    }

    @Override
    public void checkAndAwardLearningAchievements(Long userId, int learningProgress) {
        // Learning Achievement
        if (learningProgress == 10 && !hasAchievement(userId, "Quick Learner", AchievementType.LEARNING)) {
            AchievementCreateRequestDTO quickLearner = new AchievementCreateRequestDTO();
            quickLearner.setAchievementName("Quick Learner");
            quickLearner.setAchievementDescription("Completed 10 learning modules!");
            quickLearner.setAchievementType(AchievementType.LEARNING);
            quickLearner.setAchievementTier(AchievementTier.BRONZE);
            quickLearner.setPoints(25);
            quickLearner.setRequirementsThreshold(10.0);

            createAchievement(userId, quickLearner);
        }
    }

}
