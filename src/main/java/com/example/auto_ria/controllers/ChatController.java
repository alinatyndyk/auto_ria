package com.example.auto_ria.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.socket.Chat;
import com.example.auto_ria.models.socket.MessageClass;
import com.example.auto_ria.models.user.UserSQL;
import com.example.auto_ria.services.chat.ChatServiceMySQL;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RequestMapping(value = "chats")
public class ChatController {

    private ChatServiceMySQL chatServiceMySQL;
    private UsersServiceMySQLImpl usersServiceMySQL;

    @PreAuthorize("hasRole('ADMIN', 'MANAGER', 'USER')")
    @GetMapping("chat")
    public ResponseEntity<Chat> getChat(
            @RequestParam("user1Id") String user1Id,
            @RequestParam("user2Id") String user2Id,
            HttpServletRequest request) {
        try {
            UserSQL userSQL;

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                userSQL = usersServiceMySQL.getByEmail(userDetails.getUsername());
                if (userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("USER")) &&
                        (userSQL.getId() != Integer.parseInt(user1Id)
                                || userSQL.getId() != Integer.parseInt(user1Id))) {
                    throw new CustomException("Unauthorized. Cannot access foreign chat", HttpStatus.UNAUTHORIZED);
                }
            } else {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            }

            String roomKey = chatServiceMySQL.getRoomKey(user1Id, user2Id);

            return ResponseEntity.ok(chatServiceMySQL.getByRoomKey(roomKey));

        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @GetMapping("/of-user/page/{page}")
    public ResponseEntity<Page<Chat>> getChatsByUser(
            HttpServletRequest request,
            @PathVariable("page") int page) {
        try {

            UserSQL user;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                user = usersServiceMySQL.getByEmail(userDetails.getUsername());
            } else {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            }

            return ResponseEntity.ok(chatServiceMySQL.findChatsByUserId(user.getId(), page, 2));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PreAuthorize("hasRole('ADMIN', 'MANAGER', 'USER')")
    @GetMapping("/page/{page}")
    public ResponseEntity<Page<MessageClass>> getChatMessages(
            HttpServletRequest request,
            @RequestParam("user1Id") String user1Id,
            @RequestParam("user2Id") String user2Id,
            @PathVariable("page") int page) {
        try {

            UserSQL userSQL;

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                userSQL = usersServiceMySQL.getByEmail(userDetails.getUsername());
                if (userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("USER")) &&
                        (userSQL.getId() != Integer.parseInt(user1Id)
                                || userSQL.getId() != Integer.parseInt(user1Id))) {
                    throw new CustomException("Unauthorized. Cannot access foreign chat", HttpStatus.UNAUTHORIZED);
                }
            } else {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            }

            String roomKey = chatServiceMySQL.getRoomKey(user1Id, user2Id);

            Page<MessageClass> messageClasses = chatServiceMySQL.getMessagesPage(roomKey, page);

            List<MessageClass> reversedMessages = new ArrayList<>(messageClasses.getContent());
            Collections.reverse(reversedMessages);

            Page<MessageClass> reversedPage = new PageImpl<>(reversedMessages, messageClasses.getPageable(),
                    messageClasses.getTotalElements());

            return ResponseEntity.ok(reversedPage);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PreAuthorize("hasRole('ADMIN', 'MANAGER', 'USER')")
    @PatchMapping("/message/{id}")
    public ResponseEntity<MessageClass> patch(
            @PathVariable int id,
            @RequestParam("content") String content) {
        try {
            MessageClass messageClass = chatServiceMySQL.getMessageById(id);
            
            if (messageClass == null) {
                throw new CustomException("No message found", HttpStatus.BAD_REQUEST);
            }            
            UserSQL userSQL;
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                userSQL = usersServiceMySQL.getByEmail(userDetails.getUsername());

                if (userSQL.getId() != Integer.parseInt(messageClass.getSenderId())) {
                    throw new CustomException("Unauthorized. Cannot access foreign message", HttpStatus.UNAUTHORIZED);
                }
            } else {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            }
            return ResponseEntity.ok(chatServiceMySQL
                    .patchMessage(messageClass, content));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PreAuthorize("hasRole('ADMIN', 'MANAGER', 'USER')")
    @DeleteMapping("/message/{id}")
    public ResponseEntity<String> delete(
            @PathVariable int id) {
        try {

            MessageClass messageClass = chatServiceMySQL.getMessageById(id);

            if (messageClass == null) {
                throw new CustomException("No message found", HttpStatus.BAD_REQUEST);
            }

            UserSQL userSQL;

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                userSQL = usersServiceMySQL.getByEmail(userDetails.getUsername());
                if (userSQL.getId() != Integer.parseInt(messageClass.getSenderId())
                        || userSQL.getId() != Integer.parseInt(messageClass.getSenderId())) {
                    throw new CustomException("Unauthorized. Cannot access foreign message", HttpStatus.UNAUTHORIZED);
                }
            } else {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            }
            return chatServiceMySQL.deleteMessage(id);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PreAuthorize("hasRole('ADMIN', 'MANAGER', 'USER')")
    @PostMapping("/message/{id}")
    public ResponseEntity<String> seen(
            @PathVariable int id) {
        try {

            MessageClass messageClass = chatServiceMySQL.getMessageById(id);

            if (messageClass == null) {
                throw new CustomException("No message found", HttpStatus.BAD_REQUEST);
            }

            UserSQL userSQL;

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                userSQL = usersServiceMySQL.getByEmail(userDetails.getUsername());
                if (userSQL.getId() != Integer.parseInt(messageClass.getReceiverId())
                        || userSQL.getId() != Integer.parseInt(messageClass.getReceiverId())) {
                    throw new CustomException("Unauthorized. Cannot access foreign message", HttpStatus.UNAUTHORIZED);
                }
            } else {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            }
            return chatServiceMySQL.seen(messageClass);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

}
