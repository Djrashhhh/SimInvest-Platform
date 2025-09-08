// types/watchlist.ts

// Base interfaces matching your backend DTOs
export interface WatchlistSecurity {
  security_id: number;
  symbol: string;
  company_name: string;
  current_price: number | null;
  last_updated: string;
}

export interface WatchlistResponse {
  watchlist_id: number;
  user_id: number;
  name: string;
  description: string | null;
  security_count: number;
  created_at: string;
  updated_at: string;
  securities: WatchlistSecurity[];
}

export interface WatchlistSummary {
  watchlist_id: number;
  name: string;
  description: string | null;
  security_count: number;
  updated_at: string;
}

export interface WatchlistStats {
  total_watchlists: number;
  total_securities: number;
  average_securities_per_watchlist: number;
}

// Request DTOs matching your backend
export interface CreateWatchlistRequest {
  name: string;
  description?: string | null;
  security_ids?: number[];
}

export interface UpdateWatchlistRequest {
  name?: string;
  description?: string | null;
}

// API Response types
export interface WatchlistApiResponse<T> {
  data: T;
  success: boolean;
  message?: string;
}

// Hook return types
export interface UseWatchlistsReturn {
  watchlists: WatchlistSummary[];
  selectedWatchlist: WatchlistResponse | null;
  watchlistStats: WatchlistStats | null;
  loading: {
    watchlists: boolean;
    selectedWatchlist: boolean;
    create: boolean;
    update: boolean;
    delete: boolean;
    addSecurity: boolean;
    removeSecurity: boolean;
  };
  error: string | null;
  // Actions
  loadWatchlists: () => Promise<void>;
  loadWatchlistDetails: (watchlistId: number) => Promise<void>;
  createWatchlist: (request: CreateWatchlistRequest) => Promise<WatchlistResponse>;
  updateWatchlist: (watchlistId: number, request: UpdateWatchlistRequest) => Promise<WatchlistResponse>;
  deleteWatchlist: (watchlistId: number) => Promise<void>;
  addSecurityToWatchlist: (watchlistId: number, symbol: string) => Promise<WatchlistResponse>;
  removeSecurityFromWatchlist: (watchlistId: number, symbol: string) => Promise<WatchlistResponse>;
  clearWatchlist: (watchlistId: number) => Promise<WatchlistResponse>;
  refreshData: () => Promise<void>;
  setSelectedWatchlist: (watchlist: WatchlistResponse | null) => void;
  clearError: () => void;
}

// Search and filter types
export interface SecuritySearchResult {
  securityId: number;
  symbol: string;
  companyName: string;
  currentPrice: number | null;
  sector: string;
  exchange: string;
  marketCap: number | null;
  securityType?: string;
  isActive?: boolean;
}

export interface WatchlistSearchParams {
  query?: string;
  limit?: number;
  page?: number;
}

// Utility types
export type WatchlistAction = 
  | 'create'
  | 'update' 
  | 'delete'
  | 'addSecurity'
  | 'removeSecurity'
  | 'clearSecurities';

export interface WatchlistError {
  type: WatchlistAction;
  message: string;
  watchlistId?: number;
  symbol?: string;
}

// Constants for validation
export const WATCHLIST_CONSTRAINTS = {
  NAME_MAX_LENGTH: 100,
  NAME_MIN_LENGTH: 1,
  DESCRIPTION_MAX_LENGTH: 500,
  MAX_SECURITIES_PER_WATCHLIST: 50, // You can adjust this based on your needs
} as const;

// Helper type for form validation
export interface WatchlistFormData {
  name: string;
  description: string;
  errors: {
    name?: string;
    description?: string;
  };
}

// Export types for external use
export type {
  WatchlistSecurity as Security,
  WatchlistResponse as Watchlist,
  WatchlistSummary as WatchlistPreview,
};