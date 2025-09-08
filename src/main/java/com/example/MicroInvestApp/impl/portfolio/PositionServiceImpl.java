package com.example.MicroInvestApp.impl.portfolio;

import com.example.MicroInvestApp.domain.portfolio.Position;
import com.example.MicroInvestApp.domain.portfolio.Portfolio;
import com.example.MicroInvestApp.domain.market.SecurityStock;
import com.example.MicroInvestApp.domain.enums.TransactionType;
import com.example.MicroInvestApp.dto.portfolio.RequestDTOs.UpdatePositionRequestDTO;
import com.example.MicroInvestApp.dto.portfolio.ResponseDTOs.PositionResponseDTO;
import com.example.MicroInvestApp.repositories.portfolio.PositionRepository;
import com.example.MicroInvestApp.repositories.portfolio.PortfolioRepository;
import com.example.MicroInvestApp.repositories.market.SecurityStockRepository;
import com.example.MicroInvestApp.service.portfolio.PositionDayChangeService;
import com.example.MicroInvestApp.service.portfolio.PositionService;
import com.example.MicroInvestApp.service.market.MarketDataService;
import com.example.MicroInvestApp.exception.portfolio.PositionNotFoundException;
import com.example.MicroInvestApp.exception.portfolio.PortfolioNotFoundException;
import com.example.MicroInvestApp.exception.portfolio.PositionCalculationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Enhanced PositionService implementation with improved error handling,
 * performance optimizations, and comprehensive business logic
 */
@Service
@Transactional
public class PositionServiceImpl implements PositionService {

    private static final Logger logger = LoggerFactory.getLogger(PositionServiceImpl.class);
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final int RETRY_COUNT = 3;
    private static final BigDecimal MIN_PRICE = new BigDecimal("0.01");

    private final PositionRepository positionRepository;
    private final PortfolioRepository portfolioRepository;
    private final SecurityStockRepository securityStockRepository;
    private final MarketDataService marketDataService;
    private final PositionDayChangeService positionDayChangeService;

    @Autowired
    public PositionServiceImpl(PositionRepository positionRepository,
                               PortfolioRepository portfolioRepository,
                               SecurityStockRepository securityStockRepository,
                               MarketDataService marketDataService,
                               PositionDayChangeService positionDayChangeService) {
        this.positionRepository = positionRepository;
        this.portfolioRepository = portfolioRepository;
        this.securityStockRepository = securityStockRepository;
        this.marketDataService = marketDataService;
        this.positionDayChangeService = positionDayChangeService;
    }

    // ===== ENHANCED TRANSACTION PROCESSING =====

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void updatePositionFromTransaction(Long portfolioId, String stockSymbol,
                                              TransactionType transactionType,
                                              BigDecimal quantity, BigDecimal pricePerShare) {

        validateTransactionInputs(portfolioId, stockSymbol, transactionType, quantity, pricePerShare);

        logger.info("Processing {} transaction: {} shares of {} at ${} for portfolio {}",
                transactionType, quantity, stockSymbol, pricePerShare, portfolioId);

        Portfolio portfolio = getPortfolioOrThrow(portfolioId);
        SecurityStock security = getSecurityOrThrow(stockSymbol);

        int attempt = 0;
        while (attempt < RETRY_COUNT) {
            try {
                processTransactionWithOptimisticLocking(portfolio, security, transactionType, quantity, pricePerShare);
                break;
            } catch (OptimisticLockingFailureException e) {
                attempt++;
                if (attempt >= RETRY_COUNT) {
                    throw new PositionCalculationException("Failed to update position after " + RETRY_COUNT + " attempts", e);
                }
                logger.warn("Optimistic locking failure, retrying attempt {}", attempt + 1);
                try {
                    Thread.sleep(100 * attempt); // Progressive backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Transaction processing interrupted", ie);
                }
            }
        }

        logger.info("Successfully processed {} transaction for {} in portfolio {}",
                transactionType, stockSymbol, portfolioId);
    }

    private void processTransactionWithOptimisticLocking(Portfolio portfolio, SecurityStock security,
                                                         TransactionType transactionType,
                                                         BigDecimal quantity, BigDecimal pricePerShare) {
        Optional<Position> existingPosition = positionRepository.findByPortfolioAndSecurityStock(portfolio, security);
        Position position;

        if (existingPosition.isPresent()) {
            position = existingPosition.get();
            updateExistingPosition(position, transactionType, quantity, pricePerShare);
        } else {
            if (transactionType == TransactionType.SELL) {
                throw new IllegalArgumentException("Cannot sell shares that are not owned for " + security.getSymbol());
            }
            position = createNewPosition(portfolio, security, quantity, pricePerShare);
        }

        updatePositionCurrentValue(position);
        positionRepository.save(position);
        updatePortfolioTotalValue(portfolio);
    }

    // ===== ENHANCED POSITION UPDATES =====

    @Override
    public void updatePosition(Long portfolioId, String stockSymbol, UpdatePositionRequestDTO updateRequest) {
        logger.info("Updating position for {} in portfolio {} with request: {}",
                stockSymbol, portfolioId, updateRequest);

        Portfolio portfolio = getPortfolioOrThrow(portfolioId);
        SecurityStock security = getSecurityOrThrow(stockSymbol);

        Position position = positionRepository.findByPortfolioAndSecurityStock(portfolio, security)
                .orElseThrow(() -> new PositionNotFoundException(
                        "Position not found for " + stockSymbol + " in portfolio " + portfolioId));

        // Validate the update request
        if (!updateRequest.hasValidQuantityAndPrice()) {
            throw new IllegalArgumentException("Invalid update request: quantity and price must be valid");
        }

        // Update position fields
        if (updateRequest.getQuantity() != null) {
            position.setQuantity(updateRequest.getQuantity());
        }

        if (updateRequest.getAvgCostPerShare() != null) {
            position.setAvgCostPerShare(updateRequest.getAvgCostPerShare());
        }

        // Recalculate values if requested or if market value update is enabled
        if (Boolean.TRUE.equals(updateRequest.getForceRecalculate()) ||
                Boolean.TRUE.equals(updateRequest.getUpdateMarketValue())) {
            updatePositionCurrentValue(position);
        }

        positionRepository.save(position);
        updatePortfolioTotalValue(portfolio);

        logger.info("Successfully updated position for {} in portfolio {}", stockSymbol, portfolioId);
    }

    @Override
    @Transactional(readOnly = false)
    public void updatePositionValues(Long portfolioId) {
        logger.info("Updating all position values for portfolio: {}", portfolioId);

        Portfolio portfolio = getPortfolioOrThrow(portfolioId);
        List<Position> positions = positionRepository.findByPortfolioWithSecurity(portfolio)
                .stream()
                .filter(p -> p.getIsActive())
                .collect(Collectors.toList());

        if (positions.isEmpty()) {
            logger.info("No active positions found for portfolio {}", portfolioId);
            return;
        }

        int updatedCount = 0;
        for (Position position : positions) {
            try {
                if (updatePositionCurrentValue(position)) {
                    positionRepository.save(position);
                    updatedCount++;
                }
            } catch (Exception e) {
                logger.error("Failed to update position value for {} in portfolio {}: {}",
                        position.getSecurityStock().getSymbol(), portfolioId, e.getMessage());
            }
        }

        // Batch update to mark positions as recently updated
        positionRepository.touchPositionsForPortfolio(portfolioId, Instant.now());
        updatePortfolioTotalValue(portfolio);

        logger.info("Updated {} out of {} position values for portfolio {}",
                updatedCount, positions.size(), portfolioId);
    }

    @Override
    @Async
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void updateAllPositionValues() {
        logger.info("Starting scheduled update of all position values");
        Instant startTime = Instant.now();

        try {
            // First, deactivate positions with zero quantity
            int deactivatedCount = positionRepository.deactivateZeroQuantityPositions(Instant.now());
            if (deactivatedCount > 0) {
                logger.info("Deactivated {} positions with zero quantity", deactivatedCount);
            }

            // Get all active portfolios
            List<Portfolio> activePortfolios = portfolioRepository.findAllActive();
            logger.info("Found {} active portfolios to update", activePortfolios.size());

            // Process portfolios in parallel
            List<CompletableFuture<Void>> futures = activePortfolios.stream()
                    .map(portfolio -> CompletableFuture.runAsync(() -> {
                        try {
                            updatePositionValues(portfolio.getPortfolioId());
                        } catch (Exception e) {
                            logger.error("Failed to update positions for portfolio {}: {}",
                                    portfolio.getPortfolioId(), e.getMessage());
                        }
                    }))
                    .collect(Collectors.toList());

            // Wait for all updates to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            Instant endTime = Instant.now();
            long duration = ChronoUnit.SECONDS.between(startTime, endTime);
            logger.info("Completed scheduled update of all position values for {} portfolios in {} seconds",
                    activePortfolios.size(), duration);

        } catch (Exception e) {
            logger.error("Error during scheduled position values update: {}", e.getMessage(), e);
            throw new PositionCalculationException("Scheduled update failed", e);
        }
    }

    // ===== ENHANCED QUERY METHODS =====

    @Override
    @Transactional(readOnly = true)
    public List<PositionResponseDTO> getPositionsByPortfolio(Long portfolioId) {
        logger.debug("Retrieving positions for portfolio: {}", portfolioId);

        Portfolio portfolio = getPortfolioOrThrow(portfolioId);
        List<Position> positions = positionRepository.findByPortfolioWithSecurity(portfolio)
                .stream()
                .filter(p -> p.getIsActive() != null && p.getIsActive())
                .collect(Collectors.toList());

        BigDecimal portfolioTotalValue = getTotalPortfolioValue(portfolioId);

        return positions.stream()
                .map(position -> convertToResponseDTO(position, portfolioTotalValue))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PositionResponseDTO getPosition(Long portfolioId, String stockSymbol) {
        Portfolio portfolio = getPortfolioOrThrow(portfolioId);

        Position position = positionRepository.findByPortfolioIdAndSymbol(portfolioId, stockSymbol)
                .orElseThrow(() -> new PositionNotFoundException(
                        "Position not found for " + stockSymbol + " in portfolio " + portfolioId));

        BigDecimal portfolioTotalValue = getTotalPortfolioValue(portfolioId);
        return convertToResponseDTO(position, portfolioTotalValue);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCurrentQuantity(Long portfolioId, String stockSymbol) {
        try {
            Position position = positionRepository.findByPortfolioIdAndSymbol(portfolioId, stockSymbol)
                    .orElse(null);
            return position != null && position.getIsActive() ? position.getQuantity() : BigDecimal.ZERO;
        } catch (Exception e) {
            logger.warn("Error getting quantity for {} in portfolio {}: {}", stockSymbol, portfolioId, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCurrentValue(Long portfolioId, String stockSymbol) {
        try {
            Position position = positionRepository.findByPortfolioIdAndSymbol(portfolioId, stockSymbol)
                    .orElse(null);
            return position != null && position.getIsActive() ? position.getCurrentValue() : BigDecimal.ZERO;
        } catch (Exception e) {
            logger.warn("Error getting value for {} in portfolio {}: {}", stockSymbol, portfolioId, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPortfolioValue(Long portfolioId) {
        Portfolio portfolio = getPortfolioOrThrow(portfolioId);
        BigDecimal positionValue = positionRepository.getTotalCurrentValue(portfolioId);
        return portfolio.getCashBalance().add(positionValue);
    }

    // ===== ENHANCED REAL-TIME UPDATES =====

    @Override
    @Transactional
    public void refreshPositionValue(Long positionId) {
        logger.info("Refreshing value for position: {}", positionId);

        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new PositionNotFoundException("Position not found: " + positionId));

        try {
            // Fetch latest market data
            String symbol = position.getSecurityStock().getSymbol();
            marketDataService.updateCurrentPrice(symbol);

            // Update position value
            if (updatePositionCurrentValue(position)) {
                positionRepository.save(position);
                updatePortfolioTotalValue(position.getPortfolio());
                logger.info("Position {} value refreshed successfully", positionId);
            } else {
                logger.info("Position {} value unchanged after refresh", positionId);
            }
        } catch (Exception e) {
            logger.error("Failed to refresh position {} value: {}", positionId, e.getMessage());
            throw new PositionCalculationException("Failed to refresh position value", e);
        }
    }

    @Override
    @Transactional
    public void refreshPortfolioPositions(Long portfolioId) {
        logger.info("Refreshing all positions for portfolio: {}", portfolioId);

        Portfolio portfolio = getPortfolioOrThrow(portfolioId);
        List<Position> positions = positionRepository.findActiveByPortfolioIdWithSecurity(portfolioId);

        int refreshedCount = 0;
        for (Position position : positions) {
            try {
                String symbol = position.getSecurityStock().getSymbol();
                marketDataService.updateCurrentPrice(symbol);

                if (updatePositionCurrentValue(position)) {
                    positionRepository.save(position);
                    refreshedCount++;
                }
            } catch (Exception e) {
                logger.error("Failed to refresh position for {} in portfolio {}: {}",
                        position.getSecurityStock().getSymbol(), portfolioId, e.getMessage());
            }
        }

        updatePortfolioTotalValue(portfolio);
        logger.info("Refreshed {} out of {} positions for portfolio {}",
                refreshedCount, positions.size(), portfolioId);
    }

    // ===== ENHANCED ANALYTICS =====

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getUnrealizedGainLoss(Long portfolioId, String stockSymbol) {
        return positionRepository.findByPortfolioIdAndSymbol(portfolioId, stockSymbol)
                .map(Position::getUnrealizedGainLoss)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getRealizedGainLoss(Long portfolioId, String stockSymbol) {
        return positionRepository.findByPortfolioIdAndSymbol(portfolioId, stockSymbol)
                .map(Position::getRealizedGainLoss)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalUnrealizedGainLoss(Long portfolioId) {
        return positionRepository.getTotalUnrealizedGainLoss(portfolioId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalRealizedGainLoss(Long portfolioId) {
        return positionRepository.getTotalRealizedGainLoss(portfolioId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasSufficientShares(Long portfolioId, String symbol, BigDecimal requiredQuantity) {
        BigDecimal currentQuantity = getCurrentQuantity(portfolioId, symbol);
        return currentQuantity.compareTo(requiredQuantity) >= 0;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAverageCostBasis(Long portfolioId, String symbol) {
        return positionRepository.findByPortfolioIdAndSymbol(portfolioId, symbol)
                .map(Position::getAvgCostPerShare)
                .orElse(BigDecimal.ZERO);
    }

    // ===== NEW ENHANCED METHODS =====

    @Override
    @Transactional(readOnly = true)
    public List<PositionResponseDTO> getTopPerformingPositions(Long portfolioId, int limit) {
        Portfolio portfolio = getPortfolioOrThrow(portfolioId);
        List<Position> positions = positionRepository.findByPortfolioWithSecurity(portfolio);
        BigDecimal portfolioTotalValue = getTotalPortfolioValue(portfolioId);

        return positions.stream()
                .filter(p -> p.getIsActive() && p.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .sorted((p1, p2) -> {
                    BigDecimal gain1 = calculateGainLossPercentage(p1);
                    BigDecimal gain2 = calculateGainLossPercentage(p2);
                    return gain2.compareTo(gain1); // Descending order
                })
                .limit(limit)
                .map(position -> convertToResponseDTO(position, portfolioTotalValue))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionResponseDTO> getWorstPerformingPositions(Long portfolioId, int limit) {
        Portfolio portfolio = getPortfolioOrThrow(portfolioId);
        BigDecimal portfolioTotalValue = getTotalPortfolioValue(portfolioId);

        return positionRepository.findWorstPerformingByPercentage(portfolioId).stream()
                .limit(limit)
                .map(position -> convertToResponseDTO(position, portfolioTotalValue))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionResponseDTO> getPositionsRequiringAttention(Long portfolioId) {
        Portfolio portfolio = getPortfolioOrThrow(portfolioId);
        BigDecimal portfolioTotalValue = getTotalPortfolioValue(portfolioId);
        BigDecimal threshold = new BigDecimal("5.0"); // 5% change threshold

        return positionRepository.findPositionsWithSignificantDayChanges(portfolioId, threshold).stream()
                .map(position -> convertToResponseDTO(position, portfolioTotalValue))
                .collect(Collectors.toList());
    }

    // ===== HELPER METHODS =====

    private void validateTransactionInputs(Long portfolioId, String stockSymbol,
                                           TransactionType transactionType,
                                           BigDecimal quantity, BigDecimal pricePerShare) {
        if (portfolioId == null || portfolioId <= 0) {
            throw new IllegalArgumentException("Portfolio ID must be positive");
        }
        if (stockSymbol == null || stockSymbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Stock symbol cannot be null or empty");
        }
        if (transactionType == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (pricePerShare == null || pricePerShare.compareTo(MIN_PRICE) < 0) {
            throw new IllegalArgumentException("Price per share must be at least " + MIN_PRICE);
        }
    }

    private Portfolio getPortfolioOrThrow(Long portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: " + portfolioId));
    }

    private SecurityStock getSecurityOrThrow(String stockSymbol) {
        return securityStockRepository.findBySymbol(stockSymbol)
                .orElseThrow(() -> new RuntimeException("Security not found: " + stockSymbol));
    }

    private void updateExistingPosition(Position position, TransactionType transactionType,
                                        BigDecimal quantity, BigDecimal pricePerShare) {
        BigDecimal currentQuantity = position.getQuantity();
        BigDecimal currentAvgCost = position.getAvgCostPerShare();

        if (transactionType == TransactionType.BUY) {
            // Calculate new average cost using weighted average
            BigDecimal totalCost = currentQuantity.multiply(currentAvgCost).add(quantity.multiply(pricePerShare));
            BigDecimal newQuantity = currentQuantity.add(quantity);

            if (newQuantity.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal newAvgCost = totalCost.divide(newQuantity, 4, RoundingMode.HALF_UP);
                position.setAvgCostPerShare(newAvgCost);
                position.setQuantity(newQuantity);
                position.setIsActive(true);
            } else {
                throw new IllegalStateException("Position quantity cannot be zero after buy transaction");
            }

        } else if (transactionType == TransactionType.SELL) {
            if (currentQuantity.compareTo(quantity) < 0) {
                throw new IllegalArgumentException(
                        String.format("Cannot sell %s shares, only %s available", quantity, currentQuantity));
            }

            BigDecimal newQuantity = currentQuantity.subtract(quantity);
            position.setQuantity(newQuantity);

            // Calculate realized gain/loss for sold shares
            BigDecimal sellValue = quantity.multiply(pricePerShare);
            BigDecimal costBasis = quantity.multiply(currentAvgCost);
            BigDecimal realizedGainLoss = sellValue.subtract(costBasis);
            position.setRealizedGainLoss(position.getRealizedGainLoss().add(realizedGainLoss));

            // If position is completely sold, mark as inactive
            if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
                position.closePosition();
            }
        }
    }

    private Position createNewPosition(Portfolio portfolio, SecurityStock security,
                                       BigDecimal quantity, BigDecimal pricePerShare) {
        return new Position(portfolio, security, quantity, pricePerShare);
    }

    private boolean updatePositionCurrentValue(Position position) {
        if (position.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
            boolean changed = position.getCurrentValue().compareTo(BigDecimal.ZERO) != 0 ||
                    position.getUnrealizedGainLoss().compareTo(BigDecimal.ZERO) != 0;
            position.setCurrentValue(BigDecimal.ZERO);
            position.setUnrealizedGainLoss(BigDecimal.ZERO);
            position.setDayChange(BigDecimal.ZERO);
            position.setDayChangePercent(BigDecimal.ZERO);
            position.setIsActive(false);
            return changed;
        }

        BigDecimal currentMarketPrice = position.getSecurityStock().getCurrentPrice();
        if (currentMarketPrice == null || currentMarketPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        BigDecimal currentValue = position.getQuantity().multiply(currentMarketPrice);
        BigDecimal costBasis = position.getQuantity().multiply(position.getAvgCostPerShare());
        BigDecimal unrealizedGainLoss = currentValue.subtract(costBasis);

        position.setCurrentValue(currentValue);
        position.setUnrealizedGainLoss(unrealizedGainLoss);

        // Use the day change service for proper calculations
        positionDayChangeService.updatePositionDayChange(position);

        return true;
    }

    private void updatePortfolioTotalValue(Portfolio portfolio) {
        try {
            BigDecimal totalPositionValue = positionRepository.getTotalCurrentValue(portfolio.getPortfolioId());
            BigDecimal totalValue = portfolio.getCashBalance().add(totalPositionValue);
            portfolio.setTotalValue(totalValue);
            portfolioRepository.save(portfolio);
        } catch (Exception e) {
            logger.error("Failed to update portfolio total value for portfolio {}: {}",
                    portfolio.getPortfolioId(), e.getMessage());
        }
    }

    // Add helper method:
    private BigDecimal calculateGainLossPercentage(Position position) {
        BigDecimal costBasis = position.getQuantity().multiply(position.getAvgCostPerShare());
        if (costBasis.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return position.getUnrealizedGainLoss()
                .divide(costBasis, 4, RoundingMode.HALF_UP)
                .multiply(HUNDRED);
    }


    private PositionResponseDTO convertToResponseDTO(Position position, BigDecimal portfolioTotalValue) {
        PositionResponseDTO dto = new PositionResponseDTO();

        // Basic position data
        dto.setPositionId(position.getPositionId());
        dto.setPortfolioId(position.getPortfolio().getPortfolioId());
        dto.setPortfolioName(position.getPortfolio().getPortfolioName());
        dto.setSecurityId(position.getSecurityStock().getSecurityId());
        dto.setSecuritySymbol(position.getSecurityStock().getSymbol());
        dto.setCompanyName(position.getSecurityStock().getCompanyName());

        // Quantity and pricing
        dto.setQuantity(position.getQuantity());
        dto.setAvgCostPerShare(position.getAvgCostPerShare());
        dto.setCurrentPrice(position.getSecurityStock().getCurrentPrice());

        // Ensure current value is recalculated from current price
        if (position.getQuantity() != null && position.getSecurityStock().getCurrentPrice() != null) {
            dto.setCurrentValue(position.getQuantity().multiply(position.getSecurityStock().getCurrentPrice()));
        } else {
            dto.setCurrentValue(position.getCurrentValue());
        }

        // Gain/loss information
        dto.setUnrealizedGainLoss(position.getUnrealizedGainLoss());
        dto.setRealizedGainLoss(position.getRealizedGainLoss());

        // Performance metrics
        dto.setDayChange(position.getDayChange());
        dto.setDayChangePercent(position.getDayChangePercent());
        dto.setIsActive(position.getIsActive());

        // Timestamps
        dto.setOpenDate(position.getOpenDate());
        dto.setLastUpdated(position.getLastUpdated());

        // Set portfolio weight
        dto.setPortfolioWeightFromTotal(portfolioTotalValue);

        // Calculate all derived values
        try {
            dto.calculateDerivedValues();
        } catch (Exception e) {
            logger.warn("Error calculating derived values for position {}: {}",
                    position.getPositionId(), e.getMessage());
            // Set safe defaults
            dto.setHoldingPeriodDays(0L);
            dto.setAnnualizedReturn(BigDecimal.ZERO);
            dto.setUnrealizedGainLossPercentage(BigDecimal.ZERO);
        }

        return dto;
    }
}