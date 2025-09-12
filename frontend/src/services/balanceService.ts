// services/balanceService.ts to fetch and combine user account and portfolio balance data and update the values

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

interface CombinedBalanceData {
  user_account: {
    current_virtual_balance: number;
    total_invested_amount: number;
    total_returns: number;
    net_worth: number;
  };
  portfolio: {
    total_value: number;
    cash_balance: number;
    invested_amount: number;
    total_gain_loss: number;
  } | null;
  calculated: {
    actual_net_worth: number;
    actual_cash_balance: number;
    actual_invested_amount: number;
    actual_total_returns: number;
  };
}

class BalanceService {
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

  // Get comprehensive balance data combining user account and portfolio
  async getCombinedBalanceData(userId: number): Promise<CombinedBalanceData> {
    try {
      // Fetch user account data
      const userResponse = await fetch(`${API_BASE_URL}/api/v1/users/account`, {
        method: "GET",
        headers: this.getAuthHeaders(),
      });
      
      const userData = await userResponse.json();

      // Try to fetch portfolio data
      let portfolioData = null;
      try {
        const portfolioResponse = await fetch(`${API_BASE_URL}/api/v1/portfolios/user/${userId}`, {
          method: "GET",
          headers: this.getAuthHeaders(),
        });
        
        if (portfolioResponse.ok) {
          portfolioData = await portfolioResponse.json();
        }
      } catch (portfolioError) {
        console.log("No portfolio data available:", portfolioError);
      }

      // Calculate actual values based on available data
      const calculated = this.calculateActualBalances(userData, portfolioData);

      return {
        user_account: {
          current_virtual_balance: userData.current_virtual_balance || 0,
          total_invested_amount: userData.total_invested_amount || 0,
          total_returns: userData.total_returns || 0,
          net_worth: userData.net_worth || 0,
        },
        portfolio: portfolioData ? {
          total_value: portfolioData.total_value || 0,
          cash_balance: portfolioData.cash_balance || 0,
          invested_amount: (portfolioData.total_value || 0) - (portfolioData.cash_balance || 0),
          total_gain_loss: ((portfolioData.total_unrealized_gain_loss || 0) + (portfolioData.total_realized_gain_loss || 0)),
        } : null,
        calculated
      };
    } catch (error) {
      console.error("Error fetching combined balance data:", error);
      throw error;
    }
  }

  private calculateActualBalances(userData: any, portfolioData: any) {
    if (portfolioData) {
      // If portfolio exists, use portfolio data as source of truth
      const portfolioTotalValue = portfolioData.total_value || 0;
      const portfolioCashBalance = portfolioData.cash_balance || 0;
      const portfolioInvestedAmount = portfolioTotalValue - portfolioCashBalance;
      const portfolioTotalGainLoss = (portfolioData.total_unrealized_gain_loss || 0) + (portfolioData.total_realized_gain_loss || 0);

      return {
        actual_net_worth: portfolioTotalValue,
        actual_cash_balance: portfolioCashBalance,
        actual_invested_amount: portfolioInvestedAmount,
        actual_total_returns: portfolioTotalGainLoss,
      };
    } else {
      // If no portfolio, use user account data
      return {
        actual_net_worth: userData.net_worth || 0,
        actual_cash_balance: userData.current_virtual_balance || 0,
        actual_invested_amount: userData.total_invested_amount || 0,
        actual_total_returns: userData.total_returns || 0,
      };
    }
  }
}

export const balanceService = new BalanceService();