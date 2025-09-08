// Purpose: REST API endpoints for user account operations. Handles HTTP requests/responses and validation.
package com.example.MicroInvestApp.controller.user;

import com.example.MicroInvestApp.dto.user.RequestDTOs.UserLoginRequestDTO;
import com.example.MicroInvestApp.dto.user.RequestDTOs.UserRegistrationRequestDTO;
import com.example.MicroInvestApp.dto.user.RequestDTOs.UserUpdateRequestDTO;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.LoginResponseDTO;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.UserAccountResponseDTO;
import com.example.MicroInvestApp.service.user.UserAccountService;
import com.example.MicroInvestApp.domain.enums.AccountStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

import java.nio.file.attribute.UserPrincipal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserAccountController {
    private final UserAccountService userAccountService;

    @Autowired
    public UserAccountController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    // Registration endpoint
    @PostMapping("/register")
    public ResponseEntity<UserAccountResponseDTO> registerUser(
            @Valid @RequestBody UserRegistrationRequestDTO registrationRequest) {

        UserAccountResponseDTO user = userAccountService.createUserAccount(registrationRequest);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> loginUser(
            @Valid @RequestBody UserLoginRequestDTO loginRequest) {

        LoginResponseDTO response = userAccountService.authenticateUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    // Logout endpoint
    @PostMapping("/logout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> logoutUser(
            @RequestHeader("Authorization") String token) {

        userAccountService.logoutUser(token);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Successfully logged out");
        return ResponseEntity.ok(response);
    }

    // Get user profile
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('USER') ")
    public ResponseEntity<UserAccountResponseDTO> getUserById(
            @PathVariable Long userId,
            Authentication authentication) {

        // Manual security check
        String authenticatedUsername = authentication.getName();
        System.out.println("Authenticated user: " + authenticatedUsername + " requesting user data for userId: " + userId);


        return userAccountService.getUserById(userId)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }

    // Get current user account details
    @GetMapping("/account")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserAccountResponseDTO> getCurrentUserAccount(Authentication authentication) {
        // Get username from Authentication and let service handle the lookup
        String username = authentication.getName();

        return userAccountService.getUserByUsername(username)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }

    // Get current user profile
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserAccountResponseDTO> getCurrentUser(
            @RequestHeader("X-User-ID") Long userId) {

        return userAccountService.getUserById(userId)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }

    // Update user profile
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('USER') ")
    public ResponseEntity<UserAccountResponseDTO> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequestDTO updateRequest,
            Authentication authentication) {

        // Manual security check
        String authenticatedUsername = authentication.getName();
        System.out.println("Authenticated user: " + authenticatedUsername + " updating user data for userId: " + userId);

        UserAccountResponseDTO updatedUser = userAccountService.updateUser(userId, updateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    // Delete user account
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long userId,
            Authentication authentication) {

        // Manual security check
        String authenticatedUsername = authentication.getName();
        System.out.println("Authenticated user: " + authenticatedUsername + " deleting user data for userId: " + userId);

        userAccountService.deleteUser(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User account deleted successfully");
        return ResponseEntity.ok(response);
    }

    // Check email availability
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailAvailability(
            @RequestParam String email) {

        boolean available = userAccountService.isEmailAvailable(email);

        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);
        return ResponseEntity.ok(response);
    }

    // Check username availability
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsernameAvailability(
            @RequestParam String username) {

        boolean available = userAccountService.isUsernameAvailable(username);

        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);
        return ResponseEntity.ok(response);
    }

    // Verify email
    @PostMapping("/{userId}/verify-email")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> verifyEmail(
            @PathVariable Long userId,
            Authentication authentication) {

        // Manual security check
        String authenticatedUsername = authentication.getName();
        System.out.println("Authenticated user: " + authenticatedUsername + " verifying email for userId: " + userId);


        userAccountService.verifyEmail(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Email verified successfully");
        return ResponseEntity.ok(response);
    }

    // Admin endpoints
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserAccountResponseDTO>> getAllUsers() {

        List<UserAccountResponseDTO> users = userAccountService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserAccountResponseDTO>> getUsersByStatus(
            @PathVariable AccountStatus status) {

        List<UserAccountResponseDTO> users = userAccountService.getUsersByStatus(status);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/admin/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> activateAccount(@PathVariable Long userId) {

        userAccountService.activateAccount(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Account activated successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deactivateAccount(@PathVariable Long userId) {

        userAccountService.deactivateAccount(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Account deactivated successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/{userId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> suspendAccount(@PathVariable Long userId) {

        userAccountService.suspendAccount(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Account suspended successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserStats() {

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userAccountService.getTotalUserCount());
        stats.put("recentUsers", userAccountService.getRecentUsers(30));

        return ResponseEntity.ok(stats);
    }
}
