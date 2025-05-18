package com.datascience;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class EventConsumer {
    private final DataMart dataMart;
    private final ObjectMapper mapper = new ObjectMapper();
    public EventConsumer(DataMart dataMart) {
        this.dataMart = dataMart;
    }
    public void start() {
        try {
            ConnectionFactory factory = new
                    ActiveMQConnectionFactory("tcp://localhost:61616");
            Connection connection = factory.createConnection();
            connection.start();
            Session session = connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            consumeTopic(session, "News", NewsEvent.class);
            consumeTopic(session, "Videos", VideoEvent.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private <T> void consumeTopic(Session session, String topicName, Class<T> type)
            throws JMSException {
        Destination destination = session.createTopic(topicName);
        MessageConsumer consumer = session.createConsumer(destination);
        consumer.setMessageListener(message -> {
            if (message instanceof TextMessage) {
                try {
                    String json = ((TextMessage) message).getText();
                    T event = mapper.readValue(json, type);
                    dataMart.addEvent(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
