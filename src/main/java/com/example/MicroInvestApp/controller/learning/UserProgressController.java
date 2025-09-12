package com.example.MicroInvestApp.controller.learning;

import com.example.MicroInvestApp.domain.enums.ProgressStatus;
import com.example.MicroInvestApp.dto.edcontent.ProgressUpdateRequestDTO;
import com.example.MicroInvestApp.dto.edcontent.UserProgressDTO;
import com.example.MicroInvestApp.dto.edcontent.UserProgressSummaryDTO;
import com.example.MicroInvestApp.service.learning.UserProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for User Progress operations
 * Handles all user progress tracking-related HTTP requests
 */
@RestController
@RequestMapping("/api/v1/user-progress")
@Tag(name = "User Progress Management", description = "APIs for managing user learning progress")
public class UserProgressController {

    private static final Logger logger = LoggerFactory.getLogger(UserProgressController.class);

    private final UserProgressService progressService;

    @Autowired
    public UserProgressController(UserProgressService progressService) {
        this.progressService = progressService;
    }

    @Operation(summary = "Start progress tracking", description = "Starts progress tracking for user and content")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Progress tracking started successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "User or content not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/start")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProgressDTO> startProgress(
            @Parameter(description = "User ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "Content ID", required = true)
            @RequestParam Long contentId) {

        logger.info("Starting progress tracking for user: {}, content: {}", userId, contentId);

        UserProgressDTO response = progressService.startProgress(userId, contentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update user progress", description = "Updates progress for specific user and content")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progress updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Progress not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/user/{userId}/content/{contentId}")
    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.userId")
    public ResponseEntity<UserProgressDTO> updateProgress(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Content ID", required = true)
            @PathVariable Long contentId,
            @Parameter(description = "Progress update request", required = true)
            @Valid @RequestBody ProgressUpdateRequestDTO request) {

        logger.info("Updating progress for user: {}, content: {}", userId, contentId);

        UserProgressDTO response = progressService.updateProgress(userId, contentId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user progress", description = "Retrieves progress for specific user and content")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progress found"),
            @ApiResponse(responseCode = "404", description = "Progress not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user/{userId}/content/{contentId}")
    @PreAuthorize("hasRole('USER') and (#userId == authentication.principal.userId or hasRole('ADMIN'))")
    public ResponseEntity<UserProgressDTO> getUserProgress(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Content ID", required = true)
            @PathVariable Long contentId) {

        logger.debug("Fetching user progress for user: {}, content: {}", userId, contentId);

        return progressService.getUserProgress(userId, contentId)
                .map(progress -> ResponseEntity.ok(progress))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all user progress", description = "Retrieves all progress for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progress retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') and (#userId == authentication.principal.userId or hasRole('ADMIN'))")
    public ResponseEntity<List<UserProgressDTO>> getAllUserProgress(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {

        logger.debug("Fetching all progress for user: {}", userId);

        List<UserProgressDTO> progressList = progressService.getAllUserProgress(userId);
        return ResponseEntity.ok(progressList);
    }

    @Operation(summary = "Get user progress by status", description = "Retrieves user progress filtered by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progress retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user/{userId}/status/{status}")
    @PreAuthorize("hasRole('USER') and (#userId == authentication.principal.userId or hasRole('ADMIN'))")
    public ResponseEntity<List<UserProgressDTO>> getUserProgressByStatus(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Progress status", required = true)
            @PathVariable String status) {

        logger.debug("Fetching progress by status for user: {}, status: {}", userId, status);

        try {
            ProgressStatus progressStatus = ProgressStatus.valueOf(status.toUpperCase());
            List<UserProgressDTO> progressList = progressService.getUserProgressByStatus(userId, progressStatus);
            return ResponseEntity.ok(progressList);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get completed content", description = "Retrieves all completed content for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Completed content retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user/{userId}/completed")
    @PreAuthorize("hasRole('USER') and (#userId == authentication.principal.userId or hasRole('ADMIN'))")
    public ResponseEntity<List<UserProgressDTO>> getCompletedContent(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {

        logger.debug("Fetching completed content for user: {}", userId);

        List<UserProgressDTO> completedContent = progressService.getCompletedContent(userId);
        return ResponseEntity.ok(completedContent);
    }

    @Operation(summary = "Get in-progress content", description = "Retrieves all in-progress content for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "In-progress content retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user/{userId}/in-progress")
    @PreAuthorize("hasRole('USER') and (#userId == authentication.principal.userId or hasRole('ADMIN'))")
    public ResponseEntity<List<UserProgressDTO>> getInProgressContent(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {

        logger.debug("Fetching in-progress content for user: {}", userId);

        List<UserProgressDTO> inProgressContent = progressService.getInProgressContent(userId);
        return ResponseEntity.ok(inProgressContent);
    }

    @Operation(summary = "Get recently accessed content", description = "Retrieves recently accessed content for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recent content retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid hours parameter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user/{userId}/recent")
    @PreAuthorize("hasRole('USER') and (#userId == authentication.principal.userId or hasRole('ADMIN'))")
    public ResponseEntity<List<UserProgressDTO>> getRecentlyAccessedContent(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Number of hours to look back", required = false)
            @RequestParam(defaultValue = "24") int hours) {

        logger.debug("Fetching recently accessed content for user: {}, hours: {}", userId, hours);

        if (hours <= 0 || hours > 168) { // Max 1 week
            return ResponseEntity.badRequest().build();
        }

        List<UserProgressDTO> recentContent = progressService.getRecentlyAccessedContent(userId, hours);
        return ResponseEntity.ok(recentContent);
    }

    @Operation(summary = "Mark content as completed", description = "Marks specific content as completed for user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content marked as completed successfully"),
            @ApiResponse(responseCode = "404", description = "User or content not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/user/{userId}/content/{contentId}/complete")
    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.userId")
    public ResponseEntity<UserProgressDTO> markAsCompleted(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Content ID", required = true)
            @PathVariable Long contentId) {

        logger.info("Marking content as completed for user: {}, content: {}", userId, contentId);

        UserProgressDTO response = progressService.markAsCompleted(userId, contentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update user rating", description = "Updates user rating for specific content")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rating updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid rating"),
            @ApiResponse(responseCode = "404", description = "User or content not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/user/{userId}/content/{contentId}/rate")
    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.userId")
    public ResponseEntity<UserProgressDTO> updateUserRating(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Content ID", required = true)
            @PathVariable Long contentId,
            @Parameter(description = "User rating (1-5)", required = true)
            @RequestParam Integer rating) {

        logger.info("Updating user rating for user: {}, content: {}, rating: {}", userId, contentId, rating);

        if (rating < 1 || rating > 5) {
            return ResponseEntity.badRequest().build();
        }

        UserProgressDTO response = progressService.updateUserRating(userId, contentId, rating);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user progress summary", description = "Retrieves comprehensive progress summary for user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progress summary retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user/{userId}/summary")
    @PreAuthorize("hasRole('USER') and (#userId == authentication.principal.userId or hasRole('ADMIN'))")
    public ResponseEntity<UserProgressSummaryDTO> getUserProgressSummary(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {

        logger.debug("Generating progress summary for user: {}", userId);

        UserProgressSummaryDTO summary = progressService.getUserProgressSummary(userId);
        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "Get content progress statistics", description = "Retrieves progress statistics for specific content (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/content/{contentId}/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getContentProgressStatistics(
            @Parameter(description = "Content ID", required = true)
            @PathVariable Long contentId) {

        logger.debug("Generating progress statistics for content: {}", contentId);

        Map<String, Object> statistics = progressService.getContentProgressStatistics(contentId);
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Get user completion rate", description = "Retrieves user's overall completion rate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Completion rate retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user/{userId}/completion-rate")
    @PreAuthorize("hasRole('USER') and (#userId == authentication.principal.userId or hasRole('ADMIN'))")
    public ResponseEntity<Map<String, Double>> getUserCompletionRate(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {

        logger.debug("Fetching completion rate for user: {}", userId);

        Double completionRate = progressService.getUserCompletionRate(userId);
        return ResponseEntity.ok(Map.of("completionRate", completionRate));
    }

    @Operation(summary = "Get learning streak", description = "Retrieves user's current learning streak")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Learning streak retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user/{userId}/learning-streak")
    @PreAuthorize("hasRole('USER') and (#userId == authentication.principal.userId or hasRole('ADMIN'))")
    public ResponseEntity<Map<String, Integer>> getLearningStreak(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {

        logger.debug("Fetching learning streak for user: {}", userId);

        Integer streak = progressService.getLearningStreak(userId);
        return ResponseEntity.ok(Map.of("learningStreak", streak));
    }

    @Operation(summary = "Get progress in date range", description = "Retrieves user progress within a specific date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progress retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user/{userId}/date-range")
    @PreAuthorize("hasRole('USER') and (#userId == authentication.principal.userId or hasRole('ADMIN'))")
    public ResponseEntity<List<UserProgressDTO>> getProgressInDateRange(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Start date (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        logger.debug("Fetching progress in date range for user: {}, start: {}, end: {}", userId, startDate, endDate);

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        List<UserProgressDTO> progressList = progressService.getProgressInDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(progressList);
    }

    @Operation(summary = "Delete user progress", description = "Deletes progress record for specific user and content")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Progress deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Progress not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/user/{userId}/content/{contentId}")
    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.userId")
    public ResponseEntity<Void> deleteUserProgress(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Content ID", required = true)
            @PathVariable Long contentId) {

        logger.info("Deleting user progress for user: {}, content: {}", userId, contentId);

        progressService.deleteUserProgress(userId, contentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reset user progress", description = "Resets progress for specific user and content")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progress reset successfully"),
            @ApiResponse(responseCode = "404", description = "Progress not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/user/{userId}/content/{contentId}/reset")
    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.userId")
    public ResponseEntity<UserProgressDTO> resetProgress(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Content ID", required = true)
            @PathVariable Long contentId) {

        logger.info("Resetting progress for user: {}, content: {}", userId, contentId);

        UserProgressDTO response = progressService.resetProgress(userId, contentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Check if user started content", description = "Checks if user has started specific content")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check completed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user/{userId}/content/{contentId}/started")
    @PreAuthorize("hasRole('USER') and (#userId == authentication.principal.userId or hasRole('ADMIN'))")
    public ResponseEntity<Map<String, Boolean>> hasUserStartedContent(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Content ID", required = true)
            @PathVariable Long contentId) {

        logger.debug("Checking if user {} has started content {}", userId, contentId);

        boolean hasStarted = progressService.hasUserStartedContent(userId, contentId);
        return ResponseEntity.ok(Map.of("hasStarted", hasStarted));
    }

    @Operation(summary = "Get content completion rate", description = "Retrieves completion rate for specific content")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Completion rate retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/content/{contentId}/completion-rate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Double>> getContentCompletionRate(
            @Parameter(description = "Content ID", required = true)
            @PathVariable Long contentId) {

        logger.debug("Fetching completion rate for content: {}", contentId);

        Double completionRate = progressService.getContentCompletionRate(contentId);
        return ResponseEntity.ok(Map.of("completionRate", completionRate));
    }

    @Operation(summary = "Get average user rating for content", description = "Retrieves average user rating for specific content")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Average rating retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/content/{contentId}/average-rating")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Double>> getAverageUserRatingForContent(
            @Parameter(description = "Content ID", required = true)
            @PathVariable Long contentId) {

        logger.debug("Fetching average user rating for content: {}", contentId);

        Double averageRating = progressService.getAverageUserRatingForContent(contentId);
        return ResponseEntity.ok(Map.of("averageRating", averageRating));
    }
}