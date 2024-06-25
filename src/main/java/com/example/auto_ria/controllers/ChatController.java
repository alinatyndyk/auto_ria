package com.example.auto_ria.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.auto_ria.dao.socket.MessageDaoSQL;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.socket.Chat;
import com.example.auto_ria.models.socket.MessageClass;
import com.example.auto_ria.models.user.UserSQL;
import com.example.auto_ria.services.CommonService;
import com.example.auto_ria.services.chat.ChatServiceMySQL;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RequestMapping(value = "chats")
public class ChatController {

    private CommonService commonService;
    private ChatServiceMySQL chatServiceMySQL;

    @GetMapping("chat")
    public ResponseEntity<Chat> getChat(
            @RequestParam("user1Id") String user1Id,
            @RequestParam("user2Id") String user2Id,
            HttpServletRequest request) {
        try {

            UserSQL userSQL = commonService.extractUserFromHeader(request);

            if (userSQL != null
                    && userSQL.getId() != Integer.parseInt(user1Id)
                    && userSQL.getId() != Integer.parseInt(user2Id)) {
                throw new CustomException("Cannot access foreign messages", HttpStatus.UNAUTHORIZED);
            }

            // SellerSQL sellerSQL = commonService.extractSellerFromHeader(request);
            // if (sellerSQL != null && sellerSQL.getId() != Integer.parseInt(sellerId)) {
            // throw new CustomException("Cannot access foreign chat",
            // HttpStatus.UNAUTHORIZED);
            // }
            // CustomerSQL customerSQL = commonService.extractCustomerFromHeader(request);
            // if (customerSQL != null && customerSQL.getId() !=
            // Integer.parseInt(customerId)) {
            // throw new CustomException("Cannot access foreign chat",
            // HttpStatus.UNAUTHORIZED);
            // }

            // if (customerSQL == null && sellerSQL == null) {
            // throw new CustomException("Cannot access foreign chat. Messaging is
            // unavailable for your role",
            // HttpStatus.FORBIDDEN);
            // }

            String roomKey1 = chatServiceMySQL.getRoomKey(user1Id, user2Id);
            String roomKey2 = chatServiceMySQL.getRoomKey(user2Id, user1Id);

            Chat chat;

            Chat chat1 = chatServiceMySQL.getByRoomKey(roomKey1);
            Chat chat2 = chatServiceMySQL.getByRoomKey(roomKey2);

            if (chat1 != null) {
                chat = chat1;
            } else {
                chat = chat2;
            }

            // Chat chat = chatServiceMySQL.getByRoomKey(roomKey);
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

            UserSQL user = commonService.extractUserFromHeader(request);

            if (user == null) {
                throw new CustomException("For now chat function is available for seller and customers only",
                        HttpStatus.FORBIDDEN);
            }

            return ResponseEntity.ok(chatServiceMySQL.getChatsByUserId(user.getId(), page));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/page/{page}")
    public ResponseEntity<Page<MessageClass>> getChatMessages(
            HttpServletRequest request,
            @RequestParam("user1Id") String user1Id,
            @RequestParam("user2Id") String user2Id,
            @PathVariable("page") int page) {
        try {

            UserSQL userSQL = commonService.extractUserFromHeader(request);

            if (userSQL != null
                    && userSQL.getId() != Integer.parseInt(user1Id)
                    && userSQL.getId() != Integer.parseInt(user2Id)) {
                throw new CustomException("Cannot access foreign messages", HttpStatus.UNAUTHORIZED);
            }
            // CustomerSQL customerSQL = commonService.extractCustomerFromHeader(request);

            // if (customerSQL != null && customerSQL.getId() !=
            // Integer.parseInt(customerId)) {
            // throw new CustomException("Cannot access foreign messages",
            // HttpStatus.UNAUTHORIZED);
            // }

            // if (customerSQL == null && sellerSQL == null) {
            // throw new CustomException("Cannot access foreign messages. Chats are
            // unavailable for your role",
            // HttpStatus.FORBIDDEN);
            // }

            // String roomKey = chatServiceMySQL.getRoomKey(user1Id, user2Id);
            String roomKey = chatServiceMySQL.getRoomKey(user1Id, user2Id);
            
            Page<MessageClass> messageClasses = chatServiceMySQL.getMessagesPage(roomKey, page);

            // if (customerSQL != null) {

            //     messageClasses.map(messageClass -> {
            //         if (!messageClass.getIsSeen() && messageClass.getReceiverId().equals(customerId)) {
            //             messageClass.setIsSeen(true);
            //             messageDaoSQL.save(messageClass);
            //         }
            //         return null;
            //     });
            // } else {
            //     messageClasses.map(messageClass -> {
            //         if (!messageClass.getIsSeen() && messageClass.getReceiverId().equals(sellerId)) {
            //             messageClass.setIsSeen(true);
            //             messageDaoSQL.save(messageClass);
            //         }
            //         return null;
            //     });
            // }  /// потом будет син и не син

            List<MessageClass> reversedMessages = new ArrayList<>(messageClasses.getContent());
            Collections.reverse(reversedMessages);

            Page<MessageClass> reversedPage = new PageImpl<>(reversedMessages, messageClasses.getPageable(),
                    messageClasses.getTotalElements());

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
