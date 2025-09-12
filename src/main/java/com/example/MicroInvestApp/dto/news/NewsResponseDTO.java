package com.example.MicroInvestApp.dto.news;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsResponseDTO {

    private String status;
    private int totalResults;
    private List<NewsArticleDTO> articles;

    // Constructors, getters, and setters
    public NewsResponseDTO() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTotalResults() { return totalResults; }
    public void setTotalResults(int totalResults) { this.totalResults = totalResults; }

    public List<NewsArticleDTO> getArticles() { return articles; }
    public void setArticles(List<NewsArticleDTO> articles) { this.articles = articles; }
}
