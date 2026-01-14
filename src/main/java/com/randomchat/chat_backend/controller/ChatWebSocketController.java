package com.randomchat.chat_backend.controller;

import com.randomchat.chat_backend.Enums;
import com.randomchat.chat_backend.dto.OnlineStatusDTO;
import com.randomchat.chat_backend.dto.TypingStatusDTO;
import com.randomchat.chat_backend.dto.WebSocketEventDTO;
import com.randomchat.chat_backend.model.Message;
import com.randomchat.chat_backend.service.MessageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService;

    // Use a Set to track online users globally.
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    @MessageMapping("/chat.send")
    public void handleChatMessage(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        if (userId != null) {
            message.setSenderId(userId);
        }
        // Asynchronously save the message
        messageService.saveMessage(message);
        
        String topic = "/topic/room/" + message.getSenderId() + "-" + message.getConversationId();
        WebSocketEventDTO<Message> event = new WebSocketEventDTO<>(Enums.WebSocketEventType.MESSAGE, message);
        messagingTemplate.convertAndSend(topic, event);
    }

    @MessageMapping("/chat.typing")
    public void handleTypingStatus(@Payload TypingStatusDTO message, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        if (userId != null) {
            message.setSenderId(userId);
        }
        String room = message.getSenderId() + "-" + message.getConversationId();
        String topic = "/topic/room/" + room;

        // Fire-and-forget
        WebSocketEventDTO<TypingStatusDTO> event = new WebSocketEventDTO<>(Enums.WebSocketEventType.TYPING, message);
        messagingTemplate.convertAndSend(topic, event);
    }

    @MessageMapping("/chat.online")
    public void handleOnlineStatus(@Payload OnlineStatusDTO message,
                                   SimpMessageHeaderAccessor accessor) {

        String userId = (String) accessor.getSessionAttributes().get("userId");
        if (userId != null) {
            message.setSenderId(userId);
            onlineUsers.add(userId);
        }

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null && !sessionAttributes.containsKey("conversationId")) {
            sessionAttributes.put("conversationId", message.getConversationId());
        }
        
        String room = message.getSenderId() + "-" + message.getConversationId();
        String topic = "/topic/room/" + room;

        WebSocketEventDTO<OnlineStatusDTO> event = new WebSocketEventDTO<>(Enums.WebSocketEventType.ONLINE_STATUS, message);
        messagingTemplate.convertAndSend(topic, event);
    }

    /**
     * Handles the SessionSubscribeEvent to solve a race condition where the client
     * subscribes to a topic AFTER the initial status message has already been sent.
     * 
     * By listening for the subscription event, we ensure that the current online status
     * is sent to the subscriber immediately, regardless of when the subscription occurs relative
     * to other events.
     */
    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();

        // Check if the subscription is for a room topic
        if (destination != null && destination.startsWith("/topic/room/")) {
            String room = destination.substring("/topic/room/".length());
            
            // Extract senderId and conversationId from "senderId-conversationId"
            // We split by the last hyphen because senderId (UUID) might contain hyphens
            int lastHyphenIndex = room.lastIndexOf('-');
            if (lastHyphenIndex != -1) {
                String senderId = room.substring(0, lastHyphenIndex);
                String conversationIdStr = room.substring(lastHyphenIndex + 1);

                try {

                    boolean isOnline = isUserOnline(senderId);

                    OnlineStatusDTO onlineStatus = new OnlineStatusDTO(senderId, conversationIdStr, isOnline);
                    WebSocketEventDTO<OnlineStatusDTO> onlineEvent = new WebSocketEventDTO<>(Enums.WebSocketEventType.ONLINE_STATUS, onlineStatus);
                    
                    // Send the status to the topic so the subscriber receives it immediately
                    messagingTemplate.convertAndSend(destination, onlineEvent);
                } catch (NumberFormatException e) {
                    // Ignore invalid paths
                }
            }
        }
    }

    public void handleDisconnectCleanup(String userId, String conversationId) {
        if (userId != null) {
            onlineUsers.remove(userId);
        }

        if (userId != null && conversationId != null) {
            String room =  userId + "-" + conversationId;
            
            OnlineStatusDTO msg = new OnlineStatusDTO(userId, conversationId, false);
            WebSocketEventDTO<OnlineStatusDTO> event = new WebSocketEventDTO<>(Enums.WebSocketEventType.ONLINE_STATUS, msg);
            messagingTemplate.convertAndSend("/topic/room/" + room, event);
        }
    }

    public boolean isUserOnline(String userId) {
        return userId != null && onlineUsers.contains(userId);
    }
}
