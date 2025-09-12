package com.example.MicroInvestApp.dto.edcontent;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public class UpdateEducationalContentRequestDTO {
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    @JsonProperty("title")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @JsonProperty("description")
    private String description;

    @URL(message = "Content URL must be valid")
    @JsonProperty("content_url")
    private String contentUrl;

    @Min(value = 1, message = "Duration must be at least 1 minute")
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

    @JsonProperty("is_active")
    private Boolean isActive;

    // Constructors
    public UpdateEducationalContentRequestDTO() {}

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

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

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

}
