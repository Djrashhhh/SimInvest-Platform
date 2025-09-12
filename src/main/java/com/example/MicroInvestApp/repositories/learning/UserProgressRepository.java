//package com.example.MicroInvestApp.repositories.learning;
//
//import com.example.MicroInvestApp.domain.enums.ProgressStatus;
//import com.example.MicroInvestApp.domain.learning.EducationalContent;
//import com.example.MicroInvestApp.domain.learning.UserProgress;
//import com.example.MicroInvestApp.domain.user.UserAccount;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
///**
// * Repository interface for UserProgress entity operations
// * Provides CRUD operations and custom queries for user progress tracking
// */
//@Repository
//public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
//
//    /**
//     * Find user progress by user and educational content
//     * @param userAccount the user account
//     * @param educationalContent the educational content
//     * @return Optional user progress
//     */
//    Optional<UserProgress> findByUserAccountAndEducationalContent(UserAccount userAccount, EducationalContent educationalContent);
//
//    /**
//     * Find user progress by user ID and content ID
//     * @param userId the user ID
//     * @param contentId the content ID
//     * @return Optional user progress
//     */
//    @Query("SELECT up FROM UserProgress up WHERE up.userAccount.userId = :userId AND up.educationalContent.edContentId = :contentId")
//    Optional<UserProgress> findByUserIdAndContentId(@Param("userId") Long userId, @Param("contentId") Long contentId);
//
//    /**
//     * Find all progress records for a specific user
//     * @param userId the user ID
//     * @return List of user progress
//     */
//    @Query("SELECT up FROM UserProgress up JOIN FETCH up.educationalContent WHERE up.userAccount.userId = :userId ORDER BY up.lastAccessedAt DESC")
//    List<UserProgress> findByUserIdWithContent(@Param("userId") Long userId);
//
//    /**
//     * Find progress records for a user by status
//     * @param userId the user ID
//     * @param status the progress status
//     * @return List of user progress
//     */
//    @Query("SELECT up FROM UserProgress up JOIN FETCH up.educationalContent WHERE up.userAccount.userId = :userId AND up.status = :status ORDER BY up.lastAccessedAt DESC")
//    List<UserProgress> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") ProgressStatus status);
//
//    /**
//     * Find all completed progress for a user
//     * @param userId the user ID
//     * @return List of completed user progress
//     */
//    @Query("SELECT up FROM UserProgress up JOIN FETCH up.educationalContent WHERE up.userAccount.userId = :userId AND up.status = 'COMPLETED' ORDER BY up.finishedAt DESC")
//    List<UserProgress> findCompletedByUserId(@Param("userId") Long userId);
//
//    /**
//     * Find all in-progress content for a user
//     * @param userId the user ID
//     * @return List of in-progress user progress
//     */
//    @Query("SELECT up FROM UserProgress up JOIN FETCH up.educationalContent WHERE up.userAccount.userId = :userId AND up.status = 'IN_PROGRESS' ORDER BY up.lastAccessedAt DESC")
//    List<UserProgress> findInProgressByUserId(@Param("userId") Long userId);
//
//    /**
//     * Find all progress records for a specific content
//     * @param contentId the content ID
//     * @return List of user progress
//     */
//    @Query("SELECT up FROM UserProgress up JOIN FETCH up.userAccount WHERE up.educationalContent.edContentId = :contentId")
//    List<UserProgress> findByContentId(@Param("contentId") Long contentId);
//
//    /**
//     * Find progress records by completion percentage range
//     * @param userId the user ID
//     * @param minPercentage minimum completion percentage
//     * @param maxPercentage maximum completion percentage
//     * @return List of user progress
//     */
//    @Query("SELECT up FROM UserProgress up JOIN FETCH up.educationalContent WHERE up.userAccount.userId = :userId AND up.completionPercentage BETWEEN :minPercentage AND :maxPercentage")
//    List<UserProgress> findByCompletionPercentageRange(@Param("userId") Long userId,
//                                                       @Param("minPercentage") Integer minPercentage,
//                                                       @Param("maxPercentage") Integer maxPercentage);
//
//    /**
//     * Find recently accessed content for a user
//     * @param userId the user ID
//     * @param since the date/time since when to look
//     * @return List of user progress
//     */
//    @Query("SELECT up FROM UserProgress up JOIN FETCH up.educationalContent WHERE up.userAccount.userId = :userId AND up.lastAccessedAt >= :since ORDER BY up.lastAccessedAt DESC")
//    List<UserProgress> findRecentlyAccessedByUserId(@Param("userId") Long userId, @Param("since") LocalDateTime since);
//
//    /**
//     * Get completion statistics for a user
//     * @param userId the user ID
//     * @return List containing count by status
//     */
//    @Query("SELECT up.status, COUNT(up) FROM UserProgress up WHERE up.userAccount.userId = :userId GROUP BY up.status")
//    List<Object[]> getProgressStatsByUserId(@Param("userId") Long userId);
//
//    /**
//     * Get total completed content count for a user
//     * @param userId the user ID
//     * @return count of completed content
//     */
//    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.userAccount.userId = :userId AND up.status = 'COMPLETED'")
//    Long countCompletedByUserId(@Param("userId") Long userId);
//
//    /**
//     * Get total in-progress content count for a user
//     * @param userId the user ID
//     * @return count of in-progress content
//     */
//    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.userAccount.userId = :userId AND up.status = 'IN_PROGRESS'")
//    Long countInProgressByUserId(@Param("userId") Long userId);
//
//    /**
//     * Get average completion percentage for a user
//     * @param userId the user ID
//     * @return average completion percentage
//     */
//    @Query("SELECT AVG(up.completionPercentage) FROM UserProgress up WHERE up.userAccount.userId = :userId")
//    Double getAverageCompletionByUserId(@Param("userId") Long userId);
//
//    /**
//     * Get completion rate for specific content
//     * @param contentId the content ID
//     * @return completion rate as decimal (0-1)
//     */
//    @Query("SELECT CAST(COUNT(CASE WHEN up.status = 'COMPLETED' THEN 1 END) AS DOUBLE) / COUNT(up) FROM UserProgress up WHERE up.educationalContent.edContentId = :contentId")
//    Double getCompletionRateByContentId(@Param("contentId") Long contentId);
//
//    /**
//     * Find users who rated specific content
//     * @param contentId the content ID
//     * @return List of user progress with ratings
//     */
//    @Query("SELECT up FROM UserProgress up WHERE up.educationalContent.edContentId = :contentId AND up.userRating IS NOT NULL")
//    List<UserProgress> findRatingsByContentId(@Param("contentId") Long contentId);
//
//    /**
//     * Get average user rating for specific content
//     * @param contentId the content ID
//     * @return average user rating
//     */
//    @Query("SELECT AVG(up.userRating) FROM UserProgress up WHERE up.educationalContent.edContentId = :contentId AND up.userRating IS NOT NULL")
//    Double getAverageUserRatingByContentId(@Param("contentId") Long contentId);
//
//    /**
//     * Check if user has started specific content
//     * @param userId the user ID
//     * @param contentId the content ID
//     * @return true if user has progress record for content
//     */
//    @Query("SELECT COUNT(up) > 0 FROM UserProgress up WHERE up.userAccount.userId = :userId AND up.educationalContent.edContentId = :contentId")
//    boolean hasUserStartedContent(@Param("userId") Long userId, @Param("contentId") Long contentId);
//
//    /**
//     * Get learning streak for user (consecutive days with progress)
//     * @param userId the user ID
//     * @return List of distinct dates with activity
//     */
//    @Query("SELECT DISTINCT DATE(up.lastAccessedAt) FROM UserProgress up WHERE up.userAccount.userId = :userId ORDER BY DATE(up.lastAccessedAt) DESC")
//    List<java.sql.Date> getActivityDatesByUserId(@Param("userId") Long userId);
//
//    /**
//     * Find progress records updated within time range
//     * @param userId the user ID
//     * @param startDate start date
//     * @param endDate end date
//     * @return List of user progress
//     */
//    @Query("SELECT up FROM UserProgress up JOIN FETCH up.educationalContent WHERE up.userAccount.userId = :userId AND up.lastAccessedAt BETWEEN :startDate AND :endDate ORDER BY up.lastAccessedAt DESC")
//    List<UserProgress> findProgressInDateRange(@Param("userId") Long userId,
//                                               @Param("startDate") LocalDateTime startDate,
//                                               @Param("endDate") LocalDateTime endDate);
//
//    /**
//     * Delete progress records for specific content
//     * @param contentId the content ID
//     * @return number of deleted records
//     */
//    @Query("DELETE FROM UserProgress up WHERE up.educationalContent.edContentId = :contentId")
//    int deleteByContentId(@Param("contentId") Long contentId);
//
//    /**
//     * Delete progress records for specific user
//     * @param userId the user ID
//     * @return number of deleted records
//     */
//    @Query("DELETE FROM UserProgress up WHERE up.userAccount.userId = :userId")
//    int deleteByUserId(@Param("userId") Long userId);
//}