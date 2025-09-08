package com.example.MicroInvestApp.domain.user;


import com.example.MicroInvestApp.domain.enums.AuditEventCategory;
import com.example.MicroInvestApp.domain.enums.AuditEventType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.Instant;

@SuppressWarnings({ "serial", "deprecation",  })
@Entity
@Table(name = "AuditLog", indexes = {
        @Index(name = "idx_audit_user_timestamp", columnList = "userId, timestamp"),
        @Index(name = "idx_audit_type_category", columnList = "audit_type, audit_category"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_ip", columnList = "ip_address")
})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })


public class AuditLog implements Serializable {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long auditLogId; // Unique identifier for the audit log entry

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private UserAccount userAccount; // The user associated with the audit log entry

    @NotBlank(message = "User agent cannot be null")
    @Column(name = "user_agent", nullable = false)
    private String userAgent; // The user agent string of the user's device

    @NotBlank(message = "IP address cannot be null")
    @Column(name = "ip_address", nullable = false)
    private String ipAddress; // The IP address of the user when the action was performed

    @NotBlank(message = "Action cannot be null")
    @Size(max = 255, message = "Action must not exceed 255 characters")
    @Column(name = "action", nullable = false)
    private String action; // The action performed by the user (e.g., login, logout, data change, etc.)


    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp; // The timestamp when the action was performed

    @Enumerated(EnumType.STRING)
    @Column(name = "audit_type", nullable = false)
    private AuditEventType auditEventType; // The type of audit event (e.g., login, logout, data change, etc.)


    @Enumerated(EnumType.STRING)
    @Column(name = "audit_category", nullable = false)
    private AuditEventCategory auditEventCategory; // The category of the audit event (e.g., security, data integrity, etc.)

    // NEW: Additional context/details about the action
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    // NEW: Success/failure status
    @Column(name = "success", nullable = false)
    private Boolean success = true;

    // NEW: Resource affected (e.g., portfolio ID, trade ID)
    @Column(name = "resource_id")
    private String resourceId;


    public AuditLog() {
        // Default constructor
    }

    public AuditLog(UserAccount userAccount, String userAgent, String ipAddress, String action, AuditEventType auditEventType, AuditEventCategory auditEventCategory, boolean success) {
        this.userAccount = userAccount;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
        this.action = action;
        this.auditEventType = auditEventType;
        this.auditEventCategory = auditEventCategory;
        this.success = success;
    }

    // Business logic methods
    public boolean isSecurityEvent() {
        return auditEventCategory == AuditEventCategory.SECURITY;
    }

    public boolean isRecentEvent() {
        return timestamp != null && timestamp.isAfter(Instant.now().minusSeconds(3600)); // Last hour
    }


    // Getters and Setters
    public Long getAuditLogId() {
        return auditLogId;
    }

    public void setAuditLogId(Long auditLogId) {
        this.auditLogId = auditLogId;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public AuditEventType getAuditEventType() {
        return auditEventType;
    }

    public void setAuditEventType(AuditEventType auditEventType) {
        this.auditEventType = auditEventType;
    }

    public AuditEventCategory getAuditEventCategory() {
        return auditEventCategory;
    }

    public void setAuditEventCategory(AuditEventCategory auditEventCategory) {
        this.auditEventCategory = auditEventCategory;
    }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }



   @Override
    public String toString() {
        return "AuditLog{" +
                "auditLogId=" + auditLogId +
                ", userAccount=" + userAccount +
                ", userAgent='" + userAgent + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", action='" + action + '\'' +
                ", timestamp=" + timestamp +
                ", auditEventType=" + auditEventType +
                ", auditEventCategory=" + auditEventCategory +
                ", details='" + details + '\'' +
                ", success=" + success +
                ", resourceId='" + resourceId + '\'' +
                '}';
    }


}
