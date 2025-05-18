package com.datascience;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class EventLoader {

    private final ObjectMapper objectMapper = new ObjectMapper();


    public List<NewsEvent> loadNewsEvents(String filePath) {
        try {
            return objectMapper.readValue(new File(filePath), objectMapper.getTypeFactory().constructCollectionType(List.class, NewsEvent.class));
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }


    public List<VideoEvent> loadVideoEvents(String filePath) {
        try {
            return objectMapper.readValue(new File(filePath), objectMapper.getTypeFactory().constructCollectionType(List.class, VideoEvent.class));
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }
    public List<Event> loadEvents(String baseDir) {
        List<Event> events = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(baseDir))) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".events"))
                    .forEach(p -> events.addAll(readSingleFile(p)));
        } catch (IOException e) {
            System.err.println("Error cargando eventos desde " + baseDir);
            e.printStackTrace();
        }
        return events;
    }
    private List<Event> readSingleFile(Path path) {
        List<Event> result = new ArrayList<>();
        try {
            String content = Files.readString(path);


            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            JsonObject json = gson.fromJson(content, JsonObject.class);

            if (json.has("articles")) {
                for (JsonElement article : json.getAsJsonArray("articles")) {
                    result.add(new Event(
                            "news-feeder",
                            "news",
                            article.getAsJsonObject()
                    ));
                }
            } else if (json.has("items")) {
                for (JsonElement item : json.getAsJsonArray("items")) {
                    result.add(new Event(
                            "youtube-feeder",
                            "video",
                            item.getAsJsonObject()
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Error leyendo archivo: " + path);
            e.printStackTrace();
        }
        return result;
    }
}