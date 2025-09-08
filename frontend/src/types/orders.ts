// types/orders.ts
// Order and Transaction related types matching your backend DTOs

export interface OrderRequestDTO {
  portfolio_id: number;
  stock_symbol: string;
  quantity: number;
  order_price?: number;
  order_type: "MARKET" | "LIMIT";
  order_side: "BUY" | "SELL";
  expiry_date?: string;
  notes?: string;
}

export interface OrderResponseDTO {
  order_id: number;
  portfolio_id: number;
  portfolio_name: string;
  stock_symbol: string;
  company_name: string;
  quantity: number;
  order_price?: number;
  estimated_total: number;
  filled_quantity: number;
  average_fill_price?: number;
  total_fees: number;
  order_type: "MARKET" | "LIMIT";
  order_side: "BUY" | "SELL";
  order_status: "PENDING" | "FILLED" | "CANCELLED" | "PARTIALLY_FILLED" | "FAILED" | "REJECTED" | "EXPIRED";
  order_placed_date: string;
  order_executed_date?: string;
  order_cancelled_date?: string;
  expiry_date?: string;
  notes?: string;
  cancellation_reason?: string;
  remaining_quantity: number;
  can_be_cancelled: boolean;
  is_buy_order: boolean;
  is_sell_order: boolean;
  created_at: string;
  last_updated: string;
}

export interface TransactionResponseDTO {
  transaction_id: number;
  portfolio_id: number;
  portfolio_name: string;
  stock_symbol: string;
  company_name: string;
  order_id?: number;
  quantity: number;
  price_per_share: number;
  total_amount: number;
  fees: number;
  tax_amount: number;
  net_amount: number;
  transaction_date: string;
  settlement_date?: string;
  notes?: string;
  transaction_type: "BUY" | "SELL" | "DIVIDEND" | "INTEREST" | "DEPOSIT" | "WITHDRAWAL" | "FEE" | "TAX";
  order_type?: "MARKET" | "LIMIT";
  transaction_status: "PENDING" | "COMPLETED" | "FAILED" | "CANCELED";
  is_settled: boolean;
  affects_portfolio_balance: boolean;
  affects_position: boolean;
  created_at: string;
  last_updated: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  error?: string;
}

// Portfolio related types (for completeness)
export interface PortfolioResponseDTO {
  portfolio_id: number;
  user_id: number;
  portfolio_name: string;
  total_value: number;
  cash_balance: number;
  created_date: string;
  last_updated: string;
  is_active: boolean;
  position_count: number;
  total_unrealized_gain_loss: number;
  total_realized_gain_loss: number;
}

// Order statistics and analytics types
export interface OrderStats {
  total_orders: number;
  pending_orders: number;
  filled_orders: number;
  cancelled_orders: number;
  total_buy_orders: number;
  total_sell_orders: number;
  average_order_size: number;
  total_order_value: number;
}

export interface TransactionAnalytics {
  total_transactions: number;
  total_buy_transactions: number;
  total_sell_transactions: number;
  total_dividends_received: number;
  total_fees_paid: number;
  total_volume_traded: number;
  average_transaction_size: number;
  net_trading_amount: number;
}

// Cost basis information
export interface CostBasisInfo {
  stock_symbol: string;
  total_quantity_bought: number;
  total_quantity_sold: number;
  current_holding: number;
  average_cost_basis: number;
  total_cost: number;
  realized_gain_loss: number;
}

// Portfolio validation response
export interface PortfolioValidation {
  hasActivePortfolio: boolean;
  portfolio_id?: number;
}

// Hook return types for better type safety
export interface UseOrdersTransactionsReturn {
  // Data
  orders: OrderResponseDTO[];
  transactions: TransactionResponseDTO[];
  activeOrders: OrderResponseDTO[];
  recentTransactions: TransactionResponseDTO[];
  portfolioId: number | null;
  hasPortfolio: boolean;
  orderStats: OrderStats;
  transactionAnalytics: TransactionAnalytics;
  
  // State
  isLoading: boolean;
  error: string | null;
  
  // Actions
  createOrder: (orderRequest: OrderRequestDTO) => Promise<ApiResponse<OrderResponseDTO>>;
  cancelOrder: (orderId: number, reason?: string) => Promise<ApiResponse<OrderResponseDTO>>;
  executeOrder: (orderId: number) => Promise<ApiResponse<OrderResponseDTO>>;
  validateOrder: (orderRequest: OrderRequestDTO) => Promise<ApiResponse<any>>;
  getTransactionsForOrder: (orderId: number) => Promise<TransactionResponseDTO[]>;
  getCostBasisInfo: (stockSymbol: string) => Promise<CostBasisInfo>;
  refreshData: () => Promise<void>;
  loadAllData: () => Promise<void>;
  
  // Filters
  getOrdersByStatus: (status: string) => OrderResponseDTO[];
  getOrdersBySide: (side: 'BUY' | 'SELL') => OrderResponseDTO[];
  getTransactionsByStatus: (status: string) => TransactionResponseDTO[];
  getTransactionsByType: (type: string) => TransactionResponseDTO[];
  
  // Utility
  setError: (error: string | null) => void;
}

export interface UseOrdersReturn {
  orders: OrderResponseDTO[];
  activeOrders: OrderResponseDTO[];
  isSubmitting: boolean;
  error: string | null;
  submitOrder: (orderRequest: OrderRequestDTO) => Promise<ApiResponse<OrderResponseDTO>>;
  cancelOrder: (orderId: number, reason?: string) => Promise<ApiResponse<OrderResponseDTO>>;
  refreshOrders: () => void;
  setError: (error: string | null) => void;
}