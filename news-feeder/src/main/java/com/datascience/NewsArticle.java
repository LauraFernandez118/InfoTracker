package com.datascience;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NewsArticle {
    private String title;
    private String source;
    private String publishedAt;
    private String url;
    private String queryKeyword;


    public NewsArticle(String title, String source, String publishedAt, String url, String queryKeyword) {
        this.title = title;
        this.source = source;
        this.publishedAt = publishedAt;
        this.url = url;
        this.queryKeyword = queryKeyword;
    }

    public String getTitle() {
        return title;
    }

    public String getSource() {
        return source;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public String getUrl() {
        return url;
    }

    public String getQueryKeyword() {
        return queryKeyword;
    }

    public LocalDateTime getParsedPublishedAt() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        return LocalDateTime.parse(publishedAt, formatter);
    }

    @Override
    public String toString() {
        return "NewsArticle{" +
                "title='" + title + '\'' +
                ", source='" + source + '\'' +
                ", publishedAt='" + publishedAt + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}