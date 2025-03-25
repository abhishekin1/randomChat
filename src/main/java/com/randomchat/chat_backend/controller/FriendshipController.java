package com.randomchat.chat_backend.controller;

import com.randomchat.chat_backend.model.Friendship;
import com.randomchat.chat_backend.model.User;
import com.randomchat.chat_backend.service.FriendshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/friendships")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    // ✅ Create a new friendship
    @PostMapping
    public ResponseEntity<Friendship> createFriendship(@RequestBody Friendship friendship) {
        Friendship savedFriendship = friendshipService.saveFriendship(friendship);
        return ResponseEntity.ok(savedFriendship);
    }

    // ✅ Get all friendships by user ID
    @GetMapping("/{userId}")
    public ResponseEntity<List<Friendship>> getAllFriendshipsByUserId(@PathVariable String userId) {
        List<Friendship> friendships = friendshipService.getAllFriendshipsByUserId(userId);
        return ResponseEntity.ok(friendships);
    }

    // ✅ Get friendship between two users (bidirectional)
    @GetMapping("/{userId}/{friendId}")
    public ResponseEntity<Friendship> getFriendship(
            @PathVariable String userId,
            @PathVariable String friendId) {

        Optional<Friendship> friendship = friendshipService.getFriendship(userId, friendId);
        return friendship.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Get friends of a user
    @GetMapping("/{userId}/friends")
    public ResponseEntity<List<User>> getFriendsOfUser(@PathVariable String userId) {
        List<User> friends = friendshipService.getFriendsOfUser(userId);
        return ResponseEntity.ok(friends);
    }

    // ✅ Check if two users are friends
    @GetMapping("/{userId}/is-friend/{friendId}")
    public ResponseEntity<Boolean> areFriends(
            @PathVariable String userId,
            @PathVariable String friendId) {

        boolean areFriends = friendshipService.areFriends(userId, friendId);
        return ResponseEntity.ok(areFriends);
    }

    // ✅ Remove a friendship
    @DeleteMapping("/{userId}/{friendId}")
    public ResponseEntity<Void> removeFriendship(
            @PathVariable String userId,
            @PathVariable String friendId) {

        friendshipService.removeFriendship(userId, friendId);
        return ResponseEntity.noContent().build();
    }
}
