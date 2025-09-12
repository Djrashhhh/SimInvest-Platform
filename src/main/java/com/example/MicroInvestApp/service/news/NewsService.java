package com.example.MicroInvestApp.service.news;

import com.example.MicroInvestApp.dto.news.NewsArticleDTO;
import com.example.MicroInvestApp.dto.news.NewsApiRawResponse;
import com.example.MicroInvestApp.dto.news.NewsApiRawArticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NewsService {

    private static final Logger logger = LoggerFactory.getLogger(NewsService.class);
    private WebClient webClient;

    @Value("${newsapi.key:your_api_key_here}")
    private String newsApiKey;

    @Value("${newsapi.base-url:https://newsapi.org/v2}")
    private String newsApiBaseUrl;

    // Enhanced keyword mapping for better category filtering
    private static final Map<String, Set<String>> CATEGORY_KEYWORDS = Map.of(
            "technology", Set.of("technology", "tech", "software", "ai", "artificial intelligence",
                    "machine learning", "computer", "digital", "internet", "cyber",
                    "startup", "innovation", "app", "platform", "data", "cloud",
                    "semiconductor", "chip", "processor", "silicon valley", "meta",
                    "google", "apple", "microsoft", "amazon", "tesla", "nvidia",
                    "programming", "developer", "coding", "algorithm"),

            "finance", Set.of("finance", "financial", "bank", "banking", "investment", "investor",
                    "stock", "stocks", "market", "trading", "wall street", "nasdaq",
                    "s&p", "dow", "fed", "federal reserve", "interest rate", "inflation",
                    "economy", "economic", "earnings", "revenue", "profit", "ipo",
                    "merger", "acquisition", "securities", "bond", "dividend", "portfolio"),

            "crypto", Set.of("crypto", "cryptocurrency", "bitcoin", "ethereum", "blockchain",
                    "nft", "defi", "web3", "coin", "token", "mining", "wallet",
                    "binance", "coinbase", "solana", "cardano", "dogecoin", "ripple",
                    "digital currency", "virtual currency", "altcoin", "stablecoin"),

            "energy", Set.of("energy", "oil", "gas", "renewable", "solar", "wind", "nuclear",
                    "electricity", "power", "coal", "petroleum", "lng", "pipeline",
                    "green energy", "clean energy", "battery", "ev", "electric vehicle",
                    "utility", "utilities", "grid", "carbon", "emissions", "climate"),

            "healthcare", Set.of("health", "healthcare", "medical", "medicine", "pharma", "pharmaceutical",
                    "drug", "vaccine", "hospital", "doctor", "patient", "fda", "clinical",
                    "biotech", "biotechnology", "therapy", "treatment", "diagnosis",
                    "pfizer", "moderna", "johnson", "merck", "bristol myers"),

            "automotive", Set.of("automotive", "auto", "car", "vehicle", "ford", "gm", "toyota",
                    "honda", "bmw", "mercedes", "audi", "volkswagen", "electric vehicle",
                    "ev", "autonomous", "self-driving", "uber", "lyft", "transportation"),

            "real-estate", Set.of("real estate", "property", "housing", "mortgage", "rent", "rental",
                    "construction", "builder", "developer", "residential", "commercial",
                    "realestate", "home", "apartment", "office", "retail space")
    );

    // Keywords to filter out irrelevant content
    private static final Set<String> EXCLUDE_KEYWORDS = Set.of(
            "football", "basketball", "baseball", "soccer", "tennis", "golf", "hockey",
            "nfl", "nba", "mlb", "nhl", "fifa", "olympics", "sport", "sports", "game",
            "championship", "tournament", "league", "player", "coach", "team", "stadium",
            "match", "score", "season", "playoffs", "draft", "athlete", "athletic",
            "celebrity", "entertainment", "hollywood", "movie", "music", "concert"
    );

    // Trusted business news domains for better filtering
    private static final Set<String> TRUSTED_DOMAINS = Set.of(
            "bloomberg.com", "reuters.com", "wsj.com", "cnbc.com", "marketwatch.com",
            "yahoo.com", "cnn.com", "bbc.com", "ft.com", "techcrunch.com", "venturebeat.com"
    );

    @PostConstruct
    public void init() {
        logger.info("🚀 Initializing NewsService...");
        logger.info("🔍 NewsAPI Base URL: {}", newsApiBaseUrl);
        logger.info("🔑 NewsAPI Key configured: {}", newsApiKey != null && !newsApiKey.equals("your_api_key_here"));

        if (newsApiKey == null || newsApiKey.equals("your_api_key_here")) {
            logger.error("❌ NewsAPI key is not configured! Please set newsapi.key in application.properties");
        } else {
            logger.info("✅ NewsAPI Key preview: {}...", newsApiKey.substring(0, Math.min(8, newsApiKey.length())));
        }

        // Create WebClient with explicit base URL
        this.webClient = WebClient.builder()
                .baseUrl(newsApiBaseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2MB
                .build();

        logger.info("✅ WebClient initialized with base URL: {}", newsApiBaseUrl);
    }

    public List<NewsArticleDTO> getMarketNews(String category, int pageSize) {
        logger.info("🔍 Fetching market news for category: {} with pageSize: {}", category, pageSize);

        try {
            List<NewsArticleDTO> articles = new ArrayList<>();

            if ("general".equals(category)) {
                // For general, mix different approaches
                articles.addAll(getTopBusinessNews(pageSize / 2));
                articles.addAll(fetchCategorySpecificNews("technology", pageSize / 4));
                articles.addAll(fetchCategorySpecificNews("finance", pageSize / 4));
            } else {
                articles = fetchCategorySpecificNews(category, pageSize);
            }

            // Apply intelligent filtering and re-categorization
            List<NewsArticleDTO> filteredArticles = articles.stream()
                    .filter(this::isRelevantBusinessNews)
                    .map(article -> enhanceArticleCategory(article, category))
                    .distinct() // Remove duplicates based on URL
                    .sorted((a, b) -> b.getPublishedAt().compareTo(a.getPublishedAt()))
                    .limit(pageSize)
                    .collect(Collectors.toList());

            logger.info("✅ Returning {} filtered and categorized articles", filteredArticles.size());
            return filteredArticles;

        } catch (Exception e) {
            logger.error("❌ Error fetching market news: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch market news: " + e.getMessage(), e);
        }
    }

    public List<NewsArticleDTO> getTopBusinessNews(int pageSize) {
        logger.info("📈 Fetching top business news with pageSize: {}", pageSize);

        try {
            int limitedPageSize = Math.min(pageSize, 100);
            String endpoint = "/top-headlines";

            logger.info("🌐 Making request to NewsAPI /top-headlines endpoint");

            NewsApiRawResponse rawResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(endpoint)
                            .queryParam("category", "business")
                            .queryParam("country", "us")
                            .queryParam("pageSize", limitedPageSize)
                            .queryParam("apiKey", newsApiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(NewsApiRawResponse.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();

            return convertRawResponse(rawResponse, "business");

        } catch (WebClientResponseException e) {
            logger.error("❌ HTTP error from NewsAPI (headlines): {} - {}", e.getStatusCode(), e.getMessage());
            logger.error("❌ Response body: {}", e.getResponseBodyAsString());
            throw new RuntimeException("NewsAPI error: " + getErrorMessage(e), e);
        } catch (Exception e) {
            logger.error("❌ Unexpected error fetching business headlines: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch top business news: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch category-specific news using targeted search queries
     */
    private List<NewsArticleDTO> fetchCategorySpecificNews(String category, int pageSize) {
        logger.info("🎯 Fetching category-specific news for: {}", category);

        try {
            String endpoint = "/everything";
            String query = getEnhancedQueryForCategory(category);
            String domains = String.join(",", TRUSTED_DOMAINS);
            int limitedPageSize = Math.min(pageSize * 2, 100); // Fetch more to filter better

            logger.info("🔍 Enhanced query for {}: {}", category, query);

            NewsApiRawResponse rawResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(endpoint)
                            .queryParam("q", query)
                            .queryParam("domains", domains)
                            .queryParam("sortBy", "publishedAt")
                            .queryParam("pageSize", limitedPageSize)
                            .queryParam("language", "en")
                            .queryParam("apiKey", newsApiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(NewsApiRawResponse.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();

            return convertRawResponse(rawResponse, category);

        } catch (Exception e) {
            logger.warn("⚠️ Error fetching category-specific news for {}: {}", category, e.getMessage());
            // Fallback to basic query
            return fetchWithBasicQuery(category, pageSize);
        }
    }

    /**
     * Fallback method with basic query
     */
    private List<NewsArticleDTO> fetchWithBasicQuery(String category, int pageSize) {
        try {
            String endpoint = "/everything";
            String query = getQueryForCategory(category);
            int limitedPageSize = Math.min(pageSize, 100);

            NewsApiRawResponse rawResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(endpoint)
                            .queryParam("q", query)
                            .queryParam("sortBy", "publishedAt")
                            .queryParam("pageSize", limitedPageSize)
                            .queryParam("language", "en")
                            .queryParam("apiKey", newsApiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(NewsApiRawResponse.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();

            return convertRawResponse(rawResponse, category);

        } catch (Exception e) {
            logger.error("❌ Fallback query also failed for category {}: {}", category, e.getMessage());
            return List.of();
        }
    }

    /**
     * Enhanced query generation with better keyword targeting
     */
    private String getEnhancedQueryForCategory(String category) {
        Set<String> keywords = CATEGORY_KEYWORDS.get(category.toLowerCase());
        if (keywords == null || keywords.isEmpty()) {
            return getQueryForCategory(category);
        }

        // Use top keywords for the category, excluding sports/entertainment terms
        String query = keywords.stream()
                .limit(5) // Use top 5 keywords to avoid overly long queries
                .collect(Collectors.joining(" OR "));

        // Add exclusion terms
        String exclusions = EXCLUDE_KEYWORDS.stream()
                .map(keyword -> "-" + keyword)
                .collect(Collectors.joining(" "));

        return "(" + query + ") " + exclusions;
    }

    /**
     * Enhanced article category detection and assignment
     */
    private NewsArticleDTO enhanceArticleCategory(NewsArticleDTO article, String requestedCategory) {
        String content = buildContentForAnalysis(article);

        // If the requested category matches well, keep it
        if (!"general".equals(requestedCategory) && matchesCategory(content, requestedCategory)) {
            article.setCategory(requestedCategory);
            return article;
        }

        // Otherwise, detect the best category
        String detectedCategory = detectBestCategory(content);
        article.setCategory(detectedCategory != null ? detectedCategory : "general");

        logger.debug("🏷️ Article '{}' categorized as: {}",
                article.getTitle().substring(0, Math.min(50, article.getTitle().length())),
                article.getCategory());

        return article;
    }

    /**
     * Build content string for category analysis
     */
    private String buildContentForAnalysis(NewsArticleDTO article) {
        StringBuilder content = new StringBuilder();

        if (article.getTitle() != null) {
            content.append(article.getTitle().toLowerCase()).append(" ");
        }
        if (article.getDescription() != null) {
            content.append(article.getDescription().toLowerCase()).append(" ");
        }
        if (article.getContent() != null) {
            content.append(article.getContent().toLowerCase()).append(" ");
        }

        return content.toString();
    }

    /**
     * Check if content matches a specific category
     */
    private boolean matchesCategory(String content, String category) {
        Set<String> keywords = CATEGORY_KEYWORDS.get(category.toLowerCase());
        if (keywords == null) return false;

        return keywords.stream()
                .anyMatch(keyword -> content.contains(keyword.toLowerCase()));
    }

    /**
     * Detect the best category for an article
     */
    private String detectBestCategory(String content) {
        Map<String, Integer> categoryScores = new HashMap<>();

        for (Map.Entry<String, Set<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            String category = entry.getKey();
            Set<String> keywords = entry.getValue();

            int score = 0;
            for (String keyword : keywords) {
                if (content.contains(keyword.toLowerCase())) {
                    // Weight longer keywords more heavily
                    score += keyword.split(" ").length;
                }
            }

            if (score > 0) {
                categoryScores.put(category, score);
            }
        }

        return categoryScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Enhanced relevance filtering
     */
    private boolean isRelevantBusinessNews(NewsArticleDTO article) {
        if (!isValidArticle(article)) {
            return false;
        }

        String content = buildContentForAnalysis(article);

        // Filter out sports, entertainment, and other irrelevant content
        boolean containsExcludedKeywords = EXCLUDE_KEYWORDS.stream()
                .anyMatch(keyword -> content.contains(keyword.toLowerCase()));

        if (containsExcludedKeywords) {
            logger.debug("🚫 Filtered out article due to excluded keywords: {}",
                    article.getTitle().substring(0, Math.min(50, article.getTitle().length())));
            return false;
        }

        // Additional quality checks
        return article.getTitle().length() > 10 &&
                (article.getDescription() == null || article.getDescription().length() > 20);
    }

    private List<NewsArticleDTO> convertRawResponse(NewsApiRawResponse rawResponse, String category) {
        if (rawResponse != null && rawResponse.getArticles() != null) {
            logger.info("✅ Received {} articles from NewsAPI", rawResponse.getArticles().size());
            logger.info("📊 Response status: {}, Total results: {}", rawResponse.getStatus(), rawResponse.getTotalResults());

            List<NewsArticleDTO> convertedArticles = rawResponse.getArticles().stream()
                    .map(rawArticle -> new NewsArticleDTO(rawArticle, category))
                    .filter(this::isValidArticle)
                    .collect(Collectors.toList());

            logger.info("✅ Converted {} valid articles", convertedArticles.size());

            // Log sample article for debugging
            if (!convertedArticles.isEmpty()) {
                NewsArticleDTO sample = convertedArticles.get(0);
                logger.debug("📰 Sample article: title='{}', source='{}', published='{}'",
                        sample.getTitle(), sample.getSource(), sample.getPublishedAt());
            }

            return convertedArticles;
        }

        logger.warn("⚠️ No articles received from NewsAPI (response or articles was null)");
        return List.of();
    }

    private boolean isValidArticle(NewsArticleDTO article) {
        return article != null
                && article.getTitle() != null
                && !article.getTitle().equals("[Removed]")
                && !article.getTitle().trim().isEmpty()
                && article.getUrl() != null
                && !article.getUrl().trim().isEmpty()
                && !article.getTitle().toLowerCase().contains("removed");
    }

    private String getQueryForCategory(String category) {
        String query = switch (category.toLowerCase()) {
            case "technology", "tech" -> "technology OR tech OR startup OR innovation OR software";
            case "finance", "financial" -> "finance OR financial OR banking OR investment OR trading OR stocks";
            case "crypto", "cryptocurrency" -> "cryptocurrency OR bitcoin OR crypto OR blockchain OR ethereum";
            case "energy" -> "energy OR oil OR renewable OR solar OR wind OR electricity";
            case "healthcare" -> "healthcare OR pharmaceutical OR biotech OR medical OR drug";
            case "automotive" -> "automotive OR \"electric vehicle\" OR Tesla OR \"car industry\" OR auto";
            case "real-estate" -> "\"real estate\" OR housing OR property OR mortgage OR construction";
            case "general", "business" -> "business OR economy OR market OR stocks OR trading OR finance";
            default -> "business OR market OR economy OR stocks OR finance";
        };

        logger.debug("🔍 Category '{}' mapped to query: '{}'", category, query);
        return query;
    }

    private String getErrorMessage(WebClientResponseException e) {
        return switch (e.getStatusCode().value()) {
            case 401 -> "Invalid API key. Please check your NewsAPI configuration.";
            case 429 -> "Rate limit exceeded. Please try again later.";
            case 426 -> "Request was made over HTTP instead of HTTPS.";
            case 500 -> "NewsAPI server error. Please try again later.";
            default -> "HTTP " + e.getStatusCode() + ": " + e.getMessage();
        };
    }
}