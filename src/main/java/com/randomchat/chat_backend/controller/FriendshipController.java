package com.randomchat.chat_backend.controller;

import com.randomchat.chat_backend.model.Friendship;
import com.randomchat.chat_backend.model.User;
import com.randomchat.chat_backend.model.UserConversationDisplay;
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

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @PostMapping("/request/{userId}/{friendId}/{send_or_accept}")
    public String friendRequest(@PathVariable String userId, @PathVariable String friendId, @PathVariable String send_or_accept) {
        if(Objects.equals(send_or_accept, "send")){
            if (friendshipService.getFriendshipBetween(userId, friendId).isPresent()) {
                return ResponseEntity
                        .status(409)
                        .body("Friendship or request already exists").toString();
            }
            Friendship friendship = new Friendship();
            friendship.setUserId(userId);
            friendship.setFriendId(friendId);
            friendship.setCreatedAt(LocalDateTime.now());
            friendship.setStatus(Friendship.FriendshipStatus.PENDING);
            friendshipService.saveFriendship(friendship);
            return friendship.toString();
        } else {
            if (friendshipService.getFriendshipBetween(userId, friendId).isEmpty()) {
                return ResponseEntity
                        .status(404)
                        .body("Friendship or request do not exists").toString();
            }
            Friendship friendship = friendshipService.getFriendshipBetween(userId, friendId).get();
            friendship.setStatus(Friendship.FriendshipStatus.ACCEPTED);
            friendshipService.saveFriendship(friendship);
            return friendship.toString();
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




    // ✅ Get all friendships
    @GetMapping
    public List<Friendship> getAllFriendships() {
        return friendshipService.getAllFriendships();
    }

    // ✅ Create a new friendship (send request)
    @PostMapping
    public Friendship createFriendship(@RequestBody Friendship friendship) {
        friendship.setCreatedAt(LocalDateTime.now());
        friendship.setStatus(Friendship.FriendshipStatus.PENDING);
        return friendshipService.saveFriendship(friendship);
    }

    @GetMapping("conversations/{userId}")
    public List<UserConversationDisplay> getUserConversations(@PathVariable String userId) {
        return friendshipService.getUserConversations(userId);
    }
}
