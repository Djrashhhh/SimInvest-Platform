// useMarketData.ts - Custom hook for market data management
import { useState, useEffect, useCallback, useRef } from "react";
import { SecuritySector, Exchange } from "../types/market";
import type {
  MarketStats,
  PriceAlertInfo,
  SectorOverview,
  SecurityStock,
  TrendingStock,
} from "../types/market";
import type { MarketFilter, UseMarketDataReturn } from "../types/market";
import type { MarketData, MarketError, SortOption } from "../types/market";
import type {
  MarketContext,
  FilterOptions,
  LoadingState,
} from "../types/market";
import { marketService } from "../services/marketService";

const REAL_TIME_UPDATE_INTERVAL = 30000; // 30 seconds
const TRENDING_UPDATE_INTERVAL = 60000; // 1 minute
const STATS_UPDATE_INTERVAL = 300000; // 5 minutes

export const useMarketData = (): UseMarketDataReturn => {
  // Core data state
  const [securities, setSecurities] = useState<SecurityStock[]>([]);
  const [filteredSecurities, setFilteredSecurities] = useState<SecurityStock[]>(
    []
  );
  const [trending, setTrending] = useState<TrendingStock[]>([]);
  const [sectorOverview, setSectorOverview] = useState<SectorOverview[]>([]);
  const [marketStats, setMarketStats] = useState<MarketStats | null>(null);
  const [marketContext, setMarketContext] = useState<MarketContext | null>(
    null
  );
  const [priceAlerts, setPriceAlerts] = useState<PriceAlertInfo[]>([]);
  const [filterOptions, setFilterOptions] = useState<FilterOptions | null>(
    null
  );

  // Filter and sort state
  const [currentFilter, setCurrentFilter] = useState<MarketFilter>({});
  const [currentSort, setCurrentSort] = useState<SortOption>({
    field: "marketCap",
    direction: "desc",
  });

  // Loading state
  const [loading, setLoading] = useState<LoadingState>({
    securities: false,
    search: false,
    filters: false,
    trending: false,
    sectorData: false,
    marketData: false,
  });

  // Error state
  const [error, setError] = useState<MarketError | null>(null);

  // Refs for intervals
  const realTimeIntervalRef = useRef<NodeJS.Timeout | null>(null);
  const trendingIntervalRef = useRef<NodeJS.Timeout | null>(null);
  const statsIntervalRef = useRef<NodeJS.Timeout | null>(null);

  // Update loading state helper
  const updateLoading = useCallback((updates: Partial<LoadingState>) => {
    setLoading((prev) => ({ ...prev, ...updates }));
  }, []);

  // Error handler
  const handleError = useCallback((err: any, context: string) => {
    console.error(`Market data error in ${context}:`, err);
    const marketError: MarketError = {
      code: err.code || "UNKNOWN_ERROR",
      message: err.message || "An unexpected error occurred",
      details: err,
    };
    setError(marketError);
  }, []);

  // Clear error
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  // Load trending stocks
  const loadTrending = useCallback(async () => {
    updateLoading({ trending: true });

    try {
      const data = await marketService.getTrendingStocks(20);
      setTrending(data as TrendingStock[]);
    } catch (err) {
      handleError(err, "loadTrending");
    } finally {
      updateLoading({ trending: false });
    }
  }, [updateLoading, handleError]);

  // Load sector overview
  const loadSectorOverview = useCallback(async () => {
    updateLoading({ sectorData: true });

    try {
      const data = await marketService.getSectorOverview();
      setSectorOverview(data);
    } catch (err) {
      handleError(err, "loadSectorOverview");
    } finally {
      updateLoading({ sectorData: false });
    }
  }, [updateLoading, handleError]);

  // Load market stats
  const loadMarketStats = useCallback(async () => {
    try {
      const data = await marketService.getSystemStats();
      setMarketStats(data);
    } catch (err) {
      handleError(err, "loadMarketStats");
    }
  }, [handleError]);

  // Load market context
  const loadMarketContext = useCallback(async () => {
    try {
      const data = await marketService.getMarketStatus();
      setMarketContext(data);
    } catch (err) {
      handleError(err, "loadMarketContext");
    }
  }, [handleError]);

  // Load price alerts
  const loadPriceAlerts = useCallback(async () => {
    try {
      const data = await marketService.getPriceAlerts();
      setPriceAlerts(data);
    } catch (err) {
      handleError(err, "loadPriceAlerts");
    }
  }, [handleError]);

  // Load filter options
  const loadFilterOptions = useCallback(async () => {
    updateLoading({ filters: true });

    try {
      const data = await marketService.getFilterOptions();
      setFilterOptions(data);
    } catch (err) {
      handleError(err, "loadFilterOptions");
    } finally {
      updateLoading({ filters: false });
    }
  }, [updateLoading, handleError]);

  // Search securities
  const searchSecurities = useCallback(
    async (query: string): Promise<SecurityStock[]> => {
      updateLoading({ search: true });
      clearError();

      try {
        const results = await marketService.searchSecurities(query, 20);
        return results;
      } catch (err) {
        handleError(err, "searchSecurities");
        return [];
      } finally {
        updateLoading({ search: false });
      }
    },
    [updateLoading, clearError, handleError]
  );

  // Load initial securities
  const loadSecurities = useCallback(async () => {
    updateLoading({ securities: true });
    clearError();

    try {
      const data = await marketService.getActiveSecurities({
        page: 0,
        size: 100,
      });
      setSecurities(data);
      setFilteredSecurities(data);
    } catch (err) {
      handleError(err, "loadSecurities");
    } finally {
      updateLoading({ securities: false });
    }
  }, [updateLoading, clearError, handleError]);

  // Add security
  const addSecurity = useCallback(
    async (symbol: string): Promise<void> => {
      updateLoading({ marketData: true });
      clearError();

      try {
        await marketService.createOrUpdateSecurity(symbol);
        // Refresh securities list
        await loadSecurities();
      } catch (err) {
        handleError(err, "addSecurity");
        throw err;
      } finally {
        updateLoading({ marketData: false });
      }
    },
    [updateLoading, clearError, handleError, loadSecurities]
  );

  // Update price for a symbol
  const updatePrice = useCallback(
    async (symbol: string): Promise<void> => {
      try {
        await marketService.updatePrice(symbol);
        // Update the security in the list
        setSecurities((prev) =>
          prev.map((sec) =>
            sec.symbol === symbol
              ? { ...sec, updatedDate: new Date().toISOString() }
              : sec
          )
        );
      } catch (err) {
        handleError(err, "updatePrice");
        throw err;
      }
    },
    [handleError]
  );

  // Fetch historical data
  const fetchHistoricalData = useCallback(
    async (symbol: string, from: string, to: string): Promise<MarketData[]> => {
      updateLoading({ marketData: true });
      clearError();

      try {
        const response = await marketService.getHistoricalPrices(
          symbol,
          from,
          to
        );
        return response.data;
      } catch (err) {
        handleError(err, "fetchHistoricalData");
        return [];
      } finally {
        updateLoading({ marketData: false });
      }
    },
    [updateLoading, clearError, handleError]
  );

  // Apply filter
  const applyFilter = useCallback(
    (filter: MarketFilter) => {
      setCurrentFilter(filter);

      let filtered = [...securities];

      // Apply sector filter
      if (filter.sector) {
        filtered = filtered.filter((sec) => sec.sector === filter.sector);
      }

      // Apply exchange filter
      if (filter.exchange) {
        filtered = filtered.filter((sec) => sec.exchange === filter.exchange);
      }

      // Apply price range filter
      if (filter.priceMin !== undefined) {
        filtered = filtered.filter(
          (sec) => sec.currentPrice >= filter.priceMin!
        );
      }
      if (filter.priceMax !== undefined) {
        filtered = filtered.filter(
          (sec) => sec.currentPrice <= filter.priceMax!
        );
      }

      // Apply market cap range filter
      if (filter.marketCapMin !== undefined) {
        filtered = filtered.filter(
          (sec) => sec.marketCap >= filter.marketCapMin!
        );
      }
      if (filter.marketCapMax !== undefined) {
        filtered = filtered.filter(
          (sec) => sec.marketCap <= filter.marketCapMax!
        );
      }

      // Apply search query filter
      if (filter.searchQuery) {
        const query = filter.searchQuery.toLowerCase();
        filtered = filtered.filter(
          (sec) =>
            sec.symbol.toLowerCase().includes(query) ||
            sec.companyName.toLowerCase().includes(query)
        );
      }

      setFilteredSecurities(filtered);
    },
    [securities]
  );

  // Apply sort
  const applySort = useCallback((sort: SortOption) => {
    setCurrentSort(sort);

    setFilteredSecurities((prev) => {
      const sorted = [...prev].sort((a, b) => {
        let aValue: any = a[sort.field];
        let bValue: any = b[sort.field];

        // Handle different data types
        if (typeof aValue === "string" && typeof bValue === "string") {
          aValue = aValue.toLowerCase();
          bValue = bValue.toLowerCase();
        }

        if (aValue < bValue) {
          return sort.direction === "asc" ? -1 : 1;
        }
        if (aValue > bValue) {
          return sort.direction === "asc" ? 1 : -1;
        }
        return 0;
      });

      return sorted;
    });
  }, []);

  // Clear filters
  const clearFilters = useCallback(() => {
    setCurrentFilter({});
    setFilteredSecurities(securities);
  }, [securities]);

  // Start real-time updates
  const startRealTimeUpdates = useCallback(() => {
    // Clear existing intervals
    if (realTimeIntervalRef.current) {
      clearInterval(realTimeIntervalRef.current);
    }
    if (trendingIntervalRef.current) {
      clearInterval(trendingIntervalRef.current);
    }
    if (statsIntervalRef.current) {
      clearInterval(statsIntervalRef.current);
    }

    // Update prices for currently visible securities
    realTimeIntervalRef.current = setInterval(async () => {
      if (marketContext?.isMarketOpen && filteredSecurities.length > 0) {
        try {
          // Update prices for top 10 visible securities to avoid rate limiting
          const symbols = filteredSecurities
            .slice(0, 10)
            .map((sec) => sec.symbol);
          await marketService.bulkUpdatePrices(symbols);

          // Refresh securities data
          const updatedSecurities = await marketService.getActiveSecurities({
            page: 0,
            size: 100,
          });
          setSecurities(updatedSecurities);

          // Reapply current filter
          applyFilter(currentFilter);
        } catch (err) {
          console.warn("Real-time update failed:", err);
        }
      }
    }, REAL_TIME_UPDATE_INTERVAL);

    // Update trending stocks
    trendingIntervalRef.current = setInterval(() => {
      loadTrending();
    }, TRENDING_UPDATE_INTERVAL);

    // Update market stats
    statsIntervalRef.current = setInterval(() => {
      loadMarketStats();
      loadMarketContext();
      loadPriceAlerts();
    }, STATS_UPDATE_INTERVAL);
  }, [
    marketContext?.isMarketOpen,
    filteredSecurities,
    currentFilter,
    applyFilter,
    loadTrending,
    loadMarketStats,
    loadMarketContext,
    loadPriceAlerts,
  ]);

  // Stop real-time updates
  const stopRealTimeUpdates = useCallback(() => {
    if (realTimeIntervalRef.current) {
      clearInterval(realTimeIntervalRef.current);
      realTimeIntervalRef.current = null;
    }
    if (trendingIntervalRef.current) {
      clearInterval(trendingIntervalRef.current);
      trendingIntervalRef.current = null;
    }
    if (statsIntervalRef.current) {
      clearInterval(statsIntervalRef.current);
      statsIntervalRef.current = null;
    }
  }, []);

  // Refresh all data
  const refreshData = useCallback(async () => {
    clearError();

    try {
      await Promise.all([
        loadSecurities(),
        loadTrending(),
        loadSectorOverview(),
        loadMarketStats(),
        loadMarketContext(),
        loadPriceAlerts(),
        loadFilterOptions(),
      ]);
    } catch (err) {
      handleError(err, "refreshData");
    }
  }, [
    clearError,
    loadSecurities,
    loadTrending,
    loadSectorOverview,
    loadMarketStats,
    loadMarketContext,
    loadPriceAlerts,
    loadFilterOptions,
    handleError,
  ]);

  // Initial data load
  useEffect(() => {
    refreshData();
  }, [refreshData]);

  // Apply filters when securities change
  useEffect(() => {
    if (Object.keys(currentFilter).length > 0) {
      applyFilter(currentFilter);
    } else {
      setFilteredSecurities(securities);
    }
  }, [securities, currentFilter, applyFilter]);

  // Apply sort when filtered securities change
  useEffect(() => {
    if (filteredSecurities.length > 0) {
      applySort(currentSort);
    }
  }, [currentSort, applySort]);

  // Start real-time updates when market context is available
  useEffect(() => {
    if (marketContext) {
      startRealTimeUpdates();
    }

    return () => {
      stopRealTimeUpdates();
    };
  }, [marketContext, startRealTimeUpdates, stopRealTimeUpdates]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      stopRealTimeUpdates();
    };
  }, [stopRealTimeUpdates]);

  return {
    // Data
    securities: filteredSecurities,
    trending,
    sectorOverview,
    marketStats,
    marketContext,
    priceAlerts,
    filterOptions,

    // Loading states
    loading,

    // Error states
    error,

    // Actions
    searchSecurities,
    addSecurity,
    updatePrice,
    fetchHistoricalData,

    // Filters and sorting
    applyFilter,
    applySort,
    clearFilters,

    // Real-time updates
    startRealTimeUpdates,
    stopRealTimeUpdates,

    // Refresh
    refreshData,
  };
};
