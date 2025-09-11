//package com.example.MicroInvestApp.service.learning;
//
//import com.example.MicroInvestApp.domain.enums.ProgressStatus;
//import com.example.MicroInvestApp.dto.edcontent.ProgressUpdateRequestDTO;
//import com.example.MicroInvestApp.dto.edcontent.UserProgressDTO;
//import com.example.MicroInvestApp.dto.edcontent.UserProgressSummaryDTO;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//public interface UserProgressService {
//
//    /**
//     * Start progress tracking for user and content
//     * @param userId the user ID
//     * @param contentId the content ID
//     * @return created user progress DTO
//     */
//    UserProgressDTO startProgress(Long userId, Long contentId);
//
//    /**
//     * Update user progress
//     * @param userId the user ID
//     * @param contentId the content ID
//     * @param request the progress update request
//     * @return updated user progress DTO
//     */
//    UserProgressDTO updateProgress(Long userId, Long contentId, ProgressUpdateRequestDTO request);
//
//    /**
//     * Get user progress for specific content
//     * @param userId the user ID
//     * @param contentId the content ID
//     * @return Optional user progress DTO
//     */
//    Optional<UserProgressDTO> getUserProgress(Long userId, Long contentId);
//
//    /**
//     * Get all progress for a user
//     * @param userId the user ID
//     * @return List of user progress DTOs
//     */
//    List<UserProgressDTO> getAllUserProgress(Long userId);
//
//    /**
//     * Get user progress by status
//     * @param userId the user ID
//     * @param status the progress status
//     * @return List of user progress DTOs
//     */
//    List<UserProgressDTO> getUserProgressByStatus(Long userId, ProgressStatus status);
//
//    /**
//     * Get completed content for user
//     * @param userId the user ID
//     * @return List of completed user progress DTOs
//     */
//    List<UserProgressDTO> getCompletedContent(Long userId);
//
//    /**
//     * Get in-progress content for user
//     * @param userId the user ID
//     * @return List of in-progress user progress DTOs
//     */
//    List<UserProgressDTO> getInProgressContent(Long userId);
//
//    /**
//     * Get recently accessed content for user
//     * @param userId the user ID
//     * @param hours the number of hours to look back
//     * @return List of recently accessed user progress DTOs
//     */
//    List<UserProgressDTO> getRecentlyAccessedContent(Long userId, int hours);
//
//    /**
//     * Mark content as completed
//     * @param userId the user ID
//     * @param contentId the content ID
//     * @return updated user progress DTO
//     */
//    UserProgressDTO markAsCompleted(Long userId, Long contentId);
//
//    /**
//     * Update user rating for content
//     * @param userId the user ID
//     * @param contentId the content ID
//     * @param rating the user rating (1-5)
//     * @return updated user progress DTO
//     */
//    UserProgressDTO updateUserRating(Long userId, Long contentId, Integer rating);
//
//    /**
//     * Get user progress summary/statistics
//     * @param userId the user ID
//     * @return user progress summary DTO
//     */
//    UserProgressSummaryDTO getUserProgressSummary(Long userId);
//
//    /**
//     * Get progress statistics for specific content
//     * @param contentId the content ID
//     * @return Map containing progress statistics
//     */
//    Map<String, Object> getContentProgressStatistics(Long contentId);
//
//    /**
//     * Get user's completion rate
//     * @param userId the user ID
//     * @return completion rate as percentage
//     */
//    Double getUserCompletionRate(Long userId);
//
//    /**
//     * Get learning streak for user (consecutive days with activity)
//     * @param userId the user ID
//     * @return current learning streak in days
//     */
//    Integer getLearningStreak(Long userId);
//
//    /**
//     * Get progress in date range
//     * @param userId the user ID
//     * @param startDate start date
//     * @param endDate end date
//     * @return List of user progress DTOs in date range
//     */
//    List<UserProgressDTO> getProgressInDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);
//
//    /**
//     * Delete user progress for specific content
//     * @param userId the user ID
//     * @param contentId the content ID
//     */
//    void deleteUserProgress(Long userId, Long contentId);
//
//    /**
//     * Reset user progress for specific content
//     * @param userId the user ID
//     * @param contentId the content ID
//     * @return reset user progress DTO
//     */
//    UserProgressDTO resetProgress(Long userId, Long contentId);
//
//    /**
//     * Check if user has started specific content
//     * @param userId the user ID
//     * @param contentId the content ID
//     * @return true if user has started content
//     */
//    boolean hasUserStartedContent(Long userId, Long contentId);
//
//    /**
//     * Get average rating given by user
//     * @param userId the user ID
//     * @return average rating given by user
//     */
//    Double getUserAverageRating(Long userId);
//
//    /**
//     * Get content completion rate
//     * @param contentId the content ID
//     * @return completion rate as decimal (0-1)
//     */
//    Double getContentCompletionRate(Long contentId);
//
//    /**
//     * Get average user rating for content
//     * @param contentId the content ID
//     * @return average user rating for content
//     */
//    Double getAverageUserRatingForContent(Long contentId);
//
//    /**
//     * Validate user progress ownership
//     * @param userId the user ID
//     * @param progressId the progress ID
//     * @return true if user owns the progress record
//     */
//    boolean validateProgressOwnership(Long userId, Long progressId);
//}