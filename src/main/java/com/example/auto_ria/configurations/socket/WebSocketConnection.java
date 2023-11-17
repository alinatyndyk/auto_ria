package com.example.auto_ria.configurations.socket;

import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;

public class WebSocketConnection extends TextWebSocketHandler {

    private Map<String, WebSocketSession> sessionMap = new HashMap<>();
    private Map<String, Object> userToRoomMap = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        // todo inform if 2 entered chat
        System.out.println("connection established" + session.getId());
        sessionMap.put(session.getId(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        // Extract customer and seller IDs from the message or session attributes
//        String customerId = ""; // Extract customer ID
//        String sellerId = ""; // Extract seller ID

        String uri = session.getUri().toString();

        MultiValueMap<String, String> queryParams =
                UriComponentsBuilder.fromUriString(uri).build().getQueryParams();

        String customerId = queryParams.getFirst("customer");
        String sellerId = queryParams.getFirst("seller");
        String state = queryParams.getFirst("state");

        System.out.println(customerId + " " + sellerId);
        System.out.println("customer_Id + + seller_Id");

        //--------------------------------------------------- id retrieve

        String roomKey = null;
        String checkIfPresentRoomKey = getRoomKey(customerId, sellerId);


        if (userToRoomMap.containsKey(checkIfPresentRoomKey)) {
            roomKey = checkIfPresentRoomKey;

            ArrayList<String> ids = (ArrayList<String>) userToRoomMap.get(roomKey);
            ids.add(session.getId());

        } else {
            // if room exists set sessionId to room
//            roomKey = createChatRoom(customerId, sellerId, session.getId());

            //if its customer set 3 if seller - 4 parameter
            if (state.equals("customer")) {
                roomKey = createChatRoom(customerId, sellerId, session.getId(), null);
            } else if (state.equals("seller")) {
                roomKey = createChatRoom(customerId, sellerId, null, session.getId());
            }
        }

        System.out.println(roomKey);
        System.out.println("roomKey");

        System.out.println(userToRoomMap.get(roomKey));
        System.out.println("userToRoomMap.get(roomKey)");

        System.out.println(userToRoomMap.containsKey(roomKey));
        System.out.println("userToRoomMap.containsKey(roomKey)");

//        WebSocketSession targetSession = sessionMap.get(userToRoomMap.get(roomKey));

        List<String> sessionIds = (ArrayList<String>) userToRoomMap.get(roomKey);
        for (String sessionId : sessionIds) {
            WebSocketSession targetSession = sessionMap.get(sessionId);
            if (targetSession != null) {
                System.out.println(targetSession);
                System.out.println("targetSession");
                try {
                    targetSession.sendMessage(new TextMessage(message.getPayload() + " " + new Date(System.currentTimeMillis())));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private String createChatRoom(String customerId, String sellerId, String customerSessionId, String sellerSessionId) {
        String roomKey = getRoomKey(customerId, sellerId);

        List<String> sessionIds = new ArrayList<>();
        sessionIds.add(customerSessionId);
        sessionIds.add(sellerSessionId);
        userToRoomMap.put(roomKey, sessionIds); // Add the room to the userToRoomMap
        return roomKey;
    }

    private String getRoomKey(String customerId, String sellerId) {
        return customerId + "-" + sellerId;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("connection established" + session.getId());
        sessionMap.remove(session.getId());
//        super.afterConnectionClosed(session, status);
    }
}
