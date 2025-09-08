import React, { useState, useEffect, useMemo } from "react";
import { Link } from "react-router-dom";
import { 
  Search, 
  Filter, 
  BookOpen, 
  Clock, 
  Star, 
  Eye, 
  TrendingUp, 
  User, 
  Tag,
  Play,
  FileText,
  Headphones,
  Brain,
  Monitor,
  BarChart3,
  GraduationCap,
  Edit3,
  X,
  ChevronDown,
  ExternalLink,
  Award,
  Calendar
} from "lucide-react";
import { useAuth } from "../../contexts/AuthContext";
import { useEducationalContent, useContentCategories } from "../../hooks/useEducationalContent";
import type { 
  ContentCategory, 
  ContentType, 
  DifficultyLevel, 
  ContentFilters,
  EducationalContent 
} from "../../types/educationalContent";
import { 
  getCategoryDisplayName, 
  getDifficultyColor, 
  getContentTypeIcon 
} from "../../types/educationalContent";

const EducationalContentPage: React.FC = () => {
  const { user } = useAuth();
  const {
    content,
    featuredContent,
    popularContent,
    recentContent,
    isLoading,
    error,
    filterContent,
    searchContent,
    incrementViewCount,
    clearFilters
  } = useEducationalContent(user?.user_id);

  const { categories, difficultyLevels, contentTypes } = useContentCategories();

  // State for filters and search
  const [searchTerm, setSearchTerm] = useState("");
  const [searchType, setSearchType] = useState<'title' | 'author' | 'tag'>('title');
  const [showFilters, setShowFilters] = useState(false);
  const [activeFilters, setActiveFilters] = useState<ContentFilters>({});
  const [currentView, setCurrentView] = useState<'all' | 'featured' | 'popular' | 'recent'>('all');
  const [selectedContent, setSelectedContent] = useState<EducationalContent | null>(null);

  // Get current content based on view
  const currentContent = useMemo(() => {
    switch (currentView) {
      case 'featured':
        return featuredContent;
      case 'popular':
        return popularContent;
      case 'recent':
        return recentContent;
      default:
        return content;
    }
  }, [currentView, content, featuredContent, popularContent, recentContent]);

  // Handle search
  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (searchTerm.trim()) {
      await searchContent(searchTerm.trim(), searchType);
      setCurrentView('all');
    }
  };

  // Handle filter application
  const handleApplyFilters = async (filters: ContentFilters) => {
    setActiveFilters(filters);
    await filterContent(filters);
    setCurrentView('all');
    setShowFilters(false);
  };

  // Clear all filters
  const handleClearFilters = () => {
    setActiveFilters({});
    setSearchTerm("");
    clearFilters();
    setCurrentView('all');
  };

  // Handle content click
  const handleContentClick = async (contentItem: EducationalContent) => {
    setSelectedContent(contentItem);
    await incrementViewCount(contentItem.content_id);
  };

  // Get content type icon component
  const getContentTypeIconComponent = (type: ContentType) => {
    switch (type) {
      case 'VIDEO':
        return <Play className="h-4 w-4" />;
      case 'ARTICLE':
        return <FileText className="h-4 w-4" />;
      case 'PODCAST':
        return <Headphones className="h-4 w-4" />;
      case 'QUIZ':
        return <Brain className="h-4 w-4" />;
      case 'TUTORIAL':
        return <Monitor className="h-4 w-4" />;
      case 'INFOGRAPHIC':
        return <BarChart3 className="h-4 w-4" />;
      case 'COURSE':
        return <GraduationCap className="h-4 w-4" />;
      case 'BLOG_POST':
        return <Edit3 className="h-4 w-4" />;
      default:
        return <BookOpen className="h-4 w-4" />;
    }
  };

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      {/* Header */}
      <div className="bg-gray-800 border-b border-gray-700">
        <div className="max-w-7xl mx-auto px-4 py-6">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-3xl font-bold text-white mb-2">Learning Center</h1>
              <p className="text-gray-400">Expand your investment knowledge with curated educational content</p>
            </div>
            <Link
              to="/dashboard"
              className="flex items-center space-x-2 px-4 py-2 bg-gray-700 hover:bg-gray-600 rounded-lg transition-colors"
            >
              <span>Back to Dashboard</span>
            </Link>
          </div>

          {/* Search and Filters */}
          <div className="flex flex-col lg:flex-row gap-4">
            {/* Search Bar */}
            <form onSubmit={handleSearch} className="flex-1">
              <div className="flex gap-2">
                <div className="relative flex-1">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                  <input
                    type="text"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    placeholder={`Search by ${searchType}...`}
                    className="w-full pl-10 pr-4 py-3 bg-gray-700 border border-gray-600 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                
                {/* Search Type Selector */}
                <select
                  value={searchType}
                  onChange={(e) => setSearchType(e.target.value as 'title' | 'author' | 'tag')}
                  className="px-4 py-3 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="title">Title</option>
                  <option value="author">Author</option>
                  <option value="tag">Tag</option>
                </select>
                
                <button
                  type="submit"
                  className="px-6 py-3 bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors"
                >
                  Search
                </button>
              </div>
            </form>

            {/* Filter Toggle */}
            <button
              onClick={() => setShowFilters(!showFilters)}
              className="flex items-center space-x-2 px-4 py-3 bg-gray-700 hover:bg-gray-600 rounded-lg transition-colors"
            >
              <Filter className="h-5 w-5" />
              <span>Filters</span>
              <ChevronDown className={`h-4 w-4 transition-transform ${showFilters ? 'rotate-180' : ''}`} />
            </button>
          </div>

          {/* Active Filters Display */}
          {(Object.keys(activeFilters).length > 0 || searchTerm) && (
            <div className="mt-4 flex flex-wrap gap-2 items-center">
              <span className="text-sm text-gray-400">Active filters:</span>
              {searchTerm && (
                <span className="px-3 py-1 bg-blue-900/50 text-blue-300 rounded-full text-sm flex items-center gap-2">
                  Search: "{searchTerm}"
                  <button onClick={() => setSearchTerm("")} className="hover:text-blue-200">
                    <X className="h-3 w-3" />
                  </button>
                </span>
              )}
              {activeFilters.category && (
                <span className="px-3 py-1 bg-purple-900/50 text-purple-300 rounded-full text-sm">
                  {getCategoryDisplayName(activeFilters.category)}
                </span>
              )}
              {activeFilters.difficulty_level && (
                <span className="px-3 py-1 bg-orange-900/50 text-orange-300 rounded-full text-sm">
                  {activeFilters.difficulty_level}
                </span>
              )}
              {activeFilters.content_type && (
                <span className="px-3 py-1 bg-green-900/50 text-green-300 rounded-full text-sm">
                  {activeFilters.content_type}
                </span>
              )}
              <button
                onClick={handleClearFilters}
                className="px-3 py-1 bg-red-900/50 text-red-300 hover:bg-red-900/70 rounded-full text-sm transition-colors"
              >
                Clear All
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Filter Panel */}
      {showFilters && (
        <FilterPanel
          categories={categories}
          difficultyLevels={difficultyLevels}
          contentTypes={contentTypes}
          activeFilters={activeFilters}
          onApplyFilters={handleApplyFilters}
          onClose={() => setShowFilters(false)}
        />
      )}

      <div className="max-w-7xl mx-auto px-4 py-8">
        {/* View Tabs */}
        <div className="flex flex-wrap gap-2 mb-8">
          {[
            { key: 'all', label: 'All Content', count: content.length },
            { key: 'featured', label: 'Featured', count: featuredContent.length },
            { key: 'popular', label: 'Popular', count: popularContent.length },
            { key: 'recent', label: 'Recent', count: recentContent.length }
          ].map(({ key, label, count }) => (
            <button
              key={key}
              onClick={() => setCurrentView(key as any)}
              className={`px-4 py-2 rounded-lg transition-colors flex items-center gap-2 ${
                currentView === key
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
              }`}
            >
              <span>{label}</span>
              <span className="bg-gray-800 px-2 py-0.5 rounded-full text-xs">{count}</span>
            </button>
          ))}
        </div>

        {/* Loading State */}
        {isLoading && (
          <div className="flex justify-center items-center h-64">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-400"></div>
          </div>
        )}

        {/* Error State */}
        {error && (
          <div className="bg-red-900/20 border border-red-500/50 rounded-lg p-6 text-center">
            <p className="text-red-400 mb-4">{error}</p>
            <button
              onClick={() => window.location.reload()}
              className="px-4 py-2 bg-red-600 hover:bg-red-700 rounded-lg transition-colors"
            >
              Retry
            </button>
          </div>
        )}

        {/* Content Grid */}
        {!isLoading && !error && (
          <>
            {currentContent.length === 0 ? (
              <div className="text-center py-12">
                <BookOpen className="h-16 w-16 text-gray-400 mx-auto mb-4" />
                <h3 className="text-xl font-semibold text-gray-300 mb-2">No content found</h3>
                <p className="text-gray-400 mb-4">
                  {Object.keys(activeFilters).length > 0 || searchTerm
                    ? "Try adjusting your filters or search terms"
                    : "No educational content is available at the moment"}
                </p>
                {(Object.keys(activeFilters).length > 0 || searchTerm) && (
                  <button
                    onClick={handleClearFilters}
                    className="px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors"
                  >
                    Clear Filters
                  </button>
                )}
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {currentContent.map((item) => (
                  <ContentCard
                    key={item.content_id}
                    content={item}
                    onClick={() => handleContentClick(item)}
                    getContentTypeIconComponent={getContentTypeIconComponent}
                  />
                ))}
              </div>
            )}
          </>
        )}
      </div>

      {/* Content Detail Modal */}
      {selectedContent && (
        <ContentDetailModal
          content={selectedContent}
          onClose={() => setSelectedContent(null)}
          getContentTypeIconComponent={getContentTypeIconComponent}
        />
      )}
    </div>
  );
};

// Content Card Component
interface ContentCardProps {
  content: EducationalContent;
  onClick: () => void;
  getContentTypeIconComponent: (type: ContentType) => React.ReactNode;
}

const ContentCard: React.FC<ContentCardProps> = ({ content, onClick, getContentTypeIconComponent }) => {
  return (
    <div
      onClick={onClick}
      className="bg-gray-800 rounded-xl p-6 shadow-lg hover:shadow-xl transition-all duration-200 hover:scale-105 cursor-pointer border border-gray-700 hover:border-gray-600"
    >
      {/* Header */}
      <div className="flex items-start justify-between mb-4">
        <div className="flex items-center space-x-2">
          {getContentTypeIconComponent(content.content_type as ContentType)}
          <span className="text-sm text-gray-400 capitalize">
            {content.content_type.toLowerCase().replace('_', ' ')}
          </span>
        </div>
        {content.is_featured && (
          <div className="bg-yellow-900/30 text-yellow-400 px-2 py-1 rounded-full text-xs flex items-center gap-1">
            <Award className="h-3 w-3" />
            Featured
          </div>
        )}
      </div>

      {/* Title and Description */}
      <h3 className="text-lg font-semibold text-white mb-2 line-clamp-2">{content.title}</h3>
      <p className="text-gray-400 text-sm mb-4 line-clamp-3">{content.description}</p>

      {/* Metadata */}
      <div className="space-y-2 mb-4">
        <div className="flex items-center justify-between text-sm">
          <span className="text-gray-400">Author:</span>
          <span className="text-white">{content.author}</span>
        </div>
        <div className="flex items-center justify-between text-sm">
          <span className="text-gray-400">Category:</span>
          <span className="text-blue-400">{getCategoryDisplayName(content.category as ContentCategory)}</span>
        </div>
        <div className="flex items-center justify-between text-sm">
          <span className="text-gray-400">Duration:</span>
          <span className="text-white flex items-center gap-1">
            <Clock className="h-3 w-3" />
            {content.duration_minutes ? `${content.duration_minutes} min` : 'N/A'}
          </span>
        </div>
      </div>

      {/* Tags */}
      {content.tags && content.tags.length > 0 && (
        <div className="flex flex-wrap gap-1 mb-4">
          {content.tags.slice(0, 3).map((tag, index) => (
            <span
              key={index}
              className="px-2 py-1 bg-gray-700 text-gray-300 rounded-full text-xs"
            >
              #{tag}
            </span>
          ))}
          {content.tags.length > 3 && (
            <span className="px-2 py-1 bg-gray-700 text-gray-400 rounded-full text-xs">
              +{content.tags.length - 3} more
            </span>
          )}
        </div>
      )}

      {/* Footer */}
      <div className="flex items-center justify-between pt-4 border-t border-gray-700">
        {/* Difficulty Badge */}
        <div className={`px-3 py-1 rounded-full text-xs border ${getDifficultyColor(content.difficulty_level as DifficultyLevel)}`}>
          {content.difficulty_level}
        </div>

        {/* Stats */}
        <div className="flex items-center space-x-4 text-sm text-gray-400">
          <div className="flex items-center gap-1">
            <Eye className="h-3 w-3" />
            <span>{content.view_count}</span>
          </div>
          <div className="flex items-center gap-1">
            <Star className="h-3 w-3" />
            <span>{content.rating.toFixed(1)}</span>
          </div>
        </div>
      </div>

      {/* Progress Bar (if user has progress) */}
      {content.user_progress && content.user_progress.progress_percentage > 0 && (
        <div className="mt-4">
          <div className="flex items-center justify-between text-xs text-gray-400 mb-1">
            <span>Progress</span>
            <span>{content.user_progress.progress_percentage}%</span>
          </div>
          <div className="w-full bg-gray-700 rounded-full h-2">
            <div
              className="bg-blue-400 h-2 rounded-full transition-all"
              style={{ width: `${content.user_progress.progress_percentage}%` }}
            ></div>
          </div>
        </div>
      )}
    </div>
  );
};

// Filter Panel Component
interface FilterPanelProps {
  categories: ContentCategory[];
  difficultyLevels: DifficultyLevel[];
  contentTypes: ContentType[];
  activeFilters: ContentFilters;
  onApplyFilters: (filters: ContentFilters) => void;
  onClose: () => void;
}

const FilterPanel: React.FC<FilterPanelProps> = ({
  categories,
  difficultyLevels,
  contentTypes,
  activeFilters,
  onApplyFilters,
  onClose
}) => {
  const [filters, setFilters] = useState<ContentFilters>(activeFilters);

  const handleApply = () => {
    onApplyFilters(filters);
  };

  const handleReset = () => {
    setFilters({});
  };

  return (
    <div className="bg-gray-800 border-b border-gray-700">
      <div className="max-w-7xl mx-auto px-4 py-6">
        <div className="flex items-center justify-between mb-6">
          <h3 className="text-lg font-semibold text-white">Filter Content</h3>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-white transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {/* Category Filter */}
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">Category</label>
            <select
              value={filters.category || ''}
              onChange={(e) => setFilters({ ...filters, category: e.target.value as ContentCategory || undefined })}
              className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">All Categories</option>
              {categories.map((category) => (
                <option key={category} value={category}>
                  {getCategoryDisplayName(category)}
                </option>
              ))}
            </select>
          </div>

          {/* Difficulty Filter */}
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">Difficulty</label>
            <select
              value={filters.difficulty_level || ''}
              onChange={(e) => setFilters({ ...filters, difficulty_level: e.target.value as DifficultyLevel || undefined })}
              className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">All Levels</option>
              {difficultyLevels.map((level) => (
                <option key={level} value={level}>
                  {level}
                </option>
              ))}
            </select>
          </div>

          {/* Content Type Filter */}
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">Content Type</label>
            <select
              value={filters.content_type || ''}
              onChange={(e) => setFilters({ ...filters, content_type: e.target.value as ContentType || undefined })}
              className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">All Types</option>
              {contentTypes.map((type) => (
                <option key={type} value={type}>
                  {type.replace('_', ' ')}
                </option>
              ))}
            </select>
          </div>

          {/* Rating Filter */}
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">Minimum Rating</label>
            <select
              value={filters.min_rating || ''}
              onChange={(e) => setFilters({ ...filters, min_rating: e.target.value ? parseFloat(e.target.value) : undefined })}
              className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">Any Rating</option>
              <option value="4">4+ Stars</option>
              <option value="3">3+ Stars</option>
              <option value="2">2+ Stars</option>
              <option value="1">1+ Stars</option>
            </select>
          </div>
        </div>

        {/* Duration Range */}
        <div className="mt-6 grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">Min Duration (minutes)</label>
            <input
              type="number"
              value={filters.min_duration || ''}
              onChange={(e) => setFilters({ ...filters, min_duration: e.target.value ? parseInt(e.target.value) : undefined })}
              className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="0"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">Max Duration (minutes)</label>
            <input
              type="number"
              value={filters.max_duration || ''}
              onChange={(e) => setFilters({ ...filters, max_duration: e.target.value ? parseInt(e.target.value) : undefined })}
              className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="999"
            />
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex gap-4 mt-6">
          <button
            onClick={handleApply}
            className="px-6 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors"
          >
            Apply Filters
          </button>
          <button
            onClick={handleReset}
            className="px-6 py-2 bg-gray-600 hover:bg-gray-700 rounded-lg transition-colors"
          >
            Reset
          </button>
        </div>
      </div>
    </div>
  );
};

// Content Detail Modal Component
interface ContentDetailModalProps {
  content: EducationalContent;
  onClose: () => void;
  getContentTypeIconComponent: (type: ContentType) => React.ReactNode;
}

const ContentDetailModal: React.FC<ContentDetailModalProps> = ({
  content,
  onClose,
  getContentTypeIconComponent
}) => {
  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-gray-800 rounded-xl max-w-4xl max-h-[90vh] overflow-y-auto w-full">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-700">
          <div className="flex items-center space-x-3">
            {getContentTypeIconComponent(content.content_type as ContentType)}
            <div>
              <h2 className="text-xl font-bold text-white">{content.title}</h2>
              <p className="text-gray-400 text-sm">by {content.author}</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-white transition-colors"
          >
            <X className="h-6 w-6" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6">
          {/* Badges and Stats */}
          <div className="flex flex-wrap gap-4 items-center">
            <div className={`px-3 py-1 rounded-full text-xs border ${getDifficultyColor(content.difficulty_level as DifficultyLevel)}`}>
              {content.difficulty_level}
            </div>
            <div className="flex items-center gap-1 text-sm text-gray-400">
              <Clock className="h-4 w-4" />
              <span>{content.duration_minutes ? `${content.duration_minutes} minutes` : 'Duration not specified'}</span>
            </div>
            <div className="flex items-center gap-1 text-sm text-gray-400">
              <Eye className="h-4 w-4" />
              <span>{content.view_count} views</span>
            </div>
            <div className="flex items-center gap-1 text-sm text-gray-400">
              <Star className="h-4 w-4" />
              <span>{content.rating.toFixed(1)} rating</span>
            </div>
            {content.is_featured && (
              <div className="bg-yellow-900/30 text-yellow-400 px-3 py-1 rounded-full text-sm flex items-center gap-1">
                <Award className="h-4 w-4" />
                Featured Content
              </div>
            )}
          </div>

          {/* Description */}
          <div>
            <h3 className="text-lg font-semibold text-white mb-3">Description</h3>
            <p className="text-gray-300 leading-relaxed">{content.description}</p>
          </div>

          {/* Details Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h4 className="font-semibold text-white mb-3">Content Details</h4>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-400">Category:</span>
                  <span className="text-blue-400">{getCategoryDisplayName(content.category as ContentCategory)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-400">Source:</span>
                  <span className="text-white">{content.source}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-400">Created:</span>
                  <span className="text-white">{new Date(content.created_at).toLocaleDateString()}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-400">Last Updated:</span>
                  <span className="text-white">{new Date(content.updated_at).toLocaleDateString()}</span>
                </div>
              </div>
            </div>

            {content.user_progress && (
              <div>
                <h4 className="font-semibold text-white mb-3">Your Progress</h4>
                <div className="space-y-3">
                  <div>
                    <div className="flex justify-between text-sm text-gray-400 mb-1">
                      <span>Completion</span>
                      <span>{content.user_progress.progress_percentage}%</span>
                    </div>
                    <div className="w-full bg-gray-700 rounded-full h-3">
                      <div
                        className="bg-blue-400 h-3 rounded-full transition-all"
                        style={{ width: `${content.user_progress.progress_percentage}%` }}
                      ></div>
                    </div>
                  </div>
                  <div className="text-sm space-y-1">
                    <div className="flex justify-between">
                      <span className="text-gray-400">Status:</span>
                      <span className="text-white capitalize">
                        {content.user_progress.completion_status.replace('_', ' ').toLowerCase()}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-400">Time Spent:</span>
                      <span className="text-white">{content.user_progress.time_spent_minutes} minutes</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-400">Last Accessed:</span>
                      <span className="text-white">{new Date(content.user_progress.last_accessed).toLocaleDateString()}</span>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Tags */}
          {content.tags && content.tags.length > 0 && (
            <div>
              <h4 className="font-semibold text-white mb-3">Tags</h4>
              <div className="flex flex-wrap gap-2">
                {content.tags.map((tag, index) => (
                  <span
                    key={index}
                    className="px-3 py-1 bg-gray-700 text-gray-300 rounded-full text-sm flex items-center gap-1"
                  >
                    <Tag className="h-3 w-3" />
                    {tag}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Prerequisites */}
          {content.prerequisites && content.prerequisites.length > 0 && (
            <div>
              <h4 className="font-semibold text-white mb-3">Prerequisites</h4>
              <p className="text-gray-400 text-sm">
                This content builds upon {content.prerequisites.length} other piece(s) of content.
              </p>
            </div>
          )}

          {/* Action Button */}
          <div className="flex gap-4 pt-4 border-t border-gray-700">
            <a
              href={content.external_url}
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center gap-2 px-6 py-3 bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors text-white font-medium"
            >
              <ExternalLink className="h-4 w-4" />
              View Content
            </a>
            <button
              onClick={onClose}
              className="px-6 py-3 bg-gray-600 hover:bg-gray-700 rounded-lg transition-colors text-white"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EducationalContentPage;