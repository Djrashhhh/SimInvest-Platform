package com.example.MicroInvestApp.domain.learning;

import com.example.MicroInvestApp.domain.enums.ProgressStatus;
import com.example.MicroInvestApp.domain.user.UserAccount;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

@SuppressWarnings({ "serial", "deprecation",  })
@Entity
@Table(name = "user_progress")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })

public class UserProgress implements Serializable {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long userProgressId; // Unique identifier for the user progress

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount userAccount; // The user whose progress is being tracked

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ed_content_id", referencedColumnName = "ed_content_id")
    private EducationalContent educationalContent; // The educational content being tracked

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt; // The time when the user started the educational content


    @Column(name = "finished_at", nullable = false)
    private Instant finishedAt; // The time when the user finished the educational content

    @UpdateTimestamp
    @Column(name ="last_accessed_at", nullable = false)
    private LocalDateTime lastAccessedAt;

    @CreationTimestamp
    @Column(name ="created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name ="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name="user_rating", nullable = false)
    private Integer userRating;

    @Enumerated(EnumType.STRING)
    @Column(name = "progress_status", nullable = false)
    private ProgressStatus status; // Status of the user's progress (e.g., in-progress, completed, etc.)

    @Min(0) @Max(100)
    @Column(name = "completion_percentage")
    private Integer completionPercentage = 0;

    // Business logic method
    public void markAsCompleted() {
        this.status = ProgressStatus.NOT_STARTED;
        this.finishedAt = Instant.now();
        this.completionPercentage = 100;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UserProgress() {
        // Default constructor
    }

    public UserProgress(UserAccount userAccount, EducationalContent educationalContent, ProgressStatus status) {
        this.userAccount = userAccount;
        this.educationalContent = educationalContent;
        this.status = status;
    }

    // Getters and Setters
    public Long getUserProgressId() {
        return userProgressId;
    }
    public void setUserProgressId(Long userProgressId) {
        this.userProgressId = userProgressId;
    }
    public UserAccount getUserAccount() {
        return userAccount;
    }
    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }
    public EducationalContent getEducationalContent() {
        return educationalContent;
    }
    public void setEducationalContent(EducationalContent educationalContent) {
        this.educationalContent = educationalContent;
    }
    public Instant getStartedAt() {
        return startedAt;
    }
    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }
    public Instant getFinishedAt() {
        return finishedAt;
    }
    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }
    public ProgressStatus getProgressStatus() {
        return status;
    }
    public void setProgressStatus(ProgressStatus progressStatus) {
        this.status = status;
    }
    public Integer getCompletionPercentage() {
        return completionPercentage;
    }
    public void setCompletionPercentage(Integer completionPercentage) {
        if (completionPercentage < 0 || completionPercentage > 100) {
            throw new IllegalArgumentException("Completion percentage must be between 0 and 100.");
        }
        this.completionPercentage = completionPercentage;
    }
    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }
    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    public Integer getUserRating() { return userRating; }
    public void setUserRating(Integer userRating) { this.userRating = userRating; }

    @Override
    public String toString() {
        return "UserProgress{" +
                "userProgressId=" + userProgressId +
                ", userAccount=" + userAccount +
                ", educationalContent=" + educationalContent +
                ", startedAt=" + startedAt +
                ", finishedAt=" + finishedAt +
                ", progressStatus=" + status +
                '}';
    }


}
