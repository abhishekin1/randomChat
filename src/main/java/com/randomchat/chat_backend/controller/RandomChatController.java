package com.randomchat.chat_backend.controller;

import com.randomchat.chat_backend.Enums;
import com.randomchat.chat_backend.dto.UserConversationDTO;
import com.randomchat.chat_backend.dto.WebSocketEventDTO;
import com.randomchat.chat_backend.model.Message;
import com.randomchat.chat_backend.model.User;
import com.randomchat.chat_backend.service.FriendshipService;
import com.randomchat.chat_backend.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Controller
@Slf4j
@RequiredArgsConstructor
public class RandomChatController {

    @Autowired
    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    private final UserService userService;
    @Autowired
    private final FriendshipService friendshipService;

    // AtomicReference holds the single user waiting for a match.
    // This guarantees thread safety without blocking locks.
    private final AtomicReference<String> waitingUser = new AtomicReference<>();

    @MessageMapping("/chat.random")
    public void randomChat(SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        if (userId == null) return;
        
        String friendUserId;
        
        // Loop to handle race conditions (CAS failures)
        while (true) {
            friendUserId = waitingUser.get();
            
            if (friendUserId == null) {
                // Queue is empty, try to add self
                if (waitingUser.compareAndSet(null, userId)) {
                    return; // Successfully added to wait list
                }
                // Failed (someone else added themselves), loop to try matching
            } else {
                if (friendUserId.equals(userId)) {
                    return; // Already waiting
                }
                
                // Try to match with the waiting user
                if (waitingUser.compareAndSet(friendUserId, null)) {
                    break; // Match secured!
                }
                // Failed (someone else took them or they left), loop to retry
            }
        }

        // Found a match (friendUserId)
        User user = userService.getUserByDeviceId(userId).orElse(null);
        User friend = userService.getUserByDeviceId(friendUserId).orElse(null);

        if (user == null || friend == null) {
            return;
        }

        Long conversationId = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;

        UserConversationDTO toUser = new UserConversationDTO(
                conversationId,
                friend.getName(),
                friend.getUsername(),
                friend.getPhotoUrl()
        );
        UserConversationDTO toFriend = new UserConversationDTO(
                conversationId,
                user.getName(),
                user.getUsername(),
                user.getPhotoUrl()
        );

        WebSocketEventDTO<UserConversationDTO> eventToUser = new WebSocketEventDTO<>(Enums.WebSocketEventType.CONVERSATION_DTO, toUser);
        WebSocketEventDTO<UserConversationDTO> eventToFriend = new WebSocketEventDTO<>(Enums.WebSocketEventType.CONVERSATION_DTO, toFriend);

        messagingTemplate.convertAndSend("/topic/room/random/" + userId, eventToUser);
        messagingTemplate.convertAndSend("/topic/room/random/" + friendUserId, eventToFriend);
    }

    @MessageMapping("/chat.random.send")
    public void handleChatMessage(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");

        if (userId != null) {
            message.setSenderId(userId);
        }
        message.setTimeStamp(LocalDateTime.now());
        log.info("handleChatMessage: {}", message);
        String topic = "/topic/room/" + message.getSenderId() + "-" + message.getConversationId();
        WebSocketEventDTO<Message> event = new WebSocketEventDTO<>(Enums.WebSocketEventType.MESSAGE, message);
        messagingTemplate.convertAndSend(topic, event);
    }

    public void handleDisconnectCleanup(String userId) {
        // If the user was waiting, remove them. Atomic operation.
        waitingUser.compareAndSet(userId, null);
        messagingTemplate.convertAndSend("/topic/room/random/disconnect_watch/"+userId, true);
    }
}
