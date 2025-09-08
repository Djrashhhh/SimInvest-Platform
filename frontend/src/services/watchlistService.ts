// services/watchlistService.ts

import type {
  WatchlistResponse,
  WatchlistSummary,
  WatchlistStats,
  CreateWatchlistRequest,
  UpdateWatchlistRequest,
  SecuritySearchResult,
  WatchlistSearchParams,
} from "../types/watchlist";

class WatchlistService {
  private baseURL: string;
  private getAuthHeaders: () => Record<string, string>;

  constructor() {
    // Fixed: Use import.meta.env instead of process.env to match your marketService pattern
    this.baseURL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
    this.getAuthHeaders = () => {
      const token = localStorage.getItem("microinvest_token");
      return {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      };
    };
  }

  // Helper method for API calls with automatic /api/v1 prefix
  private async apiCall<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    // Add /api/v1 prefix to endpoint if not already present
    const normalizedEndpoint = endpoint.startsWith("/api/v1")
      ? endpoint
      : `/api/v1${endpoint}`;
    const url = `${this.baseURL}${normalizedEndpoint}`;

    const response = await fetch(url, {
      ...options,
      headers: {
        ...this.getAuthHeaders(),
        ...options.headers,
      },
    });

    if (!response.ok) {
      let errorMessage = `HTTP error! status: ${response.status}`;
      try {
        const errorData = await response.json();
        errorMessage = errorData.message || errorMessage;
      } catch {
        // Use default error message if JSON parsing fails
      }
      throw new Error(errorMessage);
    }

    // Handle 204 No Content responses
    if (response.status === 204) {
      return {} as T;
    }

    return response.json();
  }

  // Watchlist CRUD operations
  async getUserWatchlists(userId: number): Promise<WatchlistSummary[]> {
    return this.apiCall<WatchlistSummary[]>(`/watchlists?userId=${userId}`);
  }

  async getWatchlistById(
    watchlistId: number,
    userId: number
  ): Promise<WatchlistResponse> {
    return this.apiCall<WatchlistResponse>(
      `/watchlists/${watchlistId}?userId=${userId}`
    );
  }

  async createWatchlist(
    userId: number,
    request: CreateWatchlistRequest
  ): Promise<WatchlistResponse> {
    return this.apiCall<WatchlistResponse>(`/watchlists?userId=${userId}`, {
      method: "POST",
      body: JSON.stringify(request),
    });
  }

  async updateWatchlist(
    watchlistId: number,
    userId: number,
    request: UpdateWatchlistRequest
  ): Promise<WatchlistResponse> {
    return this.apiCall<WatchlistResponse>(
      `/watchlists/${watchlistId}?userId=${userId}`,
      {
        method: "PUT",
        body: JSON.stringify(request),
      }
    );
  }

  async deleteWatchlist(watchlistId: number, userId: number): Promise<void> {
    return this.apiCall<void>(`/watchlists/${watchlistId}?userId=${userId}`, {
      method: "DELETE",
    });
  }

  // Security management
  async addSecurityToWatchlist(
    watchlistId: number,
    userId: number,
    symbol: string
  ): Promise<WatchlistResponse> {
    return this.apiCall<WatchlistResponse>(
      `/watchlists/${watchlistId}/securities?userId=${userId}&symbol=${encodeURIComponent(symbol)}`,
      { method: "POST" }
    );
  }

  async removeSecurityFromWatchlist(
    watchlistId: number,
    userId: number,
    symbol: string
  ): Promise<WatchlistResponse> {
    return this.apiCall<WatchlistResponse>(
      `/watchlists/${watchlistId}/securities?userId=${userId}&symbol=${encodeURIComponent(symbol)}`,
      { method: "DELETE" }
    );
  }

  async clearWatchlist(
    watchlistId: number,
    userId: number
  ): Promise<WatchlistResponse> {
    return this.apiCall<WatchlistResponse>(
      `/watchlists/${watchlistId}/securities/all?userId=${userId}`,
      { method: "DELETE" }
    );
  }

  // Statistics and metadata
  async getWatchlistStats(userId: number): Promise<WatchlistStats> {
    return this.apiCall<WatchlistStats>(
      `/watchlists/statistics?userId=${userId}`
    );
  }

  async getUserWatchlistCount(
    userId: number
  ): Promise<{ watchlistCount: number }> {
    return this.apiCall<{ watchlistCount: number }>(
      `/watchlists/count?userId=${userId}`
    );
  }

  // Utility methods
  async getWatchlistSymbols(
    watchlistId: number,
    userId: number
  ): Promise<string[]> {
    return this.apiCall<string[]>(
      `/watchlists/${watchlistId}/symbols?userId=${userId}`
    );
  }

  async getWatchlistsContainingSymbol(
    userId: number,
    symbol: string
  ): Promise<WatchlistSummary[]> {
    return this.apiCall<WatchlistSummary[]>(
      `/watchlists/containing-security?userId=${userId}&symbol=${encodeURIComponent(symbol)}`
    );
  }

  async isSecurityInUserWatchlists(
    userId: number,
    symbol: string
  ): Promise<{ exists: boolean }> {
    return this.apiCall<{ exists: boolean }>(
      `/watchlists/contains-symbol?userId=${userId}&symbol=${encodeURIComponent(symbol)}`
    );
  }

  async getUserDefaultWatchlist(userId: number): Promise<WatchlistResponse> {
    return this.apiCall<WatchlistResponse>(
      `/watchlists/default?userId=${userId}`
    );
  }

  async validateWatchlistOwnership(
    watchlistId: number,
    userId: number
  ): Promise<{ isOwner: boolean }> {
    return this.apiCall<{ isOwner: boolean }>(
      `/watchlists/${watchlistId}/validate-ownership?userId=${userId}`
    );
  }

  // Security search (integration with market service)
  async searchSecurities(
    params: WatchlistSearchParams
  ): Promise<SecuritySearchResult[]> {
    const queryParams = new URLSearchParams();
    // FIXED: Use 'q' parameter and correct endpoint to match your marketService
    if (params.query) queryParams.append("q", params.query); // Changed from 'query' to 'q'
    if (params.limit) queryParams.append("limit", params.limit.toString());
    if (params.page) queryParams.append("page", params.page.toString());

    // FIXED: Use /securities/search instead of /market/securities/search
    return this.apiCall<SecuritySearchResult[]>(
      `/securities/search?${queryParams}`
    );
  }

  // Batch operations
  async addMultipleSecurities(
    watchlistId: number,
    userId: number,
    symbols: string[]
  ): Promise<WatchlistResponse> {
    // Since your backend handles one symbol at a time, we'll do sequential calls
    let updatedWatchlist: WatchlistResponse | null = null;

    for (const symbol of symbols) {
      try {
        updatedWatchlist = await this.addSecurityToWatchlist(
          watchlistId,
          userId,
          symbol
        );
      } catch (error) {
        console.warn(`Failed to add ${symbol} to watchlist:`, error);
        // Continue with other symbols even if one fails
      }
    }

    if (!updatedWatchlist) {
      throw new Error("Failed to add any securities to watchlist");
    }

    return updatedWatchlist;
  }

  async removeMultipleSecurities(
    watchlistId: number,
    userId: number,
    symbols: string[]
  ): Promise<WatchlistResponse> {
    let updatedWatchlist: WatchlistResponse | null = null;

    for (const symbol of symbols) {
      try {
        updatedWatchlist = await this.removeSecurityFromWatchlist(
          watchlistId,
          userId,
          symbol
        );
      } catch (error) {
        console.warn(`Failed to remove ${symbol} from watchlist:`, error);
      }
    }

    if (!updatedWatchlist) {
      throw new Error("Failed to remove any securities from watchlist");
    }

    return updatedWatchlist;
  }

  // Data transformation helpers
  transformSecurityToWatchlistSecurity(security: SecuritySearchResult): {
    security_id: number;
    symbol: string;
    company_name: string;
    current_price: number | null;
    last_updated: string;
  } {
    return {
      security_id: security.securityId,
      symbol: security.symbol,
      company_name: security.companyName,
      current_price: security.currentPrice,
      last_updated: new Date().toISOString(), // Fallback timestamp
    };
  }

  // Local storage helpers for caching
  private getCacheKey(userId: number, suffix: string): string {
    return `watchlist_${userId}_${suffix}`;
  }

  cacheWatchlists(userId: number, watchlists: WatchlistSummary[]): void {
    try {
      const cacheData = {
        data: watchlists,
        timestamp: Date.now(),
      };
      localStorage.setItem(
        this.getCacheKey(userId, "list"),
        JSON.stringify(cacheData)
      );
    } catch (error) {
      console.warn("Failed to cache watchlists:", error);
    }
  }

  getCachedWatchlists(
    userId: number,
    maxAgeMs: number = 5 * 60 * 1000
  ): WatchlistSummary[] | null {
    try {
      const cached = localStorage.getItem(this.getCacheKey(userId, "list"));
      if (!cached) return null;

      const cacheData = JSON.parse(cached);
      const age = Date.now() - cacheData.timestamp;

      if (age > maxAgeMs) {
        localStorage.removeItem(this.getCacheKey(userId, "list"));
        return null;
      }

      return cacheData.data;
    } catch (error) {
      console.warn("Failed to retrieve cached watchlists:", error);
      return null;
    }
  }

  clearCache(userId: number): void {
    try {
      const keys = Object.keys(localStorage).filter((key) =>
        key.startsWith(`watchlist_${userId}_`)
      );
      keys.forEach((key) => localStorage.removeItem(key));
    } catch (error) {
      console.warn("Failed to clear watchlist cache:", error);
    }
  }

  // Error handling helpers
  handleApiError(error: unknown): string {
    if (error instanceof Error) {
      // Handle specific error types
      if (error.message.includes("401")) {
        return "Authentication failed. Please log in again.";
      }
      if (error.message.includes("403")) {
        return "You do not have permission to perform this action.";
      }
      if (error.message.includes("404")) {
        return "Watchlist not found or you do not have access to it.";
      }
      if (error.message.includes("409")) {
        return "A watchlist with this name already exists.";
      }
      return error.message;
    }
    return "An unexpected error occurred. Please try again.";
  }

  // Health check
  async healthCheck(): Promise<boolean> {
    try {
      await fetch(`${this.baseURL}/api/v1/health`, {
        method: "GET",
        headers: this.getAuthHeaders(),
      });
      return true;
    } catch {
      return false;
    }
  }
}

// Export singleton instance
export const watchlistService = new WatchlistService();
export default watchlistService;
