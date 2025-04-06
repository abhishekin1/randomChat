package com.randomchat.chat_backend.controller;

import com.randomchat.chat_backend.model.Message;
import com.randomchat.chat_backend.model.TypingMessage;
import com.randomchat.chat_backend.model.OnlineStatusMessage;
import com.randomchat.chat_backend.service.MessageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
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

    private final Map<String, String> sessionTypingRoomMap = new ConcurrentHashMap<>();
    private final Map<String, String> sessionOnlineRoomMap = new ConcurrentHashMap<>();

    @MessageMapping("/chat.send")
    public void handleChatMessage(@Payload Message message) {
        System.out.println("-----------------------i am inside websocket");
        messageService.saveMessage(message);
        String topic = "/topic/room/" + message.getSenderId() + "-" + message.getConversationId();
        messagingTemplate.convertAndSend(topic, message);
    }

    @MessageMapping("/chat.typing")
    public void handleTypingStatus(@Payload TypingMessage message, @Header("simpSessionId") String sessionId) {
        String room = message.getSenderId() + "-" + message.getConversationId();
        String topic = "/topic/room/" + room;

        if (message.isTyping()) {
            typingStatusMap.put(room, System.currentTimeMillis());
            sessionTypingRoomMap.put(sessionId, room);
        } else {
            typingStatusMap.remove(room);
            sessionTypingRoomMap.remove(sessionId);
        }

        messagingTemplate.convertAndSend(topic, message);
    }

    @MessageMapping("/chat.typing.status")
    public void sendCurrentTypingStatus(@Payload TypingMessage request) {
        String room = request.getSenderId() + "-" + request.getConversationId();
        String topic = "/topic/room/" + room;

        if (typingStatusMap.containsKey(room)) {
            TypingMessage current = new TypingMessage(request.getSenderId(), request.getConversationId(), true);
            messagingTemplate.convertAndSend(topic, current);
        }
    }

    @MessageMapping("/chat.online")
    public void handleOnlineStatus(@Payload OnlineStatusMessage message, @Header("simpSessionId") String sessionId) {
        String room = message.getSenderId() + "-" + message.getConversationId();
        String topic = "/topic/room/" + room;

        if (message.isOnline()) {
            onlineStatusMap.put(room, System.currentTimeMillis());
            sessionOnlineRoomMap.put(sessionId, room);
        } else {
            onlineStatusMap.remove(room);
            sessionOnlineRoomMap.remove(sessionId);
        }

        messagingTemplate.convertAndSend(topic, message);
    }

    @MessageMapping("/chat.online.status")
    public void sendCurrentOnlineStatus(@Payload OnlineStatusMessage request) {
        String room = request.getSenderId() + "-" + request.getConversationId();
        String topic = "/topic/room/" + room;

        if (onlineStatusMap.containsKey(room)) {
            OnlineStatusMessage current = new OnlineStatusMessage(request.getSenderId(), request.getConversationId(), true);
            messagingTemplate.convertAndSend(topic, current);
        }
    }

    public void handleDisconnectCleanup(String sessionId) {
        String typingRoom = sessionTypingRoomMap.remove(sessionId);
        if (typingRoom != null) {
            typingStatusMap.remove(typingRoom);
            String[] parts = typingRoom.split("-");
            TypingMessage msg = new TypingMessage(parts[0], parts[1], false);
            messagingTemplate.convertAndSend("/topic/room/" + typingRoom, msg);
        }

        String onlineRoom = sessionOnlineRoomMap.remove(sessionId);
        if (onlineRoom != null) {
            onlineStatusMap.remove(onlineRoom);
            String[] parts = onlineRoom.split("-");
            OnlineStatusMessage msg = new OnlineStatusMessage(parts[0], parts[1], false);
            messagingTemplate.convertAndSend("/topic/room/" + onlineRoom, msg);
        }
    }
}
