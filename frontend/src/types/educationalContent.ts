// types/educationalContent.ts

// Enums - these need to be declared before interfaces that use them
export enum ContentCategory {
  INVESTING_BASICS = "INVESTING_BASICS",
  CAPITAL_MARKETS = "CAPITAL_MARKETS",
  INVESTMENT_STRATEGIES = "INVESTMENT_STRATEGIES",
  BEHAVIORAL_FINANCE = "BEHAVIORAL_FINANCE",
  RISK_MANAGEMENT = "RISK_MANAGEMENT",
  PORTFOLIO_MANAGEMENT = "PORTFOLIO_MANAGEMENT",
  FINANCIAL_ANALYSIS = "FINANCIAL_ANALYSIS",
  MARKET_TRENDS = "MARKET_TRENDS",
  CRYPTOCURRENCY = "CRYPTOCURRENCY",
  REAL_ESTATE = "REAL_ESTATE",
  RETIREMENT_PLANNING = "RETIREMENT_PLANNING",
  TAX_STRATEGIES = "TAX_STRATEGIES"
}

export enum ContentType {
  ARTICLE = "ARTICLE",
  VIDEO = "VIDEO",
  PODCAST = "PODCAST",
  QUIZ = "QUIZ",
  TUTORIAL = "TUTORIAL",
  INFOGRAPHIC = "INFOGRAPHIC",
  COURSE = "COURSE",
  BLOG_POST = "BLOG_POST"
}

export enum DifficultyLevel {
  BEGINNER = "BEGINNER",
  INTERMEDIATE = "INTERMEDIATE",
  ADVANCED = "ADVANCED",
  EXPERT = "EXPERT"
}

export enum CompletionStatus {
  NOT_STARTED = "NOT_STARTED",
  IN_PROGRESS = "IN_PROGRESS",
  COMPLETED = "COMPLETED",
  BOOKMARKED = "BOOKMARKED"
}
export interface EducationalContent {
  content_id: number;
  title: string;
  description: string;
  category: ContentCategory;
  difficulty_level: DifficultyLevel;
  content_type: ContentType;
  external_url: string;
  duration_minutes?: number;
  author: string;
  source: string;
  tags: string[];
  prerequisites: number[];
  is_featured: boolean;
  view_count: number;
  rating: number;
  user_progress?: UserProgress;
  created_at: string;
  updated_at: string;
}

export interface UserProgress {
  progress_id: number;
  user_id: number;
  content_id: number;
  completion_status: CompletionStatus;
  progress_percentage: number;
  time_spent_minutes: number;
  started_at: string;
  completed_at?: string;
  last_accessed: string;
  rating_given?: number;
  notes?: string;
}

export interface CreateEducationalContentRequest {
  title: string;
  description: string;
  category: string;
  difficulty_level: string;
  content_type: string;
  content_url: string;
  duration_minutes?: number;
  author?: string;
  source?: string;
  tags?: string[];
  prerequisites?: number[];
  is_featured?: boolean;
}

export interface UpdateEducationalContentRequest {
  title?: string;
  description?: string;
  content_url?: string;
  duration_minutes?: number;
  author?: string;
  source?: string;
  tags?: string[];
  prerequisites?: number[];
  is_featured?: boolean;
  is_active?: boolean;
}

export interface ContentFilters {
  category?: ContentCategory;
  difficulty_level?: DifficultyLevel;
  content_type?: ContentType;
  author?: string;
  tags?: string[];
  min_rating?: number;
  min_duration?: number;
  max_duration?: number;
  search_term?: string;
  is_featured?: boolean;
}

export interface ContentStatistics {
  totalActiveContent: number;
  averageRating: number;
  contentByCategory: Record<ContentCategory, number>;
}

export enum ContentCategory {
  INVESTING_BASICS = "INVESTING_BASICS",
  CAPITAL_MARKETS = "CAPITAL_MARKETS",
  INVESTMENT_STRATEGIES = "INVESTMENT_STRATEGIES",
  BEHAVIORAL_FINANCE = "BEHAVIORAL_FINANCE",
  RISK_MANAGEMENT = "RISK_MANAGEMENT",
  PORTFOLIO_MANAGEMENT = "PORTFOLIO_MANAGEMENT",
  FINANCIAL_ANALYSIS = "FINANCIAL_ANALYSIS",
  MARKET_TRENDS = "MARKET_TRENDS",
  CRYPTOCURRENCY = "CRYPTOCURRENCY",
  REAL_ESTATE = "REAL_ESTATE",
  RETIREMENT_PLANNING = "RETIREMENT_PLANNING",
  TAX_STRATEGIES = "TAX_STRATEGIES"
}

export enum ContentType {
  ARTICLE = "ARTICLE",
  VIDEO = "VIDEO",
  PODCAST = "PODCAST",
  QUIZ = "QUIZ",
  TUTORIAL = "TUTORIAL",
  INFOGRAPHIC = "INFOGRAPHIC",
  COURSE = "COURSE",
  BLOG_POST = "BLOG_POST"
}

export enum DifficultyLevel {
  BEGINNER = "BEGINNER",
  INTERMEDIATE = "INTERMEDIATE",
  ADVANCED = "ADVANCED",
  EXPERT = "EXPERT"
}

export enum CompletionStatus {
  NOT_STARTED = "NOT_STARTED",
  IN_PROGRESS = "IN_PROGRESS",
  COMPLETED = "COMPLETED",
  BOOKMARKED = "BOOKMARKED"
}

// Helper functions for display
export const getCategoryDisplayName = (category: ContentCategory): string => {
  return category.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
};

export const getDifficultyColor = (difficulty: DifficultyLevel): string => {
  switch (difficulty) {
    case DifficultyLevel.BEGINNER:
      return 'text-green-400 bg-green-900/20 border-green-500/30';
    case DifficultyLevel.INTERMEDIATE:
      return 'text-blue-400 bg-blue-900/20 border-blue-500/30';
    case DifficultyLevel.ADVANCED:
      return 'text-orange-400 bg-orange-900/20 border-orange-500/30';
    case DifficultyLevel.EXPERT:
      return 'text-red-400 bg-red-900/20 border-red-500/30';
    default:
      return 'text-gray-400 bg-gray-900/20 border-gray-500/30';
  }
};

export const getContentTypeIcon = (type: ContentType): string => {
  switch (type) {
    case ContentType.VIDEO:
      return '🎥';
    case ContentType.ARTICLE:
      return '📄';
    case ContentType.PODCAST:
      return '🎧';
    case ContentType.QUIZ:
      return '🧠';
    case ContentType.TUTORIAL:
      return '🛠️';
    case ContentType.INFOGRAPHIC:
      return '📊';
    case ContentType.COURSE:
      return '🎓';
    case ContentType.BLOG_POST:
      return '✍️';
    default:
      return '📖';
  }
};