package com.example.MicroInvestApp.repositories.portfolio;

import com.example.MicroInvestApp.domain.portfolio.Watchlist;
import com.example.MicroInvestApp.domain.user.UserAccount;
import com.example.MicroInvestApp.domain.market.SecurityStock;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for Watchlist entity operations
 * Provides CRUD operations and custom queries for watchlist management
 */
@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    /**
     * Find all watchlists for a specific user
     * @param userAccount the user account
     * @return List of watchlists
     */
    List<Watchlist> findByUserAccount(UserAccount userAccount);

    /**
     * Find all watchlists for a specific user by ID, ordered by most recently updated
     * @param userId the user ID
     * @return List of watchlists
     */
    @Query("SELECT w FROM Watchlist w WHERE w.userAccount.userId = :userId ORDER BY w.updatedAt DESC")
    List<Watchlist> findByUserId(@Param("userId") Long userId);

    /**
     * Find watchlist by name and user
     * @param name the watchlist name
     * @param userAccount the user account
     * @return Optional watchlist
     */
    Optional<Watchlist> findByNameAndUserAccount(String name, UserAccount userAccount);


    /**
     * Find watchlists containing a specific security
     * @param securityStock the security
     * @return List of watchlists
     */
    @Query("SELECT w FROM Watchlist w JOIN w.securities s WHERE s = :security")
    List<Watchlist> findBySecurityStock(@Param("security") SecurityStock securityStock);

    /**
     * Find user's watchlists containing a specific security
     * @param userId the user ID
     * @param securityId the security ID
     * @return List of watchlists
     */
    @Query("SELECT w FROM Watchlist w JOIN w.securities s " +
            "WHERE w.userAccount.userId = :userId AND s.securityId = :securityId")
    List<Watchlist> findByUserIdAndSecurityId(@Param("userId") Long userId,
                                              @Param("securityId") Long securityId);

    /**
     * Check if user has a watchlist with the given name
     * @param userId the user ID
     * @param name the watchlist name
     * @return true if watchlist exists
     */
    @Query("SELECT COUNT(w) > 0 FROM Watchlist w WHERE w.userAccount.userId = :userId " +
            "AND LOWER(w.name) = LOWER(:name)")
    boolean existsByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);

    /**
     * Find watchlists created after a specific date
     * @param date the date
     * @return List of watchlists
     */
    @Query("SELECT w FROM Watchlist w WHERE w.createdAt >= :date ORDER BY w.createdAt DESC")
    List<Watchlist> findCreatedAfter(@Param("date") Instant date);

    /**
     * Find watchlists updated after a specific date
     * @param date the date
     * @return List of watchlists
     */
    @Query("SELECT w FROM Watchlist w WHERE w.updatedAt >= :date ORDER BY w.updatedAt DESC")
    List<Watchlist> findUpdatedAfter(@Param("date") Instant date);

    /**
     * Count watchlists for a user
     * @param userId the user ID
     * @return count of watchlists
     */
    @Query("SELECT COUNT(w) FROM Watchlist w WHERE w.userAccount.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * Find most recent watchlists for a user with pagination
     * @param userId the user ID
     * @param pageable pagination information
     * @return List of recent watchlists
     */
    @Query("SELECT w FROM Watchlist w WHERE w.userAccount.userId = :userId " +
            "ORDER BY w.updatedAt DESC")
    List<Watchlist> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find watchlists by description containing (case insensitive)
     * @param userId the user ID
     * @param description the description pattern
     * @return List of watchlists
     */
    @Query("SELECT w FROM Watchlist w WHERE w.userAccount.userId = :userId " +
            "AND LOWER(w.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    List<Watchlist> findByUserIdAndDescriptionContainingIgnoreCase(@Param("userId") Long userId,
                                                                   @Param("description") String description);

    /**
     * Find all unique security IDs in user's watchlists
     * @param userId the user ID
     * @return Set of security IDs
     */
    @Query("SELECT DISTINCT s.securityId FROM Watchlist w JOIN w.securities s WHERE w.userAccount.userId = :userId")
    Set<Long> findSecurityIdsByUserId(@Param("userId") Long userId);

    /**
     * Find watchlist by ID and user ID (for ownership verification)
     * @param watchlistId the watchlist ID
     * @param userId the user ID
     * @return Optional watchlist
     */
    @Query("SELECT w FROM Watchlist w WHERE w.watchlistId = :watchlistId AND w.userAccount.userId = :userId")
    Optional<Watchlist> findByIdAndUserId(@Param("watchlistId") Long watchlistId, @Param("userId") Long userId);

    /**
     * Find the most recently updated watchlist for a user
     * @param userId the user ID
     * @return Optional watchlist
     */
    @Query("SELECT w FROM Watchlist w WHERE w.userAccount.userId = :userId " +
            "ORDER BY w.updatedAt DESC LIMIT 1")
    Optional<Watchlist> findMostRecentByUserId(@Param("userId") Long userId);

    /**
     * Count securities in a specific watchlist
     * @param watchlistId the watchlist ID
     * @return count of securities
     */
    @Query("SELECT COUNT(s) FROM Watchlist w JOIN w.securities s WHERE w.watchlistId = :watchlistId")
    Long countSecuritiesInWatchlist(@Param("watchlistId") Long watchlistId);

    /**
     * Bulk delete watchlists by user ID
     * @param userId the user ID
     */
    @Modifying
    @Query("DELETE FROM Watchlist w WHERE w.userAccount.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    /**
     * Find watchlists with no securities
     * @param userId the user ID
     * @return List of empty watchlists
     */
    @Query("SELECT w FROM Watchlist w WHERE w.userAccount.userId = :userId " +
            "AND SIZE(w.securities) = 0")
    List<Watchlist> findEmptyWatchlistsByUserId(@Param("userId") Long userId);
}