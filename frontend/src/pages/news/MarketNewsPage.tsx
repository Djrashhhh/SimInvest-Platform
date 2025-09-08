import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom"; // Add this import
import {
  ArrowLeft,
  Calendar,
  ExternalLink,
  Filter,
  RefreshCw,
  Search,
  TrendingUp,
  Globe,
  Zap,
  DollarSign,
  Cpu,
  Car,
  Home,
  Activity,
  Newspaper,
  Clock,
} from "lucide-react";

// Import your news service
import { newsService, type NewsArticle } from "../../services/newsService";

// Category configurations
const newsCategories = [
  { id: "general", name: "All News", icon: Globe, color: "blue" },
  { id: "technology", name: "Technology", icon: Cpu, color: "purple" },
  { id: "finance", name: "Finance", icon: DollarSign, color: "green" },
  { id: "crypto", name: "Crypto", icon: Zap, color: "yellow" },
  { id: "energy", name: "Energy", icon: Activity, color: "orange" },
  { id: "healthcare", name: "Healthcare", icon: Activity, color: "pink" },
  { id: "automotive", name: "Automotive", icon: Car, color: "red" },
  { id: "real-estate", name: "Real Estate", icon: Home, color: "indigo" },
];

const MarketNewsPage: React.FC = () => {
  const navigate = useNavigate(); // Add this hook
  const [articles, setArticles] = useState<NewsArticle[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedCategory, setSelectedCategory] = useState("general");
  const [searchTerm, setSearchTerm] = useState("");

  const fetchNews = async (category: string = "general") => {
    setLoading(true);
    setError(null);

    try {
      console.log("Fetching news for category:", category);
      let newsData: NewsArticle[];

      if (category === "headlines") {
        newsData = await newsService.getTopBusinessNews(30);
      } else {
        newsData = await newsService.getMarketNews(category, 30);
      }

      console.log("News data received:", newsData);
      setArticles(newsData);
    } catch (err: any) {
      console.error("Error fetching news:", err);
      setError(err.message || "Failed to load news");
      setArticles([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNews(selectedCategory);
  }, [selectedCategory]);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const filteredArticles = articles.filter(
    (article) =>
      searchTerm === "" ||
      article.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      article.description?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const getCategoryColor = (categoryId: string) => {
    const category = newsCategories.find((cat) => cat.id === categoryId);
    return category?.color || "blue";
  };

  const handleBackToDashboard = () => {
    navigate("/dashboard"); // Use navigate to go back to the dashboard
  };

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      {/* Header */}
      <div className="bg-gray-800 shadow-lg">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center space-x-4">
              <button
                onClick={handleBackToDashboard}
                className="flex items-center space-x-2 text-gray-400 hover:text-white transition-colors"
              >
                <ArrowLeft className="h-5 w-5" />
                <span>Back to Dashboard</span>
              </button>
              <div className="h-6 w-px bg-gray-600"></div>
              <h1 className="text-2xl font-bold text-white flex items-center space-x-2">
                <TrendingUp className="h-7 w-7 text-blue-400" />
                <span>Market News</span>
              </h1>
            </div>

            <button
              onClick={() => fetchNews(selectedCategory)}
              disabled={loading}
              className="flex items-center space-x-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors disabled:opacity-50"
            >
              <RefreshCw
                className={`h-4 w-4 ${loading ? "animate-spin" : ""}`}
              />
              <span>Refresh</span>
            </button>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Controls */}
        <div className="flex flex-col lg:flex-row gap-4 mb-8">
          {/* Search */}
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="text"
              placeholder="Search news..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:border-blue-500"
            />
          </div>

          {/* Category Filter */}
          <div className="flex items-center space-x-2">
            <Filter className="h-5 w-5 text-gray-400" />
            <span className="text-gray-400 text-sm">Filter:</span>
          </div>
        </div>

        {/* Category Tabs */}
        <div className="flex flex-wrap gap-2 mb-8">
          {newsCategories.map((category) => {
            const IconComponent = category.icon;
            const isActive = selectedCategory === category.id;
            return (
              <button
                key={category.id}
                onClick={() => setSelectedCategory(category.id)}
                className={`flex items-center space-x-2 px-4 py-2 rounded-lg font-medium transition-all ${
                  isActive
                    ? `bg-${category.color}-600 text-white`
                    : "bg-gray-800 text-gray-400 hover:text-white hover:bg-gray-700"
                }`}
              >
                <IconComponent className="h-4 w-4" />
                <span>{category.name}</span>
              </button>
            );
          })}
        </div>

        {/* Content */}
        {loading ? (
          <div className="flex justify-center items-center h-64">
            <div className="text-center">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-400 mx-auto mb-4"></div>
              <p className="text-gray-400">Loading latest market news...</p>
            </div>
          </div>
        ) : error ? (
          <div className="text-center py-12">
            <div className="bg-red-900/20 border border-red-500/50 rounded-lg p-6 max-w-md mx-auto">
              <Newspaper className="h-12 w-12 text-red-400 mx-auto mb-4" />
              <p className="text-red-400 mb-4">Failed to load news</p>
              <p className="text-gray-400 text-sm mb-4">{error}</p>
              <button
                onClick={() => fetchNews(selectedCategory)}
                className="px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors"
              >
                Try Again
              </button>
            </div>
          </div>
        ) : (
          <>
            {/* Results count */}
            <div className="flex items-center justify-between mb-6">
              <p className="text-gray-400">
                {filteredArticles.length} articles found
                {searchTerm && ` for "${searchTerm}"`}
              </p>
              <span className="text-sm text-gray-500">
                Last updated: {new Date().toLocaleTimeString()}
              </span>
            </div>

            {/* News Grid */}
            {filteredArticles.length === 0 ? (
              <div className="text-center py-12">
                <Newspaper className="h-16 w-16 text-gray-400 mx-auto mb-4" />
                <p className="text-gray-400 text-lg">No articles found</p>
                <p className="text-gray-500 mt-2">
                  Try adjusting your search or category filter
                </p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {filteredArticles.map((article, index) => (
                  <article
                    key={`${article.url}-${index}`}
                    className="bg-gray-800 rounded-xl overflow-hidden shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-105"
                  >
                    {/* Image */}
                    {article.imageUrl && (
                      <div className="aspect-video bg-gray-700 overflow-hidden">
                        <img
                          src={article.imageUrl}
                          alt={article.title}
                          className="w-full h-full object-cover"
                          onError={(e) => {
                            const target = e.target as HTMLImageElement;
                            target.style.display = "none";
                          }}
                        />
                      </div>
                    )}

                    <div className="p-6">
                      {/* Category Badge */}
                      <div className="flex items-center justify-between mb-3">
                        <span
                          className={`px-2 py-1 text-xs font-medium rounded-full bg-${getCategoryColor(article.category)}-600 text-white`}
                        >
                          {article.category.charAt(0).toUpperCase() +
                            article.category.slice(1)}
                        </span>
                        <span className="text-xs text-gray-400 flex items-center">
                          <Calendar className="h-3 w-3 mr-1" />
                          {formatDate(article.publishedAt)}
                        </span>
                      </div>

                      {/* Title */}
                      <h3 className="text-lg font-semibold text-white mb-3 line-clamp-2">
                        {article.title}
                      </h3>

                      {/* Description */}
                      {article.description && (
                        <p className="text-gray-400 text-sm mb-4 line-clamp-3">
                          {article.description}
                        </p>
                      )}

                      {/* Footer */}
                      <div className="flex items-center justify-between">
                        <span className="text-xs text-gray-500">
                          {article.source}
                        </span>
                        <a
                          href={article.url}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="flex items-center space-x-1 text-blue-400 hover:text-blue-300 text-sm font-medium"
                        >
                          <span>Read more</span>
                          <ExternalLink className="h-3 w-3" />
                        </a>
                      </div>
                    </div>
                  </article>
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default MarketNewsPage;
