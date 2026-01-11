package com.randomchat.chat_backend.controller;

import com.randomchat.chat_backend.Enums;
import com.randomchat.chat_backend.model.Conversation;
import com.randomchat.chat_backend.model.Friendship;
import com.randomchat.chat_backend.model.User;
import com.randomchat.chat_backend.service.ConversationService;
import com.randomchat.chat_backend.service.FriendshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/friendships")
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final ConversationService conversationService;

    public FriendshipController(FriendshipService friendshipService, ConversationService conversationService) {
        this.friendshipService = friendshipService;
        this.conversationService = conversationService;
    }

    @PostMapping("/request/{userId}/{friendId}/{send_or_accept}")
    public ResponseEntity<?> friendRequest(@PathVariable String userId, @PathVariable String friendId, @PathVariable String send_or_accept) {
        if(Objects.equals(send_or_accept, "send")){
            if (friendshipService.getFriendshipBetween(userId, friendId).isPresent()) {
                return ResponseEntity
                        .status(409)
                        .body("Friendship or request already exists");
            }
            Friendship friendship = new Friendship();
            friendship.setUserId(userId);
            friendship.setFriendId(friendId);
            friendship.setCreatedAt(LocalDateTime.now());
            friendship.setStatus(Friendship.FriendshipStatus.PENDING);
            friendshipService.saveFriendship(friendship);
            return ResponseEntity.ok(friendship);
        } else if (Objects.equals(send_or_accept, "accept")) {
            Optional<Friendship> optionalFriendship = friendshipService.getFriendshipBetween(userId, friendId);
            if (optionalFriendship.isEmpty()) {
                return ResponseEntity
                        .status(404)
                        .body("Friendship or request do not exists");
            }
            Friendship friendship = optionalFriendship.get();
            if (friendship.getStatus() == Friendship.FriendshipStatus.ACCEPTED) {
                return ResponseEntity.ok(friendship);
            }
            friendship.setStatus(Friendship.FriendshipStatus.ACCEPTED);
            friendshipService.saveFriendship(friendship);

            // Create conversation if not exists
            if (conversationService.getConversationBetween(userId, friendId).isEmpty()) {
                Conversation conversation = new Conversation();
                conversation.setUser1Id(userId);
                conversation.setUser2Id(friendId);
                conversation.setStartedAt(LocalDateTime.now());
                conversation.setTyping(Enums.TypingStatus.NO);
                conversationService.saveConversation(conversation);
            }

            return ResponseEntity.ok(friendship);
        } else if (Objects.equals(send_or_accept, "reject")) {
             Optional<Friendship> optionalFriendship = friendshipService.getFriendshipBetween(userId, friendId);
             if (optionalFriendship.isPresent()) {
                 friendshipService.deleteFriendship(userId, friendId);
                 return ResponseEntity.ok("Friend request rejected");
             }
             return ResponseEntity.status(404).body("Friendship request not found");
        } else {
            return ResponseEntity.badRequest().body("Invalid action");
        }

    }

    @GetMapping("/to-be-accepted/{userId}")
    public List<User> getPendingRequestsToBeAccepted(@PathVariable String userId) {
        return friendshipService.getRequestsToBeAccepted(userId);
    }

    @GetMapping("/pending-sent/{userId}")
    public List<User> getPendingSentRequests(@PathVariable String userId) {
        return friendshipService.getSentPendingRequests(userId);
    }

    //Used to see friends.
    @GetMapping("/list/{userId}")
    public List<User> getAllFriendUsers(@PathVariable String userId) {
        return friendshipService.getAllFriends(userId);
    }

    //bidirectional
    @DeleteMapping("/{userId}/{friendId}")
    public ResponseEntity<String> deleteFriendship(@PathVariable String userId, @PathVariable String friendId) {
        friendshipService.deleteFriendship(userId, friendId);
        return ResponseEntity.ok("Friendship deleted (if existed)");
    }

    @GetMapping("/between/{userId}/{friendId}")
    public ResponseEntity<?> getFriendshipBetweenUsers(@PathVariable String userId, @PathVariable String friendId) {
        return friendshipService.getFriendshipBetween(userId, friendId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("No friendship exists"));
    }

}
