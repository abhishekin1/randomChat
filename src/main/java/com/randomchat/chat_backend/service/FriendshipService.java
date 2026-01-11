package com.randomchat.chat_backend.service;

import com.randomchat.chat_backend.model.*;
import com.randomchat.chat_backend.repository.FriendshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    public List<User> getRequestsToBeAccepted(String myUserId) {
        List<Friendship> requests = friendshipRepository.findByFriendIdAndStatus(myUserId, Friendship.FriendshipStatus.PENDING);
        return requests.stream()
                .map(friendship -> userService.getUserByDeviceId(friendship.getUserId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public List<User> getSentPendingRequests(String myUserId) {
        List<Friendship> sentRequests = friendshipRepository.findByUserIdAndStatus(myUserId, Friendship.FriendshipStatus.PENDING);
        return sentRequests.stream()
                .map(friendship -> userService.getUserByDeviceId(friendship.getFriendId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public List<User> getAllFriends(String myUserId) {
        List<Friendship> friendships = friendshipRepository.findAllAcceptedFriendships(myUserId);

        return friendships.stream()
                .map(f -> {
                    String otherId = f.getUserId().equals(myUserId) ? f.getFriendId() : f.getUserId();
                    return userService.getUserByDeviceId(otherId);
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Transactional
    public void deleteFriendship(String userId, String friendId) {
        friendshipRepository.deleteByUserIds(userId, friendId);
    }

    public Optional<Friendship> getFriendshipBetween(String userId, String friendId) {
        return friendshipRepository.findBetweenUsers(userId, friendId);
    }

}
