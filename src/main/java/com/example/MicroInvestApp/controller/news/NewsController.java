package com.example.MicroInvestApp.controller.news;

import com.example.MicroInvestApp.dto.news.NewsArticleDTO;
import com.example.MicroInvestApp.service.news.NewsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/news")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class NewsController {

    private static final Logger logger = LoggerFactory.getLogger(NewsController.class);
    private final NewsService newsService;

    @Autowired
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    /**
     * Health check endpoint for news service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        logger.info("News service health check requested");

        try {
            // Try to fetch a small sample to test API connectivity
            List<NewsArticleDTO> test = newsService.getTopBusinessNews(1);
            logger.info("News service health check passed - fetched {} articles", test.size());

            return ResponseEntity.ok(Map.of(
                    "status", "UP",
                    "service", "NewsAPI",
                    "timestamp", LocalDateTime.now(),
                    "message", "News service is operational",
                    "articlesFound", test.size()
            ));
        } catch (Exception e) {
            logger.warn("News service health check failed: {}", e.getMessage());
            return ResponseEntity.status(503).body(Map.of(
                    "status", "DOWN",
                    "service", "NewsAPI",
                    "timestamp", LocalDateTime.now(),
                    "message", "News service is unavailable",
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Get market news by category
     */
    @GetMapping
    public ResponseEntity<List<NewsArticleDTO>> getMarketNews(
            @RequestParam(defaultValue = "general") String category,
            @RequestParam(defaultValue = "20") int limit) {

        logger.info("Fetching market news for category: {} with limit: {}", category, limit);

        try {
            List<NewsArticleDTO> news = newsService.getMarketNews(category, limit);
            logger.info("Successfully returned {} articles for category: {}", news.size(), category);
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            logger.error("Error fetching market news for category {}: {}", category, e.getMessage());
            // Return empty list instead of error to gracefully handle failures
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get top business headlines
     */
    @GetMapping("/headlines")
    public ResponseEntity<List<NewsArticleDTO>> getTopBusinessNews(
            @RequestParam(defaultValue = "20") int limit) {

        logger.info("Fetching top business headlines with limit: {}", limit);

        try {
            List<NewsArticleDTO> news = newsService.getTopBusinessNews(limit);
            logger.info("Successfully returned {} business headlines", news.size());
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            logger.error("Error fetching business headlines: {}", e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Search news articles (if you want to add this functionality)
     */
    @GetMapping("/search")
    public ResponseEntity<List<NewsArticleDTO>> searchNews(
            @RequestParam String q,
            @RequestParam(defaultValue = "20") int limit) {

        logger.info("Searching news for query: {} with limit: {}", q, limit);

        try {
            // For now, return general news since search isn't implemented in your service yet
            List<NewsArticleDTO> news = newsService.getMarketNews("general", limit);
            logger.info("Successfully returned {} search results for query: {}", news.size(), q);
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            logger.error("Error searching news for query {}: {}", q, e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get available news categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getNewsCategories() {
        logger.info("Fetching news categories");

        List<String> categories = List.of(
                "general",
                "technology",
                "finance",
                "crypto",
                "energy",
                "healthcare",
                "automotive",
                "real-estate"
        );

        logger.info("Returning {} categories", categories.size());
        return ResponseEntity.ok(categories);
    }

    /**
     * Simple test endpoint to verify controller is working
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        logger.info("News controller test endpoint called");

        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "News controller is working",
                "timestamp", LocalDateTime.now(),
                "controller", "NewsController"
        ));
    }
}