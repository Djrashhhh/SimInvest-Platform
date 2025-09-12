package com.example.MicroInvestApp.dto.edcontent;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public class EducationalContentResponseDTO {

    @JsonProperty("content_id")
    private Long edContentId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("category")
    private String category;

    @JsonProperty("difficulty_level")
    private String difficultyLevel;

    @JsonProperty("content_type")
    private String contentType;

    @JsonProperty("external_url")
    private String contentUrl;

    @JsonProperty("duration_minutes")
    private Integer durationMinutes;

    @JsonProperty("author")
    private String author;

    @JsonProperty("source")
    private String source;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("prerequisites")
    private List<Long> prerequisites;

    @JsonProperty("is_featured")
    private Boolean isFeatured;

    @JsonProperty("view_count")
    private Long viewCount;

    @JsonProperty("rating")
    private Double rating;

    //@JsonProperty("user_progress")
    //private UserProgressDTO userProgress;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public EducationalContentResponseDTO() {}

    // Getters and Setters
    public Long getContentId() { return edContentId; }
    public void setContentId(Long contentId) { this.edContentId = edContentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getContentUrl() { return contentUrl; }
    public void setContentUrl(String contentUrl) { this.contentUrl = contentUrl; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<Long> getPrerequisites() { return prerequisites; }
    public void setPrerequisites(List<Long> prerequisites) { this.prerequisites = prerequisites; }

    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }

    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    //public UserProgressDTO getUserProgress() { return userProgress; }
    //public void setUserProgress(UserProgressDTO userProgress) { this.userProgress = userProgress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
