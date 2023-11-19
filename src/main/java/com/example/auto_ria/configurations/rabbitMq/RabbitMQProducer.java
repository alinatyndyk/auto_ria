package com.example.auto_ria.configurations.rabbitMq;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer {

    private final Environment environment;

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private RabbitMQMessageRetriever messageRetriever;

    public RabbitMQProducer(Environment environment, RabbitTemplate rabbitTemplate, RabbitMQMessageRetriever messageRetriever) {
        this.environment = environment;
        this.rabbitTemplate = rabbitTemplate;
        this.messageRetriever = messageRetriever;
    }

    @SneakyThrows
    public void sendMessage(String message) {
        LOGGER.info(String.format("Message sent -> %s", message));

//        MessageProperties properties = new MessageProperties();
//        properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
//        Message message1 = new Message(message.getBytes(), properties);
//        rabbitTemplate.convertAndSend("myQueue", message);

        rabbitTemplate.convertAndSend("newQueueExchange",
                "routingKey",
                message);

//        System.out.println( messageRetriever.retrieveAllMessages());
//        System.out.println( "messageRetriever.retrieveAllMessages()");
    }

}
