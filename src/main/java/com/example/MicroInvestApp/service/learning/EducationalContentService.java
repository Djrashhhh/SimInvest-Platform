package com.example.MicroInvestApp.service.learning;

import com.example.MicroInvestApp.domain.enums.ContentCategory;
import com.example.MicroInvestApp.domain.enums.ContentType;
import com.example.MicroInvestApp.domain.enums.DifficultyLevel;
import com.example.MicroInvestApp.dto.edcontent.EducationalContentResponseDTO;
import com.example.MicroInvestApp.dto.edcontent.CreateEducationalContentRequestDTO;
import com.example.MicroInvestApp.dto.edcontent.UpdateEducationalContentRequestDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EducationalContentService {

    /**
     * Create new educational content
     * @param request the create request DTO
     * @return created educational content response DTO
     */
    EducationalContentResponseDTO createContent(CreateEducationalContentRequestDTO request);

    /**
     * Get educational content by ID
     * @param contentId the content ID
     * @return Optional educational content response DTO
     */
    Optional<EducationalContentResponseDTO> getContentById(Long contentId);

    /**
     * Get educational content by ID with user progress
     * @param contentId the content ID
     * @param userId the user ID
     * @return Optional educational content response DTO with user progress
     */
    Optional<EducationalContentResponseDTO> getContentByIdWithProgress(Long contentId, Long userId);

    /**
     * Update educational content
     * @param contentId the content ID
     * @param request the update request DTO
     * @return updated educational content response DTO
     */
    EducationalContentResponseDTO updateContent(Long contentId, UpdateEducationalContentRequestDTO request);

    /**
     * Delete educational content
     * @param contentId the content ID
     */
    void deleteContent(Long contentId);

    /**
     * Update content status (active/inactive)
     * @param contentId the content ID
     * @param isActive the active status
     * @return updated educational content response DTO
     */
    EducationalContentResponseDTO updateContentStatus(Long contentId, boolean isActive);

    /**
     * Get all active educational content
     * @return List of active educational content
     */
    List<EducationalContentResponseDTO> getAllActiveContent();

    /**
     * Get all active educational content with user progress
     * @param userId the user ID
     * @return List of active educational content with user progress
     */
    List<EducationalContentResponseDTO> getAllActiveContentWithProgress(Long userId);

    /**
     * Get educational content by category
     * @param category the content category
     * @return List of educational content
     */
    List<EducationalContentResponseDTO> getContentByCategory(ContentCategory category);

    /**
     * Get educational content by category with user progress
     * @param category the content category
     * @param userId the user ID
     * @return List of educational content with user progress
     */
    List<EducationalContentResponseDTO> getContentByCategoryWithProgress(ContentCategory category, Long userId);

    /**
     * Get educational content by difficulty level
     * @param difficultyLevel the difficulty level
     * @return List of educational content
     */
    List<EducationalContentResponseDTO> getContentByDifficultyLevel(DifficultyLevel difficultyLevel);

    /**
     * Get educational content by content type
     * @param contentType the content type
     * @return List of educational content
     */
    List<EducationalContentResponseDTO> getContentByType(ContentType contentType);

    /**
     * Get featured educational content
     * @return List of featured educational content
     */
    List<EducationalContentResponseDTO> getFeaturedContent();

    /**
     * Get featured educational content with user progress
     * @param userId the user ID
     * @return List of featured educational content with user progress
     */
    List<EducationalContentResponseDTO> getFeaturedContentWithProgress(Long userId);

    /**
     * Search educational content by title
     * @param title the title search term
     * @return List of matching educational content
     */
    List<EducationalContentResponseDTO> searchContentByTitle(String title);

    /**
     * Search educational content by author
     * @param author the author search term
     * @return List of matching educational content
     */
    List<EducationalContentResponseDTO> searchContentByAuthor(String author);

    /**
     * Get educational content by category and difficulty level
     * @param category the content category
     * @param difficultyLevel the difficulty level
     * @return List of educational content
     */
    List<EducationalContentResponseDTO> getContentByCategoryAndDifficulty(ContentCategory category, DifficultyLevel difficultyLevel);

    /**
     * Get educational content created after date
     * @param date the date
     * @return List of educational content
     */
    List<EducationalContentResponseDTO> getContentCreatedAfter(LocalDateTime date);

    /**
     * Get educational content with minimum rating
     * @param minRating the minimum rating
     * @return List of educational content
     */
    List<EducationalContentResponseDTO> getContentByMinimumRating(Double minRating);

    /**
     * Get most popular educational content
     * @param limit the maximum number of results
     * @return List of most popular educational content
     */
    List<EducationalContentResponseDTO> getMostPopularContent(int limit);

    /**
     * Get educational content by duration range
     * @param minDuration minimum duration in minutes
     * @param maxDuration maximum duration in minutes
     * @return List of educational content
     */
    List<EducationalContentResponseDTO> getContentByDurationRange(Integer minDuration, Integer maxDuration);

    /**
     * Search educational content by tags
     * @param tag the tag to search for
     * @return List of matching educational content
     */
    List<EducationalContentResponseDTO> searchContentByTag(String tag);

    /**
     * Increment view count for content
     * @param contentId the content ID
     */
    void incrementViewCount(Long contentId);

    /**
     * Update content rating
     * @param contentId the content ID
     * @param rating the new rating
     * @return updated educational content response DTO
     */
    EducationalContentResponseDTO updateContentRating(Long contentId, Double rating);

    /**
     * Get content statistics
     * @return Map containing various statistics
     */
    Map<String, Object> getContentStatistics();

    /**
     * Get content statistics by category
     * @return Map containing statistics by category
     */
    Map<ContentCategory, Long> getContentStatsByCategory();

    /**
     * Get total active content count
     * @return total count of active content
     */
    Long getTotalActiveContentCount();

    /**
     * Get average content rating
     * @return average rating across all content
     */
    Double getAverageContentRating();

    /**
     * Validate content exists and is active
     * @param contentId the content ID
     * @return true if content exists and is active
     */
    boolean validateContentExists(Long contentId);
}