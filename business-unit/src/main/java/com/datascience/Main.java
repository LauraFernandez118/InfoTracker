package com.datascience;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.swing.*;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

import static com.datascience.EventStoreBuilder.saveToFile;

public class Main {
    // Configuración de APIs
    private static final String NEWS_API_KEY = "29ee668ea1e3452487efcf707588897f";
    private static final String YOUTUBE_API_KEY = "AIzaSyABCVIV0YNFK5C2Nrdk2aWIPyCJoGfvypg";
    private static final String BROKER_URL = "tcp://localhost:61616";

    // Componentes principales
    private static final EventLoader loader = new EventLoader();
    private static final DataMart dataMart = new DataMart();

    public static void main(String[] args) {

        // 2. Luego el resto (GUI, carga de datos, etc.)

        configureLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            MainWindow mainWindow = new MainWindow(dataMart, loader);
            mainWindow.setVisible(true);
            loadInitialData(mainWindow::updateStatus);
        });
    }

    private static void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error configurando Look and Feel:");
            e.printStackTrace();
        }
    }

    private static void loadInitialData(Consumer<String> statusUpdater) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                statusUpdater.accept("Cargando datos iniciales...");
                List<Event> events = loader.loadEvents("eventstore");
                events.forEach(e -> {
                    switch (e.getTopic()) {
                        case "news" -> dataMart.addEvent(new NewsEvent(e));
                        case "video" -> dataMart.addEvent(new VideoEvent(e));
                    }
                });
                return null;
            }

            @Override
            protected void done() {
                statusUpdater.accept("Sistema listo. " +
                        dataMart.getNewsEvents().size() + " noticias y " +
                        dataMart.getVideoEvents().size() + " videos cargados");
            }
        }.execute();
    }

    // Métodos de búsqueda
    public static void fetchNews(String topic, Consumer<String> resultHandler, Runnable onComplete) {
        executeSearch(topic, "news", resultHandler, onComplete);
    }

    public static void fetchYouTubeVideos(String topic, Consumer<String> resultHandler, Runnable onComplete) {
        executeSearch(topic, "youtube", resultHandler, onComplete);
    }

    private static void executeSearch(String topic, String type, Consumer<String> resultHandler, Runnable onComplete) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String apiUrl = buildApiUrl(topic, type);
                String responseBody = makeHttpRequest(apiUrl);
                processResults(type, topic, responseBody, resultHandler);
                return null;
            }

            @Override
            protected void done() {
                onComplete.run();
            }
        }.execute();
    }

    private static String buildApiUrl(String topic, String type) {
        String formattedTopic = topic.trim().replace(" ", "+");
        if (type.equals("news")) {
            return "https://newsapi.org/v2/everything?q=" + formattedTopic + "&apiKey=" + NEWS_API_KEY;
        } else {
            return "https://www.googleapis.com/youtube/v3/search?part=snippet&q=" +
                    formattedTopic + "&type=video&key=" + YOUTUBE_API_KEY;
        }
    }

    private static String makeHttpRequest(String url) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .timeout(java.time.Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Error en la API: " + response.statusCode());
        }

        return response.body();
    }

    private static void processResults(String type, String topic, String json,
                                       Consumer<String> handler) {
        try {
            JsonObject data = JsonParser.parseString(json).getAsJsonObject();
            String itemsKey = type.equals("news") ? "articles" : "items";
            JsonArray items = data.getAsJsonArray(itemsKey);

            for (JsonElement item : items) {
                JsonObject obj = item.getAsJsonObject();
                // Para videos de YouTube, construir la URL completa
                if (type.equals("youtube") && obj.has("id")) {
                    JsonObject idObj = obj.getAsJsonObject("id");
                    if (idObj.has("videoId")) {
                        String videoId = idObj.get("videoId").getAsString();
                        obj.addProperty("url", "https://youtube.com/watch?v=" + videoId);
                    }
                }
                handler.accept(formatResult(type, obj));
            }

            saveToFile(type, topic, json);
            sendToActiveMQ(type, json);
            updateDataMart();
        } catch (Exception e) {
            System.err.println("Error procesando resultados de " + type + ". " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static String formatResult(String type, JsonObject obj) {
        if (type.equals("news")) {
            return String.format("Noticia | %s | %s | %s | %s",
                    obj.get("publishedAt").getAsString(),
                    obj.get("title").getAsString(),
                    obj.getAsJsonObject("source").get("name").getAsString(),
                    obj.get("url").getAsString());
        } else {
            JsonObject snippet = obj.getAsJsonObject("snippet");
            return String.format("Video | %s | %s | %s | https://youtube.com/watch?v=%s",
                    snippet.get("publishedAt").getAsString(),
                    snippet.get("title").getAsString(),
                    snippet.get("channelTitle").getAsString(),
                    obj.getAsJsonObject("id").get("videoId").getAsString());
        }
    }


    private static void sendToActiveMQ(String queueName, String content) throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = factory.createConnection();
        try {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(session.createQueue(queueName));
            TextMessage message = session.createTextMessage(content);
            producer.send(message);
        } finally {
            connection.close();
        }
    }

    private static void updateDataMart() {
        List<Event> newEvents = loader.loadEvents("eventstore");
        newEvents.forEach(e -> {
            boolean exists = dataMart.getNewsEvents().stream()
                    .anyMatch(n -> n.getTitle().equals(e.getData().get("title").getAsString())) ||
                    dataMart.getVideoEvents().stream()
                            .anyMatch(v -> v.getTitle().equals(e.getData().get("title").getAsString()));

            if (!exists) {
                switch (e.getTopic()) {
                    case "news" -> dataMart.addEvent(new NewsEvent(e));
                    case "video" -> dataMart.addEvent(new VideoEvent(e));
                }
            }
        });

    }

}