// Complete portfolioService.ts with all necessary methods
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

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

export interface CreatePortfolioRequest {
  portfolio_name: string;
  initial_cash_balance: number;
}

class PortfolioService {
  private getAuthToken(): string | null {
    return localStorage.getItem("microinvest_token");
  }

  private getAuthHeaders(): HeadersInit {
    const token = this.getAuthToken();
    return {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    };
  }

  // Get user's portfolio - MATCHES your backend endpoint
  async getUserPortfolio(userId: number): Promise<Portfolio | null> {
    try {
      console.log(`Fetching portfolio for user ${userId}...`);
      
      const response = await fetch(`${API_BASE_URL}/api/v1/portfolios/user/${userId}`, {
        method: "GET",
        headers: this.getAuthHeaders(),
      });

      console.log(`Portfolio response status: ${response.status}`);

      if (response.status === 404) {
        console.log('No portfolio found for user (this is normal for new users)');
        return null;
      }

      if (!response.ok) {
        throw new Error(`Failed to fetch portfolio: ${response.status}`);
      }

      const portfolio = await response.json();
      console.log('Portfolio data received:', portfolio);
      return portfolio;
    } catch (error) {
      console.error('Error fetching portfolio:', error);
      throw error;
    }
  }

  // Get user's active portfolio - MATCHES your backend endpoint  
  async getActivePortfolioByUserId(userId: number): Promise<Portfolio | null> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/portfolios/user/${userId}/active`, {
        method: "GET",
        headers: this.getAuthHeaders(),
      });

      if (response.status === 404) {
        return null;
      }

      if (!response.ok) {
        throw new Error(`Failed to fetch active portfolio: ${response.status}`);
      }

      return response.json();
    } catch (error) {
      console.error('Error fetching active portfolio:', error);
      throw error;
    }
  }

  // Check if user has active portfolio - MATCHES your backend endpoint
  async hasActivePortfolio(userId: number): Promise<{ hasActivePortfolio: boolean }> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/portfolios/user/${userId}/has-active`, {
        method: "GET",
        headers: this.getAuthHeaders(),
      });

      if (!response.ok) {
        return { hasActivePortfolio: false };
      }

      return await response.json(); // Returns { hasActivePortfolio: boolean }
    } catch (error) {
      console.error("Error checking active portfolio:", error);
      return { hasActivePortfolio: false };
    }
  }

  // Create portfolio - MATCHES your backend endpoint
  async createPortfolio(userId: number, portfolioData: CreatePortfolioRequest): Promise<Portfolio> {
    try {
      console.log('Creating portfolio for user:', userId, 'with data:', portfolioData);
      
      const response = await fetch(`${API_BASE_URL}/api/v1/portfolios?userId=${userId}`, {
        method: "POST",
        headers: this.getAuthHeaders(),
        body: JSON.stringify(portfolioData),
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Portfolio creation failed:', response.status, errorText);
        throw new Error(`Failed to create portfolio: ${response.status} - ${errorText}`);
      }

      const portfolio = await response.json();
      console.log('Portfolio created successfully:', portfolio);
      return portfolio;
    } catch (error) {
      console.error('Error creating portfolio:', error);
      throw error;
    }
  }

  // Add cash to portfolio - MATCHES your backend endpoint
  async addCash(portfolioId: number, amount: number): Promise<Portfolio> {
    const response = await fetch(`${API_BASE_URL}/api/v1/portfolios/${portfolioId}/cash/add?amount=${amount}`, {
      method: "POST",
      headers: this.getAuthHeaders(),
    });

    if (!response.ok) {
      throw new Error(`Failed to add cash: ${response.status}`);
    }

    return response.json();
  }

  // Withdraw cash from portfolio - MATCHES your backend endpoint
  async withdrawCash(portfolioId: number, amount: number): Promise<Portfolio> {
    const response = await fetch(`${API_BASE_URL}/api/v1/portfolios/${portfolioId}/cash/withdraw?amount=${amount}`, {
      method: "POST",
      headers: this.getAuthHeaders(),
    });

    if (!response.ok) {
      throw new Error(`Failed to withdraw cash: ${response.status}`);
    }

    return response.json();
  }

  // Get portfolio summary - MATCHES your backend endpoint
  async getPortfolioSummary(portfolioId: number): Promise<any> {
    const response = await fetch(`${API_BASE_URL}/api/v1/portfolios/${portfolioId}/summary`, {
      method: "GET",
      headers: this.getAuthHeaders(),
    });

    if (!response.ok) {
      throw new Error(`Failed to get portfolio summary: ${response.status}`);
    }

    return response.json();
  }

  // IMPLEMENTED: Get positions by portfolio - MATCHES your backend endpoint
  async getPositionsByPortfolio(portfolioId: number): Promise<{ success: boolean; positions: any[] }> {
    try {
      console.log(`Fetching positions for portfolio ${portfolioId}...`);
      
      const response = await fetch(`${API_BASE_URL}/api/v1/positions/portfolio/${portfolioId}`, {
        method: "GET",
        headers: this.getAuthHeaders(),
      });

      console.log(`Positions response status: ${response.status}`);

      if (response.status === 404) {
        console.log('No positions found for portfolio (this is normal for new portfolios)');
        return { success: true, positions: [] };
      }

      if (!response.ok) {
        throw new Error(`Failed to fetch positions: ${response.status}`);
      }

      const data = await response.json();
      console.log('Positions data received:', data);
      
      // Backend returns: { success: true, positions: [...], totalPositions: 5, ... }
      return { 
        success: data.success || true, 
        positions: data.positions || [] 
      };
    } catch (error) {
      console.error('Error fetching positions:', error);
      return { success: false, positions: [] };
    }
  }

  // Refresh portfolio positions - MATCHES your backend endpoint
  async refreshPortfolioPositions(portfolioId: number): Promise<void> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/positions/portfolio/${portfolioId}/refresh`, {
        method: "POST",
        headers: this.getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`Failed to refresh positions: ${response.status}`);
      }

      console.log('Portfolio positions refreshed successfully');
    } catch (error) {
      console.error('Error refreshing portfolio positions:', error);
      throw error;
    }
  }

  // PLACEHOLDER METHODS - Not needed for current portfolio page functionality
  // These can be implemented later when order management is added

  async getOrdersByPortfolio(portfolioId: number): Promise<any[]> {
    // Not implemented yet - will be needed for orders page
    return [];
  }

  async getActiveOrdersByPortfolio(portfolioId: number): Promise<any[]> {
    // Not implemented yet - will be needed for orders page
    return [];
  }

  async getPortfolioAnalytics(portfolioId: number): Promise<{ success: boolean }> {
    // Not implemented yet - will be needed for analytics features
    return { success: false };
  }

  async createOrder(orderRequest: any): Promise<{ success: boolean; order?: any; message?: string }> {
    // Not implemented yet - will be needed for trading functionality
    return { success: false, message: 'Order creation not implemented yet' };
  }

  async validateOrder(orderRequest: any): Promise<any> {
    // Not implemented yet - will be needed for trading functionality
    return { valid: false };
  }

  async cancelOrder(orderId: number, reason?: string): Promise<{ success: boolean; order?: any; message?: string }> {
    // Not implemented yet - will be needed for trading functionality
    return { success: false, message: 'Order cancellation not implemented yet' };
  }
}

export const portfolioService = new PortfolioService();