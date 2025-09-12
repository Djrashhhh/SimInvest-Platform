package com.example.MicroInvestApp.util;

import com.example.MicroInvestApp.domain.user.AuditLog;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.AuditLogResponseDTO;
import com.example.MicroInvestApp.dto.user.RequestDTOs.AuditLogCreateRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLog toEntity(AuditLogCreateRequestDTO dto) {
        AuditLog auditLog = new AuditLog();

        auditLog.setUserAgent(dto.getUserAgent());
        auditLog.setIpAddress(dto.getIpAddress());
        auditLog.setAction(dto.getAction());
        auditLog.setAuditEventType(dto.getAuditEventType());
        auditLog.setAuditEventCategory(dto.getAuditEventCategory());
        auditLog.setDetails(dto.getDetails());
        auditLog.setSuccess(dto.getSuccess());
        auditLog.setResourceId(dto.getResourceId());

        return auditLog;
    }

    public AuditLogResponseDTO toResponseDTO(AuditLog auditLog) {
        AuditLogResponseDTO dto = new AuditLogResponseDTO();

        dto.setAuditLogId(auditLog.getAuditLogId());
        dto.setUserId(auditLog.getUserAccount().getUserId());
        dto.setUserAgent(auditLog.getUserAgent());
        dto.setIpAddress(auditLog.getIpAddress());
        dto.setAction(auditLog.getAction());
        dto.setTimestamp(auditLog.getTimestamp());
        dto.setAuditEventType(auditLog.getAuditEventType());
        dto.setAuditEventCategory(auditLog.getAuditEventCategory());
        dto.setDetails(auditLog.getDetails());
        dto.setSuccess(auditLog.getSuccess());
        dto.setResourceId(auditLog.getResourceId());

        // Set calculated fields
        dto.setSecurityEvent(auditLog.isSecurityEvent());
        dto.setRecentEvent(auditLog.isRecentEvent());

        return dto;
    }
}
