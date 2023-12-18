package com.example.auto_ria.services.chat;

import com.example.auto_ria.dao.socket.ChatDaoSQL;
import com.example.auto_ria.dao.socket.MessageDaoSQL;
import com.example.auto_ria.dao.user.CustomerDaoSQL;
import com.example.auto_ria.dao.user.UserDaoSQL;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.socket.Chat;
import com.example.auto_ria.models.socket.MessageClass;
import com.example.auto_ria.models.user.CustomerSQL;
import com.example.auto_ria.models.user.SellerSQL;
import com.example.auto_ria.services.CommonService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ChatServiceMySQL {

    private ChatDaoSQL chatDaoSQL;
    private MessageDaoSQL messageDaoSQL;

    private CommonService commonService;

    private UserDaoSQL sellerDaoSQL;
    private CustomerDaoSQL customerDaoSQL;

    public String createChatRoom(int customerId, int sellerId, String customerSessionId, String sellerSessionId, ERole state) {
        try {

            if (state.equals(ERole.CUSTOMER) && sellerDaoSQL.findById(sellerId).isEmpty()) {
                throw new CustomException("Receiver doesnt exist", HttpStatus.BAD_REQUEST);
            }

            System.out.println(state + " state");
            System.out.println(customerId);
            System.out.println(customerDaoSQL.findById(customerId));
            System.out.println("customerDaoSQL.findById(customerId)");

            if (state.equals(ERole.SELLER) && customerDaoSQL.findById(customerId).isEmpty()) {
                throw new CustomException("Receiver doesnt exist", HttpStatus.BAD_REQUEST);
            }

            String roomKey = getRoomKey(String.valueOf(customerId), String.valueOf(sellerId));

            chatDaoSQL.save(Chat.builder()
                    .sellerId(sellerId)
                    .customerId(customerId)
                    .sellerSessionId(sellerSessionId)
                    .customerSessionId(customerSessionId)
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
        Page<MessageClass> chat = messageDaoSQL.getByChatId(chatId, pageable);

        return chat;
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

        CustomerSQL customerSQL = commonService.extractCustomerFromHeader(request);
        SellerSQL sellerSQL = commonService.extractSellerFromHeader(request);

        if (Integer.parseInt(messageClass.getSenderId()) != customerSQL.getId() ||
                Integer.parseInt(messageClass.getSenderId()) != sellerSQL.getId()) {
            throw new CustomException("Cannot edit foreign message", HttpStatus.UNAUTHORIZED);
        }

        return messageClass;
    }

    public Page<Chat> getChatsByUserId(int id, ERole receiverRole, int page) {
        Pageable pageable = PageRequest.of(page, 2, Sort.by("updatedAt").descending());
        if (receiverRole.equals(ERole.CUSTOMER)) {
            return chatDaoSQL.getByCustomerId(id, pageable);
        } else {
            return chatDaoSQL.getBySellerId(id, pageable);
        }
    }

    public Chat getByRoomKey(String roomKey) {
        return chatDaoSQL.getByRoomKey(roomKey);
    }

}
