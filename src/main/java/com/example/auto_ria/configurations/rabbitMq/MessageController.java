package com.example.auto_ria.configurations.rabbitMq;

import com.example.auto_ria.services.CommonService;
import com.example.auto_ria.services.user.CustomersServiceMySQL;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class MessageController {

    private final RabbitMQProducer producer;
    private CommonService commonService;
    private CustomersServiceMySQL customersServiceMySQL;
    private UsersServiceMySQLImpl usersServiceMySQL;
//    public MessageController(RabbitMQProducer producer) {
//        this.producer = producer;
//    }

    @SneakyThrows
    @GetMapping("/publish")
    public ResponseEntity<String> sendMessage(@RequestParam("message") String message,
                                              @RequestParam("to") String to,
                                              HttpServletRequest request) {

        int sellerId = 3;
        int customerId = Integer.parseInt(to);
//
//        String senderRole;
//
//        SellerSQL seller = commonService.extractSellerFromHeader(request);
//        CustomerSQL customer = commonService.extractCustomerFromHeader(request);
//
//        if (seller != null) {
//            sellerId = seller.getId();
//            senderRole = "seller";
//        } else if (customer != null) {
//            customerId = customer.getId();
//            senderRole = "customer";
//        } else {
//            throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
//        }
//
//        SellerSQL receiverSeller = usersServiceMySQL.getById(to).getBody();
//        CustomerSQL receiverCustomer = customersServiceMySQL.getById(to).getBody();
//---nono
//        if (senderRole.equals("customer") && receiverSeller == null) {
//            throw new CustomException("Reciever not found", HttpStatus.BAD_REQUEST);
//        } else if (senderRole.equals("customer") && receiverSeller != null) {
//            sellerId = receiverSeller.getId();
//        } else if (senderRole.equals("seller") && receiverCustomer != null) {
//            customerId = receiverCustomer.getId();
//        } else if (senderRole.equals("seller") && receiverCustomer == null) {
//            throw new CustomException("Reciever not found", HttpStatus.BAD_REQUEST);
//        }
        //--nono

//        if (senderRole.equals("customer") && receiverSeller == null) {
//            throw new CustomException("Reciever not found", HttpStatus.BAD_REQUEST);
//        } else if (senderRole.equals("customer")) {
//            sellerId = receiverSeller.getId();
//        } else if (receiverCustomer != null) {
//            customerId = receiverCustomer.getId();
//        } else {
//            throw new CustomException("Reciever not found", HttpStatus.BAD_REQUEST);
//        }

        // get token and retrieve id + role
        System.out.println(producer.toString());
        System.out.println("producer");

        producer.sendMessage(message, sellerId, customerId); // pass prId consId
        return ResponseEntity.ok("Message sent to RabbitMQ...");
//        return ResponseEntity.ok(messageRetriever.retrieveAllMessages());
    }
}
