package com.randomchat.chat_backend.controller;

import com.randomchat.chat_backend.model.Message;
import com.randomchat.chat_backend.model.TypingMessage;
import com.randomchat.chat_backend.model.OnlineStatusMessage;
import com.randomchat.chat_backend.model.WebSocketEvent;
import com.randomchat.chat_backend.service.MessageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService;

    private final Map<String, Long> typingStatusMap = new ConcurrentHashMap<>();
    private final Map<String, Long> onlineStatusMap = new ConcurrentHashMap<>();

    @MessageMapping("/chat.send")
    public void handleChatMessage(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        if (userId != null) {
            message.setSenderId(userId);
        }
        // Asynchronously save the message so it doesn't block the WebSocket thread
        messageService.saveMessage(message);
        
        String topic = "/topic/room/" + message.getSenderId() + "-" + message.getConversationId();
        WebSocketEvent<Message> event = new WebSocketEvent<>("MESSAGE", message);
        messagingTemplate.convertAndSend(topic, event);
    }

    @MessageMapping("/chat.typing")
    public void handleTypingStatus(@Payload TypingMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        if (userId != null) {
            message.setSenderId(userId);
        }
        String room = message.getSenderId() + "-" + message.getConversationId();
        String topic = "/topic/room/" + room;

        if (message.isTyping()) {
            typingStatusMap.put(room, System.currentTimeMillis());
        } else {
            typingStatusMap.remove(room);
        }
        WebSocketEvent<TypingMessage> event = new WebSocketEvent<>("TYPING", message);
        messagingTemplate.convertAndSend(topic, event);
    }

    @MessageMapping("/chat.online")
    public void handleOnlineStatus(@Payload OnlineStatusMessage message,
                                   SimpMessageHeaderAccessor accessor) {

        String userId = (String) accessor.getSessionAttributes().get("userId");
        if (userId != null) {
            message.setSenderId(userId);
        }

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null && !sessionAttributes.containsKey("conversationId")) {
            sessionAttributes.put("conversationId", message.getConversationId());
        }
        String room = message.getSenderId() + "-" + message.getConversationId();
        String topic = "/topic/room/" + room;

        if (message.isOnline()) {
            onlineStatusMap.put(room, System.currentTimeMillis());
        } else {
            onlineStatusMap.remove(room);
        }
        WebSocketEvent<OnlineStatusMessage> event = new WebSocketEvent<>("ONLINE_STATUS", message);
        messagingTemplate.convertAndSend(topic, event);
    }

    @MessageMapping("/chat.user.status")
    public void sendCurrentUserStatus(@Payload OnlineStatusMessage request, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        if (userId != null) {
            request.setSenderId(userId);
        }
        String room = request.getSenderId() + "-" + request.getConversationId();
        String topic = "/topic/room/" + room;
        
        OnlineStatusMessage onlineStatus = new OnlineStatusMessage(request.getSenderId(), request.getConversationId(), onlineStatusMap.containsKey(room));
        WebSocketEvent<OnlineStatusMessage> onlineEvent = new WebSocketEvent<>("ONLINE_STATUS", onlineStatus);
        messagingTemplate.convertAndSend(topic, onlineEvent);

        TypingMessage typingStatus = new TypingMessage(request.getSenderId(), request.getConversationId(), typingStatusMap.containsKey(room));
        WebSocketEvent<TypingMessage> typingEvent = new WebSocketEvent<>("TYPING", typingStatus);
        messagingTemplate.convertAndSend(topic, typingEvent);
    }

    public void handleDisconnectCleanup(String userId, String conversationId) {
        String room =  userId + "-" + conversationId;
        if (typingStatusMap.remove(room) != null) {
            TypingMessage msg = new TypingMessage(userId, conversationId, false);
            WebSocketEvent<TypingMessage> event = new WebSocketEvent<>("TYPING", msg);
            messagingTemplate.convertAndSend("/topic/room/" + room, event);
        }
        if (onlineStatusMap.remove(room) != null) {
            OnlineStatusMessage msg = new OnlineStatusMessage(userId, conversationId, false);
            WebSocketEvent<OnlineStatusMessage> event = new WebSocketEvent<>("ONLINE_STATUS", msg);
            messagingTemplate.convertAndSend("/topic/room/" + room, event);
        }
    }
}
