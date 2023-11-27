package com.example.auto_ria.configurations.socket;

import com.example.auto_ria.configurations.rabbitMq.RabbitMQProducer;
import com.example.auto_ria.dao.auth.SellerAuthDaoSQL;
import com.example.auto_ria.dao.socket.ChatDaoSQL;
import com.example.auto_ria.dao.socket.MessageDaoSQL;
import com.example.auto_ria.dao.socket.SessionDaoSQL;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.auth.AuthSQL;
import com.example.auto_ria.models.socket.Chat;
import com.example.auto_ria.models.socket.MessageClass;
import com.example.auto_ria.models.socket.Session;
import com.example.auto_ria.services.chat.ChatServiceMySQL;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.*;

//@AllArgsConstructor
public class WebSocketConnection extends TextWebSocketHandler {

    @Autowired
    private ChatDaoSQL chatDaoSQL;
    @Autowired
    private MessageDaoSQL messageDaoSQL;
    @Autowired
    private SessionDaoSQL sessionDaoSQL;
    @Autowired
    private ChatServiceMySQL chatServiceMySQL;
    @Autowired
    private RabbitMQProducer rabbitMQProducer;
    @Autowired
    private SellerAuthDaoSQL sellerAuthDaoSQL; //todo merge to one dao

    private static final Map<String, WebSocketSession> sessionMap = new HashMap<>();


    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) {
        try {
            sessionMap.put(session.getId(), session);


            String uri = Objects.requireNonNull(session.getUri()).toString();

            MultiValueMap<String, String> queryParams =
                    UriComponentsBuilder.fromUriString(uri).build().getQueryParams();

            String token = queryParams.getFirst("auth");

            AuthSQL authSQL = sellerAuthDaoSQL.findByAccessToken(token);

            if (authSQL == null) {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            }

            int sessionUserId;
            ERole sessionUserType;

            if (authSQL.getRole().equals(ERole.SELLER)) {
                sessionUserId = authSQL.getPersonId();
                sessionUserType = ERole.SELLER;

                List<Chat> chatsRequired = chatDaoSQL.getBySellerId(authSQL.getPersonId());
                for (Chat chat : chatsRequired) {
                    chat.setSellerSessionId(session.getId());
                    chatDaoSQL.save(chat);
                }

            } else if (authSQL.getRole().equals(ERole.CUSTOMER)) {
                sessionUserId = authSQL.getPersonId();
                sessionUserType = ERole.CUSTOMER;

                List<Chat> chatsRequired = chatDaoSQL.getByCustomerId(authSQL.getPersonId());
                for (Chat chat : chatsRequired) {
                    chat.setCustomerSessionId(session.getId());
                    chatDaoSQL.save(chat);
                }

            } else {
                throw new CustomException("For now chat function is available among sellers and customers only",
                        HttpStatus.UNAUTHORIZED);
            }

            sessionDaoSQL.deleteAllByUserId(authSQL.getPersonId());

            Session session1 = Session.builder()
                    .sessionId(session.getId())
                    .userId(sessionUserId)
                    .userType(sessionUserType)
                    .isOnline(true)
                    .build();

            sessionDaoSQL.save(session1);

            System.out.println("connection established" + session.getId());
        } catch (Exception e) {
            throw new CustomException("Error connecting to ws", HttpStatus.CONFLICT);
        }
    }


    @Override
    protected void handleTextMessage(@NotNull WebSocketSession session, @NotNull TextMessage message) {
        try {
            String uri = Objects.requireNonNull(session.getUri()).toString();

            MultiValueMap<String, String> queryParams =
                    UriComponentsBuilder.fromUriString(uri).build().getQueryParams();

            String token = queryParams.getFirst("auth");
            String receiverId = queryParams.getFirst("receiverId");

            if (token == null) {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            } else if (receiverId == null) {
                throw new CustomException("ReceiverId can't be null", HttpStatus.BAD_REQUEST);
            }

            AuthSQL authSQL = sellerAuthDaoSQL.findByAccessToken(token);

            if (authSQL == null) {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            }

            ERole role = authSQL.getRole();

            String checkIfPresentRoomKey;

            if (role.equals(ERole.SELLER)) {
                checkIfPresentRoomKey = chatServiceMySQL.getRoomKey(receiverId, String.valueOf(authSQL.getPersonId()));
            } else if (role.equals(ERole.CUSTOMER)) {
                checkIfPresentRoomKey = chatServiceMySQL.getRoomKey(String.valueOf(authSQL.getPersonId()), receiverId);
            } else {
                throw new CustomException("For now chat function is available among sellers and customers only",
                        HttpStatus.UNAUTHORIZED);
            }

            Chat chat = chatDaoSQL.getByRoomKey(checkIfPresentRoomKey);

            String roomKey;
            if (chat != null) {
                roomKey = checkIfPresentRoomKey;
            } else if (role.equals(ERole.CUSTOMER)) {
                roomKey = chatServiceMySQL.createChatRoom(authSQL.getPersonId(), Integer.parseInt(receiverId), session.getId(), null, role);
                chat = chatDaoSQL.getByRoomKey(roomKey);
            } else {
                roomKey = chatServiceMySQL.createChatRoom(Integer.parseInt(receiverId), authSQL.getPersonId(), null, session.getId(), role);
                chat = chatDaoSQL.getByRoomKey(roomKey);
            }

            String sellerSessionId = chat.getSellerSessionId();
            String customerSessionId = chat.getCustomerSessionId();

            MessageClass messageClass = MessageClass.builder()
                    .content(message.getPayload())
                    .chatId(chat.getId())
                    .build();


            if (role.equals(ERole.CUSTOMER)) {
                messageClass.setSenderId(String.valueOf(authSQL.getPersonId()));
                messageClass.setReceiverId(receiverId);

                rabbitMQProducer.sendMessage(message.getPayload() + " " + new Date(System.currentTimeMillis()),
                        Integer.parseInt(receiverId), authSQL.getPersonId());

            } else {
                messageClass.setSenderId(String.valueOf(authSQL.getPersonId()));
                messageClass.setReceiverId(receiverId);

                rabbitMQProducer.sendMessage(message.getPayload() + " " + new Date(System.currentTimeMillis()),
                        authSQL.getPersonId(), Integer.parseInt(receiverId));
            }

            MessageClass newMessage = messageDaoSQL.save(messageClass);
            chat.addMessage(newMessage);
            chatDaoSQL.save(chat);

            sendTextMessageIfSessionExists(sellerSessionId, message.getPayload());
            sendTextMessageIfSessionExists(customerSessionId, message.getPayload());

        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Could not send message", HttpStatus.EXPECTATION_FAILED);
        }
    }

    @SneakyThrows
    public void sendTextMessageIfSessionExists(String sessionId, String text) {
        if (sessionMap.containsKey(sessionId)) {
            sessionMap.get(sessionId).sendMessage(new TextMessage(text));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NotNull CloseStatus status) {
        System.out.println("connection closed " + session.getId());
        sessionMap.remove(session.getId());
        Session session1 = sessionDaoSQL.getBySessionId(session.getId());
//        session1.setOnline(false);
        session1.setDisconnectedAt(LocalDateTime.now());
        sessionDaoSQL.save(session1);
    }
}
