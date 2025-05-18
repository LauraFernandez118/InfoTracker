package org.junit.jupiter;

import org.apache.activemq.ActiveMQConnectionFactory;


import javax.jms.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Scanner;

import java.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.BufferedWriter;

import java.nio.file.Files;
import java.nio.file.StandardOpenOption;


public class Main {

    private static final String NEWS_API_KEY = "29ee668ea1e3452487efcf707588897f";
    private static final String YOUTUBE_API_KEY = "AIzaSyABCVIV0YNFK5C2Nrdk2aWIPyCJoGfvypg";
    private static final String BROKER_URL = "tcp://localhost:61616";

    public static void main(String[] args) {
        // Crear scanner para pedir el tema
        Scanner scanner = new Scanner(System.in);
        System.out.print("Introduce el tema de búsqueda: ");
        String userTopic = scanner.nextLine().trim().replace(" ", "+");

        // Llamar al proceso para obtener noticias
        fetchNews(userTopic);

        // Llamar al proceso para obtener videos de YouTube
        fetchYouTubeVideos(userTopic);
    }

    // Método para obtener noticias desde NewsAPI
    private static void fetchNews(String topic) {
        System.out.println("Llamando a fetchNews con topic: " + topic); // ← NUEVO

        String formattedTopic = topic.trim().replace(" ", "+");
        String apiUrl = "https://newsapi.org/v2/everything?q=" + formattedTopic + "&apiKey=" + NEWS_API_KEY;

        try {
            // Petición HTTP
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();
            saveToFile("news", topic, responseBody);
            // Envío a la cola de ActiveMQ
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
            Connection connection = factory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue("news");
            MessageProducer producer = session.createProducer(destination);

            TextMessage message = session.createTextMessage(response.body());
            producer.send(message);

            System.out.println("Mensaje enviado a la cola 'news' con el tema: " + topic);

            // Cierre
            producer.close();
            session.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Método para obtener videos de YouTube
    private static void fetchYouTubeVideos(String topic) {
        String formattedTopic = topic.trim().replace(" ", "+");
        String apiUrl = "https://www.googleapis.com/youtube/v3/search?part=snippet&q="
                + formattedTopic + "&type=video&key=" + YOUTUBE_API_KEY;

        try {
            // Petición HTTP
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            saveToFile("youtube", topic, responseBody);
            // Envío a la cola de ActiveMQ
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
            Connection connection = factory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue("youtube");
            MessageProducer producer = session.createProducer(destination);

            TextMessage message = session.createTextMessage(response.body());
            producer.send(message);

            System.out.println("Mensaje enviado a la cola 'youtube' con el tema: " + topic);

            // Cierre
            producer.close();
            session.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para enviar mensajes a ActiveMQ
    private static void sendToQueue(String queueName, String messageContent) {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
            Connection connection = factory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(queueName);
            MessageProducer producer = session.createProducer(destination);

            TextMessage message = session.createTextMessage(messageContent);
            producer.send(message);

            System.out.println("Mensaje enviado a la cola '" + queueName + "'");

            // Cierre
            producer.close();
            session.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void saveToFile(String sourceSystem, String topic, String content) throws IOException {
        String safeTopic = topic.replaceAll("\\s+", "_").toLowerCase(); // Reemplaza espacios
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // YYYYMMDD

        // Crear ruta del archivo: eventstore/{topic}/{ss}/{YYYYMMDD}.events
        Path filePath = Paths.get("eventstore", safeTopic, sourceSystem, date + ".events");

        // Crear directorios si no existen
        Files.createDirectories(filePath.getParent());

        // Añadir evento al final del archivo, con salto de línea
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(content);
            writer.newLine();
        }
    }
}
