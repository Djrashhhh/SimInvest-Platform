package com.example.MicroInvestApp.service.user;
import com.example.MicroInvestApp.domain.user.UserSession;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.UserSessionResponseDTO;
import com.example.MicroInvestApp.dto.user.RequestDTOs.UserSessionCreateRequestDTO;


import java.util.List;
import java.util.Optional;

public interface UserSessionService  {
    UserSessionResponseDTO createSession(Long userId, UserSessionCreateRequestDTO sessionRequest);
    Optional<UserSessionResponseDTO> getSessionById(Long sessionId);
    Optional<UserSessionResponseDTO> getSessionByToken(String sessionToken);
    List<UserSessionResponseDTO> getSessionsByUserId(Long userId);
    List<UserSessionResponseDTO> getActiveSessionsByUserId(Long userId);

    UserSessionResponseDTO updateSessionActivity(String sessionToken);
    void logoutSession(String sessionToken);
    void logoutAllUserSessions(Long userId);

    void cleanupExpiredSessions();
    void cleanupInactiveSessions(int hoursInactive);

    boolean isValidSession(String sessionToken);
    long countActiveSessionsByUserId(Long userId);

    List<UserSessionResponseDTO> getRecentSessions(int hoursBack);
    List<UserSessionResponseDTO> getLongRunningSessions();
}
