package com.example.MicroInvestApp.dto.user.ResponseDTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.Instant;

public class UserSessionResponseDTO {

    @JsonProperty("user_session_id")
    private Long userSessionId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("is_active")
    private boolean isActive;

    @JsonProperty("ip_address")
    private String ipAddress;

    @JsonProperty("user_agent")
    private String userAgent;

    @JsonProperty("login_timestamp")
    private Instant loginTimestamp;

    @JsonProperty("logout_timestamp")
    private Instant logoutTimestamp;

    @JsonProperty("is_authenticated")
    private boolean isAuthenticated;

    @JsonProperty("expires_at")
    private Instant expiresAt;

    @JsonProperty("last_activity")
    private Instant lastActivity;

    @JsonProperty("is_expired")
    private boolean isExpired;

    @JsonProperty("is_valid")
    private boolean isValid;

    @JsonProperty("session_duration_minutes")
    private long sessionDurationMinutes;

    @JsonProperty("is_long_running")
    private boolean isLongRunning;

    // Constructors, getters, and setters
    public UserSessionResponseDTO() {}

    // All getters and setters...
    public Long getUserSessionId() { return userSessionId; }
    public void setUserSessionId(Long userSessionId) { this.userSessionId = userSessionId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public Instant getLoginTimestamp() { return loginTimestamp; }
    public void setLoginTimestamp(Instant loginTimestamp) { this.loginTimestamp = loginTimestamp; }

    public Instant getLogoutTimestamp() { return logoutTimestamp; }
    public void setLogoutTimestamp(Instant logoutTimestamp) { this.logoutTimestamp = logoutTimestamp; }

    public boolean isAuthenticated() { return isAuthenticated; }
    public void setAuthenticated(boolean authenticated) { isAuthenticated = authenticated; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getLastActivity() { return lastActivity; }
    public void setLastActivity(Instant lastActivity) { this.lastActivity = lastActivity; }

    public boolean isExpired() { return isExpired; }
    public void setExpired(boolean expired) { isExpired = expired; }

    public boolean isValid() { return isValid; }
    public void setValid(boolean valid) { isValid = valid; }

    public long getSessionDurationMinutes() { return sessionDurationMinutes; }
    public void setSessionDurationMinutes(long sessionDurationMinutes) { this.sessionDurationMinutes = sessionDurationMinutes; }

    public boolean isLongRunning() { return isLongRunning; }
    public void setLongRunning(boolean longRunning) { isLongRunning = longRunning; }
}
