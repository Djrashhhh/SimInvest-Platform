package com.example.MicroInvestApp.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

@SuppressWarnings({ "serial", "deprecation",  })
@Entity
@Table(name = "UserSession", indexes = {
        @Index(name = "idx_session_token", columnList = "session_token"),
        @Index(name = "idx_session_user_active", columnList = "userId, is_active"),
        @Index(name = "idx_session_login_timestamp", columnList = "login_timestamp"),
        @Index(name = "idx_session_ip", columnList = "ip_address")
})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })

public class UserSession implements Serializable {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long userSessionId; // Unique identifier for the user session

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount userAccount; // The user associated with the session

    @Column(name = "session_token", nullable = false, unique = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)   // Prevents session token from being serialized in responses
    private String sessionToken; // Unique token for the session

    @Column(name = "is_active", nullable = false)
    private boolean isActive; // Indicates if the session is currently active

    @Column(name = "ip_address", nullable = false)
    private String ipAddress; // The IP address from which the session was initiated

    @Column(name = "user_agent", nullable = false)
    private String userAgent; // The user agent string of the user's device

    //login timestamp
    @NotNull(message = "Login timestamp cannot be null")
    @Column(name = "login_timestamp", nullable = false)
    private Instant loginTimestamp; // The timestamp when the user logged in

    //logout timestamp
    @NotNull(message = "Logout timestamp cannot be null")
    @Column(name = "logout_timestamp")
    private Instant logoutTimestamp; // The timestamp when the user logged out, null if still active


    //is user authenticated
    @NotNull(message = "Authentication status cannot be null")
    @Column(name = "is_authenticated", nullable = false)
    private boolean isAuthenticated; // Indicates if the user has been authenticated during this session

    // NEW: Session expiry time
    @Column(name = "expires_at")
    private Instant expiresAt;

    // NEW: Last activity timestamp for session management
    @Column(name = "last_activity")
    private Instant lastActivity;

    // constructors
    public UserSession() {
        this.loginTimestamp = Instant.now();
        this.lastActivity = Instant.now();
        this.isActive = true;
        this.isAuthenticated = false;
    }

    public UserSession(UserAccount userAccount, String sessionToken, String ipAddress, String userAgent) {
        this();
        this.userAccount = userAccount;
        this.sessionToken = sessionToken;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.expiresAt = Instant.now().plus(Duration.ofHours(24)); // 24-hour default expiry
    }


    // Business logic methods
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return isActive && isAuthenticated && !isExpired();
    }

    public void updateActivity() {
        this.lastActivity = Instant.now();
    }

    public void logout() {
        this.isActive = false;
        this.logoutTimestamp = Instant.now();
    }

    public Duration getSessionDuration() {
        Instant endTime = logoutTimestamp != null ? logoutTimestamp : Instant.now();
        return Duration.between(loginTimestamp, endTime);
    }

    public boolean isLongRunning() {
        return getSessionDuration().toHours() > 8; // Sessions longer than 8 hours
    }


    // Getters and Setters
    public Long getUserSessionId() {
        return userSessionId;
    }
    public void setUserSessionId(Long userSessionId) {
        this.userSessionId = userSessionId;
    }
    public UserAccount getUserAccount() {
        return userAccount;
    }
    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }
    public String getSessionToken() {
        return sessionToken;
    }
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        isActive = active;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public String getUserAgent() {
        return userAgent;
    }
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    public Instant getLoginTimestamp() {
        return loginTimestamp;
    }
    public void setLoginTimestamp(Instant loginTimestamp) {
        this.loginTimestamp = loginTimestamp;
    }
    public Instant getLogoutTimestamp() {
        return logoutTimestamp;
    }
    public void setLogoutTimestamp(Instant logoutTimestamp) {
        this.logoutTimestamp = logoutTimestamp;
    }
    public boolean isAuthenticated() {
        return isAuthenticated;
    }
    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getLastActivity() { return lastActivity; }
    public void setLastActivity(Instant lastActivity) { this.lastActivity = lastActivity; }

    // FIXED: Consistent boolean getter naming
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsAuthenticated() { return isAuthenticated; }
    public void setIsAuthenticated(Boolean isAuthenticated) { this.isAuthenticated = isAuthenticated; }

    @Override
    public String toString() {
        return "UserSession{" +
                "userSessionId=" + userSessionId +
                ", userAccount=" + userAccount +
                ", sessionToken='" + sessionToken + '\'' +
                ", isActive=" + isActive +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", loginTimestamp=" + loginTimestamp +
                ", logoutTimestamp=" + logoutTimestamp +
                ", isAuthenticated=" + isAuthenticated +
                ", expiresAt=" + expiresAt +
                ", lastActivity=" + lastActivity +
                '}';
    }
}





