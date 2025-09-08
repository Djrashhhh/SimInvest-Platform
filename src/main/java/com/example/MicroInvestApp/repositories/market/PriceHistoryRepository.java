package com.example.MicroInvestApp.repositories.market;

import com.example.MicroInvestApp.domain.market.PriceHistory;
import com.example.MicroInvestApp.domain.market.SecurityStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    Optional<PriceHistory> findBySecurityStockAndDate(SecurityStock securityStock, LocalDate date);   // Find price history by security stock and date

    List<PriceHistory> findBySecurityStockOrderByDateDesc(SecurityStock securityStock);  // Find all price history for a security stock, ordered by date descending

    @Query("SELECT ph FROM PriceHistory ph WHERE ph.securityStock = :securityStock " +
            "AND ph.date BETWEEN :startDate AND :endDate ORDER BY ph.date ASC")
    List<PriceHistory> findBySecurityStockAndDateRange(
            @Param("securityStock") SecurityStock securityStock,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );    // Find price history for a security stock within a date range, ordered by date ascending

}
