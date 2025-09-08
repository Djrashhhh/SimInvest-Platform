import React, { useState, useEffect } from 'react';
import {
  User,
  Mail,
  Calendar,
  Wallet,
  Shield,
  Target,
  TrendingUp,
  Edit,
  Save,
  X,
  Eye,
  EyeOff,
  Check,
  AlertCircle,
  Settings,
  DollarSign,
  Activity
} from 'lucide-react';
import { userService } from '../../services/userService';
import type { UserAccountDetails, UserProfile, UserProfileUpdateRequest, UserUpdateRequest } from '../../services/userService';
import { useAuth } from '../../contexts/AuthContext';

interface UserProfileModalProps {
  isOpen: boolean;
  onClose: () => void;
}

type TabType = 'account' | 'profile' | 'settings';

const UserProfileModal: React.FC<UserProfileModalProps> = ({ isOpen, onClose }) => {
  const { user: authUser } = useAuth();
  const [activeTab, setActiveTab] = useState<TabType>('account');
  const [userAccount, setUserAccount] = useState<UserAccountDetails | null>(null);
  const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [showBalance, setShowBalance] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Form states
  const [profileForm, setProfileForm] = useState<UserProfileUpdateRequest>({});
  const [accountForm, setAccountForm] = useState<UserUpdateRequest>({});
  const [showPassword, setShowPassword] = useState(false);

  useEffect(() => {
    if (isOpen && authUser) {
      fetchUserData();
    }
  }, [isOpen, authUser]);

  const fetchUserData = async () => {
    setIsLoading(true);
    setError(null);
    
    try {
      const accountData = await userService.getUserAccount();
      setUserAccount(accountData);
      
      try {
        const profileData = await userService.getUserProfile(accountData.user_id);
        setUserProfile(profileData);
        setProfileForm({
          experience_level: profileData.experience_level,
          investment_goal: profileData.investment_goal,
          personal_financial_goal: profileData.personal_financial_goal,
          preferred_investment_types: profileData.preferred_investment_types,
          investment_goal_target_amount: profileData.investment_goal_target_amount,
          investment_goal_target_date: profileData.investment_goal_target_date,
          personal_financial_goal_target_amount: profileData.personal_financial_goal_target_amount,
          personal_financial_goal_description: profileData.personal_financial_goal_description,
        });
      } catch (profileError: any) {
        console.log('Profile not found - user can create one', profileError.message);
        setUserProfile(null);
        setProfileForm({});
      }
      
      setAccountForm({
        email: accountData.email,
        risk_tolerance: accountData.risk_tolerance,
      });
    } catch (error: any) {
      console.error('Error fetching user data:', error);
      setError(`Failed to load user data: ${error.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSaveProfile = async () => {
    if (!userAccount) return;
    
    setError(null);
    setIsLoading(true);
    
    try {
      if (!profileForm.experience_level || !profileForm.investment_goal) {
        throw new Error('Experience level and investment goal are required');
      }

      let savedProfile: UserProfile;
      
      if (userProfile) {
        savedProfile = await userService.updateUserProfile(userAccount.user_id, profileForm);
        setSuccessMessage('Profile updated successfully!');
      } else {
        savedProfile = await userService.createUserProfile(userAccount.user_id, profileForm);
        setSuccessMessage('Profile created successfully!');
      }
      
      setUserProfile(savedProfile);
      setIsEditing(false);
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (error: any) {
      console.error('Error saving profile:', error);
      setError(`Failed to save profile: ${error.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSaveAccount = async () => {
    if (!userAccount) return;
    
    setError(null);
    setIsLoading(true);
    
    try {
      await userService.updateUser(userAccount.user_id, accountForm);
      
      const updatedAccountData = await userService.getUserAccount();
      setUserAccount(updatedAccountData);
      
      setIsEditing(false);
      setSuccessMessage('Account updated successfully!');
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (error: any) {
      console.error('Error saving account:', error);
      setError(`Failed to save account: ${error.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: userAccount?.account_currency || 'USD',
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const getStatusColor = (status: string) => {
    switch (status.toUpperCase()) {
      case 'ACTIVE': return 'text-green-400';
      case 'INACTIVE': return 'text-red-400';
      case 'SUSPENDED': return 'text-yellow-400';
      default: return 'text-gray-400';
    }
  };

  const getExperienceBadgeColor = (level?: string) => {
    switch (level) {
      case 'BEGINNER': return 'bg-green-600';
      case 'INTERMEDIATE': return 'bg-blue-600';
      case 'ADVANCED': return 'bg-purple-600';
      default: return 'bg-gray-600';
    }
  };

  // Helper function to format enum values for display
  const formatEnumForDisplay = (enumValue: string) => {
    return enumValue.toLowerCase().replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-gray-800 rounded-2xl shadow-2xl w-full max-w-4xl max-h-[90vh] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-700">
          <h2 className="text-2xl font-bold text-white">User Profile</h2>
          <button
            onClick={onClose}
            className="p-2 rounded-full hover:bg-gray-700 transition-colors"
          >
            <X className="h-6 w-6 text-gray-400" />
          </button>
        </div>

        {/* Tab Navigation */}
        <div className="flex border-b border-gray-700">
          {[
            { id: 'account' as TabType, label: 'Account', icon: User },
            { id: 'profile' as TabType, label: 'Profile', icon: Target },
            { id: 'settings' as TabType, label: 'Settings', icon: Settings },
          ].map(({ id, label, icon: Icon }) => (
            <button
              key={id}
              onClick={() => setActiveTab(id)}
              className={`flex items-center space-x-2 px-6 py-4 font-medium transition-colors ${
                activeTab === id
                  ? 'text-blue-400 border-b-2 border-blue-400 bg-gray-700/50'
                  : 'text-gray-400 hover:text-white hover:bg-gray-700/30'
              }`}
            >
              <Icon className="h-5 w-5" />
              <span>{label}</span>
            </button>
          ))}
        </div>

        {/* Content */}
        <div className="p-6 overflow-y-auto max-h-[60vh]">
          {/* Success/Error Messages */}
          {successMessage && (
            <div className="mb-4 p-4 bg-green-900/50 border border-green-500 rounded-lg flex items-center space-x-2">
              <Check className="h-5 w-5 text-green-400" />
              <span className="text-green-400">{successMessage}</span>
            </div>
          )}
          
          {error && (
            <div className="mb-4 p-4 bg-red-900/50 border border-red-500 rounded-lg flex items-center space-x-2">
              <AlertCircle className="h-5 w-5 text-red-400" />
              <span className="text-red-400">{error}</span>
            </div>
          )}

          {isLoading ? (
            <div className="flex justify-center items-center h-64">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-400"></div>
            </div>
          ) : (
            <>
              {/* Account Tab */}
              {activeTab === 'account' && userAccount && (
                <div className="space-y-6">
                  <div className="flex items-center justify-between">
                    <h3 className="text-xl font-bold text-white">Account Information</h3>
                    {!isEditing && (
                      <button
                        onClick={() => setIsEditing(true)}
                        className="flex items-center space-x-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors"
                      >
                        <Edit className="h-4 w-4" />
                        <span>Edit</span>
                      </button>
                    )}
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {/* Basic Info */}
                    <div className="bg-gray-700/50 rounded-xl p-4">
                      <h4 className="font-semibold text-white mb-4">Basic Information</h4>
                      <div className="space-y-3">
                        <div className="flex items-center space-x-3">
                          <User className="h-5 w-5 text-gray-400" />
                          <div>
                            <p className="text-gray-400 text-sm">Full Name</p>
                            <p className="text-white">{userAccount.full_name}</p>
                          </div>
                        </div>
                        <div className="flex items-center space-x-3">
                          <Mail className="h-5 w-5 text-gray-400" />
                          <div>
                            <p className="text-gray-400 text-sm">Email</p>
                            {isEditing ? (
                              <input
                                type="email"
                                value={accountForm.email || ''}
                                onChange={(e) => setAccountForm({ ...accountForm, email: e.target.value })}
                                className="bg-gray-600 text-white px-3 py-1 rounded border border-gray-500 focus:border-blue-400 focus:outline-none"
                              />
                            ) : (
                              <p className="text-white">{userAccount.email}</p>
                            )}
                          </div>
                        </div>
                        <div className="flex items-center space-x-3">
                          <Calendar className="h-5 w-5 text-gray-400" />
                          <div>
                            <p className="text-gray-400 text-sm">Member Since</p>
                            <p className="text-white">{formatDate(userAccount.created_at)}</p>
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* Account Status */}
                    <div className="bg-gray-700/50 rounded-xl p-4">
                      <h4 className="font-semibold text-white mb-4">Account Status</h4>
                      <div className="space-y-3">
                        <div className="flex items-center justify-between">
                          <span className="text-gray-400">Status</span>
                          <span className={`font-semibold ${getStatusColor(userAccount.account_status)}`}>
                            {userAccount.account_status}
                          </span>
                        </div>
                        <div className="flex items-center justify-between">
                          <span className="text-gray-400">Email Verified</span>
                          <span className={userAccount.email_verified ? 'text-green-400' : 'text-red-400'}>
                            {userAccount.email_verified ? 'Yes' : 'No'}
                          </span>
                        </div>
                        <div className="flex items-center justify-between">
                          <span className="text-gray-400">Active</span>
                          <span className={userAccount.is_active ? 'text-green-400' : 'text-red-400'}>
                            {userAccount.is_active ? 'Yes' : 'No'}
                          </span>
                        </div>
                        <div className="flex items-center justify-between">
                          <span className="text-gray-400">Risk Tolerance</span>
                          {isEditing ? (
                            <select
                              value={accountForm.risk_tolerance || ''}
                              onChange={(e) => setAccountForm({ ...accountForm, risk_tolerance: e.target.value as any })}
                              className="bg-gray-600 text-white px-3 py-1 rounded border border-gray-500 focus:border-blue-400 focus:outline-none"
                            >
                              <option value="CONSERVATIVE">Conservative</option>
                              <option value="MODERATE">Moderate</option>
                              <option value="AGGRESSIVE">Aggressive</option>
                            </select>
                          ) : (
                            <span className="text-white capitalize">{userAccount.risk_tolerance.toLowerCase()}</span>
                          )}
                        </div>
                      </div>
                    </div>

                    {/* Financial Summary */}
                    <div className="bg-gray-700/50 rounded-xl p-4 md:col-span-2">
                      <div className="flex items-center justify-between mb-4">
                        <h4 className="font-semibold text-white">Financial Summary</h4>
                        <button
                          onClick={() => setShowBalance(!showBalance)}
                          className="text-gray-400 hover:text-white"
                        >
                          {showBalance ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                        </button>
                      </div>
                      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                        <div className="flex items-center space-x-3">
                          <DollarSign className="h-8 w-8 text-green-400" />
                          <div>
                            <p className="text-gray-400 text-sm">Net Worth</p>
                            <p className="text-white font-bold">
                              {showBalance ? formatCurrency(userAccount.net_worth) : '***'}
                            </p>
                          </div>
                        </div>
                        <div className="flex items-center space-x-3">
                          <Wallet className="h-8 w-8 text-blue-400" />
                          <div>
                            <p className="text-gray-400 text-sm">Cash Balance</p>
                            <p className="text-white font-bold">
                              {showBalance ? formatCurrency(userAccount.current_virtual_balance) : '***'}
                            </p>
                          </div>
                        </div>
                        <div className="flex items-center space-x-3">
                          <TrendingUp className="h-8 w-8 text-purple-400" />
                          <div>
                            <p className="text-gray-400 text-sm">Invested</p>
                            <p className="text-white font-bold">
                              {showBalance ? formatCurrency(userAccount.total_invested_amount) : '***'}
                            </p>
                          </div>
                        </div>
                        <div className="flex items-center space-x-3">
                          <Activity className="h-8 w-8 text-yellow-400" />
                          <div>
                            <p className="text-gray-400 text-sm">Total Returns</p>
                            <p className={`font-bold ${userAccount.total_returns >= 0 ? 'text-green-400' : 'text-red-400'}`}>
                              {showBalance ? formatCurrency(userAccount.total_returns) : '***'}
                            </p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  {isEditing && (
                    <div className="flex justify-end space-x-3 pt-4 border-t border-gray-700">
                      <button
                        onClick={() => setIsEditing(false)}
                        className="px-4 py-2 bg-gray-600 hover:bg-gray-700 rounded-lg transition-colors"
                      >
                        Cancel
                      </button>
                      <button
                        onClick={handleSaveAccount}
                        className="flex items-center space-x-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors"
                      >
                        <Save className="h-4 w-4" />
                        <span>Save Changes</span>
                      </button>
                    </div>
                  )}
                </div>
              )}

              {/* Profile Tab */}
              {activeTab === 'profile' && (
                <div className="space-y-6">
                  <div className="flex items-center justify-between">
                    <h3 className="text-xl font-bold text-white">Investment Profile</h3>
                    {!isEditing && (
                      <button
                        onClick={() => setIsEditing(true)}
                        className="flex items-center space-x-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors"
                      >
                        <Edit className="h-4 w-4" />
                        <span>{userProfile ? 'Edit Profile' : 'Create Profile'}</span>
                      </button>
                    )}
                  </div>

                  {userProfile && (
                    <div className="bg-gray-700/50 rounded-xl p-4 mb-6">
                      <div className="flex items-center justify-between mb-4">
                        <h4 className="font-semibold text-white">Progress Overview</h4>
                        <div className={`px-3 py-1 rounded-full text-sm font-medium ${getExperienceBadgeColor(userProfile.experience_level)}`}>
                          {userProfile.experience_level}
                        </div>
                      </div>
                      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div>
                          <p className="text-gray-400 text-sm">Learning Progress</p>
                          <div className="flex items-center space-x-2">
                            <div className="flex-1 bg-gray-600 rounded-full h-2">
                              <div 
                                className="bg-blue-400 h-2 rounded-full" 
                                style={{ width: `${userProfile.progress_percentage}%` }}
                              ></div>
                            </div>
                            <span className="text-white text-sm">{userProfile.progress_percentage.toFixed(1)}%</span>
                          </div>
                        </div>
                        <div>
                          <p className="text-gray-400 text-sm">Investment Goal</p>
                          <p className="text-white">{formatEnumForDisplay(userProfile.investment_goal)}</p>
                        </div>
                        <div>
                          <p className="text-gray-400 text-sm">Days Until Goal</p>
                          <p className={`font-semibold ${userProfile.is_goal_overdue ? 'text-red-400' : 'text-green-400'}`}>
                            {userProfile.is_goal_overdue ? 'Overdue' : `${userProfile.days_until_goal} days`}
                          </p>
                        </div>
                      </div>
                    </div>
                  )}

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {/* Investment Preferences */}
                    <div className="bg-gray-700/50 rounded-xl p-4">
                      <h4 className="font-semibold text-white mb-4">Investment Preferences</h4>
                      <div className="space-y-4">
                        <div>
                          <label className="block text-gray-400 text-sm mb-2">Experience Level *</label>
                          {isEditing ? (
                            <select
                              value={profileForm.experience_level || ''}
                              onChange={(e) => setProfileForm({ ...profileForm, experience_level: e.target.value as any })}
                              className="w-full bg-gray-600 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-400 focus:outline-none"
                            >
                              <option value="">Select Level</option>
                              <option value="BEGINNER">Beginner</option>
                              <option value="INTERMEDIATE">Intermediate</option>
                              <option value="ADVANCED">Advanced</option>
                            </select>
                          ) : (
                            <p className="text-white">{userProfile?.experience_level || 'Not set'}</p>
                          )}
                        </div>

                        <div>
                          <label className="block text-gray-400 text-sm mb-2">Investment Goal *</label>
                          {isEditing ? (
                            <select
                              value={profileForm.investment_goal || ''}
                              onChange={(e) => setProfileForm({ ...profileForm, investment_goal: e.target.value as any })}
                              className="w-full bg-gray-600 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-400 focus:outline-none"
                            >
                              <option value="">Select Goal</option>
                              <option value="RETIREMENT">Retirement</option>
                              <option value="EDUCATION">Education</option>
                              <option value="TRAVEL">Travel</option>
                              <option value="HOME_PURCHASE">Home Purchase</option>
                              <option value="GENERIC_GROWTH">Generic Growth</option>
                              <option value="OTHER_PURCHASE">Other Purchase</option>
                            </select>
                          ) : (
                            <p className="text-white">{userProfile?.investment_goal ? formatEnumForDisplay(userProfile.investment_goal) : 'Not set'}</p>
                          )}
                        </div>

                        <div>
                          <label className="block text-gray-400 text-sm mb-2">Target Amount</label>
                          {isEditing ? (
                            <input
                              type="number"
                              value={profileForm.investment_goal_target_amount || ''}
                              onChange={(e) => setProfileForm({ ...profileForm, investment_goal_target_amount: parseFloat(e.target.value) || 0 })}
                              className="w-full bg-gray-600 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-400 focus:outline-none"
                              placeholder="Enter target amount"
                            />
                          ) : (
                            <p className="text-white">{userProfile?.investment_goal_target_amount ? formatCurrency(userProfile.investment_goal_target_amount) : 'Not set'}</p>
                          )}
                        </div>

                        <div>
                          <label className="block text-gray-400 text-sm mb-2">Target Date</label>
                          {isEditing ? (
                            <input
                              type="date"
                              value={profileForm.investment_goal_target_date || ''}
                              onChange={(e) => setProfileForm({ ...profileForm, investment_goal_target_date: e.target.value })}
                              className="w-full bg-gray-600 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-400 focus:outline-none"
                            />
                          ) : (
                            <p className="text-white">{userProfile?.investment_goal_target_date ? formatDate(userProfile.investment_goal_target_date) : 'Not set'}</p>
                          )}
                        </div>
                      </div>
                    </div>

                    {/* Personal Financial Goal */}
                    <div className="bg-gray-700/50 rounded-xl p-4">
                      <h4 className="font-semibold text-white mb-4">Personal Financial Goal</h4>
                      <div className="space-y-4">
                        <div>
                          <label className="block text-gray-400 text-sm mb-2">Goal Type</label>
                          {isEditing ? (
                            <select
                              value={profileForm.personal_financial_goal || ''}
                              onChange={(e) => setProfileForm({ ...profileForm, personal_financial_goal: e.target.value as any })}
                              className="w-full bg-gray-600 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-400 focus:outline-none"
                            >
                              <option value="">Select Goal</option>
                              <option value="DEBT_FREE">Debt Free</option>
                              <option value="TARGET_NET_WORTH">Target Net Worth</option>
                              <option value="EMERGENCY_FUND">Emergency Fund</option>
                              <option value="FINANCIAL_INDEPENDENCE">Financial Independence</option>
                              <option value="PASSIVE_INCOME">Passive Income</option>
                              <option value="OTHER">Other</option>
                            </select>
                          ) : (
                            <p className="text-white">{userProfile?.personal_financial_goal ? formatEnumForDisplay(userProfile.personal_financial_goal) : 'Not set'}</p>
                          )}
                        </div>

                        <div>
                          <label className="block text-gray-400 text-sm mb-2">Target Amount</label>
                          {isEditing ? (
                            <input
                              type="number"
                              value={profileForm.personal_financial_goal_target_amount || ''}
                              onChange={(e) => setProfileForm({ ...profileForm, personal_financial_goal_target_amount: parseFloat(e.target.value) || 0 })}
                              className="w-full bg-gray-600 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-400 focus:outline-none"
                              placeholder="Enter target amount"
                            />
                          ) : (
                            <p className="text-white">{userProfile?.personal_financial_goal_target_amount ? formatCurrency(userProfile.personal_financial_goal_target_amount) : 'Not set'}</p>
                          )}
                        </div>

                        <div>
                          <label className="block text-gray-400 text-sm mb-2">Description</label>
                          {isEditing ? (
                            <textarea
                              value={profileForm.personal_financial_goal_description || ''}
                              onChange={(e) => setProfileForm({ ...profileForm, personal_financial_goal_description: e.target.value })}
                              className="w-full bg-gray-600 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-400 focus:outline-none"
                              rows={3}
                              placeholder="Describe your financial goal..."
                              maxLength={500}
                            />
                          ) : (
                            <p className="text-white">{userProfile?.personal_financial_goal_description || 'Not set'}</p>
                          )}
                        </div>
                      </div>
                    </div>

                    {/* Preferred Investment Types */}
                    <div className="bg-gray-700/50 rounded-xl p-4 md:col-span-2">
                      <h4 className="font-semibold text-white mb-4">Preferred Investment Types</h4>
                      {isEditing ? (
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                          {['STOCKS', 'MUTUAL_FUNDS', 'ETFs', 'CRYPTO'].map((type) => (
                            <label key={type} className="flex items-center space-x-2 cursor-pointer">
                              <input
                                type="checkbox"
                                checked={profileForm.preferred_investment_types?.includes(type as any) || false}
                                onChange={(e) => {
                                  const current = profileForm.preferred_investment_types || [];
                                  if (e.target.checked) {
                                    setProfileForm({ ...profileForm, preferred_investment_types: [...current, type as any] });
                                  } else {
                                    setProfileForm({ ...profileForm, preferred_investment_types: current.filter(t => t !== type) });
                                  }
                                }}
                                className="rounded border-gray-500 bg-gray-600 text-blue-500 focus:ring-blue-400"
                              />
                              <span className="text-white">{formatEnumForDisplay(type)}</span>
                            </label>
                          ))}
                        </div>
                      ) : (
                        <div className="flex flex-wrap gap-2">
                          {userProfile?.preferred_investment_types?.map((type) => (
                            <span key={type} className="px-3 py-1 bg-blue-600 text-white rounded-full text-sm">
                              {formatEnumForDisplay(type)}
                            </span>
                          )) || <span className="text-gray-400">Not set</span>}
                        </div>
                      )}
                    </div>
                  </div>

                  {isEditing && (
                    <div className="flex justify-end space-x-3 pt-4 border-t border-gray-700">
                      <button
                        onClick={() => {
                          setIsEditing(false);
                          // Reset form to current profile data
                          if (userProfile) {
                            setProfileForm({
                              experience_level: userProfile.experience_level,
                              investment_goal: userProfile.investment_goal,
                              personal_financial_goal: userProfile.personal_financial_goal,
                              preferred_investment_types: userProfile.preferred_investment_types,
                              investment_goal_target_amount: userProfile.investment_goal_target_amount,
                              investment_goal_target_date: userProfile.investment_goal_target_date,
                              personal_financial_goal_target_amount: userProfile.personal_financial_goal_target_amount,
                              personal_financial_goal_description: userProfile.personal_financial_goal_description,
                            });
                          } else {
                            setProfileForm({});
                          }
                        }}
                        className="px-4 py-2 bg-gray-600 hover:bg-gray-700 rounded-lg transition-colors"
                      >
                        Cancel
                      </button>
                      <button
                        onClick={handleSaveProfile}
                        disabled={isLoading}
                        className="flex items-center space-x-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors disabled:opacity-50"
                      >
                        <Save className="h-4 w-4" />
                        <span>{isLoading ? 'Saving...' : 'Save Profile'}</span>
                      </button>
                    </div>
                  )}
                </div>
              )}

              {/* Settings Tab */}
              {activeTab === 'settings' && (
                <div className="space-y-6">
                  <h3 className="text-xl font-bold text-white">Account Settings</h3>
                  
                  <div className="bg-gray-700/50 rounded-xl p-4">
                    <h4 className="font-semibold text-white mb-4">Security</h4>
                    <div className="space-y-4">
                      <div>
                        <label className="block text-gray-400 text-sm mb-2">Change Password</label>
                        <div className="relative">
                          <input
                            type={showPassword ? "text" : "password"}
                            value={accountForm.password || ''}
                            onChange={(e) => setAccountForm({ ...accountForm, password: e.target.value })}
                            className="w-full bg-gray-600 text-white px-3 py-2 pr-10 rounded border border-gray-500 focus:border-blue-400 focus:outline-none"
                            placeholder="Enter new password"
                          />
                          <button
                            type="button"
                            onClick={() => setShowPassword(!showPassword)}
                            className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-white"
                          >
                            {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                          </button>
                        </div>
                        <p className="text-xs text-gray-400 mt-1">Minimum 8 characters required</p>
                      </div>

                      <div>
                        <label className="block text-gray-400 text-sm mb-2">Security Question</label>
                        <select
                          value={accountForm.security_question || ''}
                          onChange={(e) => setAccountForm({ ...accountForm, security_question: e.target.value as any })}
                          className="w-full bg-gray-600 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-400 focus:outline-none"
                        >
                          <option value="">Select a security question</option>
                          <option value="FIRST_PET">What was your first pet's name?</option>
                          <option value="MOTHERS_MAIDEN_NAME">What is your mother's maiden name?</option>
                          <option value="CHILDHOOD_STREET">What street did you grow up on?</option>
                          <option value="FIRST_SCHOOL">What was the name of your first school?</option>
                        </select>
                      </div>

                      <div>
                        <label className="block text-gray-400 text-sm mb-2">Security Answer</label>
                        <input
                          type="text"
                          value={accountForm.security_answer || ''}
                          onChange={(e) => setAccountForm({ ...accountForm, security_answer: e.target.value })}
                          className="w-full bg-gray-600 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-400 focus:outline-none"
                          placeholder="Enter your security answer"
                        />
                      </div>
                    </div>
                  </div>

                  <div className="bg-gray-700/50 rounded-xl p-4">
                    <h4 className="font-semibold text-white mb-4">Preferences</h4>
                    <div className="space-y-4">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="text-white">Email Notifications</p>
                          <p className="text-gray-400 text-sm">Receive updates about your investments</p>
                        </div>
                        <label className="relative inline-flex items-center cursor-pointer">
                          <input type="checkbox" className="sr-only peer" />
                          <div className="w-11 h-6 bg-gray-600 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                        </label>
                      </div>
                      
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="text-white">Market Alerts</p>
                          <p className="text-gray-400 text-sm">Get notified about market changes</p>
                        </div>
                        <label className="relative inline-flex items-center cursor-pointer">
                          <input type="checkbox" className="sr-only peer" defaultChecked />
                          <div className="w-11 h-6 bg-gray-600 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                        </label>
                      </div>
                    </div>
                  </div>

                  <div className="flex justify-end space-x-3 pt-4 border-t border-gray-700">
                    <button
                      onClick={handleSaveAccount}
                      disabled={isLoading}
                      className="flex items-center space-x-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors disabled:opacity-50"
                    >
                      <Save className="h-4 w-4" />
                      <span>{isLoading ? 'Saving...' : 'Save Settings'}</span>
                    </button>
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default UserProfileModal;