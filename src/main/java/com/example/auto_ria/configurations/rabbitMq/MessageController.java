package com.example.auto_ria.configurations.rabbitMq;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class MessageController {

    private final RabbitMQProducer producer;
    private RabbitMQMessageRetriever messageRetriever;
//    public MessageController(RabbitMQProducer producer) {
//        this.producer = producer;
//    }

    @SneakyThrows
    @GetMapping("/publish")
    public ResponseEntity<String> sendMessage(@RequestParam("message") String message) {
        System.out.println(producer.toString());
        System.out.println("producer");
        producer.sendMessage(message);
        return ResponseEntity.ok("Message sent to RabbitMQ...");
//        return ResponseEntity.ok(messageRetriever.retrieveAllMessages());
    }
}
