package com.example.auto_ria.configurations.rabbitMq;

import com.rabbitmq.client.ConnectionFactory;
import lombok.AllArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Configuration
@AllArgsConstructor
public class RabbitMQConfiguration {

    private Environment environment;

    @Bean
    public Queue dynamicQueue() {
        return new Queue("dynamic.queue.*"); // Use a pattern to match all queues with a specific prefix
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        return new ConnectionFactory();
    }

    //--------------------------

    @Bean
    public RabbitMQMessageRetriever messageRetriever() {
        return new RabbitMQMessageRetriever(); //todo remove
    }

    @Bean
    public Queue queue() {
        return new Queue(environment.getProperty("rabbitmq.queue.name"));
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(environment.getProperty("rabbitmq.queue.exchange"));
    }

    @Bean
    public Binding binding() {
        return BindingBuilder
                .bind(queue())
                .to(exchange())
                .with(environment.getProperty("rabbitmq.routing.key"));
    }
}
