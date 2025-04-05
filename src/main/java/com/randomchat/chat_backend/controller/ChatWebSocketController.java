package com.randomchat.chat_backend.controller;

import com.randomchat.chat_backend.model.Message;
import com.randomchat.chat_backend.model.OnlineStatusMessage;
import com.randomchat.chat_backend.model.TypingMessage;
import com.randomchat.chat_backend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService;
    // Maps to track typing and online statuses with their last activity timestamps
    private final ConcurrentHashMap<String, Long> typingStatusMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> onlineStatusMap = new ConcurrentHashMap<>();

    // TTL values in milliseconds
    private static final long TYPING_TTL = 7000;  // 7 seconds
    private static final long ONLINE_TTL = 30000; // 30 seconds

    @MessageMapping("/chat.send")
    public void handleChatMessage(@Payload Message message) {
        System.out.println("-----------------------i am inside websocket");
        messageService.saveMessage(message);
        String topic = "/topic/room/" + message.getSenderId() + "-" + message.getConversationId(); // e.g., /topic/room/abc123
        messagingTemplate.convertAndSend(topic, message);
    }

    @MessageMapping("/chat.typing")
    public void handleTypingStatus(@Payload TypingMessage message) {
        String room = message.getSenderId() + "-" + message.getConversationId();
        if (message.isTyping()) {
            typingStatusMap.put(room, System.currentTimeMillis());
        } else {
            typingStatusMap.remove(room);
        }
        messagingTemplate.convertAndSend("/topic/room/" + room, message);
    }

    @MessageMapping("/chat.typing.status")
    public void sendCurrentTypingStatus(@Payload TypingMessage request) {
        String room = request.getSenderId() + "-" + request.getConversationId();
        Long lastTyped = typingStatusMap.get(room);
        if (lastTyped != null && System.currentTimeMillis() - lastTyped <= TYPING_TTL) {
            TypingMessage msg = new TypingMessage(request.getSenderId(), request.getConversationId(), true);
            messagingTemplate.convertAndSend("/topic/room/" + room, msg);
        }
    }


    @MessageMapping("/chat.online")
    public void handleOnlineStatus(@Payload OnlineStatusMessage message) {
        String room = message.getSenderId() + "-" + message.getConversationId();
        if (message.isOnline()) {
            onlineStatusMap.put(room, System.currentTimeMillis());
        } else {
            onlineStatusMap.remove(room);
        }
        messagingTemplate.convertAndSend("/topic/room/" + room, message);
    }

    @MessageMapping("/chat.online.status")
    public void sendCurrentOnlineStatus(@Payload OnlineStatusMessage request) {
        String room = request.getSenderId() + "-" + request.getConversationId();
        Long lastSeen = onlineStatusMap.get(room);
        if (lastSeen != null && System.currentTimeMillis() - lastSeen <= ONLINE_TTL) {
            OnlineStatusMessage msg = new OnlineStatusMessage(request.getSenderId(), request.getConversationId(), true);
            messagingTemplate.convertAndSend("/topic/room/" + room, msg);
        }
    }

    @Scheduled(fixedRate = 10000) // every 10s
    public void cleanUpTypingStatus() {
        long now = System.currentTimeMillis();
        typingStatusMap.forEach((room, timestamp) -> {
            if (now - timestamp > TYPING_TTL) {
                typingStatusMap.remove(room);
                TypingMessage msg = new TypingMessage();
                String[] parts = room.split("-");
                msg.setSenderId(parts[0]);
                msg.setConversationId(parts[1]);
                msg.setTyping(false);
                messagingTemplate.convertAndSend("/topic/room/" + room, msg);
            }
        });
    }

    @Scheduled(fixedRate = 10000) // every 10s
    public void cleanUpOnlineStatus() {
        long now = System.currentTimeMillis();
        onlineStatusMap.forEach((room, timestamp) -> {
            if (now - timestamp > ONLINE_TTL) {
                onlineStatusMap.remove(room);
                OnlineStatusMessage msg = new OnlineStatusMessage();
                String[] parts = room.split("-");
                msg.setSenderId(parts[0]);
                msg.setConversationId(parts[1]);
                msg.setOnline(false);
                messagingTemplate.convertAndSend("/topic/room/" + room, msg);
            }
        });
    }

}
