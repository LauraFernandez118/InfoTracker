package com.datascience;

import com.google.gson.JsonObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class YouTubeVideo {
    private String videoId;
    private String title;
    private String channel;
    private String publishedAt;
    private Integer newsArticleId;
    private String url;
    private String queryKeyword;
    // Constructor
    public YouTubeVideo(String videoId, String title, String channel, String publishedAt) {
        this.videoId = videoId;
        this.title = title;
        this.channel = channel;
        this.publishedAt = publishedAt;
        this.url = url;
        this.queryKeyword = queryKeyword;
    }

    // Getters
    public String getVideoId()       { return videoId; }
    public String getTitle()         { return title; }
    public String getChannel()       { return channel; }
    public String getPublishedAt()   { return publishedAt; }
    public Integer getNewsArticleId(){ return newsArticleId; }

    // Setter para la clave foránea
    public void setNewsArticleId(Integer newsArticleId) {
        this.newsArticleId = newsArticleId;
    }

    // Método para obtener los datos del vídeo como JSON (listo para eventos)
    public JsonObject getVideoData() {
        JsonObject json = new JsonObject();
        json.addProperty("videoId", videoId);
        json.addProperty("title", title);
        json.addProperty("channel", channel);
        json.addProperty("publishedAt", publishedAt);
        if (newsArticleId != null) {
            json.addProperty("newsArticleId", newsArticleId);
        }
        return json;
    }


    public LocalDateTime getParsedPublishedAt() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        return LocalDateTime.parse(publishedAt, formatter);
    }
    public String getUrl() {
        return url;
    }

    public String getQueryKeyword() {
        return queryKeyword;
    }

    @Override
    public String toString() {
        return "YouTubeVideo{" +
                "videoId='" + videoId + '\'' +
                ", title='" + title + '\'' +
                ", channel='" + channel + '\'' +
                '}';
    }
}
