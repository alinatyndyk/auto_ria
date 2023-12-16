package com.example.auto_ria.controllers;

import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.socket.Chat;
import com.example.auto_ria.models.socket.MessageClass;
import com.example.auto_ria.models.user.CustomerSQL;
import com.example.auto_ria.models.user.SellerSQL;
import com.example.auto_ria.services.CommonService;
import com.example.auto_ria.services.chat.ChatServiceMySQL;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RequestMapping(value = "chats")
public class ChatController {

    private CommonService commonService;
    private ChatServiceMySQL chatServiceMySQL;

    @GetMapping("chat")
    public ResponseEntity<Chat> getChat(
            @RequestParam("sellerId") String sellerId,
            @RequestParam("customerId") String customerId,
            HttpServletRequest request
    ) {
        try {

            SellerSQL sellerSQL = commonService.extractSellerFromHeader(request);
            if (sellerSQL != null && sellerSQL.getId() != Integer.parseInt(sellerId)) {
                throw new CustomException("Cannot access foreign chat", HttpStatus.UNAUTHORIZED);
            }
            CustomerSQL customerSQL = commonService.extractCustomerFromHeader(request);
            if (customerSQL != null && customerSQL.getId() != Integer.parseInt(customerId)) {
                throw new CustomException("Cannot access foreign chat", HttpStatus.UNAUTHORIZED);
            }

            if (customerSQL == null && sellerSQL == null) {
                throw new CustomException("Cannot access foreign chat. Messaging is unavailable for your role",
                        HttpStatus.FORBIDDEN);
            }

            String roomKey = chatServiceMySQL.getRoomKey(customerId, sellerId);

            Chat chat = chatServiceMySQL.getByRoomKey(roomKey);
            return ResponseEntity.ok(chat);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @GetMapping("/of-user/page/{page}")
    public ResponseEntity<Page<Chat>> getChatsByUser(
            HttpServletRequest request,
            @PathVariable("page") int page) {
        try {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! of user");
            SellerSQL sellerSQL = commonService.extractSellerFromHeader(request);
            CustomerSQL customerSQL = commonService.extractCustomerFromHeader(request);
            System.out.println("---------" + sellerSQL + " " + customerSQL);
            int id;
            ERole role;

            if (sellerSQL != null) {
                id = sellerSQL.getId();
                role = ERole.SELLER;
            } else if (customerSQL != null) {
                id = customerSQL.getId();
                role = ERole.CUSTOMER;
            } else {
                throw new CustomException("For now chat function is available for seller and customers only",
                        HttpStatus.FORBIDDEN);
            }

            return ResponseEntity.ok(chatServiceMySQL.getChatsByUserId(id, role, page));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/page/{page}")
    public ResponseEntity<Page<MessageClass>> getChatMessages(
            HttpServletRequest request,
            @RequestParam("sellerId") String sellerId,
            @RequestParam("customerId") String customerId,
            @PathVariable("page") int page
    ) {
        try {
            SellerSQL sellerSQL = commonService.extractSellerFromHeader(request);
            if (sellerSQL != null && sellerSQL.getId() != Integer.parseInt(sellerId)) {
                throw new CustomException("Cannot access foreign messages", HttpStatus.UNAUTHORIZED);
            }
            CustomerSQL customerSQL = commonService.extractCustomerFromHeader(request);
            if (customerSQL != null && customerSQL.getId() != Integer.parseInt(customerId)) {
                throw new CustomException("Cannot access foreign messages", HttpStatus.UNAUTHORIZED);
            }

            if (customerSQL == null && sellerSQL == null) {
                throw new CustomException("Cannot access foreign messages. Chats are unavailable for your role",
                        HttpStatus.FORBIDDEN);
            }

            String roomKey = chatServiceMySQL.getRoomKey(customerId, sellerId);
            Page<MessageClass> messageClasses = chatServiceMySQL.getMessagesPage(roomKey, page);

            List<MessageClass> reversedMessages = new ArrayList<>(messageClasses.getContent());
            Collections.reverse(reversedMessages);

            Page<MessageClass> reversedPage = new PageImpl<>(reversedMessages, messageClasses.getPageable(), messageClasses.getTotalElements());

            return ResponseEntity.ok(reversedPage);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PatchMapping("/message/{id}")
    public ResponseEntity<MessageClass> patch(
            HttpServletRequest request,
            @PathVariable int id,
            @RequestParam("content") String content) {
        try {
            return ResponseEntity.ok(chatServiceMySQL
                    .patchMessage(chatServiceMySQL.hasAccessToMessage(id, request), content));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

}
