package com.example.MicroInvestApp.repositories.user;

import com.example.MicroInvestApp.domain.enums.AuditEventCategory;
import com.example.MicroInvestApp.domain.enums.AuditEventType;
import com.example.MicroInvestApp.domain.user.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserAccountUserId(Long userId);
    List<AuditLog> findByUserAccountUserIdOrderByTimestampDesc(Long userId);
    List<AuditLog> findByAuditEventType(AuditEventType eventType);
    List<AuditLog> findByAuditEventCategory(AuditEventCategory category);
    List<AuditLog> findByIpAddress(String ipAddress);

    @Query("SELECT a FROM AuditLog a WHERE a.userAccount.userId = :userId AND a.timestamp BETWEEN :startTime AND :endTime")
    List<AuditLog> findByUserIdAndTimestampRange(
            @Param("userId") Long userId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    @Query("SELECT a FROM AuditLog a WHERE a.auditEventCategory = :category AND a.timestamp >= :fromTime")
    List<AuditLog> findSecurityEventsFromTime(
            @Param("category") AuditEventCategory category,
            @Param("fromTime") Instant fromTime);

    @Query("SELECT a FROM AuditLog a WHERE a.success = false AND a.timestamp >= :fromTime")
    List<AuditLog> findFailedOperationsFromTime(@Param("fromTime") Instant fromTime);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userAccount.userId = :userId AND a.auditEventType = :eventType")
    long countEventsByUserAndType(@Param("userId") Long userId, @Param("eventType") AuditEventType eventType);

    @Query("SELECT a FROM AuditLog a WHERE a.userAccount.userId = :userId ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentActivityByUserId(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);
}
