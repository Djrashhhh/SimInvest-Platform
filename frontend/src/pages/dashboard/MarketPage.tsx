import React, {
  useState,
  useEffect,
  useCallback,
  useMemo,
  MouseEvent as ReactMouseEvent,
} from "react";
import {
  Search,
  Plus,
  TrendingUp,
  TrendingDown,
  BarChart3,
  Star,
  StarOff,
  Filter,
  RefreshCw,
  Bell,
  User,
  LogOut,
  Home,
  X,
  Eye,
  ArrowUpDown,
  ChevronDown,
  ChevronUp,
  Building2,
  Activity,
  Zap,
  PieChart,
  ExternalLink,
  AlertTriangle,
  Newspaper,
} from "lucide-react";
import { Link } from "react-router-dom";
import { marketService } from "../../services/marketService";
import { SECTOR_DISPLAY_NAMES } from "../../types/market";
import { useAuth } from "../../contexts/AuthContext";

/** ===== Types ===== */
type SectorKey = keyof typeof SECTOR_DISPLAY_NAMES | string;

interface Security {
  id?: string | number;
  securityId?: number;
  symbol: string;
  companyName: string;
  currentPrice?: number | null;
  updatedDate?: string | number | Date;
  sector: SectorKey;
  exchange: string;
  marketCap?: number | null;
  securityType?: string;
  isActive?: boolean;
  createdDate?: string | number | Date;
}

type TrendingSecurity = Security;

interface SectorOverviewItem {
  sector: SectorKey;
  count: number;
  averagePrice?: number | null;
}

interface MarketStats {
  circuitBreakerOpen: boolean;
  consecutiveFailures: number;
  todayRecords: number;
  yesterdayRecords: number;
  timestamp: string | number | Date;
}

interface MarketContext {
  isMarketOpen: boolean;
  lastUpdateTime?: string | number | Date;
}

type PriceAlertType =
  | "SIGNIFICANT_GAIN"
  | "SIGNIFICANT_LOSS"
  | "PRICE_TARGET"
  | string;

interface PriceAlert {
  symbol: string;
  changePercentage: number;
  companyName: string;
  currentPrice: number;
  alertType: PriceAlertType;
}

/** ===== Sector color mapping ===== */
const SECTOR_COLORS: Record<string, string> = {
  TECHNOLOGY: "bg-blue-500",
  HEALTHCARE: "bg-green-500",
  FINANCIALS: "bg-yellow-500",
  CONSUMER_DISCRETIONARY: "bg-purple-500",
  CONSUMER_STAPLES: "bg-orange-500",
  COMMUNICATION_SERVICES: "bg-pink-500",
  INDUSTRIALS: "bg-gray-500",
  ENERGY: "bg-red-500",
  MATERIALS: "bg-amber-600",
  UTILITIES: "bg-teal-500",
  REAL_ESTATE: "bg-indigo-500",
};

type ViewMode = "table" | "cards";
type Tab = "all" | "watchlist";

const FunctionalMarketPage: React.FC = () => {
  // Core data state
  const [securities, setSecurities] = useState<Security[]>([]);
  const [searchResults, setSearchResults] = useState<Security[]>([]);
  const [trending, setTrending] = useState<TrendingSecurity[]>([]);
  const [sectorOverview, setSectorOverview] = useState<SectorOverviewItem[]>(
    []
  );
  const [marketStats, setMarketStats] = useState<MarketStats | null>(null);
  const [marketContext, setMarketContext] = useState<MarketContext | null>(
    null
  );
  const [priceAlerts, setPriceAlerts] = useState<PriceAlert[]>([]);

  // UI state
  const [searchTerm, setSearchTerm] = useState<string>("");
  const [watchlist, setWatchlist] = useState<string[]>(["AAPL", "MSFT"]);
  const [selectedTab, setSelectedTab] = useState<Tab>("all");
  const [showFilters, setShowFilters] = useState<boolean>(false);
  const [showAddSecurity, setShowAddSecurity] = useState<boolean>(false);
  const [newSymbol, setNewSymbol] = useState<string>("");
  const [showUserMenu, setShowUserMenu] = useState<boolean>(false);
  const [showNotifications, setShowNotifications] = useState<boolean>(false);
  const [selectedSecurity, setSelectedSecurity] = useState<Security | null>(
    null
  );
  const [viewMode, setViewMode] = useState<ViewMode>("table");

  // Loading state
  const [loading, setLoading] = useState<{
    securities: boolean;
    search: boolean;
    addSecurity: boolean;
    refresh: boolean;
  }>({
    securities: false,
    search: false,
    addSecurity: false,
    refresh: false,
  });

  // Error state
  const [error, setError] = useState<string | null>(null);

  // Auth context
  const { user } = useAuth();

  // Format currency
  const formatCurrency = (amount?: number | null): string => {
    if (amount == null || Number.isNaN(amount)) return "$0.00";
    if (amount >= 1e12) return `$${(amount / 1e12).toFixed(1)}T`;
    if (amount >= 1e9) return `$${(amount / 1e9).toFixed(1)}B`;
    if (amount >= 1e6) return `$${(amount / 1e6).toFixed(1)}M`;
    if (amount >= 1e3) return `$${(amount / 1e3).toFixed(1)}K`;
    return `$${amount.toFixed(2)}`;
  };

  // Get time since last update
  const getTimeSinceUpdate = (updatedDate?: string | number | Date): string => {
    if (!updatedDate) return "—";
    const updated = new Date(updatedDate);
    if (isNaN(updated.getTime())) return "—";

    const now = new Date();
    const diffMs = now.getTime() - updated.getTime();
    const diffMins = Math.floor(diffMs / 60000);

    if (diffMins < 1) return "Just now";
    if (diffMins === 1) return "1 min ago";
    if (diffMins < 60) return `${diffMins} mins ago`;

    const diffHours = Math.floor(diffMins / 60);
    if (diffHours === 1) return "1 hour ago";
    if (diffHours < 24) return `${diffHours} hours ago`;

    return updated.toLocaleDateString();
  };

  // Load initial data
  const loadInitialData = useCallback(async () => {
    setLoading((prev) => ({ ...prev, securities: true }));
    setError(null);

    try {
      const [
        securitiesData,
        statsData,
        marketContextData,
        trendingData,
        sectorData,
        alertsData,
      ] = await Promise.allSettled([
        marketService.getActiveSecurities({ page: 0, size: 100 }),
        marketService.getSystemStats(),
        marketService.getMarketStatus(),
        marketService.getTrendingStocks(10),
        marketService.getSectorOverview(),
        marketService.getPriceAlerts(),
      ]);

      if (securitiesData.status === "fulfilled") {
        setSecurities(securitiesData.value as Security[]);
      } else {
        console.error("Failed to load securities:", securitiesData.reason);
      }

      if (statsData.status === "fulfilled") {
        setMarketStats(statsData.value as MarketStats);
      } else {
        console.error("Failed to load market stats:", statsData.reason);
      }

      if (marketContextData.status === "fulfilled") {
        setMarketContext(marketContextData.value as MarketContext);
      } else {
        console.error(
          "Failed to load market context:",
          marketContextData.reason
        );
      }

      if (trendingData.status === "fulfilled") {
        setTrending(trendingData.value as TrendingSecurity[]);
      } else {
        console.error("Failed to load trending data:", trendingData.reason);
      }

      if (sectorData.status === "fulfilled") {
        setSectorOverview(sectorData.value as SectorOverviewItem[]);
      } else {
        console.error("Failed to load sector overview:", sectorData.reason);
      }

      if (alertsData.status === "fulfilled") {
        setPriceAlerts(alertsData.value as PriceAlert[]);
      } else {
        console.error("Failed to load price alerts:", alertsData.reason);
      }
    } catch (err: any) {
      console.error("Error loading initial data:", err);
      setError(err?.message ?? "Failed to load data");
    } finally {
      setLoading((prev) => ({ ...prev, securities: false }));
    }
  }, []);

  // Search securities
  const handleSearch = useCallback(async (query: string) => {
    if (!query.trim()) {
      setSearchResults([]);
      return;
    }

    setLoading((prev) => ({ ...prev, search: true }));
    setError(null);

    try {
      const results = (await marketService.searchSecurities(
        query,
        10
      )) as Security[];
      setSearchResults(results);
    } catch (err: any) {
      console.error("Search failed:", err);
      setError(err?.message ?? "Search failed");
      setSearchResults([]);
    } finally {
      setLoading((prev) => ({ ...prev, search: false }));
    }
  }, []);

  // Debounced search
  useEffect(() => {
    const timer = setTimeout(() => {
      void handleSearch(searchTerm);
    }, 500);
    return () => clearTimeout(timer);
  }, [searchTerm, handleSearch]);

  // Add security
  const handleAddSecurity = async () => {
    if (!newSymbol.trim()) return;

    setLoading((prev) => ({ ...prev, addSecurity: true }));
    setError(null);

    try {
      await marketService.createOrUpdateSecurity(newSymbol.toUpperCase());
      await loadInitialData();
      setNewSymbol("");
      setShowAddSecurity(false);
    } catch (err: any) {
      console.error("Failed to add security:", err);
      setError(err?.message ?? "Failed to add security");
    } finally {
      setLoading((prev) => ({ ...prev, addSecurity: false }));
    }
  };

  const getSecurityId = (security: Security): number => {
    if (security.securityId) return security.securityId;
    if (typeof security.id === "number") return security.id;
    if (typeof security.id === "string") {
      const parsed = parseInt(security.id);
      if (!isNaN(parsed)) return parsed;
    }
    // If no valid ID is found, you might want to handle this case
    // For now, return 0 as fallback (you should handle this properly)
    console.warn("No valid security ID found for:", security.symbol);
    return 0;
  };

  // Update price for a symbol
  const handleUpdatePrice = async (symbol: string) => {
    try {
      await marketService.updatePrice(symbol);
      await loadInitialData();
    } catch (err: any) {
      console.error("Failed to update price:", err);
      setError(err?.message ?? "Failed to update price");
    }
  };

  // Refresh all data
  const handleRefresh = async () => {
    setLoading((prev) => ({ ...prev, refresh: true }));
    await loadInitialData();
    setLoading((prev) => ({ ...prev, refresh: false }));
  };

  // Watchlist management
  const handleAddToWatchlist = (symbol: string) => {
    setWatchlist((prev) => (prev.includes(symbol) ? prev : [...prev, symbol]));
  };
  const handleRemoveFromWatchlist = (symbol: string) => {
    setWatchlist((prev) => prev.filter((s) => s !== symbol));
  };

  // Filter securities based on selected tab (memoized)
  const filteredSecurities = useMemo(
    () =>
      securities.filter(
        (stock) => selectedTab === "all" || watchlist.includes(stock.symbol)
      ),
    [securities, selectedTab, watchlist]
  );

  // Load initial data on component mount
  useEffect(() => {
    void loadInitialData();
  }, [loadInitialData]);

  // Stock Card Component
  const StockCard: React.FC<{ stock: Security }> = ({ stock }) => {
    const isInWatchlist = watchlist.includes(stock.symbol);

    return (
      <div className="bg-slate-800/50 backdrop-blur-sm rounded-xl p-6 border border-slate-700/50 hover:bg-slate-800/70 transition-all duration-300 cursor-pointer">
        <div className="flex justify-between items-start mb-4">
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-xl font-bold text-white">{stock.symbol}</h3>
              <button
                onClick={(e: ReactMouseEvent<HTMLButtonElement>) => {
                  e.stopPropagation();
                  if (isInWatchlist) {
                    handleRemoveFromWatchlist(stock.symbol);
                  } else {
                    // Redirect to watchlist page to add this security
                    window.location.href = `/watchlist?add=${stock.symbol}`;
                  }
                }}
                className="text-yellow-400 hover:text-yellow-300 transition-colors"
              >
                {isInWatchlist ? (
                  <Star className="w-4 h-4 fill-current" />
                ) : (
                  <StarOff className="w-4 h-4" />
                )}
              </button>
            </div>
            <p
              className="text-slate-300 text-sm truncate max-w-48"
              title={stock.companyName}
            >
              {stock.companyName}
            </p>
          </div>
          <div className="text-right">
            <p className="text-2xl font-bold text-white">
              $
              {stock.currentPrice != null
                ? stock.currentPrice.toFixed(2)
                : "0.00"}
            </p>
            <p className="text-xs text-slate-400 mt-1">
              {getTimeSinceUpdate(stock.updatedDate)}
            </p>
          </div>
        </div>

        <div className="flex items-center justify-between mb-3">
          <span
            className={`text-xs px-2 py-1 rounded-full text-white ${SECTOR_COLORS[stock.sector] || "bg-gray-500"}`}
          >
            {SECTOR_DISPLAY_NAMES[stock.sector]?.abbreviation ||
              String(stock.sector)}
          </span>
          <span className="text-xs text-slate-400">{stock.exchange}</span>
        </div>

        <div className="flex justify-between items-center text-sm">
          <span className="text-slate-300">
            {formatCurrency(stock.marketCap)}
          </span>
          <button
            onClick={(e: ReactMouseEvent<HTMLButtonElement>) => {
              e.stopPropagation();
              setSelectedSecurity(stock);
            }}
            className="flex items-center gap-1 text-blue-400 hover:text-blue-300 transition-colors"
          >
            <Eye className="w-4 h-4" />
            Details
          </button>
        </div>
      </div>
    );
  };

  // Close dropdowns when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as HTMLElement | null;
      if (!target) return;
      if (!target.closest(".user-menu")) {
        setShowUserMenu(false);
      }
      if (!target.closest(".notifications-menu")) {
        setShowNotifications(false);
      }
    };

    document.addEventListener("click", handleClickOutside);
    return () => document.removeEventListener("click", handleClickOutside);
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-white">
      {/* Header */}
      <header className="bg-slate-800/80 backdrop-blur-sm border-b border-slate-700/50 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            {/* Logo and Navigation */}
            <div className="flex items-center space-x-8">
              <Link to="/dashboard" className="flex items-center space-x-2">
                <BarChart3 className="h-8 w-8 text-blue-400" />
                <span className="text-xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-cyan-400">
                  SimInvest
                </span>
              </Link>

              <nav className="hidden md:flex space-x-6">
                <Link
                  to="/dashboard"
                  className="flex items-center space-x-1 text-slate-300 hover:text-white transition-colors"
                >
                  <Home className="h-4 w-4" />
                  <span>Dashboard</span>
                </Link>
                <Link
                  to="/market"
                  className="flex items-center space-x-1 text-blue-400"
                >
                  <BarChart3 className="h-4 w-4" />
                  <span>Market</span>
                </Link>
                <Link
                  to="/watchlist"
                  className="flex items-center space-x-1 text-slate-300 hover:text-white transition-colors"
                >
                  <Star className="h-4 w-4" />
                  <span>Watchlist</span>
                </Link>
              </nav>
            </div>

            {/* Market Status and Actions */}
            <div className="flex items-center space-x-4">
              {/* Market Status Indicator */}
              {marketContext && (
                <div className="hidden sm:flex items-center space-x-2 px-3 py-1 rounded-full bg-slate-700/50">
                  <div
                    className={`w-2 h-2 rounded-full ${marketContext.isMarketOpen ? "bg-green-400 animate-pulse" : "bg-red-400"}`}
                  ></div>
                  <span className="text-sm text-slate-300">
                    {marketContext.isMarketOpen
                      ? "Market Open"
                      : "Market Closed"}
                  </span>
                  {marketContext.lastUpdateTime && (
                    <span className="text-xs text-slate-500 ml-2">
                      {new Date(
                        marketContext.lastUpdateTime
                      ).toLocaleTimeString()}
                    </span>
                  )}
                </div>
              )}

              {/* Refresh Button */}
              <button
                onClick={handleRefresh}
                disabled={loading.refresh}
                className="p-2 text-slate-400 hover:text-white transition-colors disabled:opacity-50"
                title="Refresh Data"
              >
                <RefreshCw
                  className={`h-5 w-5 ${loading.refresh ? "animate-spin" : ""}`}
                />
              </button>

              {/* Notifications */}
              <div className="notifications-menu relative">
                <button
                  onClick={() => setShowNotifications((s) => !s)}
                  className="relative p-2 text-slate-400 hover:text-white transition-colors"
                >
                  <Bell className="h-5 w-5" />
                  {priceAlerts.length > 0 && (
                    <span className="absolute top-0 right-0 h-2 w-2 bg-red-500 rounded-full animate-pulse"></span>
                  )}
                </button>

                {showNotifications && (
                  <div className="absolute right-0 mt-2 w-80 bg-slate-800 rounded-lg shadow-lg border border-slate-700 z-50">
                    <div className="p-4">
                      <div className="flex items-center justify-between mb-3">
                        <h3 className="font-semibold text-white">
                          Price Alerts
                        </h3>
                        <span className="text-xs text-slate-400">
                          {priceAlerts.length} active
                        </span>
                      </div>
                      {priceAlerts.length === 0 ? (
                        <p className="text-slate-400 text-sm">
                          No active alerts
                        </p>
                      ) : (
                        <div className="space-y-2 max-h-64 overflow-y-auto">
                          {priceAlerts.slice(0, 5).map((alert, index) => (
                            <div
                              key={`${alert.symbol}-${index}`}
                              className="p-3 bg-slate-700 rounded-lg"
                            >
                              <div className="flex items-center justify-between mb-1">
                                <span className="font-medium text-white">
                                  {alert.symbol}
                                </span>
                                <span
                                  className={`text-sm font-medium ${
                                    alert.changePercentage >= 0
                                      ? "text-green-400"
                                      : "text-red-400"
                                  }`}
                                >
                                  {alert.changePercentage >= 0 ? "+" : ""}
                                  {alert.changePercentage.toFixed(2)}%
                                </span>
                              </div>
                              <p className="text-slate-400 text-xs mb-1">
                                {alert.companyName}
                              </p>
                              <div className="flex items-center justify-between">
                                <span className="text-white text-sm">
                                  ${alert.currentPrice.toFixed(2)}
                                </span>
                                <span
                                  className={`text-xs px-2 py-1 rounded-full ${
                                    alert.alertType === "SIGNIFICANT_GAIN"
                                      ? "bg-green-900 text-green-300"
                                      : alert.alertType === "SIGNIFICANT_LOSS"
                                        ? "bg-red-900 text-red-300"
                                        : "bg-yellow-900 text-yellow-300"
                                  }`}
                                >
                                  {String(alert.alertType)
                                    .replace("_", " ")
                                    .toLowerCase()}
                                </span>
                              </div>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                )}
              </div>

              {/* User Menu */}
              <div className="user-menu relative">
                <button
                  onClick={() => setShowUserMenu((s) => !s)}
                  className="flex items-center space-x-2 p-2 rounded-lg hover:bg-slate-700 transition-colors"
                >
                  <User className="h-5 w-5 text-slate-400" />
                  <ChevronDown
                    className={`h-4 w-4 text-slate-400 transition-transform ${
                      showUserMenu ? "rotate-180" : ""
                    }`}
                  />
                </button>

                {showUserMenu && (
                  <div className="absolute right-0 mt-2 w-48 bg-slate-800 rounded-lg shadow-lg border border-slate-700 z-50">
                    <div className="p-2">
                      <Link
                        to="/dashboard"
                        className="flex items-center space-x-2 px-3 py-2 text-slate-300 hover:text-white hover:bg-slate-700 rounded-lg transition-colors"
                        onClick={() => setShowUserMenu(false)}
                      >
                        <Home className="h-4 w-4" />
                        <span>Dashboard</span>
                      </Link>

                      <button
                        onClick={() => {
                          // logout();
                          setShowUserMenu(false);
                        }}
                        className="w-full flex items-center space-x-2 px-3 py-2 text-red-400 hover:bg-slate-700 rounded-lg transition-colors"
                      >
                        <LogOut className="h-4 w-4" />
                        <span>Log Out</span>
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Page Header */}
        <div className="mb-8">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-white mb-2">
                Market Overview
              </h1>
              <p className="text-slate-400">
                Real-time market data powered by Finnhub API
              </p>
            </div>

            {/* Live Update Indicator */}
            {marketContext?.isMarketOpen && (
              <div className="flex items-center space-x-2 px-4 py-2 bg-green-900/20 border border-green-500/30 rounded-lg">
                <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
                <span className="text-green-400 text-sm font-medium">
                  Live Data
                </span>
              </div>
            )}
          </div>
        </div>

        {/* Error Display */}
        {error && (
          <div className="mb-6 bg-red-900/20 border border-red-500/50 rounded-lg p-4">
            <div className="flex items-center space-x-2">
              <AlertTriangle className="h-5 w-5 text-red-400" />
              <span className="text-red-400 font-medium">Error: {error}</span>
            </div>
            <button
              onClick={() => setError(null)}
              className="mt-2 px-3 py-1 text-sm bg-red-600 hover:bg-red-700 rounded transition-colors"
            >
              Dismiss
            </button>
          </div>
        )}

        {/* Market Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-slate-800/50 rounded-xl p-6 border border-slate-700/50">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-slate-400 text-sm">Active Securities</p>
                <p className="text-2xl font-bold text-white">
                  {loading.securities ? (
                    <span className="animate-pulse">...</span>
                  ) : (
                    (marketStats?.activeSecurities ?? securities.length)
                  )}
                </p>
                <p className="text-xs text-slate-500 mt-1">
                  {filteredSecurities.length} filtered
                </p>
              </div>
              <Building2 className="h-8 w-8 text-blue-400" />
            </div>
          </div>

          <div className="bg-slate-800/50 rounded-xl p-6 border border-slate-700/50">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-slate-400 text-sm">Price Alerts</p>
                <p className="text-2xl font-bold text-white">
                  {priceAlerts.length}
                </p>
                <p className="text-xs text-slate-500 mt-1">
                  Active notifications
                </p>
              </div>
              <Bell className="h-8 w-8 text-yellow-400" />
            </div>
          </div>

          <div className="bg-slate-800/50 rounded-xl p-6 border border-slate-700/50">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-slate-400 text-sm">Market Status</p>
                <p
                  className={`text-lg font-semibold ${
                    marketContext?.isMarketOpen
                      ? "text-green-400"
                      : "text-red-400"
                  }`}
                >
                  {marketContext?.isMarketOpen ? "Open" : "Closed"}
                </p>
                <p className="text-xs text-slate-500 mt-1">
                  {marketContext?.isMarketOpen ? "Live data" : "Last close"}
                </p>
              </div>
              <Activity
                className={`h-8 w-8 ${
                  marketContext?.isMarketOpen
                    ? "text-green-400"
                    : "text-red-400"
                }`}
              />
            </div>
          </div>

          <div className="bg-slate-800/50 rounded-xl p-6 border border-slate-700/50">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-slate-400 text-sm">Data Health</p>
                <p
                  className={`text-lg font-semibold ${
                    marketStats?.circuitBreakerOpen
                      ? "text-red-400"
                      : "text-green-400"
                  }`}
                >
                  {marketStats?.circuitBreakerOpen ? "Issues" : "Healthy"}
                </p>
                <p className="text-xs text-slate-500 mt-1">
                  {marketStats?.consecutiveFailures ?? 0} failures
                </p>
              </div>
              <Zap
                className={`h-8 w-8 ${
                  marketStats?.circuitBreakerOpen
                    ? "text-red-400"
                    : "text-green-400"
                }`}
              />
            </div>
          </div>
        </div>

        {/* Search and Filter Bar */}
        <div className="bg-slate-800/50 rounded-xl p-6 mb-8 border border-slate-700/50">
          <div className="flex flex-col sm:flex-row gap-4">
            {/* Search Input */}
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-slate-400" />
              <input
                type="text"
                placeholder="Search stocks by symbol or company name (e.g., AAPL, Tesla)..."
                value={searchTerm}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                  setSearchTerm(e.currentTarget.value)
                }
                className="w-full pl-10 pr-4 py-3 bg-slate-700/50 border border-slate-600/50 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
              {loading.search && (
                <RefreshCw className="absolute right-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-slate-400 animate-spin" />
              )}
            </div>

            {/* Action Buttons */}
            <div className="flex space-x-3">
              <button
                onClick={() => setShowAddSecurity((s) => !s)}
                className="flex items-center space-x-2 px-4 py-3 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors"
              >
                <Plus className="h-5 w-5" />
                <span>Add Stock</span>
              </button>

              {/* View Mode Toggle */}
              <div className="flex bg-slate-700/50 rounded-lg p-1">
                <button
                  onClick={() => setViewMode("table")}
                  className={`p-2 rounded transition-colors ${
                    viewMode === "table"
                      ? "bg-slate-600 text-white"
                      : "text-slate-400 hover:text-white"
                  }`}
                  title="Table View"
                >
                  <BarChart3 className="h-4 w-4" />
                </button>
                <button
                  onClick={() => setViewMode("cards")}
                  className={`p-2 rounded transition-colors ${
                    viewMode === "cards"
                      ? "bg-slate-600 text-white"
                      : "text-slate-400 hover:text-white"
                  }`}
                  title="Card View"
                >
                  <Building2 className="h-4 w-4" />
                </button>
              </div>
            </div>
          </div>

          {/* Search Results */}
          {searchTerm && searchResults.length > 0 && (
            <div className="mt-4 border-t border-slate-700/50 pt-4">
              <h3 className="text-lg font-semibold text-white mb-3">
                Search Results
              </h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                {searchResults.slice(0, 6).map((stock) => (
                  <div
                    key={`${stock.id ?? stock.symbol}`}
                    className="bg-slate-700 rounded-lg p-3 cursor-pointer hover:bg-slate-600 transition-colors"
                    onClick={() => setSelectedSecurity(stock)}
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center space-x-2">
                          <span className="font-medium text-white">
                            {stock.symbol}
                          </span>
                          <span
                            className={`text-xs px-2 py-0.5 rounded-full text-white ${
                              SECTOR_COLORS[stock.sector] || "bg-gray-500"
                            }`}
                          >
                            {SECTOR_DISPLAY_NAMES[stock.sector]?.abbreviation ||
                              String(stock.sector)}
                          </span>
                        </div>
                        <p
                          className="text-slate-400 text-sm truncate"
                          title={stock.companyName}
                        >
                          {stock.companyName}
                        </p>
                        <div className="flex items-center space-x-2 mt-1">
                          <span className="text-xs text-slate-500">
                            {stock.exchange}
                          </span>
                          <span className="text-xs text-slate-500">•</span>
                          <span className="text-xs text-slate-500">
                            {formatCurrency(stock.marketCap)}
                          </span>
                        </div>
                      </div>
                      <div className="text-right">
                        <span className="text-white font-medium">
                          $
                          {stock.currentPrice != null
                            ? stock.currentPrice.toFixed(2)
                            : "0.00"}
                        </span>
                        <p className="text-xs text-slate-400">
                          {getTimeSinceUpdate(stock.updatedDate)}
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Add Security Panel */}
          {showAddSecurity && (
            <div className="mt-4 border-t border-slate-700/50 pt-4">
              <div className="flex items-center space-x-4">
                <div className="flex-1">
                  <input
                    type="text"
                    placeholder="Enter stock symbol (e.g., AAPL, TSLA, MSFT)"
                    value={newSymbol}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                      setNewSymbol(e.currentTarget.value.toUpperCase())
                    }
                    onKeyDown={(e: React.KeyboardEvent<HTMLInputElement>) =>
                      e.key === "Enter" && handleAddSecurity()
                    }
                    className="w-full px-4 py-3 bg-slate-700/50 border border-slate-600/50 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                  <p className="text-xs text-slate-500 mt-1">
                    Stock will be automatically created with real-time data from
                    Finnhub
                  </p>
                </div>
                <button
                  onClick={handleAddSecurity}
                  disabled={!newSymbol.trim() || loading.addSecurity}
                  className="px-6 py-3 bg-green-600 hover:bg-green-700 disabled:bg-slate-600 disabled:cursor-not-allowed text-white rounded-lg transition-colors flex items-center space-x-2"
                >
                  {loading.addSecurity ? (
                    <>
                      <RefreshCw className="h-4 w-4 animate-spin" />
                      <span>Adding...</span>
                    </>
                  ) : (
                    <>
                      <Plus className="h-4 w-4" />
                      <span>Add</span>
                    </>
                  )}
                </button>
                <button
                  onClick={() => {
                    setShowAddSecurity(false);
                    setNewSymbol("");
                  }}
                  className="p-3 text-slate-400 hover:text-white transition-colors"
                >
                  <X className="h-5 w-5" />
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Tabs */}
        <div className="mb-6">
          <div className="flex gap-4">
            <Link
              to="/watchlist"
              className="px-6 py-2 rounded-lg font-medium transition-colors text-slate-300 hover:text-white hover:bg-slate-700/50"
            >
              My Watchlist
            </Link>
            <button
              onClick={() => setSelectedTab("all")}
              className={`px-6 py-2 rounded-lg font-medium transition-colors ${
                selectedTab === "all"
                  ? "bg-blue-600 text-white"
                  : "text-slate-300 hover:text-white hover:bg-slate-700/50"
              }`}
            >
              All Stocks ({securities.length})
            </button>
          </div>
        </div>

        {/* Main Content Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          {/* Securities List */}
          <div className="lg:col-span-3">
            {loading.securities ? (
              <div className="bg-slate-800/50 rounded-xl p-8 border border-slate-700/50">
                <div className="flex items-center justify-center">
                  <RefreshCw className="h-8 w-8 text-blue-400 animate-spin mr-3" />
                  <span className="text-slate-400">Loading securities...</span>
                </div>
              </div>
            ) : filteredSecurities.length === 0 ? (
              <div className="text-center py-16">
                <div className="bg-slate-800/50 backdrop-blur-sm rounded-xl p-8 max-w-md mx-auto border border-slate-700/50">
                  <BarChart3 className="w-16 h-16 text-slate-400 mx-auto mb-4" />
                  <h3 className="text-xl font-semibold text-white mb-2">
                    No stocks found
                  </h3>
                  <p className="text-slate-400 mb-4">
                    {selectedTab === "watchlist"
                      ? "Add some stocks to your watchlist to get started"
                      : "Search for stocks or add them to your dashboard"}
                  </p>
                  <button
                    onClick={() => setShowAddSecurity(true)}
                    className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors"
                  >
                    Add Your First Stock
                  </button>
                </div>
              </div>
            ) : viewMode === "cards" ? (
              <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
                {filteredSecurities.map((stock) => (
                  <StockCard
                    key={`${stock.id ?? stock.symbol}`}
                    stock={stock}
                  />
                ))}
              </div>
            ) : (
              /* Table View */
              <div className="bg-slate-800/50 rounded-xl overflow-hidden border border-slate-700/50">
                <div className="px-6 py-4 border-b border-slate-700/50">
                  <h2 className="text-xl font-bold text-white">
                    Securities ({filteredSecurities.length})
                  </h2>
                </div>
                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead className="bg-slate-700/50">
                      <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">
                          Symbol
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">
                          Company
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">
                          Price
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">
                          Market Cap
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">
                          Sector
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">
                          Exchange
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">
                          Actions
                        </th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-700/50">
                      {filteredSecurities.map((security) => {
                        const isInWatchlist = watchlist.includes(
                          security.symbol
                        );

                        return (
                          <tr
                            key={`${security.id ?? security.symbol}`}
                            className="hover:bg-slate-700/25 transition-colors"
                          >
                            <td className="px-6 py-4 whitespace-nowrap">
                              <div className="flex items-center">
                                <span className="text-sm font-medium text-white">
                                  {security.symbol}
                                </span>
                                {marketContext?.isMarketOpen && (
                                  <div className="ml-2 w-1.5 h-1.5 bg-green-400 rounded-full animate-pulse"></div>
                                )}
                              </div>
                            </td>
                            <td className="px-6 py-4">
                              <div
                                className="text-sm text-white truncate max-w-xs"
                                title={security.companyName}
                              >
                                {security.companyName}
                              </div>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap">
                              <div className="text-sm text-white font-medium">
                                $
                                {security.currentPrice != null
                                  ? security.currentPrice.toFixed(2)
                                  : "0.00"}
                              </div>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap">
                              <div className="text-sm text-white">
                                {formatCurrency(security.marketCap)}
                              </div>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap">
                              <span
                                className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium text-white ${
                                  SECTOR_COLORS[security.sector] ||
                                  "bg-gray-500"
                                }`}
                                title={
                                  SECTOR_DISPLAY_NAMES[security.sector]
                                    ?.fullName || String(security.sector)
                                }
                              >
                                {SECTOR_DISPLAY_NAMES[security.sector]
                                  ?.abbreviation || String(security.sector)}
                              </span>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap">
                              <div className="text-sm text-slate-400">
                                {security.exchange}
                              </div>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap">
                              <div className="flex items-center space-x-2">
                                <button
                                  onClick={() =>
                                    handleUpdatePrice(security.symbol)
                                  }
                                  className="text-blue-400 hover:text-blue-300 text-sm p-1 rounded hover:bg-slate-600 transition-colors"
                                  title="Update Price"
                                >
                                  <RefreshCw className="h-4 w-4" />
                                </button>
                                <button
                                  onClick={() => {
                                    if (isInWatchlist) {
                                      handleRemoveFromWatchlist(
                                        security.symbol
                                      );
                                    } else {
                                      // Redirect to watchlist page to add this security
                                      window.location.href = `/watchlist?add=${security.symbol}`;
                                    }
                                  }}
                                  className={`text-sm p-1 rounded hover:bg-slate-600 transition-colors ${
                                    isInWatchlist
                                      ? "text-yellow-400"
                                      : "text-slate-400 hover:text-yellow-400"
                                  }`}
                                  title={
                                    isInWatchlist
                                      ? "Remove from Watchlist"
                                      : "Add to Watchlist"
                                  }
                                >
                                  {isInWatchlist ? (
                                    <Star className="h-4 w-4 fill-current" />
                                  ) : (
                                    <StarOff className="h-4 w-4" />
                                  )}
                                </button>
                                <button
                                  onClick={() => setSelectedSecurity(security)}
                                  className="text-slate-400 hover:text-white text-sm p-1 rounded hover:bg-slate-600 transition-colors"
                                  title="View Details"
                                >
                                  <Eye className="h-4 w-4" />
                                </button>
                              </div>
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Trending Stocks */}
            <div className="bg-slate-800/50 rounded-xl p-6 border border-slate-700/50">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-bold text-white">Trending</h3>
                <div className="flex items-center space-x-1">
                  <TrendingUp className="h-5 w-5 text-green-400" />
                  {marketContext?.isMarketOpen && (
                    <div className="w-1.5 h-1.5 bg-green-400 rounded-full animate-pulse"></div>
                  )}
                </div>
              </div>

              {trending.length === 0 ? (
                <div className="text-center py-4">
                  <TrendingUp className="h-8 w-8 text-slate-500 mx-auto mb-2" />
                  <p className="text-slate-400 text-sm">
                    No trending data available
                  </p>
                </div>
              ) : (
                <div className="space-y-3">
                  {trending.slice(0, 10).map((stock, index) => (
                    <div
                      key={`${stock.id ?? stock.symbol}`}
                      className="flex items-center justify-between p-2 hover:bg-slate-700/50 rounded-lg transition-colors cursor-pointer"
                      onClick={() => setSelectedSecurity(stock)}
                    >
                      <div className="flex items-center space-x-2">
                        <span className="text-xs text-slate-500 w-4">
                          #{index + 1}
                        </span>
                        <div>
                          <span className="text-white font-medium text-sm">
                            {stock.symbol}
                          </span>
                          <p className="text-slate-400 text-xs truncate max-w-20">
                            {stock.companyName}
                          </p>
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="text-white text-sm">
                          $
                          {stock.currentPrice != null
                            ? stock.currentPrice.toFixed(2)
                            : "0.00"}
                        </div>
                        <div className="text-xs text-slate-400">
                          {getTimeSinceUpdate(stock.updatedDate)}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Sector Overview */}
            <div className="bg-slate-800/50 rounded-xl p-6 border border-slate-700/50">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-bold text-white">Sectors</h3>
                <PieChart className="h-5 w-5 text-purple-400" />
              </div>

              {sectorOverview.length === 0 ? (
                <div className="text-center py-4">
                  <PieChart className="h-8 w-8 text-slate-500 mx-auto mb-2" />
                  <p className="text-slate-400 text-sm">
                    No sector data available
                  </p>
                </div>
              ) : (
                <div className="space-y-3">
                  {sectorOverview.slice(0, 8).map((sector) => {
                    const maxCount =
                      sectorOverview.length > 0
                        ? Math.max(...sectorOverview.map((s) => s.count))
                        : 1;
                    const percentage = Math.max(
                      0,
                      Math.min(100, (sector.count / (maxCount || 1)) * 100)
                    );

                    return (
                      <div key={String(sector.sector)} className="space-y-2">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center space-x-2">
                            <div
                              className={`w-3 h-3 rounded-full ${
                                SECTOR_COLORS[sector.sector] || "bg-gray-500"
                              }`}
                            ></div>
                            <span className="text-white text-sm font-medium">
                              {SECTOR_DISPLAY_NAMES[sector.sector]
                                ?.abbreviation || String(sector.sector)}
                            </span>
                          </div>
                          <div className="text-right">
                            <span className="text-slate-400 text-sm">
                              {sector.count}
                            </span>
                            <div className="text-xs text-slate-500">
                              ${(sector.averagePrice ?? 0).toFixed(2)} avg
                            </div>
                          </div>
                        </div>
                        <div className="w-full bg-slate-700 rounded-full h-1.5">
                          <div
                            className={`h-1.5 rounded-full ${
                              SECTOR_COLORS[sector.sector] || "bg-gray-500"
                            }`}
                            style={{ width: `${percentage}%` }}
                          ></div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>

            {/* Market Health */}
            {marketStats && (
              <div className="bg-slate-800/50 rounded-xl p-6 border border-slate-700/50">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-bold text-white">
                    System Health
                  </h3>
                  <Activity
                    className={`h-5 w-5 ${
                      marketStats.circuitBreakerOpen
                        ? "text-red-400"
                        : "text-green-400"
                    }`}
                  />
                </div>

                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="text-slate-400 text-sm">Status</span>
                    <span
                      className={`text-sm font-medium flex items-center space-x-1 ${
                        marketStats.circuitBreakerOpen
                          ? "text-red-400"
                          : "text-green-400"
                      }`}
                    >
                      <div
                        className={`w-2 h-2 rounded-full ${
                          marketStats.circuitBreakerOpen
                            ? "bg-red-400"
                            : "bg-green-400"
                        }`}
                      ></div>
                      <span>
                        {marketStats.circuitBreakerOpen ? "Issues" : "Healthy"}
                      </span>
                    </span>
                  </div>

                  <div className="flex items-center justify-between">
                    <span className="text-slate-400 text-sm">
                      Today's Records
                    </span>
                    <span className="text-white text-sm">
                      {marketStats.todayRecords}
                    </span>
                  </div>

                  <div className="flex items-center justify-between">
                    <span className="text-slate-400 text-sm">Yesterday</span>
                    <span className="text-slate-300 text-sm">
                      {marketStats.yesterdayRecords}
                    </span>
                  </div>

                  <div className="flex items-center justify-between">
                    <span className="text-slate-400 text-sm">Failures</span>
                    <span
                      className={`text-sm ${
                        marketStats.consecutiveFailures > 0
                          ? "text-red-400"
                          : "text-green-400"
                      }`}
                    >
                      {marketStats.consecutiveFailures}
                    </span>
                  </div>

                  <div className="pt-2 border-t border-slate-700">
                    <div className="flex items-center justify-between">
                      <span className="text-slate-500 text-xs">
                        Last updated
                      </span>
                      <span className="text-slate-500 text-xs">
                        {new Date(marketStats.timestamp).toLocaleTimeString()}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Quick Actions */}
            <div className="bg-slate-800/50 rounded-xl p-6 border border-slate-700/50">
              <h3 className="text-lg font-bold text-white mb-4">
                Quick Actions
              </h3>

              <div className="space-y-3">
                <Link
                  to="/watchlist"
                  className="flex items-center justify-between p-3 bg-slate-700/50 hover:bg-slate-600/50 rounded-lg transition-colors"
                >
                  <div className="flex items-center space-x-2">
                    <Star className="h-4 w-4 text-yellow-400" />
                    <span className="text-white">View Watchlist</span>
                  </div>
                  <ExternalLink className="h-4 w-4 text-slate-400" />
                </Link>

                <Link
                  to="/dashboard"
                  className="flex items-center justify-between p-3 bg-slate-700/50 hover:bg-slate-600/50 rounded-lg transition-colors"
                >
                  <div className="flex items-center space-x-2">
                    <Home className="h-4 w-4 text-green-400" />
                    <span className="text-white">Dashboard</span>
                  </div>
                  <ExternalLink className="h-4 w-4 text-slate-400" />
                </Link>

                <Link
                  to="/news"
                  className="flex items-center justify-between p-3 bg-slate-700/50 hover:bg-slate-600/50 rounded-lg transition-colors"
                >
                  <div className="flex items-center space-x-2">
                    <Newspaper className="h-4 w-4 text-orange-400" />
                    <span className="text-white">Market News</span>
                  </div>
                  <ExternalLink className="h-4 w-4 text-slate-400" />
                </Link>

                <button
                  onClick={handleRefresh}
                  disabled={loading.refresh}
                  className="w-full flex items-center justify-between p-3 bg-slate-700/50 hover:bg-slate-600/50 disabled:opacity-50 rounded-lg transition-colors"
                >
                  <div className="flex items-center space-x-2">
                    <RefreshCw
                      className={`h-4 w-4 text-cyan-400 ${loading.refresh ? "animate-spin" : ""}`}
                    />
                    <span className="text-white">Refresh Data</span>
                  </div>
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Security Detail Modal */}
        {selectedSecurity && (
          <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
            <div className="bg-slate-800 rounded-xl p-6 max-w-2xl w-full max-h-[90vh] overflow-y-auto border border-slate-700">
              <div className="flex items-center justify-between mb-6">
                <div>
                  <h2 className="text-2xl font-bold text-white">
                    {selectedSecurity.symbol}
                  </h2>
                  <p className="text-slate-400">
                    {selectedSecurity.companyName}
                  </p>
                </div>
                <button
                  onClick={() => setSelectedSecurity(null)}
                  className="p-2 text-slate-400 hover:text-white transition-colors"
                >
                  <X className="h-6 w-6" />
                </button>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Price Information */}
                <div className="space-y-4">
                  <div>
                    <h3 className="text-lg font-semibold text-white mb-2">
                      Price Information
                    </h3>
                    <div className="bg-slate-700/50 rounded-lg p-4 space-y-2">
                      <div className="flex justify-between">
                        <span className="text-slate-400">Current Price</span>
                        <span className="text-white font-semibold text-lg">
                          $
                          {selectedSecurity.currentPrice != null
                            ? selectedSecurity.currentPrice.toFixed(2)
                            : "0.00"}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-400">Market Cap</span>
                        <span className="text-white">
                          {formatCurrency(selectedSecurity.marketCap)}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-400">Last Updated</span>
                        <span className="text-white">
                          {getTimeSinceUpdate(selectedSecurity.updatedDate)}
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* Market Information */}
                  <div>
                    <h3 className="text-lg font-semibold text-white mb-2">
                      Market Information
                    </h3>
                    <div className="bg-slate-700/50 rounded-lg p-4 space-y-2">
                      <div className="flex justify-between">
                        <span className="text-slate-400">Exchange</span>
                        <span className="text-white">
                          {selectedSecurity.exchange}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-400">Sector</span>
                        <span
                          className={`text-white px-2 py-1 rounded text-sm ${
                            SECTOR_COLORS[selectedSecurity.sector] ||
                            "bg-gray-500"
                          }`}
                        >
                          {SECTOR_DISPLAY_NAMES[selectedSecurity.sector]
                            ?.fullName || String(selectedSecurity.sector)}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-400">Security Type</span>
                        <span className="text-white">
                          {selectedSecurity.securityType ?? "—"}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-400">Status</span>
                        <span
                          className={`text-sm ${
                            selectedSecurity.isActive
                              ? "text-green-400"
                              : "text-red-400"
                          }`}
                        >
                          {selectedSecurity.isActive ? "Active" : "Inactive"}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Additional Information */}
                <div className="space-y-4">
                  <div>
                    <h3 className="text-lg font-semibold text-white mb-2">
                      Timeline
                    </h3>
                    <div className="bg-slate-700/50 rounded-lg p-4 space-y-2">
                      <div className="flex justify-between">
                        <span className="text-slate-400">Added</span>
                        <span className="text-white">
                          {selectedSecurity.createdDate
                            ? new Date(
                                selectedSecurity.createdDate
                              ).toLocaleDateString()
                            : "—"}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-400">Last Updated</span>
                        <span className="text-white">
                          {selectedSecurity.updatedDate
                            ? new Date(
                                selectedSecurity.updatedDate
                              ).toLocaleString()
                            : "—"}
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* Actions */}
                  <div>
                    <h3 className="text-lg font-semibold text-white mb-2">
                      Actions
                    </h3>
                    <div className="space-y-2">
                      <button
                        onClick={() => {
                          void handleUpdatePrice(selectedSecurity.symbol);
                          setSelectedSecurity(null);
                        }}
                        className="w-full flex items-center justify-center space-x-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors"
                      >
                        <RefreshCw className="h-4 w-4" />
                        <span>Update Price</span>
                      </button>

                      <button
                        onClick={() => {
                          const isInWatchlist = watchlist.includes(
                            selectedSecurity.symbol
                          );
                          if (isInWatchlist) {
                            handleRemoveFromWatchlist(selectedSecurity.symbol);
                          } else {
                            // Redirect to watchlist page to add this security
                            window.location.href = `/watchlist?add=${selectedSecurity.symbol}`;
                          }
                        }}
                        className={`w-full flex items-center justify-center space-x-2 px-4 py-2 rounded-lg transition-colors ${
                          watchlist.includes(selectedSecurity.symbol)
                            ? "bg-red-600 hover:bg-red-700 text-white"
                            : "bg-yellow-600 hover:bg-yellow-700 text-white"
                        }`}
                      >
                        {watchlist.includes(selectedSecurity.symbol) ? (
                          <>
                            <StarOff className="h-4 w-4" />
                            <span>Remove from Watchlist</span>
                          </>
                        ) : (
                          <>
                            <Star className="h-4 w-4" />
                            <span>Add to Watchlist</span>
                          </>
                        )}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default FunctionalMarketPage;
