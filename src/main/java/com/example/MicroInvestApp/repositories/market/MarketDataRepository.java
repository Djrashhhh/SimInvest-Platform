package com.example.MicroInvestApp.repositories.market;

import com.example.MicroInvestApp.domain.market.MarketData;
import com.example.MicroInvestApp.domain.market.SecurityStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarketDataRepository extends JpaRepository<MarketData, Long> {

    // Find market data by security stock and market date
    Optional<MarketData> findBySecurityStockAndMarketDate(SecurityStock securityStock, LocalDate marketDate);

    // Find all market data for a security stock, ordered by market date descending
    List<MarketData> findBySecurityStockOrderByMarketDateDesc(SecurityStock securityStock);

    // Find market data for a security stock within a date range, ordered by market date ascending
    @Query("SELECT md FROM MarketData md WHERE md.securityStock = :securityStock " +
            "AND md.marketDate BETWEEN :startDate AND :endDate ORDER BY md.marketDate ASC")
    List<MarketData> findBySecurityStockAndDateRange(
            @Param("securityStock") SecurityStock securityStock,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Find the latest market data for a security stock
    @Query("SELECT md FROM MarketData md WHERE md.securityStock = :securityStock " +
            "ORDER BY md.marketDate DESC LIMIT 1")
    Optional<MarketData> findLatestBySecurityStock(@Param("securityStock") SecurityStock securityStock);

    // Count market data records within date range for a security
    @Query("SELECT COUNT(md) FROM MarketData md WHERE md.securityStock = :security " +
            "AND md.marketDate BETWEEN :startDate AND :endDate")
    long countBySecurityStockAndDateRange(
            @Param("security") SecurityStock security,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Delete old market data records before a cutoff date
    @Modifying
    @Transactional
    @Query("DELETE FROM MarketData md WHERE md.marketDate < :cutoffDate")
    int deleteOldRecords(@Param("cutoffDate") LocalDate cutoffDate);

    // Count records for a specific date
    @Query("SELECT COUNT(md) FROM MarketData md WHERE md.marketDate = :date")
    int countByMarketDate(@Param("date") LocalDate date);

    // Find securities missing market data for a specific date
    @Query("SELECT ss FROM SecurityStock ss WHERE ss.isActive = true " +
            "AND NOT EXISTS (SELECT 1 FROM MarketData md WHERE md.securityStock = ss AND md.marketDate = :date)")
    List<SecurityStock> findActiveSecuritiesMissingMarketDataForDate(@Param("date") LocalDate date);

    // Find market data that hasn't been updated recently
    @Query("SELECT md FROM MarketData md WHERE md.lastUpdated < :threshold")
    List<MarketData> findStaleMarketData(@Param("threshold") java.time.Instant threshold);

    // Find market data with potential quality issues
    @Query("SELECT md FROM MarketData md WHERE " +
            "md.openPrice IS NULL OR md.closePrice IS NULL OR " +
            "md.highPrice < md.lowPrice OR " +
            "md.volume < 0")
    List<MarketData> findMarketDataWithQualityIssues();

    // Get market data summary for a date range
    @Query("SELECT md.securityStock.symbol, COUNT(md), AVG(md.volume), " +
            "MIN(md.lowPrice), MAX(md.highPrice) " +
            "FROM MarketData md WHERE md.marketDate BETWEEN :startDate AND :endDate " +
            "GROUP BY md.securityStock.symbol")
    List<Object[]> getMarketDataSummary(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Find top volume securities for a specific date
    @Query("SELECT md FROM MarketData md WHERE md.marketDate = :date " +
            "ORDER BY md.volume DESC")
    List<MarketData> findTopVolumeSecuritiesByDate(@Param("date") LocalDate date);

    // Check if market data exists for symbol and date
    @Query("SELECT COUNT(md) > 0 FROM MarketData md " +
            "WHERE md.securityStock.symbol = :symbol AND md.marketDate = :date")
    boolean existsBySymbolAndDate(@Param("symbol") String symbol, @Param("date") LocalDate date);
}