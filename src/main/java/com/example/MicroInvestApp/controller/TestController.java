package com.example.MicroInvestApp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Test Controller for verifying JWT authentication
 * Provides protected endpoints to test authentication flow
 */
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    /**
     * Public endpoint - no authentication required
     */
    @GetMapping("/public")
    public ResponseEntity<?> publicEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is a public endpoint");
        response.put("timestamp", System.currentTimeMillis());
        response.put("authenticated", false);
        return ResponseEntity.ok(response);
    }

    /**
     * Protected endpoint - requires authentication
     */
    @GetMapping("/protected")
    public ResponseEntity<?> protectedEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is a protected endpoint");
        response.put("timestamp", System.currentTimeMillis());
        response.put("authenticated", true);
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities());

        return ResponseEntity.ok(response);
    }

    /**
     * Admin only endpoint - requires ADMIN role
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')") // Ensure only users with ADMIN role can access
    public ResponseEntity<?> adminEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is an admin-only endpoint");
        response.put("timestamp", System.currentTimeMillis());
        response.put("authenticated", true);
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities());

        return ResponseEntity.ok(response);
    }

    /**
     * User profile endpoint - requires authentication
     */
    @GetMapping("/profile")
    public ResponseEntity<?> userProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User profile information");
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("authenticated", true);

        return ResponseEntity.ok(response);
    }

    /**
     * Debug endpoint to check user roles and authorities
     */
    @GetMapping("/debug-user")
    public ResponseEntity<?> debugUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Debug user information");
        response.put("timestamp", System.currentTimeMillis());
        response.put("authenticated", authentication.isAuthenticated());
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("principal", authentication.getPrincipal());

        // Additional debug info
        response.put("hasRoleUser", authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        response.put("hasRoleAdmin", authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));

        return ResponseEntity.ok(response);
    }
}