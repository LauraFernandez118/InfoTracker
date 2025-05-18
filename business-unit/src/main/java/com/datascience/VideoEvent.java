package com.datascience;

import com.google.gson.JsonObject;

public class VideoEvent {
    private final String title;
    private final String date;
    private final String channel;
    private final String url; // Nuevo campo

    public VideoEvent(Event event) {
        JsonObject data = event.getData();
        JsonObject item = data.has("item") ? data.getAsJsonObject("item") : data;
        JsonObject snippet = item.has("snippet") ? item.getAsJsonObject("snippet") : null;

        this.title = snippet != null && snippet.has("title")
                ? snippet.get("title").getAsString()
                : "Sin título";
        this.channel = snippet != null && snippet.has("channelTitle")
                ? snippet.get("channelTitle").getAsString()
                : "Desconocido";
        this.date = event.getTs();

        // Construir URL de YouTube
        JsonObject idObj = item.has("id") ? item.getAsJsonObject("id") : null;
        String videoId = idObj != null && idObj.has("videoId")
                ? idObj.get("videoId").getAsString()
                : "";
        this.url = !videoId.isEmpty() ? "https://youtube.com/watch?v=" + videoId : "";
    }
    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getChannel() {
        return channel;
    }

    public String getUrl() { return url; }
}