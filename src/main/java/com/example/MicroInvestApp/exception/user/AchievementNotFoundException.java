package com.example.MicroInvestApp.exception.user;

public class AchievementNotFoundException extends RuntimeException {
    public AchievementNotFoundException(String message) {
        super(message);
    }
    public AchievementNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public AchievementNotFoundException(Long achievementId) {
        super("Achievement not found with ID: " + achievementId);
    }
}
