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

@Controller
@RequiredArgsConstructor
public class RandomChatController {

    @Autowired
    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    private final UserService userService;
    @Autowired
    private final FriendshipService friendshipService;
    private final Queue<String> waitingQueue = new LinkedList<>();

    @MessageMapping("/chat.random")
    public void randomChat(@Payload String userId) {
        userId = userId.replaceAll("^\"+|\"+$", "");
        synchronized (waitingQueue) {
            if (!waitingQueue.isEmpty()) {
                String friendUserId = waitingQueue.poll();
                if (friendUserId.equals(userId)
//                        || friendshipService.getFriendshipBetween(userId,friendUserId).isPresent()
                ) {
                    randomChat(userId);
                    return;
                }

                User user = userService.getUserByDeviceId(userId).get();
                User friend = userService.getUserByDeviceId(friendUserId).get();

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

            } else {
                waitingQueue.offer(userId);
            }
        }
    }

    @MessageMapping("/chat.random.send")
    public void handleChatMessage(@Payload Message message) {
        System.out.println("-----------------------i am inside random send websocket");
        String topic = "/topic/room/" + message.getSenderId() + "-" + message.getConversationId();
        messagingTemplate.convertAndSend(topic, message);
    }

    public void handleDisconnectCleanup(String userId) {
        synchronized (waitingQueue) {
            waitingQueue.remove(userId);
        }
        messagingTemplate.convertAndSend("/topic/room/random/disconnect_watch"+userId, true);
        System.out.println(" ---------------- waitingqueue size: " + waitingQueue.size());
    }
}
