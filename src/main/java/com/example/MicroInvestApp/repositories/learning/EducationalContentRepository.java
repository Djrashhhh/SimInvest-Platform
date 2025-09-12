package com.example.MicroInvestApp.repositories.learning;

import com.example.MicroInvestApp.domain.enums.ContentCategory;
import com.example.MicroInvestApp.domain.enums.ContentType;
import com.example.MicroInvestApp.domain.enums.DifficultyLevel;
import com.example.MicroInvestApp.domain.learning.EducationalContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for EducationalContent entity operations
 * Provides CRUD operations and custom queries for educational content management
 */
@Repository
public interface EducationalContentRepository extends JpaRepository<EducationalContent, Long> {

    /**
     * Find all active educational content
     * @return List of active educational content
     */
    @Query("SELECT ec FROM EducationalContent ec WHERE ec.isActive = true")
    List<EducationalContent> findAllActive();

    /**
     * Find educational content by category (using native query with explicit casting)
     * @param category the content category as string
     * @return List of educational content
     */
    @Query(value = "SELECT * FROM ed_content ec WHERE ec.category = CAST(:category AS content_category_enum) AND ec.is_active = true",
            nativeQuery = true)
    List<EducationalContent> findByCategory(@Param("category") String category);

    /**
     * Find educational content by difficulty level (using native query with explicit casting)
     * @param difficultyLevel the difficulty level as string
     * @return List of educational content
     */
    @Query(value = "SELECT * FROM ed_content ec WHERE ec.content_difficulty = CAST(:difficultyLevel AS difficulty_level_enum) AND ec.is_active = true",
            nativeQuery = true)
    List<EducationalContent> findByDifficultyLevel(@Param("difficultyLevel") String difficultyLevel);

    /**
     * Find educational content by content type (using native query with explicit casting)
     * @param contentType the content type as string
     * @return List of educational content
     */
    @Query(value = "SELECT * FROM ed_content ec WHERE ec.content_type = CAST(:contentType AS content_type_enum) AND ec.is_active = true",
            nativeQuery = true)
    List<EducationalContent> findByContentType(@Param("contentType") String contentType);

    /**
     * Find featured educational content
     * @return List of featured educational content
     */
    @Query("SELECT ec FROM EducationalContent ec WHERE ec.isFeatured = true AND ec.isActive = true ORDER BY ec.createdAt DESC")
    List<EducationalContent> findFeaturedContent();

    /**
     * Find educational content by title containing (case insensitive)
     * @param title the title pattern
     * @return List of educational content
     */
    @Query("SELECT ec FROM EducationalContent ec WHERE LOWER(ec.title) LIKE LOWER(CONCAT('%', :title, '%')) AND ec.isActive = true")
    List<EducationalContent> findByTitleContainingIgnoreCase(@Param("title") String title);

    /**
     * Find educational content by author
     * @param author the author name
     * @return List of educational content
     */
    @Query("SELECT ec FROM EducationalContent ec WHERE LOWER(ec.author) LIKE LOWER(CONCAT('%', :author, '%')) AND ec.isActive = true")
    List<EducationalContent> findByAuthorContainingIgnoreCase(@Param("author") String author);

    /**
     * Find educational content by category and difficulty level (using native query)
     * @param category the content category as string
     * @param difficultyLevel the difficulty level as string
     * @return List of educational content
     */
    @Query(value = "SELECT * FROM ed_content ec WHERE ec.category = CAST(:category AS content_category_enum) AND ec.content_difficulty = CAST(:difficultyLevel AS difficulty_level_enum) AND ec.is_active = true",
            nativeQuery = true)
    List<EducationalContent> findByCategoryAndDifficultyLevel(@Param("category") String category,
                                                              @Param("difficultyLevel") String difficultyLevel);

    /**
     * Find educational content created after a specific date
     * @param date the date
     * @return List of educational content
     */
    @Query("SELECT ec FROM EducationalContent ec WHERE ec.createdAt >= :date AND ec.isActive = true ORDER BY ec.createdAt DESC")
    List<EducationalContent> findCreatedAfter(@Param("date") LocalDateTime date);

    /**
     * Find educational content with minimum rating
     * @param minRating the minimum rating
     * @return List of educational content
     */
    @Query("SELECT ec FROM EducationalContent ec WHERE ec.rating >= :minRating AND ec.isActive = true ORDER BY ec.rating DESC")
    List<EducationalContent> findByMinimumRating(@Param("minRating") Double minRating);

    /**
     * Find most popular educational content (by view count) - using native query for LIMIT
     * @param limit the maximum number of results
     * @return List of most popular educational content
     */
    @Query(value = "SELECT * FROM ed_content ec WHERE ec.is_active = true ORDER BY ec.view_count DESC LIMIT :limit",
            nativeQuery = true)
    List<EducationalContent> findMostPopular(@Param("limit") int limit);

    /**
     * Find educational content by duration range
     * @param minDuration minimum duration in minutes
     * @param maxDuration maximum duration in minutes
     * @return List of educational content
     */
    @Query("SELECT ec FROM EducationalContent ec WHERE ec.durationMinutes BETWEEN :minDuration AND :maxDuration AND ec.isActive = true")
    List<EducationalContent> findByDurationRange(@Param("minDuration") Integer minDuration,
                                                 @Param("maxDuration") Integer maxDuration);

    /**
     * Get total count of active educational content
     * @return count of active educational content
     */
    @Query("SELECT COUNT(ec) FROM EducationalContent ec WHERE ec.isActive = true")
    Long countActiveContent();

    /**
     * Get total count of content by category (using native query)
     * @param category the content category as string
     * @return count of content in category
     */
    @Query(value = "SELECT COUNT(*) FROM ed_content ec WHERE ec.category = CAST(:category AS content_category_enum) AND ec.is_active = true",
            nativeQuery = true)
    Long countByCategory(@Param("category") String category);

    /**
     * Get average rating of all content
     * @return average rating
     */
    @Query("SELECT AVG(ec.rating) FROM EducationalContent ec WHERE ec.isActive = true AND ec.rating > 0")
    Double getAverageRating();

    /**
     * Find educational content by tags containing
     * @param tag the tag to search for
     * @return List of educational content
     */
    @Query("SELECT ec FROM EducationalContent ec WHERE ec.tags LIKE CONCAT('%', :tag, '%') AND ec.isActive = true")
    List<EducationalContent> findByTagsContaining(@Param("tag") String tag);

    /**
     * Update content status
     * @param contentId the content ID
     * @param isActive the new status
     * @return number of updated records
     */
    @Modifying
    @Query("UPDATE EducationalContent ec SET ec.isActive = :isActive WHERE ec.edContentId = :contentId")
    int updateContentStatus(@Param("contentId") Long contentId, @Param("isActive") boolean isActive);

    /**
     * Increment view count for content
     * @param contentId the content ID
     * @return number of updated records
     */
    @Modifying
    @Query("UPDATE EducationalContent ec SET ec.viewCount = ec.viewCount + 1 WHERE ec.edContentId = :contentId")
    int incrementViewCount(@Param("contentId") Long contentId);

    /**
     * Update content rating
     * @param contentId the content ID
     * @param rating the new rating
     * @return number of updated records
     */
    @Modifying
    @Query("UPDATE EducationalContent ec SET ec.rating = :rating WHERE ec.edContentId = :contentId")
    int updateRating(@Param("contentId") Long contentId, @Param("rating") Double rating);
}