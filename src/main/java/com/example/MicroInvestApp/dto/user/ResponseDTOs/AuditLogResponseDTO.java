package com.example.MicroInvestApp.dto.user.ResponseDTOs;

import com.example.MicroInvestApp.domain.enums.AuditEventType;
import com.example.MicroInvestApp.domain.enums.AuditEventCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public class AuditLogResponseDTO {
    @JsonProperty("audit_log_id")
    private Long auditLogId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("user_agent")
    private String userAgent;

    @JsonProperty("ip_address")
    private String ipAddress;

    private String action;
    private Instant timestamp;

    @JsonProperty("audit_event_type")
    private AuditEventType auditEventType;

    @JsonProperty("audit_event_category")
    private AuditEventCategory auditEventCategory;

    private String details;
    private Boolean success;

    @JsonProperty("resource_id")
    private String resourceId;

    @JsonProperty("is_security_event")
    private boolean isSecurityEvent;

    @JsonProperty("is_recent_event")
    private boolean isRecentEvent;

    // Constructors, getters, and setters
    public AuditLogResponseDTO() {}

    // All getters and setters...
    public Long getAuditLogId() { return auditLogId; }
    public void setAuditLogId(Long auditLogId) { this.auditLogId = auditLogId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public AuditEventType getAuditEventType() { return auditEventType; }
    public void setAuditEventType(AuditEventType auditEventType) { this.auditEventType = auditEventType; }

    public AuditEventCategory getAuditEventCategory() { return auditEventCategory; }
    public void setAuditEventCategory(AuditEventCategory auditEventCategory) { this.auditEventCategory = auditEventCategory; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public boolean isSecurityEvent() { return isSecurityEvent; }
    public void setSecurityEvent(boolean securityEvent) { isSecurityEvent = securityEvent; }

    public boolean isRecentEvent() { return isRecentEvent; }
    public void setRecentEvent(boolean recentEvent) { isRecentEvent = recentEvent; }
}
