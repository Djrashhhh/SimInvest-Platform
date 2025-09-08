import React, { createContext, useContext, useReducer, useEffect } from "react";
import type { UserAccount, RegisterRequest } from "../types/auth";
import { authService } from "../services/authService";

interface AuthState {
  user: UserAccount | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

type AuthAction =
  | { type: "LOGIN_START" }
  | { type: "LOGIN_SUCCESS"; payload: UserAccount }
  | { type: "LOGIN_FAILURE"; payload: string }
  | { type: "LOGOUT" }
  | { type: "CLEAR_ERROR" };

const initialState: AuthState = {
  user: null,
  isAuthenticated: false,
  isLoading: true, // Start with loading true
  error: null,
};

function authReducer(state: AuthState, action: AuthAction): AuthState {
  switch (action.type) {
    case "LOGIN_START":
      return { ...state, isLoading: true, error: null };
    case "LOGIN_SUCCESS":
      return {
        ...state,
        user: action.payload,
        isAuthenticated: true,
        isLoading: false,
        error: null,
      };
    case "LOGIN_FAILURE":
      return {
        ...state,
        user: null,
        isAuthenticated: false,
        isLoading: false,
        error: action.payload,
      };
    case "LOGOUT":
      return {
        ...state,
        user: null,
        isAuthenticated: false,
        isLoading: false,
        error: null,
      };
    case "CLEAR_ERROR":
      return { ...state, error: null };
    default:
      return state;
  }
}

interface AuthContextType extends AuthState {
  login: (credentials: {
    usernameOrEmail: string;
    password: string;
  }) => Promise<void>;
  register: (userData: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  clearError: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, dispatch] = useReducer(authReducer, initialState);

  // Check for existing token on mount
  useEffect(() => {
    const checkAuth = async () => {
      const token = authService.getToken();
      if (token) {
        try {
          const validation = await authService.validateToken();
          if (validation.valid && validation.username) {
            // You need to get the full user data including userId
            // Option 1: Add a getCurrentUser endpoint to your backend
            // Option 2: Store user data in localStorage along with token
            // Option 3: Modify validateToken to return full user info

            // For now, let's try to get user data from localStorage
            const storedUser = localStorage.getItem("microinvest_user");
            if (storedUser) {
              const userData = JSON.parse(storedUser);
              if (userData.userId) {
                dispatch({
                  type: "LOGIN_SUCCESS",
                  payload: userData,
                });
                return;
              }
            }

            // If no stored user data, you might need to make another API call
            // to get the complete user information
            console.warn("Token is valid but no complete user data found");
            authService.logout(); // Clear invalid/incomplete session
            dispatch({ type: "LOGOUT" });
          } else {
            authService.logout(); // Clear invalid token
            dispatch({ type: "LOGOUT" });
          }
        } catch (error) {
          console.error("Token validation error:", error);
          authService.logout(); // Clear invalid token
          dispatch({ type: "LOGOUT" });
        }
      } else {
        // No token found, user is not authenticated
        dispatch({ type: "LOGOUT" });
      }
    };

    checkAuth();
  }, []);

  const login = async (credentials: {
    usernameOrEmail: string;
    password: string;
  }) => {
    dispatch({ type: "LOGIN_START" });
    try {
      console.log("Attempting login with credentials:", {
        usernameOrEmail: credentials.usernameOrEmail,
      });

      const response = await authService.login(credentials);

      console.log("Login response received:", response);

      // Check if we have the expected response structure
      if (!response || !response.token) {
        throw new Error("Invalid login response - no token received");
      }

      if (!response.user_info) {
        throw new Error("Invalid login response - no user info received");
      }

      // Store user data in localStorage for persistence
      localStorage.setItem(
        "microinvest_user",
        JSON.stringify(response.user_info)
      );

      // The token is already saved by authService.login()
      console.log("Login successful, dispatching success action");

      dispatch({ type: "LOGIN_SUCCESS", payload: response.user_info });
    } catch (error) {
      console.error("Login error:", error);

      // Clear any stored data on login failure
      localStorage.removeItem("microinvest_token");
      localStorage.removeItem("microinvest_user");

      let errorMessage = "Login failed";
      if (error instanceof Error) {
        errorMessage = error.message;
      } else if (typeof error === "string") {
        errorMessage = error;
      } else if (error && typeof error === "object" && "message" in error) {
        errorMessage = (error as any).message;
      }

      dispatch({
        type: "LOGIN_FAILURE",
        payload: errorMessage,
      });
      throw error;
    }
  };

  const register = async (userData: RegisterRequest) => {
    dispatch({ type: "LOGIN_START" });
    try {
      console.log("Attempting registration for:", userData.username);

      const response = await authService.register(userData);

      console.log("Registration response received:", response);

      // Extract user data from response - backend always uses user_info
      if (!response.user_info) {
        throw new Error("No user data received from server");
      }

      if (!response.token) {
        throw new Error("No authentication token received from server");
      }

      // Store user data in localStorage for persistence
      localStorage.setItem(
        "microinvest_user",
        JSON.stringify(response.user_info)
      );

      // The token is already saved by authService.register()
      console.log("Registration successful, dispatching success action");

      dispatch({ type: "LOGIN_SUCCESS", payload: response.user_info });
    } catch (error) {
      console.error("Registration error:", error);

      // Clear any stored data on registration failure
      localStorage.removeItem("microinvest_token");
      localStorage.removeItem("microinvest_user");

      let errorMessage = "Registration failed";
      if (error instanceof Error) {
        errorMessage = error.message;
      } else if (typeof error === "string") {
        errorMessage = error;
      } else if (error && typeof error === "object" && "message" in error) {
        errorMessage = (error as any).message;
      }

      dispatch({
        type: "LOGIN_FAILURE",
        payload: errorMessage,
      });
      throw error;
    }
  };

  const logout = async () => {
    try {
      await authService.logout();
    } catch (error) {
      console.error("Logout error:", error);
    } finally {
      // Clear stored user data
      localStorage.removeItem("microinvest_user");
      dispatch({ type: "LOGOUT" });
    }
  };

  const clearError = () => {
    dispatch({ type: "CLEAR_ERROR" });
  };

  const value: AuthContextType = {
    ...state,
    login,
    register,
    logout,
    clearError,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
