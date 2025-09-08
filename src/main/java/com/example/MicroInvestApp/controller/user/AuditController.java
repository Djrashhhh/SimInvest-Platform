package com.example.MicroInvestApp.controller.user;

import com.example.MicroInvestApp.dto.user.ResponseDTOs.AuditLogResponseDTO;
import com.example.MicroInvestApp.service.user.AuditLogService;
import com.example.MicroInvestApp.domain.enums.AuditEventType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuditController {

    private final AuditLogService auditLogService;

    @Autowired
    public AuditController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/{auditLogId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditLogResponseDTO> getAuditLogById(@PathVariable Long auditLogId) {

        return auditLogService.getAuditLogById(auditLogId)
                .map(auditLog -> ResponseEntity.ok(auditLog))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponseDTO>> getUserAuditLogs(@PathVariable Long userId) {

        List<AuditLogResponseDTO> auditLogs = auditLogService.getAuditLogsByUserId(userId);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/users/{userId}/recent")
    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponseDTO>> getRecentUserActivity(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {

        List<AuditLogResponseDTO> recentActivity = auditLogService.getRecentUserActivity(userId, limit);
        return ResponseEntity.ok(recentActivity);
    }

    @GetMapping("/users/{userId}/range")
    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponseDTO>> getUserAuditLogsInRange(
            @PathVariable Long userId,
            @RequestParam String startTime,
            @RequestParam String endTime) {

        Instant start = Instant.parse(startTime);
        Instant end = Instant.parse(endTime);

        List<AuditLogResponseDTO> auditLogs = auditLogService.getAuditLogsByUserIdAndTimeRange(userId, start, end);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/security-events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponseDTO>> getSecurityEvents(
            @RequestParam(defaultValue = "24") int hoursBack) {

        Instant fromTime = Instant.now().minus(hoursBack, ChronoUnit.HOURS);
        List<AuditLogResponseDTO> securityEvents = auditLogService.getSecurityEvents(fromTime);
        return ResponseEntity.ok(securityEvents);
    }

    @GetMapping("/failed-operations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponseDTO>> getFailedOperations(
            @RequestParam(defaultValue = "24") int hoursBack) {

        Instant fromTime = Instant.now().minus(hoursBack, ChronoUnit.HOURS);
        List<AuditLogResponseDTO> failedOps = auditLogService.getFailedOperations(fromTime);
        return ResponseEntity.ok(failedOps);
    }
}
