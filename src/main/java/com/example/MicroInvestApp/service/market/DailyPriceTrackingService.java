package com.example.MicroInvestApp.service.market;

import com.example.MicroInvestApp.domain.market.PriceHistory;
import com.example.MicroInvestApp.domain.market.SecurityStock;
import com.example.MicroInvestApp.domain.portfolio.Position;
import com.example.MicroInvestApp.repositories.market.PriceHistoryRepository;
import com.example.MicroInvestApp.repositories.market.SecurityStockRepository;
import com.example.MicroInvestApp.repositories.portfolio.PositionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for tracking daily price changes and managing position day-change calculations
 */
@Service
public class DailyPriceTrackingService {

    private static final Logger logger = LoggerFactory.getLogger(DailyPriceTrackingService.class);
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final PriceHistoryRepository priceHistoryRepository;
    private final SecurityStockRepository securityStockRepository;
    private final PositionRepository positionRepository;

    @Autowired
    public DailyPriceTrackingService(PriceHistoryRepository priceHistoryRepository,
                                     SecurityStockRepository securityStockRepository,
                                     PositionRepository positionRepository) {
        this.priceHistoryRepository = priceHistoryRepository;
        this.securityStockRepository = securityStockRepository;
        this.positionRepository = positionRepository;
    }

    /**
     * Scheduled job: Capture end-of-day closing prices after market close
     * Runs at 4:05 PM EST on weekdays (after market close)
     */
    @Scheduled(cron = "0 5 16 * * MON-FRI", zone = "America/New_York")
    @Transactional
    public void captureEndOfDayClosingPrices() {
        if (!isMarketDay(LocalDate.now())) {
            logger.debug("Not a market day, skipping end-of-day capture");
            return;
        }

        logger.info("Starting end-of-day closing price capture");
        long startTime = System.currentTimeMillis();

        try {
            List<SecurityStock> activeSecurities = securityStockRepository.findByIsActiveTrue();
            LocalDate today = LocalDate.now();

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);

            for (SecurityStock security : activeSecurities) {
                try {
                    captureClosingPriceForSecurity(security, today);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    logger.error("Failed to capture closing price for {}: {}",
                            security.getSymbol(), e.getMessage());
                    errorCount.incrementAndGet();
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            logger.info("End-of-day capture completed in {}ms - Success: {}, Errors: {}",
                    duration, successCount.get(), errorCount.get());

        } catch (Exception e) {
            logger.error("Fatal error during end-of-day capture: {}", e.getMessage(), e);
        }
    }

    /**
     * Scheduled job: Calculate day changes using yesterday's closing prices
     * Runs at 9:35 AM EST on weekdays (after market open)
     */
    @Scheduled(cron = "0 35 9 * * MON-FRI", zone = "America/New_York")
    @Transactional
    public void calculateDailyChanges() {
        if (!isMarketDay(LocalDate.now())) {
            logger.debug("Not a market day, skipping daily change calculation");
            return;
        }

        logger.info("Starting daily change calculations");
        long startTime = System.currentTimeMillis();

        try {
            LocalDate today = LocalDate.now();
            LocalDate previousMarketDay = getPreviousMarketDay(today);

            List<SecurityStock> activeSecurities = securityStockRepository.findByIsActiveTrue();

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);

            for (SecurityStock security : activeSecurities) {
                try {
                    calculateDayChangeForSecurity(security, previousMarketDay);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    logger.error("Failed to calculate day change for {}: {}",
                            security.getSymbol(), e.getMessage());
                    errorCount.incrementAndGet();
                }
            }

            // Update all positions with new day changes
            updatePositionDayChanges();

            long duration = System.currentTimeMillis() - startTime;
            logger.info("Daily change calculation completed in {}ms - Success: {}, Errors: {}",
                    duration, successCount.get(), errorCount.get());

        } catch (Exception e) {
            logger.error("Fatal error during daily change calculation: {}", e.getMessage(), e);
        }
    }

    /**
     * Manual trigger for calculating day changes for all securities
     */
    @Transactional
    public void triggerDayChangeCalculation() {
        logger.info("Manually triggered day change calculation");
        calculateDailyChanges();
    }

    /**
     * Get day change information for a specific security
     */
    @Transactional(readOnly = true)
    public DayChangeInfo getDayChangeInfo(String symbol) {
        SecurityStock security = securityStockRepository.findBySymbol(symbol)
                .orElse(null);

        if (security == null) {
            return new DayChangeInfo(symbol, null, null, null);
        }

        LocalDate previousMarketDay = getPreviousMarketDay(LocalDate.now());
        BigDecimal previousClose = getPreviousClosingPrice(security, previousMarketDay);

        BigDecimal dayChange = null;
        BigDecimal dayChangePercent = null;

        if (previousClose != null && security.getCurrentPrice() != null) {
            dayChange = security.getCurrentPrice().subtract(previousClose);
            if (previousClose.compareTo(BigDecimal.ZERO) > 0) {
                dayChangePercent = dayChange.divide(previousClose, 4, RoundingMode.HALF_UP)
                        .multiply(HUNDRED);
            }
        }

        return new DayChangeInfo(symbol, security.getCurrentPrice(), dayChange, dayChangePercent);
    }

    /**
     * Capture closing price for a specific security
     */
    private void captureClosingPriceForSecurity(SecurityStock security, LocalDate date) {
        if (security.getCurrentPrice() == null) {
            logger.warn("No current price available for {}, skipping closing price capture",
                    security.getSymbol());
            return;
        }

        // Check if we already have a price history entry for today
        Optional<PriceHistory> existingHistory = priceHistoryRepository
                .findBySecurityStockAndDate(security, date);

        if (existingHistory.isPresent()) {
            // Update existing entry with current closing price
            PriceHistory priceHistory = existingHistory.get();
            priceHistory.setClosePrice(security.getCurrentPrice());

            // Calculate price change from previous day if available
            LocalDate previousDay = getPreviousMarketDay(date);
            BigDecimal previousClose = getPreviousClosingPrice(security, previousDay);

            if (previousClose != null) {
                BigDecimal priceChange = security.getCurrentPrice().subtract(previousClose);
                BigDecimal percentChange = BigDecimal.ZERO;

                if (previousClose.compareTo(BigDecimal.ZERO) > 0) {
                    percentChange = priceChange.divide(previousClose, 4, RoundingMode.HALF_UP)
                            .multiply(HUNDRED);
                }

                priceHistory.setPriceChange(priceChange);
                priceHistory.setPercentChange(percentChange);
            }

            priceHistoryRepository.save(priceHistory);
            logger.debug("Updated closing price for {} on {}: ${}",
                    security.getSymbol(), date, security.getCurrentPrice());
        } else {
            // Create new price history entry if none exists
            // This is a fallback - normally PriceHistory entries would be created by your MarketDataScheduler
            logger.debug("No existing price history found for {} on {}, would need full OHLCV data",
                    security.getSymbol(), date);
        }

        // Update SecurityStock's previousClose field for quick access
        security.setPreviousClose(security.getCurrentPrice());
        securityStockRepository.save(security);
    }

    /**
     * Calculate day change for a specific security
     */
    private void calculateDayChangeForSecurity(SecurityStock security, LocalDate previousMarketDay) {
        BigDecimal currentPrice = security.getCurrentPrice();
        if (currentPrice == null) {
            logger.warn("No current price for {}, cannot calculate day change", security.getSymbol());
            return;
        }

        BigDecimal previousClose = getPreviousClosingPrice(security, previousMarketDay);
        if (previousClose == null) {
            logger.debug("No previous closing price for {}, setting day change to zero",
                    security.getSymbol());
            security.setPriceChange(BigDecimal.ZERO);
            security.setPriceChangePercent(BigDecimal.ZERO);
        } else {
            BigDecimal priceChange = currentPrice.subtract(previousClose);
            BigDecimal priceChangePercent = BigDecimal.ZERO;

            if (previousClose.compareTo(BigDecimal.ZERO) > 0) {
                priceChangePercent = priceChange.divide(previousClose, 4, RoundingMode.HALF_UP)
                        .multiply(HUNDRED);
            }

            security.setPriceChange(priceChange);
            security.setPriceChangePercent(priceChangePercent);

            logger.debug("Calculated day change for {}: ${} ({}%)",
                    security.getSymbol(), priceChange, priceChangePercent);
        }

        securityStockRepository.save(security);
    }

    /**
     * Update all positions with new day changes
     */
    private void updatePositionDayChanges() {
        logger.info("Updating position day changes");

        try {
            List<Position> activePositions = positionRepository.findAll().stream()
                    .filter(position -> position.getIsActive() != null && position.getIsActive())
                    .toList();

            AtomicInteger updatedCount = new AtomicInteger(0);

            for (Position position : activePositions) {
                try {
                    updatePositionDayChange(position);
                    updatedCount.incrementAndGet();
                } catch (Exception e) {
                    logger.error("Failed to update day change for position {}: {}",
                            position.getPositionId(), e.getMessage());
                }
            }

            logger.info("Updated day changes for {} positions", updatedCount.get());

        } catch (Exception e) {
            logger.error("Error updating position day changes: {}", e.getMessage());
        }
    }

    /**
     * Update day change for a specific position
     */
    private void updatePositionDayChange(Position position) {
        SecurityStock security = position.getSecurityStock();
        if (security == null || position.getQuantity() == null) {
            return;
        }

        // Get current position value
        BigDecimal currentValue = position.getCurrentValue();
        if (currentValue == null || currentValue.compareTo(BigDecimal.ZERO) == 0) {
            position.setDayChange(BigDecimal.ZERO);
            position.setDayChangePercent(BigDecimal.ZERO);
            positionRepository.save(position);
            return;
        }

        // Calculate position value at previous day's close
        LocalDate previousMarketDay = getPreviousMarketDay(LocalDate.now());
        BigDecimal previousClose = getPreviousClosingPrice(security, previousMarketDay);

        if (previousClose != null) {
            BigDecimal previousValue = position.getQuantity().multiply(previousClose);
            BigDecimal dayChange = currentValue.subtract(previousValue);

            BigDecimal dayChangePercent = BigDecimal.ZERO;
            if (previousValue.compareTo(BigDecimal.ZERO) > 0) {
                dayChangePercent = dayChange.divide(previousValue, 4, RoundingMode.HALF_UP)
                        .multiply(HUNDRED);
            }

            position.setDayChange(dayChange);
            position.setDayChangePercent(dayChangePercent);
        } else {
            // No previous close data available
            position.setDayChange(BigDecimal.ZERO);
            position.setDayChangePercent(BigDecimal.ZERO);
        }

        positionRepository.save(position);
    }

    /**
     * Get previous closing price from PriceHistory
     */
    private BigDecimal getPreviousClosingPrice(SecurityStock security, LocalDate date) {
        Optional<PriceHistory> priceHistory = priceHistoryRepository
                .findBySecurityStockAndDate(security, date);

        if (priceHistory.isPresent()) {
            return priceHistory.get().getClosePrice();
        }

        // Fallback: use SecurityStock's previousClose field
        return security.getPreviousClose();
    }

    /**
     * Get previous market day (excluding weekends)
     */
    private LocalDate getPreviousMarketDay(LocalDate date) {
        LocalDate previousDay = date.minusDays(1);

        // Skip weekends
        while (!isMarketDay(previousDay)) {
            previousDay = previousDay.minusDays(1);
        }

        return previousDay;
    }

    /**
     * Check if given date is a market day (weekday)
     */
    private boolean isMarketDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    /**
     * Data class for day change information
     */
    public static class DayChangeInfo {
        private final String symbol;
        private final BigDecimal currentPrice;
        private final BigDecimal dayChange;
        private final BigDecimal dayChangePercent;

        public DayChangeInfo(String symbol, BigDecimal currentPrice,
                             BigDecimal dayChange, BigDecimal dayChangePercent) {
            this.symbol = symbol;
            this.currentPrice = currentPrice;
            this.dayChange = dayChange;
            this.dayChangePercent = dayChangePercent;
        }

        public String getSymbol() { return symbol; }
        public BigDecimal getCurrentPrice() { return currentPrice; }
        public BigDecimal getDayChange() { return dayChange; }
        public BigDecimal getDayChangePercent() { return dayChangePercent; }

        @Override
        public String toString() {
            return String.format("DayChangeInfo{symbol='%s', currentPrice=%s, dayChange=%s, dayChangePercent=%s}",
                    symbol, currentPrice, dayChange, dayChangePercent);
        }
    }
}