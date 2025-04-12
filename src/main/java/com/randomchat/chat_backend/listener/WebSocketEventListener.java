package com.randomchat.chat_backend.listener;

import com.randomchat.chat_backend.controller.ChatWebSocketController;
import com.randomchat.chat_backend.controller.RandomChatController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    @Autowired
    private ChatWebSocketController chatWebSocketController;
    @Autowired
    private RandomChatController randomChatController;

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String conversationId = (String) headerAccessor.getSessionAttributes().get("conversationId");
        if (userId != null) {
            chatWebSocketController.handleDisconnectCleanup(userId, conversationId);
            randomChatController.handleDisconnectCleanup(userId);
            System.out.println("User disconnected and removed: " + userId);
        }
        System.out.println("Disconnected user: " + userId);

    }
}
