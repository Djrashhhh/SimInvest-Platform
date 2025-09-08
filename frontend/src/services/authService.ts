//authentication and authorization api calls
import type {
  LoginRequest,
  RegisterRequest,
  LoginResponse,
  UserAccount,    
} from "../types/auth";
import { apiClient } from "./api";

export const authService = {
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>(
      "/api/v1/auth/login",
      credentials
    );

    // Log the entire response for debugging (remove in production)
    console.log("LOGIN RESPONSE STRUCTURE:", response);
  console.log("USER INFO:", response.user_info);
  console.log("USER ID:", response.user_info?.userId);

    // Store token in localStorage
    if (response.token) {
      localStorage.setItem("microinvest_token", response.token);
      
      // Ensure userId is properly mapped
    if (response.user_info) {
      console.log("Storing user data:", response.user_info);
      localStorage.setItem("microinvest_user", JSON.stringify(response.user_info));
    }
    }

    return response;
  },

  async register(userData: RegisterRequest): Promise<LoginResponse> {
    // Log the data being sent for debugging (remove in production)
    console.log("Registration data being sent:", userData);

    const response = await apiClient.post<LoginResponse>(
      "/api/v1/auth/register",
      userData
    );

    // Store token in localStorage
    if (response.token) {
      localStorage.setItem("microinvest_token", response.token);
      // Also store user data for persistence
      if (response.user_info) {
        localStorage.setItem("microinvest_user", JSON.stringify(response.user_info));
      }
    }

    return response;
  },

  async validateToken(): Promise<{ valid: boolean; username?: string }> {
    return apiClient.get("/api/v1/auth/validate");
  },

  // NEW: Get current user information
  async getCurrentUser(): Promise<UserAccount> {
    try {
      return await apiClient.get<UserAccount>("/api/v1/auth/me");
    } catch (error: any) {
      // If unauthorized, clean up local storage
      if (error.status === 401) {
        this.logout();
        throw new Error("Authentication expired");
      }
      throw error;
    }
  },

  async logout(): Promise<void> {
    try {
      await apiClient.post("/api/v1/auth/logout");
    } catch (error) {
      // Even if logout fails on server, we still want to clear local storage
      console.error("Server logout failed:", error);
    } finally {
      localStorage.removeItem("microinvest_token");
      localStorage.removeItem("microinvest_user"); // Clean up user data too
    }
  },

  // Add the missing saveToken method
  saveToken(token: string): void {
    localStorage.setItem("microinvest_token", token);
  },

  getToken(): string | null {
    return localStorage.getItem("microinvest_token");
  },

  isAuthenticated(): boolean {
    return !!this.getToken();
  },
};