package com.example.MicroInvestApp.dto.edcontent;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProgressUpdateRequestDTO {
    @JsonProperty("status")
    private String status;

    @JsonProperty("completion_percentage")
    private Integer completionPercentage;


    @JsonProperty("user_rating")
    private Integer userRating;


    // Constructors
    public ProgressUpdateRequestDTO() {}

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(Integer completionPercentage) { this.completionPercentage = completionPercentage; }


    public Integer getUserRating() { return userRating; }
    public void setUserRating(Integer userRating) { this.userRating = userRating; }

}
