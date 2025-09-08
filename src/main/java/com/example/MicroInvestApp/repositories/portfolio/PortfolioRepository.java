package com.example.MicroInvestApp.repositories.portfolio;

import com.example.MicroInvestApp.domain.portfolio.Portfolio;
import com.example.MicroInvestApp.domain.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Portfolio entity operations
 * Provides CRUD operations and custom queries for portfolio management
 */
@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    /**
     * Find portfolio by user account
     * @param userAccount the user account
     * @return Optional portfolio
     */
    Optional<Portfolio> findByUserAccount(UserAccount userAccount);

    /**
     * Find portfolio by user ID
     * @param userId the user ID
     * @return Optional portfolio
     */
    @Query("SELECT p FROM Portfolio p JOIN FETCH p.userAccount WHERE p.userAccount.userId = :userId")
    Optional<Portfolio> findByUserIdWithUser(@Param("userId") Long userId);

    /**
     * Find active portfolio by user ID
     * @param userId the user ID
     * @return Optional portfolio
     */
    @Query("SELECT p FROM Portfolio p JOIN FETCH p.userAccount WHERE p.userAccount.userId = :userId AND p.isActive = true")
    Optional<Portfolio> findActiveByUserIdWithUser(@Param("userId") Long userId);

    /**
     * Find all active portfolios
     * @return List of active portfolios
     */
    @Query("SELECT p FROM Portfolio p WHERE p.isActive = true")
    List<Portfolio> findAllActive();

    /**
     * Find portfolios by total value range
     * @param minValue minimum total value
     * @param maxValue maximum total value
     * @return List of portfolios
     */
    @Query("SELECT p FROM Portfolio p WHERE p.totalValue BETWEEN :minValue AND :maxValue")
    List<Portfolio> findByTotalValueBetween(@Param("minValue") BigDecimal minValue,
                                            @Param("maxValue") BigDecimal maxValue);

    /**
     * Find portfolios created after a specific date
     * @param date the date
     * @return List of portfolios
     */
    @Query("SELECT p FROM Portfolio p WHERE p.createdDate >= :date")
    List<Portfolio> findCreatedAfter(@Param("date") Instant date);

    /**
     * Find portfolios with cash balance greater than specified amount
     * @param amount the minimum cash balance
     * @return List of portfolios
     */
    @Query("SELECT p FROM Portfolio p WHERE p.cashBalance >= :amount")
    List<Portfolio> findWithCashBalanceGreaterThan(@Param("amount") BigDecimal amount);

    /**
     * Get total count of active portfolios
     * @return count of active portfolios
     */
    @Query("SELECT COUNT(p) FROM Portfolio p WHERE p.isActive = true")
    Long countActivePortfolios();

    /**
     * Get sum of all portfolio values
     * @return total sum of portfolio values
     */
    @Query("SELECT SUM(p.totalValue) FROM Portfolio p WHERE p.isActive = true")
    BigDecimal getTotalPortfolioValue();

    /**
     * Find portfolios by name containing (case insensitive)
     * @param name the name pattern
     * @return List of portfolios
     */
    @Query("SELECT p FROM Portfolio p WHERE LOWER(p.portfolioName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Portfolio> findByPortfolioNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Check if user has an active portfolio
     * @param userId the user ID
     * @return true if user has active portfolio
     */
    @Query("SELECT COUNT(p) > 0 FROM Portfolio p WHERE p.userAccount.userId = :userId AND p.isActive = true")
    boolean hasActivePortfolio(@Param("userId") Long userId);

    /**
     * Update portfolio status
     * @param portfolioId the portfolio ID
     * @param isActive the new status
     * @return number of updated records
     */
    @Query("UPDATE Portfolio p SET p.isActive = :isActive WHERE p.portfolioId = :portfolioId")
    int updatePortfolioStatus(@Param("portfolioId") Long portfolioId, @Param("isActive") boolean isActive);

}