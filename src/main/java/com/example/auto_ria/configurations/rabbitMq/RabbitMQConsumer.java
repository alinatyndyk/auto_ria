package com.example.auto_ria.configurations.rabbitMq;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListeners;
import org.springframework.amqp.support.AmqpHeaders;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RabbitMQConsumer {

    private String yourQueueName = "blabla";
    private List<String> queuesArray = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQConsumer.class);


//    @RabbitListener(queues = "1-3")
    @RabbitListener(queues = "dynamic.queue.*")
    public void consume(Message message) {
        String queueName = message.getMessageProperties().getConsumerQueue();
        System.out.println(queueName + " queueName");
        LOGGER.info(String.format("Received message -> %s", message));
//        LOGGER.info(String.format("Received from queue -> %s", request.getHeader(AmqpHeaders.CONSUMER_QUEUE)));
    }

    public List<String> dynamicQueueNames() {
        // Logic to retrieve the list of queue names, for example from a database or configuration
        List<String> queues = new ArrayList<>();
        queues.add("1-3");
        queues.add("1-5");
        // Add logic here to populate the list of queue names dynamically
        return queues;
    }

//    @RabbitListener
//    public void consumeAll(String message, HttpServletRequest request) {
//
//        System.out.println(request.getHeader(AmqpHeaders.CONSUMER_QUEUE));
//        LOGGER.info(String.format("Received message -> %s", message));
//        LOGGER.info(String.format("Received from queue -> %s", request.getHeader(AmqpHeaders.CONSUMER_QUEUE)));
//    }
}
