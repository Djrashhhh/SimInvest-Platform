package com.example.MicroInvestApp.service.market;

import com.example.MicroInvestApp.domain.market.SecurityStock;
import com.example.MicroInvestApp.repositories.market.SecurityStockRepository;
import com.example.MicroInvestApp.repositories.market.MarketDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

@Service
public class MarketDataMonitor {

    private static final Logger logger = LoggerFactory.getLogger(MarketDataMonitor.class);

    private final SecurityStockRepository securityStockRepository;
    private final MarketDataRepository marketDataRepository;

    @Autowired
    public MarketDataMonitor(SecurityStockRepository securityStockRepository,
                             MarketDataRepository marketDataRepository) {
        this.securityStockRepository = securityStockRepository;
        this.marketDataRepository = marketDataRepository;
    }

    /**
     * Generate system health report
     */
    public MarketDataHealthReport generateHealthReport() {
        try {
            int totalSecurities = securityStockRepository.countByIsActiveTrue();
            int todayRecords = marketDataRepository.countByMarketDate(LocalDate.now());
            List<String> issues = new ArrayList<>();
            List<String> recommendations = new ArrayList<>();

            boolean isHealthy = todayRecords > 0 && totalSecurities > 0;
            int healthScore = calculateHealthScore(totalSecurities, todayRecords);

            if (todayRecords == 0) {
                issues.add("No market data records for today");
                recommendations.add("Check data fetching services");
            }

            if (totalSecurities == 0) {
                issues.add("No active securities found");
                recommendations.add("Add securities to the system");
            }

            return new MarketDataHealthReport(
                    isHealthy,
                    healthScore,
                    isHealthy ? "System is healthy" : "System has issues",
                    Instant.now().toString(),
                    issues,
                    recommendations
            );
        } catch (Exception e) {
            logger.error("Error generating health report: {}", e.getMessage());
            return MarketDataHealthReport.createErrorReport(e.getMessage());
        }
    }

    /**
     * Generate data coverage report
     */
    public DataCoverageReport generateDataCoverageReport(LocalDate from, LocalDate to) {
        try {
            int totalSecurities = securityStockRepository.countByIsActiveTrue();
            // Simplified coverage calculation
            double coveragePercentage = totalSecurities > 0 ? 85.0 : 0.0; // Mock percentage
            List<String> missingData = new ArrayList<>();

            return new DataCoverageReport(
                    from.toString(),
                    to.toString(),
                    totalSecurities,
                    coveragePercentage,
                    missingData
            );
        } catch (Exception e) {
            logger.error("Error generating coverage report: {}", e.getMessage());
            return DataCoverageReport.createErrorReport(e.getMessage());
        }
    }

    /**
     * Detect significant price movements
     */
    public List<PriceAlertInfo> detectSignificantPriceMovements() {
        List<PriceAlertInfo> alerts = new ArrayList<>();
        try {
            List<SecurityStock> securities = securityStockRepository
                    .findSecuritiesWithSignificantPriceChanges(BigDecimal.valueOf(5.0));

            for (SecurityStock security : securities) {
                BigDecimal changePercent = security.getPriceChangePercent();
                if (changePercent != null) {
                    alerts.add(new PriceAlertInfo(
                            security.getSymbol(),
                            security.getCompanyName(),
                            security.getCurrentPrice().doubleValue(),
                            security.getPreviousClose() != null ? security.getPreviousClose().doubleValue() : 0.0,
                            changePercent.doubleValue(),
                            changePercent.doubleValue() > 0 ? "SIGNIFICANT_GAIN" : "SIGNIFICANT_LOSS"
                    ));
                }
            }
        } catch (Exception e) {
            logger.error("Error detecting price movements: {}", e.getMessage());
        }
        return alerts;
    }

    /**
     * Identify problematic securities
     */
    public List<SecurityStock> identifyProblematicSecurities() {
        try {
            Instant staleThreshold = Instant.now().minusSeconds(24 * 3600); // 24 hours ago
            return securityStockRepository.findSecuritiesWithStalePrices(staleThreshold);
        } catch (Exception e) {
            logger.error("Error identifying problematic securities: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Generate recovery recommendations
     */
    public List<String> generateRecoveryRecommendations() {
        List<String> recommendations = new ArrayList<>();
        try {
            List<SecurityStock> staleSecurities = identifyProblematicSecurities();

            if (!staleSecurities.isEmpty()) {
                recommendations.add("Update prices for " + staleSecurities.size() + " stale securities");
                recommendations.add("Check Finnhub API connectivity");
                recommendations.add("Verify market data scheduler is running");
            } else {
                recommendations.add("System appears to be running normally");
            }
        } catch (Exception e) {
            recommendations.add("Error generating recommendations: " + e.getMessage());
        }
        return recommendations;
    }

    private int calculateHealthScore(int totalSecurities, int todayRecords) {
        if (totalSecurities == 0) return 0;
        if (todayRecords == 0) return 25;
        if (todayRecords >= totalSecurities * 0.8) return 100;
        if (todayRecords >= totalSecurities * 0.5) return 75;
        return 50;
    }

    // Data classes
    public static class MarketDataHealthReport {
        public final boolean healthy;
        public final int healthScore;
        public final String summary;
        public final String lastUpdateTime;
        public final List<String> issues;
        public final List<String> recommendations;

        public MarketDataHealthReport(boolean healthy, int healthScore, String summary,
                                      String lastUpdateTime, List<String> issues,
                                      List<String> recommendations) {
            this.healthy = healthy;
            this.healthScore = healthScore;
            this.summary = summary;
            this.lastUpdateTime = lastUpdateTime;
            this.issues = issues;
            this.recommendations = recommendations;
        }

        public boolean isHealthy() { return healthy; }
        public String getSummary() { return summary; }

        public static MarketDataHealthReport createErrorReport(String error) {
            return new MarketDataHealthReport(
                    false, 0, "Error: " + error, Instant.now().toString(),
                    List.of(error), List.of("Check system logs", "Restart services")
            );
        }
    }

    public static class DataCoverageReport {
        public final String fromDate;
        public final String toDate;
        public final int totalSecurities;
        public final double coveragePercentage;
        public final List<String> missingData;

        public DataCoverageReport(String fromDate, String toDate, int totalSecurities,
                                  double coveragePercentage, List<String> missingData) {
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.totalSecurities = totalSecurities;
            this.coveragePercentage = coveragePercentage;
            this.missingData = missingData;
        }

        public static DataCoverageReport createErrorReport(String error) {
            return new DataCoverageReport("", "", 0, 0.0, List.of("Error: " + error));
        }
    }

    public static class PriceAlertInfo {
        public final String symbol;
        public final String companyName;
        public final double currentPrice;
        public final double previousPrice;
        public final double changePercentage;
        public final String alertType;

        public PriceAlertInfo(String symbol, String companyName, double currentPrice,
                              double previousPrice, double changePercentage, String alertType) {
            this.symbol = symbol;
            this.companyName = companyName;
            this.currentPrice = currentPrice;
            this.previousPrice = previousPrice;
            this.changePercentage = changePercentage;
            this.alertType = alertType;
        }
    }
}