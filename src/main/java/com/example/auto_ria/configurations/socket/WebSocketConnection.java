package com.example.auto_ria.configurations.socket;

import com.example.auto_ria.dao.socket.ChatDaoSQL;
import com.example.auto_ria.dao.socket.MessageDaoSQL;
import com.example.auto_ria.dao.socket.SessionDaoSQL;
import com.example.auto_ria.models.socket.Chat;
import com.example.auto_ria.models.socket.MessageClass;
import com.example.auto_ria.models.socket.Session;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;

public class WebSocketConnection extends TextWebSocketHandler {

    private ChatDaoSQL chatDaoSQL;
    private MessageDaoSQL messageDaoSQL;
    private SessionDaoSQL sessionDaoSQL;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

//        String uri = session.getUri().toString();
//
//        MultiValueMap<String, String> queryParams =
//                UriComponentsBuilder.fromUriString(uri).build().getQueryParams();
//
//        String customerId = queryParams.getFirst("customer");
//        String sellerId = queryParams.getFirst("seller");
//        String state = queryParams.getFirst("state"); // todo transform to token

        System.out.println("connection established" + session.getId());

//        Session session1 = Session.builder()
//                .sessionId(session.getId())
//                .build();
//
//        if (state.equals("customer")) { // todo remove state do tough token
//            session1.setUserId(customerId);
//        } else if (state.equals("seller")) {
//            session1.setUserId(sellerId);
//        }
//
//        sessionDaoSQL.save(session1);

//        sessionMap.put(session.getId(), session);
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String uri = session.getUri().toString();

        MultiValueMap<String, String> queryParams =
                UriComponentsBuilder.fromUriString(uri).build().getQueryParams();

        String customerId = queryParams.getFirst("customer");
        String sellerId = queryParams.getFirst("seller");
        String state = queryParams.getFirst("state"); // todo transform to token
        //прийде токен та айді кому будемо писати
        //знайти токен кастомер чи селлер в базі

        System.out.println(customerId + " " + sellerId);
        System.out.println("customer_Id + + seller_Id");

        //--------------------------------------------------- id retrieve

        String roomKey = null;
        String checkIfPresentRoomKey = getRoomKey(customerId, sellerId);


        if (chatDaoSQL.getByRoomKey(checkIfPresentRoomKey) != null) {
            roomKey = checkIfPresentRoomKey; //якшо кімната вже існує та чат вже почато в минулому

        } else {
            //todo to listen all the time
            if (state.equals("customer")) { //todo enum
                roomKey = createChatRoom(customerId, sellerId, session.getId(), null, state);
            } else if (state.equals("seller")) {
                roomKey = createChatRoom(customerId, sellerId, null, session.getId(), state);
            }
        }
//
//        List<String> sessionIds = (ArrayList<String>) userToRoomMap.get(roomKey);
//        for (String sessionId : sessionIds) {
//            WebSocketSession targetSession = sessionMap.get(sessionId);
//            if (targetSession != null) {
//
//                MessageClass messageClass = MessageClass.builder()
//                        .content(message.getPayload()) //todo set chat!!
//                        .build();
//
//                if (state.equals("customer")) {
//                    messageClass.setSenderId(customerId);
//                    messageClass.setReceiverId(sellerId);
//                } else if (state.equals("seller")) {
//                    messageClass.setSenderId(sellerId);
//                    messageClass.setReceiverId(customerId);
//                }
//
//                messageDaoSQL.save(messageClass);
//
//                System.out.println(targetSession);
//                System.out.println("targetSession");
//        sessionMap.forEach((s, session1) -> {
//            try {
//                session1.sendMessage(new TextMessage(message.getPayload() + " " + new Date(System.currentTimeMillis())));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//        });
//            }
//        }
    }

    private String createChatRoom(String customerId, String sellerId, String customerSessionId, String sellerSessionId, String state) {
        String roomKey = getRoomKey(customerId, sellerId);

        chatDaoSQL.save(Chat.builder() // todo check if ids exist in db
                .sellerId(Integer.parseInt(sellerId))
                .customerId(Integer.parseInt(customerId))
                //sessions
                .sellerSessionId(sellerSessionId)
                .customerSessionId(customerSessionId)
                .build());

        return roomKey;
    }

    private String getRoomKey(String customerId, String sellerId) {
        return customerId + "-" + sellerId;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("connection closed" + session.getId());
//        sessionMap.remove(session.getId());
//        super.afterConnectionClosed(session, status);
    }
}
