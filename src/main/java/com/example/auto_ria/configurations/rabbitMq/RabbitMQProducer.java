package com.example.auto_ria.configurations.rabbitMq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void declareQueueExchangeBinding(String queueName, String exchangeName, String routingKey) {

        rabbitTemplate.execute(channel -> {
            channel.queueDeclare(queueName, true, false, false, null);
            channel.exchangeDeclare(exchangeName, "topic", true);
            channel.queueBind(queueName, exchangeName, routingKey);
            return null;
        });
    }

    public void sendMessage(String message, int sellerID, int customerID) {
        LOGGER.info(String.format("Message sent -> %s", message));

        declareQueueExchangeBinding(getQueueName(customerID, sellerID),
                getExchange(customerID, sellerID),
                getRoutingKey(customerID, sellerID));

        MessageProperties properties = new MessageProperties();
        properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        Message amqpMessage = new Message(message.getBytes(), properties);

        rabbitTemplate.send(getExchange(customerID, sellerID),
                getRoutingKey(customerID, sellerID),
                amqpMessage);
    }

    private String getRoutingKey(int customerId, int sellerId) {
        return customerId + "-" + sellerId + "-routingKey";
    }

    private String getExchange(int customerId, int sellerId) {
        return customerId + "-" + sellerId + "-exchange";
    }

    private String getQueueName(int customerId, int sellerId) {
        return "dynamic.queue." + customerId + "-" + sellerId;
    }

}
