package com.example.auto_ria.configurations.socket;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.assertj.core.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.auto_ria.configurations.rabbitMq.RabbitMQProducer;
import com.example.auto_ria.dao.auth.UserAuthDaoSQL;
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
    private UserAuthDaoSQL userAuthDaoSQL;

    private static final Map<String, WebSocketSession> sessionMap = new HashMap<>();

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) {
        try {
            sessionMap.put(session.getId(), session);

            String uri = Objects.requireNonNull(session.getUri()).toString();

            MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUriString(uri).build()
                    .getQueryParams();

            String token = queryParams.getFirst("auth");

            AuthSQL authSQL = userAuthDaoSQL.findByAccessToken(token);

            if (authSQL == null) {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            }

            int sessionUserId = authSQL.getPersonId();
            ERole sessionUserType = authSQL.getRole();

            if (!sessionUserType.equals(ERole.USER)) {
                throw new CustomException("For now chat function is available among users only",
                        HttpStatus.UNAUTHORIZED);
            }

            // List<Chat> chatsRequired = chatDaoSQL.getByUserId(authSQL.getPersonId());
            // for (Chat chat : chatsRequired) {
            // chat.setSellerSessionId(session.getId());
            // chatDaoSQL.save(chat); //тут походу чтобі показівать когда впослндний раз
            // человек был онлайн но это можно сделать только 1 раз для профиля а не каждого
            // чата

            // int sessionUserId;
            // ERole sessionUserType;

            // if (authSQL.getRole().equals(ERole.USER)) {
            // sessionUserId = authSQL.getPersonId();
            // sessionUserType = ERole.USER;

            // List<Chat> chatsRequired = chatDaoSQL.getBySellerId(authSQL.getPersonId());
            // for (Chat chat : chatsRequired) {
            // chat.setSellerSessionId(session.getId());
            // chatDaoSQL.save(chat);
            // }

            // } else if (authSQL.getRole().equals(ERole.CUSTOMER)) {
            // sessionUserId = authSQL.getPersonId();
            // sessionUserType = ERole.CUSTOMER;

            // List<Chat> chatsRequired = chatDaoSQL.getByCustomerId(authSQL.getPersonId());
            // for (Chat chat : chatsRequired) {
            // chat.setCustomerSessionId(session.getId());
            // chatDaoSQL.save(chat);
            // }

            // System.out.println(sessionUserType);
            // } else {
            // // проверить не смотрит ли все аус
            // throw new CustomException("For now chat function is available among users
            // only",
            // HttpStatus.UNAUTHORIZED);
            // }

            Session userSession = sessionDaoSQL.findByUserId(authSQL.getPersonId());

            if (userSession == null) {
                Session session1 = Session.builder()
                        .sessionId(session.getId())
                        .userId(sessionUserId)
                        .isOnline(true) // fix
                        .build();

                sessionDaoSQL.save(session1);
            } else {
                userSession.setSessionId(session.getId());
                userSession.setOnline(true);
                userSession.setDisconnectedAt(null);
                sessionDaoSQL.save(userSession);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new CustomException("Error connecting to ws", HttpStatus.CONFLICT);
        }
    }

    @Override
    protected void handleTextMessage(@NotNull WebSocketSession session, @NotNull TextMessage message) {
        try {
            String uri = Objects.requireNonNull(session.getUri()).toString();

            MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUriString(uri).build()
                    .getQueryParams();

            String token = queryParams.getFirst("auth");
            String receiverId = queryParams.getFirst("receiverId");

            if (token == null) {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            } else if (receiverId == null) {
                throw new CustomException("ReceiverId can't be null", HttpStatus.BAD_REQUEST);
            }

            AuthSQL authSQL = userAuthDaoSQL.findByAccessToken(token);

            if (authSQL == null) {
                throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
            }

            // String checkIfPresentRoomKey;

            // if (role.equals(ERole.SELLER)) {
            // checkIfPresentRoomKey = chatServiceMySQL.getRoomKey(receiverId,
            // String.valueOf(authSQL.getPersonId()));
            // } else if (role.equals(ERole.CUSTOMER)) {
            // checkIfPresentRoomKey =
            // chatServiceMySQL.getRoomKey(String.valueOf(authSQL.getPersonId()),
            // receiverId);
            // } else {
            // throw new CustomException("For now chat function is available among users
            // only",
            // HttpStatus.UNAUTHORIZED);
            // }

            // Chat chat = chatDaoSQL.getByRoomKey(checkIfPresentRoomKey);

            // вместо этого найти комнату по одному из юзеров

            Chat chat;

            chat = chatServiceMySQL.getChatByUserIdFilterReciever(authSQL.getPersonId(),
                    Integer.valueOf(receiverId)); // находим все чаты сендера и фильтром берем только с ресиверайди
            System.out.println("chat-------------" + chat);

            if (chat == null) {

                List<Integer> userList = new ArrayList<>();
                userList.add(authSQL.getPersonId());
                userList.add(Integer.parseInt(receiverId));

                List<String> sessionList = new ArrayList<>();
                sessionList.add(session.getId()); // второго пока еще нет

                chat = Chat.builder().users(userList)
                        .sessions(sessionList)
                        .build();

                chatServiceMySQL.save(chat);
            }

            // String roomKey;
            // if (chat != null) {
            // roomKey = chat.getRoomKey();
            // } else if (role.equals(ERole.CUSTOMER)) {
            // roomKey = chatServiceMySQL.createChatRoom(authSQL.getPersonId(),
            // Integer.parseInt(receiverId),
            // session.getId(), null, role);
            // // chat = chatDaoSQL.getByRoomKey(roomKey);
            // } else {
            // // roomKey = chatServiceMySQL.createChatRoom(Integer.parseInt(receiverId),
            // authSQL.getPersonId(), null,
            // // session.getId(), role);
            // // chat = chatDaoSQL.getByRoomKey(roomKey);
            // }

            // String user1SessionId = chat.getUser1SessionId();
            // String user2SessionId = chat.getUser2SessionId(); //!sessions from array

            MessageClass messageClass = MessageClass.builder()
                    .content(message.getPayload())
                    .chatId(chat.getId())
                    .build();

            // if (role.equals(ERole.CUSTOMER)) {
            messageClass.setSenderId(String.valueOf(authSQL.getPersonId()));
            messageClass.setReceiverId(receiverId);
            messageClass.setIsSeen(false);

            rabbitMQProducer.sendMessage(message.getPayload() + " " + new Date(System.currentTimeMillis()),
                    Integer.parseInt(receiverId), authSQL.getPersonId());

            MessageClass newMessage = messageDaoSQL.save(messageClass);
            chat.addMessage(newMessage);
            // chat.setNotSeenSeller(chat.getNotSeenSeller() + 1); //FIX NOT SEEN

            // } else {
            // messageClass.setSenderId(String.valueOf(authSQL.getPersonId()));
            // messageClass.setReceiverId(receiverId);
            // messageClass.setIsSeen(false);

            // rabbitMQProducer.sendMessage(message.getPayload() + " " + new
            // Date(System.currentTimeMillis()),
            // authSQL.getPersonId(), Integer.parseInt(receiverId));

            // MessageClass newMessage = messageDaoSQL.save(messageClass);
            // chat.addMessage(newMessage);
            // chat.setNotSeenCustomer(chat.getNotSeenCustomer() + 1);
            // }

            chatDaoSQL.save(chat);

            for (String sess : chat.getSessions()) {
                sendTextMessageIfSessionExists(sess, message.getPayload() + " " + messageClass.getIsSeen());
            }

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
        session1.setOnline(false);
        session1.setDisconnectedAt(LocalDateTime.now());
        sessionDaoSQL.save(session1);
    }
}
