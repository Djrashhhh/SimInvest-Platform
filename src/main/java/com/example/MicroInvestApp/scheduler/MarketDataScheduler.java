package com.example.MicroInvestApp.scheduler;

import com.example.MicroInvestApp.domain.market.MarketData;
import com.example.MicroInvestApp.domain.market.PriceHistory;
import com.example.MicroInvestApp.domain.market.SecurityStock;
import com.example.MicroInvestApp.dto.finnhub.FinnhubQuoteDTO;
import com.example.MicroInvestApp.repositories.market.PriceHistoryRepository;
import com.example.MicroInvestApp.repositories.market.SecurityStockRepository;
import com.example.MicroInvestApp.repositories.market.MarketDataRepository;
import com.example.MicroInvestApp.service.market.DailyPriceTrackingService;
import com.example.MicroInvestApp.service.market.MarketDataService;
import com.example.MicroInvestApp.service.market.FinnhubClientService;
import com.example.MicroInvestApp.impl.market.MarketDataServiceImpl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ConditionalOnProperty(name = "scheduler.market-data.enabled", havingValue = "true", matchIfMissing = true)
public class MarketDataScheduler {
    private static final Logger logger = LoggerFactory.getLogger(MarketDataScheduler.class);

    private final MarketDataService marketDataService;
    private final SecurityStockRepository securityStockRepository;
    private final MarketDataRepository marketDataRepository;
    private final FinnhubClientService finnhubClientService;
    private ExecutorService executorService;
    private final DailyPriceTrackingService dailyPriceTrackingService;
    private final PriceHistoryRepository priceHistoryRepository;

    // Configuration properties
    @Value("${scheduler.market-data.batch-size:50}")
    private int batchSize;

    @Value("${scheduler.market-data.max-threads:5}")
    private int maxThreads;

    @Value("${scheduler.market-data.api-delay-ms:100}")
    private long apiDelayMs;

    @Value("${scheduler.market-data.circuit-breaker.failure-threshold:5}")
    private int circuitBreakerFailureThreshold;

    @Value("${scheduler.market-data.circuit-breaker.timeout-minutes:5}")
    private int circuitBreakerTimeoutMinutes;

    // Circuit breaker state
    private volatile boolean circuitBreakerOpen = false;
    private volatile LocalTime circuitBreakerOpenTime = null;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);

    @Autowired
    public MarketDataScheduler(MarketDataService marketDataService,
                               SecurityStockRepository securityStockRepository,
                               MarketDataRepository marketDataRepository,
                               FinnhubClientService finnhubClientService,
                               DailyPriceTrackingService dailyPriceTrackingService,
                               PriceHistoryRepository priceHistoryRepository) {
        this.marketDataService = marketDataService;
        this.securityStockRepository = securityStockRepository;
        this.marketDataRepository = marketDataRepository;
        this.finnhubClientService = finnhubClientService;
        this.dailyPriceTrackingService = dailyPriceTrackingService;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    @PostConstruct
    public void init() {
        // Initialize ExecutorService after all @Value injections are complete
        logger.info("Initializing MarketDataScheduler with {} threads", maxThreads);
        this.executorService = Executors.newFixedThreadPool(maxThreads);
    }

    /**
     * Enhanced price updates with circuit breaker, batching, and better error handling
     */
    @Scheduled(cron = "0 */15 9-15 * * MON-FRI", zone = "America/New_York")
    public void updateCurrentPricesDuringMarketHours() {
        if (!isMarketOpen()) {
            logger.debug("Market is closed - skipping price updates");
            return;
        }

        if (isCircuitBreakerOpen()) {
            logger.warn("Circuit breaker is open - skipping price updates");
            return;
        }

        logger.info("Starting scheduled current price updates");
        long startTime = System.currentTimeMillis();

        try {
            int totalSecurities = securityStockRepository.countByIsActiveTrue();
            logger.info("Updating prices for {} active securities", totalSecurities);

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);

            // Process securities in batches
            for (int offset = 0; offset < totalSecurities; offset += batchSize) {
                Pageable pageable = PageRequest.of(offset / batchSize, batchSize);
                List<SecurityStock> batch = securityStockRepository.findByIsActiveTrue(pageable).getContent();

                if (batch.isEmpty()) break;

                //Enhanced batch processing with day change calculation
                processBatchWithDayChangeCalculation(batch, successCount, errorCount);

                // Rate limiting between batches
                if (offset + batchSize < totalSecurities) {
                    try {
                        Thread.sleep(apiDelayMs * 2); // Longer delay between batches
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.warn("Price update interrupted");
                        return;
                    }
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            logger.info("Completed scheduled price updates in {}ms - Success: {}, Errors: {}",
                    duration, successCount.get(), errorCount.get());

            // Reset circuit breaker on successful batch
            if (errorCount.get() == 0) {
                resetCircuitBreaker();
            } else if (errorCount.get() > successCount.get()) {
                // More errors than successes - consider opening circuit breaker
                handleCircuitBreakerLogic(errorCount.get());
            }

        } catch (Exception e) {
            logger.error("Fatal error during scheduled price updates: {}", e.getMessage(), e);
            handleCircuitBreakerLogic(1);
        }
    }

    /**
     * Enhanced daily market data fetch with better error recovery
     */
    @Scheduled(cron = "0 0 17 * * MON-FRI", zone = "America/New_York")
    public void fetchDailyMarketData() {
        if (!wasMarketOpenToday()) {
            logger.info("Market was closed today - skipping daily data fetch");
            return;
        }

        logger.info("Starting scheduled daily market data fetch");
        long startTime = System.currentTimeMillis();

        try {
            List<SecurityStock> activeSecurities = securityStockRepository.findByIsActiveTrue();
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);

            // Process in smaller batches for daily data
            int dailyBatchSize = Math.max(1, batchSize / 2);

            for (int i = 0; i < activeSecurities.size(); i += dailyBatchSize) {
                int endIndex = Math.min(i + dailyBatchSize, activeSecurities.size());
                List<SecurityStock> batch = activeSecurities.subList(i, endIndex);

                for (SecurityStock security : batch) {
                    try {
                        marketDataService.fetchAndStoreCurrentMarketData(security.getSymbol());

                        // NEW: Store the closing price for day-change calculations
                        storeClosingPriceForDayChangeCalculation(security);

                        successCount.incrementAndGet();

                        // Rate limiting
                        Thread.sleep(apiDelayMs * 2);

                    } catch (Exception e) {
                        logger.error("Failed to fetch daily data for {}: {}",
                                security.getSymbol(), e.getMessage());
                        errorCount.incrementAndGet();
                    }
                }

                // Longer pause between batches
                if (endIndex < activeSecurities.size()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.warn("Daily fetch interrupted");
                        return;
                    }
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            logger.info("Completed daily market data fetch in {}ms - Success: {}, Errors: {}",
                    duration, successCount.get(), errorCount.get());

        } catch (Exception e) {
            logger.error("Fatal error during daily market data fetch: {}", e.getMessage(), e);
        }
    }

    // NEW METHOD: Store closing price for day-change tracking
    private void storeClosingPriceForDayChangeCalculation(SecurityStock security) {
        try {
            if (security.getCurrentPrice() != null) {
                LocalDate today = LocalDate.now();

                // Store today's closing price in PriceHistory for tomorrow's calculations
                Optional<PriceHistory> todayHistory = priceHistoryRepository
                        .findBySecurityStockAndDate(security, today);

                if (todayHistory.isPresent()) {
                    // Update existing PriceHistory with closing price
                    PriceHistory history = todayHistory.get();
                    history.setClosePrice(security.getCurrentPrice());

                    // Calculate change from previous day
                    if (security.getPreviousClose() != null && security.getPreviousClose().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal change = security.getCurrentPrice().subtract(security.getPreviousClose());
                        BigDecimal changePercent = change.divide(security.getPreviousClose(), 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"));
                        history.setPriceChange(change);
                        history.setPercentChange(changePercent);
                    } else {
                        history.setPriceChange(BigDecimal.ZERO);
                        history.setPercentChange(BigDecimal.ZERO);
                    }

                    priceHistoryRepository.save(history);
                    logger.debug("Updated PriceHistory closing price for {} on {}: ${}",
                            security.getSymbol(), today, security.getCurrentPrice());
                } else {
                    // Create complete PriceHistory entry using current market data
                    createCompletePriceHistoryEntry(security, today);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to store closing price for {}: {}", security.getSymbol(), e.getMessage());
        }
    }

    private void createCompletePriceHistoryEntry(SecurityStock security, LocalDate date) {
        try {
            // Try to get today's full market data first
            Optional<MarketData> todayMarketData = marketDataRepository
                    .findBySecurityStockAndMarketDate(security, date);

            PriceHistory history = new PriceHistory();
            history.setSecurityStock(security);
            history.setDate(date);

            if (todayMarketData.isPresent()) {
                MarketData data = todayMarketData.get();
                history.setOpenPrice(data.getOpenPrice());
                history.setHighPrice(data.getHighPrice());
                history.setLowPrice(data.getLowPrice());
                history.setClosePrice(data.getClosePrice());
                history.setVolume(data.getVolume());
            } else {
                // Fallback: use current price for all OHLC values
                BigDecimal currentPrice = security.getCurrentPrice();
                history.setOpenPrice(currentPrice);
                history.setHighPrice(currentPrice);
                history.setLowPrice(currentPrice);
                history.setClosePrice(currentPrice);
                history.setVolume(1L);
            }

            // Calculate price change from previous day
            if (security.getPreviousClose() != null && security.getPreviousClose().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal change = history.getClosePrice().subtract(security.getPreviousClose());
                BigDecimal changePercent = change.divide(security.getPreviousClose(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                history.setPriceChange(change);
                history.setPercentChange(changePercent);
            } else {
                history.setPriceChange(BigDecimal.ZERO);
                history.setPercentChange(BigDecimal.ZERO);
            }

            // Set moving averages (simplified)
            history.setMovingAverage50Day(history.getClosePrice());
            history.setMovingAverage200Day(history.getClosePrice());

            priceHistoryRepository.save(history);
            logger.debug("Created complete PriceHistory for {} on {}: ${}",
                    security.getSymbol(), date, history.getClosePrice());

        } catch (Exception e) {
            logger.error("Failed to create complete price history for {} on {}: {}",
                    security.getSymbol(), date, e.getMessage());
        }
    }

    // NEW METHOD: Manual trigger for day change calculations
    /**
     * Manual trigger for calculating day changes
     * Can be called via API or internal processes
     */
    public void triggerDayChangeCalculation() {
        logger.info("Manual trigger for day change calculation");
        try {
            dailyPriceTrackingService.triggerDayChangeCalculation();
            logger.info("Day change calculation completed successfully");
        } catch (Exception e) {
            logger.error("Error during manual day change calculation: {}", e.getMessage());
        }
    }

    /**
     * Enhanced historical data update with gap detection
     */
    @Scheduled(cron = "0 0 2 * * SUN", zone = "America/New_York")
    public void weeklyHistoricalDataUpdate() {
        logger.info("Starting weekly historical data update");

        try {
            List<SecurityStock> activeSecurities = securityStockRepository.findByIsActiveTrue();
            LocalDate endDate = getLastMarketDay();
            LocalDate startDate = endDate.minusDays(30);

            int successCount = 0;
            int errorCount = 0;

            for (SecurityStock security : activeSecurities) {
                try {
                    // Check for data gaps before fetching
                    if (hasDataGaps(security, startDate, endDate)) {
                        logger.info("Filling data gaps for security: {}", security.getSymbol());
                        marketDataService.fetchAndStoreHistoricalMarketData(
                                security.getSymbol(), startDate, endDate);
                    } else {
                        logger.debug("No data gaps found for security: {}", security.getSymbol());
                    }

                    successCount++;
                    Thread.sleep(500); // Respect API limits

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Historical data update interrupted");
                    break;
                } catch (Exception e) {
                    logger.error("Failed to update historical data for {}: {}",
                            security.getSymbol(), e.getMessage());
                    errorCount++;
                }
            }

            logger.info("Completed weekly historical data update - Success: {}, Errors: {}",
                    successCount, errorCount);

        } catch (Exception e) {
            logger.error("Fatal error during weekly historical update: {}", e.getMessage(), e);
        }
    }

    /**
     * Pre-market data preparation job
     */
    @Scheduled(cron = "0 0 8 * * MON-FRI", zone = "America/New_York")
    public void preMarketDataPreparation() {
        if (!isMarketDay()) {
            return;
        }

        logger.info("Starting pre-market data preparation");

        try {
            // Validate all active securities
            List<SecurityStock> activeSecurities = securityStockRepository.findByIsActiveTrue();
            int invalidCount = 0;

            for (SecurityStock security : activeSecurities) {
                try {
                    // Quick validation of security status
                    Boolean isValid = finnhubClientService.validateSymbol(security.getSymbol()).block();
                    if (Boolean.FALSE.equals(isValid)) {
                        logger.warn("Security {} appears to be invalid or delisted", security.getSymbol());
                        // Could mark as inactive or flag for review
                        invalidCount++;
                    }

                    Thread.sleep(50); // Light rate limiting

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Pre-market validation interrupted");
                    break;
                } catch (Exception e) {
                    logger.warn("Failed to validate security {}: {}", security.getSymbol(), e.getMessage());
                }
            }

            logger.info("Pre-market preparation completed - {} securities validated, {} flagged as invalid",
                    activeSecurities.size(), invalidCount);

        } catch (Exception e) {
            logger.error("Error during pre-market preparation: {}", e.getMessage(), e);
        }
    }

    /**
     * Recovery job for failed updates
     */
    @Scheduled(cron = "0 30 16 * * MON-FRI", zone = "America/New_York") // 4:30 PM EST
    public void recoverFailedUpdates() {
        if (!wasMarketOpenToday()) {
            return;
        }

        logger.info("Starting recovery job for today's failed updates");

        try {
            LocalDate today = LocalDate.now();
            List<SecurityStock> securitiesMissingData =
                    securityStockRepository.findActiveSecuritiesMissingMarketDataForDate(today);

            if (!securitiesMissingData.isEmpty()) {
                logger.info("Found {} securities missing today's data, attempting recovery",
                        securitiesMissingData.size());

                int recovered = 0;
                for (SecurityStock security : securitiesMissingData) {
                    try {
                        marketDataService.fetchAndStoreCurrentMarketData(security.getSymbol());
                        recovered++;
                        Thread.sleep(apiDelayMs * 3); // Conservative rate limiting
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.warn("Recovery job interrupted");
                        break;
                    } catch (Exception e) {
                        logger.warn("Recovery failed for {}: {}", security.getSymbol(), e.getMessage());
                    }
                }

                logger.info("Recovery completed - {} out of {} securities recovered",
                        recovered, securitiesMissingData.size());
            } else {
                logger.info("No missing data found for today - recovery not needed");
            }

        } catch (Exception e) {
            logger.error("Error during recovery job: {}", e.getMessage());
        }
    }

    /**
     * Process a batch of securities concurrently with proper error handling
     */
    private void processBatchConcurrently(List<SecurityStock> batch,
                                          AtomicInteger successCount,
                                          AtomicInteger errorCount) {

        List<CompletableFuture<Void>> futures = batch.stream()
                .map(security -> CompletableFuture.runAsync(() -> {
                    try {
                        marketDataService.updateCurrentPrice(security.getSymbol());
                        successCount.incrementAndGet();

                        // Rate limiting per request
                        try {
                            Thread.sleep(apiDelayMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                    } catch (Exception e) {
                        logger.error("Failed to update price for {}: {}",
                                security.getSymbol(), e.getMessage());
                        errorCount.incrementAndGet();
                    }
                }, executorService))
                .toList();

        // Wait for all futures to complete with timeout
        try {
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));
            allFutures.get(30, TimeUnit.SECONDS); // 30 second timeout per batch
        } catch (TimeoutException e) {
            logger.warn("Batch processing timed out, some updates may not have completed");
            futures.forEach(future -> future.cancel(true));
        } catch (Exception e) {
            logger.error("Error during batch processing: {}", e.getMessage());
        }
    }

    /**
     * Extended market hours job for after-hours trading data
     */
    @Scheduled(cron = "0 */30 16-20 * * MON-FRI", zone = "America/New_York")
    public void updateExtendedHoursData() {
        if (!isMarketDay()) {
            return;
        }

        logger.debug("Starting extended hours price updates");

        try {
            // Get only the most actively traded securities for extended hours
            List<SecurityStock> highVolumeSecurities = getHighVolumeSecurities();

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);

            for (SecurityStock security : highVolumeSecurities) {
                try {
                    marketDataService.updateCurrentPrice(security.getSymbol());
                    successCount.incrementAndGet();
                    Thread.sleep(apiDelayMs * 3); // Slower rate for extended hours
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Extended hours update interrupted");
                    break;
                } catch (Exception e) {
                    logger.warn("Failed to update extended hours price for {}: {}",
                            security.getSymbol(), e.getMessage());
                    errorCount.incrementAndGet();
                }
            }

            logger.debug("Extended hours updates completed - Success: {}, Errors: {}",
                    successCount.get(), errorCount.get());

        } catch (Exception e) {
            logger.error("Error during extended hours updates: {}", e.getMessage());
        }
    }

    /**
     * Data quality monitoring job
     */
    @Scheduled(cron = "0 0 6 * * MON-FRI", zone = "America/New_York") // 6 AM EST
    public void dataQualityCheck() {
        if (!isMarketDay()) {
            return;
        }

        logger.info("Starting data quality check");

        try {
            LocalDate yesterday = getLastMarketDay();
            List<SecurityStock> activeSecurities = securityStockRepository.findByIsActiveTrue();

            int missingDataCount = 0;
            int staleDataCount = 0;
            int totalChecked = 0;

            for (SecurityStock security : activeSecurities) {
                try {
                    // Check if yesterday's data exists
                    if (!marketDataRepository.findBySecurityStockAndMarketDate(security, yesterday).isPresent()) {
                        missingDataCount++;
                        logger.warn("Missing market data for {} on {}", security.getSymbol(), yesterday);
                    }

                    // Check if current price is stale
                    var latestData = marketDataRepository.findLatestBySecurityStock(security);
                    if (latestData.isPresent() && isDataStale(latestData.get())) {
                        staleDataCount++;
                        logger.warn("Stale data detected for {}: last updated {}",
                                security.getSymbol(), latestData.get().getLastUpdated());
                    }

                    totalChecked++;

                } catch (Exception e) {
                    logger.warn("Error checking data quality for {}: {}", security.getSymbol(), e.getMessage());
                }
            }

            logger.info("Data quality check completed - Checked: {}, Missing: {}, Stale: {}",
                    totalChecked, missingDataCount, staleDataCount);

            // Trigger alerts if data quality issues exceed threshold
            if (missingDataCount > totalChecked * 0.1) { // More than 10% missing
                logger.error("ALERT: High missing data rate detected - {}/{} securities missing yesterday's data",
                        missingDataCount, totalChecked);
            }

        } catch (Exception e) {
            logger.error("Error during data quality check: {}", e.getMessage());
        }
    }

    /**
     * Weekly data cleanup job
     */
    @Scheduled(cron = "0 0 3 * * SUN", zone = "America/New_York") // 3 AM on Sundays
    public void weeklyDataCleanup() {
        logger.info("Starting weekly data cleanup");

        try {
            LocalDate cutoffDate = LocalDate.now().minusYears(2);
            int deletedRecords = marketDataRepository.deleteOldRecords(cutoffDate);
            logger.info("Deleted {} old market data records older than {}", deletedRecords, cutoffDate);

        } catch (Exception e) {
            logger.error("Error during weekly cleanup: {}", e.getMessage());
        }
    }

    /**
     * Enhanced health check with metrics
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void healthCheck() {
        try {
            int activeSecurities = securityStockRepository.countByIsActiveTrue();
            boolean marketOpen = isMarketOpen();
            boolean circuitBreakerStatus = isCircuitBreakerOpen();

            logger.info("Market Data Scheduler Health Check - Active Securities: {}, Market Open: {}, Circuit Breaker: {}",
                    activeSecurities, marketOpen, circuitBreakerStatus ? "OPEN" : "CLOSED");

            // Publish health metrics for monitoring systems
            publishHealthMetrics(activeSecurities, marketOpen, circuitBreakerStatus);

        } catch (Exception e) {
            logger.error("Health check failed: {}", e.getMessage());
        }
    }

    /**
     * Enhanced market status checking with holiday support
     */
    private boolean isMarketOpen() {
        ZonedDateTime nowEST = ZonedDateTime.now(ZoneId.of("America/New_York"));
        LocalDate today = nowEST.toLocalDate();
        LocalTime currentTime = nowEST.toLocalTime();

        // Check if it's a weekday
        if (!isMarketDay(today)) {
            return false;
        }

        // Check market hours (9:30 AM - 4:00 PM EST)
        LocalTime marketOpen = LocalTime.of(9, 30);
        LocalTime marketClose = LocalTime.of(16, 0);

        return !currentTime.isBefore(marketOpen) && !currentTime.isAfter(marketClose);
    }

    private boolean isMarketDay() {
        return isMarketDay(LocalDate.now());
    }

    private boolean isMarketDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    private boolean wasMarketOpenToday() {
        return isMarketDay(LocalDate.now());
    }

    private LocalDate getLastMarketDay() {
        LocalDate date = LocalDate.now().minusDays(1);
        while (!isMarketDay(date)) {
            date = date.minusDays(1);
        }
        return date;
    }

    /**
     * Check if there are gaps in market data for a security
     */
    private boolean hasDataGaps(SecurityStock security, LocalDate startDate, LocalDate endDate) {
        try {
            long expectedDays = countMarketDaysBetween(startDate, endDate);
            long actualRecords = marketDataRepository.countBySecurityStockAndDateRange(
                    security, startDate, endDate);

            boolean hasGaps = actualRecords < expectedDays * 0.9; // Allow 10% tolerance

            if (hasGaps) {
                logger.debug("Data gaps detected for {}: expected {} days, found {} records",
                        security.getSymbol(), expectedDays, actualRecords);
            }

            return hasGaps;
        } catch (Exception e) {
            logger.warn("Could not check data gaps for {}: {}", security.getSymbol(), e.getMessage());
            return true; // Assume gaps exist if we can't check
        }
    }

    private long countMarketDaysBetween(LocalDate start, LocalDate end) {
        return start.datesUntil(end.plusDays(1))
                .filter(this::isMarketDay)
                .count();
    }

    /**
     * Circuit breaker logic to prevent API abuse during failures
     */
    private void handleCircuitBreakerLogic(int currentErrors) {
        int failures = consecutiveFailures.addAndGet(currentErrors);

        if (failures >= circuitBreakerFailureThreshold && !circuitBreakerOpen) {
            logger.error("Opening circuit breaker due to {} consecutive failures", failures);
            circuitBreakerOpen = true;
            circuitBreakerOpenTime = LocalTime.now();
        }
    }

    private boolean isCircuitBreakerOpen() {
        if (!circuitBreakerOpen) {
            return false;
        }

        // Check if enough time has passed to try again
        if (circuitBreakerOpenTime != null &&
                LocalTime.now().isAfter(circuitBreakerOpenTime.plusMinutes(circuitBreakerTimeoutMinutes))) {

            logger.info("Circuit breaker timeout elapsed, attempting to close circuit breaker");
            circuitBreakerOpen = false;
            circuitBreakerOpenTime = null;
            consecutiveFailures.set(0);
            return false;
        }

        return true;
    }

    private void resetCircuitBreaker() {
        if (circuitBreakerOpen || consecutiveFailures.get() > 0) {
            logger.info("Resetting circuit breaker after successful operations");
            circuitBreakerOpen = false;
            circuitBreakerOpenTime = null;
            consecutiveFailures.set(0);
        }
    }

    /**
     * Get high volume securities for extended hours trading
     */
    private List<SecurityStock> getHighVolumeSecurities() {
        // Return a subset of active securities for extended hours
        List<SecurityStock> allActive = securityStockRepository.findByIsActiveTrue();
        int maxExtendedHours = Math.min(100, allActive.size()); // Limit to top 100
        return allActive.subList(0, maxExtendedHours);
    }

    /**
     * Check if market data is stale
     */
    private boolean isDataStale(MarketData marketData) {
        if (marketData.getLastUpdated() == null) {
            return true;
        }

        Instant staleThreshold = Instant.now().minusSeconds(24 * 3600L); // 24 hours
        return marketData.getLastUpdated().isBefore(staleThreshold);
    }

    /**
     * Placeholder for publishing health metrics to monitoring system
     */
    private void publishHealthMetrics(int activeSecurities, boolean marketOpen, boolean circuitBreakerOpen) {
        // Implementation would depend on your monitoring solution
        // Could be Micrometer metrics, custom endpoints, etc.
        logger.debug("Publishing health metrics - Securities: {}, Market: {}, Circuit Breaker: {}",
                activeSecurities, marketOpen, circuitBreakerOpen);
    }

    /**
     * Get market data statistics for monitoring
     */
    public MarketDataServiceImpl.MarketDataStats getMarketDataStats() {
        try {
            int totalActiveSecurities = securityStockRepository.countByIsActiveTrue();
            LocalDate today = LocalDate.now();
            LocalDate yesterday = getLastMarketDay();

            int todayRecords = marketDataRepository.countByMarketDate(today);
            int yesterdayRecords = marketDataRepository.countByMarketDate(yesterday);

            return new MarketDataServiceImpl.MarketDataStats(
                    totalActiveSecurities,
                    todayRecords,
                    yesterdayRecords,
                    isCircuitBreakerOpen(),
                    consecutiveFailures.get()
            );
        } catch (Exception e) {
            logger.error("Error calculating market data stats: {}", e.getMessage());
            return new MarketDataServiceImpl.MarketDataStats(0, 0, 0, true, -1);
        }
    }

    /**
     * Shutdown hook to clean up executor service
     */
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down market data scheduler");
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                    logger.warn("Forced shutdown of executor service");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executorService.shutdownNow();
                logger.warn("Interrupted during shutdown, forcing immediate termination");
            }
        }
    }

    // NEW METHOD: Enhanced batch processing with day change calculation
    private void processBatchWithDayChangeCalculation(List<SecurityStock> batch,
                                                      AtomicInteger successCount,
                                                      AtomicInteger errorCount) {

        List<CompletableFuture<Void>> futures = batch.stream()
                .map(security -> CompletableFuture.runAsync(() -> {
                    try {
                        // Update current price (your existing logic)
                        marketDataService.updateCurrentPrice(security.getSymbol());

                        // Calculate day change using previous close
                        calculateIntradayChange(security);

                        successCount.incrementAndGet();
                        Thread.sleep(apiDelayMs);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        logger.error("Failed to update price for {}: {}",
                                security.getSymbol(), e.getMessage());
                        errorCount.incrementAndGet();
                    }
                }, executorService))
                .toList();

        // Wait for all futures to complete with timeout
        try {
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));
            allFutures.get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            logger.warn("Batch processing timed out, some updates may not have completed");
            futures.forEach(future -> future.cancel(true));
        } catch (Exception e) {
            logger.error("Error during batch processing: {}", e.getMessage());
        }
    }

    // NEW METHOD: Calculate intraday change using previous close
    private void calculateIntradayChange(SecurityStock security) {
        try {
            if (security.getCurrentPrice() != null && security.getPreviousClose() != null &&
                    security.getPreviousClose().compareTo(BigDecimal.ZERO) > 0) {

                BigDecimal priceChange = security.getCurrentPrice().subtract(security.getPreviousClose());
                BigDecimal priceChangePercent = priceChange
                        .divide(security.getPreviousClose(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));

                security.setPriceChange(priceChange);
                security.setPriceChangePercent(priceChangePercent);

                securityStockRepository.save(security);

                logger.debug("Updated intraday change for {}: ${} ({}%)",
                        security.getSymbol(), priceChange, priceChangePercent);
            }
        } catch (Exception e) {
            logger.warn("Failed to calculate intraday change for {}: {}",
                    security.getSymbol(), e.getMessage());
        }
    }

    // NEW SCHEDULED JOB: Initialize previous close prices for new trading day
    @Scheduled(cron = "0 0 9 * * MON-FRI", zone = "America/New_York")
    public void initializeTradingDay() {
        if (!isMarketDay()) {
            return;
        }

        logger.info("Initializing trading day - setting up previous close prices from quotes");

        try {
            List<SecurityStock> activeSecurities = securityStockRepository.findByIsActiveTrue();
            int updatedCount = 0;
            int errorCount = 0;

            for (SecurityStock security : activeSecurities) {
                try {
                    // Get quote which includes previous close
                    FinnhubQuoteDTO quote = finnhubClientService.getQuote(security.getSymbol()).block();

                    if (quote != null && quote.getPreviousClose() != null &&
                            quote.getPreviousClose().compareTo(BigDecimal.ZERO) > 0) {

                        security.setPreviousClose(quote.getPreviousClose());
                        securityStockRepository.save(security);
                        updatedCount++;

                        logger.debug("Set previous close for {} to ${}",
                                security.getSymbol(), quote.getPreviousClose());
                    } else {
                        logger.warn("Could not get valid previous closing price for {}",
                                security.getSymbol());
                        errorCount++;
                    }

                    // Rate limiting
                    Thread.sleep(apiDelayMs);

                } catch (Exception e) {
                    logger.warn("Failed to initialize previous close for {}: {}",
                            security.getSymbol(), e.getMessage());
                    errorCount++;
                }
            }

            logger.info("Trading day initialization completed - updated {} securities, {} errors",
                    updatedCount, errorCount);
        } catch (Exception e) {
            logger.error("Error during trading day initialization: {}", e.getMessage());
        }
    }


    // Helper method to get closing price from PriceHistory
    private BigDecimal getClosingPriceFromHistory(SecurityStock security, LocalDate date) {
        try {
            Optional<PriceHistory> priceHistory = priceHistoryRepository
                    .findBySecurityStockAndDate(security, date);

            if (priceHistory.isPresent()) {
                return priceHistory.get().getClosePrice();
            }
        } catch (Exception e) {
            logger.debug("Could not get closing price from history for {} on {}: {}",
                    security.getSymbol(), date, e.getMessage());
        }
        return null;
    }

    // Helper method to get previous market day
    private LocalDate getPreviousMarketDay(LocalDate date) {
        LocalDate previousDay = date.minusDays(1);

        while (!isMarketDay(previousDay)) {
            previousDay = previousDay.minusDays(1);
        }

        return previousDay;
    }

}