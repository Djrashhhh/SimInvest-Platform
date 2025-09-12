package com.example.MicroInvestApp.controller.user;

import com.example.MicroInvestApp.dto.user.ResponseDTOs.UserSessionResponseDTO;
import com.example.MicroInvestApp.service.user.UserSessionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/sessions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserSessionController {

    private final UserSessionService userSessionService;

    @Autowired
    public UserSessionController(UserSessionService userSessionService) {
        this.userSessionService = userSessionService;
    }

    @GetMapping("/{sessionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserSessionResponseDTO> getSessionById(@PathVariable Long sessionId) {

        return userSessionService.getSessionById(sessionId)
                .map(session -> ResponseEntity.ok(session))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<List<UserSessionResponseDTO>> getUserSessions(@PathVariable Long userId) {

        List<UserSessionResponseDTO> sessions = userSessionService.getSessionsByUserId(userId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/users/{userId}/active")
    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<List<UserSessionResponseDTO>> getActiveUserSessions(@PathVariable Long userId) {

        List<UserSessionResponseDTO> activeSessions = userSessionService.getActiveSessionsByUserId(userId);
        return ResponseEntity.ok(activeSessions);
    }

    @PostMapping("/logout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> logoutSession(
            @RequestHeader("Authorization") String authToken) {

        // Extract token from Bearer header
        String sessionToken = authToken.replace("Bearer ", "");
        userSessionService.logoutSession(sessionToken);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Session logged out successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{userId}/logout-all")
    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> logoutAllUserSessions(@PathVariable Long userId) {

        userSessionService.logoutAllUserSessions(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "All user sessions logged out successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cleanup/expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> cleanupExpiredSessions() {

        userSessionService.cleanupExpiredSessions();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Expired sessions cleaned up successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cleanup/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> cleanupInactiveSessions(
            @RequestParam(defaultValue = "24") int hoursInactive) {

        userSessionService.cleanupInactiveSessions(hoursInactive);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Inactive sessions cleaned up successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSessionResponseDTO>> getRecentSessions(
            @RequestParam(defaultValue = "24") int hoursBack) {

        List<UserSessionResponseDTO> recentSessions = userSessionService.getRecentSessions(hoursBack);
        return ResponseEntity.ok(recentSessions);
    }

    @GetMapping("/admin/long-running")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSessionResponseDTO>> getLongRunningSessions() {

        List<UserSessionResponseDTO> longRunningSessions = userSessionService.getLongRunningSessions();
        return ResponseEntity.ok(longRunningSessions);
    }
}
