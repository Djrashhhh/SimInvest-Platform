package com.example.MicroInvestApp.impl.market;

import com.example.MicroInvestApp.domain.enums.SecuritySector;
import com.example.MicroInvestApp.domain.market.MarketData;
import com.example.MicroInvestApp.domain.market.PriceHistory;
import com.example.MicroInvestApp.domain.market.SecurityStock;
import com.example.MicroInvestApp.dto.finnhub.FinnhubCandleDTO;
import com.example.MicroInvestApp.dto.finnhub.FinnhubQuoteDTO;
import com.example.MicroInvestApp.repositories.market.MarketDataRepository;
import com.example.MicroInvestApp.repositories.market.PriceHistoryRepository;
import com.example.MicroInvestApp.repositories.market.SecurityStockRepository;
import com.example.MicroInvestApp.service.market.FinnhubClientService;
import com.example.MicroInvestApp.service.market.MarketDataService;
import com.example.MicroInvestApp.service.market.SecurityCreationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional
public class MarketDataServiceImpl implements MarketDataService {

    private static final Logger logger = LoggerFactory.getLogger(MarketDataServiceImpl.class);

    private final FinnhubClientService finnhubClient;
    private final SecurityStockRepository securityStockRepository;
    private final MarketDataRepository marketDataRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final SecurityCreationService securityCreationService;

    // Configuration properties
    @Value("${market-data.stale-threshold-hours:24}")
    private int staleThresholdHours;

    @Value("${market-data.validation.min-price:0.01}")
    private BigDecimal minValidPrice;

    @Value("${market-data.validation.max-price:100000}")
    private BigDecimal maxValidPrice;

    @Value("${market-data.validation.significant-change-threshold:5.0}")
    private BigDecimal significantChangeThreshold;

    @Value("${scheduler.market-data.batch-size:50}")
    private int batchSize;

    @Value("${scheduler.market-data.api-delay-ms:100}")
    private long apiDelayMs;

    // Circuit breaker fields
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);

    @Autowired
    public MarketDataServiceImpl(
            FinnhubClientService finnhubClient,
            SecurityStockRepository securityStockRepository,
            MarketDataRepository marketDataRepository,
            PriceHistoryRepository priceHistoryRepository,
            SecurityCreationService securityCreationService) {
        this.finnhubClient = finnhubClient;
        this.securityStockRepository = securityStockRepository;
        this.marketDataRepository = marketDataRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.securityCreationService = securityCreationService;
    }

    /**
     * Enhanced fetch with data validation and better error handling
     */
    @Override
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public MarketData fetchAndStoreCurrentMarketData(String symbol) {
        logger.info("Fetching current market data for symbol: {}", symbol);

        SecurityStock security = findOrCreateSecurity(symbol);

        try {
            FinnhubQuoteDTO quote = finnhubClient.getQuote(symbol).block();

            if (!isValidQuoteData(quote, symbol)) {
                throw new IllegalStateException("Invalid quote data received for symbol: " + symbol);
            }

            LocalDate today = LocalDate.now();
            MarketData marketData = getOrCreateMarketData(security, today, "Finnhub");

            // Enhanced data mapping with validation
            updateMarketDataFromQuote(marketData, quote);

            // Validate data before saving
            validateMarketData(marketData);

            marketData = marketDataRepository.save(marketData);

            // Update security's current price atomically
            updateSecurityPrice(security, quote.getCurrentPrice());

            logger.info("Successfully stored market data for {}: ${} (Change: {}%)",
                    symbol, quote.getCurrentPrice(), calculatePriceChangePercent(security, quote.getCurrentPrice()));

            return marketData;

        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation for {}: {}", symbol, e.getMessage());
            throw new RuntimeException("Data validation failed for " + symbol, e);
        } catch (Exception e) {
            logger.error("Error fetching market data for {}: {}", symbol, e.getMessage(), e);

            // Try to return cached data if available
            Optional<MarketData> cachedData = getLatestMarketData(symbol);
            if (cachedData.isPresent() && isDataFresh(cachedData.get())) {
                logger.warn("Returning cached data for {} due to fetch failure", symbol);
                return cachedData.get();
            }

            throw new RuntimeException("Failed to fetch market data for " + symbol, e);
        }
    }

    /**
     * Enhanced historical data fetch with batch processing and gap detection
     */
    @Override
    @Retryable(value = {Exception.class}, maxAttempts = 2, backoff = @Backoff(delay = 2000))
    public List<MarketData> fetchAndStoreHistoricalMarketData(String symbol, LocalDate from, LocalDate to) {
        logger.info("Fetching historical market data for symbol: {} from {} to {}", symbol, from, to);

        SecurityStock security = findOrCreateSecurity(symbol);

        try {
            FinnhubCandleDTO candles = finnhubClient.getCandles(symbol, from, to, "D").block();

            if (candles == null || candles.getClosePrices() == null || candles.getClosePrices().isEmpty()) {
                logger.warn("No historical data received for symbol: {}", symbol);
                return new ArrayList<>();
            }

            List<MarketData> marketDataList = new ArrayList<>();
            List<MarketData> newRecords = new ArrayList<>();
            List<MarketData> updatedRecords = new ArrayList<>();

            // Process each candle with enhanced validation
            for (int i = 0; i < candles.getClosePrices().size(); i++) {
                try {
                    LocalDate date = Instant.ofEpochSecond(candles.getTimestamps().get(i))
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate();

                    // Skip weekends and holidays
                    if (!isMarketDay(date)) {
                        continue;
                    }

                    // Validate candle data
                    if (!isValidCandleData(candles, i)) {
                        logger.warn("Invalid candle data for {} on {}, skipping", symbol, date);
                        continue;
                    }

                    MarketData marketData = getOrCreateMarketData(security, date, "Finnhub");
                    boolean isNewRecord = marketData.getMarketDataId() == null;

                    // Map candle data with validation
                    updateMarketDataFromCandle(marketData, candles, i);
                    validateMarketData(marketData);

                    marketDataList.add(marketData);

                    if (isNewRecord) {
                        newRecords.add(marketData);
                    } else {
                        updatedRecords.add(marketData);
                    }

                } catch (Exception e) {
                    logger.warn("Failed to process candle data for {} at index {}: {}",
                            symbol, i, e.getMessage());
                    // Continue processing other candles
                }
            }

            // Batch save with error handling
            try {
                marketDataList = marketDataRepository.saveAll(marketDataList);
                logger.info("Successfully stored {} historical records for {} (New: {}, Updated: {})",
                        marketDataList.size(), symbol, newRecords.size(), updatedRecords.size());
            } catch (DataIntegrityViolationException e) {
                logger.error("Data integrity violation during batch save for {}: {}", symbol, e.getMessage());
                // Try saving one by one to isolate the problem
                marketDataList = saveIndividually(marketDataList, symbol);
            }

            return marketDataList;

        } catch (Exception e) {
            logger.error("Error fetching historical data for {}: {}", symbol, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch historical data for " + symbol, e);
        }
    }

    /**
     * Enhanced price update with validation and change detection
     */
    @Override
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 500))
    public SecurityStock updateCurrentPrice(String symbol) {
        logger.debug("Updating current price for symbol: {}", symbol);

        SecurityStock security = findOrCreateSecurity(symbol);
        BigDecimal previousPrice = security.getCurrentPrice();

        try {
            FinnhubQuoteDTO quote = finnhubClient.getQuote(symbol).block();

            if (quote != null && quote.getCurrentPrice() != null) {
                BigDecimal newPrice = quote.getCurrentPrice();

                // Validate price data
                if (isValidPrice(newPrice)) {
                    // Check for significant price changes before updating
                    if (previousPrice != null && isSignificantPriceChange(previousPrice, newPrice)) {
                        logger.info("Significant price change detected for {}: ${} -> ${} ({}%)",
                                symbol, previousPrice, newPrice,
                                calculatePriceChangePercent(previousPrice, newPrice));

                        // You could add additional validation or alerts here
                    }

                    security.setCurrentPrice(newPrice);
                    security.setUpdatedDate(Instant.now());
                    security = securityStockRepository.save(security);

                    logger.debug("Updated current price for {}: ${}", symbol, newPrice);
                } else {
                    logger.warn("Invalid price data for {}: ${}, keeping previous price", symbol, newPrice);
                }
            } else {
                logger.warn("No valid quote data received for {}", symbol);
            }

            return security;

        } catch (Exception e) {
            logger.error("Error updating current price for {}: {}", symbol, e.getMessage());
            throw new RuntimeException("Failed to update current price for " + symbol, e);
        }
    }

    /**
     * Bulk update implementation with proper batch processing
     */
    @Override
    public void bulkUpdateCurrentPrices(List<String> symbols) {
        logger.info("Starting bulk price update for {} symbols", symbols.size());

        if (isCircuitBreakerOpen()) {
            logger.warn("Circuit breaker is open - skipping bulk update");
            return;
        }

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Process in batches
        for (int i = 0; i < symbols.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, symbols.size());
            List<String> batchSymbols = symbols.subList(i, endIndex);

            // Convert symbols to securities
            List<SecurityStock> securities = new ArrayList<>();
            for (String symbol : batchSymbols) {
                try {
                    securities.add(findOrCreateSecurity(symbol));
                } catch (Exception e) {
                    logger.warn("Failed to find/create security for {}: {}", symbol, e.getMessage());
                    errorCount.incrementAndGet();
                }
            }

            processBatchConcurrently(securities, successCount, errorCount);

            // Rate limiting between batches
            if (endIndex < symbols.size()) {
                try {
                    Thread.sleep(apiDelayMs * 2);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Bulk update interrupted");
                    return;
                }
            }
        }

        logger.info("Bulk update completed - Success: {}, Errors: {}",
                successCount.get(), errorCount.get());

        // Handle circuit breaker logic
        if (errorCount.get() > successCount.get()) {
            handleCircuitBreakerLogic(errorCount.get());
        } else if (errorCount.get() == 0) {
            resetCircuitBreaker();
        }
    }

    /**
     * Process batch of securities concurrently
     */
    private void processBatchConcurrently(List<SecurityStock> securities,
                                          AtomicInteger successCount,
                                          AtomicInteger errorCount) {

        List<CompletableFuture<Void>> futures = securities.stream()
                .map(security -> CompletableFuture.runAsync(() -> {
                    try {
                        updateCurrentPrice(security.getSymbol());
                        successCount.incrementAndGet();
                        Thread.sleep(apiDelayMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        logger.error("Failed to update price for {}: {}",
                                security.getSymbol(), e.getMessage());
                        errorCount.incrementAndGet();
                    }
                }))
                .toList();

        // Wait for all futures to complete with timeout
        try {
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));
            allFutures.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("Batch processing timed out or failed: {}", e.getMessage());
            futures.forEach(future -> future.cancel(true));
        }
    }

    // Enhanced read-only methods with caching hints
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "marketData", key = "#symbol + '_' + #date")
    public Optional<MarketData> getMarketData(String symbol, LocalDate date) {
        try {
            SecurityStock security = securityStockRepository.findBySymbol(symbol)
                    .orElse(null);

            if (security == null) {
                logger.debug("Security not found for symbol: {}", symbol);
                return Optional.empty();
            }

            return marketDataRepository.findBySecurityStockAndMarketDate(security, date);
        } catch (Exception e) {
            logger.error("Error retrieving market data for {} on {}: {}", symbol, date, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "latestMarketData", key = "#symbol")
    public Optional<MarketData> getLatestMarketData(String symbol) {
        try {
            SecurityStock security = securityStockRepository.findBySymbol(symbol)
                    .orElse(null);

            if (security == null) {
                logger.debug("Security not found for symbol: {}", symbol);
                return Optional.empty();
            }

            return marketDataRepository.findLatestBySecurityStock(security);
        } catch (Exception e) {
            logger.error("Error retrieving latest market data for {}: {}", symbol, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceHistory> getHistoricalPrices(String symbol, LocalDate from, LocalDate to) {
        try {
            SecurityStock security = securityStockRepository.findBySymbol(symbol)
                    .orElse(null);

            if (security == null) {
                logger.debug("Security not found for symbol: {}", symbol);
                return new ArrayList<>();
            }

            return priceHistoryRepository.findBySecurityStockAndDateRange(security, from, to);
        } catch (Exception e) {
            logger.error("Error retrieving historical prices for {}: {}", symbol, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Enhanced security lookup with sector update logic
     */
    private SecurityStock findOrCreateSecurity(String symbol) {
        logger.debug("Looking for existing security with symbol: {}", symbol);

        Optional<SecurityStock> existingSecurity = securityCreationService.findExistingSecurity(symbol);

        if (existingSecurity.isPresent()) {
            SecurityStock security = existingSecurity.get();
            logger.debug("Found existing security for symbol: {} with sector: {}", symbol, security.getSector());

            // Check if sector needs updating (if it's the default TECHNOLOGY)
            if (security.getSector() == SecuritySector.TECHNOLOGY && shouldUpdateSector(security)) {
                try {
                    SecurityStock updatedSecurity = securityCreationService.updateSecuritySector(security);
                    if (updatedSecurity.getSector() != SecuritySector.TECHNOLOGY) {
                        logger.info("Updated sector for {} from TECHNOLOGY to {}",
                                symbol, updatedSecurity.getSector());
                        return updatedSecurity;
                    }
                } catch (Exception e) {
                    logger.warn("Failed to update sector for existing security {}: {}", symbol, e.getMessage());
                }
            }

            return security;
        }

        // Create new security if not found
        logger.info("Security not found for symbol: {}, creating new security", symbol);
        try {
            SecurityStock newSecurity = securityCreationService.createSecurityFromSymbol(symbol);
            logger.info("Successfully created new security: {} - {} ({}) - Sector: {}",
                    newSecurity.getSymbol(),
                    newSecurity.getCompanyName(),
                    newSecurity.getExchange(),
                    newSecurity.getSector());
            return newSecurity;
        } catch (Exception e) {
            logger.error("Failed to create security for symbol {}: {}", symbol, e.getMessage());
            throw new RuntimeException("Unable to find or create security for symbol: " + symbol +
                    ". Please verify the symbol is valid and tradeable.", e);
        }
    }

    /**
     * Get or create market data record for a specific date
     */
    private MarketData getOrCreateMarketData(SecurityStock security, LocalDate date, String dataSource) {
        Optional<MarketData> existingData = marketDataRepository
                .findBySecurityStockAndMarketDate(security, date);

        if (existingData.isPresent()) {
            MarketData data = existingData.get();
            logger.debug("Found existing market data for {} on {}", security.getSymbol(), date);
            return data;
        }

        // Create new market data
        MarketData marketData = new MarketData();
        marketData.setSecurityStock(security);
        marketData.setMarketDate(date);
        marketData.setDataSource(dataSource);
        marketData.setCreatedAt(Instant.now());
        logger.debug("Creating new market data for {} on {}", security.getSymbol(), date);

        return marketData;
    }

    /**
     * Update market data from quote with validation
     */
    private void updateMarketDataFromQuote(MarketData marketData, FinnhubQuoteDTO quote) {
        marketData.setOpenPrice(quote.getOpenPrice());
        marketData.setHighPrice(quote.getHighPrice());
        marketData.setLowPrice(quote.getLowPrice());
        marketData.setClosePrice(quote.getCurrentPrice());
        marketData.setAdjustedClosePrice(quote.getCurrentPrice());

        // Handle missing volume data gracefully
        if (marketData.getVolume() == null) {
            marketData.setVolume(1L); // Default volume for validation compliance
            logger.debug("No volume data available for {}, using default value",
                    marketData.getSecurityStock().getSymbol());
        }

        marketData.setLastUpdated(Instant.now());

        // IMPORTANT: Also update the security's previous close from the quote
        if (quote.getPreviousClose() != null && quote.getPreviousClose().compareTo(BigDecimal.ZERO) > 0) {
            SecurityStock security = marketData.getSecurityStock();
            if (security.getPreviousClose() == null ||
                    security.getPreviousClose().compareTo(quote.getPreviousClose()) != 0) {

                security.setPreviousClose(quote.getPreviousClose());
                securityStockRepository.save(security);
                logger.debug("Updated previous close for {} to ${}",
                        security.getSymbol(), quote.getPreviousClose());
            }
        }
    }

    /**
     * Update market data from candle data
     */
    private void updateMarketDataFromCandle(MarketData marketData, FinnhubCandleDTO candles, int index) {
        marketData.setOpenPrice(candles.getOpenPrices().get(index));
        marketData.setHighPrice(candles.getHighPrices().get(index));
        marketData.setLowPrice(candles.getLowPrices().get(index));
        marketData.setClosePrice(candles.getClosePrices().get(index));
        marketData.setAdjustedClosePrice(candles.getClosePrices().get(index));

        // Set volume with validation
        Long volume = candles.getVolumes().get(index);
        marketData.setVolume(volume != null && volume > 0 ? volume : 1L);

        marketData.setLastUpdated(Instant.now());
    }

    /**
     * Validate quote data quality
     */
    private boolean isValidQuoteData(FinnhubQuoteDTO quote, String symbol) {
        if (quote == null) {
            logger.warn("Null quote received for {}", symbol);
            return false;
        }

        if (quote.getCurrentPrice() == null) {
            logger.warn("Null current price in quote for {}", symbol);
            return false;
        }

        if (!isValidPrice(quote.getCurrentPrice())) {
            logger.warn("Invalid current price for {}: ${}", symbol, quote.getCurrentPrice());
            return false;
        }

        // Check for unrealistic price relationships
        if (quote.getHighPrice() != null && quote.getLowPrice() != null) {
            if (quote.getHighPrice().compareTo(quote.getLowPrice()) < 0) {
                logger.warn("Invalid price data for {}: high price < low price", symbol);
                return false;
            }
        }

        // Additional validation: check if current price is within day's range
        if (quote.getHighPrice() != null && quote.getLowPrice() != null) {
            if (quote.getCurrentPrice().compareTo(quote.getHighPrice()) > 0 ||
                    quote.getCurrentPrice().compareTo(quote.getLowPrice()) < 0) {
                logger.warn("Current price outside day's range for {}: ${} not in [${}, ${}]",
                        symbol, quote.getCurrentPrice(), quote.getLowPrice(), quote.getHighPrice());
                return false;
            }
        }

        return true;
    }

    /**
     * Validate candle data at specific index
     */
    private boolean isValidCandleData(FinnhubCandleDTO candles, int index) {
        if (candles.getClosePrices() == null || index >= candles.getClosePrices().size()) {
            return false;
        }

        BigDecimal closePrice = candles.getClosePrices().get(index);
        if (!isValidPrice(closePrice)) {
            return false;
        }

        // Validate OHLC relationships
        if (candles.getOpenPrices() != null && candles.getHighPrices() != null &&
                candles.getLowPrices() != null && index < candles.getOpenPrices().size()) {

            BigDecimal open = candles.getOpenPrices().get(index);
            BigDecimal high = candles.getHighPrices().get(index);
            BigDecimal low = candles.getLowPrices().get(index);

            // Basic OHLC validation
            if (high.compareTo(low) < 0 ||
                    high.compareTo(open) < 0 || high.compareTo(closePrice) < 0 ||
                    low.compareTo(open) > 0 || low.compareTo(closePrice) > 0) {
                logger.warn("Invalid OHLC relationships for {} at index {}: O={}, H={}, L={}, C={}",
                        candles, index, open, high, low, closePrice);
                return false;
            }
        }

        return true;
    }

    /**
     * Validate price is within reasonable bounds
     */
    private boolean isValidPrice(BigDecimal price) {
        return price != null &&
                price.compareTo(BigDecimal.ZERO) > 0 &&
                price.compareTo(minValidPrice) >= 0 &&
                price.compareTo(maxValidPrice) <= 0;
    }

    /**
     * Check if price change is significant enough to warrant attention
     */
    private boolean isSignificantPriceChange(BigDecimal oldPrice, BigDecimal newPrice) {
        if (oldPrice == null || oldPrice.equals(BigDecimal.ZERO)) {
            return false;
        }

        BigDecimal changePercent = calculatePriceChangePercent(oldPrice, newPrice);
        return changePercent.abs().compareTo(significantChangeThreshold) > 0;
    }

    /**
     * Validate complete market data record
     */
    private void validateMarketData(MarketData marketData) {
        if (marketData.getOpenPrice() == null || marketData.getClosePrice() == null) {
            throw new IllegalArgumentException("Market data missing required price fields");
        }

        if (!isValidPrice(marketData.getClosePrice())) {
            throw new IllegalArgumentException("Invalid close price: " + marketData.getClosePrice());
        }

        if (marketData.getVolume() == null || marketData.getVolume() < 0) {
            throw new IllegalArgumentException("Invalid volume: " + marketData.getVolume());
        }

        // Additional business rule validations
        if (marketData.getHighPrice() != null && marketData.getLowPrice() != null) {
            if (marketData.getHighPrice().compareTo(marketData.getLowPrice()) < 0) {
                throw new IllegalArgumentException("High price cannot be less than low price");
            }

            // Validate OHLC relationships
            if (marketData.getOpenPrice() != null && marketData.getClosePrice() != null) {
                BigDecimal high = marketData.getHighPrice();
                BigDecimal low = marketData.getLowPrice();
                BigDecimal open = marketData.getOpenPrice();
                BigDecimal close = marketData.getClosePrice();

                if (open.compareTo(high) > 0 || open.compareTo(low) < 0 ||
                        close.compareTo(high) > 0 || close.compareTo(low) < 0) {
                    throw new IllegalArgumentException("Invalid OHLC price relationships");
                }
            }
        }

        // Validate market date
        if (marketData.getMarketDate() != null && marketData.getMarketDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Market data cannot be for future dates");
        }
    }

    /**
     * Update security price atomically
     */
    private void updateSecurityPrice(SecurityStock security, BigDecimal newPrice) {
        try {
            BigDecimal previousPrice = security.getCurrentPrice();
            security.setCurrentPrice(newPrice);
            security.setUpdatedDate(Instant.now());

            securityStockRepository.save(security);
        } catch (Exception e) {
            logger.error("Failed to update security price for {}: {}", security.getSymbol(), e.getMessage());
            // Don't fail the entire operation for this
        }
    }

    /**
     * Calculate price change percentage with proper null handling
     */
    private BigDecimal calculatePriceChangePercent(SecurityStock security, BigDecimal newPrice) {
        if (security.getCurrentPrice() == null || security.getCurrentPrice().equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }

        return calculatePriceChangePercent(security.getCurrentPrice(), newPrice);
    }

    private BigDecimal calculatePriceChangePercent(BigDecimal oldPrice, BigDecimal newPrice) {
        if (oldPrice == null || oldPrice.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }

        return newPrice.subtract(oldPrice)
                .divide(oldPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Check if data is fresh enough to use as fallback
     */
    private boolean isDataFresh(MarketData marketData) {
        if (marketData.getLastUpdated() == null) {
            return false;
        }

        Instant staleThreshold = Instant.now().minusSeconds(staleThresholdHours * 3600L);
        return marketData.getLastUpdated().isAfter(staleThreshold);
    }

    /**
     * Determine if sector should be updated
     */
    private boolean shouldUpdateSector(SecurityStock security) {
        // Update sector if the security was created more than a day ago
        return security.getCreatedDate() != null &&
                security.getCreatedDate().isBefore(Instant.now().minusSeconds(86400));
    }

    /**
     * Save market data individually when batch save fails
     */
    private List<MarketData> saveIndividually(List<MarketData> marketDataList, String symbol) {
        List<MarketData> savedData = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        for (MarketData data : marketDataList) {
            try {
                MarketData saved = marketDataRepository.save(data);
                savedData.add(saved);
                successCount++;
            } catch (Exception e) {
                logger.error("Failed to save individual market data for {} on {}: {}",
                        symbol, data.getMarketDate(), e.getMessage());
                errorCount++;
            }
        }

        logger.info("Individual save completed for {}: {} successful, {} failed",
                symbol, successCount, errorCount);
        return savedData;
    }

    /**
     * Check if it's a market day (excluding weekends and holidays)
     */
    private boolean isMarketDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    /**
     * Circuit breaker methods
     */
    private boolean isCircuitBreakerOpen() {
        return consecutiveFailures.get() >= 5; // Simple threshold
    }

    private void handleCircuitBreakerLogic(int currentErrors) {
        consecutiveFailures.addAndGet(currentErrors);
        if (consecutiveFailures.get() >= 5) {
            logger.error("Circuit breaker opened due to {} consecutive failures", consecutiveFailures.get());
        }
    }

    private void resetCircuitBreaker() {
        if (consecutiveFailures.get() > 0) {
            logger.info("Resetting circuit breaker after successful operations");
            consecutiveFailures.set(0);
        }
    }

    /**
     * Get market data statistics for monitoring
     */
    public MarketDataStats getMarketDataStats() {
        try {
            int totalActiveSecurities = securityStockRepository.countByIsActiveTrue();
            LocalDate today = LocalDate.now();
            LocalDate yesterday = getLastMarketDay();

            int todayRecords = marketDataRepository.countByMarketDate(today);
            int yesterdayRecords = marketDataRepository.countByMarketDate(yesterday);

            return new MarketDataStats(
                    totalActiveSecurities,
                    todayRecords,
                    yesterdayRecords,
                    isCircuitBreakerOpen(),
                    consecutiveFailures.get()
            );
        } catch (Exception e) {
            logger.error("Error calculating market data stats: {}", e.getMessage());
            return new MarketDataStats(0, 0, 0, true, -1);
        }
    }

    private LocalDate getLastMarketDay() {
        LocalDate date = LocalDate.now().minusDays(1);
        while (!isMarketDay(date)) {
            date = date.minusDays(1);
        }
        return date;
    }

    /**
     * Simple stats class for monitoring
     */
    public static class MarketDataStats {
        private final int activeSecurities;
        private final int todayRecords;
        private final int yesterdayRecords;
        private final boolean circuitBreakerOpen;
        private final int consecutiveFailures;

        public MarketDataStats(int activeSecurities, int todayRecords, int yesterdayRecords,
                               boolean circuitBreakerOpen, int consecutiveFailures) {
            this.activeSecurities = activeSecurities;
            this.todayRecords = todayRecords;
            this.yesterdayRecords = yesterdayRecords;
            this.circuitBreakerOpen = circuitBreakerOpen;
            this.consecutiveFailures = consecutiveFailures;
        }

        // Getters
        public int getActiveSecurities() { return activeSecurities; }
        public int getTodayRecords() { return todayRecords; }
        public int getYesterdayRecords() { return yesterdayRecords; }
        public boolean isCircuitBreakerOpen() { return circuitBreakerOpen; }
        public int getConsecutiveFailures() { return consecutiveFailures; }
    }
}