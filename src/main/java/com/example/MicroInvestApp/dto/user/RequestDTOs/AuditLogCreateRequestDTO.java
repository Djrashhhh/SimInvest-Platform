package com.example.MicroInvestApp.dto.user.RequestDTOs;

import com.example.MicroInvestApp.domain.enums.AuditEventType;
import com.example.MicroInvestApp.domain.enums.AuditEventCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public class AuditLogCreateRequestDTO {
    @NotBlank(message = "User agent is required")
    @JsonProperty("user_agent")
    private String userAgent;

    @NotBlank(message = "IP address is required")
    @JsonProperty("ip_address")
    private String ipAddress;

    @NotBlank(message = "Action is required")
    @Size(max = 255, message = "Action must not exceed 255 characters")
    private String action;

    @NotNull(message = "Audit event type is required")
    @JsonProperty("audit_event_type")
    private AuditEventType auditEventType;

    @NotNull(message = "Audit event category is required")
    @JsonProperty("audit_event_category")
    private AuditEventCategory auditEventCategory;

    private String details;

    @JsonProperty("success")
    private Boolean success = true;

    @JsonProperty("resource_id")
    private String resourceId;

    // Constructors, getters, and setters
    public AuditLogCreateRequestDTO() {}

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

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
}
