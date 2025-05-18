package com.datascience;

import com.google.gson.JsonObject;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class YouTubeEventPublisher {
    private static final String BROKER_URL = "tcp://localhost:61616";

    public void publishVideoEvent(YouTubeVideo video) {
        Connection connection = null;
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
            connection = factory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic("Videos");

            MessageProducer producer = session.createProducer(topic);
            JsonObject eventData = video.getVideoData();
            Event event = new Event("youtube-feeder", "Videos", eventData);

            String json = new com.google.gson.Gson().toJson(event);
            TextMessage message = session.createTextMessage(json);
            producer.send(message);

        } catch (JMSException e) {
            System.err.println("Error al publicar: " + e.getMessage());
        } finally {
            if (connection != null) {
                try { connection.close(); } catch (JMSException e) {  }
            }
        }
    }
}