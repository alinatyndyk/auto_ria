package com.example.auto_ria.configurations.rabbitMq;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
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

    public void declareQueueExchangeBinding(String queueName, String exchangeName, String routingKey) {
        Queue queue = new Queue(queueName);
        TopicExchange exchange = new TopicExchange(exchangeName);
        System.out.println(exchangeName);
        System.out.println("exchangeName");
        System.out.println(queueName);
        System.out.println("queueName");
        System.out.println(routingKey);
        System.out.println("routingKey");

        Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);

        rabbitTemplate.execute(channel -> {
            channel.queueDeclare(queueName, true, false, false, null);
            channel.exchangeDeclare(exchangeName, "topic", true);
            channel.queueBind(queueName, exchangeName, routingKey);
            return null;
        });
    }

    @SneakyThrows
    public void sendMessage(String message, int sellerID, int customerID) { //also newqueueecxh and routing key
        LOGGER.info(String.format("Message sent -> %s", message));

        declareQueueExchangeBinding(getQueueName(customerID, sellerID),
                getExchange(customerID, sellerID),
                getRoutingKey(customerID, sellerID));

        rabbitTemplate.convertAndSend(getExchange(customerID, sellerID),
                getRoutingKey(customerID, sellerID),
                message);
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
