package com.datascience;


import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class ActiveMQPublisher {
    private static final String BROKER_URL = "tcp://localhost:61616";

    public void publish(Event event) {
        try {

            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
            factory.setConnectResponseTimeout(10000);
            factory.setExceptionListener(e -> System.err.println("[ActiveMQ] Error: " + e));

            Connection connection = factory.createConnection();
            connection.start();


            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(event.getTopic());
            MessageProducer producer = session.createProducer(topic);

            String json = new com.google.gson.Gson().toJson(event);
            TextMessage message = session.createTextMessage(json);
            producer.send(message);

            System.out.println("[ActiveMQ] Evento publicado en tópico: " + event.getTopic());


            producer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            System.err.println("[ActiveMQ] Error al publicar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}