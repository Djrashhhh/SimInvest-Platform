// hooks/useOrdersTransactions.ts - CORRECTED VERSION
import { useState, useEffect, useCallback } from "react";
import { useAuth } from "../contexts/AuthContext";
import { orderTransactionService } from "../services/orderTransactionService";

// Import all types from the types file, not the service file
import type { 
  OrderRequestDTO,
  OrderResponseDTO,
  TransactionResponseDTO,
  ApiResponse,
  PortfolioResponseDTO,
  OrderStats,
  TransactionAnalytics,
  CostBasisInfo,
  UseOrdersTransactionsReturn,
  UseOrdersReturn
} from "../types/orders";

export const useOrdersTransactions = (): UseOrdersTransactionsReturn => {
  const { user, isAuthenticated, isLoading: authLoading } = useAuth();

  // FIXED: Use user_id consistently (matching backend)
  console.log("=== ORDERS TRANSACTIONS HOOK DEBUG ===");
  console.log("Auth user:", user);
  console.log("User ID:", user?.user_id);
  console.log("User ID type:", typeof user?.user_id);
  console.log("Is authenticated:", isAuthenticated);
  console.log("Auth loading:", authLoading);

  // State management
  const [orders, setOrders] = useState<OrderResponseDTO[]>([]);
  const [transactions, setTransactions] = useState<TransactionResponseDTO[]>([]);
  const [activeOrders, setActiveOrders] = useState<OrderResponseDTO[]>([]);
  const [recentTransactions, setRecentTransactions] = useState<TransactionResponseDTO[]>([]);
  const [portfolioId, setPortfolioId] = useState<number | null>(null);
  const [hasPortfolio, setHasPortfolio] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [orderStats, setOrderStats] = useState<OrderStats>({} as OrderStats);
  const [transactionAnalytics, setTransactionAnalytics] = useState<TransactionAnalytics>({} as TransactionAnalytics);

  // Check if user has portfolio and get portfolio ID
  const checkPortfolio = useCallback(async () => {
    if (!user?.user_id) {
      console.log('No user_id available for portfolio check');
      return null;
    }

    try {
      console.log('Checking portfolio for user:', user.user_id);
      const result = await orderTransactionService.hasActivePortfolio(user.user_id);
      setHasPortfolio(result.hasActivePortfolio);

      if (result.hasActivePortfolio) {
        const id = await orderTransactionService.getDefaultPortfolioId(user.user_id);
        setPortfolioId(id);
        return id;
      }
      return null;
    } catch (error) {
      console.error('Error checking portfolio:', error);
      setError('Failed to check portfolio status');
      return null;
    }
  }, [user?.user_id]);

  // Load all orders for the portfolio
  const loadOrders = useCallback(async (pId: number) => {
    try {
      console.log('Loading orders for portfolio:', pId);
      const [allOrders, activeOrdersList] = await Promise.all([
        orderTransactionService.getOrdersByPortfolio(pId),
        orderTransactionService.getActiveOrdersByPortfolio(pId)
      ]);

      setOrders(allOrders);
      setActiveOrders(activeOrdersList);
      
      // Load order statistics
      const stats = await orderTransactionService.getOrderStatsByPortfolio(pId);
      setOrderStats(stats);
    } catch (error) {
      console.error('Error loading orders:', error);
      setError('Failed to load orders');
    }
  }, []);

  // Load all transactions for the portfolio
  const loadTransactions = useCallback(async (pId: number) => {
    try {
      console.log('Loading transactions for portfolio:', pId);
      const [allTransactions, recentTransactionsList] = await Promise.all([
        orderTransactionService.getTransactionsByPortfolio(pId),
        orderTransactionService.getRecentTransactions(pId, 10)
      ]);

      setTransactions(allTransactions);
      setRecentTransactions(recentTransactionsList);

      // Load transaction analytics
      const analytics = await orderTransactionService.getTransactionAnalytics(pId);
      setTransactionAnalytics(analytics);
    } catch (error) {
      console.error('Error loading transactions:', error);
      setError('Failed to load transactions');
    }
  }, []);

  // Load all data
  const loadAllData = useCallback(async () => {
    if (!user?.user_id || user.user_id <= 0) {
      console.log('Invalid user ID:', user?.user_id);
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const pId = await checkPortfolio();
      
      if (pId) {
        await Promise.all([
          loadOrders(pId),
          loadTransactions(pId)
        ]);
      } else {
        console.log('No active portfolio found for user');
        // Clear all data if no portfolio
        setOrders([]);
        setTransactions([]);
        setActiveOrders([]);
        setRecentTransactions([]);
        setOrderStats({} as OrderStats);
        setTransactionAnalytics({} as TransactionAnalytics);
      }
    } catch (error) {
      console.error('Error loading data:', error);
      setError('Failed to load portfolio data');
    } finally {
      setIsLoading(false);
    }
  }, [user?.user_id, checkPortfolio, loadOrders, loadTransactions]);

  // Create a new order
  const createOrder = useCallback(async (orderRequest: OrderRequestDTO): Promise<ApiResponse<OrderResponseDTO>> => {
    try {
      setError(null);
      
      // Ensure portfolio ID is set
      if (!portfolioId) {
        throw new Error('No active portfolio found');
      }

      // Set portfolio ID if not provided
      const requestWithPortfolio = {
        ...orderRequest,
        portfolio_id: orderRequest.portfolio_id || portfolioId
      };

      console.log('Creating order:', requestWithPortfolio);
      const result = await orderTransactionService.createOrder(requestWithPortfolio);

      if (result.success) {
        // Refresh orders after successful creation
        await loadOrders(portfolioId);
        // Also refresh transactions as market orders might execute immediately
        await loadTransactions(portfolioId);
      }

      return result;
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to create order';
      setError(errorMessage);
      return {
        success: false,
        error: errorMessage
      };
    }
  }, [portfolioId, loadOrders, loadTransactions]);

  // Cancel an order
  const cancelOrder = useCallback(async (orderId: number, reason?: string): Promise<ApiResponse<OrderResponseDTO>> => {
    try {
      setError(null);
      
      console.log('Cancelling order:', orderId);
      const result = await orderTransactionService.cancelOrder(orderId, reason);

      if (result.success && portfolioId) {
        // Refresh orders after successful cancellation
        await loadOrders(portfolioId);
      }

      return result;
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to cancel order';
      setError(errorMessage);
      return {
        success: false,
        error: errorMessage
      };
    }
  }, [portfolioId, loadOrders]);

  // Execute an order manually
  const executeOrder = useCallback(async (orderId: number): Promise<ApiResponse<OrderResponseDTO>> => {
    try {
      setError(null);
      
      console.log('Executing order:', orderId);
      const result = await orderTransactionService.executeOrder(orderId);

      if (result.success && portfolioId) {
        // Refresh both orders and transactions after execution
        await Promise.all([
          loadOrders(portfolioId),
          loadTransactions(portfolioId)
        ]);
      }

      return result;
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to execute order';
      setError(errorMessage);
      return {
        success: false,
        error: errorMessage
      };
    }
  }, [portfolioId, loadOrders, loadTransactions]);

  // Validate an order before creating
  const validateOrder = useCallback(async (orderRequest: OrderRequestDTO): Promise<ApiResponse<any>> => {
    try {
      const requestWithPortfolio = {
        ...orderRequest,
        portfolio_id: orderRequest.portfolio_id || portfolioId || 0
      };

      console.log('Validating order:', requestWithPortfolio);
      return await orderTransactionService.validateOrder(requestWithPortfolio);
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to validate order';
      return {
        success: false,
        error: errorMessage
      };
    }
  }, [portfolioId]);

  // Get transactions for a specific order
  const getTransactionsForOrder = useCallback(async (orderId: number): Promise<TransactionResponseDTO[]> => {
    try {
      console.log('Getting transactions for order:', orderId);
      return await orderTransactionService.getTransactionsByOrder(orderId);
    } catch (error) {
      console.error('Error getting transactions for order:', error);
      return [];
    }
  }, []);

  // Get cost basis information for a security
  const getCostBasisInfo = useCallback(async (stockSymbol: string): Promise<CostBasisInfo> => {
    if (!portfolioId) return {} as CostBasisInfo;

    try {
      console.log('Getting cost basis for:', stockSymbol);
      return await orderTransactionService.getCostBasisInfo(portfolioId, stockSymbol);
    } catch (error) {
      console.error('Error getting cost basis info:', error);
      return {} as CostBasisInfo;
    }
  }, [portfolioId]);

  // Refresh data
  const refreshData = useCallback(async () => {
    await loadAllData();
  }, [loadAllData]);

  // Filter functions
  const getOrdersByStatus = useCallback((status: string) => {
    if (status === 'all') return orders;
    return orders.filter(order => order.order_status.toLowerCase() === status.toLowerCase());
  }, [orders]);

  const getOrdersBySide = useCallback((side: 'BUY' | 'SELL') => {
    return orders.filter(order => order.order_side === side);
  }, [orders]);

  const getTransactionsByStatus = useCallback((status: string) => {
    if (status === 'all') return transactions;
    return transactions.filter(transaction => transaction.transaction_status.toLowerCase() === status.toLowerCase());
  }, [transactions]);

  const getTransactionsByType = useCallback((type: string) => {
    if (type === 'all') return transactions;
    return transactions.filter(transaction => transaction.transaction_type.toLowerCase() === type.toLowerCase());
  }, [transactions]);

  // FIXED: Load data when authentication is complete and user is available
  useEffect(() => {
    console.log("=== ORDERS TRANSACTIONS EFFECT TRIGGERED ===");
    console.log("Auth loading:", authLoading);
    console.log("Is authenticated:", isAuthenticated);
    console.log("User:", user);
    console.log("User ID:", user?.user_id);

    // Only run when auth is not loading
    if (!authLoading) {
      if (isAuthenticated && user?.user_id) {
        console.log("Loading orders/transactions for authenticated user");
        loadAllData();
      } else {
        console.log("User not authenticated or no user ID, clearing state");
        // Clear state when user logs out or is not authenticated
        setOrders([]);
        setTransactions([]);
        setActiveOrders([]);
        setRecentTransactions([]);
        setOrderStats({} as OrderStats);
        setTransactionAnalytics({} as TransactionAnalytics);
        setPortfolioId(null);
        setHasPortfolio(false);
        setError(null);
        setIsLoading(false);
      }
    }
  }, [authLoading, isAuthenticated, user?.user_id, loadAllData]);

  return {
    // Data
    orders,
    transactions,
    activeOrders,
    recentTransactions,
    portfolioId,
    hasPortfolio,
    orderStats,
    transactionAnalytics,
    
    // State
    isLoading,
    error,
    
    // Actions
    createOrder,
    cancelOrder,
    executeOrder,
    validateOrder,
    getTransactionsForOrder,
    getCostBasisInfo,
    refreshData,
    loadAllData,
    
    // Filters
    getOrdersByStatus,
    getOrdersBySide,
    getTransactionsByStatus,
    getTransactionsByType,
    
    // Utility
    setError, // For clearing errors
  };
};

// Specialized hook for order management only
export const useOrders = (portfolioId?: number): UseOrdersReturn => {
  const { user } = useAuth();
  const [orders, setOrders] = useState<OrderResponseDTO[]>([]);
  const [activeOrders, setActiveOrders] = useState<OrderResponseDTO[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadOrders = useCallback(async (pId: number) => {
    try {
      const [allOrders, activeOrdersList] = await Promise.all([
        orderTransactionService.getOrdersByPortfolio(pId),
        orderTransactionService.getActiveOrdersByPortfolio(pId)
      ]);
      setOrders(allOrders);
      setActiveOrders(activeOrdersList);
    } catch (error) {
      console.error('Error loading orders:', error);
      setError('Failed to load orders');
    }
  }, []);

  const submitOrder = useCallback(async (orderRequest: OrderRequestDTO): Promise<ApiResponse<OrderResponseDTO>> => {
    setIsSubmitting(true);
    setError(null);

    try {
      const result = await orderTransactionService.createOrder(orderRequest);
      
      if (result.success && portfolioId) {
        await loadOrders(portfolioId);
      }

      return result;
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to submit order';
      setError(errorMessage);
      return {
        success: false,
        error: errorMessage
      };
    } finally {
      setIsSubmitting(false);
    }
  }, [portfolioId, loadOrders]);

  const cancelOrder = useCallback(async (orderId: number, reason?: string): Promise<ApiResponse<OrderResponseDTO>> => {
    try {
      setError(null);
      const result = await orderTransactionService.cancelOrder(orderId, reason);
      
      if (result.success && portfolioId) {
        await loadOrders(portfolioId);
      }

      return result;
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to cancel order';
      setError(errorMessage);
      return {
        success: false,
        error: errorMessage
      };
    }
  }, [portfolioId, loadOrders]);

  const refreshOrders = useCallback(() => {
    if (portfolioId) {
      loadOrders(portfolioId);
    }
  }, [portfolioId, loadOrders]);

  useEffect(() => {
    if (portfolioId) {
      loadOrders(portfolioId);
    }
  }, [portfolioId, loadOrders]);

  return {
    orders,
    activeOrders,
    isSubmitting,
    error,
    submitOrder,
    cancelOrder,
    refreshOrders,
    setError,
  };
};