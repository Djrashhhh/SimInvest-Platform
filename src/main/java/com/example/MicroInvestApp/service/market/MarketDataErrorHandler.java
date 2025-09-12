package com.example.MicroInvestApp.service.market;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enhanced error handling and retry mechanism for market data operations
 */
@Component
public class MarketDataErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(MarketDataErrorHandler.class);

    // Track errors by symbol
    private final Map<String, SymbolErrorTracker> errorTrackers = new ConcurrentHashMap<>();

    // Global error statistics
    private final AtomicInteger totalErrors = new AtomicInteger(0);
    private final AtomicInteger totalRecoveries = new AtomicInteger(0);

    /**
     * Record an error for a specific symbol
     */
    public void recordError(String symbol, String operation, Exception error) {
        SymbolErrorTracker tracker = errorTrackers.computeIfAbsent(symbol, SymbolErrorTracker::new);
        tracker.recordError(operation, error);
        totalErrors.incrementAndGet();

        logger.error("Error recorded for {} during {}: {}", symbol, operation, error.getMessage());

        // Check if this symbol should be temporarily disabled
        if (tracker.shouldTemporarilyDisable()) {
            logger.warn("Symbol {} has too many consecutive errors - recommending temporary disable", symbol);
        }
    }

    /**
     * Record a successful operation for a symbol
     */
    public void recordSuccess(String symbol, String operation) {
        SymbolErrorTracker tracker = errorTrackers.get(symbol);
        if (tracker != null) {
            boolean wasInErrorState = tracker.hasRecentErrors();
            tracker.recordSuccess(operation);

            if (wasInErrorState) {
                totalRecoveries.incrementAndGet();
                logger.info("Symbol {} recovered from error state during {}", symbol, operation);
            }
        }
    }

    /**
     * Check if a symbol should be skipped due to recent errors
     */
    public boolean shouldSkipSymbol(String symbol) {
        SymbolErrorTracker tracker = errorTrackers.get(symbol);
        return tracker != null && tracker.shouldTemporarilyDisable();
    }

    /**
     * Get error statistics for a specific symbol
     */
    public SymbolErrorStats getSymbolErrorStats(String symbol) {
        SymbolErrorTracker tracker = errorTrackers.get(symbol);
        return tracker != null ? tracker.getStats() : new SymbolErrorStats(symbol);
    }

    /**
     * Get global error statistics
     */
    public GlobalErrorStats getGlobalErrorStats() {
        GlobalErrorStats stats = new GlobalErrorStats();
        stats.totalErrors = totalErrors.get();
        stats.totalRecoveries = totalRecoveries.get();
        stats.symbolsInErrorState = (int) errorTrackers.values().stream()
                .filter(SymbolErrorTracker::hasRecentErrors)
                .count();
        stats.symbolsTemporarilyDisabled = (int) errorTrackers.values().stream()
                .filter(SymbolErrorTracker::shouldTemporarilyDisable)
                .count();
        stats.timestamp = Instant.now();
        return stats;
    }

    /**
     * Get list of symbols currently in error state
     */
    public List<String> getSymbolsInErrorState() {
        return errorTrackers.entrySet().stream()
                .filter(entry -> entry.getValue().hasRecentErrors())
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Reset error state for a specific symbol
     */
    public void resetSymbolErrors(String symbol) {
        errorTrackers.remove(symbol);
        logger.info("Error state reset for symbol: {}", symbol);
    }

    /**
     * Reset all error states (use with caution)
     */
    public void resetAllErrors() {
        int clearedCount = errorTrackers.size();
        errorTrackers.clear();
        totalErrors.set(0);
        totalRecoveries.set(0);
        logger.info("All error states reset - {} symbols cleared", clearedCount);
    }

    /**
     * Clean up old error trackers to prevent memory leaks
     */
    public void cleanupOldTrackers() {
        Instant cutoff = Instant.now().minusSeconds(86400 * 7); // 7 days
        AtomicInteger removedCount = new AtomicInteger(0);

        errorTrackers.entrySet().removeIf(entry -> {
            if (entry.getValue().getLastActivity().isBefore(cutoff)) {
                removedCount.incrementAndGet();
                return true;
            }
            return false;
        });

        if (removedCount.get() > 0) {
            logger.info("Cleaned up {} old error trackers", removedCount.get());
        }
    }

    /**
     * Track errors for individual symbols
     */
    private static class SymbolErrorTracker {
        private final String symbol;
        private final AtomicInteger consecutiveErrors = new AtomicInteger(0);
        private final AtomicInteger totalErrors = new AtomicInteger(0);
        private final AtomicInteger totalSuccesses = new AtomicInteger(0);
        private volatile Instant lastErrorTime;
        private volatile Instant lastSuccessTime;
        private volatile Instant lastActivity;
        private volatile String lastErrorMessage;
        private volatile String lastOperation;

        public SymbolErrorTracker(String symbol) {
            this.symbol = symbol;
            this.lastActivity = Instant.now();
        }

        public void recordError(String operation, Exception error) {
            consecutiveErrors.incrementAndGet();
            totalErrors.incrementAndGet();
            lastErrorTime = Instant.now();
            lastActivity = Instant.now();
            lastErrorMessage = error.getMessage();
            lastOperation = operation;
        }

        public void recordSuccess(String operation) {
            consecutiveErrors.set(0);
            totalSuccesses.incrementAndGet();
            lastSuccessTime = Instant.now();
            lastActivity = Instant.now();
            lastOperation = operation;
        }

        public boolean hasRecentErrors() {
            return consecutiveErrors.get() > 0 &&
                    lastErrorTime != null &&
                    lastErrorTime.isAfter(Instant.now().minusSeconds(3600)); // 1 hour
        }

        public boolean shouldTemporarilyDisable() {
            return consecutiveErrors.get() >= 5 && hasRecentErrors();
        }

        public Instant getLastActivity() {
            return lastActivity;
        }

        public SymbolErrorStats getStats() {
            SymbolErrorStats stats = new SymbolErrorStats(symbol);
            stats.consecutiveErrors = consecutiveErrors.get();
            stats.totalErrors = totalErrors.get();
            stats.totalSuccesses = totalSuccesses.get();
            stats.lastErrorTime = lastErrorTime;
            stats.lastSuccessTime = lastSuccessTime;
            stats.lastErrorMessage = lastErrorMessage;
            stats.lastOperation = lastOperation;
            stats.shouldTemporarilyDisable = shouldTemporarilyDisable();
            return stats;
        }
    }

    /**
     * Error statistics for individual symbols
     */
    public static class SymbolErrorStats {
        public String symbol;
        public int consecutiveErrors;
        public int totalErrors;
        public int totalSuccesses;
        public Instant lastErrorTime;
        public Instant lastSuccessTime;
        public String lastErrorMessage;
        public String lastOperation;
        public boolean shouldTemporarilyDisable;

        public SymbolErrorStats(String symbol) {
            this.symbol = symbol;
        }

        public double getSuccessRate() {
            int total = totalErrors + totalSuccesses;
            return total > 0 ? (double) totalSuccesses / total * 100 : 100.0;
        }

        public String getSummary() {
            return String.format("Symbol: %s | Success Rate: %.1f%% | Consecutive Errors: %d | Status: %s",
                    symbol, getSuccessRate(), consecutiveErrors,
                    shouldTemporarilyDisable ? "DISABLED" : "ACTIVE");
        }
    }

    /**
     * Global error statistics
     */
    public static class GlobalErrorStats {
        public int totalErrors;
        public int totalRecoveries;
        public int symbolsInErrorState;
        public int symbolsTemporarilyDisabled;
        public Instant timestamp;

        public double getGlobalSuccessRate() {
            int total = totalErrors + totalRecoveries;
            return total > 0 ? (double) totalRecoveries / total * 100 : 100.0;
        }

        public String getSummary() {
            return String.format("Global Stats | Success Rate: %.1f%% | Errors: %d | Recoveries: %d | Disabled: %d",
                    getGlobalSuccessRate(), totalErrors, totalRecoveries, symbolsTemporarilyDisabled);
        }
    }

    /**
     * Retry strategy configuration
     */
    public static class RetryConfig {
        public int maxAttempts = 3;
        public long baseDelayMs = 1000;
        public double backoffMultiplier = 2.0;
        public long maxDelayMs = 30000;
        public List<Class<? extends Exception>> retryableExceptions = List.of(
                java.net.SocketTimeoutException.class,
                java.net.ConnectException.class,
                org.springframework.web.client.ResourceAccessException.class
        );

        public boolean shouldRetry(Exception exception, int attemptNumber) {
            if (attemptNumber >= maxAttempts) {
                return false;
            }

            return retryableExceptions.stream()
                    .anyMatch(retryableClass -> retryableClass.isInstance(exception));
        }

        public long calculateDelay(int attemptNumber) {
            long delay = (long) (baseDelayMs * Math.pow(backoffMultiplier, attemptNumber - 1));
            return Math.min(delay, maxDelayMs);
        }
    }

    /**
     * Execute operation with retry logic
     */
    public <T> T executeWithRetry(String symbol, String operation,
                                  java.util.function.Supplier<T> supplier) {
        RetryConfig retryConfig = new RetryConfig();
        Exception lastException = null;

        for (int attempt = 1; attempt <= retryConfig.maxAttempts; attempt++) {
            try {
                T result = supplier.get();
                recordSuccess(symbol, operation);
                return result;

            } catch (Exception e) {
                lastException = e;
                recordError(symbol, operation, e);

                if (!retryConfig.shouldRetry(e, attempt)) {
                    logger.error("Operation {} failed for {} after {} attempts (non-retryable): {}",
                            operation, symbol, attempt, e.getMessage());
                    break;
                }

                if (attempt < retryConfig.maxAttempts) {
                    long delay = retryConfig.calculateDelay(attempt);
                    logger.warn("Operation {} failed for {} (attempt {}/{}), retrying in {}ms: {}",
                            operation, symbol, attempt, retryConfig.maxAttempts, delay, e.getMessage());

                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                } else {
                    logger.error("Operation {} failed for {} after {} attempts: {}",
                            operation, symbol, retryConfig.maxAttempts, e.getMessage());
                }
            }
        }

        throw new RuntimeException("Operation failed after " + retryConfig.maxAttempts + " attempts", lastException);
    }

    /**
     * Batch error reporting for multiple operations
     */
    public BatchErrorReport processBatchResults(String operationType,
                                                Map<String, Exception> errors,
                                                List<String> successes) {
        BatchErrorReport report = new BatchErrorReport();
        report.operationType = operationType;
        report.totalProcessed = errors.size() + successes.size();
        report.successCount = successes.size();
        report.errorCount = errors.size();
        report.timestamp = Instant.now();

        // Record individual results
        errors.forEach(this::recordError);
        successes.forEach(symbol -> recordSuccess(symbol, operationType));

        // Calculate success rate
        if (report.totalProcessed > 0) {
            report.successRate = (double) report.successCount / report.totalProcessed * 100;
        }

        // Categorize errors
        report.errorsByType = errors.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        e -> e.getClass().getSimpleName(),
                        java.util.stream.Collectors.counting()
                ));

        logger.info("Batch {} completed - Success: {}/{} ({}%), Error types: {}",
                operationType, report.successCount, report.totalProcessed,
                String.format("%.1f", report.successRate), report.errorsByType);

        return report;
    }

    private void recordError(String s, Exception e) {
        // This method appears to be incomplete in the original code
        // It should probably call the main recordError method with an operation parameter
        recordError(s, "batch_operation", e);
    }

    /**
     * Batch processing error report
     */
    public static class BatchErrorReport {
        public String operationType;
        public int totalProcessed;
        public int successCount;
        public int errorCount;
        public double successRate;
        public Map<String, Long> errorsByType;
        public Instant timestamp;

        public boolean isAcceptable() {
            return successRate >= 90.0; // 90% success rate threshold
        }

        public String getSummary() {
            return String.format("%s batch: %d/%d successful (%.1f%%) - %s",
                    operationType, successCount, totalProcessed, successRate,
                    isAcceptable() ? "ACCEPTABLE" : "NEEDS ATTENTION");
        }
    }

    /**
     * Comprehensive error analysis and recommendations
     */
    public ErrorAnalysisReport analyzeSystemErrors() {
        ErrorAnalysisReport report = new ErrorAnalysisReport();
        report.timestamp = Instant.now();

        // Analyze error patterns
        List<SymbolErrorStats> allStats = errorTrackers.values().stream()
                .map(SymbolErrorTracker::getStats)
                .toList();

        report.totalSymbolsTracked = allStats.size();
        report.symbolsWithErrors = (int) allStats.stream().filter(s -> s.totalErrors > 0).count();
        report.symbolsDisabled = (int) allStats.stream().filter(s -> s.shouldTemporarilyDisable).count();

        // Find most problematic symbols
        report.mostProblematicSymbols = allStats.stream()
                .filter(s -> s.totalErrors > 0)
                .sorted((a, b) -> Integer.compare(b.consecutiveErrors, a.consecutiveErrors))
                .limit(10)
                .map(s -> s.symbol)
                .toList();

        // Error distribution by type
        Map<String, Integer> errorTypes = new ConcurrentHashMap<>();
        errorTrackers.values().forEach(tracker -> {
            if (tracker.lastErrorMessage != null) {
                String errorType = categorizeError(tracker.lastErrorMessage);
                errorTypes.merge(errorType, 1, Integer::sum);
            }
        });
        report.errorDistribution = errorTypes;

        // Generate recommendations
        report.recommendations = generateErrorRecommendations(report);

        return report;
    }

    private String categorizeError(String errorMessage) {
        if (errorMessage == null) return "Unknown";

        String lower = errorMessage.toLowerCase();
        if (lower.contains("timeout") || lower.contains("timed out")) {
            return "Timeout";
        } else if (lower.contains("connection") || lower.contains("connect")) {
            return "Connection";
        } else if (lower.contains("rate limit") || lower.contains("429")) {
            return "Rate Limit";
        } else if (lower.contains("invalid") || lower.contains("validation")) {
            return "Validation";
        } else if (lower.contains("not found") || lower.contains("404")) {
            return "Not Found";
        } else if (lower.contains("unauthorized") || lower.contains("401")) {
            return "Authentication";
        } else {
            return "Other";
        }
    }

    private List<String> generateErrorRecommendations(ErrorAnalysisReport report) {
        List<String> recommendations = new java.util.ArrayList<>();

        if (report.symbolsDisabled > 0) {
            recommendations.add(String.format("Review %d temporarily disabled symbols", report.symbolsDisabled));
        }

        // Analyze error distribution
        report.errorDistribution.forEach((errorType, count) -> {
            if (count > 5) { // Threshold for significant error count
                switch (errorType) {
                    case "Timeout":
                        recommendations.add("Consider increasing API timeout values");
                        break;
                    case "Rate Limit":
                        recommendations.add("Increase delays between API calls");
                        break;
                    case "Connection":
                        recommendations.add("Check network connectivity and API endpoint status");
                        break;
                    case "Validation":
                        recommendations.add("Review data validation rules and API response format");
                        break;
                    case "Authentication":
                        recommendations.add("Verify API token and authentication configuration");
                        break;
                }
            }
        });

        if (report.symbolsWithErrors > report.totalSymbolsTracked * 0.1) {
            recommendations.add("High error rate detected - consider circuit breaker activation");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Error levels are within acceptable ranges");
        }

        return recommendations;
    }

    /**
     * Error analysis report
     */
    public static class ErrorAnalysisReport {
        public int totalSymbolsTracked;
        public int symbolsWithErrors;
        public int symbolsDisabled;
        public List<String> mostProblematicSymbols;
        public Map<String, Integer> errorDistribution;
        public List<String> recommendations;
        public Instant timestamp;

        public boolean requiresAttention() {
            return symbolsDisabled > 0 ||
                    (totalSymbolsTracked > 0 && symbolsWithErrors > totalSymbolsTracked * 0.05);
        }
    }
}