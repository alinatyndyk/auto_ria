package com.example.auto_ria.services.chat;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.auto_ria.dao.socket.ChatDaoSQL;
import com.example.auto_ria.dao.socket.MessageDaoSQL;
import com.example.auto_ria.dao.user.UserDaoSQL;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.socket.Chat;
import com.example.auto_ria.models.socket.MessageClass;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ChatServiceMySQL {

    private ChatDaoSQL chatDaoSQL;
    private MessageDaoSQL messageDaoSQL;
    private UserDaoSQL userDaoSQL;

    public String createChatRoom(int user1Id, int user2Id, String user1SessionId, String user2SessionId) {
        try {
            if (userDaoSQL.findById(user1Id).isEmpty() || userDaoSQL.findById(user2Id).isEmpty()) {
                throw new CustomException("Receiver doesnt exist", HttpStatus.BAD_REQUEST);
            }

            String roomKey = getRoomKey(String.valueOf(user1Id), String.valueOf(user2Id));

            List<Integer> userList = new ArrayList<>();
            userList.add(user1Id);
            userList.add(user2Id);

            List<String> sessionList = new ArrayList<>();
            sessionList.add(user1SessionId);
            sessionList.add(user2SessionId);

            chatDaoSQL.save(Chat.builder()
                    .sessions(sessionList)
                    .users(userList)
                    .roomKey(roomKey)
                    .build());

            return roomKey;
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Error creating room", HttpStatus.BAD_REQUEST);
        }
    }

    public String getRoomKey(String customerId, String sellerId) {
        if (customerId.compareTo(sellerId) < 0) {
            return customerId + "-" + sellerId;
        } else {
            return sellerId + "-" + customerId;
        }
    }

    public Page<MessageClass> getMessagesPage(String roomKey, int page) {
        Pageable pageable = PageRequest.of(page, 2, Sort.by("id").descending());
        int chatId = getByRoomKey(roomKey).getId();

        return messageDaoSQL.getByChatId(chatId, pageable);
    }

    public MessageClass getMessageById(int id) {
        return messageDaoSQL.getById(id);
    }

    public Page<Chat> findChatsByUserId(int userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Chat> chatPage = chatDaoSQL.findAll(pageable);
        List<Chat> filteredChats = chatPage.getContent().stream()
                .filter(chat -> chat.getUsers().contains(userId))
                .toList();
        return new PageImpl<>(filteredChats, pageable, chatPage.getTotalElements());
    }

    public Chat save(Chat chat) {
        return chatDaoSQL.save(chat);
    }

    public MessageClass patchMessage(MessageClass messageClass, String content) {
        messageClass.setContent(content);
        messageClass.setIsEdited(true);
        return messageDaoSQL.save(messageClass);
    }

    public ResponseEntity<String> deleteMessage(int id) {
        messageDaoSQL.deleteById(id);
        return ResponseEntity.ok("deleted");
    }

    public ResponseEntity<String> seen(MessageClass messageClass) {
        messageClass.setIsSeen(true);
        messageDaoSQL.save(messageClass);
        return ResponseEntity.ok("seen");
    }

    public Chat getByRoomKey(String roomKey) {
        return chatDaoSQL.getByRoomKey(roomKey);
    }

}
