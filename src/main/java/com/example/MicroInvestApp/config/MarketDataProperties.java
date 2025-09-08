package com.example.MicroInvestApp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "market-data")
@Validated
public class MarketDataProperties {

    /**
     * Scheduler configuration
     */
    private Scheduler scheduler = new Scheduler();

    /**
     * Data validation configuration
     */
    private Validation validation = new Validation();

    /**
     * Circuit breaker configuration
     */
    private CircuitBreaker circuitBreaker = new CircuitBreaker();

    /**
     * Cache configuration
     */
    private Cache cache = new Cache();

    public static class Scheduler {
        @Min(1)
        @Max(1000)
        private int batchSize = 50;

        @Min(1)
        @Max(20)
        private int maxThreads = 5;

        @Min(50)
        @Max(5000)
        private long apiDelayMs = 100;

        @Min(1)
        @Max(48)
        private int staleThresholdHours = 24;

        private boolean enableRecoveryJob = true;
        private boolean enablePreMarketValidation = true;
        private boolean enableDataCleanup = true;

        // Getters and setters
        public int getBatchSize() { return batchSize; }
        public void setBatchSize(int batchSize) { this.batchSize = batchSize; }

        public int getMaxThreads() { return maxThreads; }
        public void setMaxThreads(int maxThreads) { this.maxThreads = maxThreads; }

        public long getApiDelayMs() { return apiDelayMs; }
        public void setApiDelayMs(long apiDelayMs) { this.apiDelayMs = apiDelayMs; }

        public int getStaleThresholdHours() { return staleThresholdHours; }
        public void setStaleThresholdHours(int staleThresholdHours) { this.staleThresholdHours = staleThresholdHours; }

        public boolean isEnableRecoveryJob() { return enableRecoveryJob; }
        public void setEnableRecoveryJob(boolean enableRecoveryJob) { this.enableRecoveryJob = enableRecoveryJob; }

        public boolean isEnablePreMarketValidation() { return enablePreMarketValidation; }
        public void setEnablePreMarketValidation(boolean enablePreMarketValidation) { this.enablePreMarketValidation = enablePreMarketValidation; }

        public boolean isEnableDataCleanup() { return enableDataCleanup; }
        public void setEnableDataCleanup(boolean enableDataCleanup) { this.enableDataCleanup = enableDataCleanup; }
    }

    public static class Validation {
        private BigDecimal minPrice = new BigDecimal("0.01");
        private BigDecimal maxPrice = new BigDecimal("100000");
        private BigDecimal significantChangeThreshold = new BigDecimal("5.0");
        private boolean enableDataValidation = true;
        private boolean enablePriceChangeAlerts = true;

        // Getters and setters
        public BigDecimal getMinPrice() { return minPrice; }
        public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

        public BigDecimal getMaxPrice() { return maxPrice; }
        public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }

        public BigDecimal getSignificantChangeThreshold() { return significantChangeThreshold; }
        public void setSignificantChangeThreshold(BigDecimal significantChangeThreshold) { this.significantChangeThreshold = significantChangeThreshold; }

        public boolean isEnableDataValidation() { return enableDataValidation; }
        public void setEnableDataValidation(boolean enableDataValidation) { this.enableDataValidation = enableDataValidation; }

        public boolean isEnablePriceChangeAlerts() { return enablePriceChangeAlerts; }
        public void setEnablePriceChangeAlerts(boolean enablePriceChangeAlerts) { this.enablePriceChangeAlerts = enablePriceChangeAlerts; }
    }

    public static class CircuitBreaker {
        @Min(1)
        @Max(50)
        private int failureThreshold = 5;

        @Min(1)
        @Max(60)
        private int timeoutMinutes = 5;

        @Min(1)
        @Max(10)
        private int maxRetryAttempts = 3;

        private boolean enabled = true;

        // Getters and setters
        public int getFailureThreshold() { return failureThreshold; }
        public void setFailureThreshold(int failureThreshold) { this.failureThreshold = failureThreshold; }

        public int getTimeoutMinutes() { return timeoutMinutes; }
        public void setTimeoutMinutes(int timeoutMinutes) { this.timeoutMinutes = timeoutMinutes; }

        public int getMaxRetryAttempts() { return maxRetryAttempts; }
        public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class Cache {
        @Min(1)
        @Max(24)
        private int quoteCacheHours = 1;

        @Min(1)
        @Max(168)
        private int historicalCacheHours = 24;

        private boolean enableCaching = true;

        // Getters and setters
        public int getQuoteCacheHours() { return quoteCacheHours; }
        public void setQuoteCacheHours(int quoteCacheHours) { this.quoteCacheHours = quoteCacheHours; }

        public int getHistoricalCacheHours() { return historicalCacheHours; }
        public void setHistoricalCacheHours(int historicalCacheHours) { this.historicalCacheHours = historicalCacheHours; }

        public boolean isEnableCaching() { return enableCaching; }
        public void setEnableCaching(boolean enableCaching) { this.enableCaching = enableCaching; }
    }

    // Main getters and setters
    public Scheduler getScheduler() { return scheduler; }
    public void setScheduler(Scheduler scheduler) { this.scheduler = scheduler; }

    public Validation getValidation() { return validation; }
    public void setValidation(Validation validation) { this.validation = validation; }

    public CircuitBreaker getCircuitBreaker() { return circuitBreaker; }
    public void setCircuitBreaker(CircuitBreaker circuitBreaker) { this.circuitBreaker = circuitBreaker; }

    public Cache getCache() { return cache; }
    public void setCache(Cache cache) { this.cache = cache; }
}