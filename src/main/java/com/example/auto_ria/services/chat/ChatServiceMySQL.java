package com.example.auto_ria.services.chat;

import com.example.auto_ria.dao.socket.ChatDaoSQL;
import com.example.auto_ria.dao.socket.MessageDaoSQL;
import com.example.auto_ria.models.socket.Chat;
import com.example.auto_ria.models.socket.MessageClass;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ChatServiceMySQL {

    private ChatDaoSQL chatDaoSQL;
    private MessageDaoSQL messageDaoSQL;

    public String createChatRoom(String customerId, String sellerId, String customerSessionId, String sellerSessionId, String state) {
        String roomKey = getRoomKey(customerId, sellerId);

        chatDaoSQL.save(Chat.builder() // todo check if ids exist in db
                .sellerId(Integer.parseInt(sellerId))
                .customerId(Integer.parseInt(customerId))
                //sessions
                .sellerSessionId(sellerSessionId)
                .customerSessionId(customerSessionId)
                .roomKey(roomKey)
                .build());

        return roomKey;
    }

    public String getRoomKey(String customerId, String sellerId) {
        return customerId + "-" + sellerId;
    }

    public Page<MessageClass> getMessagesPage(String roomKey, int page) {
        Pageable pageable = PageRequest.of(page, 2);
        return messageDaoSQL.getByChatId(getByRoomKey(roomKey).getId(), pageable);
    }

    public MessageClass patchMessage(int id, String content) {
        MessageClass messageClass = messageDaoSQL.findById(id).get();
        messageClass.setContent(content);
        messageClass.setIsEdited(true);
        messageDaoSQL.save(messageClass);
        return messageClass;
    } // react like carForUpdate

    public Chat getByRoomKey(String roomKey) {
        return chatDaoSQL.getByRoomKey(roomKey);
    }

}
