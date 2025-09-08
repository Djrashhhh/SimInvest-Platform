import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import {
  TrendingUp,
  DollarSign,
  Wallet,
  Bell,
  Search,
  User,
  LogOut,
  Plus,
  RefreshCw,
  Eye,
  EyeOff,
  Target,
  Award,
  Clock,
  Activity,
  Settings,
  ArrowRight,
  BarChart3,
  PieChart,
  ChevronDown,
  Newspaper,
} from "lucide-react";
import { useAuth } from "../../contexts/AuthContext";
import { useWatchlists } from "../../hooks/useWatchlist";
import UserProfileModal from "../../components/ui/UserProfileModal";
import type { Watchlist } from "../../types/watchlist";
import { balanceService } from "../../services/balanceService";

// Define types based on your DTOs
type UserAccountDetails = {
  user_id: number;
  first_name: string;
  last_name: string;
  email: string;
  username: string;
  risk_tolerance: string;
  account_status: string;
  is_active: boolean;
  email_verified: boolean;
  created_at: string;
  last_updated: string;
  current_virtual_balance: number;
  total_invested_amount: number;
  total_returns: number;
  account_currency: string;
  full_name: string;
  net_worth: number;
  return_on_investment: number;
};

type UserProfile = {
  profile_id: number;
  user_id: number;
  experience_level: string;
  investment_goal: string;
  personal_financial_goal: string;
  preferred_investment_types: string[];
  investment_goal_target_amount: number;
  investment_goal_target_date: string;
  personal_financial_goal_target_amount: number;
  personal_financial_goal_description: string;
  learning_progress: number;
  progress_percentage: number;
  days_until_goal: number;
  is_goal_overdue: boolean;
  is_experienced: boolean;
};

type PortfolioSummaryResponseDTO = {
  portfolio_id: number;
  portfolio_name: string;
  total_value: number;
  cash_balance: number;
  invested_amount: number;
  total_gain_loss: number;
  total_gain_loss_percentage: number;
  position_count: number;
  last_updated: string;
};

const DashboardPage = () => {
  const { user: authUser, logout } = useAuth();
  const { watchlists, stats } = useWatchlists();
  const [userDetails, setUserDetails] = useState<UserAccountDetails | null>(
    null
  );
  const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
  const [portfolios, setPortfolios] = useState<PortfolioSummaryResponseDTO[]>(
    []
  );
  const [showBalance, setShowBalance] = useState(true);
  const [isLoading, setIsLoading] = useState(true);
  const [notifications, setNotifications] = useState<any[]>([]);
  const [showNotifications, setShowNotifications] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [showProfileModal, setShowProfileModal] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Get the base API URL
  const API_BASE_URL = "http://localhost:8080/api/v1";

  // Function to get auth token
  const getAuthToken = () => {
    return localStorage.getItem("microinvest_token");
  };

  // Function to get auth headers
  const getAuthHeaders = () => {
    const token = getAuthToken();
    return {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    };
  };

  // Fetch user account details - FIXED endpoint
  const fetchUserAccountDetails = async () => {
    try {
      const token = getAuthToken();
      if (!token) {
        console.error("No authentication token found in localStorage");
        throw new Error("No authentication token found");
      }

      console.log(
        "Fetching user account details with token:",
        token.substring(0, 20) + "..."
      );

      // FIXED: Using the correct endpoint from UserAccountController
      const response = await fetch(`${API_BASE_URL}/users/account`, {
        method: "GET",
        headers: getAuthHeaders(),
      });

      if (!response.ok) {
        if (response.status === 401) {
          console.error("Unauthorized - token may be invalid or expired");
          await logout();
          return;
        }
        throw new Error(
          `Failed to fetch user account: ${response.status} ${response.statusText}`
        );
      }

      const userData = await response.json();
      console.log("User account data received:", userData);
      setUserDetails(userData);
    } catch (error) {
      console.error("Error fetching user account:", error);
      setError("Failed to load user account data");
      if (
        error instanceof Error &&
        (error.message.includes("authentication") ||
          error.message.includes("401"))
      ) {
        await logout();
      }
    }
  };

  // Fetch user profile data
  const fetchUserProfile = async () => {
    if (!userDetails) return;

    try {
      const response = await fetch(
        `${API_BASE_URL}/users/${userDetails.user_id}/profile`,
        {
          method: "GET",
          headers: getAuthHeaders(),
        }
      );

      if (response.ok) {
        const profileData = await response.json();
        console.log("User profile data received:", profileData);
        setUserProfile(profileData);
      } else if (response.status === 404) {
        console.log("User profile not found - this is normal for new users");
      } else {
        console.error(
          "Failed to fetch user profile:",
          response.status,
          response.statusText
        );
      }
    } catch (error) {
      console.error("Error fetching user profile:", error);
    }
  };

  // Fetch portfolios data

  const fetchPortfolios = async () => {
    if (!userDetails) {
      console.log("No user details available, skipping portfolio fetch");
      return;
    }

    try {
      console.log(
        `Attempting to fetch portfolio for user ${userDetails.user_id}...`
      );

      // FIXED: Use the correct endpoint that exists in your controller
      const response = await fetch(
        `${API_BASE_URL}/portfolios/user/${userDetails.user_id}`, // <- Changed this line
        {
          method: "GET",
          headers: getAuthHeaders(),
        }
      );

      console.log("Portfolio response status:", response.status);

      if (response.ok) {
        const portfolioData = await response.json();
        console.log("Portfolio data received:", portfolioData);
        // Convert single portfolio to array format expected by your dashboard
        setPortfolios([portfolioData]); // <- Wrap in array since your UI expects an array
      } else if (response.status === 404) {
        console.log("No portfolio found - this is normal for new users");
        setPortfolios([]);
      } else {
        console.error(
          "Failed to fetch portfolio:",
          response.status,
          response.statusText
        );
        setPortfolios([]);
      }
    } catch (error) {
      console.error("Error fetching portfolio:", error);
      setPortfolios([]);
    }
  };

  useEffect(() => {
    const loadData = async () => {
      console.log("Loading dashboard data...");
      console.log("AuthUser:", authUser);
      console.log(
        "Token in localStorage:",
        getAuthToken()?.substring(0, 20) + "..."
      );

      setIsLoading(true);
      setError(null);

      try {
        await fetchUserAccountDetails();
      } catch (error) {
        console.error("Error loading dashboard data:", error);
      } finally {
        setIsLoading(false);
      }
    };

    if (authUser) {
      loadData();
    } else {
      console.log("No authenticated user found");
      setIsLoading(false);
    }
  }, [authUser]);

  useEffect(() => {
    const loadPortfolioAndBalances = async () => {
      if (!userDetails) return;

      try {
        console.log("Loading real-time balance data...");

        // Use the new balance service for accurate data
        const balanceData = await balanceService.getCombinedBalanceData(
          userDetails.user_id
        );
        console.log("Balance data received:", balanceData);

        // Update state with real-time data
        setUserDetails((prevDetails) => {
          if (!prevDetails) return null;

          // Only update if data has actually changed to prevent unnecessary re-renders
          const newDetails = {
            ...prevDetails,
            current_virtual_balance: balanceData.calculated.actual_cash_balance,
            total_invested_amount:
              balanceData.calculated.actual_invested_amount,
            total_returns: balanceData.calculated.actual_total_returns,
            net_worth: balanceData.calculated.actual_net_worth,
          };

          // Check if anything actually changed
          const hasChanged =
            prevDetails.current_virtual_balance !==
              newDetails.current_virtual_balance ||
            prevDetails.total_invested_amount !==
              newDetails.total_invested_amount ||
            prevDetails.total_returns !== newDetails.total_returns ||
            prevDetails.net_worth !== newDetails.net_worth;

          return hasChanged ? newDetails : prevDetails;
        });

        // If portfolio exists, set it for the portfolio section
        if (balanceData.portfolio) {
          const portfolioSummary = {
            portfolio_id: 1, // You'll need the actual portfolio ID
            portfolio_name: "Investment Portfolio", // You'll need the actual name
            total_value: balanceData.portfolio.total_value,
            cash_balance: balanceData.portfolio.cash_balance,
            invested_amount: balanceData.portfolio.invested_amount,
            total_gain_loss: balanceData.portfolio.total_gain_loss,
            total_gain_loss_percentage:
              balanceData.portfolio.total_value > 0
                ? (balanceData.portfolio.total_gain_loss /
                    balanceData.portfolio.total_value) *
                  100
                : 0,
            position_count: balanceData.portfolio.position_count || 0, // to get this from the portfolio data
            last_updated: new Date().toISOString(),
          };
          setPortfolios([portfolioSummary]);
        } else {
          setPortfolios([]);
        }

        // Also load user profile
        fetchUserProfile();
      } catch (error) {
        console.error("Error loading balance data:", error);
        // Fallback to original method
        fetchPortfolios().catch((err) => {
          console.log("Portfolio fetch failed:", err);
          setPortfolios([]);
        });
      }
    };

    // FIXED: Only run once when userDetails is first available
    // Use a flag to prevent multiple calls
    if (userDetails && !userDetails.dataLoaded) {
      // Add a flag to prevent re-running
      setUserDetails((prev) => (prev ? { ...prev, dataLoaded: true } : null));
      loadPortfolioAndBalances();
    }
  }, [userDetails?.user_id]);

  // Close dropdowns when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as Element;
      if (!target.closest(".user-menu")) {
        setShowUserMenu(false);
      }
      if (!target.closest(".notifications-menu")) {
        setShowNotifications(false);
      }
    };

    document.addEventListener("click", handleClickOutside);
    return () => document.removeEventListener("click", handleClickOutside);
  }, []);

  if (!authUser) {
    return (
      <div className="min-h-screen bg-gray-900 text-white flex items-center justify-center">
        <div className="text-center">
          <p className="text-xl text-gray-400">
            Please log in to access the dashboard
          </p>
        </div>
      </div>
    );
  }

  const formatCurrency = (amount: number | undefined | null) => {
    const safeAmount = amount ?? 0;
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: userDetails?.account_currency || "USD",
    }).format(safeAmount);
  };

  const getGainLossClass = (value: number | undefined | null) => {
    const safeValue = value ?? 0;
    if (safeValue > 0) return "text-green-400";
    if (safeValue < 0) return "text-red-400";
    return "text-gray-400";
  };

  const formatGainLoss = (value: number | undefined | null) => {
    const safeValue = value ?? 0; // FIXED: Handle undefined/null values
    if (safeValue > 0) return `+${safeValue.toFixed(2)}`;
    return safeValue.toFixed(2);
  };

  const totalNetWorth = userDetails?.net_worth ?? 0;
  const totalReturn = userDetails?.total_returns ?? 0;

  return (
    <div className="min-h-screen bg-gray-900 text-white flex flex-col items-center p-4 sm:p-8 font-sans">
      <div className="w-full max-w-7xl">
        {/* Header */}
        <header className="flex justify-between items-center py-4 px-6 bg-gray-800 rounded-xl shadow-lg mb-8">
          <div className="flex items-center space-x-4">
            <span className="text-xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-purple-600">
              SimInvest Platform
            </span>
          </div>
          <div className="flex items-center space-x-4">
            {/* Notifications */}
            <div className="notifications-menu relative">
              <button
                onClick={() => setShowNotifications(!showNotifications)}
                className="relative p-2 rounded-full hover:bg-gray-700 transition-colors"
              >
                <Bell className="h-6 w-6 text-gray-400" />
                {notifications.length > 0 && (
                  <span className="absolute top-1 right-1 h-3 w-3 bg-red-500 rounded-full border-2 border-gray-800"></span>
                )}
              </button>

              {showNotifications && (
                <div className="absolute right-0 mt-2 w-80 bg-gray-700 rounded-xl shadow-lg border border-gray-600 z-50">
                  <div className="p-4">
                    <h3 className="font-semibold text-white mb-3">
                      Notifications
                    </h3>
                    {notifications.length === 0 ? (
                      <p className="text-gray-400 text-sm">
                        No new notifications
                      </p>
                    ) : (
                      <div className="space-y-2">
                        {notifications.map((notification, index) => (
                          <div
                            key={index}
                            className="p-3 bg-gray-600 rounded-lg"
                          >
                            <p className="text-white text-sm">
                              {notification.message}
                            </p>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>

            {/* User Menu */}
            <div className="user-menu relative">
              <button
                onClick={() => setShowUserMenu(!showUserMenu)}
                className="flex items-center space-x-2 rounded-full p-2 hover:bg-gray-700 transition-colors"
              >
                <User className="h-6 w-6 text-gray-400" />
                <ChevronDown
                  className={`h-4 w-4 text-gray-400 transition-transform ${showUserMenu ? "rotate-180" : ""}`}
                />
              </button>

              {showUserMenu && (
                <div className="absolute right-0 mt-2 w-64 bg-gray-700 rounded-xl shadow-lg border border-gray-600 z-50">
                  <div className="p-4 border-b border-gray-600">
                    <div className="flex items-center space-x-3">
                      <div className="h-10 w-10 bg-gradient-to-r from-blue-500 to-purple-600 rounded-full flex items-center justify-center">
                        <User className="h-5 w-5 text-white" />
                      </div>
                      <div>
                        <p className="font-semibold text-white">
                          {userDetails?.first_name && userDetails?.last_name
                            ? `${userDetails.first_name} ${userDetails.last_name}`
                            : authUser.username}
                        </p>
                        <p className="text-gray-400 text-sm">
                          {userDetails?.email || authUser.email}
                        </p>
                      </div>
                    </div>
                  </div>

                  <div className="p-2">
                    <button
                      onClick={() => {
                        setShowProfileModal(true);
                        setShowUserMenu(false);
                      }}
                      className="w-full flex items-center space-x-3 px-3 py-2 text-left hover:bg-gray-600 rounded-lg transition-colors"
                    >
                      <User className="h-5 w-5 text-gray-400" />
                      <span className="text-white">View Profile</span>
                    </button>

                    <Link
                      to="/settings"
                      className="w-full flex items-center space-x-3 px-3 py-2 text-left hover:bg-gray-600 rounded-lg transition-colors"
                      onClick={() => setShowUserMenu(false)}
                    >
                      <Settings className="h-5 w-5 text-gray-400" />
                      <span className="text-white">Settings</span>
                    </Link>

                    <button
                      onClick={() => {
                        logout();
                        setShowUserMenu(false);
                      }}
                      className="w-full flex items-center space-x-3 px-3 py-2 text-left hover:bg-gray-600 rounded-lg transition-colors text-red-400"
                    >
                      <LogOut className="h-5 w-5" />
                      <span>Log Out</span>
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </header>

        {/* Navigation Links */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
          <Link
            to="/market"
            className="bg-gradient-to-r from-green-600 to-green-700 hover:from-green-700 hover:to-green-800 rounded-xl p-4 shadow-lg transition-all duration-200 hover:scale-105"
          >
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-bold text-lg text-white">Market</h3>
                <p className="text-green-100 text-sm">
                  Explore stocks & trends
                </p>
              </div>
              <BarChart3 className="h-8 w-8 text-white" />
            </div>
          </Link>
          {/* NEW: Orders and Transactions Link */}
          <Link
            to="/orders-transactions"
            className="bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 rounded-xl p-4 shadow-lg transition-all duration-200 hover:scale-105"
          >
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-bold text-lg text-white">Trading</h3>
                <p className="text-blue-100 text-sm">Buy & Sell Securities</p>
              </div>
              <PieChart className="h-8 w-8 text-white" />
            </div>
          </Link>

          {/* NEW: Market News Link */}
          <Link
            to="/news"
            className="bg-gradient-to-r from-orange-600 to-red-700 hover:from-orange-700 hover:to-red-800 rounded-xl p-4 shadow-lg transition-all duration-200 hover:scale-105"
          >
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-bold text-lg text-white">Market News</h3>
                <p className="text-orange-100 text-sm">
                  Latest market insights
                </p>
              </div>
              <div className="relative">
                <Newspaper className="h-8 w-8 text-white" />
                <span className="absolute -top-1 -right-1 h-3 w-3 bg-yellow-400 rounded-full animate-pulse"></span>
              </div>
            </div>
          </Link>

          <Link
            to="/learning"
            className="bg-gradient-to-r from-purple-600 to-purple-700 hover:from-purple-700 hover:to-purple-800 rounded-xl p-4 shadow-lg transition-all duration-200 hover:scale-105"
          >
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-bold text-lg text-white">Resources</h3>
                <p className="text-purple-100 text-sm">Educational Content</p>
              </div>
              <Activity className="h-8 w-8 text-white" />
            </div>
          </Link>
        </div>

        {isLoading ? (
          <div className="flex justify-center items-center h-64">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-400"></div>
          </div>
        ) : error ? (
          <div className="text-center text-red-500 my-8 bg-red-900/20 border border-red-500/50 rounded-lg p-6">
            <p className="text-lg mb-4">{error}</p>
            <p className="text-sm text-gray-400 mb-4">Debug info:</p>
            <p className="text-xs text-gray-500 mb-4">
              Token exists: {!!getAuthToken()}
              <br />
              Auth user: {authUser?.username}
              <br />
              API URL: {API_BASE_URL}
            </p>
            <button
              onClick={() => window.location.reload()}
              className="mr-4 px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors"
            >
              Retry
            </button>
            <button
              onClick={logout}
              className="px-4 py-2 bg-red-600 hover:bg-red-700 rounded-lg transition-colors"
            >
              Log Out
            </button>
          </div>
        ) : (
          <>
            {/* Welcome Message */}
            <div className="bg-gray-800 rounded-2xl p-6 shadow-lg mb-8">
              <h2 className="text-2xl font-bold text-white mb-2">
                Welcome back,{" "}
                {userDetails?.first_name && userDetails?.last_name
                  ? `${userDetails.first_name} ${userDetails.last_name}`
                  : userDetails?.first_name
                    ? userDetails.first_name
                    : authUser.first_name || authUser.username}
                ! 👋
              </h2>
              <p className="text-gray-400">
                Here's your investment overview for today.
              </p>
              {userProfile && (
                <div className="mt-4 flex items-center space-x-4">
                  <div className="flex items-center space-x-2">
                    <div
                      className={`w-2 h-2 rounded-full ${
                        userProfile.experience_level === "BEGINNER"
                          ? "bg-green-400"
                          : userProfile.experience_level === "INTERMEDIATE"
                            ? "bg-blue-400"
                            : "bg-purple-400"
                      }`}
                    ></div>
                    <span className="text-sm text-gray-300 capitalize">
                      {userProfile.experience_level} Investor
                    </span>
                  </div>
                  {userProfile.progress_percentage > 0 && (
                    <div className="flex items-center space-x-2">
                      <div className="w-16 bg-gray-600 rounded-full h-1">
                        <div
                          className="bg-blue-400 h-1 rounded-full"
                          style={{
                            width: `${Math.min(userProfile.progress_percentage, 100)}%`,
                          }}
                        ></div>
                      </div>
                      <span className="text-xs text-gray-400">
                        {userProfile.progress_percentage.toFixed(0)}% Learning
                        Progress
                      </span>
                    </div>
                  )}
                </div>
              )}
            </div>

            {/* Main Content */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-8">
              {/* Card 1: Net Worth */}
              <div className="bg-gray-800 rounded-2xl p-6 shadow-lg flex flex-col justify-between transition-transform duration-300 hover:scale-105">
                <div>
                  <div className="flex items-center justify-between mb-4">
                    <h2 className="text-lg font-medium text-gray-400">
                      Total Net Worth
                    </h2>
                    <button
                      onClick={() => setShowBalance(!showBalance)}
                      className="text-gray-400 hover:text-white"
                    >
                      {showBalance ? (
                        <EyeOff className="h-5 w-5" />
                      ) : (
                        <Eye className="h-5 w-5" />
                      )}
                    </button>
                  </div>
                  <p className="text-4xl font-bold text-white">
                    {showBalance ? formatCurrency(totalNetWorth) : "***"}
                  </p>
                </div>
                <div className="mt-4 text-sm">
                  <span
                    className={`font-semibold ${getGainLossClass(totalReturn)}`}
                  >
                    {formatGainLoss(totalReturn)} (
                    {totalNetWorth > 0
                      ? ((totalReturn / totalNetWorth) * 100).toFixed(2)
                      : "0.00"}
                    %)
                  </span>
                  <span className="text-gray-400 ml-2">Total Return</span>
                </div>
              </div>

              {/* Card 2: Virtual Balance */}
              <div className="bg-gray-800 rounded-2xl p-6 shadow-lg flex flex-col justify-between transition-transform duration-300 hover:scale-105">
                <div>
                  <div className="flex items-center justify-between mb-4">
                    <h2 className="text-lg font-medium text-gray-400">
                      Cash Balance
                    </h2>
                    <button
                      onClick={() => setShowBalance(!showBalance)}
                      className="text-gray-400 hover:text-white"
                    >
                      {showBalance ? (
                        <EyeOff className="h-5 w-5" />
                      ) : (
                        <Eye className="h-5 w-5" />
                      )}
                    </button>
                  </div>
                  <p className="text-4xl font-bold text-white">
                    {showBalance
                      ? formatCurrency(
                          userDetails?.current_virtual_balance || 0
                        )
                      : "***"}
                  </p>
                </div>
                <div className="mt-4 flex items-center justify-between">
                  <span className="text-sm text-gray-400">
                    Total Invested:{" "}
                    {formatCurrency(userDetails?.total_invested_amount || 0)}
                  </span>
                  <button className="flex items-center text-blue-400 hover:text-blue-300 text-sm">
                    <Plus className="h-4 w-4 mr-1" />
                    Add Funds
                  </button>
                </div>
              </div>

              {/* Card 3: Enhanced Quick Actions with News */}
              <div className="bg-gray-800 rounded-2xl p-6 shadow-lg">
                <h2 className="text-lg font-medium text-gray-400 mb-4">
                  Quick Actions
                </h2>
                <div className="space-y-3">
                  <Link
                    to="/portfolios"
                    className="flex items-center justify-between p-3 bg-gray-700 rounded-lg hover:bg-gray-600 transition-colors"
                  >
                    <span className="text-white">My Portfolio</span>
                    <ArrowRight className="h-4 w-4 text-gray-400" />
                  </Link>
                  <Link
                    to="/orders-transactions"
                    className="flex items-center justify-between p-3 bg-gray-700 rounded-lg hover:bg-gray-600 transition-colors"
                  >
                    <span className="text-white">Trading Dashboard</span>
                    <ArrowRight className="h-4 w-4 text-gray-400" />
                  </Link>
                  {/* Market News Link with highlight */}
                  <Link
                    to="/news"
                    className="flex items-center justify-between p-3 bg-gradient-to-r from-orange-600/20 to-red-600/20 border border-orange-500/30 rounded-lg hover:from-orange-600/30 hover:to-red-600/30 transition-all"
                  >
                    <div className="flex items-center space-x-2">
                      <TrendingUp className="h-4 w-4 text-orange-400" />
                      <span className="text-white font-medium">
                        Market News
                      </span>
                      <span className="px-2 py-1 bg-orange-500 text-white text-xs rounded-full">
                        New
                      </span>
                    </div>
                    <ArrowRight className="h-4 w-4 text-orange-400" />
                  </Link>
                  <button
                    onClick={() => setShowProfileModal(true)}
                    className="w-full flex items-center justify-between p-3 bg-gray-700 rounded-lg hover:bg-gray-600 transition-colors"
                  >
                    <span className="text-white">Manage Profile</span>
                    <User className="h-4 w-4 text-gray-400" />
                  </button>
                  <Link
                    to="/watchlist" // Correct link
                    className="flex items-center justify-between p-3 bg-gray-700 rounded-lg hover:bg-gray-600 transition-colors"
                  >
                    <span className="text-white">My Watchlist</span>
                    <ArrowRight className="h-4 w-4 text-gray-400" />
                  </Link>
                </div>
              </div>
            </div>

            {/* Portfolios Overview */}
            {portfolios.length > 0 && (
              <div className="bg-gray-800 rounded-2xl p-6 shadow-lg mb-8">
                <div className="flex items-center justify-between mb-6">
                  <h3 className="text-xl font-bold text-white">
                    Your Portfolios
                  </h3>
                  <Link
                    to="/portfolios"
                    className="text-blue-400 hover:text-blue-300 text-sm flex items-center space-x-1"
                  >
                    <span>View All</span>
                    <ArrowRight className="h-4 w-4" />
                  </Link>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {portfolios.slice(0, 3).map((portfolio) => (
                    <div
                      key={portfolio.portfolio_id}
                      className="bg-gray-700/50 rounded-xl p-4"
                    >
                      <div className="flex items-center justify-between mb-3">
                        <h4 className="font-semibold text-white">
                          {portfolio.portfolio_name || "Unamed Portfolio"}
                        </h4>
                      </div>
                      <div className="space-y-2">
                        <div className="flex items-center justify-between">
                          <span className="text-gray-400 text-sm">
                            Total Value
                          </span>
                          <span className="text-white font-medium">
                            {formatCurrency(portfolio.total_value)}
                          </span>
                        </div>
                        <div className="flex items-center justify-between">
                          <span className="text-gray-400 text-sm">
                            Gain/Loss
                          </span>
                          <span
                            className={`font-medium ${getGainLossClass(portfolio.total_gain_loss)}`}
                          >
                            {formatGainLoss(portfolio.total_gain_loss)} (
                            {(
                              portfolio.total_gain_loss_percentage ?? 0
                            ).toFixed(2)}
                            %)
                          </span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Market News Call-to-Action Section */}
            <div className="bg-gray-800 rounded-2xl p-6 shadow-lg mb-8">
              <div className="flex items-center justify-between mb-6">
                <div className="flex items-center space-x-3">
                  <div className="p-2 bg-orange-600/20 rounded-lg">
                    <Newspaper className="h-6 w-6 text-orange-400" />
                  </div>
                  <div>
                    <h3 className="text-xl font-bold text-white">
                      Stay Informed with Market News
                    </h3>
                    <p className="text-gray-400 text-sm">
                      Get the latest insights on technology, finance, crypto,
                      and more
                    </p>
                  </div>
                </div>
                <Link
                  to="/news"
                  className="flex items-center space-x-2 px-6 py-3 bg-gradient-to-r from-orange-600 to-red-600 hover:from-orange-700 hover:to-red-700 rounded-lg transition-all text-white font-medium"
                >
                  <span>Read News</span>
                  <ArrowRight className="h-4 w-4" />
                </Link>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="bg-gray-700/30 rounded-xl p-4 border border-orange-500/20">
                  <div className="flex items-center space-x-3 mb-3">
                    <TrendingUp className="h-8 w-8 text-orange-400" />
                    <div>
                      <h4 className="font-semibold text-white">
                        Real-time Updates
                      </h4>
                      <p className="text-gray-400 text-sm">
                        Latest market developments
                      </p>
                    </div>
                  </div>
                </div>

                <div className="bg-gray-700/30 rounded-xl p-4 border border-blue-500/20">
                  <div className="flex items-center space-x-3 mb-3">
                    <Target className="h-8 w-8 text-blue-400" />
                    <div>
                      <h4 className="font-semibold text-white">
                        Category Filter
                      </h4>
                      <p className="text-gray-400 text-sm">
                        Technology, Finance, Crypto & more
                      </p>
                    </div>
                  </div>
                </div>

                <div className="bg-gray-700/30 rounded-xl p-4 border border-green-500/20">
                  <div className="flex items-center space-x-3 mb-3">
                    <Activity className="h-8 w-8 text-green-400" />
                    <div>
                      <h4 className="font-semibold text-white">
                        Market Insights
                      </h4>
                      <p className="text-gray-400 text-sm">
                        Expert analysis & trends
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Pro Tip Section */}
            <div className="bg-gray-800 rounded-2xl p-6 shadow-lg mb-8">
              <div className="flex items-center space-x-4 mb-4">
                <div className="bg-gray-700 p-3 rounded-full">
                  <Award className="h-6 w-6 text-yellow-400" />
                </div>
                <div>
                  <h3 className="font-bold text-lg">Pro Tip</h3>
                  <p className="text-sm opacity-90 text-gray-400">
                    Daily investment insight
                  </p>
                </div>
              </div>
              <p className="text-sm mb-4 opacity-90 text-gray-300">
                {userProfile?.experience_level === "BEGINNER"
                  ? "Start with diversified ETFs to reduce risk while learning the market basics."
                  : userProfile?.experience_level === "INTERMEDIATE"
                    ? "Consider dollar-cost averaging to smooth out market volatility over time."
                    : userProfile?.experience_level === "ADVANCED"
                      ? "Monitor sector rotation patterns to optimize your portfolio allocation."
                      : "Diversification is key to managing risk. Consider spreading your investments across different sectors and asset classes."}
              </p>
              {userProfile && userProfile.learning_progress < 100 && (
                <Link
                    to="/learning"
                    className="text-blue-400 hover:text-blue-300 text-sm flex items-center space-x-1"
                  >
                    <span>Learning Center</span>
                    <ArrowRight className="h-4 w-4" />
                  </Link>
              )}
            </div>

            {/* Goals Section - if user has profile */}
            {userProfile &&
              (userProfile.investment_goal_target_amount > 0 ||
                userProfile.personal_financial_goal_target_amount > 0) && (
                <div className="bg-gray-800 rounded-2xl p-6 shadow-lg mb-8">
                  <h3 className="text-xl font-bold text-white mb-6">
                    Your Goals
                  </h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {userProfile.investment_goal_target_amount > 0 && (
                      <div className="bg-gray-700/50 rounded-xl p-4">
                        <div className="flex items-center space-x-3 mb-3">
                          <Target className="h-6 w-6 text-blue-400" />
                          <div>
                            <h4 className="font-semibold text-white">
                              Investment Goal
                            </h4>
                            <p className="text-sm text-gray-400 capitalize">
                              {userProfile.investment_goal.replace("_", " ")}
                            </p>
                          </div>
                        </div>
                        <div className="space-y-2">
                          <div className="flex items-center justify-between">
                            <span className="text-gray-400 text-sm">
                              Target
                            </span>
                            <span className="text-white font-medium">
                              {formatCurrency(
                                userProfile.investment_goal_target_amount
                              )}
                            </span>
                          </div>
                          <div className="flex items-center justify-between">
                            <span className="text-gray-400 text-sm">
                              Progress
                            </span>
                            <span className="text-white font-medium">
                              {(
                                ((userDetails?.total_invested_amount || 0) /
                                  userProfile.investment_goal_target_amount) *
                                100
                              ).toFixed(1)}
                              %
                            </span>
                          </div>
                          <div className="w-full bg-gray-600 rounded-full h-2 mt-2">
                            <div
                              className="bg-blue-400 h-2 rounded-full"
                              style={{
                                width: `${Math.min(((userDetails?.total_invested_amount || 0) / userProfile.investment_goal_target_amount) * 100, 100)}%`,
                              }}
                            ></div>
                          </div>
                        </div>
                      </div>
                    )}

                    {userProfile.personal_financial_goal_target_amount > 0 && (
                      <div className="bg-gray-700/50 rounded-xl p-4">
                        <div className="flex items-center space-x-3 mb-3">
                          <Wallet className="h-6 w-6 text-green-400" />
                          <div>
                            <h4 className="font-semibold text-white">
                              Financial Goal
                            </h4>
                            <p className="text-sm text-gray-400 capitalize">
                              {userProfile.personal_financial_goal.replace(
                                "_",
                                " "
                              )}
                            </p>
                          </div>
                        </div>
                        <div className="space-y-2">
                          <div className="flex items-center justify-between">
                            <span className="text-gray-400 text-sm">
                              Target
                            </span>
                            <span className="text-white font-medium">
                              {formatCurrency(
                                userProfile.personal_financial_goal_target_amount
                              )}
                            </span>
                          </div>
                          <div className="flex items-center justify-between">
                            <span className="text-gray-400 text-sm">
                              Time Left
                            </span>
                            <span
                              className={`font-medium ${userProfile.is_goal_overdue ? "text-red-400" : "text-green-400"}`}
                            >
                              {userProfile.is_goal_overdue
                                ? "Overdue"
                                : `${userProfile.days_until_goal} days`}
                            </span>
                          </div>
                          {userProfile.personal_financial_goal_description && (
                            <p className="text-xs text-gray-400 mt-2">
                              {userProfile.personal_financial_goal_description}
                            </p>
                          )}
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              )}
          </>
        )}

        {/* User Profile Modal */}
        <UserProfileModal
          isOpen={showProfileModal}
          onClose={() => setShowProfileModal(false)}
        />
      </div>
    </div>
  );
};

export default DashboardPage;
