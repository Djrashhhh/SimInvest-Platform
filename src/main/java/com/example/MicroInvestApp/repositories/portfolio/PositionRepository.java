package com.example.MicroInvestApp.repositories.portfolio;

import com.example.MicroInvestApp.domain.enums.SecuritySector;
import com.example.MicroInvestApp.domain.portfolio.Position;
import com.example.MicroInvestApp.domain.portfolio.Portfolio;
import com.example.MicroInvestApp.domain.market.SecurityStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Enhanced Repository interface for Position entity operations
 * Provides CRUD operations and optimized custom queries for position management
 */
@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    // ===== BASIC FINDER METHODS =====

    /**
     * Find all active positions for a specific portfolio
     */
    @Query("SELECT p FROM Position p WHERE p.portfolio = :portfolio AND p.isActive = true")
    List<Position> findActiveByPortfolio(@Param("portfolio") Portfolio portfolio);

    /**
     * Find all positions (active and inactive) for a specific portfolio
     */
    List<Position> findByPortfolio(Portfolio portfolio);

    /**
     * Count total positions in a portfolio
     */
    @Query("SELECT COUNT(p) FROM Position p WHERE p.portfolio.portfolioId = :portfolioId")
    Long countByPortfolioId(@Param("portfolioId") Long portfolioId);


    /**
     * Enhanced: Find positions by portfolio ID with security data eagerly loaded
     */
    @Query("SELECT p FROM Position p JOIN FETCH p.securityStock s " +
            "WHERE p.portfolio.portfolioId = :portfolioId AND p.isActive = true " +
            "ORDER BY p.currentValue DESC")
    List<Position> findActiveByPortfolioIdWithSecurity(@Param("portfolioId") Long portfolioId);

    /**
     * Enhanced: Find all positions (including inactive) by portfolio ID with security data
     */
    @Query("SELECT p FROM Position p JOIN FETCH p.securityStock s " +
            "WHERE p.portfolio.portfolioId = :portfolioId " +
            "ORDER BY p.isActive DESC, p.currentValue DESC")
    List<Position> findByPortfolioIdWithSecurity(@Param("portfolioId") Long portfolioId);

    /**
     * Find position by portfolio and security
     */
    Optional<Position> findByPortfolioAndSecurityStock(Portfolio portfolio, SecurityStock securityStock);

    /**
     * Enhanced: Find position by portfolio and security symbol
     */
    @Query("SELECT p FROM Position p JOIN FETCH p.securityStock s " +
            "WHERE p.portfolio.portfolioId = :portfolioId AND s.symbol = :symbol")
    Optional<Position> findByPortfolioIdAndSymbol(@Param("portfolioId") Long portfolioId,
                                                  @Param("symbol") String symbol);

    List<Position> findBySecurityStock(SecurityStock securityStock);

    // ===== PERFORMANCE AND ANALYTICS QUERIES =====

    /**
     * Enhanced: Get positions with gains above threshold
     */
    @Query("SELECT p FROM Position p JOIN FETCH p.securityStock " +
            "WHERE p.portfolio.portfolioId = :portfolioId " +
            "AND p.unrealizedGainLoss > :threshold AND p.isActive = true " +
            "ORDER BY p.unrealizedGainLoss DESC")
    List<Position> findPositionsWithGainsAbove(@Param("portfolioId") Long portfolioId,
                                               @Param("threshold") BigDecimal threshold);

    /**
     * Enhanced: Get positions with losses below threshold
     */
    @Query("SELECT p FROM Position p JOIN FETCH p.securityStock " +
            "WHERE p.portfolio.portfolioId = :portfolioId " +
            "AND p.unrealizedGainLoss < :threshold AND p.isActive = true " +
            "ORDER BY p.unrealizedGainLoss ASC")
    List<Position> findPositionsWithLossesBelow(@Param("portfolioId") Long portfolioId,
                                                @Param("threshold") BigDecimal threshold);

    /**
     * Enhanced: Find top performing positions by percentage
     */
    @Query("SELECT p FROM Position p JOIN FETCH p.securityStock " +
            "WHERE p.portfolio.portfolioId = :portfolioId AND p.isActive = true " +
            "AND p.quantity > 0 AND p.avgCostPerShare > 0 " +
            "ORDER BY (p.unrealizedGainLoss / (p.quantity * p.avgCostPerShare)) DESC")
    List<Position> findTopPerformingByPercentage(@Param("portfolioId") Long portfolioId);

    /**
     * Enhanced: Find bottom performing positions by percentage
     */
    @Query("SELECT p FROM Position p JOIN FETCH p.securityStock " +
            "WHERE p.portfolio.portfolioId = :portfolioId AND p.isActive = true " +
            "AND p.quantity > 0 AND p.avgCostPerShare > 0 " +
            "ORDER BY (p.unrealizedGainLoss / (p.quantity * p.avgCostPerShare)) ASC")
    List<Position> findWorstPerformingByPercentage(@Param("portfolioId") Long portfolioId);

    // ===== AGGREGATION QUERIES =====

    /**
     * Get total unrealized gain/loss for active positions
     */
    @Query("SELECT COALESCE(SUM(p.unrealizedGainLoss), 0) FROM Position p " +
            "WHERE p.portfolio.portfolioId = :portfolioId AND p.isActive = true")
    BigDecimal getTotalUnrealizedGainLoss(@Param("portfolioId") Long portfolioId);

    /**
     * Get total realized gain/loss for all positions
     */
    @Query("SELECT COALESCE(SUM(p.realizedGainLoss), 0) FROM Position p " +
            "WHERE p.portfolio.portfolioId = :portfolioId")
    BigDecimal getTotalRealizedGainLoss(@Param("portfolioId") Long portfolioId);

    /**
     * Get total current value of active positions
     */
    @Query("SELECT COALESCE(SUM(p.currentValue), 0) FROM Position p " +
            "WHERE p.portfolio.portfolioId = :portfolioId AND p.isActive = true")
    BigDecimal getTotalCurrentValue(@Param("portfolioId") Long portfolioId);

    /**
     * Enhanced: Get total cost basis for active positions
     */
    @Query("SELECT COALESCE(SUM(p.quantity * p.avgCostPerShare), 0) FROM Position p " +
            "WHERE p.portfolio.portfolioId = :portfolioId AND p.isActive = true")
    BigDecimal getTotalCostBasis(@Param("portfolioId") Long portfolioId);

    /**
     * Enhanced: Get portfolio summary statistics
     */
    @Query("SELECT new map(" +
            "COUNT(p) as totalPositions, " +
            "SUM(CASE WHEN p.isActive = true THEN 1 ELSE 0 END) as activePositions, " +
            "COALESCE(SUM(CASE WHEN p.isActive = true THEN p.currentValue ELSE 0 END), 0) as totalValue, " +
            "COALESCE(SUM(CASE WHEN p.isActive = true THEN p.unrealizedGainLoss ELSE 0 END), 0) as unrealizedGainLoss, " +
            "COALESCE(SUM(p.realizedGainLoss), 0) as realizedGainLoss" +
            ") FROM Position p WHERE p.portfolio.portfolioId = :portfolioId")
    Object getPortfolioSummary(@Param("portfolioId") Long portfolioId);

    // ===== FILTERING AND SEARCH QUERIES =====

    /**
     * Enhanced: Find positions by minimum value threshold
     */
    @Query("SELECT p FROM Position p JOIN FETCH p.securityStock " +
            "WHERE p.portfolio.portfolioId = :portfolioId AND p.isActive = true " +
            "AND p.currentValue >= :minValue ORDER BY p.currentValue DESC")
    List<Position> findByMinValue(@Param("portfolioId") Long portfolioId,
                                  @Param("minValue") BigDecimal minValue);

    /**
     * Enhanced: Find positions by quantity range
     */
    @Query("SELECT p FROM Position p JOIN FETCH p.securityStock " +
            "WHERE p.portfolio.portfolioId = :portfolioId AND p.isActive = true " +
            "AND p.quantity BETWEEN :minQuantity AND :maxQuantity")
    List<Position> findByQuantityRange(@Param("portfolioId") Long portfolioId,
                                       @Param("minQuantity") BigDecimal minQuantity,
                                       @Param("maxQuantity") BigDecimal maxQuantity);

    /**
     * Enhanced: Find positions updated after specific date
     */
    @Query("SELECT p FROM Position p JOIN FETCH p.securityStock " +
            "WHERE p.portfolio.portfolioId = :portfolioId " +
            "AND p.lastUpdated >= :afterDate ORDER BY p.lastUpdated DESC")
    List<Position> findUpdatedAfter(@Param("portfolioId") Long portfolioId,
                                    @Param("afterDate") Instant afterDate);

    /**
     * Enhanced: Find positions by security sector or industry (assuming these fields exist)
     */
    @Query("SELECT p FROM Position p JOIN FETCH p.securityStock s " +
            "WHERE p.portfolio.portfolioId = :portfolioId AND p.isActive = true " +
            "AND s.sector = :sector")
    List<Position> findBySector(@Param("portfolioId") Long portfolioId,
                                @Param("sector") SecuritySector sector);
    // ===== COUNT AND STATISTICS =====

    /**
     * Count active positions in portfolio
     */
    @Query("SELECT COUNT(p) FROM Position p WHERE p.portfolio.portfolioId = :portfolioId AND p.isActive = true")
    Long countActiveByPortfolioId(@Param("portfolioId") Long portfolioId);

    /**
     * Count positions with gains
     */
    @Query("SELECT COUNT(p) FROM Position p WHERE p.portfolio.portfolioId = :portfolioId " +
            "AND p.isActive = true AND p.unrealizedGainLoss > 0")
    Long countPositionsWithGains(@Param("portfolioId") Long portfolioId);

    /**
     * Count positions with losses
     */
    @Query("SELECT COUNT(p) FROM Position p WHERE p.portfolio.portfolioId = :portfolioId " +
            "AND p.isActive = true AND p.unrealizedGainLoss < 0")
    Long countPositionsWithLosses(@Param("portfolioId") Long portfolioId);

    // ===== USER-LEVEL QUERIES =====

    /**
     * Find all active positions by user ID
     */
    @Query("SELECT p FROM Position p JOIN FETCH p.securityStock " +
            "WHERE p.portfolio.userAccount.userId = :userId AND p.isActive = true " +
            "ORDER BY p.currentValue DESC")
    List<Position> findActiveByUserId(@Param("userId") Long userId);

    /**
     * Enhanced: Get user's total portfolio value across all portfolios
     */
    @Query("SELECT COALESCE(SUM(p.currentValue), 0) FROM Position p " +
            "WHERE p.portfolio.userAccount.userId = :userId AND p.isActive = true")
    BigDecimal getTotalValueByUserId(@Param("userId") Long userId);

    // ===== BATCH UPDATE OPERATIONS =====

    /**
     * Enhanced: Batch update current values for positions needing refresh
     */
    @Modifying
    @Transactional
    @Query("UPDATE Position p SET p.lastUpdated = :updateTime " +
            "WHERE p.portfolio.portfolioId = :portfolioId AND p.isActive = true")
    int touchPositionsForPortfolio(@Param("portfolioId") Long portfolioId,
                                   @Param("updateTime") Instant updateTime);

    /**
     * Enhanced: Mark positions as inactive when quantity is zero
     */
    @Modifying
    @Transactional
    @Query("UPDATE Position p SET p.isActive = false, p.lastUpdated = :updateTime " +
            "WHERE p.quantity = 0 AND p.isActive = true")
    int deactivateZeroQuantityPositions(@Param("updateTime") Instant updateTime);

    /**
     * Enhanced: Find stale positions that need value updates
     */
    @Query("SELECT p FROM Position p JOIN FETCH p.securityStock " +
            "WHERE p.isActive = true AND p.lastUpdated < :staleThreshold " +
            "ORDER BY p.lastUpdated ASC")
    List<Position> findStalePositions(@Param("staleThreshold") Instant staleThreshold);

    // ===== SPECIALIZED QUERIES =====

    /**
     * Enhanced: Find positions that might need rebalancing (over/under weight)
     */
    @Query("SELECT p FROM Position p JOIN FETCH p.securityStock " +
            "WHERE p.portfolio.portfolioId = :portfolioId AND p.isActive = true " +
            "AND p.currentValue > :portfolioValue * :maxWeight / 100")
    List<Position> findOverweightPositions(@Param("portfolioId") Long portfolioId,
                                           @Param("portfolioValue") BigDecimal portfolioValue,
                                           @Param("maxWeight") BigDecimal maxWeight);

    /**
     * Enhanced: Find positions with significant day changes
     */
    @Query("SELECT p FROM Position p JOIN FETCH p.securityStock " +
            "WHERE p.portfolio.portfolioId = :portfolioId AND p.isActive = true " +
            "AND ABS(p.dayChangePercent) > :threshold " +
            "ORDER BY ABS(p.dayChangePercent) DESC")
    List<Position> findPositionsWithSignificantDayChanges(@Param("portfolioId") Long portfolioId,
                                                          @Param("threshold") BigDecimal threshold);

    // ===== LEGACY COMPATIBILITY =====

    /**
     * Find all positions for a specific portfolio by ID (original method)
     */
    @Query("SELECT p FROM Position p WHERE p.portfolio.portfolioId = :portfolioId")
    List<Position> findByPortfolioId(@Param("portfolioId") Long portfolioId);

    /**
     * Find positions by portfolio ID and security symbol (original method)
     */
    @Query("SELECT p FROM Position p WHERE p.portfolio.portfolioId = :portfolioId " +
            "AND p.securityStock.symbol = :symbol")
    List<Position> findByPortfolioIdAndSecuritySymbol(@Param("portfolioId") Long portfolioId,
                                                      @Param("symbol") String symbol);

    /**
     * Original method maintained for compatibility
     */
    @Query("SELECT p FROM Position p JOIN FETCH p.securityStock WHERE p.portfolio = :portfolio")
    List<Position> findByPortfolioWithSecurity(@Param("portfolio") Portfolio portfolio);
}