// Fixed marketService.ts - Updated to match corrected backend endpoints
import { SecuritySector, Exchange } from "../types/market";
import type {
  SecurityStock,
  MarketStats,
  MarketData,
  ApiResponse,
  FilterOptions,
  MarketContext,
  MarketDataHealthReport,
  PriceAlertInfo,
  SectorOverview,
} from "../types/market";

// Base URL matches your .env configuration
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

console.log("API_BASE_URL:", API_BASE_URL);

// Get auth headers with token
const getAuthHeaders = (): HeadersInit => {
  const token = localStorage.getItem("microinvest_token");
  console.log("Token for request:", token ? "exists" : "missing");
  return {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  };
};

// Handle API responses with proper error handling
const handleApiResponse = async <T>(response: Response): Promise<T> => {
  console.log("Response:", response.status, response.statusText, response.url);

  if (!response.ok) {
    if (response.status === 401) {
      console.error("Unauthorized - redirecting to login");
      localStorage.removeItem("microinvest_token");
      window.location.href = "/login";
      throw new Error("Unauthorized");
    }

    if (response.status === 403) {
      console.error("Forbidden - check authentication");
      throw new Error("Access forbidden - please check your authentication");
    }

    if (response.status === 404) {
      console.error("Endpoint not found:", response.url);
      throw new Error("API endpoint not found");
    }

    let errorMessage = `HTTP ${response.status}: ${response.statusText}`;
    try {
      const errorData = await response.json();
      errorMessage = errorData.details || errorData.error || errorData.message || errorMessage;
      console.error("API Error details:", errorData);
    } catch {
      // If response is not JSON, use status text
    }

    throw new Error(errorMessage);
  }

  const data = await response.json();
  return data;
};

// Securities API calls (matching your SecurityController endpoints)
export const securityService = {
  // Get all active securities
  getActiveSecurities: async (params?: {
    page?: number;
    size?: number;
  }): Promise<SecurityStock[]> => {
    const searchParams = new URLSearchParams();
    if (params?.page !== undefined) searchParams.append("page", params.page.toString());
    if (params?.size !== undefined) searchParams.append("size", params.size.toString());

    const url = `${API_BASE_URL}/api/v1/securities/active?${searchParams.toString()}`;
    console.log("🔍 Getting active securities:", url);

    const response = await fetch(url, {
      headers: getAuthHeaders(),
      mode: "cors",
      credentials: "include",
    });

    return handleApiResponse<SecurityStock[]>(response);
  },

  // Search securities by symbol or company name
  searchSecurities: async (query: string, limit = 10): Promise<SecurityStock[]> => {
    const searchParams = new URLSearchParams({
      q: query,
      limit: limit.toString(),
    });

    const url = `${API_BASE_URL}/api/v1/securities/search?${searchParams.toString()}`;
    console.log("🔍 Searching securities:", url, "query:", query);

    const response = await fetch(url, {
      headers: getAuthHeaders(),
      mode: "cors",
      credentials: "include",
    });

    return handleApiResponse<SecurityStock[]>(response);
  },

  // Create or update security (uses Finnhub data)
  createOrUpdateSecurity: async (symbol: string): Promise<ApiResponse<SecurityStock>> => {
    const url = `${API_BASE_URL}/api/v1/securities/create-or-update/${symbol}`;
    console.log("🚀 Creating/updating security:", url, "symbol:", symbol);

    const response = await fetch(url, {
      method: "POST",
      headers: getAuthHeaders(),
      mode: "cors",
      credentials: "include",
    });

    console.log("📡 Response received:", {
      status: response.status,
      statusText: response.statusText,
      ok: response.ok,
    });

    return handleApiResponse<ApiResponse<SecurityStock>>(response);
  },

  // Get security by symbol
  getSecurityBySymbol: async (symbol: string): Promise<SecurityStock> => {
    const url = `${API_BASE_URL}/api/v1/securities/${symbol}`;
    console.log("🔍 Getting security by symbol:", url);

    const response = await fetch(url, {
      headers: getAuthHeaders(),
      mode: "cors",
      credentials: "include",
    });

    return handleApiResponse<SecurityStock>(response);
  },

  // Get trending stocks (most recently updated)
  getTrendingStocks: async (limit = 10): Promise<SecurityStock[]> => {
    const url = `${API_BASE_URL}/api/v1/securities/trending?limit=${limit}`;
    console.log("📈 Getting trending stocks:", url);

    const response = await fetch(url, {
      headers: getAuthHeaders(),
      mode: "cors",
      credentials: "include",
    });

    return handleApiResponse<SecurityStock[]>(response);
  },

  // Get securities by sector
  getSecuritiesBySector: async (sector: SecuritySector, limit = 50): Promise<SecurityStock[]> => {
    const url = `${API_BASE_URL}/api/v1/securities/by-sector/${sector}?limit=${limit}`;
    console.log("🏭 Getting securities by sector:", url);

    const response = await fetch(url, {
      headers: getAuthHeaders(),
      mode: "cors",
      credentials: "include",
    });

    return handleApiResponse<SecurityStock[]>(response);
  },

  // Get securities by exchange
  getSecuritiesByExchange: async (exchange: Exchange, limit = 50): Promise<SecurityStock[]> => {
    const url = `${API_BASE_URL}/api/v1/securities/by-exchange/${exchange}?limit=${limit}`;
    console.log("🏛️ Getting securities by exchange:", url);

    const response = await fetch(url, {
      headers: getAuthHeaders(),
      mode: "cors",
      credentials: "include",
    });

    return handleApiResponse<SecurityStock[]>(response);
  },

  // Get sector overview with statistics
  getSectorOverview: async (): Promise<SectorOverview[]> => {
    const url = `${API_BASE_URL}/api/v1/securities/sector-overview`;
    console.log("📊 Getting sector overview:", url);

    const response = await fetch(url, {
      headers: getAuthHeaders(),
      mode: "cors",
      credentials: "include",
    });

    return handleApiResponse<SectorOverview[]>(response);
  },

  // Get filter options (available sectors and exchanges)
  getFilterOptions: async (): Promise<FilterOptions> => {
    const url = `${API_BASE_URL}/api/v1/securities/filters`;
    console.log("🔧 Getting filter options:", url);

    const response = await fetch(url, {
      headers: getAuthHeaders(),
      mode: "cors",
      credentials: "include",
    });

    return handleApiResponse<FilterOptions>(response);
  },

  // Get securities statistics
  getSecurityStats: async (): Promise<{
    totalActive: number;
    totalInactive: number;
    bySector: Record<SecuritySector, number>;
    byExchange: Record<Exchange, number>;
    timestamp: string;
  }> => {
    const url = `${API_BASE_URL}/api/v1/securities/stats`;
    console.log("📈 Getting security stats:", url);

    const response = await fetch(url, {
      headers: getAuthHeaders(),
      mode: "cors",
      credentials: "include",
    });

    return handleApiResponse<any>(response);
  },

  // Get price alerts for significant movements
  getPriceAlerts: async (threshold = 5.0): Promise<PriceAlertInfo[]> => {
    const url = `${API_BASE_URL}/api/v1/securities/price-alerts?threshold=${threshold}`;
    console.log("🚨 Getting price alerts:", url);

    const response = await fetch(url, {
      headers: getAuthHeaders(),
      mode: "cors",
      credentials: "include",
    });

    const alerts = await handleApiResponse<any[]>(response);
    return alerts.map((alert) => ({
      symbol: alert.symbol,
      companyName: alert.companyName,
      currentPrice: alert.currentPrice,
      previousPrice: alert.previousPrice || alert.currentPrice,
      changePercentage: alert.changePercentage || 0,
      alertType: alert.alertType || "SIGNIFICANT_GAIN",
    }));
  },

  // Get top securities by market cap
  getTopByMarketCap: async (limit = 20): Promise<SecurityStock[]> => {
    const url = `${API_BASE_URL}/api/v1/securities/top-by-market-cap?limit=${limit}`;
    console.log("🏆 Getting top securities by market cap:", url);

    const response = await fetch(url, {
      headers: getAuthHeaders(),
      mode: "cors",
      credentials: "include",
    });

    return handleApiResponse<SecurityStock[]>(response);
  },
};

// Market Data API calls - FIXED: Updated endpoints to match corrected backend
export const marketDataService = {
  // Update price for a symbol (triggers Finnhub fetch)
  updatePrice: async (symbol: string): Promise<ApiResponse<any>> => {
    // FIXED: Changed from /api/v1/market-data to /api/market-data
    const url = `${API_BASE_URL}/api/market-data/update-price/${symbol}`;
    console.log("💰 Updating price:", url, "symbol:", symbol);

    const response = await fetch(url, {
      method: "POST",
      headers: getAuthHeaders(),
      mode: "cors",
      credentials: "include",
    });

    return handleApiResponse<ApiResponse<any>>(response);
  },

  // Fetch market data for a symbol
  fetchMarketData: async (symbol: string): Promise<ApiResponse<MarketData>> => {
    // FIXED: Changed endpoint path
    const url = `${API_BASE_URL}/api/market-data/fetch-data/${symbol}`;
    console.log("📊 Fetching market data:", url);

    const response = await fetch(url, {
      method: "POST",
      headers: getAuthHeaders(),
      mode: "cors",
      credentials: "include",
    });

    return handleApiResponse<ApiResponse<MarketData>>(response);
  },

  // Bulk update prices for multiple symbols
  bulkUpdatePrices: async (symbols: string[]): Promise<ApiResponse<any>> => {
    // FIXED: Changed endpoint path
    const url = `${API_BASE_URL}/api/market-data/bulk-update`;
    console.log("🔄 Bulk updating prices:", url, "symbols:", symbols);

    const response = await fetch(url, {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify(symbols),
      mode: "cors",
      credentials: "include",
    });

    return handleApiResponse<ApiResponse<any>>(response);
  },

  // Get system health report
  getHealthReport: async (): Promise<MarketDataHealthReport> => {
    // FIXED: Changed endpoint path
    const url = `${API_BASE_URL}/api/market-data/health`;
    console.log("🏥 Getting health report:", url);

    const response = await fetch(url, {
      headers: getAuthHeaders(),
      mode: "cors",
      credentials: "include",
    });

    return handleApiResponse<MarketDataHealthReport>(response);
  },

  // Get system statistics
  getSystemStats: async (): Promise<MarketStats> => {
    // FIXED: Changed endpoint path
    const url = `${API_BASE_URL}/api/market-data/stats`;
    console.log("📊 Getting system stats:", url);

    const response = await fetch(url, {
      headers: getAuthHeaders(),
      mode: "cors",
      credentials: "include",
    });

    return handleApiResponse<MarketStats>(response);
  },

  // Get market status (open/closed)
  getMarketStatus: async (): Promise<MarketContext> => {
    // FIXED: Changed endpoint path
    const url = `${API_BASE_URL}/api/market-data/market-status`;
    console.log("🕐 Getting market status:", url);

    const response = await fetch(url, {
      headers: getAuthHeaders(),
      mode: "cors",
      credentials: "include",
    });

    const data = await handleApiResponse<any>(response);
    return {
      isMarketOpen: data.isMarketHours,
      nextMarketOpen: data.nextMarketDay,
      marketTimezone: "America/New_York",
      lastUpdateTime: data.currentTimeEST,
    };
  },

  // Get historical prices
  getHistoricalPrices: async (
    symbol: string,
    from: string,
    to: string
  ): Promise<{
    symbol: string;
    fromDate: string;
    toDate: string;
    recordCount: number;
    data: MarketData[];
  }> => {
    const searchParams = new URLSearchParams({ from, to });
    // FIXED: Changed endpoint path
    const url = `${API_BASE_URL}/api/market-data/${symbol}/history?${searchParams.toString()}`;
    console.log("📈 Getting historical prices:", url);

    const response = await fetch(url, {
      headers: getAuthHeaders(),
      mode: "cors",
      credentials: "include",
    });

    return handleApiResponse<any>(response);
  },

  // Test connectivity
  testConnectivity: async (): Promise<{
    status: string;
    responseTimeMs: number;
    testSymbol: string;
    currentPrice: number;
    timestamp: string;
  }> => {
    // FIXED: Changed endpoint path
    const url = `${API_BASE_URL}/api/market-data/admin/test-connectivity`;
    console.log("🔌 Testing connectivity:", url);

    const response = await fetch(url, {
      headers: getAuthHeaders(),
      mode: "cors",
      credentials: "include",
    });

    return handleApiResponse<any>(response);
  },
};

// Combined market service
export const marketService = {
  ...securityService,
  ...marketDataService,
};