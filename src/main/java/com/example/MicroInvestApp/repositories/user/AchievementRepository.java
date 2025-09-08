package com.example.MicroInvestApp.repositories.user;

import com.example.MicroInvestApp.domain.enums.AchievementTier;
import com.example.MicroInvestApp.domain.enums.AchievementType;
import com.example.MicroInvestApp.domain.user.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    List<Achievement> findByUserAccountUserId(Long userId);
    List<Achievement> findByUserAccountUserIdAndAchievementType(Long userId, AchievementType type);
    List<Achievement> findByAchievementType(AchievementType type);
    List<Achievement> findByAchievementTier(AchievementTier tier);

    @Query("SELECT a FROM Achievement a WHERE a.userAccount.userId = :userId AND a.dateEarned >= :fromDate")
    List<Achievement> findRecentAchievements(@Param("userId") Long userId, @Param("fromDate") LocalDate fromDate);

    @Query("SELECT a FROM Achievement a WHERE a.points >= :minPoints")
    List<Achievement> findHighValueAchievements(@Param("minPoints") int minPoints);

    @Query("SELECT SUM(a.points) FROM Achievement a WHERE a.userAccount.userId = :userId")
    Integer getTotalPointsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(a) FROM Achievement a WHERE a.userAccount.userId = :userId")
    long countAchievementsByUserId(@Param("userId") Long userId);

    boolean existsByUserAccountUserIdAndAchievementNameAndAchievementType(
            Long userId, String achievementName, AchievementType type);

}
