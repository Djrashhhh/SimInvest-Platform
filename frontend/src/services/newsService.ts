// Enhanced newsService.ts with debugging - replace your existing newsService.ts with this version

export interface NewsArticle {
  title: string;
  description: string;
  url: string;
  imageUrl?: string;
  source: string;
  author?: string;
  publishedAt: string;
  content?: string;
  category: string;
}

export interface NewsCategory {
  id: string;
  name: string;
  description?: string;
}

class NewsService {
  private readonly API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

  private getAuthHeaders(): HeadersInit {
    const token = localStorage.getItem("microinvest_token");
    console.log("🔑 Token exists:", !!token);
    console.log("🔑 Token preview:", token?.substring(0, 50) + "...");

    return {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    };
  }

  /**
   * Get market news by category
   */
  async getMarketNews(
    category: string = "general",
    limit: number = 20
  ): Promise<NewsArticle[]> {
    const url = `${this.API_BASE_URL}/api/v1/news?category=${encodeURIComponent(category)}&limit=${limit}`;
    console.log("📰 Fetching market news...");
    console.log("📰 URL:", url);
    console.log("📰 Category:", category);
    console.log("📰 Limit:", limit);

    try {
      const headers = this.getAuthHeaders();
      console.log("📰 Headers:", headers);

      const response = await fetch(url, {
        method: "GET",
        headers: headers,
      });

      console.log("📰 Response status:", response.status);
      console.log(
        "📰 Response headers:",
        Object.fromEntries(response.headers.entries())
      );

      if (!response.ok) {
        const errorText = await response.text();
        console.error("📰 Error response body:", errorText);
        throw new Error(
          `Failed to fetch news: ${response.status} ${response.statusText} - ${errorText}`
        );
      }

      const data = await response.json();
      console.log("📰 Success! Received", data.length, "articles");
      console.log("📰 Sample article:", data[0]);

      return data;
    } catch (error) {
      console.error("📰 Error fetching market news:", error);
      throw error;
    }
  }

  /**
   * Get top business headlines
   */
  async getTopBusinessNews(limit: number = 20): Promise<NewsArticle[]> {
    const url = `${this.API_BASE_URL}/api/v1/news/headlines?limit=${limit}`;
    console.log("📈 Fetching business headlines...");
    console.log("📈 URL:", url);

    try {
      const response = await fetch(url, {
        method: "GET",
        headers: this.getAuthHeaders(),
      });

      console.log("📈 Response status:", response.status);

      if (!response.ok) {
        const errorText = await response.text();
        console.error("📈 Error response body:", errorText);
        throw new Error(
          `Failed to fetch headlines: ${response.status} ${response.statusText} - ${errorText}`
        );
      }

      const data = await response.json();
      console.log("📈 Success! Received", data.length, "headlines");

      return data;
    } catch (error) {
      console.error("📈 Error fetching business news:", error);
      throw error;
    }
  }

  /**
   * Search news articles
   */
  async searchNews(
    searchTerm: string,
    limit: number = 20
  ): Promise<NewsArticle[]> {
    const url = `${this.API_BASE_URL}/api/v1/news/search?q=${encodeURIComponent(searchTerm)}&limit=${limit}`;
    console.log("🔍 Searching news...");
    console.log("🔍 URL:", url);

    try {
      const response = await fetch(url, {
        method: "GET",
        headers: this.getAuthHeaders(),
      });

      console.log("🔍 Response status:", response.status);

      if (!response.ok) {
        const errorText = await response.text();
        console.error("🔍 Error response body:", errorText);
        throw new Error(
          `Failed to search news: ${response.status} ${response.statusText} - ${errorText}`
        );
      }

      const data = await response.json();
      console.log("🔍 Success! Found", data.length, "articles");

      return data;
    } catch (error) {
      console.error("🔍 Error searching news:", error);
      throw error;
    }
  }

  /**
   * Get available news categories
   */
  async getNewsCategories(): Promise<NewsCategory[]> {
    const url = `${this.API_BASE_URL}/api/v1/news/categories`;
    console.log("📂 Fetching categories...");
    console.log("📂 URL:", url);

    try {
      const response = await fetch(url, {
        method: "GET",
        headers: this.getAuthHeaders(),
      });

      console.log("📂 Response status:", response.status);

      if (!response.ok) {
        console.warn("📂 Categories API failed, using defaults");
        return this.getDefaultCategories();
      }

      const categories = await response.json();
      const formattedCategories = categories.map((cat: string) => ({
        id: cat,
        name: this.formatCategoryName(cat),
      }));

      console.log("📂 Success! Categories:", formattedCategories);
      return formattedCategories;
    } catch (error) {
      console.error("📂 Error fetching news categories:", error);
      console.log("📂 Using default categories");
      return this.getDefaultCategories();
    }
  }

  /**
   * Get news preview for dashboard
   */
  async getNewsPreview(limit: number = 3): Promise<NewsArticle[]> {
    console.log("👀 Fetching news preview...");
    try {
      const preview = await this.getTopBusinessNews(limit);
      console.log("👀 Preview success:", preview.length, "articles");
      return preview;
    } catch (error) {
      console.error("👀 Error fetching news preview:", error);
      return [];
    }
  }

  /**
   * Test connection to news API
   */
  async testConnection(): Promise<{ success: boolean; message: string }> {
    console.log("🧪 Testing news API connection...");

    try {
      const response = await fetch(`${this.API_BASE_URL}/api/v1/news/categories`, {
        method: "GET",
        headers: this.getAuthHeaders(),
      });

      if (response.ok) {
        console.log("🧪 Connection test SUCCESS");
        return { success: true, message: "News API connection successful" };
      } else {
        console.log("🧪 Connection test FAILED:", response.status);
        return { success: false, message: `API returned ${response.status}` };
      }
    } catch (error) {
      console.error("🧪 Connection test ERROR:", error);
      return { success: false, message: `Connection failed: ${error}` };
    }
  }

  private formatCategoryName(category: string): string {
    return category
      .split("-")
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(" ");
  }

  private getDefaultCategories(): NewsCategory[] {
    return [
      { id: "general", name: "All News" },
      { id: "technology", name: "Technology" },
      { id: "finance", name: "Finance" },
      { id: "crypto", name: "Crypto" },
      { id: "energy", name: "Energy" },
      { id: "healthcare", name: "Healthcare" },
      { id: "automotive", name: "Automotive" },
      { id: "real-estate", name: "Real Estate" },
    ];
  }

  /**
   * Utility method to format dates
   */
  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  /**
   * Utility method to get relative time
   */
  getRelativeTime(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffInHours = Math.floor(
      (now.getTime() - date.getTime()) / (1000 * 60 * 60)
    );

    if (diffInHours < 1) {
      return "Just now";
    } else if (diffInHours < 24) {
      return `${diffInHours}h ago`;
    } else {
      const diffInDays = Math.floor(diffInHours / 24);
      return `${diffInDays}d ago`;
    }
  }
}

export const newsService = new NewsService();
