package com.example.MicroInvestApp.repositories.user;

import com.example.MicroInvestApp.domain.enums.ExperienceLevel;
import com.example.MicroInvestApp.domain.enums.InvestmentGoalType;
import com.example.MicroInvestApp.domain.user.UserProfile;
import com.example.MicroInvestApp.dto.user.RequestDTOs.UserProfileUpdateRequestDTO;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.UserProfileResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile,Long> {


    Optional<UserProfile> findByUserAccountUserId(Long userId);
    List<UserProfile> findByExperienceLevel(ExperienceLevel experienceLevel);
    List<UserProfile> findByInvestmentGoal(InvestmentGoalType investmentGoal);

    @Query("SELECT p FROM UserProfile p WHERE p.investmentGoalTargetDate BETWEEN :startDate AND :endDate")
    List<UserProfile> findByGoalTargetDateRange(@Param("startDate") LocalDate start, @Param("endDate") LocalDate end);

    @Query("SELECT p FROM UserProfile p WHERE p.investmentGoalTargetAmount >= :minAmount")
    List<UserProfile> findByMinimumGoalAmount(@Param("minAmount") double minAmount);

    @Query("SELECT p FROM UserProfile p WHERE p.learningProgress >= :minProgress")
    List<UserProfile> findByMinimumLearningProgress(@Param("minProgress") int minProgress);

    @Query("SELECT AVG(p.investmentGoalTargetAmount) FROM UserProfile p")
    Double getAverageGoalAmount();
}
