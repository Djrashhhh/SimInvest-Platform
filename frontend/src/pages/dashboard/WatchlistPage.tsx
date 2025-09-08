import React, { useState, useEffect } from "react";
import { Link, useSearchParams } from "react-router-dom";
import {
  Star,
  Plus,
  Search,
  RefreshCw,
  Bell,
  User,
  LogOut,
  Home,
  BarChart3,
  X,
  Edit3,
  Trash2,
  ChevronDown,
  AlertTriangle,
  Building2,
  ExternalLink,
} from "lucide-react";
import { useAuth } from "../../contexts/AuthContext";
import { useWatchlists, useSecuritySearch, useWatchlistValidation, useWatchlistUtils } from "../../hooks/useWatchlist";
import type { CreateWatchlistRequest, UpdateWatchlistRequest } from "../../types/watchlist";

const WatchlistPage: React.FC = () => {
  const { user, logout } = useAuth();
  const [searchParams] = useSearchParams();
  
  // Main watchlist hook
  const {
    watchlists,
    selectedWatchlist,
    watchlistStats,
    loading,
    error,
    loadWatchlistDetails,
    createWatchlist,
    updateWatchlist,
    deleteWatchlist,
    addSecurityToWatchlist,
    removeSecurityFromWatchlist,
    refreshData,
    setSelectedWatchlist,
    clearError,
  } = useWatchlists();

  // Security search hook
  const {
    searchResults,
    searchLoading,
    searchSecurities,
    clearSearch,
  } = useSecuritySearch();

  // Validation hook
  const { validateCreateRequest } = useWatchlistValidation();

  // Utilities hook
  const {
    formatLastUpdated,
    formatCurrency,
    canModifyWatchlist,
    isSecurityInWatchlist,
    sortWatchlistsByUpdated,
  } = useWatchlistUtils();

  // Local UI state
  const [searchTerm, setSearchTerm] = useState("");
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [showNotifications, setShowNotifications] = useState(false);
  
  // Form state
  const [createForm, setCreateForm] = useState<CreateWatchlistRequest>({
    name: "",
    description: "",
  });
  const [editForm, setEditForm] = useState<UpdateWatchlistRequest>({
    name: "",
    description: "",
  });
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});

  // Handle URL parameters (e.g., ?add=AAPL)
  useEffect(() => {
    const addSymbol = searchParams.get('add');
    if (addSymbol && watchlists.length > 0 && !selectedWatchlist) {
      // Load the first watchlist and add the symbol
      const firstWatchlist = sortWatchlistsByUpdated(watchlists)[0];
      loadWatchlistDetails(firstWatchlist.watchlist_id).then(() => {
        addSecurityToWatchlist(firstWatchlist.watchlist_id, addSymbol);
      });
    }
  }, [searchParams, watchlists, selectedWatchlist, loadWatchlistDetails, addSecurityToWatchlist, sortWatchlistsByUpdated]);

  // Debounced security search
  useEffect(() => {
    const timer = setTimeout(() => {
      if (searchTerm.trim()) {
        searchSecurities({ query: searchTerm, limit: 10 });
      } else {
        clearSearch();
      }
    }, 500);
    return () => clearTimeout(timer);
  }, [searchTerm, searchSecurities, clearSearch]);

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

  // Form handlers
  const handleCreateWatchlist = async () => {
    const errors = validateCreateRequest(createForm);
    setFormErrors(errors);
    
    if (Object.keys(errors).length > 0) {
      return;
    }

    try {
      await createWatchlist(createForm);
      setShowCreateModal(false);
      setCreateForm({ name: "", description: "" });
      setFormErrors({});
    } catch (error) {
      // Error is handled by the hook
    }
  };

  const handleUpdateWatchlist = async () => {
    if (!selectedWatchlist) return;

    const errors = validateCreateRequest(editForm as CreateWatchlistRequest);
    setFormErrors(errors);
    
    if (Object.keys(errors).length > 0) {
      return;
    }

    try {
      await updateWatchlist(selectedWatchlist.watchlist_id, editForm);
      setShowEditModal(false);
      setEditForm({ name: "", description: "" });
      setFormErrors({});
    } catch (error) {
      // Error is handled by the hook
    }
  };

  const handleDeleteWatchlist = async (watchlistId: number, watchlistName: string) => {
    if (window.confirm(`Delete "${watchlistName}"? This cannot be undone.`)) {
      try {
        await deleteWatchlist(watchlistId);
      } catch (error) {
        // Error is handled by the hook
      }
    }
  };

  const handleEditClick = (watchlistId: number, name: string, description: string | null) => {
    setEditForm({ name, description: description || "" });
    setShowEditModal(true);
  };

  if (!user) {
    return (
      <div className="min-h-screen bg-gray-900 text-white flex items-center justify-center">
        <div className="text-center">
          <p className="text-xl text-gray-400">Please log in to access your watchlists</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-white">
      {/* Header */}
      <header className="bg-slate-800/80 backdrop-blur-sm border-b border-slate-700/50 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center space-x-8">
              <Link to="/dashboard" className="flex items-center space-x-2">
                <BarChart3 className="h-8 w-8 text-blue-400" />
                <span className="text-xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-cyan-400">
                  SimInvest
                </span>
              </Link>

              <nav className="hidden md:flex space-x-6">
                <Link
                  to="/dashboard"
                  className="flex items-center space-x-1 text-slate-300 hover:text-white transition-colors"
                >
                  <Home className="h-4 w-4" />
                  <span>Dashboard</span>
                </Link>
                <Link
                  to="/market"
                  className="flex items-center space-x-1 text-slate-300 hover:text-white transition-colors"
                >
                  <BarChart3 className="h-4 w-4" />
                  <span>Market</span>
                </Link>
                <Link
                  to="/watchlist"
                  className="flex items-center space-x-1 text-blue-400"
                >
                  <Star className="h-4 w-4" />
                  <span>Watchlist</span>
                </Link>
              </nav>
            </div>

            <div className="flex items-center space-x-4">
              <div className="notifications-menu relative">
                <button
                  onClick={() => setShowNotifications(!showNotifications)}
                  className="relative p-2 text-slate-400 hover:text-white transition-colors"
                >
                  <Bell className="h-5 w-5" />
                </button>
              </div>

              <div className="user-menu relative">
                <button
                  onClick={() => setShowUserMenu(!showUserMenu)}
                  className="flex items-center space-x-2 p-2 rounded-lg hover:bg-slate-700 transition-colors"
                >
                  <User className="h-5 w-5 text-slate-400" />
                  <ChevronDown className={`h-4 w-4 text-slate-400 transition-transform ${showUserMenu ? "rotate-180" : ""}`} />
                </button>

                {showUserMenu && (
                  <div className="absolute right-0 mt-2 w-48 bg-slate-800 rounded-lg shadow-lg border border-slate-700 z-50">
                    <div className="p-2">
                      <Link
                        to="/dashboard"
                        className="flex items-center space-x-2 px-3 py-2 text-slate-300 hover:text-white hover:bg-slate-700 rounded-lg transition-colors"
                        onClick={() => setShowUserMenu(false)}
                      >
                        <Home className="h-4 w-4" />
                        <span>Dashboard</span>
                      </Link>
                      <button
                        onClick={() => {
                          logout();
                          setShowUserMenu(false);
                        }}
                        className="w-full flex items-center space-x-2 px-3 py-2 text-red-400 hover:bg-slate-700 rounded-lg transition-colors"
                      >
                        <LogOut className="h-4 w-4" />
                        <span>Log Out</span>
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Page Header */}
        <div className="mb-8">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-white mb-2">My Watchlists</h1>
              <p className="text-slate-400">Track your favorite securities and monitor their performance</p>
            </div>

            <div className="flex items-center space-x-3">
              <button
                onClick={refreshData}
                className="p-2 text-slate-400 hover:text-white transition-colors"
                title="Refresh"
              >
                <RefreshCw className={`h-5 w-5 ${loading.watchlists ? 'animate-spin' : ''}`} />
              </button>

              <button
                onClick={() => setShowCreateModal(true)}
                className="flex items-center space-x-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors"
              >
                <Plus className="h-4 w-4" />
                <span>New Watchlist</span>
              </button>
            </div>
          </div>
        </div>

        {/* Error Display */}
        {error && (
          <div className="mb-6 bg-red-900/20 border border-red-500/50 rounded-lg p-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <AlertTriangle className="h-5 w-5 text-red-400" />
                <span className="text-red-400 font-medium">{error}</span>
              </div>
              <button
                onClick={clearError}
                className="px-3 py-1 text-sm bg-red-600 hover:bg-red-700 rounded transition-colors"
              >
                Dismiss
              </button>
            </div>
          </div>
        )}

        {/* Stats Cards */}
        {watchlistStats && (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <div className="bg-slate-800/50 rounded-xl p-6 border border-slate-700/50">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-slate-400 text-sm">Total Watchlists</p>
                  <p className="text-2xl font-bold text-white">{watchlistStats.total_watchlists}</p>
                </div>
                <Star className="h-8 w-8 text-yellow-400" />
              </div>
            </div>

            <div className="bg-slate-800/50 rounded-xl p-6 border border-slate-700/50">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-slate-400 text-sm">Total Securities</p>
                  <p className="text-2xl font-bold text-white">{watchlistStats.total_securities}</p>
                </div>
                <Building2 className="h-8 w-8 text-blue-400" />
              </div>
            </div>

            <div className="bg-slate-800/50 rounded-xl p-6 border border-slate-700/50">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-slate-400 text-sm">Avg per Watchlist</p>
                  <p className="text-2xl font-bold text-white">
                    {watchlistStats.average_securities_per_watchlist.toFixed(1)}
                  </p>
                </div>
                <BarChart3 className="h-8 w-8 text-green-400" />
              </div>
            </div>
          </div>
        )}

        {/* Main Content Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          {/* Watchlist Sidebar */}
          <div className="lg:col-span-1">
            <div className="bg-slate-800/50 rounded-xl p-6 border border-slate-700/50">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-bold text-white">Your Watchlists</h3>
                <span className="text-sm text-slate-400">({watchlists.length})</span>
              </div>

              {loading.watchlists ? (
                <div className="flex justify-center py-8">
                  <RefreshCw className="h-6 w-6 text-blue-400 animate-spin" />
                </div>
              ) : watchlists.length === 0 ? (
                <div className="text-center py-8">
                  <Star className="h-12 w-12 text-slate-500 mx-auto mb-3" />
                  <p className="text-slate-400 text-sm mb-4">No watchlists yet</p>
                  <button
                    onClick={() => setShowCreateModal(true)}
                    className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors"
                  >
                    Create First Watchlist
                  </button>
                </div>
              ) : (
                <div className="space-y-3">
                  {sortWatchlistsByUpdated(watchlists).map((watchlist) => (
                    <div
                      key={watchlist.watchlist_id}
                      className={`p-3 rounded-lg border transition-all cursor-pointer ${
                        selectedWatchlist?.watchlist_id === watchlist.watchlist_id
                          ? "bg-blue-600/20 border-blue-500/50"
                          : "bg-slate-700/30 border-slate-600/30 hover:bg-slate-700/50"
                      }`}
                      onClick={() => loadWatchlistDetails(watchlist.watchlist_id)}
                    >
                      <div className="flex items-center justify-between mb-2">
                        <h4 className="font-medium text-white truncate">{watchlist.name}</h4>
                        <div className="flex items-center space-x-1">
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              handleEditClick(watchlist.watchlist_id, watchlist.name, watchlist.description);
                            }}
                            className="p-1 text-slate-400 hover:text-blue-400 transition-colors"
                          >
                            <Edit3 className="h-3 w-3" />
                          </button>
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              handleDeleteWatchlist(watchlist.watchlist_id, watchlist.name);
                            }}
                            className="p-1 text-slate-400 hover:text-red-400 transition-colors"
                          >
                            <Trash2 className="h-3 w-3" />
                          </button>
                        </div>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-slate-400">{watchlist.security_count} securities</span>
                        <span className="text-xs text-slate-500">{formatLastUpdated(watchlist.updated_at)}</span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Main Watchlist Content */}
          <div className="lg:col-span-3">
            {selectedWatchlist ? (
              <div className="space-y-6">
                {/* Watchlist Header */}
                <div className="bg-slate-800/50 rounded-xl p-6 border border-slate-700/50">
                  <div className="flex items-center justify-between mb-4">
                    <div>
                      <h2 className="text-2xl font-bold text-white">{selectedWatchlist.name}</h2>
                      {selectedWatchlist.description && (
                        <p className="text-slate-400 mt-1">{selectedWatchlist.description}</p>
                      )}
                    </div>
                    <div className="flex items-center space-x-2">
                      <span className="text-sm text-slate-400">
                        {selectedWatchlist.security_count} securities
                      </span>
                      <span className="text-xs text-slate-500">
                        Updated {formatLastUpdated(selectedWatchlist.updated_at)}
                      </span>
                    </div>
                  </div>

                  {/* Search and Add Securities */}
                  <div className="flex flex-col sm:flex-row gap-4">
                    <div className="flex-1 relative">
                      <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-slate-400" />
                      <input
                        type="text"
                        placeholder="Search securities to add (e.g., AAPL, Tesla)..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="w-full pl-10 pr-4 py-3 bg-slate-700/50 border border-slate-600/50 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      />
                      {searchLoading && (
                        <RefreshCw className="absolute right-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-slate-400 animate-spin" />
                      )}
                    </div>
                  </div>

                  {/* Search Results */}
                  {searchTerm && searchResults.length > 0 && (
                    <div className="mt-4 border-t border-slate-700/50 pt-4">
                      <h4 className="text-sm font-medium text-slate-300 mb-3">Search Results</h4>
                      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                        {searchResults.slice(0, 6).map((security) => {
                          const isAlreadyInWatchlist = isSecurityInWatchlist(selectedWatchlist, security.symbol);
                          
                          return (
                            <div
                              key={security.securityId}
                              className="bg-slate-700/30 rounded-lg p-3 border border-slate-600/30"
                            >
                              <div className="flex items-center justify-between mb-2">
                                <div className="flex-1 min-w-0">
                                  <div className="flex items-center space-x-2">
                                    <span className="font-medium text-white">{security.symbol}</span>
                                    <span className="text-xs px-2 py-0.5 bg-blue-600/30 text-blue-300 rounded">
                                      {security.exchange}
                                    </span>
                                  </div>
                                  <p className="text-slate-400 text-sm truncate" title={security.companyName}>
                                    {security.companyName}
                                  </p>
                                  <div className="flex items-center space-x-2 mt-1">
                                    <span className="text-white font-medium">
                                      {formatCurrency(security.currentPrice)}
                                    </span>
                                  </div>
                                </div>
                                <button
                                  onClick={() => addSecurityToWatchlist(selectedWatchlist.watchlist_id, security.symbol)}
                                  disabled={loading.addSecurity || isAlreadyInWatchlist}
                                  className="p-2 bg-green-600 hover:bg-green-700 disabled:bg-slate-600 text-white rounded-lg transition-colors"
                                  title={isAlreadyInWatchlist ? "Already in watchlist" : "Add to Watchlist"}
                                >
                                  <Plus className="h-4 w-4" />
                                </button>
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  )}
                </div>

                {/* Securities List */}
                <div className="bg-slate-800/50 rounded-xl border border-slate-700/50">
                  <div className="px-6 py-4 border-b border-slate-700/50">
                    <h3 className="text-lg font-bold text-white">
                      Securities ({selectedWatchlist.securities.length})
                    </h3>
                  </div>

                  {loading.selectedWatchlist ? (
                    <div className="flex justify-center py-12">
                      <RefreshCw className="h-8 w-8 text-blue-400 animate-spin" />
                    </div>
                  ) : selectedWatchlist.securities.length === 0 ? (
                    <div className="text-center py-12">
                      <Building2 className="h-12 w-12 text-slate-500 mx-auto mb-3" />
                      <p className="text-slate-400 mb-4">No securities in this watchlist yet</p>
                      <p className="text-slate-500 text-sm">Search and add securities above to get started</p>
                    </div>
                  ) : (
                    <div className="overflow-x-auto">
                      <table className="w-full">
                        <thead className="bg-slate-700/50">
                          <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">
                              Symbol
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">
                              Company
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">
                              Price
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">
                              Last Updated
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">
                              Actions
                            </th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-700/50">
                          {selectedWatchlist.securities.map((security) => (
                            <tr
                              key={security.security_id}
                              className="hover:bg-slate-700/25 transition-colors"
                            >
                              <td className="px-6 py-4 whitespace-nowrap">
                                <div className="flex items-center">
                                  <span className="text-sm font-medium text-white">
                                    {security.symbol}
                                  </span>
                                </div>
                              </td>
                              <td className="px-6 py-4">
                                <div className="text-sm text-white truncate max-w-xs" title={security.company_name}>
                                  {security.company_name}
                                </div>
                              </td>
                              <td className="px-6 py-4 whitespace-nowrap">
                                <div className="text-sm text-white font-medium">
                                  {formatCurrency(security.current_price)}
                                </div>
                              </td>
                              <td className="px-6 py-4 whitespace-nowrap">
                                <div className="text-sm text-slate-400">
                                  {formatLastUpdated(security.last_updated)}
                                </div>
                              </td>
                              <td className="px-6 py-4 whitespace-nowrap">
                                <div className="flex items-center space-x-2">
                                  <Link
                                    to={`/market?symbol=${security.symbol}`}
                                    className="text-blue-400 hover:text-blue-300 text-sm p-1 rounded hover:bg-slate-600 transition-colors"
                                    title="View in Market"
                                  >
                                    <ExternalLink className="h-4 w-4" />
                                  </Link>
                                  {canModifyWatchlist(selectedWatchlist) && (
                                    <button
                                      onClick={() => removeSecurityFromWatchlist(selectedWatchlist.watchlist_id, security.symbol)}
                                      disabled={loading.removeSecurity}
                                      className="text-red-400 hover:text-red-300 text-sm p-1 rounded hover:bg-slate-600 transition-colors disabled:opacity-50"
                                      title="Remove from Watchlist"
                                    >
                                      <Trash2 className="h-4 w-4" />
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
              </div>
            ) : (
              <div className="bg-slate-800/50 rounded-xl p-12 border border-slate-700/50 text-center">
                <Star className="h-16 w-16 text-slate-500 mx-auto mb-4" />
                <h3 className="text-xl font-bold text-white mb-2">Select a Watchlist</h3>
                <p className="text-slate-400 mb-6">
                  Choose a watchlist from the sidebar to view and manage your securities
                </p>
                {watchlists.length === 0 && (
                  <button
                    onClick={() => setShowCreateModal(true)}
                    className="px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors"
                  >
                    Create Your First Watchlist
                  </button>
                )}
              </div>
            )}
          </div>
        </div>

        {/* Quick Actions */}
        <div className="mt-8 bg-slate-800/50 rounded-xl p-6 border border-slate-700/50">
          <h3 className="text-lg font-bold text-white mb-4">Quick Actions</h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <Link
              to="/market"
              className="flex items-center justify-between p-3 bg-slate-700/50 hover:bg-slate-600/50 rounded-lg transition-colors"
            >
              <div className="flex items-center space-x-2">
                <BarChart3 className="h-4 w-4 text-blue-400" />
                <span className="text-white">Browse Market</span>
              </div>
              <ExternalLink className="h-4 w-4 text-slate-400" />
            </Link>

            <Link
              to="/dashboard"
              className="flex items-center justify-between p-3 bg-slate-700/50 hover:bg-slate-600/50 rounded-lg transition-colors"
            >
              <div className="flex items-center space-x-2">
                <Home className="h-4 w-4 text-green-400" />
                <span className="text-white">Dashboard</span>
              </div>
              <ExternalLink className="h-4 w-4 text-slate-400" />
            </Link>

            <button
              onClick={() => setShowCreateModal(true)}
              className="flex items-center justify-between p-3 bg-slate-700/50 hover:bg-slate-600/50 rounded-lg transition-colors"
            >
              <div className="flex items-center space-x-2">
                <Plus className="h-4 w-4 text-purple-400" />
                <span className="text-white">New Watchlist</span>
              </div>
            </button>

            <button
              onClick={refreshData}
              className="flex items-center justify-between p-3 bg-slate-700/50 hover:bg-slate-600/50 rounded-lg transition-colors"
            >
              <div className="flex items-center space-x-2">
                <RefreshCw className={`h-4 w-4 text-cyan-400 ${loading.watchlists ? 'animate-spin' : ''}`} />
                <span className="text-white">Refresh Data</span>
              </div>
            </button>
          </div>
        </div>
      </div>

      {/* Create Watchlist Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-slate-800 rounded-xl p-6 max-w-md w-full border border-slate-700">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-bold text-white">Create New Watchlist</h2>
              <button
                onClick={() => {
                  setShowCreateModal(false);
                  setCreateForm({ name: "", description: "" });
                  setFormErrors({});
                }}
                className="p-2 text-slate-400 hover:text-white transition-colors"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">
                  Name <span className="text-red-400">*</span>
                </label>
                <input
                  type="text"
                  value={createForm.name}
                  onChange={(e) => setCreateForm({ ...createForm, name: e.target.value })}
                  placeholder="Enter watchlist name"
                  className={`w-full px-4 py-2 bg-slate-700 border rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                    formErrors.name ? 'border-red-500' : 'border-slate-600'
                  }`}
                  maxLength={100}
                />
                {formErrors.name && (
                  <p className="mt-1 text-sm text-red-400">{formErrors.name}</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">
                  Description
                </label>
                <textarea
                  value={createForm.description}
                  onChange={(e) => setCreateForm({ ...createForm, description: e.target.value })}
                  placeholder="Optional description"
                  rows={3}
                  className={`w-full px-4 py-2 bg-slate-700 border rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none ${
                    formErrors.description ? 'border-red-500' : 'border-slate-600'
                  }`}
                  maxLength={500}
                />
                {formErrors.description && (
                  <p className="mt-1 text-sm text-red-400">{formErrors.description}</p>
                )}
              </div>

              <div className="flex items-center justify-end space-x-3 pt-4">
                <button
                  onClick={() => {
                    setShowCreateModal(false);
                    setCreateForm({ name: "", description: "" });
                    setFormErrors({});
                  }}
                  className="px-4 py-2 text-slate-400 hover:text-white transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={handleCreateWatchlist}
                  disabled={!createForm.name.trim() || loading.create}
                  className="px-6 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-slate-600 disabled:cursor-not-allowed text-white rounded-lg transition-colors flex items-center space-x-2"
                >
                  {loading.create ? (
                    <>
                      <RefreshCw className="h-4 w-4 animate-spin" />
                      <span>Creating...</span>
                    </>
                  ) : (
                    <>
                      <Plus className="h-4 w-4" />
                      <span>Create</span>
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Edit Watchlist Modal */}
      {showEditModal && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-slate-800 rounded-xl p-6 max-w-md w-full border border-slate-700">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-bold text-white">Edit Watchlist</h2>
              <button
                onClick={() => {
                  setShowEditModal(false);
                  setEditForm({ name: "", description: "" });
                  setFormErrors({});
                }}
                className="p-2 text-slate-400 hover:text-white transition-colors"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">
                  Name <span className="text-red-400">*</span>
                </label>
                <input
                  type="text"
                  value={editForm.name}
                  onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
                  placeholder="Enter watchlist name"
                  className={`w-full px-4 py-2 bg-slate-700 border rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                    formErrors.name ? 'border-red-500' : 'border-slate-600'
                  }`}
                  maxLength={100}
                />
                {formErrors.name && (
                  <p className="mt-1 text-sm text-red-400">{formErrors.name}</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">
                  Description
                </label>
                <textarea
                  value={editForm.description || ""}
                  onChange={(e) => setEditForm({ ...editForm, description: e.target.value })}
                  placeholder="Optional description"
                  rows={3}
                  className={`w-full px-4 py-2 bg-slate-700 border rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none ${
                    formErrors.description ? 'border-red-500' : 'border-slate-600'
                  }`}
                  maxLength={500}
                />
                {formErrors.description && (
                  <p className="mt-1 text-sm text-red-400">{formErrors.description}</p>
                )}
              </div>

              <div className="flex items-center justify-end space-x-3 pt-4">
                <button
                  onClick={() => {
                    setShowEditModal(false);
                    setEditForm({ name: "", description: "" });
                    setFormErrors({});
                  }}
                  className="px-4 py-2 text-slate-400 hover:text-white transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={handleUpdateWatchlist}
                  disabled={!editForm.name?.trim() || loading.update}
                  className="px-6 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-slate-600 disabled:cursor-not-allowed text-white rounded-lg transition-colors flex items-center space-x-2"
                >
                  {loading.update ? (
                    <>
                      <RefreshCw className="h-4 w-4 animate-spin" />
                      <span>Updating...</span>
                    </>
                  ) : (
                    <>
                      <Edit3 className="h-4 w-4" />
                      <span>Update</span>
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default WatchlistPage;