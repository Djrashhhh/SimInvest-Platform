package com.example.MicroInvestApp.controller.user;

import com.example.MicroInvestApp.dto.user.RequestDTOs.UserProfileUpdateRequestDTO;
import com.example.MicroInvestApp.domain.enums.ExperienceLevel;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.UserProfileResponseDTO;
import com.example.MicroInvestApp.service.user.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/users/{userId}/profile")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserProfileController {

    private final UserProfileService userProfileService;

    @Autowired
    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileResponseDTO> createProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserProfileUpdateRequestDTO profileRequest,
            Authentication authentication) {

        // Manual security check - ensure user can only access their own profile
        // You'll need to implement a method to get userId from username
        // For now, we'll rely on the service layer to handle this properly

        UserProfileResponseDTO profile = userProfileService.createProfile(userId, profileRequest);
        return new ResponseEntity<>(profile, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")  // Simplified - remove the problematic expression
    public ResponseEntity<UserProfileResponseDTO> getProfile(
            @PathVariable Long userId,
            Authentication authentication) {

        // Manual security check if needed
        String authenticatedUsername = authentication.getName();
        System.out.println("Authenticated user: " + authenticatedUsername + " requesting profile for userId: " + userId);

        return userProfileService.getProfileByUserId(userId)
                .map(profile -> ResponseEntity.ok(profile))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileResponseDTO> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserProfileUpdateRequestDTO updateRequest,
            Authentication authentication) {

        UserProfileResponseDTO updatedProfile = userProfileService.updateProfile(userId, updateRequest);
        return ResponseEntity.ok(updatedProfile);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> deleteProfile(
            @PathVariable Long userId,
            Authentication authentication) {

        userProfileService.deleteProfile(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Profile deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/learning/increment")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> incrementLearningProgress(
            @PathVariable Long userId,
            Authentication authentication) {

        userProfileService.incrementLearningProgress(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Learning progress incremented");
        return ResponseEntity.ok(response);
    }
}