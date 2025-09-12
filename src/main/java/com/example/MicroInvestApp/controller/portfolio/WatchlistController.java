package com.example.MicroInvestApp.controller.portfolio;

import com.example.MicroInvestApp.dto.portfolio.RequestDTOs.CreateWatchlistRequestDTO;
import com.example.MicroInvestApp.dto.portfolio.RequestDTOs.UpdateWatchlistRequestDTO;
import com.example.MicroInvestApp.dto.portfolio.ResponseDTOs.WatchlistResponseDTO;
import com.example.MicroInvestApp.dto.portfolio.ResponseDTOs.WatchlistStatsResponseDTO;
import com.example.MicroInvestApp.dto.portfolio.ResponseDTOs.WatchlistSummaryResponseDTO;
import com.example.MicroInvestApp.service.portfolio.WatchlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Watchlist operations
 * Handles all watchlist-related HTTP requests for authenticated users
 */
@RestController
@RequestMapping("/api/v1/watchlists")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Watchlist Management", description = "APIs for managing investment watchlists")
public class WatchlistController {

    private static final Logger logger = LoggerFactory.getLogger(WatchlistController.class);

    private final WatchlistService watchlistService;

    @Autowired
    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @Operation(summary = "Create a new watchlist", description = "Creates a new watchlist for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Watchlist created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Watchlist with this name already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WatchlistResponseDTO> createWatchlist(
            @Parameter(description = "User ID", required = true) @RequestParam Long userId,
            @Parameter(description = "Watchlist creation request", required = true)
            @Valid @RequestBody CreateWatchlistRequestDTO createRequest) {

        logger.info("Creating watchlist for user ID: {}", userId);

        WatchlistResponseDTO watchlist = watchlistService.createWatchlist(userId, createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(watchlist);
    }

    @Operation(summary = "Get watchlist by ID", description = "Retrieves a watchlist by its ID (owner only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Watchlist found"),
            @ApiResponse(responseCode = "404", description = "Watchlist not found or access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{watchlistId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WatchlistResponseDTO> getWatchlistById(
            @Parameter(description = "Watchlist ID", required = true)
            @PathVariable @NotNull @Positive Long watchlistId,
            @Parameter(description = "User ID", required = true)
            @RequestParam @NotNull @Positive Long userId) {

        logger.debug("Fetching watchlist by ID: {} for user: {}", watchlistId, userId);

        return watchlistService.getWatchlistById(watchlistId, userId)
                .map(watchlist -> ResponseEntity.ok(watchlist))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all user watchlists", description = "Retrieves all watchlists for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Watchlists retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<WatchlistSummaryResponseDTO>> getUserWatchlists(
            @Parameter(description = "User ID", required = true)
            @RequestParam @NotNull @Positive Long userId) {

        logger.debug("Fetching watchlists for user ID: {}", userId);

        List<WatchlistSummaryResponseDTO> watchlists = watchlistService.getUserWatchlists(userId);
        return ResponseEntity.ok(watchlists);
    }

    @Operation(summary = "Update watchlist", description = "Updates watchlist information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Watchlist updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Watchlist not found or access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/{watchlistId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WatchlistResponseDTO> updateWatchlist(
            @Parameter(description = "Watchlist ID", required = true)
            @PathVariable @NotNull @Positive Long watchlistId,
            @Parameter(description = "User ID", required = true)
            @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "Watchlist update request", required = true)
            @Valid @RequestBody UpdateWatchlistRequestDTO updateRequest) {

        logger.info("Updating watchlist ID: {} for user: {}", watchlistId, userId);

        WatchlistResponseDTO watchlist = watchlistService.updateWatchlist(watchlistId, userId, updateRequest);
        return ResponseEntity.ok(watchlist);
    }

    @Operation(summary = "Delete watchlist", description = "Deletes a watchlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Watchlist deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Watchlist not found or access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{watchlistId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteWatchlist(
            @Parameter(description = "Watchlist ID", required = true)
            @PathVariable @NotNull @Positive Long watchlistId,
            @Parameter(description = "User ID", required = true)
            @RequestParam @NotNull @Positive Long userId) {

        logger.info("Deleting watchlist ID: {} for user: {}", watchlistId, userId);

        watchlistService.deleteWatchlist(watchlistId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add security to watchlist by symbol", description = "Adds a security to watchlist using its symbol")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Security added successfully"),
            @ApiResponse(responseCode = "404", description = "Watchlist or security not found"),
            @ApiResponse(responseCode = "400", description = "Invalid symbol or security already exists in watchlist"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/{watchlistId}/securities")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WatchlistResponseDTO> addSecurityBySymbol(
            @Parameter(description = "Watchlist ID", required = true)
            @PathVariable @NotNull @Positive Long watchlistId,
            @Parameter(description = "User ID", required = true)
            @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "Security symbol", required = true)
            @RequestParam @NotBlank String symbol) {

        logger.info("Adding security {} to watchlist {} for user {}", symbol, watchlistId, userId);

        WatchlistResponseDTO watchlist = watchlistService.addSecurityBySymbol(watchlistId, userId, symbol);
        return ResponseEntity.ok(watchlist);
    }

    @Operation(summary = "Remove security from watchlist by symbol", description = "Removes a security from watchlist using its symbol")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Security removed successfully"),
            @ApiResponse(responseCode = "404", description = "Watchlist or security not found"),
            @ApiResponse(responseCode = "400", description = "Invalid symbol"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{watchlistId}/securities")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WatchlistResponseDTO> removeSecurityBySymbol(
            @Parameter(description = "Watchlist ID", required = true)
            @PathVariable @NotNull @Positive Long watchlistId,
            @Parameter(description = "User ID", required = true)
            @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "Security symbol", required = true)
            @RequestParam @NotBlank String symbol) {

        logger.info("Removing security {} from watchlist {} for user {}", symbol, watchlistId, userId);

        WatchlistResponseDTO watchlist = watchlistService.removeSecurityBySymbol(watchlistId, userId, symbol);
        return ResponseEntity.ok(watchlist);
    }

    @Operation(summary = "Clear all securities from watchlist", description = "Removes all securities from the watchlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Watchlist cleared successfully"),
            @ApiResponse(responseCode = "404", description = "Watchlist not found or access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{watchlistId}/securities/all")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WatchlistResponseDTO> clearWatchlist(
            @Parameter(description = "Watchlist ID", required = true)
            @PathVariable @NotNull @Positive Long watchlistId,
            @Parameter(description = "User ID", required = true)
            @RequestParam @NotNull @Positive Long userId) {

        logger.info("Clearing all securities from watchlist {} for user {}", watchlistId, userId);

        WatchlistResponseDTO watchlist = watchlistService.clearWatchlist(watchlistId, userId);
        return ResponseEntity.ok(watchlist);
    }

    @Operation(summary = "Get watchlist symbols", description = "Retrieves all security symbols in a watchlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Symbols retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Watchlist not found or access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{watchlistId}/symbols")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<String>> getWatchlistSymbols(
            @Parameter(description = "Watchlist ID", required = true)
            @PathVariable @NotNull @Positive Long watchlistId,
            @Parameter(description = "User ID", required = true)
            @RequestParam @NotNull @Positive Long userId) {

        logger.debug("Fetching symbols for watchlist {} and user {}", watchlistId, userId);

        // Verify ownership before returning symbols
        if (!watchlistService.isWatchlistOwner(watchlistId, userId)) {
            return ResponseEntity.notFound().build();
        }

        List<String> symbols = watchlistService.getWatchlistSymbols(watchlistId);
        return ResponseEntity.ok(symbols);
    }

    @Operation(summary = "Get watchlists containing a security", description = "Retrieves user's watchlists that contain a specific security")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Watchlists retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Security not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/containing-security")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<WatchlistSummaryResponseDTO>> getWatchlistsContainingSymbol(
            @Parameter(description = "User ID", required = true)
            @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "Security symbol", required = true)
            @RequestParam @NotBlank String symbol) {

        logger.debug("Fetching watchlists containing symbol {} for user {}", symbol, userId);

        List<WatchlistSummaryResponseDTO> watchlists = watchlistService.getWatchlistsContainingSymbol(symbol, userId);
        return ResponseEntity.ok(watchlists);
    }

    @Operation(summary = "Get user's default watchlist", description = "Retrieves the most recently updated watchlist for the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Default watchlist found"),
            @ApiResponse(responseCode = "404", description = "No watchlist found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/default")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WatchlistResponseDTO> getUserDefaultWatchlist(
            @Parameter(description = "User ID", required = true)
            @RequestParam @NotNull @Positive Long userId) {

        logger.debug("Fetching default watchlist for user {}", userId);

        return watchlistService.getUserDefaultWatchlist(userId)
                .map(watchlist -> ResponseEntity.ok(watchlist))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get user watchlist statistics", description = "Retrieves comprehensive statistics about user's watchlists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WatchlistStatsResponseDTO> getUserWatchlistStats(
            @Parameter(description = "User ID", required = true)
            @RequestParam @NotNull @Positive Long userId) {

        logger.debug("Fetching watchlist statistics for user {}", userId);

        WatchlistStatsResponseDTO stats = watchlistService.getUserWatchlistStats(userId);
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get watchlist count", description = "Returns the total number of watchlists for the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/count")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Long>> getUserWatchlistCount(
            @Parameter(description = "User ID", required = true)
            @RequestParam @NotNull @Positive Long userId) {

        logger.debug("Getting watchlist count for user {}", userId);

        long count = watchlistService.getUserWatchlistCount(userId);
        return ResponseEntity.ok(Map.of("watchlistCount", count));
    }

    @Operation(summary = "Check if security exists in user watchlists", description = "Checks if a security exists in any of the user's watchlists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check completed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/contains-symbol")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Boolean>> isSecurityInUserWatchlists(
            @Parameter(description = "User ID", required = true)
            @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "Security symbol", required = true)
            @RequestParam @NotBlank String symbol) {

        logger.debug("Checking if symbol {} exists in watchlists for user {}", symbol, userId);

        boolean exists = watchlistService.isSymbolInUserWatchlists(userId, symbol);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @Operation(summary = "Validate watchlist ownership", description = "Validates if the user owns a specific watchlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation completed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{watchlistId}/validate-ownership")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Boolean>> validateWatchlistOwnership(
            @Parameter(description = "Watchlist ID", required = true)
            @PathVariable @NotNull @Positive Long watchlistId,
            @Parameter(description = "User ID", required = true)
            @RequestParam @NotNull @Positive Long userId) {

        logger.debug("Validating watchlist ownership - Watchlist: {}, User: {}", watchlistId, userId);

        boolean isOwner = watchlistService.isWatchlistOwner(watchlistId, userId);
        return ResponseEntity.ok(Map.of("isOwner", isOwner));
    }
}