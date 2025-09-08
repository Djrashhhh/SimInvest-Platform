// userService.ts
const API_BASE_URL = "http://localhost:8080/api/v1";

// Types based on your DTOs - FIXED to match Java enums exactly
export type ExperienceLevel = "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
export type InvestmentGoalType =
  | "RETIREMENT"
  | "EDUCATION"
  | "TRAVEL"
  | "HOME_PURCHASE"
  | "GENERIC_GROWTH"
  | "OTHER_PURCHASE";
export type PersonalFinancialGoalType =
  | "DEBT_FREE"
  | "TARGET_NET_WORTH"
  | "EMERGENCY_FUND"
  | "FINANCIAL_INDEPENDENCE"
  | "PASSIVE_INCOME"
  | "OTHER";
export type InvestmentType = "STOCKS" | "MUTUAL_FUNDS" | "ETFs" | "CRYPTO";
export type RiskTolerance = "CONSERVATIVE" | "MODERATE" | "AGGRESSIVE";
export type SecurityQuestion =
  | "FIRST_PET"
  | "MOTHERS_MAIDEN_NAME"
  | "CHILDHOOD_STREET"
  | "FIRST_SCHOOL";

export interface UserAccountDetails {
  user_id: number;
  first_name: string;
  last_name: string;
  email: string;
  username: string;
  risk_tolerance: RiskTolerance;
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
}

export interface UserProfile {
  profile_id: number;
  user_id: number;
  experience_level: ExperienceLevel;
  investment_goal: InvestmentGoalType;
  personal_financial_goal: PersonalFinancialGoalType;
  preferred_investment_types: InvestmentType[];
  investment_goal_target_amount: number;
  investment_goal_target_date: string;
  personal_financial_goal_target_amount: number;
  personal_financial_goal_description: string;
  learning_progress: number;
  progress_percentage: number;
  days_until_goal: number;
  is_goal_overdue: boolean;
  is_experienced: boolean;
}

export interface UserProfileUpdateRequest {
  experience_level?: ExperienceLevel;
  investment_goal?: InvestmentGoalType;
  personal_financial_goal?: PersonalFinancialGoalType;
  preferred_investment_types?: InvestmentType[];
  investment_goal_target_amount?: number;
  investment_goal_target_date?: string;
  personal_financial_goal_target_amount?: number;
  personal_financial_goal_description?: string;
}

export interface UserUpdateRequest {
  email?: string;
  password?: string;
  security_question?: SecurityQuestion;
  security_answer?: string;
  risk_tolerance?: RiskTolerance;
}

class UserService {
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

  // Get current user account details
  async getUserAccount(): Promise<UserAccountDetails> {
    const response = await fetch(`${API_BASE_URL}/users/account`, {
      method: "GET",
      headers: this.getAuthHeaders(),
    });

    if (!response.ok) {
      throw new Error(`Failed to fetch user account: ${response.status}`);
    }

    return response.json();
  }

  // Get user by username (if needed by backend)
  async getUserByUsername(username: string): Promise<UserAccountDetails> {
    const response = await fetch(
      `${API_BASE_URL}/users/profile?username=${encodeURIComponent(username)}`,
      {
        method: "GET",
        headers: this.getAuthHeaders(),
      }
    );

    if (!response.ok) {
      throw new Error(`Failed to fetch user by username: ${response.status}`);
    }

    return response.json();
  }

  async updateUser(
    userId: number,
    updateData: UserUpdateRequest
  ): Promise<UserAccountDetails> {
    const response = await fetch(`${API_BASE_URL}/users/${userId}`, {
      method: "PUT",
      headers: this.getAuthHeaders(),
      body: JSON.stringify(updateData),
    });

    if (!response.ok) {
      throw new Error(`Failed to update user: ${response.status}`);
    }

    return response.json();
  }

  // FIXED: Use the correct endpoint that matches your backend controller
  async getUserProfile(userId: number): Promise<UserProfile> {
    const response = await fetch(`${API_BASE_URL}/users/${userId}/profile`, {
      method: "GET",
      headers: this.getAuthHeaders(),
    });

    if (!response.ok) {
      if (response.status === 404) {
        throw new Error("Profile not found");
      }
      throw new Error(`Failed to fetch user profile: ${response.status}`);
    }

    return response.json();
  }

  // FIXED: Use the correct endpoint for creating profile
  async createUserProfile(
    userId: number,
    profileData: UserProfileUpdateRequest
  ): Promise<UserProfile> {
    const response = await fetch(`${API_BASE_URL}/users/${userId}/profile`, {
      method: "POST",
      headers: this.getAuthHeaders(),
      body: JSON.stringify(profileData), // Don't include user_id in body, it's in the URL path
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(
        `Failed to create user profile: ${response.status} - ${errorText}`
      );
    }

    return response.json();
  }

  // FIXED: Use the correct endpoint for updating profile
  async updateUserProfile(
    userId: number,
    profileData: UserProfileUpdateRequest
  ): Promise<UserProfile> {
    const response = await fetch(`${API_BASE_URL}/users/${userId}/profile`, {
      method: "PUT",
      headers: this.getAuthHeaders(),
      body: JSON.stringify(profileData),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(
        `Failed to update user profile: ${response.status} - ${errorText}`
      );
    }

    return response.json();
  }

  async incrementLearningProgress(userId: number): Promise<void> {
    const response = await fetch(
      `${API_BASE_URL}/users/${userId}/profile/learning/increment`,
      {
        method: "POST",
        headers: this.getAuthHeaders(),
      }
    );

    if (!response.ok) {
      throw new Error(
        `Failed to increment learning progress: ${response.status}`
      );
    }
  }

  async checkEmailAvailability(email: string): Promise<boolean> {
    const response = await fetch(
      `${API_BASE_URL}/users/check-email?email=${encodeURIComponent(email)}`,
      {
        method: "GET",
        headers: { "Content-Type": "application/json" },
      }
    );

    if (!response.ok) {
      throw new Error(`Failed to check email availability: ${response.status}`);
    }

    const data = await response.json();
    return data.available;
  }

  async checkUsernameAvailability(username: string): Promise<boolean> {
    const response = await fetch(
      `${API_BASE_URL}/users/check-username?username=${encodeURIComponent(username)}`,
      {
        method: "GET",
        headers: { "Content-Type": "application/json" },
      }
    );

    if (!response.ok) {
      throw new Error(
        `Failed to check username availability: ${response.status}`
      );
    }

    const data = await response.json();
    return data.available;
  }

  async verifyEmail(userId: number): Promise<void> {
    const response = await fetch(
      `${API_BASE_URL}/users/${userId}/verify-email`,
      {
        method: "POST",
        headers: this.getAuthHeaders(),
      }
    );

    if (!response.ok) {
      throw new Error(`Failed to verify email: ${response.status}`);
    }
  }
}

export const userService = new UserService();
