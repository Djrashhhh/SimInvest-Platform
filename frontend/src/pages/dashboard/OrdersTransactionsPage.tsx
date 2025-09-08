import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import {
  ArrowLeft,
  Plus,
  TrendingUp,
  TrendingDown,
  DollarSign,
  BarChart3,
  Eye,
  EyeOff,
  RefreshCw,
  AlertCircle,
  CheckCircle,
  Clock,
  X,
  ShoppingCart,
  Receipt,
  Activity,
  Filter,
  Search,
  Calendar,
  ExternalLink,
} from "lucide-react";
import { useAuth } from "../../contexts/AuthContext";
import { useOrdersTransactions } from "../../hooks/useOrdersTransactions";
import type { 
  OrderRequestDTO,
  OrderResponseDTO,
  TransactionResponseDTO 
} from "../../types/orders";
import type { ApiResponse } from "../../types/market";
import { balanceService } from '../../services/balanceService';

// Create Order Modal Component
const CreateOrderModal = ({
  isOpen,
  onClose,
  orderType,
  onOrderSubmitted,
  portfolioId,
  createOrder, //Added createOrder prop to use the createOrder function from the hook
}: {
  isOpen: boolean;
  onClose: () => void;
  orderType: "BUY" | "SELL";
  onOrderSubmitted: () => void;
  portfolioId: number | null;
  createOrder: (orderRequest: OrderRequestDTO) => Promise<ApiResponse<OrderResponseDTO>>;    //Added createOrder prop type
}) => {
  const [formData, setFormData] = useState<OrderRequestDTO>({
    portfolio_id: portfolioId || 0,
    stock_symbol: "",
    quantity: 1,
    order_type: "MARKET",
    order_side: "BUY",  // Default to BUY, will be updated based on orderType prop
    notes: "",
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Update portfolio_id when it changes
  useEffect(() => {
    if (portfolioId) {
      setFormData(prev => ({ ...prev, portfolio_id: portfolioId }));
    }
  }, [portfolioId]);

  // FIXED: Update order_side when orderType changes
  useEffect(() => {
    setFormData(prev => ({ ...prev, order_side: orderType }));
  }, [orderType]);

  // FIXED: Reset form when modal opens/closes
  useEffect(() => {
    if (isOpen) {
      setFormData({
        portfolio_id: portfolioId || 0,
        stock_symbol: "",
        quantity: 1,
        order_type: "MARKET",
        order_side: orderType, // Set the correct order side when opening
        notes: "",
      });
      setError(null);
    }
  }, [isOpen, orderType, portfolioId]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!portfolioId) {
      setError("No active portfolio found. Please create a portfolio first.");
      return;
    }

    setIsSubmitting(true);
    setError(null);

     try {
      // FIXED: Use the hook's createOrder function instead of direct API call
      const result = await createOrder({
        ...formData,
        portfolio_id: portfolioId, // Ensure we use the correct portfolio ID
      });

      if (result.success) {
        onOrderSubmitted();
        onClose();
        // Reset form
        setFormData({
          portfolio_id: portfolioId,
          stock_symbol: "",
          quantity: 1,
          order_type: "MARKET",
          order_side: orderType,
          notes: "",
        });
      } else {
        setError(result.error || "Failed to create order");
      }
    } catch (error) {
      setError("Failed to create order");
    } finally {
      setIsSubmitting(false);
    }
  };

 return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-gray-800 rounded-2xl p-6 w-full max-w-md mx-4">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold text-white">
            {orderType === "BUY" ? "Buy Order" : "Sell Order"}
          </h2>
          <button onClick={onClose} className="text-gray-400 hover:text-white">
            <X className="h-6 w-6" />
          </button>
        </div>

        {error && (
          <div className="mb-4 p-3 bg-red-900/20 border border-red-500/50 rounded-lg">
            <p className="text-red-400 text-sm">{error}</p>
          </div>
        )}

        {/* DEBUG: Show current order_side for verification }
        <div className="mb-4 p-2 bg-blue-900/20 border border-blue-500/50 rounded-lg">
          <p className="text-blue-400 text-xs">
            Debug: Order Side = {formData.order_side} | Order Type = {orderType}
          </p>
        </div> */}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">
              Stock Symbol
            </label>
            <input
              type="text"
              value={formData.stock_symbol}
              onChange={(e) =>
                setFormData((prev) => ({
                  ...prev,
                  stock_symbol: e.target.value.toUpperCase(),
                }))
              }
              className="w-full bg-gray-700 border border-gray-600 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="AAPL"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">
              Quantity
            </label>
            <input
              type="number"
              value={formData.quantity}
              onChange={(e) =>
                setFormData((prev) => ({
                  ...prev,
                  quantity: Number(e.target.value),
                }))
              }
              className="w-full bg-gray-700 border border-gray-600 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              min="1"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">
              Order Type
            </label>
            <select
              value={formData.order_type}
              onChange={(e) =>
                setFormData((prev) => ({
                  ...prev,
                  order_type: e.target.value as "MARKET" | "LIMIT",
                }))
              }
              className="w-full bg-gray-700 border border-gray-600 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="MARKET">Market Order</option>
              <option value="LIMIT">Limit Order</option>
            </select>
          </div>

          {formData.order_type === "LIMIT" && (
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">
                Limit Price ($)
              </label>
              <input
                type="number"
                value={formData.order_price || ""}
                onChange={(e) =>
                  setFormData((prev) => ({
                    ...prev,
                    order_price: e.target.value
                      ? Number(e.target.value)
                      : undefined,
                  }))
                }
                className="w-full bg-gray-700 border border-gray-600 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                step="0.01"
                min="0"
                required
              />
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">
              Notes (Optional)
            </label>
            <textarea
              value={formData.notes || ""}
              onChange={(e) =>
                setFormData((prev) => ({ ...prev, notes: e.target.value }))
              }
              className="w-full bg-gray-700 border border-gray-600 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              rows={2}
              placeholder="Order notes..."
            />
          </div>

          <div className="flex space-x-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2 border border-gray-600 text-gray-300 rounded-lg hover:bg-gray-700 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting || !portfolioId}
              className="flex-1 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors disabled:opacity-50"
            >
              {isSubmitting ? "Creating..." : `${orderType} Stock`}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
  

const OrdersTransactionsPage = () => {
  const { user } = useAuth();
  const {
    orders,
    transactions,
    activeOrders,
    recentTransactions,
    portfolioId,
    hasPortfolio,
    orderStats,
    transactionAnalytics,
    isLoading,
    error,
    createOrder,
    cancelOrder,
    executeOrder,
    refreshData,
    getOrdersByStatus,
    getOrdersBySide,
    getTransactionsByStatus,
    getTransactionsByType,
    setError,
  } = useOrdersTransactions();

  const [activeTab, setActiveTab] = useState<"orders" | "transactions">("orders");
  const [showOrderModal, setShowOrderModal] = useState(false);
  const [orderType, setOrderType] = useState<"BUY" | "SELL">("BUY");
  const [filterStatus, setFilterStatus] = useState<string>("all");

  // Mock portfolio stats - replace with actual data from your hooks
  const [portfolioStats, setPortfolioStats] = useState({
    totalValue: 0,
    cashBalance: 0,
    totalGainLoss: 0,
    totalGainLossPercentage: 0,
    activeOrders: activeOrders.length,
    pendingTransactions: transactions.filter(t => t.transaction_status === "PENDING").length,
  });

  // Update portfolio stats when data changes
  useEffect(() => {
    setPortfolioStats(prev => ({
      ...prev,
      activeOrders: activeOrders.length,
      pendingTransactions: transactions.filter(t => t.transaction_status === "PENDING").length,
    }));

    // Fetch real portfolio data when component mounts or when orders/transactions change
  fetchRealPortfolioData();
  }, [activeOrders, transactions, user?.user_id]);

  const handleOrderSubmitted = async () => {       //refresh portfolio data after order is submitted
    await refreshData();
    await fetchRealPortfolioData();

  };

  const openOrderModal = (type: "BUY" | "SELL") => {
    if (!hasPortfolio || !portfolioId) {
      setError("Please create a portfolio before placing orders.");
      return;
    }
    setOrderType(type);
    setShowOrderModal(true);
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const getOrderStatusColor = (status: string) => {
    switch (status) {
      case "FILLED":
        return "text-green-400";
      case "PENDING":
        return "text-yellow-400";
      case "CANCELLED":
        return "text-red-400";
      case "FAILED":
      case "REJECTED":
        return "text-red-400";
      case "PARTIALLY_FILLED":
        return "text-blue-400";
      case "EXPIRED":
        return "text-gray-400";
      default:
        return "text-gray-400";
    }
  };

  const getTransactionStatusColor = (status: string) => {
    switch (status) {
      case "COMPLETED":
        return "text-green-400";
      case "PENDING":
        return "text-yellow-400";
      case "FAILED":
        return "text-red-400";
      case "CANCELED":
        return "text-red-400";
      default:
        return "text-gray-400";
    }
  };

  const fetchRealPortfolioData = async () => {          // New function to fetch real portfolio data
  if (!user?.user_id) return;

  try {
    const balanceData = await balanceService.getCombinedBalanceData(user.user_id);
    
    setPortfolioStats(prev => ({
      ...prev,
      totalValue: balanceData.calculated.actual_net_worth,
      cashBalance: balanceData.calculated.actual_cash_balance,
      totalGainLoss: balanceData.calculated.actual_total_returns,
      totalGainLossPercentage: balanceData.calculated.actual_net_worth > 0 
        ? (balanceData.calculated.actual_total_returns / balanceData.calculated.actual_net_worth) * 100 
        : 0,
    }));
  } catch (error) {
    console.error('Error fetching real portfolio data:', error);
  }
};

  const filteredOrders = getOrdersByStatus(filterStatus);
  const filteredTransactions = getTransactionsByStatus(filterStatus);

  if (!user) {
    return (
      <div className="min-h-screen bg-gray-900 text-white flex items-center justify-center">
        <div className="text-center">
          <p className="text-xl text-gray-400">
            Please log in to access orders and transactions
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-900 text-white p-4 sm:p-8">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div className="flex items-center space-x-4">
            <Link
              to="/dashboard"
              className="p-2 rounded-lg bg-gray-800 hover:bg-gray-700 transition-colors"
            >
              <ArrowLeft className="h-6 w-6 text-gray-400" />
            </Link>
            <div>
              <h1 className="text-3xl font-bold text-white">
                Orders & Transactions
              </h1>
              <p className="text-gray-400">Manage your trading activity</p>
            </div>
          </div>
          <div className="flex items-center space-x-2">
            <button
              onClick={async () => {
                refreshData ();
                await fetchRealPortfolioData();   // Refresh portfolio data on button click
              }}
              disabled={isLoading}
              className="p-2 rounded-lg bg-gray-800 hover:bg-gray-700 transition-colors disabled:opacity-50"
              title="Refresh data"
            >
              <RefreshCw className={`h-5 w-5 text-gray-400 ${isLoading ? 'animate-spin' : ''}`} />
            </button>
          </div>
        </div>

        {/* Portfolio Check Warning */}
        {!hasPortfolio && (
          <div className="mb-8 p-4 bg-yellow-900/20 border border-yellow-500/50 rounded-lg">
            <div className="flex items-center space-x-2">
              <AlertCircle className="h-5 w-5 text-yellow-400" />
              <p className="text-yellow-400">
                You don't have an active portfolio. Please create one to start trading.
              </p>
            </div>
          </div>
        )}

        {/* Portfolio Stats Overview */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="bg-gray-800 rounded-2xl p-6 shadow-lg">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-medium text-gray-400">
                Portfolio Value
              </h3>
              <TrendingUp className="h-6 w-6 text-green-400" />
            </div>
            <p className="text-3xl font-bold text-white">
              {formatCurrency(portfolioStats.totalValue)}
            </p>
          </div>

          <div className="bg-gray-800 rounded-2xl p-6 shadow-lg">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-medium text-gray-400">
                Cash Balance
              </h3>
              <DollarSign className="h-6 w-6 text-green-400" />
            </div>
            <p className="text-3xl font-bold text-white">
              {formatCurrency(portfolioStats.cashBalance)}
            </p>
          </div>

          <div className="bg-gray-800 rounded-2xl p-6 shadow-lg">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-medium text-gray-400">
                Total Gain/Loss
              </h3>
              {portfolioStats.totalGainLoss >= 0 ? (
                <TrendingUp className="h-6 w-6 text-green-400" />
              ) : (
                <TrendingDown className="h-6 w-6 text-red-400" />
              )}
            </div>
            <p
              className={`text-3xl font-bold ${portfolioStats.totalGainLoss >= 0 ? "text-green-400" : "text-red-400"}`}
            >
              {portfolioStats.totalGainLoss >= 0 ? "+" : ""}
              {formatCurrency(portfolioStats.totalGainLoss)}
            </p>
            <p
              className={`text-sm ${portfolioStats.totalGainLoss >= 0 ? "text-green-400" : "text-red-400"}`}
            >
              {portfolioStats.totalGainLoss >= 0 ? "+" : ""}
              {portfolioStats.totalGainLossPercentage.toFixed(2)}%
            </p>
          </div>

          <div className="bg-gray-800 rounded-2xl p-6 shadow-lg">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-medium text-gray-400">
                Active Orders
              </h3>
              <Activity className="h-6 w-6 text-blue-400" />
            </div>
            <p className="text-3xl font-bold text-white">
              {portfolioStats.activeOrders}
            </p>
          </div>
        </div>

        {/* Trading Actions */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-8">
          <button
            onClick={() => openOrderModal("BUY")}
            disabled={!hasPortfolio}
            className="bg-green-600 hover:bg-green-700 rounded-xl p-6 shadow-lg transition-all duration-200 hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <div className="flex items-center justify-between">
              <div className="text-left">
                <h3 className="font-bold text-lg text-white">Buy Securities</h3>
                <p className="text-green-100 text-sm">
                  Purchase stocks and ETFs
                </p>
              </div>
              <ShoppingCart className="h-8 w-8 text-white" />
            </div>
          </button>

          <button
            onClick={() => openOrderModal("SELL")}
            disabled={!hasPortfolio}
            className="bg-red-600 hover:bg-red-700 rounded-xl p-6 shadow-lg transition-all duration-200 hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <div className="flex items-center justify-between">
              <div className="text-left">
                <h3 className="font-bold text-lg text-white">
                  Sell Securities
                </h3>
                <p className="text-red-100 text-sm">Sell your holdings</p>
              </div>
              <Receipt className="h-8 w-8 text-white" />
            </div>
          </button>
        </div>

        {/* Tab Navigation */}
        <div className="flex items-center justify-between mb-6">
          <div className="flex space-x-1 bg-gray-800 rounded-lg p-1">
            <button
              onClick={() => setActiveTab("orders")}
              className={`flex items-center space-x-2 px-4 py-2 rounded-lg transition-colors ${
                activeTab === "orders"
                  ? "bg-blue-600 text-white"
                  : "text-gray-400 hover:text-white hover:bg-gray-700"
              }`}
            >
              <Receipt className="h-4 w-4" />
              <span>Orders ({orders.length})</span>
            </button>
            <button
              onClick={() => setActiveTab("transactions")}
              className={`flex items-center space-x-2 px-4 py-2 rounded-lg transition-colors ${
                activeTab === "transactions"
                  ? "bg-blue-600 text-white"
                  : "text-gray-400 hover:text-white hover:bg-gray-700"
              }`}
            >
              <Activity className="h-4 w-4" />
              <span>Transactions ({transactions.length})</span>
            </button>
          </div>

          {/* Filter */}
          <div className="flex items-center space-x-2">
            <Filter className="h-4 w-4 text-gray-400" />
            <select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
              className="bg-gray-800 border border-gray-600 rounded-lg px-3 py-1 text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="all">All Status</option>
              <option value="pending">Pending</option>
              <option value="completed">Completed</option>
              <option value="filled">Filled</option>
              <option value="cancelled">Cancelled</option>
              <option value="failed">Failed</option>
            </select>
          </div>
        </div>

        {/* Content */}
        {isLoading ? (
          <div className="flex justify-center items-center h-64">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-400"></div>
          </div>
        ) : error ? (
          <div className="text-center text-red-500 my-8 bg-red-900/20 border border-red-500/50 rounded-lg p-6">
            <AlertCircle className="h-12 w-12 text-red-400 mx-auto mb-4" />
            <p className="text-lg mb-4">{error}</p>
            <button
              onClick={() => {
                setError(null);
                refreshData();
              }}
              className="px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors"
            >
              Try Again
            </button>
          </div>
        ) : (
          <div className="bg-gray-800 rounded-2xl p-6 shadow-lg">
            {activeTab === "orders" ? (
              <div>
                <h3 className="text-xl font-bold text-white mb-6">
                  Your Orders
                </h3>

                {filteredOrders.length === 0 ? (
                  <div className="text-center py-12">
                    <Receipt className="h-16 w-16 text-gray-600 mx-auto mb-4" />
                    <h4 className="text-lg font-medium text-gray-400 mb-2">
                      No Orders Found
                    </h4>
                    <p className="text-gray-500 mb-6">
                      {filterStatus === "all"
                        ? "Start trading by placing your first order"
                        : `No ${filterStatus} orders found`}
                    </p>
                    {hasPortfolio && (
                      <button
                        onClick={() => openOrderModal("BUY")}
                        className="px-6 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors"
                      >
                        Place Order
                      </button>
                    )}
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left">
                      <thead>
                        <tr className="border-b border-gray-700">
                          <th className="py-3 px-4 text-gray-400 font-medium">
                            Symbol
                          </th>
                          <th className="py-3 px-4 text-gray-400 font-medium">
                            Side
                          </th>
                          <th className="py-3 px-4 text-gray-400 font-medium">
                            Type
                          </th>
                          <th className="py-3 px-4 text-gray-400 font-medium">
                            Quantity
                          </th>
                          <th className="py-3 px-4 text-gray-400 font-medium">
                            Price
                          </th>
                          <th className="py-3 px-4 text-gray-400 font-medium">
                            Status
                          </th>
                          <th className="py-3 px-4 text-gray-400 font-medium">
                            Date
                          </th>
                          <th className="py-3 px-4 text-gray-400 font-medium">
                            Actions
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        {filteredOrders.map((order) => (
                          <tr
                            key={order.order_id}
                            className="border-b border-gray-700/50 hover:bg-gray-700/20"
                          >
                            <td className="py-4 px-4">
                              <div>
                                <span className="font-medium text-white">
                                  {order.stock_symbol}
                                </span>
                                <p className="text-gray-400 text-xs">
                                  {order.company_name}
                                </p>
                              </div>
                            </td>
                            <td className="py-4 px-4">
                              <span
                                className={`px-2 py-1 rounded text-xs font-medium ${
                                  order.order_side === "BUY"
                                    ? "bg-green-900 text-green-200"
                                    : "bg-red-900 text-red-200"
                                }`}
                              >
                                {order.order_side}
                              </span>
                            </td>
                            <td className="py-4 px-4 text-white">
                              {order.order_type}
                            </td>
                            <td className="py-4 px-4 text-white">
                              <div>
                                <span>{order.quantity}</span>
                                {order.filled_quantity > 0 && (
                                  <p className="text-xs text-gray-400">
                                    Filled: {order.filled_quantity}
                                  </p>
                                )}
                              </div>
                            </td>
                            <td className="py-4 px-4 text-white">
                              {order.order_price
                                ? formatCurrency(order.order_price)
                                : "Market"}
                            </td>
                            <td
                              className={`py-4 px-4 font-medium ${getOrderStatusColor(order.order_status)}`}
                            >
                              {order.order_status}
                            </td>
                            <td className="py-4 px-4 text-gray-400 text-sm">
                              {formatDate(order.order_placed_date)}
                            </td>
                            <td className="py-4 px-4">
                              <div className="flex items-center space-x-2">
                                {order.can_be_cancelled && (
                                  <button
                                    onClick={() => cancelOrder(order.order_id)}
                                    className="px-2 py-1 bg-red-600 hover:bg-red-700 text-white text-xs rounded transition-colors"
                                  >
                                    Cancel
                                  </button>
                                )}
                                {order.order_status === "PENDING" && (
                                  <button
                                    onClick={() => executeOrder(order.order_id)}
                                    className="px-2 py-1 bg-blue-600 hover:bg-blue-700 text-white text-xs rounded transition-colors"
                                  >
                                    Execute
                                  </button>
                                )}
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            ) : (
              <div>
                <h3 className="text-xl font-bold text-white mb-6">
                  Your Transactions
                </h3>

                {filteredTransactions.length === 0 ? (
                  <div className="text-center py-12">
                    <Activity className="h-16 w-16 text-gray-600 mx-auto mb-4" />
                    <h4 className="text-lg font-medium text-gray-400 mb-2">
                      No Transactions Found
                    </h4>
                    <p className="text-gray-500 mb-6">
                      {filterStatus === "all"
                        ? "Your transaction history will appear here"
                        : `No ${filterStatus} transactions found`}
                    </p>
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left">
                      <thead>
                        <tr className="border-b border-gray-700">
                          <th className="py-3 px-4 text-gray-400 font-medium">
                            Symbol
                          </th>
                          <th className="py-3 px-4 text-gray-400 font-medium">
                            Type
                          </th>
                          <th className="py-3 px-4 text-gray-400 font-medium">
                            Quantity
                          </th>
                          <th className="py-3 px-4 text-gray-400 font-medium">
                            Price
                          </th>
                          <th className="py-3 px-4 text-gray-400 font-medium">
                            Total
                          </th>
                          <th className="py-3 px-4 text-gray-400 font-medium">
                            Fees
                          </th>
                          <th className="py-3 px-4 text-gray-400 font-medium">
                            Status
                          </th>
                          <th className="py-3 px-4 text-gray-400 font-medium">
                            Date
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        {filteredTransactions.map((transaction) => (
                          <tr
                            key={transaction.transaction_id}
                            className="border-b border-gray-700/50 hover:bg-gray-700/20"
                          >
                            <td className="py-4 px-4">
                              <div>
                                <span className="font-medium text-white">
                                  {transaction.stock_symbol}
                                </span>
                                <p className="text-gray-400 text-xs">
                                  {transaction.company_name}
                                </p>
                              </div>
                            </td>
                            <td className="py-4 px-4">
                              <span
                                className={`px-2 py-1 rounded text-xs font-medium ${
                                  transaction.transaction_type === "BUY"
                                    ? "bg-green-900 text-green-200"
                                    : transaction.transaction_type === "SELL"
                                      ? "bg-red-900 text-red-200"
                                      : transaction.transaction_type ===
                                          "DIVIDEND"
                                        ? "bg-blue-900 text-blue-200"
                                        : "bg-gray-900 text-gray-200"
                                }`}
                              >
                                {transaction.transaction_type}
                              </span>
                            </td>
                            <td className="py-4 px-4 text-white">
                              {transaction.quantity}
                            </td>
                            <td className="py-4 px-4 text-white">
                              {formatCurrency(transaction.price_per_share)}
                            </td>
                            <td className="py-4 px-4 text-white">
                              {formatCurrency(transaction.total_amount)}
                            </td>
                            <td className="py-4 px-4 text-white">
                              {formatCurrency(transaction.fees)}
                            </td>
                            <td
                              className={`py-4 px-4 font-medium ${getTransactionStatusColor(transaction.transaction_status)}`}
                            >
                              <div className="flex items-center space-x-2">
                                <span>{transaction.transaction_status}</span>
                                {transaction.is_settled && (
                                  <CheckCircle
                                    className="h-4 w-4 text-green-400"
                                    title="Settled"
                                  />
                                )}
                                {!transaction.is_settled &&
                                  transaction.transaction_status ===
                                    "PENDING" && (
                                    <Clock
                                      className="h-4 w-4 text-yellow-400"
                                      title="Settling"
                                    />
                                  )}
                              </div>
                            </td>
                            <td className="py-4 px-4 text-gray-400 text-sm">
                              <div>
                                <p>
                                  {formatDate(transaction.transaction_date)}
                                </p>
                                {transaction.settlement_date && (
                                  <p className="text-xs text-gray-500">
                                    Settles:{" "}
                                    {formatDate(transaction.settlement_date)}
                                  </p>
                                )}
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            )}
          </div>
        )}

        {/* Quick Navigation */}
        <div className="mt-8 flex justify-center space-x-4">
          <Link
            to="/dashboard"
            className="flex items-center space-x-2 px-6 py-3 bg-gray-800 hover:bg-gray-700 text-white rounded-lg transition-colors"
          >
            <ArrowLeft className="h-5 w-5" />
            <span>Dashboard</span>
          </Link>
          <Link
            to="/market"
            className="flex items-center space-x-2 px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors"
          >
            <BarChart3 className="h-5 w-5" />
            <span>Market Data</span>
          </Link>
        </div>

        {/* Create Order Modal */}
        <CreateOrderModal
          isOpen={showOrderModal}
          onClose={() => setShowOrderModal(false)}
          orderType={orderType}
          onOrderSubmitted={handleOrderSubmitted}
          portfolioId={portfolioId}
          createOrder={createOrder} // Add this line - pass the createOrder function from the hook
        />
      </div>
    </div>
  );
};

export default OrdersTransactionsPage;