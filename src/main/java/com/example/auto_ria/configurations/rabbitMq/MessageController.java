package com.example.auto_ria.configurations.rabbitMq;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class MessageController {

    private final RabbitMQProducer producer;


    @GetMapping("/publish")
    public ResponseEntity<String> sendMessage(@RequestParam("message") String message,
                                              @RequestParam("to") String to) {

        int sellerId = 3;
        int customerId = Integer.parseInt(to);

        producer.sendMessage(message, sellerId, customerId);
        return ResponseEntity.ok("Message sent to RabbitMQ...");
    }
}
