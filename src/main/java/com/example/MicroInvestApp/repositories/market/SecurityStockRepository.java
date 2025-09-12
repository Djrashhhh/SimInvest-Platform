package com.example.MicroInvestApp.repositories.market;

import com.example.MicroInvestApp.domain.enums.Exchange;
import com.example.MicroInvestApp.domain.enums.SecuritySector;
import com.example.MicroInvestApp.domain.market.SecurityStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityStockRepository extends JpaRepository<SecurityStock, Long> {

    // ========== EXISTING METHODS (Enhanced) ==========

    /**
     * Find a stock by its symbol
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.symbol = :symbol")
    Optional<SecurityStock> findBySymbol(@Param("symbol") String symbol);

    /**
     * Find all active stocks
     */
    List<SecurityStock> findByIsActiveTrue();

    /**
     * Find active stocks with pagination
     */
    Page<SecurityStock> findByIsActiveTrue(Pageable pageable);

    /**
     * Find inactive stocks
     */
    List<SecurityStock> findByIsActiveFalse();

    /**
     * Count active stocks
     */
    int countByIsActiveTrue();

    /**
     * Count inactive stocks
     */
    long countByIsActiveFalse();

    /**
     * Find active stocks by a list of symbols
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.symbol IN :symbols AND s.isActive = true")
    List<SecurityStock> findActiveBySymbolIn(@Param("symbols") List<String> symbols);

    /**
     * Find active securities missing market data for a specific date
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true " +
            "AND NOT EXISTS (SELECT 1 FROM MarketData md WHERE md.securityStock = s AND md.marketDate = :date)")
    List<SecurityStock> findActiveSecuritiesMissingMarketDataForDate(@Param("date") LocalDate date);

    /**
     * Find securities with stale current prices (not updated recently)
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true AND s.updatedDate < :threshold")
    List<SecurityStock> findSecuritiesWithStalePrices(@Param("threshold") Instant threshold);

    /**
     * Find securities by sector (enhanced)
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true AND s.sector = :sector")
    List<SecurityStock> findActiveBySecuritySector(@Param("sector") SecuritySector sector);

    /**
     * Find securities by exchange (enhanced to use enum)
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true AND s.exchange = :exchange")
    List<SecurityStock> findActiveByExchange(@Param("exchange") Exchange exchange);

    /**
     * Find securities with significant price changes
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true " +
            "AND s.currentPrice IS NOT NULL AND s.previousClose IS NOT NULL " +
            "AND s.previousClose <> 0 " +
            "AND ABS(((s.currentPrice - s.previousClose) / s.previousClose) * 100.0) > :threshold")
    List<SecurityStock> findSecuritiesWithSignificantPriceChanges(@Param("threshold") BigDecimal threshold);

    /**
     * Get top securities by market cap
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true " +
            "ORDER BY s.marketCap DESC")
    List<SecurityStock> findTopSecuritiesByMarketCap(Pageable pageable);

    /**
     * Find securities that need sector updates
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true " +
            "AND s.sector = :defaultSector " +
            "AND s.createdDate < :threshold")
    List<SecurityStock> findSecuritiesNeedingSectorUpdate(
            @Param("defaultSector") SecuritySector defaultSector,
            @Param("threshold") Instant threshold);

    /**
     * Count securities by sector
     */
    @Query("SELECT s.sector, COUNT(s) FROM SecurityStock s WHERE s.isActive = true GROUP BY s.sector")
    List<Object[]> countSecuritiesBySector();

    /**
     * Find recently created securities that might need validation
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true " +
            "AND s.createdDate > :threshold")
    List<SecurityStock> findRecentlyCreatedSecurities(@Param("threshold") Instant threshold);

    // ========== NEW METHODS FOR MARKET PAGE SUPPORT ==========

    /**
     * Search securities by symbol or company name (case-insensitive)
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true AND " +
            "(UPPER(s.symbol) LIKE UPPER(CONCAT('%', :symbol, '%')) OR " +
            "UPPER(s.companyName) LIKE UPPER(CONCAT('%', :companyName, '%')))")
    List<SecurityStock> findBySymbolContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(
            @Param("symbol") String symbol,
            @Param("companyName") String companyName,
            Pageable pageable);

    /**
     * Find trending stocks (most recently updated)
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true " +
            "ORDER BY s.updatedDate DESC")
    List<SecurityStock> findByIsActiveTrueOrderByUpdatedDateDesc(Pageable pageable);

    /**
     * Find securities by sector with pagination
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true AND s.sector = :sector " +
            "ORDER BY s.marketCap DESC")
    List<SecurityStock> findBySectorAndIsActiveTrue(@Param("sector") SecuritySector sector, Pageable pageable);

    /**
     * Find securities by exchange with pagination
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true AND s.exchange = :exchange " +
            "ORDER BY s.marketCap DESC")
    List<SecurityStock> findByExchangeAndIsActiveTrue(@Param("exchange") Exchange exchange, Pageable pageable);

    /**
     * Count securities by exchange
     */
    @Query("SELECT s.exchange, COUNT(s) FROM SecurityStock s WHERE s.isActive = true GROUP BY s.exchange")
    List<Object[]> countSecuritiesByExchange();

    /**
     * Find securities within a price range
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true AND " +
            "s.currentPrice BETWEEN :minPrice AND :maxPrice " +
            "ORDER BY s.currentPrice ASC")
    List<SecurityStock> findByPriceRange(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    /**
     * Find securities by minimum market cap
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true AND " +
            "s.marketCap >= :minMarketCap ORDER BY s.marketCap DESC")
    List<SecurityStock> findByMinMarketCap(
            @Param("minMarketCap") BigDecimal minMarketCap,
            Pageable pageable);

    /**
     * Find securities with highest price changes (gainers)
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true " +
            "AND s.currentPrice IS NOT NULL AND s.previousClose IS NOT NULL " +
            "AND s.previousClose > 0 " +
            "ORDER BY ((s.currentPrice - s.previousClose) / s.previousClose) DESC")
    List<SecurityStock> findTopGainers(Pageable pageable);

    /**
     * Find securities with lowest price changes (losers)
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true " +
            "AND s.currentPrice IS NOT NULL AND s.previousClose IS NOT NULL " +
            "AND s.previousClose > 0 " +
            "ORDER BY ((s.currentPrice - s.previousClose) / s.previousClose) ASC")
    List<SecurityStock> findTopLosers(Pageable pageable);

    /**
     * Find securities by multiple sectors
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true AND s.sector IN :sectors " +
            "ORDER BY s.marketCap DESC")
    List<SecurityStock> findBySectorIn(@Param("sectors") List<SecuritySector> sectors, Pageable pageable);

    /**
     * Find securities by multiple exchanges
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true AND s.exchange IN :exchanges " +
            "ORDER BY s.marketCap DESC")
    List<SecurityStock> findByExchangeIn(@Param("exchanges") List<Exchange> exchanges, Pageable pageable);

    /**
     * Get average price by sector
     */
    @Query("SELECT s.sector, AVG(s.currentPrice) FROM SecurityStock s " +
            "WHERE s.isActive = true AND s.currentPrice IS NOT NULL " +
            "GROUP BY s.sector")
    List<Object[]> getAveragePriceBySector();

    /**
     * Get total market cap by sector
     */
    @Query("SELECT s.sector, SUM(s.marketCap) FROM SecurityStock s " +
            "WHERE s.isActive = true AND s.marketCap IS NOT NULL " +
            "GROUP BY s.sector")
    List<Object[]> getTotalMarketCapBySector();

    /**
     * Find securities updated within a time range
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true " +
            "AND s.updatedDate BETWEEN :startTime AND :endTime " +
            "ORDER BY s.updatedDate DESC")
    List<SecurityStock> findByUpdatedDateBetween(
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    /**
     * Find securities created within a time range
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true " +
            "AND s.createdDate BETWEEN :startTime AND :endTime " +
            "ORDER BY s.createdDate DESC")
    List<SecurityStock> findByCreatedDateBetween(
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    /**
     * Find securities with null or zero prices (data quality check)
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true " +
            "AND (s.currentPrice IS NULL OR s.currentPrice = 0)")
    List<SecurityStock> findSecuritiesWithInvalidPrices();

    /**
     * Find securities with null or zero market cap (data quality check)
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true " +
            "AND (s.marketCap IS NULL OR s.marketCap = 0)")
    List<SecurityStock> findSecuritiesWithInvalidMarketCap();

    /**
     * Get securities count by security type
     */
    @Query("SELECT s.securityType, COUNT(s) FROM SecurityStock s " +
            "WHERE s.isActive = true GROUP BY s.securityType")
    List<Object[]> countSecuritiesByType();

    /**
     * Find securities with most recent price updates (for watchlist functionality)
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.symbol IN :symbols " +
            "AND s.isActive = true ORDER BY s.updatedDate DESC")
    List<SecurityStock> findWatchlistSecurities(@Param("symbols") List<String> symbols);

    /**
     * Advanced search with multiple criteria
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true " +
            "AND (:sector IS NULL OR s.sector = :sector) " +
            "AND (:exchange IS NULL OR s.exchange = :exchange) " +
            "AND (:minPrice IS NULL OR s.currentPrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR s.currentPrice <= :maxPrice) " +
            "AND (:minMarketCap IS NULL OR s.marketCap >= :minMarketCap) " +
            "AND (:searchTerm IS NULL OR " +
            "     UPPER(s.symbol) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR " +
            "     UPPER(s.companyName) LIKE UPPER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY s.marketCap DESC")
    List<SecurityStock> findWithFilters(
            @Param("sector") SecuritySector sector,
            @Param("exchange") Exchange exchange,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minMarketCap") BigDecimal minMarketCap,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    /**
     * Get market overview statistics
     */
    @Query("SELECT " +
            "COUNT(s) as totalCount, " +
            "AVG(s.currentPrice) as avgPrice, " +
            "SUM(s.marketCap) as totalMarketCap, " +
            "MAX(s.currentPrice) as maxPrice, " +
            "MIN(s.currentPrice) as minPrice " +
            "FROM SecurityStock s WHERE s.isActive = true " +
            "AND s.currentPrice IS NOT NULL AND s.marketCap IS NOT NULL")
    Object[] getMarketOverviewStats();

    /**
     * Find securities that haven't been updated recently (stale data detection)
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true " +
            "AND s.updatedDate < :threshold " +
            "ORDER BY s.updatedDate ASC")
    List<SecurityStock> findStaleSecurities(@Param("threshold") Instant threshold, Pageable pageable);

    /**
     * Get random sample of securities (for testing or display purposes)
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true " +
            "ORDER BY RANDOM()")
    List<SecurityStock> findRandomSecurities(Pageable pageable);

    /**
     * Count securities created today

    @Query("SELECT COUNT(s) FROM SecurityStock s WHERE s.isActive = true " +
            "AND DATE(s.createdDate) = CURRENT_DATE")
    long countSecuritiesCreatedToday(); */

    /**
     * Count securities updated today

    @Query("SELECT COUNT(s) FROM SecurityStock s WHERE s.isActive = true " +
            "AND DATE(s.updatedDate) = CURRENT_DATE")
    long countSecuritiesUpdatedToday(); */

    /**
     * Find securities with the highest trading volume (if you have volume data)
     */
    @Query("SELECT DISTINCT s FROM SecurityStock s " +
            "JOIN s.marketData md " +
            "WHERE s.isActive = true " +
            "AND md.marketDate = CURRENT_DATE " +
            "ORDER BY md.volume DESC")
    List<SecurityStock> findHighestVolumeSecurities(Pageable pageable);

    /**
     * Find securities missing company names (data quality)
     */
    @Query("SELECT s FROM SecurityStock s WHERE s.isActive = true " +
            "AND (s.companyName IS NULL OR s.companyName = '' OR s.companyName = s.symbol)")
    List<SecurityStock> findSecuritiesMissingCompanyNames();

    /**
     * Find duplicate securities (same symbol, multiple entries)
     */
    @Query("SELECT s.symbol, COUNT(s) FROM SecurityStock s " +
            "GROUP BY s.symbol HAVING COUNT(s) > 1")
    List<Object[]> findDuplicateSecurities();

    /**
     * Performance query: Get securities with basic info only (for large lists)
     */
    @Query("SELECT NEW com.example.MicroInvestApp.dto.SecuritySummaryDTO(" +
            "s.symbol, s.companyName, s.currentPrice, s.sector, s.exchange) " +
            "FROM SecurityStock s WHERE s.isActive = true " +
            "ORDER BY s.marketCap DESC")
    List<Object> findSecuritiesSummary(Pageable pageable);

    // ========== NATIVE QUERIES FOR COMPLEX OPERATIONS ==========

    /**
     * Complex analytics query using native SQL
     */
    @Query(value = "SELECT " +
            "s.sector, " +
            "COUNT(*) as count, " +
            "AVG(s.current_price) as avg_price, " +
            "SUM(s.market_cap) as total_market_cap, " +
            "STDDEV(s.current_price) as price_volatility " +
            "FROM security_stock s " +
            "WHERE s.is_active = true " +
            "AND s.current_price IS NOT NULL " +
            "GROUP BY s.sector " +
            "ORDER BY total_market_cap DESC",
            nativeQuery = true)
    List<Object[]> getSectorAnalytics();

    /**
     * Performance monitoring query
     */
    @Query(value = "SELECT " +
            "DATE(s.updated_date) as update_date, " +
            "COUNT(*) as updates_count, " +
            "COUNT(DISTINCT s.symbol) as unique_symbols " +
            "FROM security_stock s " +
            "WHERE s.is_active = true " +
            "AND s.updated_date >= CURRENT_DATE - INTERVAL '7 days' " +
            "GROUP BY DATE(s.updated_date) " +
            "ORDER BY update_date DESC",
            nativeQuery = true)
    List<Object[]> getUpdateActivity();

    // ========== CUSTOM FINDER METHODS ==========

    /**
     * Spring Data JPA automatically implements these based on method names
     */

    // Find by exact symbol match
    Optional<SecurityStock> findBySymbolIgnoreCase(String symbol);

    // Find by company name containing text
    List<SecurityStock> findByCompanyNameContainingIgnoreCase(String companyName);

    // Find by sector and active status
    List<SecurityStock> findBySectorAndIsActive(SecuritySector sector, boolean isActive);

    // Find by exchange and active status
    List<SecurityStock> findByExchangeAndIsActive(Exchange exchange, boolean isActive);

    // Find by creation date after
    List<SecurityStock> findByCreatedDateAfter(Instant date);

    // Find by update date after
    List<SecurityStock> findByUpdatedDateAfter(Instant date);

    // Find by current price greater than
    List<SecurityStock> findByCurrentPriceGreaterThan(BigDecimal price);

    // Find by current price less than
    List<SecurityStock> findByCurrentPriceLessThan(BigDecimal price);

    // Find by market cap greater than
    List<SecurityStock> findByMarketCapGreaterThan(BigDecimal marketCap);

    // Check if symbol exists
    boolean existsBySymbol(String symbol);

    // Check if active symbol exists
    boolean existsBySymbolAndIsActive(String symbol, boolean isActive);
}