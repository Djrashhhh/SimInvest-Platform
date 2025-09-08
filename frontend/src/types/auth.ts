// Authentication related types

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface RegisterRequest {
  first_name: string;
  last_name: string;
  email: string;
  username: string;
  password: string;
  risk_tolerance: "CONSERVATIVE" | "MODERATE" | "AGGRESSIVE";
  security_question:
    | "MOTHER_MAIDEN_NAME"
    | "FIRST_PET_NAME"
    | "HIGH_SCHOOL_NAME";
  security_answer: string;
  account_currency: "USD";
  initial_virtual_balance: number;
}

export interface UserAccount {
  user_id:number;       //changed from userId to user_id to match backend
  username: string;
  email: string;
  first_name?: string;
  last_name?: string;
  account_currency?: string;
  initial_virtual_balance?: number;
  current_virtual_balance?: number;      //changed to snake_case to match backend
  risk_tolerance?: string;
  account_status?: string;        //more fields to match backend UserAccount entity
  is_active?: boolean;
  email_verified?: boolean;
  created_at?: string;
  last_updated?: string;
  total_invested_amount?: number;
  total_returns?: number;
  net_worth?: number;
  return_on_investment?: number;
}

export interface LoginResponse {
  token: string;
  token_type: string; // matches @JsonProperty("token_type")
  expires_in: number; // matches @JsonProperty("expires_in")
  user_info: UserAccount; // matches @JsonProperty("user_info") - this is the primary field
  message: string;
}

// For backward compatibility with your AuthContext
export interface AuthUser extends UserAccount {
  // Add any additional fields your auth context expects
}
