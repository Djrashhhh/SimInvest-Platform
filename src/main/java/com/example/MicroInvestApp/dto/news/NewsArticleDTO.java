package com.example.MicroInvestApp.dto.news;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsArticleDTO {
    private String title;
    private String description;
    private String url;
    private String imageUrl;
    private String source;
    private String author;
    private String publishedAt;
    private String content;
    private String category;

    // Constructors
    public NewsArticleDTO() {}

    public NewsArticleDTO(NewsApiRawArticle rawArticle, String category) {
        this.title = rawArticle.getTitle();
        this.description = rawArticle.getDescription();
        this.url = rawArticle.getUrl();
        this.imageUrl = rawArticle.getUrlToImage();
        this.source = rawArticle.getSource() != null ? rawArticle.getSource().getName() : "Unknown Source";
        this.author = rawArticle.getAuthor();
        this.publishedAt = rawArticle.getPublishedAt();
        this.content = rawArticle.getContent();
        this.category = category;
    }

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}