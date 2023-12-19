package com.example.auto_ria.configurations.rabbitMq;

import lombok.AllArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Objects;

@Configuration
@AllArgsConstructor
public class RabbitMQConfiguration {

    private Environment environment;

    @Bean
    public Queue queue() {
        return new Queue(Objects.requireNonNull(environment.getProperty("rabbitmq.queue.name")));
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
