// market.ts - Types and interfaces for market data
export enum SecuritySector {
  TECHNOLOGY = "TECHNOLOGY",
  HEALTHCARE = "HEALTHCARE",
  FINANCIALS = "FINANCIALS",
  CONSUMER_DISCRETIONARY = "CONSUMER_DISCRETIONARY",
  CONSUMER_STAPLES = "CONSUMER_STAPLES",
  COMMUNICATION_SERVICES = "COMMUNICATION_SERVICES",
  INDUSTRIALS = "INDUSTRIALS",
  ENERGY = "ENERGY",
  MATERIALS = "MATERIALS",
  UTILITIES = "UTILITIES",
  REAL_ESTATE = "REAL_ESTATE",
}

export enum Exchange {
  NYSE = "NYSE",
  NASDAQ = "NASDAQ",
  TSX = "TSX",
  LSE = "LSE",
  HKEX = "HKEX",
  JPX = "JPX",
  SSE = "SSE",
  SZSE = "SZSE",
}

export enum SecurityType {
  STOCK = "STOCK",
  BOND = "BOND",
  MUTUAL_FUND = "MUTUAL_FUND",
  ETF = "ETF",
  CRYPTOCURRENCY = "CRYPTOCURRENCY",
}

// Security/Stock Types
export interface SecurityStock {
  id: number;
  symbol: string;
  companyName: string;
  currentPrice: number;
  sector: SecuritySector;
  exchange: Exchange;
  securityType: SecurityType;
  marketCap: number;
  isActive: boolean;
  createdDate: string;
  updatedDate: string;
}

export interface SecuritySummaryDTO {
  symbol: string;
  companyName: string;
  currentPrice: number;
  sector: SecuritySector;
  exchange: Exchange;
}

// Finnhub DTOs
export interface FinnhubQuoteDTO {
  currentPrice: number; // 'c' in API
  highPrice: number; // 'h' in API
  lowPrice: number; // 'l' in API
  openPrice: number; // 'o' in API
  previousClose: number; // 'pc' in API
  timestamp: number; // 't' in API
}

export interface FinnhubCompanyProfileDTO {
  companyName: string; // 'name' in API
  symbol: string; // 'ticker' in API
  exchange: string;
  industry: string; // 'finnhubIndustry' in API
  subIndustry: string; // 'gicsSubIndustry' in API
  sector: string; // 'gicsSector' in API
  marketCap: number; // 'marketCapitalization' in API
  country: string;
  currency: string;
  website: string; // 'weburl' in API
  logoUrl: string; // 'logo' in API
}

export interface FinnhubCandleDTO {
  closePrices: number[]; // 'c' in API
  highPrices: number[]; // 'h' in API
  lowPrices: number[]; // 'l' in API
  openPrices: number[]; // 'o' in API
  timestamps: number[]; // 't' in API
  volumes: number[]; // 'v' in API
  status: string; // 's' in API
}

// Market Data Types
export interface MarketData {
  id: number;
  symbol: string;
  marketDate: string;
  openPrice: number;
  highPrice: number;
  lowPrice: number;
  closePrice: number;
  volume: number;
  createdDate: string;
  updatedDate: string;
}

export interface PriceHistory {
  id: number;
  symbol: string;
  date: string;
  openPrice: number;
  highPrice: number;
  lowPrice: number;
  closePrice: number;
  volume: number;
}

// API Response Types
export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  error?: string;
  details?: string;
  timestamp: string;
}

// Generic API response for endpoints that return data directly
export interface ApiSuccessResponse<T> extends ApiResponse<T> {
  success: true;
  data: T;
}

export interface ApiErrorResponse extends ApiResponse<never> {
  success: false;
  error: string;
  details?: string;
}

export interface MarketDataHealthReport {
  healthy: boolean;
  healthScore: number;
  summary: string;
  lastUpdateTime: string;
  issues: string[];
  recommendations: string[];
}

export interface DataCoverageReport {
  fromDate: string;
  toDate: string;
  totalSecurities: number;
  coveragePercentage: number;
  missingData: string[];
}

export interface PriceAlertInfo {
  symbol: string;
  companyName: string;
  currentPrice: number;
  previousPrice: number;
  changePercentage: number;
  alertType: "SIGNIFICANT_GAIN" | "SIGNIFICANT_LOSS" | "HIGH_VOLUME";
}

// Market Page Specific Types
export interface MarketFilter {
  sector?: SecuritySector;
  exchange?: Exchange;
  priceMin?: number;
  priceMax?: number;
  marketCapMin?: number;
  marketCapMax?: number;
  searchQuery?: string;
}

export interface SortOption {
  field:
    | "symbol"
    | "companyName"
    | "currentPrice"
    | "marketCap"
    | "sector"
    | "exchange";
  direction: "asc" | "desc";
}

export interface MarketOverview {
  totalSecurities: number;
  totalActive: number;
  totalInactive: number;
  bySector: Record<SecuritySector, number>;
  byExchange: Record<Exchange, number>;
  timestamp: string;
}

export interface SectorOverview {
  sector: SecuritySector;
  sectorName: string;
  count: number;
  averagePrice: number;
  totalMarketCap: number;
}

export interface FilterOptions {
  sectors: Record<string, string>;
  exchanges: Record<string, string>;
}

// Search and Discovery Types
export interface TrendingStock extends SecurityStock {
  priceChange?: number;
  priceChangePercentage?: number;
  volume?: number;
}

export interface MarketStats {
  activeSecurities: number;
  todayRecords: number;
  yesterdayRecords: number;
  circuitBreakerOpen: boolean;
  consecutiveFailures: number;
  timestamp: string;
}

// Chart and Visualization Types
export interface ChartDataPoint {
  date: string;
  price: number;
  volume?: number;
}

export interface PriceMovement {
  current: number;
  previous: number;
  change: number;
  changePercentage: number;
  direction: "up" | "down" | "neutral";
}

// Error and Loading States
export interface MarketError {
  code: string;
  message: string;
  details?: any;
}

export interface LoadingState {
  securities: boolean;
  search: boolean;
  filters: boolean;
  trending: boolean;
  sectorData: boolean;
  marketData: boolean;
}

// Utility Types
export type TimeRange = "1D" | "1W" | "1M" | "3M" | "6M" | "1Y" | "YTD" | "ALL";

export interface MarketContext {
  isMarketOpen: boolean;
  nextMarketOpen: string;
  marketTimezone: string;
  lastUpdateTime: string;
}

// Hook Return Types
export interface UseMarketDataReturn {
  // Data
  securities: SecurityStock[];
  trending: TrendingStock[];
  sectorOverview: SectorOverview[];
  marketStats: MarketStats | null;
  marketContext: MarketContext | null;
  priceAlerts: PriceAlertInfo[];
  filterOptions: FilterOptions | null;

  // Loading states
  loading: LoadingState;

  // Error states
  error: MarketError | null;

  // Actions
  searchSecurities: (query: string) => Promise<SecurityStock[]>;
  addSecurity: (symbol: string) => Promise<void>;
  updatePrice: (symbol: string) => Promise<void>;
  fetchHistoricalData: (
    symbol: string,
    from: string,
    to: string
  ) => Promise<MarketData[]>;

  // Filters and sorting
  applyFilter: (filter: MarketFilter) => void;
  applySort: (sort: SortOption) => void;
  clearFilters: () => void;

  // Real-time updates
  startRealTimeUpdates: () => void;
  stopRealTimeUpdates: () => void;

  // Refresh
  refreshData: () => Promise<void>;
}

// Sector display mapping (matching Java enum)
export const SECTOR_DISPLAY_NAMES: Record<
  SecuritySector,
  { fullName: string; abbreviation: string }
> = {
  [SecuritySector.TECHNOLOGY]: { fullName: "Technology", abbreviation: "Tech" },
  [SecuritySector.HEALTHCARE]: {
    fullName: "Healthcare",
    abbreviation: "Health",
  },
  [SecuritySector.FINANCIALS]: { fullName: "Financials", abbreviation: "Fin" },
  [SecuritySector.CONSUMER_DISCRETIONARY]: {
    fullName: "Consumer Discretionary",
    abbreviation: "Discretion",
  },
  [SecuritySector.CONSUMER_STAPLES]: {
    fullName: "Consumer Staples",
    abbreviation: "Staples",
  },
  [SecuritySector.COMMUNICATION_SERVICES]: {
    fullName: "Communication Services",
    abbreviation: "Comm",
  },
  [SecuritySector.INDUSTRIALS]: {
    fullName: "Industrials",
    abbreviation: "Ind",
  },
  [SecuritySector.ENERGY]: { fullName: "Energy", abbreviation: "EN" },
  [SecuritySector.MATERIALS]: { fullName: "Materials", abbreviation: "Mat" },
  [SecuritySector.UTILITIES]: { fullName: "Utilities", abbreviation: "Util" },
  [SecuritySector.REAL_ESTATE]: { fullName: "Real Estate", abbreviation: "RE" },
};

// Exchange timezone mapping (matching Java enum)
export const EXCHANGE_TIMEZONES: Record<Exchange, string> = {
  [Exchange.NYSE]: "America/New_York",
  [Exchange.NASDAQ]: "America/New_York",
  [Exchange.TSX]: "America/Toronto",
  [Exchange.LSE]: "Europe/London",
  [Exchange.HKEX]: "Asia/Hong_Kong",
  [Exchange.JPX]: "Asia/Tokyo",
  [Exchange.SSE]: "Asia/Shanghai",
  [Exchange.SZSE]: "Asia/Shanghai",
};
