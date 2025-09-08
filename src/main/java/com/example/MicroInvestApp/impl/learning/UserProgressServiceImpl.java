package com.example.MicroInvestApp.impl.learning;

import com.example.MicroInvestApp.domain.enums.ProgressStatus;
import com.example.MicroInvestApp.domain.learning.EducationalContent;
import com.example.MicroInvestApp.domain.learning.UserProgress;
import com.example.MicroInvestApp.domain.user.UserAccount;
import com.example.MicroInvestApp.dto.edcontent.ProgressUpdateRequestDTO;
import com.example.MicroInvestApp.dto.edcontent.UserProgressDTO;
import com.example.MicroInvestApp.dto.edcontent.UserProgressSummaryDTO;
import com.example.MicroInvestApp.exception.learning.ContentNotFoundException;
import com.example.MicroInvestApp.exception.learning.ProgressNotFoundException;
import com.example.MicroInvestApp.repositories.learning.EducationalContentRepository;
import com.example.MicroInvestApp.repositories.learning.UserProgressRepository;
import com.example.MicroInvestApp.repositories.user.UserAccountRepository;
import com.example.MicroInvestApp.service.learning.UserProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of UserProgressService
 * Handles all user progress-related business logic
 */
@Service
@Transactional
public class UserProgressServiceImpl implements UserProgressService {

    private static final Logger logger = LoggerFactory.getLogger(UserProgressServiceImpl.class);

    private final UserProgressRepository progressRepository;
    private final EducationalContentRepository contentRepository;
    private final UserAccountRepository userAccountRepository;

    @Autowired
    public UserProgressServiceImpl(UserProgressRepository progressRepository,
                                   EducationalContentRepository contentRepository,
                                   UserAccountRepository userAccountRepository) {
        this.progressRepository = progressRepository;
        this.contentRepository = contentRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserProgressDTO startProgress(Long userId, Long contentId) {
        logger.info("Starting progress tracking for user: {}, content: {}", userId, contentId);

        // Check if progress already exists
        Optional<UserProgress> existingProgress = progressRepository.findByUserIdAndContentId(userId, contentId);
        if (existingProgress.isPresent()) {
            logger.info("Progress already exists for user: {}, content: {}", userId, contentId);
            return convertToDTO(existingProgress.get());
        }

        // Get user and content
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        EducationalContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ContentNotFoundException("Educational content not found with ID: " + contentId));

        // Create new progress record
        UserProgress progress = new UserProgress(user, content, ProgressStatus.IN_PROGRESS);
        progress.setStartedAt(Instant.now());
        progress.setCompletionPercentage(0);

        UserProgress savedProgress = progressRepository.save(progress);
        logger.info("Progress started successfully for user: {}, content: {}", userId, contentId);

        return convertToDTO(savedProgress);
    }

    @Override
    public UserProgressDTO updateProgress(Long userId, Long contentId, ProgressUpdateRequestDTO request) {
        logger.info("Updating progress for user: {}, content: {}", userId, contentId);

        UserProgress progress = progressRepository.findByUserIdAndContentId(userId, contentId)
                .orElseThrow(() -> new ProgressNotFoundException("Progress not found for user: " + userId + ", content: " + contentId));

        // Update fields if provided
        if (request.getStatus() != null) {
            progress.setProgressStatus(ProgressStatus.valueOf(request.getStatus().toUpperCase()));
        }
        if (request.getCompletionPercentage() != null) {
            progress.setCompletionPercentage(request.getCompletionPercentage());

            // Auto-complete if 100%
            if (request.getCompletionPercentage() == 100) {
                progress.setProgressStatus(ProgressStatus.COMPLETED);
                progress.setFinishedAt(Instant.now());
            }
        }
        if (request.getUserRating() != null) {
            progress.setUserRating(request.getUserRating());
        }

        UserProgress updatedProgress = progressRepository.save(progress);
        logger.info("Progress updated successfully for user: {}, content: {}", userId, contentId);

        return convertToDTO(updatedProgress);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserProgressDTO> getUserProgress(Long userId, Long contentId) {
        logger.debug("Fetching user progress for user: {}, content: {}", userId, contentId);

        return progressRepository.findByUserIdAndContentId(userId, contentId)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProgressDTO> getAllUserProgress(Long userId) {
        logger.debug("Fetching all progress for user: {}", userId);

        return progressRepository.findByUserIdWithContent(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProgressDTO> getUserProgressByStatus(Long userId, ProgressStatus status) {
        logger.debug("Fetching progress by status for user: {}, status: {}", userId, status);

        return progressRepository.findByUserIdAndStatus(userId, status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProgressDTO> getCompletedContent(Long userId) {
        logger.debug("Fetching completed content for user: {}", userId);

        return progressRepository.findCompletedByUserId(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProgressDTO> getInProgressContent(Long userId) {
        logger.debug("Fetching in-progress content for user: {}", userId);

        return progressRepository.findInProgressByUserId(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProgressDTO> getRecentlyAccessedContent(Long userId, int hours) {
        logger.debug("Fetching recently accessed content for user: {}, hours: {}", userId, hours);

        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return progressRepository.findRecentlyAccessedByUserId(userId, since)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserProgressDTO markAsCompleted(Long userId, Long contentId) {
        logger.info("Marking content as completed for user: {}, content: {}", userId, contentId);

        UserProgress progress = progressRepository.findByUserIdAndContentId(userId, contentId)
                .orElse(null);

        if (progress == null) {
            // Create new progress if doesn't exist
            progress = createNewProgress(userId, contentId);
        }

        progress.markAsCompleted();
        progress.setCompletionPercentage(100);
        progress.setFinishedAt(Instant.now());

        UserProgress savedProgress = progressRepository.save(progress);
        return convertToDTO(savedProgress);
    }

    @Override
    public UserProgressDTO updateUserRating(Long userId, Long contentId, Integer rating) {
        logger.info("Updating user rating for user: {}, content: {}, rating: {}", userId, contentId, rating);

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        UserProgress progress = progressRepository.findByUserIdAndContentId(userId, contentId)
                .orElse(createNewProgress(userId, contentId));

        progress.setUserRating(rating);
        UserProgress savedProgress = progressRepository.save(progress);

        return convertToDTO(savedProgress);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProgressSummaryDTO getUserProgressSummary(Long userId) {
        logger.debug("Generating progress summary for user: {}", userId);

        List<Object[]> stats = progressRepository.getProgressStatsByUserId(userId);
        Long completedCount = progressRepository.countCompletedByUserId(userId);
        Long inProgressCount = progressRepository.countInProgressByUserId(userId);
        Double averageCompletion = progressRepository.getAverageCompletionByUserId(userId);
        Integer learningStreak = getLearningStreak(userId);

        UserProgressSummaryDTO summary = new UserProgressSummaryDTO();
        summary.setUserId(userId);
        summary.setCompletedCount(completedCount);
        summary.setInProgressCount(inProgressCount);
        summary.setAverageCompletionPercentage(averageCompletion != null ? averageCompletion : 0.0);
        summary.setLearningStreak(learningStreak);

        // Process status statistics
        Map<String, Long> statusCounts = new HashMap<>();
        for (Object[] stat : stats) {
            statusCounts.put(stat[0].toString(), (Long) stat[1]);
        }
        summary.setStatusCounts(statusCounts);

        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getContentProgressStatistics(Long contentId) {
        logger.debug("Generating progress statistics for content: {}", contentId);

        List<UserProgress> progressList = progressRepository.findByContentId(contentId);
        Double completionRate = progressRepository.getCompletionRateByContentId(contentId);
        Double averageUserRating = progressRepository.getAverageUserRatingByContentId(contentId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", (long) progressList.size());
        stats.put("completionRate", completionRate != null ? completionRate : 0.0);
        stats.put("averageUserRating", averageUserRating != null ? averageUserRating : 0.0);

        // Count by status
        Map<ProgressStatus, Long> statusCounts = progressList.stream()
                .collect(Collectors.groupingBy(UserProgress::getProgressStatus, Collectors.counting()));
        stats.put("statusCounts", statusCounts);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getUserCompletionRate(Long userId) {
        Long totalProgress = (long) progressRepository.findByUserIdWithContent(userId).size();
        Long completedCount = progressRepository.countCompletedByUserId(userId);

        if (totalProgress == 0) {
            return 0.0;
        }

        return (completedCount.doubleValue() / totalProgress.doubleValue()) * 100.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getLearningStreak(Long userId) {
        List<Date> activityDates = progressRepository.getActivityDatesByUserId(userId);

        if (activityDates.isEmpty()) {
            return 0;
        }

        // Sort dates in descending order
        activityDates.sort(Collections.reverseOrder());

        int streak = 1;
        Date previousDate = activityDates.get(0);

        for (int i = 1; i < activityDates.size(); i++) {
            Date currentDate = activityDates.get(i);

            // Check if dates are consecutive
            if (isConsecutiveDays(currentDate, previousDate)) {
                streak++;
                previousDate = currentDate;
            } else {
                break;
            }
        }

        return streak;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProgressDTO> getProgressInDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Fetching progress in date range for user: {}, start: {}, end: {}", userId, startDate, endDate);

        return progressRepository.findProgressInDateRange(userId, startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUserProgress(Long userId, Long contentId) {
        logger.info("Deleting user progress for user: {}, content: {}", userId, contentId);

        UserProgress progress = progressRepository.findByUserIdAndContentId(userId, contentId)
                .orElseThrow(() -> new ProgressNotFoundException("Progress not found for user: " + userId + ", content: " + contentId));

        progressRepository.delete(progress);
        logger.info("User progress deleted successfully for user: {}, content: {}", userId, contentId);
    }

    @Override
    public UserProgressDTO resetProgress(Long userId, Long contentId) {
        logger.info("Resetting progress for user: {}, content: {}", userId, contentId);

        UserProgress progress = progressRepository.findByUserIdAndContentId(userId, contentId)
                .orElseThrow(() -> new ProgressNotFoundException("Progress not found for user: " + userId + ", content: " + contentId));

        progress.setProgressStatus(ProgressStatus.NOT_STARTED);
        progress.setCompletionPercentage(0);
        progress.setFinishedAt(null);
        progress.setUserRating(null);
        progress.setStartedAt(Instant.now());

        UserProgress resetProgress = progressRepository.save(progress);
        return convertToDTO(resetProgress);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserStartedContent(Long userId, Long contentId) {
        return progressRepository.hasUserStartedContent(userId, contentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getUserAverageRating(Long userId) {
        List<UserProgress> progressList = progressRepository.findByUserIdWithContent(userId);

        OptionalDouble average = progressList.stream()
                .filter(p -> p.getUserRating() != null)
                .mapToInt(UserProgress::getUserRating)
                .average();

        return average.isPresent() ? average.getAsDouble() : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getContentCompletionRate(Long contentId) {
        Double rate = progressRepository.getCompletionRateByContentId(contentId);
        return rate != null ? rate * 100.0 : 0.0; // Convert to percentage
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageUserRatingForContent(Long contentId) {
        Double rating = progressRepository.getAverageUserRatingByContentId(contentId);
        return rating != null ? rating : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateProgressOwnership(Long userId, Long progressId) {
        Optional<UserProgress> progress = progressRepository.findById(progressId);
        return progress.isPresent() &&
                progress.get().getUserAccount().getUserId().equals(userId);
    }

    // Private helper methods
    private UserProgress createNewProgress(Long userId, Long contentId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        EducationalContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ContentNotFoundException("Educational content not found with ID: " + contentId));

        UserProgress progress = new UserProgress(user, content, ProgressStatus.IN_PROGRESS);
        progress.setStartedAt(Instant.now());
        progress.setCompletionPercentage(0);

        return progress;
    }

    private UserProgressDTO convertToDTO(UserProgress progress) {
        UserProgressDTO dto = new UserProgressDTO();
        dto.setStatus(progress.getProgressStatus().toString());
        dto.setCompletionPercentage(progress.getCompletionPercentage());
        dto.setUserRating(progress.getUserRating());

        if (progress.getStartedAt() != null) {
            dto.setStartedAt(LocalDateTime.ofInstant(progress.getStartedAt(), java.time.ZoneOffset.UTC));
        }
        if (progress.getFinishedAt() != null) {
            dto.setCompletedAt(LocalDateTime.ofInstant(progress.getFinishedAt(), java.time.ZoneOffset.UTC));
        }
        dto.setLastAccessedAt(progress.getLastAccessedAt());

        return dto;
    }

    private boolean isConsecutiveDays(Date date1, Date date2) {
        long diffInMillis = Math.abs(date2.getTime() - date1.getTime());
        long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);
        return diffInDays == 1;
    }
}