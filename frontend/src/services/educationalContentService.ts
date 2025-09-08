// services/educationalContentService.ts
import type {
  EducationalContent,
  CreateEducationalContentRequest,
  UpdateEducationalContentRequest,
  ContentFilters,
  ContentStatistics,
  UserProgress
} from "../types/educationalContent";
import { 
  ContentCategory, 
  ContentType, 
  DifficultyLevel 
} from "../types/educationalContent";
import { apiClient } from "./api";

export const educationalContentService = {
  // Get all active educational content
  async getAllContent(userId?: number): Promise<EducationalContent[]> {
    const params = userId ? { userId } : {};
    return apiClient.get<EducationalContent[]>("/api/v1/educational-content", { params });
  },

  // Get content by ID
  async getContentById(contentId: number, userId?: number): Promise<EducationalContent> {
    const url = userId 
      ? `/api/v1/educational-content/${contentId}/progress?userId=${userId}`
      : `/api/v1/educational-content/${contentId}`;
    return apiClient.get<EducationalContent>(url);
  },

  // Get featured content
  async getFeaturedContent(userId?: number): Promise<EducationalContent[]> {
    const params = userId ? { userId } : {};
    return apiClient.get<EducationalContent[]>("/api/v1/educational-content/featured", { params });
  },

  // Get content by category
  async getContentByCategory(category: ContentCategory, userId?: number): Promise<EducationalContent[]> {
    const params = userId ? { userId } : {};
    return apiClient.get<EducationalContent[]>(`/api/v1/educational-content/category/${category}`, { params });
  },

  // Get content by difficulty level
  async getContentByDifficulty(difficulty: DifficultyLevel): Promise<EducationalContent[]> {
    return apiClient.get<EducationalContent[]>(`/api/v1/educational-content/difficulty/${difficulty}`);
  },

  // Get content by type
  async getContentByType(type: ContentType): Promise<EducationalContent[]> {
    return apiClient.get<EducationalContent[]>(`/api/v1/educational-content/type/${type}`);
  },

  // Search content by title
  async searchByTitle(title: string): Promise<EducationalContent[]> {
    return apiClient.get<EducationalContent[]>("/api/v1/educational-content/search/title", {
      params: { title }
    });
  },

  // Search content by author
  async searchByAuthor(author: string): Promise<EducationalContent[]> {
    return apiClient.get<EducationalContent[]>("/api/v1/educational-content/search/author", {
      params: { author }
    });
  },

  // Search content by tag
  async searchByTag(tag: string): Promise<EducationalContent[]> {
    return apiClient.get<EducationalContent[]>("/api/v1/educational-content/search/tag", {
      params: { tag }
    });
  },

  // Get content by duration range
  async getContentByDurationRange(minDuration: number, maxDuration: number): Promise<EducationalContent[]> {
    return apiClient.get<EducationalContent[]>("/api/v1/educational-content/duration-range", {
      params: { minDuration, maxDuration }
    });
  },

  // Get content by minimum rating
  async getContentByMinRating(minRating: number): Promise<EducationalContent[]> {
    return apiClient.get<EducationalContent[]>("/api/v1/educational-content/rating", {
      params: { minRating }
    });
  },

  // Get most popular content
  async getMostPopularContent(limit: number = 10): Promise<EducationalContent[]> {
    return apiClient.get<EducationalContent[]>("/api/v1/educational-content/popular", {
      params: { limit }
    });
  },

  // Get recent content
  async getRecentContent(date: string): Promise<EducationalContent[]> {
    return apiClient.get<EducationalContent[]>("/api/v1/educational-content/created-after", {
      params: { date }
    });
  },

  // Increment view count
  async incrementViewCount(contentId: number): Promise<void> {
    return apiClient.post(`/api/v1/educational-content/${contentId}/view`);
  },

  // Get content statistics (Admin only)
  async getContentStatistics(): Promise<ContentStatistics> {
    return apiClient.get<ContentStatistics>("/api/v1/educational-content/statistics");
  },

  // Validate content exists
  async validateContentExists(contentId: number): Promise<{ exists: boolean }> {
    return apiClient.get<{ exists: boolean }>(`/api/v1/educational-content/${contentId}/validate`);
  },

  // Admin functions
  async createContent(request: CreateEducationalContentRequest): Promise<EducationalContent> {
    return apiClient.post<EducationalContent>("/api/v1/educational-content", request);
  },

  async updateContent(contentId: number, request: UpdateEducationalContentRequest): Promise<EducationalContent> {
    return apiClient.put<EducationalContent>(`/api/v1/educational-content/${contentId}`, request);
  },

  async deleteContent(contentId: number): Promise<void> {
    return apiClient.delete(`/api/v1/educational-content/${contentId}`);
  },

  async updateContentStatus(contentId: number, isActive: boolean): Promise<EducationalContent> {
    return apiClient.patch<EducationalContent>(`/api/v1/educational-content/${contentId}/status`, null, {
      params: { isActive }
    });
  },

  // Advanced filtering
  async getFilteredContent(filters: ContentFilters, userId?: number): Promise<EducationalContent[]> {
    // Build query based on filters
    let endpoint = "/api/v1/educational-content";
    const params: any = {};

    if (userId) {
      params.userId = userId;
    }

    // Handle different filter types
    if (filters.category) {
      endpoint = `/api/v1/educational-content/category/${filters.category}`;
    } else if (filters.difficulty_level) {
      endpoint = `/api/v1/educational-content/difficulty/${filters.difficulty_level}`;
    } else if (filters.content_type) {
      endpoint = `/api/v1/educational-content/type/${filters.content_type}`;
    } else if (filters.is_featured) {
      endpoint = "/api/v1/educational-content/featured";
    } else if (filters.search_term) {
      endpoint = "/api/v1/educational-content/search/title";
      params.title = filters.search_term;
    } else if (filters.author) {
      endpoint = "/api/v1/educational-content/search/author";
      params.author = filters.author;
    } else if (filters.min_rating) {
      endpoint = "/api/v1/educational-content/rating";
      params.minRating = filters.min_rating;
    } else if (filters.min_duration && filters.max_duration) {
      endpoint = "/api/v1/educational-content/duration-range";
      params.minDuration = filters.min_duration;
      params.maxDuration = filters.max_duration;
    }

    return apiClient.get<EducationalContent[]>(endpoint, { params });
  },

  // Get all categories for filtering
  getCategories(): ContentCategory[] {
    return Object.values(ContentCategory);
  },

  // Get all difficulty levels
  getDifficultyLevels(): DifficultyLevel[] {
    return Object.values(DifficultyLevel);
  },

  // Get all content types
  getContentTypes(): ContentType[] {
    return Object.values(ContentType);
  }
};