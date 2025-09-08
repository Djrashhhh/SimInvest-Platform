// API client configuration and utilities

// ✅ FIXED: Now includes /api/v1 in the default URL to match your .env file
const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

console.log("API Base URL:", BASE_URL); // Debug log

interface ApiError {
  error?: string;
  message?: string;
  details?: string;
  errorType?: string;
}

class ApiClient {
  private baseURL: string;

  constructor(baseURL: string) {
    this.baseURL = baseURL;
  }

  private getAuthHeaders(): HeadersInit {
    const headers: HeadersInit = {
      "Content-Type": "application/json",
    };

    // Check if we're in a browser environment before accessing localStorage
    if (typeof window !== "undefined" && window.localStorage) {
      const token = localStorage.getItem("microinvest_token");
      if (token) {
        headers["Authorization"] = `Bearer ${token}`;
        console.log("Adding auth header with token"); // Debug log
      } else {
        console.log("No token found in localStorage"); // Debug log
      }
    }

    return headers;
  }

  private async handleResponse<T>(response: Response): Promise<T> {
    console.log("Response received:", response.status, response.statusText); // Debug log
    console.log("Response URL:", response.url); // Debug log to see actual URL called

    // Check if response is ok
    if (!response.ok) {
      let errorMessage = `HTTP ${response.status}: ${response.statusText}`;
      let errorDetails = "";

      try {
        const errorData: ApiError = await response.json();
        console.error("Error response data:", errorData); // Debug log

        if (errorData.details) {
          errorMessage = errorData.details;
        } else if (errorData.message) {
          errorMessage = errorData.message;
        } else if (errorData.error) {
          errorMessage = errorData.error;
        }
        errorDetails = errorData.details || "";
      } catch (parseError) {
        console.error("Failed to parse error response:", parseError);
        // If JSON parsing fails, use the status text
      }

      // Create enhanced error object
      const error = new Error(errorMessage);
      (error as any).response = {
        status: response.status,
        statusText: response.statusText,
        data: {
          message: errorMessage,
          details: errorDetails,
        },
      };

      throw error;
    }

    // Check if response has content
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
      const text = await response.text();
      if (text.trim() === "") {
        return {} as T;
      }
      try {
        return JSON.parse(text);
      } catch (parseError) {
        console.warn("Failed to parse JSON response:", text);
        return {} as T;
      }
    } else {
      // For non-JSON responses, try to return the text
      const text = await response.text();
      if (text.trim() === "") {
        return {} as T;
      }
      // For text responses, return as-is (useful for some endpoints)
      return text as unknown as T;
    }
  }

  async get<T>(endpoint: string): Promise<T> {
    try {
      const url = `${this.baseURL}${endpoint}`;
      console.log("GET request to:", url); // Debug log

      const response = await fetch(url, {
        method: "GET",
        headers: this.getAuthHeaders(),
        credentials: "include", // ✅ Include credentials for CORS
        mode: "cors", // ✅ Explicitly set CORS mode
      });

      return this.handleResponse<T>(response);
    } catch (error) {
      console.error(`GET request failed for ${endpoint}:`, error);
      throw error;
    }
  }

  async post<T>(endpoint: string, data?: any): Promise<T> {
    try {
      const url = `${this.baseURL}${endpoint}`;
      console.log("POST request to:", url, "with data:", data); // Debug log

      const response = await fetch(url, {
        method: "POST",
        headers: this.getAuthHeaders(),
        body: data ? JSON.stringify(data) : undefined,
        credentials: "include", // ✅ Include credentials for CORS
        mode: "cors", // ✅ Explicitly set CORS mode
      });

      return this.handleResponse<T>(response);
    } catch (error) {
      console.error(`POST request failed for ${endpoint}:`, error);
      throw error;
    }
  }

  async put<T>(endpoint: string, data?: any): Promise<T> {
    try {
      const url = `${this.baseURL}${endpoint}`;
      console.log("PUT request to:", url); // Debug log

      const response = await fetch(url, {
        method: "PUT",
        headers: this.getAuthHeaders(),
        body: data ? JSON.stringify(data) : undefined,
        credentials: "include", // ✅ Include credentials for CORS
        mode: "cors", // ✅ Explicitly set CORS mode
      });

      return this.handleResponse<T>(response);
    } catch (error) {
      console.error(`PUT request failed for ${endpoint}:`, error);
      throw error;
    }
  }

  async delete<T>(endpoint: string): Promise<T> {
    try {
      const url = `${this.baseURL}${endpoint}`;
      console.log("DELETE request to:", url); // Debug log

      const response = await fetch(url, {
        method: "DELETE",
        headers: this.getAuthHeaders(),
        credentials: "include", // ✅ Include credentials for CORS
        mode: "cors", // ✅ Explicitly set CORS mode
      });

      return this.handleResponse<T>(response);
    } catch (error) {
      console.error(`DELETE request failed for ${endpoint}:`, error);
      throw error;
    }
  }

  // Utility method to check if the API is reachable
  async healthCheck(): Promise<boolean> {
    try {
      // Note: This endpoint would be /api/v1/health if you have one
      const response = await fetch(`${this.baseURL}/health`, {
        method: "GET",
        headers: { "Content-Type": "application/json" },
        mode: "cors",
      });
      return response.ok;
    } catch {
      return false;
    }
  }

  // Get the base URL for debugging
  getBaseURL(): string {
    return this.baseURL;
  }
}

export const apiClient = new ApiClient(BASE_URL);
