package com.example.MicroInvestApp.repositories.user;

import com.example.MicroInvestApp.domain.user.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findBySessionToken(String sessionToken);
    List<UserSession> findByUserAccountUserId(Long userId);
    List<UserSession> findByUserAccountUserIdAndIsActive(Long userId, boolean isActive);
    List<UserSession> findByIpAddress(String ipAddress);

    @Query("SELECT s FROM UserSession s WHERE s.userAccount.userId = :userId AND s.isActive = true")
    List<UserSession> findActiveSessionsByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM UserSession s WHERE s.expiresAt < :currentTime AND s.isActive = true")
    List<UserSession> findExpiredActiveSessions(@Param("currentTime") Instant currentTime);

    @Query("SELECT s FROM UserSession s WHERE s.loginTimestamp >= :fromTime")
    List<UserSession> findSessionsFromTime(@Param("fromTime") Instant fromTime);

    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.userAccount.userId = :userId AND s.isActive = true")
    long countActiveSessionsByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM UserSession s WHERE s.lastActivity < :cutoffTime AND s.isActive = true")
    List<UserSession> findInactiveSessions(@Param("cutoffTime") Instant cutoffTime);

    void deleteByUserAccountUserIdAndIsActive(Long userId, boolean isActive);
}
