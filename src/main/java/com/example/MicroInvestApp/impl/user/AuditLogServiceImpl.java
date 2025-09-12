package com.example.MicroInvestApp.impl.user;

import com.example.MicroInvestApp.domain.user.AuditLog;
import com.example.MicroInvestApp.domain.user.UserAccount;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.AuditLogResponseDTO;
import com.example.MicroInvestApp.dto.user.RequestDTOs.AuditLogCreateRequestDTO;
import com.example.MicroInvestApp.repositories.user.AuditLogRepository;
import com.example.MicroInvestApp.repositories.user.UserAccountRepository;
import com.example.MicroInvestApp.service.user.AuditLogService;
import com.example.MicroInvestApp.util.AuditLogMapper;
import com.example.MicroInvestApp.exception.user.UserNotFoundException;
import com.example.MicroInvestApp.domain.enums.AuditEventType;
import com.example.MicroInvestApp.domain.enums.AuditEventCategory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuditLogServiceImpl implements AuditLogService{
    private final AuditLogRepository auditLogRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuditLogMapper auditLogMapper;

    @Autowired
    public AuditLogServiceImpl(AuditLogRepository auditLogRepository,
                           UserAccountRepository userAccountRepository,
                           AuditLogMapper auditLogMapper) {
        this.auditLogRepository = auditLogRepository;
        this.userAccountRepository = userAccountRepository;
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    public AuditLogResponseDTO createAuditLog(Long userId, AuditLogCreateRequestDTO auditRequest) {
        UserAccount userAccount = userAccountRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        AuditLog auditLog = auditLogMapper.toEntity(auditRequest);
        auditLog.setUserAccount(userAccount);
        auditLog.setTimestamp(Instant.now());

        AuditLog savedAuditLog = auditLogRepository.save(auditLog);
        return auditLogMapper.toResponseDTO(savedAuditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuditLogResponseDTO> getAuditLogById(Long auditLogId) {
        return auditLogRepository.findById(auditLogId)
                .map(auditLogMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDTO> getAuditLogsByUserId(Long userId) {
        return auditLogRepository.findByUserAccountUserIdOrderByTimestampDesc(userId)
                .stream()
                .map(auditLogMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDTO> getAuditLogsByUserIdAndTimeRange(Long userId, Instant startTime, Instant endTime) {
        return auditLogRepository.findByUserIdAndTimestampRange(userId, startTime, endTime)
                .stream()
                .map(auditLogMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDTO> getSecurityEvents(Instant fromTime) {
        return auditLogRepository.findSecurityEventsFromTime(AuditEventCategory.SECURITY, fromTime)
                .stream()
                .map(auditLogMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDTO> getFailedOperations(Instant fromTime) {
        return auditLogRepository.findFailedOperationsFromTime(fromTime)
                .stream()
                .map(auditLogMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDTO> getRecentUserActivity(Long userId, int limit) {
        return auditLogRepository.findRecentActivityByUserId(userId, PageRequest.of(0, limit))
                .stream()
                .map(auditLogMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countEventsByUserAndType(Long userId, AuditEventType eventType) {
        return auditLogRepository.countEventsByUserAndType(userId, eventType);
    }

    @Override
    public void logUserLogin(Long userId, String ipAddress, String userAgent, boolean success) {
        AuditLogCreateRequestDTO auditRequest = new AuditLogCreateRequestDTO();
        auditRequest.setUserAgent(userAgent);
        auditRequest.setIpAddress(ipAddress);
        auditRequest.setAction(success ? "User login successful" : "User login failed");
        auditRequest.setAuditEventType(AuditEventType.LOGIN);
        auditRequest.setAuditEventCategory(AuditEventCategory.SECURITY);
        auditRequest.setSuccess(success);

        createAuditLog(userId, auditRequest);
    }

    @Override
    public void logUserLogout(Long userId, String ipAddress, String userAgent) {
        AuditLogCreateRequestDTO auditRequest = new AuditLogCreateRequestDTO();
        auditRequest.setUserAgent(userAgent);
        auditRequest.setIpAddress(ipAddress);
        auditRequest.setAction("User logout");
        auditRequest.setAuditEventType(AuditEventType.LOGOUT);
        auditRequest.setAuditEventCategory(AuditEventCategory.SECURITY);
        auditRequest.setSuccess(true);

        createAuditLog(userId, auditRequest);
    }

    @Override
    public void logDataChange(Long userId, String action, String resourceId, String details) {
        AuditLogCreateRequestDTO auditRequest = new AuditLogCreateRequestDTO();
        auditRequest.setUserAgent("System");
        auditRequest.setIpAddress("127.0.0.1");
        auditRequest.setAction(action);
        auditRequest.setAuditEventType(AuditEventType.DATA_CHANGE);
        auditRequest.setAuditEventCategory(AuditEventCategory.DATA_MANAGEMENT);
        auditRequest.setResourceId(resourceId);
        auditRequest.setDetails(details);
        auditRequest.setSuccess(true);

        createAuditLog(userId, auditRequest);
    }

    @Override
    public void logSecurityEvent(Long userId, String action, String ipAddress, String details, boolean success) {
        AuditLogCreateRequestDTO auditRequest = new AuditLogCreateRequestDTO();
        auditRequest.setUserAgent("System");
        auditRequest.setIpAddress(ipAddress);
        auditRequest.setAction(action);
        auditRequest.setAuditEventType(AuditEventType.SECURITY_ALERT);
        auditRequest.setAuditEventCategory(AuditEventCategory.SECURITY);
        auditRequest.setDetails(details);
        auditRequest.setSuccess(success);

        createAuditLog(userId, auditRequest);
    }
}
