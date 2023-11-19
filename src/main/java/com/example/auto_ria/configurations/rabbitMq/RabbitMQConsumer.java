package com.example.auto_ria.configurations.rabbitMq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQConsumer {

    private String yourQueueName = "blabla";
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQConsumer.class);


//    @RabbitListener(queues = "newQueue")
//    public void consume(String message) {
//        LOGGER.info(String.format("Received message -> %s", message));
//    }
}
