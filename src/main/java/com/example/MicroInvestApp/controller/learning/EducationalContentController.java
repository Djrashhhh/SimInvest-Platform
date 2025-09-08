package com.example.MicroInvestApp.controller.learning;

import com.example.MicroInvestApp.domain.enums.ContentCategory;
import com.example.MicroInvestApp.domain.enums.ContentType;
import com.example.MicroInvestApp.domain.enums.DifficultyLevel;
import com.example.MicroInvestApp.dto.edcontent.CreateEducationalContentRequestDTO;
import com.example.MicroInvestApp.dto.edcontent.EducationalContentResponseDTO;
import com.example.MicroInvestApp.dto.edcontent.UpdateEducationalContentRequestDTO;
import com.example.MicroInvestApp.service.learning.EducationalContentService;
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
 * REST Controller for Educational Content operations
 * Handles all educational content-related HTTP requests
 */
@RestController
@RequestMapping("/api/v1/educational-content")
@Tag(name = "Educational Content Management", description = "APIs for managing educational content")
public class EducationalContentController {

    private static final Logger logger = LoggerFactory.getLogger(EducationalContentController.class);

    private final EducationalContentService contentService;

    @Autowired
    public EducationalContentController(EducationalContentService contentService) {
        this.contentService = contentService;
    }

    @Operation(summary = "Create educational content", description = "Creates new educational content (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Content created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EducationalContentResponseDTO> createContent(
            @Parameter(description = "Content creation request", required = true)
            @Valid @RequestBody CreateEducationalContentRequestDTO request) {

        logger.info("Creating educational content with title: {}", request.getTitle());

        EducationalContentResponseDTO response = contentService.createContent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get content by ID", description = "Retrieves educational content by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content found"),
            @ApiResponse(responseCode = "404", description = "Content not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{contentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EducationalContentResponseDTO> getContentById(
            @Parameter(description = "Content ID", required = true)
            @PathVariable Long contentId) {

        logger.debug("Fetching educational content by ID: {}", contentId);

        return contentService.getContentById(contentId)
                .map(content -> ResponseEntity.ok(content))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get content by ID with progress", description = "Retrieves educational content by its ID with user progress")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content found"),
            @ApiResponse(responseCode = "404", description = "Content not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{contentId}/progress")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EducationalContentResponseDTO> getContentByIdWithProgress(
            @Parameter(description = "Content ID", required = true)
            @PathVariable Long contentId,
            @Parameter(description = "User ID", required = true)
            @RequestParam Long userId) {

        logger.debug("Fetching educational content by ID: {} with progress for user: {}", contentId, userId);

        return contentService.getContentByIdWithProgress(contentId, userId)
                .map(content -> ResponseEntity.ok(content))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update educational content", description = "Updates educational content information (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Content not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{contentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EducationalContentResponseDTO> updateContent(
            @Parameter(description = "Content ID", required = true)
            @PathVariable Long contentId,
            @Parameter(description = "Content update request", required = true)
            @Valid @RequestBody UpdateEducationalContentRequestDTO request) {

        logger.info("Updating educational content ID: {}", contentId);

        EducationalContentResponseDTO response = contentService.updateContent(contentId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete educational content", description = "Deletes educational content (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Content deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Content not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{contentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteContent(
            @Parameter(description = "Content ID", required = true)
            @PathVariable Long contentId) {

        logger.info("Deleting educational content ID: {}", contentId);

        contentService.deleteContent(contentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update content status", description = "Activates or deactivates educational content (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Content not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PatchMapping("/{contentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EducationalContentResponseDTO> updateContentStatus(
            @Parameter(description = "Content ID", required = true)
            @PathVariable Long contentId,
            @Parameter(description = "Active status", required = true)
            @RequestParam boolean isActive) {

        logger.info("Updating content status - ID: {}, Active: {}", contentId, isActive);

        EducationalContentResponseDTO response = contentService.updateContentStatus(contentId, isActive);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all active content", description = "Retrieves all active educational content")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EducationalContentResponseDTO>> getAllActiveContent(
            @Parameter(description = "User ID for progress tracking (optional)")
            @RequestParam(required = false) Long userId) {

        logger.debug("Fetching all active educational content");

        List<EducationalContentResponseDTO> content;
        if (userId != null) {
            content = contentService.getAllActiveContentWithProgress(userId);
        } else {
            content = contentService.getAllActiveContent();
        }

        return ResponseEntity.ok(content);
    }

    @Operation(summary = "Get content by category", description = "Retrieves educational content by category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid category"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EducationalContentResponseDTO>> getContentByCategory(
            @Parameter(description = "Content category", required = true)
            @PathVariable String category,
            @Parameter(description = "User ID for progress tracking (optional)")
            @RequestParam(required = false) Long userId) {

        logger.debug("Fetching educational content by category: {}", category);

        try {
            ContentCategory contentCategory = ContentCategory.valueOf(category.toUpperCase());
            List<EducationalContentResponseDTO> content;

            if (userId != null) {
                content = contentService.getContentByCategoryWithProgress(contentCategory, userId);
            } else {
                content = contentService.getContentByCategory(contentCategory);
            }

            return ResponseEntity.ok(content);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get content by difficulty level", description = "Retrieves educational content by difficulty level")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid difficulty level"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/difficulty/{difficultyLevel}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EducationalContentResponseDTO>> getContentByDifficultyLevel(
            @Parameter(description = "Difficulty level", required = true)
            @PathVariable String difficultyLevel) {

        logger.debug("Fetching educational content by difficulty level: {}", difficultyLevel);

        try {
            DifficultyLevel difficulty = DifficultyLevel.valueOf(difficultyLevel.toUpperCase());
            List<EducationalContentResponseDTO> content = contentService.getContentByDifficultyLevel(difficulty);
            return ResponseEntity.ok(content);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get content by type", description = "Retrieves educational content by content type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid content type"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/type/{contentType}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EducationalContentResponseDTO>> getContentByType(
            @Parameter(description = "Content type", required = true)
            @PathVariable String contentType) {

        logger.debug("Fetching educational content by type: {}", contentType);

        try {
            ContentType type = ContentType.valueOf(contentType.toUpperCase());
            List<EducationalContentResponseDTO> content = contentService.getContentByType(type);
            return ResponseEntity.ok(content);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get featured content", description = "Retrieves featured educational content")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Featured content retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/featured")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EducationalContentResponseDTO>> getFeaturedContent(
            @Parameter(description = "User ID for progress tracking (optional)")
            @RequestParam(required = false) Long userId) {

        logger.debug("Fetching featured educational content");

        List<EducationalContentResponseDTO> content;
        if (userId != null) {
            content = contentService.getFeaturedContentWithProgress(userId);
        } else {
            content = contentService.getFeaturedContent();
        }

        return ResponseEntity.ok(content);
    }

    @Operation(summary = "Search content by title", description = "Searches educational content by title")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/search/title")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EducationalContentResponseDTO>> searchContentByTitle(
            @Parameter(description = "Title search term", required = true)
            @RequestParam String title) {

        logger.debug("Searching educational content by title: {}", title);

        List<EducationalContentResponseDTO> content = contentService.searchContentByTitle(title);
        return ResponseEntity.ok(content);
    }

    @Operation(summary = "Search content by author", description = "Searches educational content by author")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/search/author")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EducationalContentResponseDTO>> searchContentByAuthor(
            @Parameter(description = "Author search term", required = true)
            @RequestParam String author) {

        logger.debug("Searching educational content by author: {}", author);

        List<EducationalContentResponseDTO> content = contentService.searchContentByAuthor(author);
        return ResponseEntity.ok(content);
    }

    @Operation(summary = "Search content by tag", description = "Searches educational content by tag")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/search/tag")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EducationalContentResponseDTO>> searchContentByTag(
            @Parameter(description = "Tag search term", required = true)
            @RequestParam String tag) {

        logger.debug("Searching educational content by tag: {}", tag);

        List<EducationalContentResponseDTO> content = contentService.searchContentByTag(tag);
        return ResponseEntity.ok(content);
    }

    @Operation(summary = "Get content by duration range", description = "Retrieves educational content within duration range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid duration range"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/duration-range")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EducationalContentResponseDTO>> getContentByDurationRange(
            @Parameter(description = "Minimum duration in minutes", required = true)
            @RequestParam Integer minDuration,
            @Parameter(description = "Maximum duration in minutes", required = true)
            @RequestParam Integer maxDuration) {

        logger.debug("Fetching educational content by duration range: {} - {}", minDuration, maxDuration);

        if (minDuration < 0 || maxDuration < 0 || minDuration > maxDuration) {
            return ResponseEntity.badRequest().build();
        }

        List<EducationalContentResponseDTO> content = contentService.getContentByDurationRange(minDuration, maxDuration);
        return ResponseEntity.ok(content);
    }

    @Operation(summary = "Get content by minimum rating", description = "Retrieves educational content with minimum rating")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid rating"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/rating")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EducationalContentResponseDTO>> getContentByMinimumRating(
            @Parameter(description = "Minimum rating", required = true)
            @RequestParam Double minRating) {

        logger.debug("Fetching educational content with minimum rating: {}", minRating);

        if (minRating < 0.0 || minRating > 5.0) {
            return ResponseEntity.badRequest().build();
        }

        List<EducationalContentResponseDTO> content = contentService.getContentByMinimumRating(minRating);
        return ResponseEntity.ok(content);
    }

    @Operation(summary = "Get most popular content", description = "Retrieves most popular educational content")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Popular content retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid limit"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/popular")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EducationalContentResponseDTO>> getMostPopularContent(
            @Parameter(description = "Maximum number of results", required = false)
            @RequestParam(defaultValue = "10") int limit) {

        logger.debug("Fetching most popular educational content, limit: {}", limit);

        if (limit <= 0 || limit > 100) {
            return ResponseEntity.badRequest().build();
        }

        List<EducationalContentResponseDTO> content = contentService.getMostPopularContent(limit);
        return ResponseEntity.ok(content);
    }

    @Operation(summary = "Get content created after date", description = "Retrieves educational content created after a specific date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date format"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/created-after")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EducationalContentResponseDTO>> getContentCreatedAfter(
            @Parameter(description = "Date after which to search (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {

        logger.debug("Fetching educational content created after: {}", date);

        List<EducationalContentResponseDTO> content = contentService.getContentCreatedAfter(date);
        return ResponseEntity.ok(content);
    }

    @Operation(summary = "Increment view count", description = "Increments view count for content")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "View count incremented successfully"),
            @ApiResponse(responseCode = "404", description = "Content not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/{contentId}/view")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> incrementViewCount(
            @Parameter(description = "Content ID", required = true)
            @PathVariable Long contentId) {

        logger.debug("Incrementing view count for content ID: {}", contentId);

        contentService.incrementViewCount(contentId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get content statistics", description = "Retrieves content statistics (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getContentStatistics() {
        logger.debug("Fetching content statistics");

        Map<String, Object> statistics = contentService.getContentStatistics();
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Validate content exists", description = "Validates if content exists and is active")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation completed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{contentId}/validate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Boolean>> validateContentExists(
            @Parameter(description = "Content ID", required = true)
            @PathVariable Long contentId) {

        logger.debug("Validating content exists - Content ID: {}", contentId);

        boolean exists = contentService.validateContentExists(contentId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}