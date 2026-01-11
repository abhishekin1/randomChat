package com.randomchat.chat_backend.controller;

import com.randomchat.chat_backend.dto.OnlineStatusDTO;
import com.randomchat.chat_backend.dto.TypingStatusDTO;
import com.randomchat.chat_backend.dto.WebSocketEventDTO;
import com.randomchat.chat_backend.model.Message;
import com.randomchat.chat_backend.service.MessageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

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
        WebSocketEventDTO<Message> event = new WebSocketEventDTO<>("MESSAGE", message);
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
        WebSocketEventDTO<TypingStatusDTO> event = new WebSocketEventDTO<>("TYPING", message);
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

        WebSocketEventDTO<OnlineStatusDTO> event = new WebSocketEventDTO<>("ONLINE_STATUS", message);
        messagingTemplate.convertAndSend(topic, event);
    }

    @MessageMapping("/chat.user.status")
    public void sendCurrentUserStatus(@Payload OnlineStatusDTO request, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        if (userId != null) {
            request.setSenderId(userId);
        }
        String room = request.getSenderId() + "-" + request.getConversationId();
        String topic = "/topic/room/" + room;

        boolean isOnline = isUserOnline(request.getSenderId());

        OnlineStatusDTO onlineStatus = new OnlineStatusDTO(request.getSenderId(), request.getConversationId(), isOnline);
        WebSocketEventDTO<OnlineStatusDTO> onlineEvent = new WebSocketEventDTO<>("ONLINE_STATUS", onlineStatus);
        messagingTemplate.convertAndSend(topic, onlineEvent);
    }

    public void handleDisconnectCleanup(String userId, String conversationId) {
        if (userId != null) {
            onlineUsers.remove(userId);
        }

        if (userId != null && conversationId != null) {
            String room =  userId + "-" + conversationId;
            
            OnlineStatusDTO msg = new OnlineStatusDTO(userId, conversationId, false);
            WebSocketEventDTO<OnlineStatusDTO> event = new WebSocketEventDTO<>("ONLINE_STATUS", msg);
            messagingTemplate.convertAndSend("/topic/room/" + room, event);
        }
    }

    public boolean isUserOnline(String userId) {
        return userId != null && onlineUsers.contains(userId);
    }
}
