package com.example.MicroInvestApp.service.portfolio;

import com.example.MicroInvestApp.domain.enums.TransactionType;
import com.example.MicroInvestApp.dto.portfolio.RequestDTOs.UpdatePositionRequestDTO;
import com.example.MicroInvestApp.dto.portfolio.ResponseDTOs.PositionResponseDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * Enhanced PositionService interface for comprehensive position management
 */
public interface PositionService {

    // ===== CORE POSITION MANAGEMENT =====

    /**
     * Update position from a buy/sell transaction
     */
    void updatePositionFromTransaction(Long portfolioId, String stockSymbol,
                                       TransactionType transactionType,
                                       BigDecimal quantity, BigDecimal pricePerShare);

    /**
     * Enhanced: Update position with detailed request DTO
     */
    void updatePosition(Long portfolioId, String stockSymbol, UpdatePositionRequestDTO updateRequest);

    /**
     * Update all position values for a specific portfolio
     */
    void updatePositionValues(Long portfolioId);

    /**
     * Scheduled update of all position values across all portfolios
     */
    void updateAllPositionValues();

    // ===== POSITION QUERIES =====

    /**
     * Get all active positions for a portfolio with comprehensive data
     */
    List<PositionResponseDTO> getPositionsByPortfolio(Long portfolioId);

    /**
     * Get specific position details
     */
    PositionResponseDTO getPosition(Long portfolioId, String stockSymbol);

    /**
     * Get current quantity of shares for a specific position
     */
    BigDecimal getCurrentQuantity(Long portfolioId, String stockSymbol);

    /**
     * Get current market value of a specific position
     */
    BigDecimal getCurrentValue(Long portfolioId, String stockSymbol);

    /**
     * Get total portfolio value including cash and positions
     */
    BigDecimal getTotalPortfolioValue(Long portfolioId);

    // ===== REAL-TIME UPDATES =====

    /**
     * Refresh market value for a specific position
     */
    void refreshPositionValue(Long positionId);

    /**
     * Refresh all position values in a portfolio
     */
    void refreshPortfolioPositions(Long portfolioId);

    // ===== POSITION ANALYTICS =====

    /**
     * Get unrealized gain/loss for a specific position
     */
    BigDecimal getUnrealizedGainLoss(Long portfolioId, String stockSymbol);

    /**
     * Get realized gain/loss for a specific position
     */
    BigDecimal getRealizedGainLoss(Long portfolioId, String stockSymbol);

    /**
     * Get total unrealized gain/loss across all positions in portfolio
     */
    BigDecimal getTotalUnrealizedGainLoss(Long portfolioId);

    /**
     * Get total realized gain/loss across all positions in portfolio
     */
    BigDecimal getTotalRealizedGainLoss(Long portfolioId);

    /**
     * Check if portfolio has sufficient shares for a sell order
     */
    boolean hasSufficientShares(Long portfolioId, String symbol, BigDecimal requiredQuantity);

    /**
     * Get average cost basis for a position
     */
    BigDecimal getAverageCostBasis(Long portfolioId, String symbol);

    // ===== ENHANCED ANALYTICS =====

    /**
     * Enhanced: Get top performing positions by percentage gain
     */
    List<PositionResponseDTO> getTopPerformingPositions(Long portfolioId, int limit);

    /**
     * Enhanced: Get worst performing positions by percentage loss
     */
    List<PositionResponseDTO> getWorstPerformingPositions(Long portfolioId, int limit);

    /**
     * Enhanced: Get positions that require attention (significant day changes)
     */
    List<PositionResponseDTO> getPositionsRequiringAttention(Long portfolioId);
}