package com.example.MicroInvestApp.dto.news;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsApiRawArticle {
    private NewsApiSource source;
    private String author;
    private String title;
    private String description;
    private String url;
    @JsonProperty("urlToImage")
    private String urlToImage;
    @JsonProperty("publishedAt")
    private String publishedAt;
    private String content;

    public NewsApiRawArticle() {}

    // Getters and setters
    public NewsApiSource getSource() { return source; }
    public void setSource(NewsApiSource source) { this.source = source; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getUrlToImage() { return urlToImage; }
    public void setUrlToImage(String urlToImage) { this.urlToImage = urlToImage; }

    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}