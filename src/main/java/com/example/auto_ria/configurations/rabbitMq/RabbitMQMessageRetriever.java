package com.example.auto_ria.configurations.rabbitMq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class RabbitMQMessageRetriever {
    private static final String QUEUE_NAME = "newQueue";

    public List<String> retrieveAllMessages() throws IOException, TimeoutException {
    List<String> messages = new ArrayList<>();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // Replace with your RabbitMQ server host
        factory.setPort(5672); // Replace with the appropriate port
        factory.setUsername("guest"); // Replace with your RabbitMQ username
        factory.setPassword("guest"); // Replace with your RabbitMQ password

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            boolean autoAck = true;
            GetResponse response;

//            do {
                response = channel.basicGet(QUEUE_NAME, autoAck);
                System.out.println(response);
                System.out.println(response.getBody());
                System.out.println(response.getMessageCount());
                System.out.println("response");

                if (response != null) {
                    String message = new String(response.getBody());
//                    messages.add(message);
                    message = new String(response.getBody(), "UTF-8");
                    messages.add(message);

                    // Acknowledge the message manually
                    channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                }
//            } while (response != null);
        }

        return messages;
    }
}
