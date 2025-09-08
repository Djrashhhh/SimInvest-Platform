// hooks/usePortfolio.ts - FIXED VERSION with proper user_id handling
import { useState, useEffect, useCallback, useRef } from "react";
import type {
  Portfolio,
  PortfolioSummary,
  Position,
  CreatePortfolioRequest,
  Order,
  OrderRequest,
  PortfolioAnalytics,
} from "../types/portfolio";
import { portfolioService } from "../services/portfolioService";

// Hook for managing portfolio data
export const usePortfolio = (userId: number) => {
  const [portfolio, setPortfolio] = useState<Portfolio | null>(null);
  const [portfolioSummary, setPortfolioSummary] = useState<PortfolioSummary | null>(null);
  const [positions, setPositions] = useState<Position[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [activeOrders, setActiveOrders] = useState<Order[]>([]);
  const [analytics, setAnalytics] = useState<PortfolioAnalytics | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [hasPortfolio, setHasPortfolio] = useState<boolean>(false);
  
  // Use ref to track if initial load is complete
  const hasInitializedRef = useRef(false);

  // Check if user has an active portfolio
  const checkPortfolioExists = useCallback(async (userIdToCheck: number): Promise<boolean> => {
    if (!userIdToCheck || userIdToCheck <= 0) {
      console.log("Invalid userId provided:", userIdToCheck);
      setHasPortfolio(false);
      return false;
    }

    try {
      console.log("Checking if user has active portfolio for userId:", userIdToCheck);
      const result = await portfolioService.hasActivePortfolio(userIdToCheck);
      console.log("hasActivePortfolio result:", result);
      setHasPortfolio(result.hasActivePortfolio);
      return result.hasActivePortfolio;
    } catch (error) {
      console.error("Error checking portfolio existence:", error);
      setHasPortfolio(false);
      setError(error instanceof Error ? error.message : "Failed to check portfolio existence");
      return false;
    }
  }, []);

  // Fetch portfolio data
  const fetchPortfolio = useCallback(async (userIdToFetch: number) => {
    if (!userIdToFetch || userIdToFetch <= 0) {
      console.log("No valid userId provided for portfolio fetch:", userIdToFetch);
      setIsLoading(false);
      return;
    }

    try {
      setIsLoading(true);
      setError(null);

      console.log("Fetching portfolio for user:", userIdToFetch);

      // Check if portfolio exists first
      const hasActive = await checkPortfolioExists(userIdToFetch);

      if (!hasActive) {
        console.log("No portfolio found for user:", userIdToFetch);
        setIsLoading(false);
        return;
      }

      // Fetch active portfolio
      console.log("Fetching user portfolio...");
      const portfolioData = await portfolioService.getUserPortfolio(userIdToFetch);
      console.log("Portfolio data received:", portfolioData);

      if (portfolioData) {
        setPortfolio(portfolioData);

        // Fetch portfolio summary
        try {
          console.log("Fetching portfolio summary...");
          const summaryData = await portfolioService.getPortfolioSummary(portfolioData.portfolio_id);
          setPortfolioSummary(summaryData);
          console.log("Portfolio summary received:", summaryData);
        } catch (summaryError) {
          console.log("Portfolio summary not available, creating basic summary:", summaryError);
          // Create a basic summary from portfolio data
          const basicSummary: PortfolioSummary = {
            portfolio_id: portfolioData.portfolio_id,
            portfolio_name: portfolioData.portfolio_name,
            total_value: portfolioData.total_value,
            cash_balance: portfolioData.cash_balance,
            invested_amount: portfolioData.total_value - portfolioData.cash_balance,
            total_gain_loss: (portfolioData.total_unrealized_gain_loss || 0) + (portfolioData.total_realized_gain_loss || 0),
            total_gain_loss_percentage: portfolioData.total_value > 0 
              ? (((portfolioData.total_unrealized_gain_loss || 0) + (portfolioData.total_realized_gain_loss || 0)) / portfolioData.total_value) * 100 
              : 0,
            position_count: portfolioData.position_count || 0,
            last_updated: portfolioData.last_updated
          };
          setPortfolioSummary(basicSummary);
        }
      }

    } catch (error) {
      console.error("Error fetching portfolio:", error);
      setError(error instanceof Error ? error.message : "Failed to fetch portfolio data");
    } finally {
      setIsLoading(false);
    }
  }, [checkPortfolioExists]);

  // Fetch positions
  const fetchPositions = useCallback(async (portfolioId: number) => {
    try {
      const response = await portfolioService.getPositionsByPortfolio(portfolioId);
      if (response.success && response.positions) {
        setPositions(response.positions);
      }
    } catch (error) {
      console.error("Error fetching positions:", error);
    }
  }, []);

  // Fetch orders
  const fetchOrders = useCallback(async (portfolioId: number) => {
    try {
      const [allOrders, activeOrdersData] = await Promise.all([
        portfolioService.getOrdersByPortfolio(portfolioId),
        portfolioService.getActiveOrdersByPortfolio(portfolioId),
      ]);
      setOrders(allOrders);
      setActiveOrders(activeOrdersData);
    } catch (error) {
      console.error("Error fetching orders:", error);
    }
  }, []);

  // Fetch analytics
  const fetchAnalytics = useCallback(async (portfolioId: number) => {
    try {
      const response = await portfolioService.getPortfolioAnalytics(portfolioId);
      if (response.success) {
        setAnalytics(response as any);
      }
    } catch (error) {
      console.error("Error fetching analytics:", error);
    }
  }, []);

  // Create portfolio
  const createPortfolio = useCallback(
    async (request: CreatePortfolioRequest) => {
      if (!userId || userId <= 0) {
        throw new Error("Invalid user ID");
      }

      try {
        setIsLoading(true);
        setError(null);

        const newPortfolio = await portfolioService.createPortfolio(userId, request);
        setPortfolio(newPortfolio);
        setHasPortfolio(true);

        // Fetch summary for the new portfolio
        try {
          const summaryData = await portfolioService.getPortfolioSummary(newPortfolio.portfolio_id);
          setPortfolioSummary(summaryData);
        } catch (summaryError) {
          // Create basic summary if endpoint not available
          const basicSummary: PortfolioSummary = {
            portfolio_id: newPortfolio.portfolio_id,
            portfolio_name: newPortfolio.portfolio_name,
            total_value: newPortfolio.total_value,
            cash_balance: newPortfolio.cash_balance,
            invested_amount: 0,
            total_gain_loss: 0,
            total_gain_loss_percentage: 0,
            position_count: 0,
            last_updated: newPortfolio.last_updated
          };
          setPortfolioSummary(basicSummary);
        }

        return newPortfolio;
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "Failed to create portfolio";
        setError(errorMessage);
        throw new Error(errorMessage);
      } finally {
        setIsLoading(false);
      }
    },
    [userId]
  );

  // Add cash to portfolio
  const addCash = useCallback(async (amount: number) => {
    if (!portfolio) {
      throw new Error("No portfolio available");
    }

    try {
      const updatedPortfolio = await portfolioService.addCash(portfolio.portfolio_id, amount);
      setPortfolio(updatedPortfolio);

      // Refresh summary
      try {
        const summaryData = await portfolioService.getPortfolioSummary(portfolio.portfolio_id);
        setPortfolioSummary(summaryData);
      } catch (summaryError) {
        console.log("Could not refresh summary after cash addition");
      }

      return updatedPortfolio;
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "Failed to add cash";
      setError(errorMessage);
      throw new Error(errorMessage);
    }
  }, [portfolio]);

  // Withdraw cash from portfolio
  const withdrawCash = useCallback(async (amount: number) => {
    if (!portfolio) {
      throw new Error("No portfolio available");
    }

    try {
      const updatedPortfolio = await portfolioService.withdrawCash(portfolio.portfolio_id, amount);
      setPortfolio(updatedPortfolio);

      // Refresh summary
      try {
        const summaryData = await portfolioService.getPortfolioSummary(portfolio.portfolio_id);
        setPortfolioSummary(summaryData);
      } catch (summaryError) {
        console.log("Could not refresh summary after cash withdrawal");
      }

      return updatedPortfolio;
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "Failed to withdraw cash";
      setError(errorMessage);
      throw new Error(errorMessage);
    }
  }, [portfolio]);

  // Refresh all data
  const refreshData = useCallback(async () => {
    if (userId && userId > 0) {
      await fetchPortfolio(userId);
      
      if (portfolio?.portfolio_id) {
        await Promise.all([
          fetchPositions(portfolio.portfolio_id),
          fetchOrders(portfolio.portfolio_id),
          fetchAnalytics(portfolio.portfolio_id),
        ]);
      }
    }
  }, [userId, portfolio?.portfolio_id, fetchPortfolio, fetchPositions, fetchOrders, fetchAnalytics]);

  // Refresh portfolio positions (market data update)
  const refreshPositions = useCallback(async () => {
    if (!portfolio) return;

    try {
      await portfolioService.refreshPortfolioPositions(portfolio.portfolio_id);
      await fetchPositions(portfolio.portfolio_id);
      await fetchAnalytics(portfolio.portfolio_id);

      // Refresh portfolio summary
      try {
        const summaryData = await portfolioService.getPortfolioSummary(portfolio.portfolio_id);
        setPortfolioSummary(summaryData);
      } catch (summaryError) {
        console.log("Could not refresh summary after position update");
      }
    } catch (error) {
      console.error("Error refreshing positions:", error);
    }
  }, [portfolio, fetchPositions, fetchAnalytics]);

  // FIXED: Single useEffect for initial data loading
  useEffect(() => {
    const shouldLoad = userId && userId > 0 && !hasInitializedRef.current;
    
    if (shouldLoad) {
      console.log("Initializing portfolio data for user:", userId);
      hasInitializedRef.current = true;
      fetchPortfolio(userId);
    }
  }, [userId, fetchPortfolio]);

  // Load additional data when portfolio becomes available
  useEffect(() => {
    if (portfolio?.portfolio_id) {
      console.log("Loading additional portfolio data for:", portfolio.portfolio_id);
      Promise.all([
        fetchPositions(portfolio.portfolio_id),
        fetchOrders(portfolio.portfolio_id),
        fetchAnalytics(portfolio.portfolio_id),
      ]).catch(error => {
        console.error("Error loading additional portfolio data:", error);
      });
    }
  }, [portfolio?.portfolio_id, fetchPositions, fetchOrders, fetchAnalytics]);

  return {
    portfolio,
    portfolioSummary,
    positions,
    orders,
    activeOrders,
    analytics,
    isLoading,
    error,
    hasPortfolio,
    createPortfolio,
    addCash,
    withdrawCash,
    refreshData,
    refreshPositions,
    setError, // For clearing errors
  };
};

// Hook for order management
export const useOrders = (portfolioId: number | null) => {
  const [isSubmittingOrder, setIsSubmittingOrder] = useState(false);
  const [orderError, setOrderError] = useState<string | null>(null);

  const submitOrder = useCallback(
    async (orderRequest: OrderRequest) => {
      if (!portfolioId) {
        throw new Error("No portfolio selected");
      }

      try {
        setIsSubmittingOrder(true);
        setOrderError(null);

        const orderWithPortfolio = {
          ...orderRequest,
          portfolio_id: portfolioId,
        };

        const response = await portfolioService.createOrder(orderWithPortfolio);

        if (response.success) {
          return response.order;
        } else {
          throw new Error(response.message || "Failed to submit order");
        }
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "Failed to submit order";
        setOrderError(errorMessage);
        throw new Error(errorMessage);
      } finally {
        setIsSubmittingOrder(false);
      }
    },
    [portfolioId]
  );

  const validateOrder = useCallback(
    async (orderRequest: OrderRequest) => {
      if (!portfolioId) {
        throw new Error("No portfolio selected");
      }

      try {
        const orderWithPortfolio = {
          ...orderRequest,
          portfolio_id: portfolioId,
        };

        const response = await portfolioService.validateOrder(orderWithPortfolio);
        return response;
      } catch (error) {
        console.error("Error validating order:", error);
        throw error;
      }
    },
    [portfolioId]
  );

  const cancelOrder = useCallback(async (orderId: number, reason?: string) => {
    try {
      const response = await portfolioService.cancelOrder(orderId, reason);
      if (response.success) {
        return response.order;
      } else {
        throw new Error(response.message || "Failed to cancel order");
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "Failed to cancel order";
      setOrderError(errorMessage);
      throw new Error(errorMessage);
    }
  }, []);

  return {
    submitOrder,
    validateOrder,
    cancelOrder,
    isSubmittingOrder,
    orderError,
    setOrderError,
  };
};