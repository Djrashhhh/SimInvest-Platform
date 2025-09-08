// hooks/useEducationalContent.ts
import { useState, useEffect, useCallback, useRef } from "react";
import {
  ContentCategory,
  ContentType, 
  DifficultyLevel
} from "../types/educationalContent";
import type {
  EducationalContent,
  ContentFilters,
  ContentStatistics
} from "../types/educationalContent";
import { educationalContentService } from "../services/educationalContentService";

export const useEducationalContent = (userId?: number) => {
  const [content, setContent] = useState<EducationalContent[]>([]);
  const [featuredContent, setFeaturedContent] = useState<EducationalContent[]>([]);
  const [popularContent, setPopularContent] = useState<EducationalContent[]>([]);
  const [recentContent, setRecentContent] = useState<EducationalContent[]>([]);
  const [statistics, setStatistics] = useState<ContentStatistics | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  // Refs to prevent duplicate API calls
  const hasInitializedRef = useRef(false);
  const lastFiltersRef = useRef<string>("");

  // Fetch all content
  const fetchAllContent = useCallback(async () => {
    try {
      setIsLoading(true);
      setError(null);
      
      const data = await educationalContentService.getAllContent(userId);
      setContent(data);
    } catch (error) {
      console.error("Error fetching content:", error);
      setError(error instanceof Error ? error.message : "Failed to fetch content");
    } finally {
      setIsLoading(false);
    }
  }, [userId]);

  // Fetch featured content
  const fetchFeaturedContent = useCallback(async () => {
    try {
      const data = await educationalContentService.getFeaturedContent(userId);
      setFeaturedContent(data);
    } catch (error) {
      console.error("Error fetching featured content:", error);
    }
  }, [userId]);

  // Fetch popular content
  const fetchPopularContent = useCallback(async () => {
    try {
      const data = await educationalContentService.getMostPopularContent(10);
      setPopularContent(data);
    } catch (error) {
      console.error("Error fetching popular content:", error);
    }
  }, []);

  // Fetch recent content
  const fetchRecentContent = useCallback(async () => {
    try {
      const oneWeekAgo = new Date();
      oneWeekAgo.setDate(oneWeekAgo.getDate() - 7);
      const data = await educationalContentService.getRecentContent(oneWeekAgo.toISOString());
      setRecentContent(data);
    } catch (error) {
      console.error("Error fetching recent content:", error);
    }
  }, []);

  // Fetch statistics (for admin users)
  const fetchStatistics = useCallback(async () => {
    try {
      const data = await educationalContentService.getContentStatistics();
      setStatistics(data);
    } catch (error) {
      console.error("Error fetching statistics:", error);
    }
  }, []);

  // Filter content
  const filterContent = useCallback(async (filters: ContentFilters) => {
    const filtersKey = JSON.stringify(filters);
    
    // Prevent duplicate calls with same filters
    if (lastFiltersRef.current === filtersKey) {
      return;
    }
    
    try {
      setIsLoading(true);
      setError(null);
      lastFiltersRef.current = filtersKey;
      
      const data = await educationalContentService.getFilteredContent(filters, userId);
      setContent(data);
    } catch (error) {
      console.error("Error filtering content:", error);
      setError(error instanceof Error ? error.message : "Failed to filter content");
    } finally {
      setIsLoading(false);
    }
  }, [userId]);

  // Search content
  const searchContent = useCallback(async (searchTerm: string, searchType: 'title' | 'author' | 'tag' = 'title') => {
    try {
      setIsLoading(true);
      setError(null);
      
      let data: EducationalContent[];
      
      switch (searchType) {
        case 'author':
          data = await educationalContentService.searchByAuthor(searchTerm);
          break;
        case 'tag':
          data = await educationalContentService.searchByTag(searchTerm);
          break;
        default:
          data = await educationalContentService.searchByTitle(searchTerm);
      }
      
      setContent(data);
    } catch (error) {
      console.error("Error searching content:", error);
      setError(error instanceof Error ? error.message : "Failed to search content");
    } finally {
      setIsLoading(false);
    }
  }, []);

  // Get content by ID with progress tracking
  const getContentById = useCallback(async (contentId: number) => {
    try {
      return await educationalContentService.getContentById(contentId, userId);
    } catch (error) {
      console.error("Error fetching content by ID:", error);
      throw error;
    }
  }, [userId]);

  // Increment view count
  const incrementViewCount = useCallback(async (contentId: number) => {
    try {
      await educationalContentService.incrementViewCount(contentId);
    } catch (error) {
      console.error("Error incrementing view count:", error);
    }
  }, []);

  // Refresh all data
  const refreshData = useCallback(async () => {
    await Promise.all([
      fetchAllContent(),
      fetchFeaturedContent(),
      fetchPopularContent(),
      fetchRecentContent()
    ]);
  }, [fetchAllContent, fetchFeaturedContent, fetchPopularContent, fetchRecentContent]);

  // Clear filters and reset to all content
  const clearFilters = useCallback(() => {
    lastFiltersRef.current = "";
    fetchAllContent();
  }, [fetchAllContent]);

  // Initial data loading
  useEffect(() => {
    if (!hasInitializedRef.current) {
      hasInitializedRef.current = true;
      refreshData();
    }
  }, [refreshData]);

  return {
    content,
    featuredContent,
    popularContent,
    recentContent,
    statistics,
    isLoading,
    error,
    filterContent,
    searchContent,
    getContentById,
    incrementViewCount,
    refreshData,
    clearFilters,
    fetchStatistics,
    setError // For clearing errors
  };
};

// Hook for content categories and filtering options
export const useContentCategories = () => {
  const categories = educationalContentService.getCategories();
  const difficultyLevels = educationalContentService.getDifficultyLevels();
  const contentTypes = educationalContentService.getContentTypes();

  const getCategoryContent = useCallback(async (category: ContentCategory, userId?: number) => {
    return educationalContentService.getContentByCategory(category, userId);
  }, []);

  const getDifficultyContent = useCallback(async (difficulty: DifficultyLevel) => {
    return educationalContentService.getContentByDifficulty(difficulty);
  }, []);

  const getTypeContent = useCallback(async (type: ContentType) => {
    return educationalContentService.getContentByType(type);
  }, []);

  return {
    categories,
    difficultyLevels,
    contentTypes,
    getCategoryContent,
    getDifficultyContent,
    getTypeContent
  };
};

// Hook for managing individual content interaction
export const useContentInteraction = (contentId: number, userId?: number) => {
  const [content, setContent] = useState<EducationalContent | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadContent = useCallback(async () => {
    try {
      setIsLoading(true);
      setError(null);
      
      const data = await educationalContentService.getContentById(contentId, userId);
      setContent(data);
      
      // Increment view count when content is loaded
      await educationalContentService.incrementViewCount(contentId);
    } catch (error) {
      console.error("Error loading content:", error);
      setError(error instanceof Error ? error.message : "Failed to load content");
    } finally {
      setIsLoading(false);
    }
  }, [contentId, userId]);

  useEffect(() => {
    if (contentId > 0) {
      loadContent();
    }
  }, [contentId, loadContent]);

  return {
    content,
    isLoading,
    error,
    refreshContent: loadContent
  };
};