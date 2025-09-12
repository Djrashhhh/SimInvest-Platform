package com.example.MicroInvestApp.impl.user;

import com.example.MicroInvestApp.domain.user.UserSession;
import com.example.MicroInvestApp.domain.user.UserAccount;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.UserSessionResponseDTO;
import com.example.MicroInvestApp.dto.user.RequestDTOs.UserSessionCreateRequestDTO;
import com.example.MicroInvestApp.repositories.user.UserSessionRepository;
import com.example.MicroInvestApp.repositories.user.UserAccountRepository;
import com.example.MicroInvestApp.service.user.UserSessionService;
import com.example.MicroInvestApp.util.UserSessionMapper;
import com.example.MicroInvestApp.exception.user.UserNotFoundException;
import com.example.MicroInvestApp.exception.user.SessionNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional


public class UserSessionServiceImpl implements UserSessionService {

    private final UserSessionRepository userSessionRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserSessionMapper userSessionMapper;

    @Autowired
    public UserSessionServiceImpl(UserSessionRepository userSessionRepository,
                              UserAccountRepository userAccountRepository,
                              UserSessionMapper userSessionMapper) {
        this.userSessionRepository = userSessionRepository;
        this.userAccountRepository = userAccountRepository;
        this.userSessionMapper = userSessionMapper;
    }

    @Override
    public UserSessionResponseDTO createSession(Long userId, UserSessionCreateRequestDTO sessionRequest) {
        UserAccount userAccount = userAccountRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        String sessionToken = generateSessionToken();

        UserSession session = new UserSession();
        session.setUserAccount(userAccount);
        session.setSessionToken(sessionToken);
        session.setIpAddress(sessionRequest.getIpAddress());
        session.setUserAgent(sessionRequest.getUserAgent());
        session.setAuthenticated(true);

        // Set expiry based on remember me
        Duration sessionDuration = sessionRequest.isRememberMe() ?
                Duration.ofDays(30) : Duration.ofHours(24);
        session.setExpiresAt(Instant.now().plus(sessionDuration));

        UserSession savedSession = userSessionRepository.save(session);
        return userSessionMapper.toResponseDTO(savedSession);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserSessionResponseDTO> getSessionById(Long sessionId) {
        return userSessionRepository.findById(sessionId)
                .map(userSessionMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserSessionResponseDTO> getSessionByToken(String sessionToken) {
        return userSessionRepository.findBySessionToken(sessionToken)
                .map(userSessionMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSessionResponseDTO> getSessionsByUserId(Long userId) {
        return userSessionRepository.findByUserAccountUserId(userId)
                .stream()
                .map(userSessionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSessionResponseDTO> getActiveSessionsByUserId(Long userId) {
        return userSessionRepository.findActiveSessionsByUserId(userId)
                .stream()
                .map(userSessionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserSessionResponseDTO updateSessionActivity(String sessionToken) {
        UserSession session = userSessionRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new SessionNotFoundException("Session not found with token"));

        session.updateActivity();
        UserSession updatedSession = userSessionRepository.save(session);

        return userSessionMapper.toResponseDTO(updatedSession);
    }

    @Override
    public void logoutSession(String sessionToken) {
        UserSession session = userSessionRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new SessionNotFoundException("Session not found with token"));

        session.logout();
        userSessionRepository.save(session);
    }

    @Override
    public void logoutAllUserSessions(Long userId) {
        List<UserSession> activeSessions = userSessionRepository.findActiveSessionsByUserId(userId);

        for (UserSession session : activeSessions) {
            session.logout();
        }

        userSessionRepository.saveAll(activeSessions);
    }

    @Override
    public void cleanupExpiredSessions() {
        List<UserSession> expiredSessions = userSessionRepository.findExpiredActiveSessions(Instant.now());

        for (UserSession session : expiredSessions) {
            session.logout();
        }

        userSessionRepository.saveAll(expiredSessions);
    }

    @Override
    public void cleanupInactiveSessions(int hoursInactive) {
        Instant cutoffTime = Instant.now().minus(Duration.ofHours(hoursInactive));
        List<UserSession> inactiveSessions = userSessionRepository.findInactiveSessions(cutoffTime);

        for (UserSession session : inactiveSessions) {
            session.logout();
        }

        userSessionRepository.saveAll(inactiveSessions);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValidSession(String sessionToken) {
        Optional<UserSession> sessionOpt = userSessionRepository.findBySessionToken(sessionToken);

        if (sessionOpt.isEmpty()) {
            return false;
        }

        return sessionOpt.get().isValid();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveSessionsByUserId(Long userId) {
        return userSessionRepository.countActiveSessionsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSessionResponseDTO> getRecentSessions(int hoursBack) {
        Instant fromTime = Instant.now().minus(Duration.ofHours(hoursBack));
        return userSessionRepository.findSessionsFromTime(fromTime)
                .stream()
                .map(userSessionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSessionResponseDTO> getLongRunningSessions() {
        return userSessionRepository.findAll()
                .stream()
                .filter(UserSession::isLongRunning)
                .map(userSessionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    private String generateSessionToken() {
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }
}
