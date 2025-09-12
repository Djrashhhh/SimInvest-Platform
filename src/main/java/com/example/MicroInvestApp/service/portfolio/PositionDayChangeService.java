package com.example.MicroInvestApp.service.portfolio;

import com.example.MicroInvestApp.domain.portfolio.Position;
import com.example.MicroInvestApp.domain.market.SecurityStock;
import com.example.MicroInvestApp.domain.market.PriceHistory;
import com.example.MicroInvestApp.repositories.portfolio.PositionRepository;
import com.example.MicroInvestApp.repositories.market.PriceHistoryRepository;
import com.example.MicroInvestApp.repositories.market.SecurityStockRepository;
import com.example.MicroInvestApp.service.market.DailyPriceTrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing day change calculations in positions
 * Integrates with the DailyPriceTrackingService to provide accurate day-to-day changes
 */
@Service
@Transactional
public class PositionDayChangeService {

    private static final Logger logger = LoggerFactory.getLogger(PositionDayChangeService.class);
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final PositionRepository positionRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final SecurityStockRepository securityStockRepository;
    private final DailyPriceTrackingService dailyPriceTrackingService;

    @Autowired
    public PositionDayChangeService(PositionRepository positionRepository,
                                    PriceHistoryRepository priceHistoryRepository,
                                    SecurityStockRepository securityStockRepository,
                                    DailyPriceTrackingService dailyPriceTrackingService) {
        this.positionRepository = positionRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.securityStockRepository = securityStockRepository;
        this.dailyPriceTrackingService = dailyPriceTrackingService;
    }

    /**
     * Update day change for a specific position using historical data
     */
    public void updatePositionDayChange(Long positionId) {
        Position position = positionRepository.findById(positionId).orElse(null);
        if (position == null) {
            logger.warn("Position not found: {}", positionId);
            return;
        }

        updatePositionDayChange(position);
    }

    /**
     * Update day change for a position entity
     */
    public void updatePositionDayChange(Position position) {
        try {
            SecurityStock security = position.getSecurityStock();
            if (security == null || position.getQuantity() == null ||
                    position.getQuantity().compareTo(BigDecimal.ZERO) == 0) {

                // Set to zero for inactive positions
                position.setDayChange(BigDecimal.ZERO);
                position.setDayChangePercent(BigDecimal.ZERO);
                positionRepository.save(position);
                return;
            }

            // Get previous market day's closing price
            LocalDate previousMarketDay = getPreviousMarketDay(LocalDate.now());
            BigDecimal previousClose = getPreviousClosingPrice(security, previousMarketDay);

            if (previousClose != null && security.getCurrentPrice() != null) {
                calculateDayChangeFromPrices(position, previousClose, security.getCurrentPrice());
            } else {
                // Fallback to using security's built-in day change data
                calculateDayChangeFromSecurity(position, security);
            }

            positionRepository.save(position);

        } catch (Exception e) {
            logger.error("Error updating day change for position {}: {}",
                    position.getPositionId(), e.getMessage());
        }
    }

    /**
     * Update day changes for all positions in a portfolio
     */
    public void updatePortfolioDayChanges(Long portfolioId) {
        logger.info("Updating day changes for all positions in portfolio {}", portfolioId);

        try {
            List<Position> positions = positionRepository.findByPortfolioIdWithSecurity(portfolioId);

            int updatedCount = 0;
            for (Position position : positions) {
                if (position.getIsActive()) {
                    updatePositionDayChange(position);
                    updatedCount++;
                }
            }

            logger.info("Updated day changes for {} positions in portfolio {}", updatedCount, portfolioId);

        } catch (Exception e) {
            logger.error("Error updating portfolio day changes for portfolio {}: {}", portfolioId, e.getMessage());
        }
    }

    /**
     * Update day changes for all active positions across all portfolios
     */
    public void updateAllPositionDayChanges() {
        logger.info("Updating day changes for all active positions");

        try {
            List<Position> activePositions = getAllActivePositions();

            int updatedCount = 0;
            int errorCount = 0;

            for (Position position : activePositions) {
                try {
                    updatePositionDayChange(position);
                    updatedCount++;
                } catch (Exception e) {
                    logger.error("Failed to update day change for position {}: {}",
                            position.getPositionId(), e.getMessage());
                    errorCount++;
                }
            }

            logger.info("Day change update completed - Success: {}, Errors: {}", updatedCount, errorCount);

        } catch (Exception e) {
            logger.error("Error during bulk day change update: {}", e.getMessage());
        }
    }

    /**
     * Get day change information for a specific position
     */
    @Transactional(readOnly = true)
    public PositionDayChangeInfo getPositionDayChangeInfo(Long positionId) {
        Position position = positionRepository.findById(positionId).orElse(null);
        if (position == null) {
            return null;
        }

        return new PositionDayChangeInfo(
                position.getPositionId(),
                position.getSecurityStock().getSymbol(),
                position.getCurrentValue(),
                position.getDayChange(),
                position.getDayChangePercent(),
                position.getLastUpdated()
        );
    }

    /**
     * Calculate position day change from current and previous prices
     */
    private void calculateDayChangeFromPrices(Position position, BigDecimal previousClose, BigDecimal currentPrice) {
        BigDecimal quantity = position.getQuantity();

        BigDecimal previousValue = quantity.multiply(previousClose);
        BigDecimal currentValue = quantity.multiply(currentPrice);
        BigDecimal dayChange = currentValue.subtract(previousValue);

        BigDecimal dayChangePercent = BigDecimal.ZERO;
        if (previousValue.compareTo(BigDecimal.ZERO) > 0) {
            dayChangePercent = dayChange.divide(previousValue, 4, RoundingMode.HALF_UP).multiply(HUNDRED);
        }

        position.setDayChange(dayChange);
        position.setDayChangePercent(dayChangePercent);

        logger.debug("Calculated day change for position {}: ${} ({}%)",
                position.getPositionId(), dayChange, dayChangePercent);
    }

    /**
     * Calculate position day change using security's price change data
     */
    private void calculateDayChangeFromSecurity(Position position, SecurityStock security) {
        if (security.getPriceChange() != null) {
            // Position day change = quantity * security price change
            BigDecimal dayChange = position.getQuantity().multiply(security.getPriceChange());
            position.setDayChange(dayChange);

            // Use security's percentage change
            if (security.getPriceChangePercent() != null) {
                position.setDayChangePercent(security.getPriceChangePercent());
            } else {
                position.setDayChangePercent(BigDecimal.ZERO);
            }
        } else {
            // No price change data available
            position.setDayChange(BigDecimal.ZERO);
            position.setDayChangePercent(BigDecimal.ZERO);
        }

        logger.debug("Used security day change for position {}: ${} ({}%)",
                position.getPositionId(), position.getDayChange(), position.getDayChangePercent());
    }

    /**
     * Get previous closing price for a security
     */
    private BigDecimal getPreviousClosingPrice(SecurityStock security, LocalDate date) {
        // First try to get from PriceHistory
        Optional<PriceHistory> priceHistory = priceHistoryRepository
                .findBySecurityStockAndDate(security, date);

        if (priceHistory.isPresent() && priceHistory.get().getClosePrice() != null) {
            return priceHistory.get().getClosePrice();
        }

        // Fallback to SecurityStock's previousClose field
        return security.getPreviousClose();
    }

    /**
     * Get all active positions with security data loaded
     */
    private List<Position> getAllActivePositions() {
        return positionRepository.findAll().stream()
                .filter(position -> position.getIsActive() != null && position.getIsActive())
                .toList();
    }

    /**
     * Get previous market day (excluding weekends)
     */
    private LocalDate getPreviousMarketDay(LocalDate date) {
        LocalDate previousDay = date.minusDays(1);

        while (!isMarketDay(previousDay)) {
            previousDay = previousDay.minusDays(1);
        }

        return previousDay;
    }

    /**
     * Check if given date is a market day
     */
    private boolean isMarketDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    /**
     * Fix null day change values for existing positions
     */
    public void fixNullDayChanges() {
        logger.info("Fixing null day change values for existing positions");

        try {
            List<Position> allPositions = positionRepository.findAll();
            int fixedCount = 0;

            for (Position position : allPositions) {
                boolean needsSaving = false;

                if (position.getDayChange() == null) {
                    position.setDayChange(BigDecimal.ZERO);
                    needsSaving = true;
                }

                if (position.getDayChangePercent() == null) {
                    position.setDayChangePercent(BigDecimal.ZERO);
                    needsSaving = true;
                }

                if (needsSaving) {
                    positionRepository.save(position);
                    fixedCount++;
                }
            }

            logger.info("Fixed null day change values for {} positions", fixedCount);

        } catch (Exception e) {
            logger.error("Error fixing null day changes: {}", e.getMessage());
        }
    }

    /**
     * Data class for position day change information
     */
    public static class PositionDayChangeInfo {
        private final Long positionId;
        private final String symbol;
        private final BigDecimal currentValue;
        private final BigDecimal dayChange;
        private final BigDecimal dayChangePercent;
        private final java.time.Instant lastUpdated;

        public PositionDayChangeInfo(Long positionId, String symbol, BigDecimal currentValue,
                                     BigDecimal dayChange, BigDecimal dayChangePercent,
                                     java.time.Instant lastUpdated) {
            this.positionId = positionId;
            this.symbol = symbol;
            this.currentValue = currentValue;
            this.dayChange = dayChange;
            this.dayChangePercent = dayChangePercent;
            this.lastUpdated = lastUpdated;
        }

        // Getters
        public Long getPositionId() { return positionId; }
        public String getSymbol() { return symbol; }
        public BigDecimal getCurrentValue() { return currentValue; }
        public BigDecimal getDayChange() { return dayChange; }
        public BigDecimal getDayChangePercent() { return dayChangePercent; }
        public java.time.Instant getLastUpdated() { return lastUpdated; }

        @Override
        public String toString() {
            return String.format("PositionDayChangeInfo{positionId=%d, symbol='%s', dayChange=%s, dayChangePercent=%s}",
                    positionId, symbol, dayChange, dayChangePercent);
        }
    }
}