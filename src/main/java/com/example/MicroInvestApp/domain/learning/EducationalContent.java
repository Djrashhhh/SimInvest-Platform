package com.example.MicroInvestApp.domain.learning;

import com.example.MicroInvestApp.domain.enums.ContentCategory;
import com.example.MicroInvestApp.domain.enums.DifficultyLevel;
import com.example.MicroInvestApp.domain.enums.ContentType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

@SuppressWarnings({ "serial", "deprecation",  })
@Entity
@Table(name = "ed_content", indexes = {
        @Index(name = "idx_content_type", columnList = "content_type"),
        @Index(name = "idx_difficulty", columnList = "content_difficulty"),
        @Index(name = "idx_published", columnList = "published_at")
})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class EducationalContent implements Serializable {

//    @Id
//    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
//    @Column(name = "ed_content_id", updatable = false, nullable = false)
//    private Long edContentId; // Unique identifier for the educational content
//
//    @NotBlank(message = "Title cannot be null")
//    @Size(max = 200, message = "Title cannot exceed 200 characters")
//    @Column(name = "title", nullable = false, length = 200)
//    private String title; // Title of the educational content
//
//    @NotBlank(message = "Description cannot be null")
//    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
//    @Column(name = "description", nullable = false, length = 1000)
//    private String description; // Description of the educational content
//
//    @Enumerated(EnumType.STRING)
//    @NotNull(message = "Content category cannot be null")
//    @Column(name = "category", nullable = false, columnDefinition = "content_category_enum")
//    private ContentCategory category;
//
//    @NotBlank(message = "Content URL cannot be null")
//    @URL(message = "Content URL must be valid")
//    @Column(name = "external_url", nullable = false)
//    private String contentUrl; // URL to the educational content (e.g., video, article, etc.)
//
//    @Enumerated(EnumType.STRING)
//    @NotNull(message = "Content type cannot be null")
//    @Column(name = "content_type", nullable = false, columnDefinition = "content_type_enum")
//    private ContentType contentType; // Type of content (e.g., video, article, etc.)
//
//    @Enumerated(EnumType.STRING)
//    @NotNull(message = "Content difficulty cannot be null")
//    @Column(name = "content_difficulty", nullable = false, columnDefinition = "difficulty_level_enum")
//    private DifficultyLevel difficultyLevel; // Difficulty level of the content (e.g., beginner, intermediate, advanced)
//
//    @Min(value = 1, message = "Duration must be at least 1 minute")
//    @Column(name = "duration_minutes")
//    private Integer durationMinutes;
//
//    @Column(name = "is_active", nullable = false)
//    private boolean isActive = true;
//
//    @Column(name="author", nullable = false)
//    private String author;
//
//    @Column(name="source", nullable = false)
//    private String source;
//
//    @Column(name="tags", nullable = false)
//    private String tags; // JSON string for tags
//
//    @Column(name="prerequisites", nullable = false)
//    private String prerequisites; // JSON string for prerequisite content IDs
//
//    @Column(name="is_featured", nullable = false)
//    private Boolean isFeatured;
//
//    @Column(name="view_count", nullable = false)
//    private Long viewCount;
//
//    @Column(name="rating", nullable = false)
//    private Double rating;
//
//    @Column(name="created_at", updatable = false)
//    @CreationTimestamp
//    private LocalDateTime createdAt;
//
//    @Column(name="updated_at", nullable = false)
//    @UpdateTimestamp
//    private LocalDateTime updatedAt;
//
//
//    // Constructors
//    public EducationalContent() {
//        this.isActive = true;
//        this.isFeatured = false;
//        this.viewCount = 0L;
//        this.rating = 0.0;
//        this.createdAt = LocalDateTime.now();
//        this.updatedAt = LocalDateTime.now();
//    }
//
//    public EducationalContent(String title, String description, ContentCategory category,
//                              DifficultyLevel difficultyLevel, ContentType contentType, String contentUrl) {
//        this();
//        this.title = title;
//        this.description = description;
//        this.category = category;
//        this.difficultyLevel = difficultyLevel;
//        this.contentType = contentType;
//        this.contentUrl = contentUrl;
//    }
//
//    // Getters and Setters
//    public Long getContentId() { return edContentId; }
//    public void setContentId(Long edContentId) { this.edContentId = edContentId; }
//
//    public String getTitle() { return title; }
//    public void setTitle(String title) { this.title = title; }
//
//    public String getDescription() { return description; }
//    public void setDescription(String description) { this.description = description; }
//
//    public ContentCategory getCategory() { return category; }
//    public void setCategory(ContentCategory category) { this.category = category; }
//
//    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
//    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }
//
//    public ContentType getContentType() { return contentType; }
//    public void setContentType(ContentType contentType) { this.contentType = contentType; }
//
//    public String getContentUrl() { return contentUrl; }
//    public void setContentUrl(String contentUrl) { this.contentUrl = contentUrl; }
//
//    public Integer getDurationMinutes() { return durationMinutes; }
//    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
//
//    public String getAuthor() { return author; }
//    public void setAuthor(String author) { this.author = author; }
//
//    public String getSource() { return source; }
//    public void setSource(String source) { this.source = source; }
//
//    public String getTags() { return tags; }
//    public void setTags(String tags) { this.tags = tags; }
//
//    public String getPrerequisites() { return prerequisites; }
//    public void setPrerequisites(String prerequisites) { this.prerequisites = prerequisites; }
//
//    public Boolean getIsFeatured() { return isFeatured; }
//    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
//
//    public Boolean getIsActive() { return isActive; }
//    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
//
//    public Long getViewCount() { return viewCount; }
//    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }
//
//    public Double getRating() { return rating; }
//    public void setRating(Double rating) { this.rating = rating; }
//
//    public LocalDateTime getCreatedAt() { return createdAt; }
//    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
//
//    public LocalDateTime getUpdatedAt() { return updatedAt; }
//    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
//
//
//
//
//
//@Override
//    public String toString() {
//    return "EducationalContent{" +
//            "edContentId=" + edContentId +
//            ", title='" + title + '\'' +
//            ", description='" + description + '\'' +
//            ", contentUrl='" + contentUrl + '\'' +
//            ", contentType=" + contentType +
//            ", difficultyLevel=" + difficultyLevel +
//            ", durationMinutes=" + durationMinutes +
//            ", isActive=" + isActive +
//            ", author='" + author + '\'' +
//            ", source='" + source + '\'' +
//            ", tags='" + tags + '\'' +
//            ", prerequisites='" + prerequisites + '\'' +
//            ", isFeatured=" + isFeatured +
//            ", viewCount=" + viewCount +
//            ", rating=" + rating +
//            ", createdAt=" + createdAt +
//            ", updatedAt=" + updatedAt +
//            '}';
//}
}
