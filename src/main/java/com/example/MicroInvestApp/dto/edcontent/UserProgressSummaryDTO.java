//package com.example.MicroInvestApp.dto.edcontent;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//
//import java.util.Map;
//
//public class UserProgressSummaryDTO {
//    @JsonProperty("user_id")
//    private Long userId;
//
//    @JsonProperty("completed_count")
//    private Long completedCount;
//
//    @JsonProperty("in_progress_count")
//    private Long inProgressCount;
//
//    @JsonProperty("average_completion_percentage")
//    private Double averageCompletionPercentage;
//
//    @JsonProperty("learning_streak")
//    private Integer learningStreak;
//
//    @JsonProperty("status_counts")
//    private Map<String, Long> statusCounts;
//
//    // Constructors
//    public UserProgressSummaryDTO() {}
//
//    // Getters and Setters
//    public Long getUserId() { return userId; }
//    public void setUserId(Long userId) { this.userId = userId; }
//
//    public Long getCompletedCount() { return completedCount; }
//    public void setCompletedCount(Long completedCount) { this.completedCount = completedCount; }
//
//    public Long getInProgressCount() { return inProgressCount; }
//    public void setInProgressCount(Long inProgressCount) { this.inProgressCount = inProgressCount; }
//
//    public Double getAverageCompletionPercentage() { return averageCompletionPercentage; }
//    public void setAverageCompletionPercentage(Double averageCompletionPercentage) {
//        this.averageCompletionPercentage = averageCompletionPercentage;
//    }
//
//    public Integer getLearningStreak() { return learningStreak; }
//    public void setLearningStreak(Integer learningStreak) { this.learningStreak = learningStreak; }
//
//    public Map<String, Long> getStatusCounts() { return statusCounts; }
//    public void setStatusCounts(Map<String, Long> statusCounts) { this.statusCounts = statusCounts; }
//}
