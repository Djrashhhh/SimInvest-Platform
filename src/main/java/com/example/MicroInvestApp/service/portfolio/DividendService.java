package com.example.MicroInvestApp.service.portfolio;

import com.example.MicroInvestApp.domain.portfolio.Dividend;
import com.example.MicroInvestApp.domain.enums.DividendFrequency;
import com.example.MicroInvestApp.domain.enums.DividendType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DividendService {

    /**
     * Create a new dividend record
     */
    Dividend createDividend(Dividend dividend);

    /**
     * Get dividend by ID
     */
    Optional<Dividend> getDividendById(Long dividendId);

    /**
     * Get all dividends for a specific security
     */
    List<Dividend> getDividendsBySecurityId(Long securityId);

    /**
     * Get all dividends for a specific security with pagination
     */
    Page<Dividend> getDividendsBySecurityId(Long securityId, Pageable pageable);

    /**
     * Get dividends by security and date range
     */
    List<Dividend> getDividendsBySecurityAndDateRange(Long securityId, LocalDate startDate, LocalDate endDate);

    /**
     * Get dividends by frequency
     */
    List<Dividend> getDividendsByFrequency(DividendFrequency frequency);

    /**
     * Get dividends by type
     */
    List<Dividend> getDividendsByType(DividendType dividendType);

    /**
     * Get upcoming dividends (ex-dividend date in the future)
     */
    List<Dividend> getUpcomingDividends();

    /**
     * Get recent dividends (payment date within last N days)
     */
    List<Dividend> getRecentDividends(int days);

    /**
     * Calculate total dividend amount for a security
     */
    BigDecimal getTotalDividendAmountBySecurityId(Long securityId);

    /**
     * Calculate total dividend amount for a security within date range
     */
    BigDecimal getTotalDividendAmountBySecurityAndDateRange(Long securityId, LocalDate startDate, LocalDate endDate);

    /**
     * Update dividend information
     */
    Dividend updateDividend(Long dividendId, Dividend updatedDividend);

    /**
     * Delete dividend by ID
     */
    void deleteDividend(Long dividendId);

    /**
     * Check if dividend exists
     */
    boolean dividendExists(Long dividendId);

    /**
     * Get dividend yield for a security (annual dividends / current price)
     */
    BigDecimal getDividendYield(Long securityId);

    /**
     * Get all dividends with pagination
     */
    Page<Dividend> getAllDividends(Pageable pageable);
}