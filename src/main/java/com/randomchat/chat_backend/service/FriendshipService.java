package com.randomchat.chat_backend.service;

import com.randomchat.chat_backend.model.Friendship;
import com.randomchat.chat_backend.model.User;
import com.randomchat.chat_backend.repository.FriendshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    @Autowired
    private UserService userService;

    public FriendshipService(FriendshipRepository friendshipRepository) {
        this.friendshipRepository = friendshipRepository;
    }

    // ✅ Create or Save a friendship
    public Friendship saveFriendship(Friendship friendship) {
        return friendshipRepository.save(friendship);
    }

    // ✅ Get all friendships by user ID
    public List<Friendship> getAllFriendshipsByUserId(String userId) {
        return friendshipRepository.findAll()
                .stream()
                .filter(f -> f.getUserId().equals(userId) || f.getFriendId().equals(userId))
                .toList();
    }

    // ✅ Get friendship between two users (bidirectional)
    public Optional<Friendship> getFriendship(String userId, String friendId) {
        return friendshipRepository.findByUserIds(userId, friendId);
    }

    // ✅ Get friendship between two users (bidirectional)
    public List<User> getFriendsOfUser(String userId) {
        List<String> friendIds = friendshipRepository.findFriendIdsByUserId(userId);
        return userService.getUsersByIds(friendIds);

    }

    // ✅ Check if two users are friends
    public boolean areFriends(String userId, String friendId) {
        return friendshipRepository.existsByUserIds(userId, friendId);
    }

    // ✅ Remove a friendship
    public void removeFriendship(String userId, String friendId) {
        getFriendship(userId, friendId).ifPresent(friendshipRepository::delete);
    }
}
