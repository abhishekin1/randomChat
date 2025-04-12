package com.randomchat.chat_backend.controller;

import com.randomchat.chat_backend.model.Message;
import com.randomchat.chat_backend.model.TypingMessage;
import com.randomchat.chat_backend.model.OnlineStatusMessage;
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
    public void handleChatMessage(@Payload Message message) {
        System.out.println("-----------------------i am inside websocket /chat.send");
        messageService.saveMessage(message);
        String topic = "/topic/room/" + message.getSenderId() + "-" + message.getConversationId();
        messagingTemplate.convertAndSend(topic, message);
    }

    @MessageMapping("/chat.typing")
    public void handleTypingStatus(@Payload TypingMessage message) {
        String room = message.getSenderId() + "-" + message.getConversationId();
        String topic = "/topic/room/" + room;

        if (message.isTyping()) {
            typingStatusMap.put(room, System.currentTimeMillis());
        } else {
            typingStatusMap.remove(room);
        }
        messagingTemplate.convertAndSend(topic, message);
    }

    @MessageMapping("/chat.online")
    public void handleOnlineStatus(@Payload OnlineStatusMessage message,
                                   org.springframework.messaging.Message<?> rawMessage) {

        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(rawMessage);

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
        messagingTemplate.convertAndSend(topic, message);
    }

    @MessageMapping("/chat.user.status")
    public void sendCurrentUserStatus(@Payload OnlineStatusMessage request) {
        String room = request.getSenderId() + "-" + request.getConversationId();
        String topic = "/topic/room/" + room;
        OnlineStatusMessage onlineStatus = new OnlineStatusMessage(request.getSenderId(), request.getConversationId(), onlineStatusMap.containsKey(room));
        TypingMessage typingStatus = new TypingMessage(request.getSenderId(), request.getConversationId(), typingStatusMap.containsKey(room));
        messagingTemplate.convertAndSend(topic, onlineStatus);
        messagingTemplate.convertAndSend(topic, typingStatus);
    }

    public void handleDisconnectCleanup(String userId, String conversationId) {
        String room =  userId + "-" + conversationId;
        if (typingStatusMap.remove(room) != null) {
            TypingMessage msg = new TypingMessage(userId, conversationId, false);
            messagingTemplate.convertAndSend("/topic/room/" + room, msg);
        }
        if (onlineStatusMap.remove(room) != null) {
            OnlineStatusMessage msg = new OnlineStatusMessage(userId, conversationId, false);
            messagingTemplate.convertAndSend("/topic/room/" + room, msg);
        }
        System.out.println("-------------------🔌 WebSocket disconnect: userId=" + userId + ", conversationId=" + conversationId);
        System.out.print("-------------------typingStatusMap size: " + typingStatusMap.size());
        System.out.println("  onlineStatusMap size: " + onlineStatusMap.size());
    }
}
