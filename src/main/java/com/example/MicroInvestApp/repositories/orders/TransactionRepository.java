package com.example.MicroInvestApp.repositories.orders;

import com.example.MicroInvestApp.domain.enums.TransactionStatus;
import com.example.MicroInvestApp.domain.enums.TransactionType;
import com.example.MicroInvestApp.domain.market.SecurityStock;
import com.example.MicroInvestApp.domain.orders.Order;
import com.example.MicroInvestApp.domain.orders.Transaction;
import com.example.MicroInvestApp.domain.portfolio.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find transactions by portfolio
    List<Transaction> findByPortfolioOrderByTransactionDateDesc(Portfolio portfolio);

    // Find transactions by portfolio with pagination
    Page<Transaction> findByPortfolioOrderByTransactionDateDesc(Portfolio portfolio, Pageable pageable);

    // Find transactions by order
    List<Transaction> findByOrderOrderByTransactionDateDesc(Order order);

    // Find transactions by status
    List<Transaction> findByTransactionStatusOrderByTransactionDateDesc(TransactionStatus status);

    // Find transactions by type
    List<Transaction> findByTransactionTypeOrderByTransactionDateDesc(TransactionType type);

    // Find transactions by portfolio and type
    List<Transaction> findByPortfolioAndTransactionTypeOrderByTransactionDateDesc(Portfolio portfolio, TransactionType type);

    // Find transactions by portfolio and status
    List<Transaction> findByPortfolioAndTransactionStatusOrderByTransactionDateDesc(Portfolio portfolio, TransactionStatus status);

    // Find transactions by security
    List<Transaction> findBySecurityStockOrderByTransactionDateDesc(SecurityStock securityStock);

    // Find transactions by portfolio and security
    List<Transaction> findByPortfolioAndSecurityStockOrderByTransactionDateDesc(Portfolio portfolio, SecurityStock securityStock);

    // Find transactions within date range
    @Query("SELECT t FROM Transaction t WHERE t.transactionDate >= :startDate AND t.transactionDate <= :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findTransactionsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Find transactions by portfolio within date range
    @Query("SELECT t FROM Transaction t WHERE t.portfolio = :portfolio AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findTransactionsByPortfolioAndDateRange(@Param("portfolio") Portfolio portfolio,
                                                              @Param("startDate") LocalDateTime startDate,
                                                              @Param("endDate") LocalDateTime endDate);

    // Find buy transactions for a security in a portfolio (for cost basis calculation)
    @Query("SELECT t FROM Transaction t WHERE t.portfolio = :portfolio AND t.securityStock = :security AND t.transactionType = 'BUY' AND t.transactionStatus = 'COMPLETED' ORDER BY t.transactionDate ASC")
    List<Transaction> findBuyTransactionsForSecurity(@Param("portfolio") Portfolio portfolio, @Param("security") SecurityStock security);

    // Find sell transactions for a security in a portfolio
    @Query("SELECT t FROM Transaction t WHERE t.portfolio = :portfolio AND t.securityStock = :security AND t.transactionType = 'SELL' AND t.transactionStatus = 'COMPLETED' ORDER BY t.transactionDate ASC")
    List<Transaction> findSellTransactionsForSecurity(@Param("portfolio") Portfolio portfolio, @Param("security") SecurityStock security);

    // Calculate total quantity bought for a security in a portfolio
    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM Transaction t WHERE t.portfolio = :portfolio AND t.securityStock = :security AND t.transactionType = 'BUY' AND t.transactionStatus = 'COMPLETED'")
    BigDecimal getTotalQuantityBought(@Param("portfolio") Portfolio portfolio, @Param("security") SecurityStock security);

    // Calculate total quantity sold for a security in a portfolio
    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM Transaction t WHERE t.portfolio = :portfolio AND t.securityStock = :security AND t.transactionType = 'SELL' AND t.transactionStatus = 'COMPLETED'")
    BigDecimal getTotalQuantitySold(@Param("portfolio") Portfolio portfolio, @Param("security") SecurityStock security);

    // Calculate average cost basis for a security
    @Query("SELECT COALESCE(SUM(t.totalAmount) / SUM(t.quantity), 0) FROM Transaction t WHERE t.portfolio = :portfolio AND t.securityStock = :security AND t.transactionType = 'BUY' AND t.transactionStatus = 'COMPLETED'")
    BigDecimal getAverageCostBasis(@Param("portfolio") Portfolio portfolio, @Param("security") SecurityStock security);

    // Find unsettled transactions
    @Query("SELECT t FROM Transaction t WHERE t.settlementDate <= :currentDate AND t.transactionStatus = 'PENDING'")
    List<Transaction> findUnsettledTransactions(@Param("currentDate") LocalDateTime currentDate);

    // Calculate total dividend income for portfolio
    @Query("SELECT COALESCE(SUM(t.netAmount), 0) FROM Transaction t WHERE t.portfolio = :portfolio AND t.transactionType = 'DIVIDEND' AND t.transactionStatus = 'COMPLETED'")
    BigDecimal getTotalDividendIncome(@Param("portfolio") Portfolio portfolio);

    // Calculate total fees paid by portfolio
    @Query("SELECT COALESCE(SUM(t.fees), 0) FROM Transaction t WHERE t.portfolio = :portfolio AND t.transactionStatus = 'COMPLETED'")
    BigDecimal getTotalFeesPaid(@Param("portfolio") Portfolio portfolio);

    // Get transaction statistics for portfolio
    @Query("SELECT " +
            "COUNT(t) as totalTransactions, " +
            "COUNT(CASE WHEN t.transactionType = 'BUY' THEN 1 END) as buyTransactions, " +
            "COUNT(CASE WHEN t.transactionType = 'SELL' THEN 1 END) as sellTransactions, " +
            "COUNT(CASE WHEN t.transactionType = 'DIVIDEND' THEN 1 END) as dividendTransactions, " +
            "COALESCE(SUM(CASE WHEN t.transactionType = 'BUY' THEN t.netAmount END), 0) as totalBought, " +
            "COALESCE(SUM(CASE WHEN t.transactionType = 'SELL' THEN t.netAmount END), 0) as totalSold " +
            "FROM Transaction t WHERE t.portfolio = :portfolio AND t.transactionStatus = 'COMPLETED'")
    Object[] getTransactionStatsByPortfolio(@Param("portfolio") Portfolio portfolio);

    // Find recent transactions for dashboard
    @Query("SELECT t FROM Transaction t WHERE t.portfolio = :portfolio ORDER BY t.transactionDate DESC")
    List<Transaction> findRecentTransactions(@Param("portfolio") Portfolio portfolio, Pageable pageable);
}
