package com.example.MicroInvestApp.controller.portfolio;

import com.example.MicroInvestApp.dto.portfolio.RequestDTOs.UpdatePositionRequestDTO;
import com.example.MicroInvestApp.dto.portfolio.ResponseDTOs.PositionResponseDTO;
import com.example.MicroInvestApp.exception.portfolio.PositionCalculationException;
import com.example.MicroInvestApp.service.portfolio.PositionDayChangeService;
import com.example.MicroInvestApp.service.portfolio.PositionService;
import com.example.MicroInvestApp.exception.portfolio.PositionNotFoundException;
import com.example.MicroInvestApp.exception.portfolio.PortfolioNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enhanced Position Controller with comprehensive position management endpoints
 */
@RestController
@RequestMapping("/api/v1/positions")
@Tag(name = "Position Management", description = "Enhanced APIs for managing portfolio positions with analytics")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PositionController {

    private static final Logger logger = LoggerFactory.getLogger(PositionController.class);

    private final PositionService positionService;
    private final PositionDayChangeService positionDayChangeService;


    @Autowired
    public PositionController(PositionService positionService, PositionDayChangeService positionDayChangeService) {
        this.positionService = positionService;
        this.positionDayChangeService = positionDayChangeService;
    }

    // ===== BASIC POSITION QUERIES =====

    @GetMapping("/portfolio/{portfolioId}")
    @Operation(summary = "Get all positions for a portfolio",
            description = "Retrieves all active positions within a specific portfolio with comprehensive analytics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved positions"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getPositionsByPortfolio(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable @NotNull @Positive Long portfolioId) {

        logger.info("Retrieving positions for portfolio: {}", portfolioId);

        try {
            List<PositionResponseDTO> positions = positionService.getPositionsByPortfolio(portfolioId);
            BigDecimal totalValue = positionService.getTotalPortfolioValue(portfolioId);

            Map<String, Object> response = createSuccessResponse("Positions retrieved successfully");
            response.put("portfolioId", portfolioId);
            response.put("positions", positions);
            response.put("totalPositions", positions.size());
            response.put("totalPortfolioValue", totalValue);

            // Add summary statistics
            BigDecimal totalUnrealized = positions.stream()
                    .map(PositionResponseDTO::getUnrealizedGainLoss)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalRealized = positions.stream()
                    .map(PositionResponseDTO::getRealizedGainLoss)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            response.put("totalUnrealizedGainLoss", totalUnrealized);
            response.put("totalRealizedGainLoss", totalRealized);
            response.put("totalGainLoss", totalUnrealized.add(totalRealized));

            logger.info("Successfully retrieved {} positions for portfolio {}", positions.size(), portfolioId);
            return ResponseEntity.ok(response);

        } catch (PortfolioNotFoundException e) {
            logger.error("Portfolio not found: {}", portfolioId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Portfolio not found", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error retrieving positions for portfolio {}: {}", portfolioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve positions", e.getMessage()));
        }
    }

    @GetMapping("/portfolio/{portfolioId}/security/{stockSymbol}")
    @Operation(summary = "Get specific position",
            description = "Retrieves detailed information for a specific position")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved position"),
            @ApiResponse(responseCode = "404", description = "Position or portfolio not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getPosition(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable @NotNull @Positive Long portfolioId,
            @Parameter(description = "Stock symbol", required = true)
            @PathVariable @NotNull String stockSymbol) {

        logger.info("Retrieving position for {} in portfolio {}", stockSymbol, portfolioId);

        try {
            PositionResponseDTO position = positionService.getPosition(portfolioId, stockSymbol);

            Map<String, Object> response = createSuccessResponse("Position retrieved successfully");
            response.put("position", position);

            logger.info("Successfully retrieved position for {} in portfolio {}", stockSymbol, portfolioId);
            return ResponseEntity.ok(response);

        } catch (PositionNotFoundException | PortfolioNotFoundException e) {
            logger.error("Position not found: {} in portfolio {}", stockSymbol, portfolioId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Position not found", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error retrieving position for {} in portfolio {}: {}", stockSymbol, portfolioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve position", e.getMessage()));
        }
    }

    // ===== ENHANCED ANALYTICS ENDPOINTS =====

    @GetMapping("/portfolio/{portfolioId}/analytics")
    @Operation(summary = "Get comprehensive portfolio position analytics",
            description = "Retrieves detailed analytical data for all positions in a portfolio")
    public ResponseEntity<Map<String, Object>> getPortfolioPositionAnalytics(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable @NotNull @Positive Long portfolioId) {

        logger.info("Retrieving position analytics for portfolio: {}", portfolioId);

        try {
            List<PositionResponseDTO> positions = positionService.getPositionsByPortfolio(portfolioId);
            BigDecimal totalValue = positionService.getTotalPortfolioValue(portfolioId);
            BigDecimal totalUnrealizedGainLoss = positionService.getTotalUnrealizedGainLoss(portfolioId);
            BigDecimal totalRealizedGainLoss = positionService.getTotalRealizedGainLoss(portfolioId);

            Map<String, Object> analytics = createSuccessResponse("Portfolio analytics retrieved successfully");
            analytics.put("portfolioId", portfolioId);
            analytics.put("totalPortfolioValue", totalValue);
            analytics.put("totalUnrealizedGainLoss", totalUnrealizedGainLoss);
            analytics.put("totalRealizedGainLoss", totalRealizedGainLoss);
            analytics.put("totalGainLoss", totalUnrealizedGainLoss.add(totalRealizedGainLoss));
            analytics.put("totalActivePositions", positions.size());

            // Calculate performance metrics
            BigDecimal totalCostBasis = positions.stream()
                    .map(PositionResponseDTO::getCostBasis)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalCostBasis.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal totalReturnPercent = totalUnrealizedGainLoss
                        .divide(totalCostBasis, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                analytics.put("totalUnrealizedGainLossPercentage", totalReturnPercent);
            } else {
                analytics.put("totalUnrealizedGainLossPercentage", BigDecimal.ZERO);
            }

            // Position distribution
            long gainPositions = positions.stream()
                    .mapToLong(p -> p.getUnrealizedGainLoss().compareTo(BigDecimal.ZERO) > 0 ? 1 : 0)
                    .sum();
            long lossPositions = positions.stream()
                    .mapToLong(p -> p.getUnrealizedGainLoss().compareTo(BigDecimal.ZERO) < 0 ? 1 : 0)
                    .sum();

            analytics.put("positionsWithGains", gainPositions);
            analytics.put("positionsWithLosses", lossPositions);
            analytics.put("neutralPositions", positions.size() - gainPositions - lossPositions);

            logger.info("Successfully retrieved analytics for portfolio {}", portfolioId);
            return ResponseEntity.ok(analytics);

        } catch (PortfolioNotFoundException e) {
            logger.error("Portfolio not found: {}", portfolioId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Portfolio not found", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error retrieving analytics for portfolio {}: {}", portfolioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve analytics", e.getMessage()));
        }
    }

    @GetMapping("/portfolio/{portfolioId}/top-performers")
    @Operation(summary = "Get top performing positions",
            description = "Retrieves the best performing positions in a portfolio by percentage gain")
    public ResponseEntity<Map<String, Object>> getTopPerformingPositions(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable @NotNull @Positive Long portfolioId,
            @Parameter(description = "Number of positions to return", required = false)
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit) {

        logger.info("Retrieving top {} performing positions for portfolio {}", limit, portfolioId);

        try {
            List<PositionResponseDTO> topPerformers = positionService.getTopPerformingPositions(portfolioId, limit);

            Map<String, Object> response = createSuccessResponse("Top performing positions retrieved successfully");
            response.put("portfolioId", portfolioId);
            response.put("limit", limit);
            response.put("positions", topPerformers);
            response.put("count", topPerformers.size());

            logger.info("Successfully retrieved {} top performing positions for portfolio {}",
                    topPerformers.size(), portfolioId);
            return ResponseEntity.ok(response);

        } catch (PortfolioNotFoundException e) {
            logger.error("Portfolio not found: {}", portfolioId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Portfolio not found", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error retrieving top performers for portfolio {}: {}", portfolioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve top performers", e.getMessage()));
        }
    }

    @GetMapping("/portfolio/{portfolioId}/worst-performers")
    @Operation(summary = "Get worst performing positions",
            description = "Retrieves the worst performing positions in a portfolio by percentage loss")
    public ResponseEntity<Map<String, Object>> getWorstPerformingPositions(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable @NotNull @Positive Long portfolioId,
            @Parameter(description = "Number of positions to return", required = false)
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit) {

        logger.info("Retrieving worst {} performing positions for portfolio {}", limit, portfolioId);

        try {
            List<PositionResponseDTO> worstPerformers = positionService.getWorstPerformingPositions(portfolioId, limit);

            Map<String, Object> response = createSuccessResponse("Worst performing positions retrieved successfully");
            response.put("portfolioId", portfolioId);
            response.put("limit", limit);
            response.put("positions", worstPerformers);
            response.put("count", worstPerformers.size());

            return ResponseEntity.ok(response);

        } catch (PortfolioNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Portfolio not found", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error retrieving worst performers for portfolio {}: {}", portfolioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve worst performers", e.getMessage()));
        }
    }

    @GetMapping("/portfolio/{portfolioId}/attention-required")
    @Operation(summary = "Get positions requiring attention",
            description = "Retrieves positions with significant day changes that may require attention")
    public ResponseEntity<Map<String, Object>> getPositionsRequiringAttention(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable @NotNull @Positive Long portfolioId) {

        logger.info("Retrieving positions requiring attention for portfolio {}", portfolioId);

        try {
            List<PositionResponseDTO> attentionPositions = positionService.getPositionsRequiringAttention(portfolioId);

            Map<String, Object> response = createSuccessResponse("Attention positions retrieved successfully");
            response.put("portfolioId", portfolioId);
            response.put("positions", attentionPositions);
            response.put("count", attentionPositions.size());
            response.put("description", "Positions with significant day changes (>5%)");

            return ResponseEntity.ok(response);

        } catch (PortfolioNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Portfolio not found", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error retrieving attention positions for portfolio {}: {}", portfolioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve attention positions", e.getMessage()));
        }
    }

    // ===== POSITION UPDATES =====

    @PutMapping("/portfolio/{portfolioId}/security/{stockSymbol}")
    @Operation(summary = "Update position",
            description = "Updates position details with comprehensive validation")
    public ResponseEntity<Map<String, Object>> updatePosition(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable @NotNull @Positive Long portfolioId,
            @Parameter(description = "Stock symbol", required = true)
            @PathVariable @NotNull String stockSymbol,
            @Valid @RequestBody UpdatePositionRequestDTO updateRequest) {

        logger.info("Updating position for {} in portfolio {} with request: {}",
                stockSymbol, portfolioId, updateRequest);

        try {
            positionService.updatePosition(portfolioId, stockSymbol, updateRequest);

            Map<String, Object> response = createSuccessResponse("Position updated successfully");
            response.put("portfolioId", portfolioId);
            response.put("stockSymbol", stockSymbol);
            response.put("updateRequest", updateRequest);

            logger.info("Successfully updated position for {} in portfolio {}", stockSymbol, portfolioId);
            return ResponseEntity.ok(response);

        } catch (PositionNotFoundException | PortfolioNotFoundException e) {
            logger.error("Position or portfolio not found: {} in portfolio {}", stockSymbol, portfolioId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Position or portfolio not found", e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid update request for {} in portfolio {}: {}", stockSymbol, portfolioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Invalid request", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating position for {} in portfolio {}: {}", stockSymbol, portfolioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update position", e.getMessage()));
        }
    }

    // ===== REAL-TIME UPDATES =====

    @PostMapping("/portfolio/{portfolioId}/refresh")
    @Operation(summary = "Refresh all positions",
            description = "Updates market values for all positions in a portfolio")
    public ResponseEntity<Map<String, Object>> refreshPortfolioPositions(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable @NotNull @Positive Long portfolioId) {

        logger.info("Refreshing positions for portfolio: {}", portfolioId);

        try {
            positionService.refreshPortfolioPositions(portfolioId);
            positionDayChangeService.updatePortfolioDayChanges(portfolioId);

            Map<String, Object> response = createSuccessResponse("Portfolio positions refreshed successfully");
            response.put("portfolioId", portfolioId);
            response.put("refreshedAt", Instant.now());
            response.put("dayChangesUpdated", true);

            logger.info("Successfully refreshed positions for portfolio {}", portfolioId);
            return ResponseEntity.ok(response);

        } catch (PortfolioNotFoundException e) {
            logger.error("Portfolio not found: {}", portfolioId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Portfolio not found", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error refreshing positions for portfolio {}: {}", portfolioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to refresh positions", e.getMessage()));
        }
    }

    @PostMapping("/{positionId}/refresh")
    @Operation(summary = "Refresh single position",
            description = "Updates market value for a specific position")
    public ResponseEntity<Map<String, Object>> refreshPositionValue(
            @Parameter(description = "Position ID", required = true)
            @PathVariable @NotNull @Positive Long positionId) {

        logger.info("Refreshing position: {}", positionId);

        try {
            positionService.refreshPositionValue(positionId);

            Map<String, Object> response = createSuccessResponse("Position refreshed successfully");
            response.put("positionId", positionId);
            response.put("refreshedAt", Instant.now());

            logger.info("Successfully refreshed position {}", positionId);
            return ResponseEntity.ok(response);

        } catch (PositionNotFoundException e) {
            logger.error("Position not found: {}", positionId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Position not found", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error refreshing position {}: {}", positionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to refresh position", e.getMessage()));
        }
    }

    @PostMapping("/update-all")
    @Operation(summary = "Update all position values",
            description = "Triggers scheduled update for all position values across all portfolios")
    public ResponseEntity<Map<String, Object>> updateAllPositionValues() {
        logger.info("Initiating update of all position values");

        try {
            positionService.updateAllPositionValues();

            Map<String, Object> response = createSuccessResponse("Position values update initiated successfully");
            response.put("initiatedAt", Instant.now());

            logger.info("Successfully initiated update of all position values");
            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            logger.error("Error initiating position values update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to initiate position values update", e.getMessage()));
        }
    }

    // ===== UTILITY ENDPOINTS =====

    @GetMapping("/portfolio/{portfolioId}/security/{stockSymbol}/quantity")
    @Operation(summary = "Get current quantity",
            description = "Retrieves the current quantity of a specific security in a portfolio")
    public ResponseEntity<Map<String, Object>> getCurrentQuantity(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable @NotNull @Positive Long portfolioId,
            @Parameter(description = "Stock symbol", required = true)
            @PathVariable @NotNull String stockSymbol) {

        try {
            BigDecimal quantity = positionService.getCurrentQuantity(portfolioId, stockSymbol);

            Map<String, Object> response = createSuccessResponse("Quantity retrieved successfully");
            response.put("portfolioId", portfolioId);
            response.put("stockSymbol", stockSymbol);
            response.put("quantity", quantity);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving quantity for {} in portfolio {}: {}", stockSymbol, portfolioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve quantity", e.getMessage()));
        }
    }

    @GetMapping("/portfolio/{portfolioId}/security/{stockSymbol}/value")
    @Operation(summary = "Get current value",
            description = "Retrieves the current market value of a specific security position")
    public ResponseEntity<Map<String, Object>> getCurrentValue(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable @NotNull @Positive Long portfolioId,
            @Parameter(description = "Stock symbol", required = true)
            @PathVariable @NotNull String stockSymbol) {

        try {
            BigDecimal value = positionService.getCurrentValue(portfolioId, stockSymbol);

            Map<String, Object> response = createSuccessResponse("Value retrieved successfully");
            response.put("portfolioId", portfolioId);
            response.put("stockSymbol", stockSymbol);
            response.put("currentValue", value);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving value for {} in portfolio {}: {}", stockSymbol, portfolioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve value", e.getMessage()));
        }
    }

    @GetMapping("/portfolio/{portfolioId}/security/{stockSymbol}/gain-loss")
    @Operation(summary = "Get gain/loss information",
            description = "Retrieves both realized and unrealized gain/loss for a specific position")
    public ResponseEntity<Map<String, Object>> getPositionGainLoss(
            @Parameter(description = "Portfolio ID", required = true)
            @PathVariable @NotNull @Positive Long portfolioId,
            @Parameter(description = "Stock symbol", required = true)
            @PathVariable @NotNull String stockSymbol) {

        try {
            BigDecimal unrealizedGainLoss = positionService.getUnrealizedGainLoss(portfolioId, stockSymbol);
            BigDecimal realizedGainLoss = positionService.getRealizedGainLoss(portfolioId, stockSymbol);

            Map<String, Object> response = createSuccessResponse("Gain/loss retrieved successfully");
            response.put("portfolioId", portfolioId);
            response.put("stockSymbol", stockSymbol);
            response.put("unrealizedGainLoss", unrealizedGainLoss);
            response.put("realizedGainLoss", realizedGainLoss);
            response.put("totalGainLoss", unrealizedGainLoss.add(realizedGainLoss));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving gain/loss for {} in portfolio {}: {}", stockSymbol, portfolioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve gain/loss", e.getMessage()));
        }
    }

    // ===== HELPER METHODS =====

    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", Instant.now());
        return response;
    }

    private Map<String, Object> createErrorResponse(String message, String details) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("details", details);
        errorResponse.put("timestamp", Instant.now());
        return errorResponse;
    }

    // ===== EXCEPTION HANDLERS =====

    @ExceptionHandler(PositionCalculationException.class)
    public ResponseEntity<Map<String, Object>> handlePositionCalculationException(PositionCalculationException e) {
        logger.error("Position calculation error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(createErrorResponse("Position calculation failed",
                        "Unable to calculate position values. Please try again later."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.error("Invalid argument: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("Invalid request", e.getMessage()));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLockingFailure(Exception e) {
        logger.error("Optimistic locking failure: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse("Concurrent modification detected",
                        "The position was modified by another process. Please retry your request."));
    }
}