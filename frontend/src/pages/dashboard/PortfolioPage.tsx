import React, { useState } from "react";
import { Link } from "react-router-dom";
import {
  ArrowLeft,
  Plus,
  PieChart,
  TrendingUp,
  TrendingDown,
  DollarSign,
  BarChart3,
  Eye,
  EyeOff,
  RefreshCw,
  AlertCircle,
  X,
  Wallet,
  Target,
  Activity,
  Edit3,
} from "lucide-react";
import { useAuth } from "../../contexts/AuthContext";
import { usePortfolio } from "../../hooks/usePortfolio";
import type { CreatePortfolioRequest, Position } from "../../types/portfolio";


// Create Portfolio Modal Component
const CreatePortfolioModal = ({ isOpen, onClose, onSubmit, isLoading }: {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: CreatePortfolioRequest) => void;
  isLoading: boolean;
}) => {
  const [formData, setFormData] = useState({
    portfolio_name: '',
    initial_cash_balance: 10000
  });

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit(formData);
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-gray-800 rounded-2xl p-6 w-full max-w-md mx-4">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold text-white">Create New Portfolio</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-white">
            <X className="h-6 w-6" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">
              Portfolio Name
            </label>
            <input
              type="text"
              value={formData.portfolio_name}
              onChange={(e) => setFormData(prev => ({ ...prev, portfolio_name: e.target.value }))}
              className="w-full bg-gray-700 border border-gray-600 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="My Investment Portfolio"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">
              Initial Cash Balance
            </label>
            <input
              type="number"
              value={formData.initial_cash_balance}
              onChange={(e) => setFormData(prev => ({ ...prev, initial_cash_balance: Number(e.target.value) }))}
              className="w-full bg-gray-700 border border-gray-600 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              min="0"
              step="0.01"
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
              disabled={isLoading}
              className="flex-1 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors disabled:opacity-50"
            >
              {isLoading ? 'Creating...' : 'Create Portfolio'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// Cash Management Modal Component
const CashManagementModal = ({ isOpen, onClose, portfolioId, currentBalance, onCashUpdated }: {
  isOpen: boolean;
  onClose: () => void;
  portfolioId: number;
  currentBalance: number;
  onCashUpdated: () => void;
}) => {
  const { addCash, withdrawCash } = usePortfolio(0);
  const [action, setAction] = useState<'add' | 'withdraw'>('add');
  const [amount, setAmount] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const numAmount = parseFloat(amount);
    
    if (numAmount <= 0) {
      setError('Amount must be greater than 0');
      return;
    }

    if (action === 'withdraw' && numAmount > currentBalance) {
      setError('Insufficient balance');
      return;
    }

    try {
      setIsLoading(true);
      setError(null);
      
      if (action === 'add') {
        await addCash(numAmount);
      } else {
        await withdrawCash(numAmount);
      }
      
      onCashUpdated();
      onClose();
      setAmount('');
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Transaction failed');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-gray-800 rounded-2xl p-6 w-full max-w-md mx-4">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold text-white">Manage Cash</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-white">
            <X className="h-6 w-6" />
          </button>
        </div>

        <div className="mb-4">
          <p className="text-gray-400 text-sm">
            Current Balance: <span className="text-white font-medium">${currentBalance.toFixed(2)}</span>
          </p>
        </div>

        {error && (
          <div className="mb-4 p-3 bg-red-900/20 border border-red-500/50 rounded-lg">
            <p className="text-red-400 text-sm">{error}</p>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">Action</label>
            <div className="flex space-x-2">
              <button
                type="button"
                onClick={() => setAction('add')}
                className={`flex-1 py-2 px-4 rounded-lg transition-colors ${
                  action === 'add' 
                    ? 'bg-green-600 text-white' 
                    : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
                }`}
              >
                Add Cash
              </button>
              <button
                type="button"
                onClick={() => setAction('withdraw')}
                className={`flex-1 py-2 px-4 rounded-lg transition-colors ${
                  action === 'withdraw' 
                    ? 'bg-red-600 text-white' 
                    : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
                }`}
              >
                Withdraw
              </button>
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">Amount ($)</label>
            <input
              type="number"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              className="w-full bg-gray-700 border border-gray-600 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              step="0.01"
              min="0"
              required
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
              disabled={isLoading}
              className="flex-1 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors disabled:opacity-50"
            >
              {isLoading ? 'Processing...' : `${action === 'add' ? 'Add' : 'Withdraw'} Cash`}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// Main Portfolio Page Component
const PortfolioPage = () => {
  const { user } = useAuth();
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showCashModal, setShowCashModal] = useState(false);
  const [showBalance, setShowBalance] = useState(true);

  // FIXED: Use user_id instead of userId
  const userId = user?.user_id || user?.user_id || 0;
  
  const {
    portfolio,
    portfolioSummary,
    positions,
    isLoading,
    error,
    hasPortfolio,
    createPortfolio,
    refreshData,
    setError,
  } = usePortfolio(userId);

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  const getGainLossClass = (value: number) => {
    if (value > 0) return 'text-green-400';
    if (value < 0) return 'text-red-400';
    return 'text-gray-400';
  };

  const formatGainLoss = (value: number) => {
    if (value > 0) return `+${value.toFixed(2)}`;
    return value.toFixed(2);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const handleCreatePortfolio = async (data: CreatePortfolioRequest) => {
    try {
      await createPortfolio(data);
      setShowCreateModal(false);
    } catch (error) {
      console.error('Failed to create portfolio:', error);
    }
  };

  const handleCashUpdated = () => {
    refreshData();
  };

  // Show loading state
  if (!user) {
    return (
      <div className="min-h-screen bg-gray-900 text-white flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-400 mx-auto mb-4"></div>
          <p className="text-xl text-gray-400">Loading user data...</p>
        </div>
      </div>
    );
  }

  // Show error for invalid user
  if (!userId || userId <= 0) {
    return (
      <div className="min-h-screen bg-gray-900 text-white flex items-center justify-center">
        <div className="text-center max-w-md mx-auto p-6">
          <AlertCircle className="h-12 w-12 text-red-400 mx-auto mb-4" />
          <h2 className="text-xl font-bold mb-2">Invalid User Session</h2>
          <p className="text-gray-400 mb-4">Please log in again to access your portfolio.</p>
          <Link
            to="/login"
            className="px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors"
          >
            Go to Login
          </Link>
        </div>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-900 text-white flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-400 mx-auto mb-4"></div>
          <p className="text-xl text-gray-400">Loading portfolio...</p>
          <p className="text-sm text-gray-500 mt-2">User ID: {userId}</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-900 text-white flex items-center justify-center">
        <div className="text-center max-w-md mx-auto p-6">
          <AlertCircle className="h-12 w-12 text-red-400 mx-auto mb-4" />
          <h2 className="text-xl font-bold mb-2">Error Loading Portfolio</h2>
          <p className="text-gray-400 mb-4">{error}</p>
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
      </div>
    );
  }

  // No Portfolio State
  if (!hasPortfolio) {
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
                <h1 className="text-3xl font-bold text-white">Portfolio</h1>
                <p className="text-gray-400">Create your first investment portfolio</p>
              </div>
            </div>
          </div>

          {/* No Portfolio Message */}
          <div className="text-center py-16">
            <div className="bg-gray-800 rounded-2xl p-12 shadow-lg max-w-2xl mx-auto">
              <div className="flex items-center justify-center mb-6">
                <div className="bg-blue-600 p-4 rounded-full">
                  <PieChart className="h-12 w-12 text-white" />
                </div>
              </div>
              <h2 className="text-3xl font-bold text-white mb-4">
                Create Your Investment Portfolio
              </h2>
              <p className="text-gray-400 text-lg mb-8">
                Start your investment journey by creating a portfolio to track and manage your investments.
              </p>
              <button
                onClick={() => setShowCreateModal(true)}
                className="inline-flex items-center px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors"
              >
                <Plus className="h-5 w-5 mr-2" />
                Create Portfolio
              </button>
            </div>
          </div>

          <CreatePortfolioModal
            isOpen={showCreateModal}
            onClose={() => setShowCreateModal(false)}
            onSubmit={handleCreatePortfolio}
            isLoading={isLoading}
          />
        </div>
      </div>
    );
  }

  // Main Portfolio View
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
              <h1 className="text-3xl font-bold text-white">{portfolio?.portfolio_name || 'My Portfolio'}</h1>
              <p className="text-gray-400">Portfolio Overview & Management</p>
            </div>
          </div>
          <div className="flex items-center space-x-2">
            <button
              onClick={refreshData}
              className="p-2 rounded-lg bg-gray-800 hover:bg-gray-700 transition-colors"
              title="Refresh portfolio data"
            >
              <RefreshCw className="h-5 w-5 text-gray-400" />
            </button>
            <button
              onClick={() => setShowCashModal(true)}
              className="flex items-center space-x-2 px-4 py-2 bg-green-600 hover:bg-green-700 text-white font-medium rounded-lg transition-colors"
            >
              <Wallet className="h-5 w-5" />
              <span>Manage Cash</span>
            </button>
          </div>
        </div>

        {/* Portfolio Overview Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="bg-gray-800 rounded-2xl p-6 shadow-lg">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-medium text-gray-400">Total Value</h3>
              <button onClick={() => setShowBalance(!showBalance)} className="text-gray-400 hover:text-white">
                {showBalance ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
              </button>
            </div>
            <p className="text-3xl font-bold text-white">
              {showBalance ? formatCurrency(portfolio?.total_value || 0) : '***'}
            </p>
          </div>

          <div className="bg-gray-800 rounded-2xl p-6 shadow-lg">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-medium text-gray-400">Cash Balance</h3>
              <DollarSign className="h-6 w-6 text-green-400" />
            </div>
            <p className="text-3xl font-bold text-white">
              {showBalance ? formatCurrency(portfolio?.cash_balance || 0) : '***'}
            </p>
          </div>

          <div className="bg-gray-800 rounded-2xl p-6 shadow-lg">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-medium text-gray-400">Total Gain/Loss</h3>
              {portfolioSummary && portfolioSummary.total_gain_loss >= 0 ? 
                <TrendingUp className="h-6 w-6 text-green-400" /> : 
                <TrendingDown className="h-6 w-6 text-red-400" />
              }
            </div>
            <p className={`text-3xl font-bold ${getGainLossClass(portfolioSummary?.total_gain_loss || 0)}`}>
              {showBalance ? formatGainLoss(portfolioSummary?.total_gain_loss || 0) : '***'}
            </p>
            {portfolioSummary && (
              <p className={`text-sm ${getGainLossClass(portfolioSummary.total_gain_loss)}`}>
                {portfolioSummary.total_gain_loss_percentage.toFixed(2)}%
              </p>
            )}
          </div>

          <div className="bg-gray-800 rounded-2xl p-6 shadow-lg">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-medium text-gray-400">Positions</h3>
              <Target className="h-6 w-6 text-blue-400" />
            </div>
            <p className="text-3xl font-bold text-white">
              {portfolioSummary?.position_count || 0}
            </p>
          </div>
        </div>

        {/* Portfolio Details */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
          {/* Portfolio Information */}
          <div className="lg:col-span-2 bg-gray-800 rounded-2xl p-6 shadow-lg">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-xl font-bold text-white">Portfolio Details</h3>
              <button className="p-2 rounded-lg bg-gray-700 hover:bg-gray-600 transition-colors">
                <Edit3 className="h-5 w-5 text-gray-400" />
              </button>
            </div>

            <div className="space-y-4">
              <div className="flex items-center justify-between py-3 border-b border-gray-700">
                <span className="text-gray-400">Portfolio Name</span>
                <span className="text-white font-medium">{portfolio?.portfolio_name}</span>
              </div>
              
              <div className="flex items-center justify-between py-3 border-b border-gray-700">
                <span className="text-gray-400">Created Date</span>
                <span className="text-white font-medium">
                  {portfolio?.created_date ? formatDate(portfolio.created_date) : 'N/A'}
                </span>
              </div>
              
              <div className="flex items-center justify-between py-3 border-b border-gray-700">
                <span className="text-gray-400">Last Updated</span>
                <span className="text-white font-medium">
                  {portfolio?.last_updated ? formatDate(portfolio.last_updated) : 'N/A'}
                </span>
              </div>
              
              <div className="flex items-center justify-between py-3 border-b border-gray-700">
                <span className="text-gray-400">Status</span>
                <span className={`font-medium px-2 py-1 rounded-full text-xs ${
                  portfolio?.is_active ? 'bg-green-900 text-green-300' : 'bg-red-900 text-red-300'
                }`}>
                  {portfolio?.is_active ? 'Active' : 'Inactive'}
                </span>
              </div>

              <div className="flex items-center justify-between py-3">
                <span className="text-gray-400">Invested Amount</span>
                <span className="text-white font-medium">
                  {formatCurrency(portfolioSummary?.invested_amount || 0)}
                </span>
              </div>
            </div>
          </div>

          {/* Performance Summary */}
          <div className="bg-gray-800 rounded-2xl p-6 shadow-lg">
            <h3 className="text-xl font-bold text-white mb-6">Performance Summary</h3>
            <div className="space-y-4">
              <div className="flex justify-between items-center">
                <span className="text-gray-400">Unrealized P&L</span>
                <span className={`font-medium ${getGainLossClass(portfolio?.total_unrealized_gain_loss || 0)}`}>
                  {formatGainLoss(portfolio?.total_unrealized_gain_loss || 0)}
                </span>
              </div>
              
              <div className="flex justify-between items-center">
                <span className="text-gray-400">Realized P&L</span>
                <span className={`font-medium ${getGainLossClass(portfolio?.total_realized_gain_loss || 0)}`}>
                  {formatGainLoss(portfolio?.total_realized_gain_loss || 0)}
                </span>
              </div>
              
              <div className="flex justify-between items-center">
                <span className="text-gray-400">Total Return</span>
                <span className={`font-medium ${getGainLossClass(portfolioSummary?.total_gain_loss || 0)}`}>
                  {portfolioSummary?.total_gain_loss_percentage.toFixed(2) || '0.00'}%
                </span>
              </div>
            </div>

            {/* Asset Allocation Placeholder */}
            <div className="mt-8 pt-6 border-t border-gray-700">
              <h4 className="text-lg font-medium text-white mb-4">Asset Allocation</h4>
              <div className="flex items-center justify-center h-32 text-gray-400">
                <div className="text-center">
                  <PieChart className="h-12 w-12 mx-auto mb-2" />
                  <p className="text-sm">Chart visualization</p>
                  <p className="text-xs">Coming soon</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Quick Actions */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <Link
            to="/orders-transactions"
            className="bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 rounded-xl p-6 shadow-lg transition-all duration-200 hover:scale-105"
          >
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-bold text-lg text-white">Trading Hub</h3>
                <p className="text-blue-100 text-sm">Buy & sell securities</p>
              </div>
              <BarChart3 className="h-8 w-8 text-white" />
            </div>
          </Link>

          <Link
            to="/market"
            className="bg-gradient-to-r from-green-600 to-green-700 hover:from-green-700 hover:to-green-800 rounded-xl p-6 shadow-lg transition-all duration-200 hover:scale-105"
          >
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-bold text-lg text-white">Market Data</h3>
                <p className="text-green-100 text-sm">Research & analysis</p>
              </div>
              <Activity className="h-8 w-8 text-white" />
            </div>
          </Link>

          <button
            onClick={() => setShowCashModal(true)}
            className="bg-gradient-to-r from-purple-600 to-purple-700 hover:from-purple-700 hover:to-purple-800 rounded-xl p-6 shadow-lg transition-all duration-200 hover:scale-105"
          >
            <div className="flex items-center justify-between">
              <div className="text-left">
                <h3 className="font-bold text-lg text-white">Cash Management</h3>
                <p className="text-purple-100 text-sm">Add or withdraw funds</p>
              </div>
              <Wallet className="h-8 w-8 text-white" />
            </div>
          </button>
        </div>

        {/* Portfolio Positions */}
        <div className="bg-gray-800 rounded-2xl p-6 shadow-lg">
          <div className="flex items-center justify-between mb-6">
            <h3 className="text-xl font-bold text-white">Your Positions</h3>
            <div className="flex items-center space-x-2">
              <span className="text-gray-400 text-sm">{positions.length} positions</span>
              <button
                onClick={refreshData}
                className="p-2 rounded-lg bg-gray-700 hover:bg-gray-600 transition-colors"
                title="Refresh positions"
              >
                <RefreshCw className="h-4 w-4 text-gray-400" />
              </button>
            </div>
          </div>

          {positions.length === 0 ? (
            <div className="text-center py-12">
              <Target className="h-16 w-16 text-gray-600 mx-auto mb-4" />
              <h4 className="text-lg font-medium text-gray-400 mb-2">No Positions Yet</h4>
              <p className="text-gray-500 mb-6">Start investing to build your portfolio</p>
              <Link
                to="/orders-transactions"
                className="px-6 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors"
              >
                Start Trading
              </Link>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left">
                <thead>
                  <tr className="border-b border-gray-700">
                    <th className="py-3 px-4 text-gray-400 font-medium">Symbol</th>
<th className="py-3 px-4 text-gray-400 font-medium">Quantity</th>
<th className="py-3 px-4 text-gray-400 font-medium">Avg Cost</th>
<th className="py-3 px-4 text-gray-400 font-medium">Current Price</th>
<th className="py-3 px-4 text-gray-400 font-medium">Market Value</th>
<th className="py-3 px-4 text-gray-400 font-medium">Day Change</th>
<th className="py-3 px-4 text-gray-400 font-medium">Total P&L</th>
<th className="py-3 px-4 text-gray-400 font-medium">%</th>
                  </tr>
                </thead>
                <tbody>
                  {positions.map((position) => {
                    const gainLossClass = getGainLossClass(position.unrealized_gain_loss || 0);
                    const dayChangeClass = getGainLossClass(position.day_change || 0);
                    
                    return (
                      <tr key={position.position_id} className="border-b border-gray-700/50 hover:bg-gray-700/20">
                        <td className="py-4 px-4">
                          <div>
                            <span className="font-medium text-white">{position.security_symbol}</span>
                            <p className="text-gray-400 text-xs">{position.company_name}</p>
                          </div>
                        </td>
                        <td className="py-4 px-4 text-white">
                          {parseFloat(position.quantity || 0).toFixed(2)}
                        </td>
                        <td className="py-4 px-4 text-white">
                          {formatCurrency(position.avg_cost_per_share || 0)}
                        </td>
                        <td className="py-4 px-4 text-white">
                          {formatCurrency(position.current_price || 0)}
                        </td>
                        <td className="py-4 px-4 text-white">
                          {formatCurrency(position.current_value || 0)}
                        </td>
                        <td className={`py-4 px-4 font-medium ${dayChangeClass}`}>
                          <div>
                            <span>{formatGainLoss(position.day_change || 0)}</span>
                            <div className="text-xs">
                              ({(position.day_change_percent || 0).toFixed(2)}%)
                            </div>
                          </div>
                        </td>
                        <td className={`py-4 px-4 font-medium ${gainLossClass}`}>
                          {formatGainLoss(position.unrealized_gain_loss || 0)}
                        </td>
                        <td className={`py-4 px-4 font-medium ${gainLossClass}`}>
                          {(position.unrealized_gain_loss_percentage || 0).toFixed(2)}%
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Modals */}
        <CreatePortfolioModal
          isOpen={showCreateModal}
          onClose={() => setShowCreateModal(false)}
          onSubmit={handleCreatePortfolio}
          isLoading={isLoading}
        />

        <CashManagementModal
          isOpen={showCashModal}
          onClose={() => setShowCashModal(false)}
          portfolioId={portfolio?.portfolio_id || 0}
          currentBalance={portfolio?.cash_balance || 0}
          onCashUpdated={handleCashUpdated}
        />
      </div>
    </div>
  );
};

export default PortfolioPage;