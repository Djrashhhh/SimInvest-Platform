package com.example.MicroInvestApp.service.user;

import com.example.MicroInvestApp.domain.enums.AuditEventType;

import com.example.MicroInvestApp.dto.user.RequestDTOs.AuditLogCreateRequestDTO;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.AuditLogResponseDTO;


import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AuditLogService  {
    AuditLogResponseDTO createAuditLog(Long userId, AuditLogCreateRequestDTO auditRequest);
    Optional<AuditLogResponseDTO> getAuditLogById(Long auditLogId);
    List<AuditLogResponseDTO> getAuditLogsByUserId(Long userId);
    List<AuditLogResponseDTO> getAuditLogsByUserIdAndTimeRange(Long userId, Instant startTime, Instant endTime);

    List<AuditLogResponseDTO> getSecurityEvents(Instant fromTime);
    List<AuditLogResponseDTO> getFailedOperations(Instant fromTime);
    List<AuditLogResponseDTO> getRecentUserActivity(Long userId, int limit);

    long countEventsByUserAndType(Long userId, AuditEventType eventType);

    // Convenience methods for common audit operations
    void logUserLogin(Long userId, String ipAddress, String userAgent, boolean success);
    void logUserLogout(Long userId, String ipAddress, String userAgent);
    void logDataChange(Long userId, String action, String resourceId, String details);
    void logSecurityEvent(Long userId, String action, String ipAddress, String details, boolean success);
}
