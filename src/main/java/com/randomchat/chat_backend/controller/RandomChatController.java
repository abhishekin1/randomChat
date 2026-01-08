package com.randomchat.chat_backend.controller;

import com.randomchat.chat_backend.model.Message;
import com.randomchat.chat_backend.model.User;
import com.randomchat.chat_backend.model.UserConversationDisplay;
import com.randomchat.chat_backend.service.FriendshipService;
import com.randomchat.chat_backend.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

@Controller
@RequiredArgsConstructor
public class RandomChatController {

    @Autowired
    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    private final UserService userService;
    @Autowired
    private final FriendshipService friendshipService;
    
    // Use a concurrent queue to avoid manual synchronization blocks
    private final Queue<String> waitingQueue = new ConcurrentLinkedQueue<>();

    @MessageMapping("/chat.random")
    public void randomChat(@Payload String userId) {
        userId = userId.replaceAll("^\"+|\"+$", "");
        
        String friendUserId = waitingQueue.poll();
        
        if (friendUserId == null) {
            // No one waiting, add self to queue
            waitingQueue.offer(userId);
        } else {
            // Found a match
            if (friendUserId.equals(userId)) {
                // Matched with self (rare race condition or re-entry), put back in queue
                waitingQueue.offer(userId);
                return;
            }

            // Fetch users (this is still blocking, but outside a global lock now)
            // Ideally, these should be cached or fetched asynchronously if possible
            User user = userService.getUserByDeviceId(userId).orElse(null);
            User friend = userService.getUserByDeviceId(friendUserId).orElse(null);

            if (user == null || friend == null) {
                // Handle case where user might have disconnected or doesn't exist
                return;
            }

            Long conversationId = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;

            UserConversationDisplay toUser = new UserConversationDisplay(
                    conversationId,
                    friend.getName(),
                    friend.getUsername(),
                    friend.getPhotoId()
            );
            UserConversationDisplay toFriend = new UserConversationDisplay(
                    conversationId,
                    user.getName(),
                    user.getUsername(),
                    user.getPhotoId()
            );

            messagingTemplate.convertAndSend("/topic/room/random/" + userId, toUser);
            messagingTemplate.convertAndSend("/topic/room/random/" + friendUserId, toFriend);
        }
    }

    @MessageMapping("/chat.random.send")
    public void handleChatMessage(@Payload Message message) {
        String topic = "/topic/room/" + message.getSenderId() + "-" + message.getConversationId();
        messagingTemplate.convertAndSend(topic, message);
    }

    public void handleDisconnectCleanup(String userId) {
        // ConcurrentLinkedQueue remove is O(n), but thread-safe
        waitingQueue.remove(userId);
        messagingTemplate.convertAndSend("/topic/room/random/disconnect_watch"+userId, true);
    }
}
