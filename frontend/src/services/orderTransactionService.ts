// services/orderTransactionService.ts - CORRECTED VERSION
// Matches your backend DTOs and uses proper user_id field naming

// Import types from the new types file
import type {
  OrderRequestDTO,
  OrderResponseDTO,
  TransactionResponseDTO,
  ApiResponse,
  PortfolioResponseDTO,
  PortfolioValidation,
  OrderStats,
  TransactionAnalytics,
  CostBasisInfo,
} from "../types/orders";

const API_BASE_URL = "http://localhost:8080/api/v1";

// Helper function to get auth token
const getAuthToken = (): string | null => {
  return localStorage.getItem("microinvest_token");
};

// Helper function to get auth headers
const getAuthHeaders = (): Record<string, string> => {
  const token = getAuthToken();
  return {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  };
};

class OrderTransactionService {
  // ==================== PORTFOLIO OPERATIONS ====================

  /**
   * Get user's portfolio data
   * FIXED: Uses user_id parameter name to match backend expectations
   */
  async getUserPortfolio(
    user_id: number
  ): Promise<PortfolioResponseDTO | null> {
    try {
      console.log("Fetching portfolio for user:", user_id);

      const response = await fetch(
        `${API_BASE_URL}/portfolios/user/${user_id}`,
        {
          method: "GET",
          headers: getAuthHeaders(),
        }
      );

      if (!response.ok) {
        if (response.status === 404) {
          console.log("No portfolio found for user");
          return null;
        }
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      console.log("Portfolio data received:", data);
      return data;
    } catch (error) {
      console.error("Error fetching user portfolio:", error);
      return null;
    }
  }

  /**
   * Get portfolio summary with additional analytics
   */
  async getPortfolioSummary(portfolioId: number): Promise<any> {
    try {
      console.log("Fetching portfolio summary for:", portfolioId);

      const response = await fetch(
        `${API_BASE_URL}/portfolios/${portfolioId}/summary`,
        {
          method: "GET",
          headers: getAuthHeaders(),
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      console.log("Portfolio summary received:", data);
      return data;
    } catch (error) {
      console.error("Error fetching portfolio summary:", error);
      return {};
    }
  }

  /**
   * Check if user has an active portfolio
   * FIXED: Uses user_id parameter name
   */
  async hasActivePortfolio(user_id: number): Promise<PortfolioValidation> {
    try {
      console.log("Checking if user has active portfolio:", user_id);

      const response = await fetch(
        `${API_BASE_URL}/portfolios/user/${user_id}/has-active`,
        {
          method: "GET",
          headers: getAuthHeaders(),
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error("Error checking active portfolio:", error);
      return { hasActivePortfolio: false };
    }
  }

  /**
   * Get default portfolio ID for a user
   * FIXED: Uses user_id parameter name
   */
  async getDefaultPortfolioId(user_id: number): Promise<number | null> {
    try {
      const portfolio = await this.getUserPortfolio(user_id);
      return portfolio?.portfolio_id || null;
    } catch (error) {
      console.error("Error getting default portfolio ID:", error);
      return null;
    }
  }

  // ==================== ORDER OPERATIONS ====================

  /**
   * Create a new order
   */
  async createOrder(
    orderRequest: OrderRequestDTO
  ): Promise<ApiResponse<OrderResponseDTO>> {
    try {
      console.log("Creating order:", orderRequest);

      const response = await fetch(`${API_BASE_URL}/orders`, {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify(orderRequest),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(
          data.message || `HTTP error! status: ${response.status}`
        );
      }

      return {
        success: true,
        data: data.order || data,
        message: data.message || "Order created successfully",
      };
    } catch (error) {
      console.error("Error creating order:", error);
      return {
        success: false,
        error:
          error instanceof Error ? error.message : "Failed to create order",
      };
    }
  }

  /**
   * Get orders by portfolio ID
   */
  async getOrdersByPortfolio(portfolioId: number): Promise<OrderResponseDTO[]> {
    try {
      console.log("Fetching orders for portfolio:", portfolioId);

      const response = await fetch(
        `${API_BASE_URL}/orders/portfolio/${portfolioId}`,
        {
          method: "GET",
          headers: getAuthHeaders(),
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      return Array.isArray(data) ? data : [];
    } catch (error) {
      console.error("Error fetching orders:", error);
      return [];
    }
  }

  /**
   * Get active orders by portfolio ID
   */
  async getActiveOrdersByPortfolio(
    portfolioId: number
  ): Promise<OrderResponseDTO[]> {
    try {
      console.log("Fetching active orders for portfolio:", portfolioId);

      const response = await fetch(
        `${API_BASE_URL}/orders/portfolio/${portfolioId}/active`,
        {
          method: "GET",
          headers: getAuthHeaders(),
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      return Array.isArray(data) ? data : [];
    } catch (error) {
      console.error("Error fetching active orders:", error);
      return [];
    }
  }

  /**
   * Cancel an order
   */
  async cancelOrder(
    orderId: number,
    reason?: string
  ): Promise<ApiResponse<OrderResponseDTO>> {
    try {
      console.log("Cancelling order:", orderId, "Reason:", reason);

      const response = await fetch(`${API_BASE_URL}/orders/${orderId}/cancel`, {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify({ reason: reason || "Cancelled by user" }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(
          data.message || `HTTP error! status: ${response.status}`
        );
      }

      return {
        success: true,
        data: data.order || data,
        message: data.message || "Order cancelled successfully",
      };
    } catch (error) {
      console.error("Error cancelling order:", error);
      return {
        success: false,
        error:
          error instanceof Error ? error.message : "Failed to cancel order",
      };
    }
  }

  /**
   * Execute an order
   */
  async executeOrder(orderId: number): Promise<ApiResponse<OrderResponseDTO>> {
    try {
      console.log("Executing order:", orderId);

      const response = await fetch(
        `${API_BASE_URL}/orders/${orderId}/execute`,
        {
          method: "POST",
          headers: getAuthHeaders(),
        }
      );

      const data = await response.json();

      if (!response.ok) {
        throw new Error(
          data.message || `HTTP error! status: ${response.status}`
        );
      }

      return {
        success: true,
        data: data.order || data,
        message: data.message || "Order executed successfully",
      };
    } catch (error) {
      console.error("Error executing order:", error);
      return {
        success: false,
        error:
          error instanceof Error ? error.message : "Failed to execute order",
      };
    }
  }

  /**
   * Validate an order before creating
   */
  async validateOrder(
    orderRequest: OrderRequestDTO
  ): Promise<ApiResponse<any>> {
    try {
      console.log("Validating order:", orderRequest);

      const response = await fetch(`${API_BASE_URL}/orders/validate`, {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify(orderRequest),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(
          data.message || `HTTP error! status: ${response.status}`
        );
      }

      return {
        success: data.valid || false,
        data: data,
        message: data.message || "Order validation completed",
      };
    } catch (error) {
      console.error("Error validating order:", error);
      return {
        success: false,
        error:
          error instanceof Error ? error.message : "Failed to validate order",
      };
    }
  }

  // ==================== TRANSACTION OPERATIONS ====================

  /**
   * Get transactions by portfolio ID
   */
  async getTransactionsByPortfolio(
    portfolioId: number
  ): Promise<TransactionResponseDTO[]> {
    try {
      console.log("Fetching transactions for portfolio:", portfolioId);

      const response = await fetch(
        `${API_BASE_URL}/transactions/portfolio/${portfolioId}`,
        {
          method: "GET",
          headers: getAuthHeaders(),
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      return Array.isArray(data) ? data : [];
    } catch (error) {
      console.error("Error fetching transactions:", error);
      return [];
    }
  }

  /**
   * Get transactions by order ID
   */
  async getTransactionsByOrder(
    orderId: number
  ): Promise<TransactionResponseDTO[]> {
    try {
      console.log("Fetching transactions for order:", orderId);

      const response = await fetch(
        `${API_BASE_URL}/transactions/order/${orderId}`,
        {
          method: "GET",
          headers: getAuthHeaders(),
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      return Array.isArray(data) ? data : [];
    } catch (error) {
      console.error("Error fetching transactions for order:", error);
      return [];
    }
  }

  /**
   * Get recent transactions
   */
  async getRecentTransactions(
    portfolioId: number,
    limit: number = 10
  ): Promise<TransactionResponseDTO[]> {
    try {
      console.log("Fetching recent transactions for portfolio:", portfolioId);

      const response = await fetch(
        `${API_BASE_URL}/transactions/portfolio/${portfolioId}/recent?limit=${limit}`,
        {
          method: "GET",
          headers: getAuthHeaders(),
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      return Array.isArray(data) ? data : [];
    } catch (error) {
      console.error("Error fetching recent transactions:", error);
      return [];
    }
  }

  /**
   * Get transaction analytics
   */
  async getTransactionAnalytics(
    portfolioId: number
  ): Promise<TransactionAnalytics> {
    try {
      console.log("Fetching transaction analytics for portfolio:", portfolioId);

      const response = await fetch(
        `${API_BASE_URL}/transactions/portfolio/${portfolioId}/analytics`,
        {
          method: "GET",
          headers: getAuthHeaders(),
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      return data || {};
    } catch (error) {
      console.error("Error fetching transaction analytics:", error);
      return {} as TransactionAnalytics;
    }
  }

  /**
   * Get cost basis information for a security
   */
  async getCostBasisInfo(
    portfolioId: number,
    stockSymbol: string
  ): Promise<CostBasisInfo> {
    try {
      console.log("Fetching cost basis info for:", portfolioId, stockSymbol);

      const response = await fetch(
        `${API_BASE_URL}/transactions/portfolio/${portfolioId}/security/${stockSymbol}/cost-basis`,
        {
          method: "GET",
          headers: getAuthHeaders(),
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      return data || ({} as CostBasisInfo);
    } catch (error) {
      console.error("Error fetching cost basis info:", error);
      return {} as CostBasisInfo;
    }
  }

  /**
   * Get order statistics for portfolio
   */
  async getOrderStatsByPortfolio(portfolioId: number): Promise<OrderStats> {
    try {
      console.log("Fetching order stats for portfolio:", portfolioId);

      const response = await fetch(
        `${API_BASE_URL}/orders/portfolio/${portfolioId}/stats`,
        {
          method: "GET",
          headers: getAuthHeaders(),
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error("Error fetching order stats:", error);
      return {} as OrderStats;
    }
  }

  /**
   * Get positions data for portfolio
   */
  async getPortfolioPositions(portfolioId: number): Promise<any[]> {
    try {
      console.log("Fetching positions for portfolio:", portfolioId);

      const response = await fetch(
        `${API_BASE_URL}/positions/portfolio/${portfolioId}`,
        {
          method: "GET",
          headers: getAuthHeaders(),
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      return data.positions || [];
    } catch (error) {
      console.error("Error fetching positions:", error);
      return [];
    }
  }

  // ==================== ERROR HANDLING ====================

  /**
   * Handle API errors with proper error messages
   */
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
        return "Resource not found.";
      }
      if (error.message.includes("409")) {
        return "Order already exists or conflicts with existing data.";
      }
      if (error.message.includes("422")) {
        return "Invalid order data. Please check your inputs.";
      }
      return error.message;
    }
    return "An unexpected error occurred. Please try again.";
  }

  // ==================== UTILITY METHODS ====================

  /**
   * Format currency values
   */
  formatCurrency(amount: number | null): string {
    if (amount == null || isNaN(amount)) return "$0.00";
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount);
  }

  /**
   * Format date strings
   */
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  /**
   * Check if order can be cancelled
   */
  canCancelOrder(order: OrderResponseDTO): boolean {
    return (
      order.can_be_cancelled &&
      (order.order_status === "PENDING" ||
        order.order_status === "PARTIALLY_FILLED")
    );
  }

  /**
   * Calculate order progress percentage
   */
  getOrderProgress(order: OrderResponseDTO): number {
    if (!order.quantity || order.quantity === 0) return 0;
    return Math.round((order.filled_quantity / order.quantity) * 100);
  }

  /**
   * Get order status color for UI
   */
  getOrderStatusColor(status: string): string {
    switch (status) {
      case "FILLED":
        return "green";
      case "PENDING":
        return "yellow";
      case "PARTIALLY_FILLED":
        return "blue";
      case "CANCELLED":
        return "red";
      case "FAILED":
      case "REJECTED":
        return "red";
      case "EXPIRED":
        return "gray";
      default:
        return "gray";
    }
  }

  /**
   * Get transaction status color for UI
   */
  getTransactionStatusColor(status: string): string {
    switch (status) {
      case "COMPLETED":
        return "green";
      case "PENDING":
        return "yellow";
      case "FAILED":
      case "CANCELED":
        return "red";
      default:
        return "gray";
    }
  }

  // ==================== HEALTH CHECK ====================

  /**
   * Health check endpoint
   */
  async healthCheck(): Promise<boolean> {
    try {
      const response = await fetch(`${API_BASE_URL}/orders/health`, {
        method: "GET",
        headers: getAuthHeaders(),
      });
      return response.ok;
    } catch {
      return false;
    }
  }
}

// Export singleton instance and types
export const orderTransactionService = new OrderTransactionService();

// Re-export types for convenience
export type {
  OrderRequestDTO,
  OrderResponseDTO,
  TransactionResponseDTO,
  ApiResponse,
  PortfolioResponseDTO,
  PortfolioValidation,
  OrderStats,
  TransactionAnalytics,
  CostBasisInfo,
} from "../types/orders";
