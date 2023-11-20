package com.example.auto_ria.configurations.socket;

import com.example.auto_ria.dao.socket.ChatDaoSQL;
import com.example.auto_ria.dao.socket.MessageDaoSQL;
import com.example.auto_ria.dao.socket.SessionDaoSQL;
import com.example.auto_ria.exceptions.CustomException;
import com.example.auto_ria.models.socket.Chat;
import com.example.auto_ria.models.socket.MessageClass;
import com.example.auto_ria.models.socket.Session;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@AllArgsConstructor
public class WebSocketConnection extends TextWebSocketHandler {

    @Autowired
    private ChatDaoSQL chatDaoSQL;
    @Autowired
    private MessageDaoSQL messageDaoSQL;
    @Autowired
    private SessionDaoSQL sessionDaoSQL;

    private static Map<String, WebSocketSession> sessionMap = new HashMap<>();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {

            sessionMap.put(session.getId(), session);

            String uri = session.getUri().toString();

            MultiValueMap<String, String> queryParams =
                    UriComponentsBuilder.fromUriString(uri).build().getQueryParams();

            String customerId = queryParams.getFirst("customer");
            String sellerId = queryParams.getFirst("seller");
            String state = queryParams.getFirst("state"); // todo transform to token

            System.out.println("connection established" + session.getId());

            Session session1 = Session.builder()
                    .sessionId(session.getId())
                    .isOnline(true)
                    .build();

            if (state.equals("customer")) { // todo remove state do tough token
                session1.setUserId(customerId);
                session1.setUserType("customer"); //from token


                List<Chat> chatsRequired = chatDaoSQL.getByCustomerId(Integer.parseInt(customerId)); //diff if a lot chats
                for (Chat chat : chatsRequired) {
                    chat.setCustomerSessionId(session.getId());
                    chatDaoSQL.save(chat);
                }

            } else if (state.equals("seller")) {
                session1.setUserId(sellerId); // from token
                session1.setUserType("seller");


                List<Chat> chatsRequired = chatDaoSQL.getBySellerId(Integer.parseInt(sellerId)); //diff if a lot chats
                for (Chat chat : chatsRequired) {
                    chat.setSellerSessionId(session.getId());
                    chatDaoSQL.save(chat);
                }
            }


            sessionDaoSQL.save(session1);
        } catch (Exception e) {
            throw new CustomException("Error connecting to ws", HttpStatus.CONFLICT);
        }
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String uri = session.getUri().toString();

            MultiValueMap<String, String> queryParams =
                    UriComponentsBuilder.fromUriString(uri).build().getQueryParams();

            String customerId = queryParams.getFirst("customer");
            String sellerId = queryParams.getFirst("seller");
            String state = queryParams.getFirst("state"); // todo transform to token
            //прийде токен та айді кому будемо писати
            //знайти токен кастомер чи селлер в базі

            //check if those 2 exist in db

            System.out.println(customerId + " " + sellerId);
            System.out.println("customer_Id + + seller_Id");

            //--------------------------------------------------- id retrieve

            String roomKey = null;
            String checkIfPresentRoomKey = getRoomKey(customerId, sellerId);
            System.out.println(checkIfPresentRoomKey + "checkIfPresentRoomKey");

            Chat chat = chatDaoSQL.getByRoomKey(checkIfPresentRoomKey);
            System.out.println(chat + "chat");

            if (chat != null) {
                System.out.println("chat present");
                roomKey = checkIfPresentRoomKey; //якшо кімната вже існує та чат вже почато в минулому
                System.out.println(chat.getId() + "    chat    " + chat.getCustomerSessionId() + chat.getSellerSessionId());

            } else if (state.equals("customer")) {
                System.out.println(110);
                roomKey = createChatRoom(customerId, sellerId, session.getId(), null, state);
                chat = chatDaoSQL.getByRoomKey(roomKey);
                System.out.println(chat);
                System.out.println("chat");
            } else if (state.equals("seller")) {
                System.out.println(114);
                roomKey = createChatRoom(customerId, sellerId, null, session.getId(), state);
                System.out.println(roomKey + "room key");
                chat = chatDaoSQL.getByRoomKey(roomKey);
                System.out.println(chat.getId() + "chat" + chat.getCustomerSessionId() + chat.getSellerSessionId());
            } else {
                throw new CustomException("Invalid params", HttpStatus.BAD_REQUEST);
            }

            //the moment user with that session appears send him all those unreached messages

            String sellerSessionId = chat.getSellerSessionId();
            String customerSessionId = chat.getCustomerSessionId();

            System.out.println(sessionDaoSQL.getBySessionId(sellerSessionId).getIsOnline());
            System.out.println(sessionDaoSQL.getBySessionId(sellerSessionId));
            System.out.println("---------------------------");
            System.out.println(sessionDaoSQL.getBySessionId(customerSessionId).getIsOnline());
            System.out.println(sessionDaoSQL.getBySessionId(customerSessionId));

            if (sellerSessionId == null) {
                throw new CustomException("Seller is not connected to chat", HttpStatus.BAD_GATEWAY);
            } else if (customerSessionId == null) { //session map blabla if someone is offline then send to only 1 session
                throw new CustomException("Customer is not connected to chat", HttpStatus.BAD_GATEWAY);
            } else if (sessionDaoSQL.getBySessionId(sellerSessionId).getIsOnline().equals(false)) {
                throw new CustomException("Seller offline", HttpStatus.BAD_GATEWAY);
            } else if (sessionDaoSQL.getBySessionId(customerSessionId).getIsOnline().equals(false)) {
                throw new CustomException("Customer offline", HttpStatus.BAD_GATEWAY);
            }  //if so, collect unread messages

            MessageClass messageClass = MessageClass.builder()
                    .content(message.getPayload()) //todo set chat!!
                    .chatId(chat.getId())
                    .build();

            System.out.println(messageClass + " messageClass");

            if (state.equals("customer")) {
                messageClass.setSenderId(customerId);
                messageClass.setReceiverId(sellerId);
            } else if (state.equals("seller")) {
                messageClass.setSenderId(sellerId); // in which session was message sent
                messageClass.setReceiverId(customerId);
            }

            System.out.println(messageClass + " messageClass1");

            MessageClass newMessage = messageDaoSQL.save(messageClass);
            chat.addMessage(newMessage);

            System.out.println("after message save");
            System.out.println(sessionMap.get(sellerSessionId)); //no session fix
            System.out.println(sessionMap.get(customerSessionId));

            try {
                sessionMap.get(sellerSessionId).sendMessage(new TextMessage(message.getPayload() + " " + new Date(System.currentTimeMillis())));

                sessionMap.get(customerSessionId).sendMessage(new TextMessage(message.getPayload() + " " + new Date(System.currentTimeMillis())));

            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            } //todo SessionConnectException

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //todo get chat by id
    // todo get msg by id, delete, update
    //todo seen message function
    // TODO rabbitMq storage + permanent!!!

    private String createChatRoom(String customerId, String sellerId, String customerSessionId, String sellerSessionId, String state) {
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

    private String getRoomKey(String customerId, String sellerId) {
        return customerId + "-" + sellerId;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        System.out.println("connection closed " + session.getId());
        sessionMap.remove(session.getId());
        Session session1 = sessionDaoSQL.getBySessionId(session.getId());
        session1.setOnline(false);
        session1.setDisconnectedAt(LocalDateTime.now());
        sessionDaoSQL.save(session1);
//        super.afterConnectionClosed(session, status);
    }
}
