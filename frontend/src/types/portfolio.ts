// types/portfolio.ts - COMPLETE VERSION WITH ALL EXPORTS INCLUDING UPDATED POSITION
export interface Portfolio {
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

export interface PortfolioSummary {
  portfolio_id: number;
  portfolio_name: string;
  total_value: number;
  cash_balance: number;
  invested_amount: number;
  total_gain_loss: number;
  total_gain_loss_percentage: number;
  position_count: number;
  last_updated: string;
}

// UPDATED: Position interface to match backend PositionResponseDTO
export interface Position {
  position_id: number;
  portfolio_id: number;
  portfolio_name: string;
  security_id: number;
  security_symbol: string;
  company_name: string;
  quantity: number;
  avg_cost_per_share: number;
  current_price: number;
  current_value: number;
  cost_basis: number;
  unrealized_gain_loss: number;
  unrealized_gain_loss_percentage: number;
  realized_gain_loss: number;
  total_gain_loss: number;
  day_change: number;
  day_change_percent: number;
  portfolio_weight: number;
  is_active: boolean;
  performance_status: string;
  open_date: string;
  last_updated: string;
  break_even_price: number;
  annualized_return: number;
  holding_period_days: number;
}

export interface CreatePortfolioRequest {
  portfolio_name: string;
  initial_cash_balance: number;
}

export interface UpdatePortfolioRequest {
  portfolio_name?: string;
  is_active?: boolean;
}

export interface OrderRequest {
  portfolio_id: number;
  stock_symbol: string;
  quantity: number;
  order_price?: number;
  order_type: "MARKET" | "LIMIT";
  order_side: "BUY" | "SELL";
  expiry_date?: string;
  notes?: string;
}

export interface Order {
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
  order_status:
    | "PENDING"
    | "FILLED"
    | "CANCELLED"
    | "PARTIALLY_FILLED"
    | "FAILED";
  order_placed_date: string;
  order_executed_date?: string;
  order_cancelled_date?: string;
  expiry_date?: string;
  notes?: string;
  cancellation_reason?: string;
  created_at: string;
  last_updated: string;
  remaining_quantity: number;
  can_be_cancelled: boolean;
  is_fully_filled: boolean;
  is_partially_filled: boolean;
  is_buy_order: boolean;
  is_sell_order: boolean;
}

export interface PortfolioAnalytics {
  portfolio_id: number;
  total_portfolio_value: number;
  total_unrealized_gain_loss: number;
  total_realized_gain_loss: number;
  total_unrealized_gain_loss_percentage: number;
  total_positions: number;
  positions: Position[];
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
  error?: string;
}