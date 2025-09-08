package com.example.MicroInvestApp.controller.user;

import com.example.MicroInvestApp.dto.user.ResponseDTOs.AchievementResponseDTO;
import com.example.MicroInvestApp.dto.user.RequestDTOs.AchievementCreateRequestDTO;
import com.example.MicroInvestApp.dto.user.ResponseDTOs.UserStatsResponseDTO;
import com.example.MicroInvestApp.service.user.AchievementService;
import com.example.MicroInvestApp.domain.enums.AchievementType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/achievements")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AchievementController {

    private final AchievementService achievementService;

    @Autowired
    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @PostMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AchievementResponseDTO> createAchievement(
            @PathVariable Long userId,
            @Valid @RequestBody AchievementCreateRequestDTO achievementRequest) {

        AchievementResponseDTO achievement = achievementService.createAchievement(userId, achievementRequest);
        return new ResponseEntity<>(achievement, HttpStatus.CREATED);
    }

    @GetMapping("/{achievementId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<AchievementResponseDTO> getAchievementById(@PathVariable Long achievementId) {

        return achievementService.getAchievementById(achievementId)
                .map(achievement -> ResponseEntity.ok(achievement))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<List<AchievementResponseDTO>> getUserAchievements(@PathVariable Long userId) {

        List<AchievementResponseDTO> achievements = achievementService.getAchievementsByUserId(userId);
        return ResponseEntity.ok(achievements);
    }

    @GetMapping("/users/{userId}/type/{type}")
    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<List<AchievementResponseDTO>> getUserAchievementsByType(
            @PathVariable Long userId,
            @PathVariable AchievementType type) {

        List<AchievementResponseDTO> achievements = achievementService.getAchievementsByUserIdAndType(userId, type);
        return ResponseEntity.ok(achievements);
    }

    @GetMapping("/users/{userId}/recent")
    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<List<AchievementResponseDTO>> getRecentAchievements(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {

        List<AchievementResponseDTO> achievements = achievementService.getRecentAchievements(userId, days);
        return ResponseEntity.ok(achievements);
    }

    @GetMapping("/users/{userId}/stats")
    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<UserStatsResponseDTO> getUserStats(@PathVariable Long userId) {

        UserStatsResponseDTO stats = achievementService.getUserStats(userId);
        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/{achievementId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteAchievement(@PathVariable Long achievementId) {

        achievementService.deleteAchievement(achievementId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Achievement deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/high-value")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AchievementResponseDTO>> getHighValueAchievements(
            @RequestParam(defaultValue = "100") int minPoints) {

        List<AchievementResponseDTO> achievements = achievementService.getHighValueAchievements(minPoints);
        return ResponseEntity.ok(achievements);
    }
}
