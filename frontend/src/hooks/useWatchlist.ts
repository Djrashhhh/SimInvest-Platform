// hooks/useWatchlists.ts - COMPLETE FIXED VERSION

import { useState, useCallback, useEffect } from "react";
import { useAuth } from "../contexts/AuthContext";
import { watchlistService } from "../services/watchlistService";
import type {
  WatchlistResponse,
  WatchlistSummary,
  WatchlistStats,
  CreateWatchlistRequest,
  UpdateWatchlistRequest,
  UseWatchlistsReturn,
  SecuritySearchResult,
  WatchlistSearchParams,
} from "../types/watchlist";

export const useWatchlists = (): UseWatchlistsReturn => {
  const { user, isAuthenticated, isLoading: authLoading } = useAuth();

  // DEBUG: Add logging to see what we're getting
  console.log("=== WATCHLIST HOOK DEBUG ===");
  console.log("Auth user:", user);
  console.log("User ID:", user?.user_id); // FIXED: Changed from userId to user_id
  console.log("User ID type:", typeof user?.user_id);
  console.log("Is authenticated:", isAuthenticated);
  console.log("Auth loading:", authLoading);
  console.log("========================");

  // State management
  const [watchlists, setWatchlists] = useState<WatchlistSummary[]>([]);
  const [selectedWatchlist, setSelectedWatchlist] =
    useState<WatchlistResponse | null>(null);
  const [watchlistStats, setWatchlistStats] = useState<WatchlistStats | null>(
    null
  );
  const [error, setError] = useState<string | null>(null);

  // Loading states
  const [loading, setLoading] = useState({
    watchlists: false,
    selectedWatchlist: false,
    create: false,
    update: false,
    delete: false,
    addSecurity: false,
    removeSecurity: false,
  });

  // Helper function to update loading state
  const setLoadingState = useCallback(
    (key: keyof typeof loading, value: boolean) => {
      setLoading((prev) => ({ ...prev, [key]: value }));
    },
    []
  );

  // Helper function to handle errors
  const handleError = useCallback((error: unknown, context?: string) => {
    console.error("Watchlist error context:", context);
    console.error("Watchlist error details:", error);
    const errorMessage = watchlistService.handleApiError(error);
    const fullMessage = context ? `${context}: ${errorMessage}` : errorMessage;
    setError(fullMessage);
  }, []);

  // Load all watchlists for the current user
  const loadWatchlists = useCallback(async (): Promise<void> => {
    console.log("=== LOAD WATCHLISTS DEBUG ===");
    console.log("Auth loading:", authLoading);
    console.log("Is authenticated:", isAuthenticated);
    console.log("User object:", user);
    console.log("User ID from user object:", user?.user_id); // FIXED

    if (authLoading) {
      console.log("Still loading auth, skipping watchlist load");
      return;
    }

    if (!isAuthenticated) {
      console.log("User not authenticated, clearing watchlists");
      setError("User not authenticated");
      return;
    }

    if (!user) {
      console.log("No user object available");
      setError("User data not available");
      return;
    }

    if (!user.user_id) {
      // FIXED: Changed from userId to user_id
      console.log("No user_id in user object:", user);
      setError("User ID not available");
      return;
    }

    console.log("Proceeding to load watchlists for user:", user.user_id); // FIXED

    setLoadingState("watchlists", true);
    setError(null);

    try {
      // Try to get cached data first
      const cached = watchlistService.getCachedWatchlists(user.user_id); // FIXED
      if (cached) {
        console.log("Using cached watchlists:", cached);
        setWatchlists(cached);
      }

      // Always fetch fresh data
      console.log("Fetching fresh watchlists for user:", user.user_id); // FIXED
      const watchlistsData = await watchlistService.getUserWatchlists(
        user.user_id
      ); // FIXED
      console.log("Fresh watchlists received:", watchlistsData);
      setWatchlists(watchlistsData);

      // Cache the fresh data
      watchlistService.cacheWatchlists(user.user_id, watchlistsData); // FIXED
    } catch (error) {
      console.error("Failed to load watchlists:", error);
      handleError(error, "Failed to load watchlists");
    } finally {
      setLoadingState("watchlists", false);
    }
  }, [
    user?.user_id,
    isAuthenticated,
    authLoading,
    setLoadingState,
    handleError,
  ]); // FIXED

  // Load detailed information for a specific watchlist
  const loadWatchlistDetails = useCallback(
    async (watchlistId: number): Promise<void> => {
      if (!user?.user_id) {
        // FIXED
        console.log("Cannot load watchlist details: no user ID");
        setError("User not authenticated");
        return;
      }

      setLoadingState("selectedWatchlist", true);
      setError(null);

      try {
        console.log(
          "Loading watchlist details for:",
          watchlistId,
          "user:",
          user.user_id
        ); // FIXED
        const watchlistData = await watchlistService.getWatchlistById(
          watchlistId,
          user.user_id
        ); // FIXED
        console.log("Watchlist details loaded:", watchlistData);
        setSelectedWatchlist(watchlistData);
      } catch (error) {
        console.error("Failed to load watchlist details:", error);
        handleError(error, "Failed to load watchlist details");
      } finally {
        setLoadingState("selectedWatchlist", false);
      }
    },
    [user?.user_id, setLoadingState, handleError]
  ); // FIXED

  // Create a new watchlist
  const createWatchlist = useCallback(
    async (request: CreateWatchlistRequest): Promise<WatchlistResponse> => {
      if (!user?.user_id) {
        // FIXED
        throw new Error("User not authenticated");
      }

      setLoadingState("create", true);
      setError(null);

      try {
        console.log(
          "Creating watchlist for user:",
          user.user_id,
          "request:",
          request
        ); // FIXED
        const newWatchlist = await watchlistService.createWatchlist(
          user.user_id,
          request
        ); // FIXED
        console.log("Watchlist created:", newWatchlist);

        // Update local state
        const newSummary: WatchlistSummary = {
          watchlist_id: newWatchlist.watchlist_id,
          name: newWatchlist.name,
          description: newWatchlist.description,
          security_count: newWatchlist.security_count,
          updated_at: newWatchlist.updated_at,
        };

        setWatchlists((prev) => [newSummary, ...prev]);
        setSelectedWatchlist(newWatchlist);

        // Clear cache to force refresh on next load
        watchlistService.clearCache(user.user_id); // FIXED

        return newWatchlist;
      } catch (error) {
        console.error("Failed to create watchlist:", error);
        handleError(error, "Failed to create watchlist");
        throw error;
      } finally {
        setLoadingState("create", false);
      }
    },
    [user?.user_id, setLoadingState, handleError]
  ); // FIXED

  // Update an existing watchlist
  const updateWatchlist = useCallback(
    async (
      watchlistId: number,
      request: UpdateWatchlistRequest
    ): Promise<WatchlistResponse> => {
      if (!user?.user_id) {
        // FIXED
        throw new Error("User not authenticated");
      }

      setLoadingState("update", true);
      setError(null);

      try {
        const updatedWatchlist = await watchlistService.updateWatchlist(
          watchlistId,
          user.user_id,
          request
        ); // FIXED

        // Update local state
        const updatedSummary: WatchlistSummary = {
          watchlist_id: updatedWatchlist.watchlist_id,
          name: updatedWatchlist.name,
          description: updatedWatchlist.description,
          security_count: updatedWatchlist.security_count,
          updated_at: updatedWatchlist.updated_at,
        };

        setWatchlists((prev) =>
          prev.map((w) => (w.watchlist_id === watchlistId ? updatedSummary : w))
        );

        if (selectedWatchlist?.watchlist_id === watchlistId) {
          setSelectedWatchlist(updatedWatchlist);
        }

        // Clear cache
        watchlistService.clearCache(user.user_id); // FIXED

        return updatedWatchlist;
      } catch (error) {
        handleError(error, "Failed to update watchlist");
        throw error;
      } finally {
        setLoadingState("update", false);
      }
    },
    [user?.user_id, selectedWatchlist, setLoadingState, handleError]
  ); // FIXED

  // Delete a watchlist
  const deleteWatchlist = useCallback(
    async (watchlistId: number): Promise<void> => {
      if (!user?.user_id) {
        // FIXED
        throw new Error("User not authenticated");
      }

      setLoadingState("delete", true);
      setError(null);

      try {
        await watchlistService.deleteWatchlist(watchlistId, user.user_id); // FIXED

        // Update local state
        setWatchlists((prev) =>
          prev.filter((w) => w.watchlist_id !== watchlistId)
        );

        if (selectedWatchlist?.watchlist_id === watchlistId) {
          const remainingWatchlists = watchlists.filter(
            (w) => w.watchlist_id !== watchlistId
          );
          if (remainingWatchlists.length > 0) {
            await loadWatchlistDetails(remainingWatchlists[0].watchlist_id);
          } else {
            setSelectedWatchlist(null);
          }
        }

        // Clear cache
        watchlistService.clearCache(user.user_id); // FIXED
      } catch (error) {
        handleError(error, "Failed to delete watchlist");
        throw error;
      } finally {
        setLoadingState("delete", false);
      }
    },
    [
      user?.user_id,
      selectedWatchlist,
      watchlists,
      loadWatchlistDetails,
      setLoadingState,
      handleError,
    ]
  ); // FIXED

  // Add a security to a watchlist
  const addSecurityToWatchlist = useCallback(
    async (watchlistId: number, symbol: string): Promise<WatchlistResponse> => {
      if (!user?.user_id) {
        // FIXED
        throw new Error("User not authenticated");
      }

      setLoadingState("addSecurity", true);
      setError(null);

      try {
        const updatedWatchlist = await watchlistService.addSecurityToWatchlist(
          watchlistId,
          user.user_id, // FIXED
          symbol
        );

        // Update local state
        setWatchlists((prev) =>
          prev.map((w) =>
            w.watchlist_id === watchlistId
              ? {
                  ...w,
                  security_count: updatedWatchlist.security_count,
                  updated_at: updatedWatchlist.updated_at,
                }
              : w
          )
        );

        if (selectedWatchlist?.watchlist_id === watchlistId) {
          setSelectedWatchlist(updatedWatchlist);
        }

        // Clear cache
        watchlistService.clearCache(user.user_id); // FIXED

        return updatedWatchlist;
      } catch (error) {
        handleError(error, `Failed to add ${symbol} to watchlist`);
        throw error;
      } finally {
        setLoadingState("addSecurity", false);
      }
    },
    [user?.user_id, selectedWatchlist, setLoadingState, handleError]
  ); // FIXED

  // Remove a security from a watchlist
  const removeSecurityFromWatchlist = useCallback(
    async (watchlistId: number, symbol: string): Promise<WatchlistResponse> => {
      if (!user?.user_id) {
        // FIXED
        throw new Error("User not authenticated");
      }

      setLoadingState("removeSecurity", true);
      setError(null);

      try {
        const updatedWatchlist =
          await watchlistService.removeSecurityFromWatchlist(
            watchlistId,
            user.user_id, // FIXED
            symbol
          );

        // Update local state
        setWatchlists((prev) =>
          prev.map((w) =>
            w.watchlist_id === watchlistId
              ? {
                  ...w,
                  security_count: updatedWatchlist.security_count,
                  updated_at: updatedWatchlist.updated_at,
                }
              : w
          )
        );

        if (selectedWatchlist?.watchlist_id === watchlistId) {
          setSelectedWatchlist(updatedWatchlist);
        }

        // Clear cache
        watchlistService.clearCache(user.user_id); // FIXED

        return updatedWatchlist;
      } catch (error) {
        handleError(error, `Failed to remove ${symbol} from watchlist`);
        throw error;
      } finally {
        setLoadingState("removeSecurity", false);
      }
    },
    [user?.user_id, selectedWatchlist, setLoadingState, handleError]
  ); // FIXED

  // Clear all securities from a watchlist
  const clearWatchlist = useCallback(
    async (watchlistId: number): Promise<WatchlistResponse> => {
      if (!user?.user_id) {
        // FIXED
        throw new Error("User not authenticated");
      }

      setLoadingState("removeSecurity", true);
      setError(null);

      try {
        const updatedWatchlist = await watchlistService.clearWatchlist(
          watchlistId,
          user.user_id
        ); // FIXED

        // Update local state
        setWatchlists((prev) =>
          prev.map((w) =>
            w.watchlist_id === watchlistId
              ? {
                  ...w,
                  security_count: 0,
                  updated_at: updatedWatchlist.updated_at,
                }
              : w
          )
        );

        if (selectedWatchlist?.watchlist_id === watchlistId) {
          setSelectedWatchlist(updatedWatchlist);
        }

        // Clear cache
        watchlistService.clearCache(user.user_id); // FIXED

        return updatedWatchlist;
      } catch (error) {
        handleError(error, "Failed to clear watchlist");
        throw error;
      } finally {
        setLoadingState("removeSecurity", false);
      }
    },
    [user?.user_id, selectedWatchlist, setLoadingState, handleError]
  ); // FIXED

  // Load watchlist statistics
  const loadWatchlistStats = useCallback(async (): Promise<void> => {
    if (!user?.user_id) {
      // FIXED
      console.log("Cannot load watchlist stats: no user ID");
      return;
    }

    try {
      console.log("Loading watchlist stats for user:", user.user_id); // FIXED
      const stats = await watchlistService.getWatchlistStats(user.user_id); // FIXED
      console.log("Watchlist stats loaded:", stats);
      setWatchlistStats(stats);
    } catch (error) {
      console.warn("Failed to load watchlist stats:", error);
      // Don't set error state for stats since it's not critical
    }
  }, [user?.user_id]); // FIXED

  // Refresh all data
  const refreshData = useCallback(async (): Promise<void> => {
    console.log("Refreshing all watchlist data");
    await Promise.all([loadWatchlists(), loadWatchlistStats()]);

    if (selectedWatchlist) {
      await loadWatchlistDetails(selectedWatchlist.watchlist_id);
    }
  }, [
    loadWatchlists,
    loadWatchlistStats,
    selectedWatchlist,
    loadWatchlistDetails,
  ]);

  // Clear error state
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  // FIXED: Load data when authentication is complete and user is available
  useEffect(() => {
    console.log("=== WATCHLIST EFFECT TRIGGERED ===");
    console.log("Auth loading:", authLoading);
    console.log("Is authenticated:", isAuthenticated);
    console.log("User:", user);
    console.log("User ID:", user?.user_id); // FIXED

    // Only run when auth is not loading
    if (!authLoading) {
      if (isAuthenticated && user?.user_id) {
        // FIXED
        console.log("Loading watchlists and stats for authenticated user");
        loadWatchlists();
        loadWatchlistStats();
      } else {
        console.log("User not authenticated or no user ID, clearing state");
        // Clear state when user logs out or is not authenticated
        setWatchlists([]);
        setSelectedWatchlist(null);
        setWatchlistStats(null);
        setError(null);
      }
    }
  }, [
    authLoading,
    isAuthenticated,
    user?.user_id,
    loadWatchlists,
    loadWatchlistStats,
  ]); // FIXED

  // Return the hook interface
  return {
    // State
    watchlists,
    selectedWatchlist,
    watchlistStats,
    loading,
    error,

    // Actions
    loadWatchlists,
    loadWatchlistDetails,
    createWatchlist,
    updateWatchlist,
    deleteWatchlist,
    addSecurityToWatchlist,
    removeSecurityFromWatchlist,
    clearWatchlist,
    refreshData,
    setSelectedWatchlist,
    clearError,
  };
};

// Additional specialized hooks remain the same
export const useSecuritySearch = () => {
  const [searchResults, setSearchResults] = useState<SecuritySearchResult[]>(
    []
  );
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState<string | null>(null);

  const searchSecurities = useCallback(
    async (params: WatchlistSearchParams) => {
      if (!params.query?.trim()) {
        setSearchResults([]);
        return;
      }

      setSearchLoading(true);
      setSearchError(null);

      try {
        const results = await watchlistService.searchSecurities(params);
        setSearchResults(results);
      } catch (error) {
        const errorMessage = watchlistService.handleApiError(error);
        setSearchError(errorMessage);
        setSearchResults([]);
      } finally {
        setSearchLoading(false);
      }
    },
    []
  );

  const clearSearch = useCallback(() => {
    setSearchResults([]);
    setSearchError(null);
  }, []);

  return {
    searchResults,
    searchLoading,
    searchError,
    searchSecurities,
    clearSearch,
  };
};

export const useWatchlistValidation = () => {
  const validateWatchlistName = useCallback((name: string): string | null => {
    if (!name.trim()) {
      return "Watchlist name is required";
    }
    if (name.length > 100) {
      return "Watchlist name must be 100 characters or less";
    }
    return null;
  }, []);

  const validateWatchlistDescription = useCallback(
    (description: string): string | null => {
      if (description.length > 500) {
        return "Description must be 500 characters or less";
      }
      return null;
    },
    []
  );

  const validateCreateRequest = useCallback(
    (request: CreateWatchlistRequest): Record<string, string> => {
      const errors: Record<string, string> = {};

      const nameError = validateWatchlistName(request.name);
      if (nameError) errors.name = nameError;

      if (request.description) {
        const descriptionError = validateWatchlistDescription(
          request.description
        );
        if (descriptionError) errors.description = descriptionError;
      }

      return errors;
    },
    [validateWatchlistName, validateWatchlistDescription]
  );

  return {
    validateWatchlistName,
    validateWatchlistDescription,
    validateCreateRequest,
  };
};

export const useWatchlistUtils = () => {
  const { user } = useAuth();

  const isSecurityInWatchlist = useCallback(
    (watchlist: WatchlistResponse | null, symbol: string): boolean => {
      if (!watchlist?.securities) return false;
      return watchlist.securities.some(
        (security) => security.symbol.toUpperCase() === symbol.toUpperCase()
      );
    },
    []
  );

  const getWatchlistByName = useCallback(
    (watchlists: WatchlistSummary[], name: string): WatchlistSummary | null => {
      return (
        watchlists.find((w) => w.name.toLowerCase() === name.toLowerCase()) ||
        null
      );
    },
    []
  );

  const sortWatchlistsByName = useCallback(
    (watchlists: WatchlistSummary[]): WatchlistSummary[] => {
      return [...watchlists].sort((a, b) => a.name.localeCompare(b.name));
    },
    []
  );

  const sortWatchlistsByUpdated = useCallback(
    (watchlists: WatchlistSummary[]): WatchlistSummary[] => {
      return [...watchlists].sort(
        (a, b) =>
          new Date(b.updated_at).getTime() - new Date(a.updated_at).getTime()
      );
    },
    []
  );

  const sortWatchlistsBySecurityCount = useCallback(
    (watchlists: WatchlistSummary[]): WatchlistSummary[] => {
      return [...watchlists].sort(
        (a, b) => b.security_count - a.security_count
      );
    },
    []
  );

  const formatLastUpdated = useCallback((dateString: string): string => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);

    if (diffMins < 1) return "Just now";
    if (diffMins === 1) return "1 minute ago";
    if (diffMins < 60) return `${diffMins} minutes ago`;

    const diffHours = Math.floor(diffMins / 60);
    if (diffHours === 1) return "1 hour ago";
    if (diffHours < 24) return `${diffHours} hours ago`;

    const diffDays = Math.floor(diffHours / 24);
    if (diffDays === 1) return "1 day ago";
    if (diffDays < 7) return `${diffDays} days ago`;

    return date.toLocaleDateString();
  }, []);

  const formatCurrency = useCallback((amount: number | null): string => {
    if (amount == null || Number.isNaN(amount)) return "$0.00";
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount);
  }, []);

  const calculateTotalValue = useCallback(
    (watchlist: WatchlistResponse | null): number => {
      if (!watchlist?.securities) return 0;
      return watchlist.securities.reduce((total, security) => {
        return total + (security.current_price || 0);
      }, 0);
    },
    []
  );

  const getUniqueSymbols = useCallback(
    (watchlists: WatchlistResponse[]): string[] => {
      const symbolSet = new Set<string>();
      watchlists.forEach((watchlist) => {
        watchlist.securities?.forEach((security) => {
          symbolSet.add(security.symbol);
        });
      });
      return Array.from(symbolSet).sort();
    },
    []
  );

  // FIXED: Check if user can perform actions using user_id
  const canModifyWatchlist = useCallback(
    (watchlist: WatchlistResponse | null): boolean => {
      return !!(user?.user_id && watchlist?.user_id === user.user_id); // FIXED
    },
    [user?.user_id]
  ); // FIXED

  return {
    isSecurityInWatchlist,
    getWatchlistByName,
    sortWatchlistsByName,
    sortWatchlistsByUpdated,
    sortWatchlistsBySecurityCount,
    formatLastUpdated,
    formatCurrency,
    calculateTotalValue,
    getUniqueSymbols,
    canModifyWatchlist,
  };
};

export default useWatchlists;
