package com.example.MicroInvestApp.util;

import com.example.MicroInvestApp.domain.user.UserSession;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.UserSessionResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class UserSessionMapper {

    public UserSessionResponseDTO toResponseDTO(UserSession session) {
        UserSessionResponseDTO dto = new UserSessionResponseDTO();

        dto.setUserSessionId(session.getUserSessionId());
        dto.setUserId(session.getUserAccount().getUserId());
        dto.setActive(session.isActive());
        dto.setIpAddress(session.getIpAddress());
        dto.setUserAgent(session.getUserAgent());
        dto.setLoginTimestamp(session.getLoginTimestamp());
        dto.setLogoutTimestamp(session.getLogoutTimestamp());
        dto.setAuthenticated(session.isAuthenticated());
        dto.setExpiresAt(session.getExpiresAt());
        dto.setLastActivity(session.getLastActivity());

        // Set calculated fields
        dto.setExpired(session.isExpired());
        dto.setValid(session.isValid());
        dto.setSessionDurationMinutes(session.getSessionDuration().toMinutes());
        dto.setLongRunning(session.isLongRunning());

        return dto;
    }
}
