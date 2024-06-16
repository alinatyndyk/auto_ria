package com.example.auto_ria.services.chat;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.auto_ria.dao.socket.ChatDaoSQL;
import com.example.auto_ria.dao.socket.MessageDaoSQL;
import com.example.auto_ria.dao.user.UserDaoSQL;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.socket.Chat;
import com.example.auto_ria.models.socket.MessageClass;
import com.example.auto_ria.models.user.UserSQL;
import com.example.auto_ria.services.CommonService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ChatServiceMySQL {

    private ChatDaoSQL chatDaoSQL;
    private MessageDaoSQL messageDaoSQL;

    private CommonService commonService;
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
        return customerId + "-" + sellerId;
    }

    public Page<MessageClass> getMessagesPage(String roomKey, int page) {
        Pageable pageable = PageRequest.of(page, 2, Sort.by("id").descending());
        int chatId = getByRoomKey(roomKey).getId();

        return messageDaoSQL.getByChatId(chatId, pageable);
    }

    public void save(Chat chat) {
        chatDaoSQL.save(chat);
    }

    public MessageClass patchMessage(MessageClass messageClass, String content) {
        messageClass.setContent(content);
        messageClass.setIsEdited(true);
        messageDaoSQL.save(messageClass);
        return messageClass;
    }

    public MessageClass hasAccessToMessage(int id, HttpServletRequest request) {

        if (messageDaoSQL.findById(id).isEmpty()) {
            throw new CustomException("Couldn't find message", HttpStatus.BAD_REQUEST);
        }
        MessageClass messageClass = messageDaoSQL.findById(id).get();

        UserSQL userSQL = commonService.extractUserFromHeader(request);

        if (Integer.parseInt(messageClass.getSenderId()) != userSQL.getId()) {
            throw new CustomException("Cannot edit foreign message", HttpStatus.UNAUTHORIZED);
        }

        return messageClass;
    }

    public Page<Chat> getChatsByUserId(int id, int page) {
        Pageable pageable = PageRequest.of(page, 2, Sort.by("updatedAt").descending());
        // if (receiverRole.equals(ERole.CUSTOMER)) {
        // return chatDaoSQL.getByCustomerId(id, pageable);
        // } else {
        return chatDaoSQL.findAllChatsByUserId(id, pageable);
        // }
    }

    public Chat getChatByUserIdFilterReciever(int id, int receiver) {

        return chatDaoSQL.findByUsers(id, receiver);

    }

    public Chat getByRoomKey(String roomKey) {
        return chatDaoSQL.getByRoomKey(roomKey);
    }

    public Chat getByUser1IdandUser2Id(int user1Id, int user2Id) {
        return chatDaoSQL.findByUsers(user1Id, user2Id);
    }

}
