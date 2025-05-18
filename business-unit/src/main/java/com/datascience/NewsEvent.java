package com.datascience;

import com.google.gson.JsonObject;

public class NewsEvent {
    private final String title;
    private final String date;
    private final String location;
    private final String url;

    public NewsEvent(Event event) {
        JsonObject data = event.getData();
        JsonObject article = data.has("article") ? data.getAsJsonObject("article") : data;

        this.title = article.has("title") ? article.get("title").getAsString() : "Sin título";
        this.date = event.getTs();
        this.location = article.has("source")
                ? article.getAsJsonObject("source").get("name").getAsString()
                : "Desconocido";
        this.url = article.has("url") ? article.get("url").getAsString() : "";
    }
    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public String getUrl() { return url; }
}