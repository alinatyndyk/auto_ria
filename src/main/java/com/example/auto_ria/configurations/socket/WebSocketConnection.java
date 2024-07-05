package com.example.auto_ria.configurations.socket;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.auto_ria.dao.auth.UserAuthDaoSQL;
import com.example.auto_ria.dao.socket.ChatDaoSQL;
import com.example.auto_ria.dao.socket.MessageDaoSQL;
import com.example.auto_ria.dao.socket.SessionDaoSQL;
import com.example.auto_ria.dao.user.UserDaoSQL;
import com.example.auto_ria.enums.ERole;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.auth.AuthSQL;
import com.example.auto_ria.models.socket.Chat;
import com.example.auto_ria.models.socket.MessageClass;
import com.example.auto_ria.models.socket.Session;
import com.example.auto_ria.models.user.UserSQL;
import com.example.auto_ria.services.chat.ChatServiceMySQL;

import jakarta.validation.constraints.NotNull;

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
    private UserAuthDaoSQL userAuthDaoSQL;
    @Autowired
    private UserDaoSQL userDaoSQL;

    private static final Map<String, WebSocketSession> sessionMap = new HashMap<>();

    @Override
    public void afterConnectionEstablished(@SuppressWarnings("null") @NotNull WebSocketSession session) {
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

            ERole sessionUserType = authSQL.getRole();

            if (!sessionUserType.equals(ERole.USER)) {
                throw new CustomException("For now chat function is available among users only",
                        HttpStatus.UNAUTHORIZED);
            }

            String recieverId = queryParams.getFirst("receiverId");

            if (!userDaoSQL.findById(Integer.parseInt(recieverId)).isPresent()) {
                throw new CustomException("Reciever doesnt exist",
                        HttpStatus.BAD_REQUEST);
            }

            UserSQL userSQL = userDaoSQL.findById(authSQL.getPersonId()).orElse(null);

            List<Chat> chatsById = chatDaoSQL.findAll();
            chatsById.stream()
                    .filter(chat -> chat.getSessions().contains(userSQL.getSession()))
                    .peek(chat -> {
                        List<String> sessions = chat.getSessions();
                        int index = sessions.indexOf(userSQL.getSession());
                        if (index != -1) {
                            sessions.set(index, session.getId());
                        }
                    })
                    .collect(Collectors.toList());

            userSQL.setSession(session.getId());
            userDaoSQL.save(userSQL);
            int sessionUserId = authSQL.getPersonId();

            Session userSession = sessionDaoSQL.findByUserId(authSQL.getPersonId());

            if (userSession == null) {
                Session session1 = Session.builder()
                        .sessionId(session.getId())
                        .userId(sessionUserId)
                        .isOnline(true)
                        .build();

                sessionDaoSQL.save(session1);

            } else {
                userSession.setSessionId(session.getId());
                userSession.setOnline(true);
                userSession.setDisconnectedAt(null);

                sessionDaoSQL.save(userSession);
            }

        } catch (Exception e) {
            throw new CustomException("Error connecting to ws", HttpStatus.CONFLICT);
        }
    }

    @Override
    protected void handleTextMessage(@SuppressWarnings("null") @NotNull WebSocketSession session,
            @NotNull TextMessage message) {
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

            UserSQL userSQL = userDaoSQL.findById(authSQL.getPersonId()).orElse(null);
            UserSQL receiverSQL = userDaoSQL.findById(Integer.parseInt(receiverId)).orElse(null);

            Chat chat;
            String roomKey = chatServiceMySQL.getRoomKey(String.valueOf(authSQL.getPersonId()), receiverId);
            chat = chatServiceMySQL.getByRoomKey(roomKey);

            List<String> sessionList = new ArrayList<>();
            sessionList.add(userSQL.getSession());
            sessionList.add(receiverSQL.getSession());

            if (chat == null) {
                List<Integer> userList = new ArrayList<>();
                userList.add(authSQL.getPersonId());

                userList.add(Integer.parseInt(receiverId));
                Chat chatNew = Chat.builder().users(userList)
                        .sessions(sessionList)
                        .users(userList)
                        .roomKey(roomKey)
                        .build();

                chatDaoSQL.save(chatNew);
                chat = chatNew;

            } else {
                chat.setSessions(sessionList);
                chatDaoSQL.save(chat);
            }

            MessageClass messageClass = MessageClass.builder()
                    .content(message.getPayload())
                    .chatId(chat.getId())
                    .build();

            messageClass.setSenderId(String.valueOf(authSQL.getPersonId()));
            messageClass.setReceiverId(receiverId);
            messageClass.setIsSeen(false);

            MessageClass newMessage = messageDaoSQL.save(messageClass);

            chat.addMessage(newMessage);
            chatDaoSQL.save(chat);

            Set<String> usedSessions = new HashSet<>();

            for (String sess : chat.getSessions()) {
                if (!usedSessions.contains(sess)) {
                    sendTextMessageIfSessionExists(sess, message.getPayload());
                    usedSessions.add(sess);
                }
            }

        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        } catch (Exception e) {
            throw new CustomException("Could not send message", HttpStatus.EXPECTATION_FAILED);
        }
    }

    public void sendTextMessageIfSessionExists(String sessionId, String text) {
        try {
            if (sessionMap.containsKey(sessionId)) {
                sessionMap.get(sessionId).sendMessage(new TextMessage(text));
            }
        } catch (Exception e) {
            throw new CustomException(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Override
    public void afterConnectionClosed(@SuppressWarnings("null") WebSocketSession session, @NotNull CloseStatus status) {
        sessionMap.remove(session.getId());
        Session session1 = sessionDaoSQL.getBySessionId(session.getId());
        session1.setOnline(false);
        session1.setDisconnectedAt(LocalDateTime.now());
        sessionDaoSQL.save(session1);
    }
}