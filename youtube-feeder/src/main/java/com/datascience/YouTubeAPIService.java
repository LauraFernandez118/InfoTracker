package com.datascience;

import com.google.gson.*;

import okhttp3.*;

import java.util.ArrayList;
import java.util.List;
public class YouTubeAPIService {
    private final OkHttpClient client = new OkHttpClient();
    private final String apiKey;

    public YouTubeAPIService(String apiKey) {
        this.apiKey = apiKey;
    }

    public List<YouTubeVideo> searchVideos(String query) throws Exception {
        String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&q=" + query + "&key=" + apiKey;
        Request request = new Request.Builder().url(url).build();
        List<YouTubeVideo> videos = new ArrayList<>();

        try (Response response = client.newCall(request).execute()) {
            JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
            JsonArray items = json.getAsJsonArray("items");

            for (var item : items) {
                JsonObject snippet = item.getAsJsonObject().getAsJsonObject("snippet");
                YouTubeVideo video = new YouTubeVideo(
                        item.getAsJsonObject().getAsJsonObject("id").get("videoId").getAsString(),
                        snippet.get("title").getAsString(),
                        snippet.get("channelTitle").getAsString(),
                        snippet.get("publishedAt").getAsString()
                );


                videos.add(video);
            }
        }
        return videos;
    }

}