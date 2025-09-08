package com.example.MicroInvestApp.dto.news;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsApiRawResponse {
    private String status;
    private int totalResults;
    private List<NewsApiRawArticle> articles;

    public NewsApiRawResponse() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTotalResults() { return totalResults; }
    public void setTotalResults(int totalResults) { this.totalResults = totalResults; }

    public List<NewsApiRawArticle> getArticles() { return articles; }
    public void setArticles(List<NewsApiRawArticle> articles) { this.articles = articles; }
}