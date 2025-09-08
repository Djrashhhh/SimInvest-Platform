package com.example.MicroInvestApp.repositories.portfolio;

import com.example.MicroInvestApp.domain.portfolio.Dividend;
import com.example.MicroInvestApp.domain.enums.DividendType;
import com.example.MicroInvestApp.domain.enums.DividendFrequency;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Dividend entity operations
 * Provides CRUD operations and custom queries for dividend management
 */
@Repository
public interface DividendRepository extends JpaRepository<Dividend, Long>{
    List<Dividend> findBySecurityStockSecurityIdOrderByExDividendDateDesc(Long securityId);

    Page<Dividend> findBySecurityStockSecurityId(Long securityId, Pageable pageable);

    List<Dividend> findBySecurityStockSecurityIdAndExDividendDateBetween(Long securityId, LocalDate startDate, LocalDate endDate);

    List<Dividend> findByDividendFrequency(DividendFrequency frequency);

    List<Dividend> findByDividendType(DividendType dividendType);

    List<Dividend> findByExDividendDateAfterOrderByExDividendDateAsc(LocalDate date);

    //Find recent dividends (payment date after cutoff date)
    @Query("SELECT d FROM Dividend d WHERE d.paymentDate >= :cutoffDate ORDER BY d.paymentDate DESC")
    List<Dividend> findRecentDividends(@Param("cutoffDate") LocalDate cutoffDate);

    @Query("SELECT SUM(d.amountPerShare) FROM Dividend d WHERE d.securityStock.securityId = :securityId")
    BigDecimal sumDividendAmountBySecurityId(@Param("securityId") Long securityId);

    @Query("SELECT SUM(d.amountPerShare) FROM Dividend d WHERE d.securityStock.securityId = :securityId AND d.exDividendDate BETWEEN :startDate AND :endDate")
    BigDecimal sumDividendAmountBySecurityIdAndDateRange(@Param("securityId") Long securityId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}