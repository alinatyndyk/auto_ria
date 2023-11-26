package com.example.auto_ria.controllers;

import com.example.auto_ria.dao.socket.ChatDaoSQL;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.socket.Chat;
import com.example.auto_ria.models.socket.MessageClass;
import com.example.auto_ria.services.CommonService;
import com.example.auto_ria.services.chat.ChatServiceMySQL;
import com.example.auto_ria.services.user.AdministratorServiceMySQL;
import com.example.auto_ria.services.user.ManagerServiceMySQL;
import com.example.auto_ria.services.user.UsersServiceMySQLImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RequestMapping(value = "chats")
public class ChatController {

    private ManagerServiceMySQL managerServiceMySQL;
    private UsersServiceMySQLImpl usersServiceMySQL;
    private CommonService commonService;
    private AdministratorServiceMySQL administratorServiceMySQL;
    private ChatServiceMySQL chatServiceMySQL;
    private ChatDaoSQL chatDaoSQL;

    @GetMapping("chat")
    public ResponseEntity<Chat> getChat(
            @RequestParam("sellerId") String sellerId,
            @RequestParam("customerId") String customerId
    ) {
        try {
            String roomKey = chatServiceMySQL.getRoomKey(customerId, sellerId);

            Chat chat = chatServiceMySQL.getByRoomKey(roomKey);
            System.out.println(chat);
            return ResponseEntity.ok(chat);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PostMapping("/page/{page}") // todo change to get
    public ResponseEntity<Page<MessageClass>> getChatMessages(
            @PathVariable("page") int page,
            @RequestParam("sellerId") String sellerId,
            @RequestParam("customerId") String customerId
    ) {
        try {
            String roomKey = chatServiceMySQL.getRoomKey(customerId, sellerId);

            Page<MessageClass> messageClasses = chatServiceMySQL.getMessagesPage(roomKey, page);

            return ResponseEntity.ok(messageClasses);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @PatchMapping("/message/{id}")
    public ResponseEntity<MessageClass> patch(
//            HttpServletRequest request,
            @PathVariable int id,
            @RequestParam("content") String content) {
        try {
//            managerServiceMySQL.checkCredentials(request, id);

            return ResponseEntity.ok(chatServiceMySQL.patchMessage(id, content));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable int id,
                                             HttpServletRequest request) {
        try {
            if (administratorServiceMySQL.getById(String.valueOf(id)).getBody() == null) {
                managerServiceMySQL.checkCredentials(request, id);
            }
            commonService.removeAvatar(Objects.requireNonNull(managerServiceMySQL.getById(id).getBody()).getAvatar());
            return managerServiceMySQL.deleteById(id);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        }
    }

}
