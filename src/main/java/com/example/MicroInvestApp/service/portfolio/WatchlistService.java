package com.example.MicroInvestApp.service.portfolio;

import com.example.MicroInvestApp.dto.portfolio.RequestDTOs.CreateWatchlistRequestDTO;
import com.example.MicroInvestApp.dto.portfolio.RequestDTOs.UpdateWatchlistRequestDTO;
import com.example.MicroInvestApp.dto.portfolio.ResponseDTOs.WatchlistResponseDTO;
import com.example.MicroInvestApp.dto.portfolio.ResponseDTOs.WatchlistStatsResponseDTO;
import com.example.MicroInvestApp.dto.portfolio.ResponseDTOs.WatchlistSummaryResponseDTO;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WatchlistService {

    /**
     * Create a new watchlist for a user
     */
    WatchlistResponseDTO createWatchlist(Long userId, CreateWatchlistRequestDTO createRequest);

    /**
     * Get watchlist by ID
     */
    Optional<WatchlistResponseDTO> getWatchlistById(Long watchlistId, Long userId);

    /**
     * Get all watchlists for a user
     */
    List<WatchlistSummaryResponseDTO> getUserWatchlists(Long userId);



    /**
     * Update watchlist information
     */
    WatchlistResponseDTO updateWatchlist(Long watchlistId, Long userId, UpdateWatchlistRequestDTO updateRequest);

    /**
     * Delete watchlist
     */
    void deleteWatchlist(Long watchlistId, Long userId);

    /**
     * Add securities to watchlist
     */
    WatchlistResponseDTO addSecuritiesToWatchlist(Long watchlistId, Long userId, Set<Long> securityIds);

    /**
     * Remove securities from watchlist
     */
    WatchlistResponseDTO removeSecuritiesFromWatchlist(Long watchlistId, Long userId, Set<Long> securityIds);

    /**
     * Remove all securities from watchlist
     */
    WatchlistResponseDTO clearWatchlist(Long watchlistId, Long userId);

    /**
     * Get watchlists containing a specific security for a user
     */
    List<WatchlistSummaryResponseDTO> getWatchlistsContainingSecurity(Long securityId, Long userId);

    /**
     * Check if user has watchlist with given name
     */
    boolean userHasWatchlistWithName(Long userId, String name);

    /**
     * Get watchlist count for user
     */
    Long getUserWatchlistCount(Long userId);

    /**
     * Check if watchlist exists
     */
    boolean watchlistExists(Long watchlistId);

    /**
     * Check if user owns watchlist
     */
    boolean isWatchlistOwner(Long watchlistId, Long userId);


    /**
     * Get user's default watchlist (first or most recently used)
     */
    Optional<WatchlistResponseDTO> getUserDefaultWatchlist(Long userId);

    /**
     * Get symbol list from watchlist
     */
    List<String> getWatchlistSymbols(Long watchlistId);

    /**
     * Add security by symbol (finds security ID internally)
     */
    WatchlistResponseDTO addSecurityBySymbol(Long watchlistId, Long userId, String symbol);

    /**
     * Remove security by symbol
     */
    WatchlistResponseDTO removeSecurityBySymbol(Long watchlistId, Long userId, String symbol);

    /**
     * Get watchlist statistics for user
     */
    WatchlistStatsResponseDTO getUserWatchlistStats(Long userId);

    /**
     * Get watchlists containing a specific security by symbol
     */
    List<WatchlistSummaryResponseDTO> getWatchlistsContainingSymbol(String symbol, Long userId);

    /**
     * Check if security symbol exists in any of user's watchlists
     */
    boolean isSymbolInUserWatchlists(Long userId, String symbol);

}