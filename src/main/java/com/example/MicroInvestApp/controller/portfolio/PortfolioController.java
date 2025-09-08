package com.example.MicroInvestApp.controller.portfolio;

import com.example.MicroInvestApp.dto.portfolio.RequestDTOs.CreatePortfolioRequestDTO;
import com.example.MicroInvestApp.dto.portfolio.RequestDTOs.UpdatePortfolioRequestDTO;
import com.example.MicroInvestApp.dto.portfolio.ResponseDTOs.PortfolioResponseDTO;
import com.example.MicroInvestApp.dto.portfolio.ResponseDTOs.PortfolioSummaryResponseDTO;
import com.example.MicroInvestApp.service.portfolio.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Portfolio operations
 * Handles all portfolio-related HTTP requests
 */
@RestController
@RequestMapping("/api/v1/portfolios")
@Tag(name = "Portfolio Management", description = "APIs for managing investment portfolios")
public class PortfolioController {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioController.class);

    private final PortfolioService portfolioService;

    @Autowired
    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @Operation(summary = "Create a new portfolio", description = "Creates a new portfolio for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Portfolio created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "User already has an active portfolio"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PortfolioResponseDTO> createPortfolio(
            @Parameter(description = "User ID", required = true) @RequestParam Long userId,
            @Parameter(description = "Portfolio creation request", required = true)
            @Valid @RequestBody CreatePortfolioRequestDTO request) {

        logger.info("Creating portfolio for user ID: {}", userId);

        PortfolioResponseDTO response = portfolioService.createPortfolio(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get portfolio by ID", description = "Retrieves a portfolio by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolio found"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{portfolioId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PortfolioResponseDTO> getPortfolioById(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long portfolioId) {

        logger.debug("Fetching portfolio by ID: {}", portfolioId);

        return portfolioService.getPortfolioById(portfolioId)
                .map(portfolio -> ResponseEntity.ok(portfolio))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get user's portfolio", description = "Retrieves the portfolio for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolio found"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') ")  // Access controlled in PortfolioSecurityService
    public ResponseEntity<PortfolioResponseDTO> getPortfolioByUserId(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            Authentication authentication) {     //Authentication object to get current user details

        logger.debug("Fetching portfolio for user ID: {}", userId);

        // Manual security check - ensure user can only access their own portfolio
        String username = authentication.getName();

        return portfolioService.getPortfolioByUserId(userId)
                .map(portfolio -> ResponseEntity.ok(portfolio))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get user's active portfolio", description = "Retrieves the active portfolio for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active portfolio found"),
            @ApiResponse(responseCode = "404", description = "No active portfolio found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user/{userId}/active")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PortfolioResponseDTO> getActivePortfolioByUserId(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {

        logger.debug("Fetching active portfolio for user ID: {}", userId);

        return portfolioService.getActivePortfolioByUserId(userId)
                .map(portfolio -> ResponseEntity.ok(portfolio))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update portfolio", description = "Updates portfolio information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolio updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/{portfolioId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PortfolioResponseDTO> updatePortfolio(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long portfolioId,
            @Parameter(description = "Portfolio update request", required = true)
            @Valid @RequestBody UpdatePortfolioRequestDTO request) {

        logger.info("Updating portfolio ID: {}", portfolioId);

        PortfolioResponseDTO response = portfolioService.updatePortfolio(portfolioId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete portfolio", description = "Deletes a portfolio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Portfolio deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found"),
            @ApiResponse(responseCode = "400", description = "Cannot delete portfolio with existing positions"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{portfolioId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deletePortfolio(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long portfolioId) {

        logger.info("Deleting portfolio ID: {}", portfolioId);

        portfolioService.deletePortfolio(portfolioId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update portfolio status", description = "Activates or deactivates a portfolio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolio status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PatchMapping("/{portfolioId}/status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PortfolioResponseDTO> updatePortfolioStatus(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long portfolioId,
            @Parameter(description = "Active status", required = true)
            @RequestParam boolean isActive) {

        logger.info("Updating portfolio status - ID: {}, Active: {}", portfolioId, isActive);

        PortfolioResponseDTO response = portfolioService.updatePortfolioStatus(portfolioId, isActive);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all active portfolios", description = "Retrieves all active portfolios (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active portfolios retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PortfolioResponseDTO>> getAllActivePortfolios() {
        logger.debug("Fetching all active portfolios");

        List<PortfolioResponseDTO> portfolios = portfolioService.getAllActivePortfolios();
        return ResponseEntity.ok(portfolios);
    }

    @Operation(summary = "Get portfolios by value range", description = "Retrieves portfolios within a specified value range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolios retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid value range"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/value-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PortfolioResponseDTO>> getPortfoliosByValueRange(
            @Parameter(description = "Minimum value", required = true)
            @RequestParam BigDecimal minValue,
            @Parameter(description = "Maximum value", required = true)
            @RequestParam BigDecimal maxValue) {

        logger.debug("Fetching portfolios by value range: {} - {}", minValue, maxValue);

        if (minValue.compareTo(maxValue) > 0) {
            return ResponseEntity.badRequest().build();
        }

        List<PortfolioResponseDTO> portfolios = portfolioService.getPortfoliosByValueRange(minValue, maxValue);
        return ResponseEntity.ok(portfolios);
    }

    @Operation(summary = "Get portfolio summary", description = "Retrieves a comprehensive summary of the portfolio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolio summary retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{portfolioId}/summary")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PortfolioSummaryResponseDTO> getPortfolioSummary(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long portfolioId) {

        logger.debug("Generating portfolio summary for ID: {}", portfolioId);

        PortfolioSummaryResponseDTO summary = portfolioService.getPortfolioSummary(portfolioId);
        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "Recalculate portfolio value", description = "Recalculates the total portfolio value based on current positions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolio value recalculated successfully"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/{portfolioId}/recalculate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PortfolioResponseDTO> recalculatePortfolioValue(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long portfolioId) {

        logger.info("Recalculating portfolio value for ID: {}", portfolioId);

        PortfolioResponseDTO response = portfolioService.recalculatePortfolioValue(portfolioId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Add cash to portfolio", description = "Adds cash to the portfolio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cash added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid amount"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/{portfolioId}/cash/add")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PortfolioResponseDTO> addCash(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long portfolioId,
            @Parameter(description = "Amount to add", required = true)
            @RequestParam BigDecimal amount) {

        logger.info("Adding cash to portfolio - ID: {}, Amount: {}", portfolioId, amount);

        PortfolioResponseDTO response = portfolioService.addCash(portfolioId, amount);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Withdraw cash from portfolio", description = "Withdraws cash from the portfolio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cash withdrawn successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid amount or insufficient funds"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/{portfolioId}/cash/withdraw")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PortfolioResponseDTO> withdrawCash(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long portfolioId,
            @Parameter(description = "Amount to withdraw", required = true)
            @RequestParam BigDecimal amount) {

        logger.info("Withdrawing cash from portfolio - ID: {}, Amount: {}", portfolioId, amount);

        PortfolioResponseDTO response = portfolioService.withdrawCash(portfolioId, amount);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search portfolios by name", description = "Searches portfolios by name pattern")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolios found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PortfolioResponseDTO>> searchPortfoliosByName(
            @Parameter(description = "Name pattern to search", required = true)
            @RequestParam String namePattern) {

        logger.debug("Searching portfolios by name pattern: {}", namePattern);

        List<PortfolioResponseDTO> portfolios = portfolioService.findPortfoliosByName(namePattern);
        return ResponseEntity.ok(portfolios);
    }

    @Operation(summary = "Get portfolios with minimum cash balance", description = "Retrieves portfolios with cash balance above specified amount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolios retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid cash amount"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/cash-balance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PortfolioResponseDTO>> getPortfoliosWithCashBalance(
            @Parameter(description = "Minimum cash balance", required = true)
            @RequestParam BigDecimal minCashBalance) {

        logger.debug("Fetching portfolios with cash balance >= {}", minCashBalance);

        List<PortfolioResponseDTO> portfolios = portfolioService.getPortfoliosWithCashBalance(minCashBalance);
        return ResponseEntity.ok(portfolios);
    }

    @Operation(summary = "Get portfolios created after date", description = "Retrieves portfolios created after a specific date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolios retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date format"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/created-after")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PortfolioResponseDTO>> getPortfoliosCreatedAfter(
            @Parameter(description = "Date after which to search (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date) {

        logger.debug("Fetching portfolios created after: {}", date);

        List<PortfolioResponseDTO> portfolios = portfolioService.getPortfoliosCreatedAfter(date);
        return ResponseEntity.ok(portfolios);
    }

    @Operation(summary = "Check if user has active portfolio", description = "Checks whether a user has an active portfolio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check completed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user/{userId}/has-active")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Boolean>> hasActivePortfolio(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {

        logger.debug("Checking if user {} has active portfolio", userId);

        boolean hasActive = portfolioService.hasActivePortfolio(userId);
        return ResponseEntity.ok(Map.of("hasActivePortfolio", hasActive));
    }

    @Operation(summary = "Get portfolio statistics", description = "Retrieves system-wide portfolio statistics (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPortfolioStatistics() {
        logger.debug("Fetching portfolio statistics");

        Long totalActivePortfolios = portfolioService.getTotalActivePortfolioCount();
        BigDecimal totalPortfolioValue = portfolioService.getTotalPortfolioValue();

        Map<String, Object> statistics = Map.of(
                "totalActivePortfolios", totalActivePortfolios,
                "totalPortfolioValue", totalPortfolioValue
        );

        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Validate portfolio ownership", description = "Validates if a user owns a specific portfolio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation completed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{portfolioId}/validate-ownership")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Boolean>> validatePortfolioOwnership(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable Long portfolioId,
            @Parameter(description = "User ID", required = true)
            @RequestParam Long userId) {

        logger.debug("Validating portfolio ownership - Portfolio: {}, User: {}", portfolioId, userId);

        boolean isOwner = portfolioService.validatePortfolioOwnership(portfolioId, userId);
        return ResponseEntity.ok(Map.of("isOwner", isOwner));
    }
}