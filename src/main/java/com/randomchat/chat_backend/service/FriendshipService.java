package com.randomchat.chat_backend.service;

import com.randomchat.chat_backend.Enums;
import com.randomchat.chat_backend.model.*;
import com.randomchat.chat_backend.repository.FriendshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private MessageService messageService;

    public FriendshipService(FriendshipRepository friendshipRepository) {
        this.friendshipRepository = friendshipRepository;
    }




    // ✅ Create or Save a friendship
    public Friendship saveFriendship(Friendship friendship) {
        return friendshipRepository.save(friendship);
    }
    public List<Friendship> getAllFriendships() {
        return friendshipRepository.findAll();
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


    // ✅ Get all conversations by user (either as user1 or user2)
    public List<UserConversationDisplay> getUserConversations(String userId) {
        List<Friendship> friendships = friendshipRepository.findAllAcceptedFriendships(userId);
        List<UserConversationDisplay> userConversationDisplays = new ArrayList<>();
        for(Friendship f :  friendships) {
            long friendshipId = f.getId();

            User friendUser = Objects.equals(f.getUserId(), userId) ?  userService.getUserByDeviceId(f.getFriendId()).get() : userService.getUserByDeviceId(f.getUserId()).get();
            String friendUserId = friendUser.getDeviceId();
            String friendUserName = friendUser.getName();
            String photoUrl = friendUser.getPhotoId();

            Optional<Message> optionalMessage = messageService.getLatestMessageInConversation(friendshipId);
            String lastMessage = null;
            Boolean isByYou = null;
            Enums.MessageStatus messageStatus = null;
            LocalDateTime lastMessageTime = null;
            Enums.MessageType messageType = null;

            if (optionalMessage.isPresent()) {
                Message latestMessage = optionalMessage.get();
                lastMessage = latestMessage.getMessage();
                isByYou = latestMessage.getSenderId().equals(userId);
                messageStatus = latestMessage.getStatus();
                lastMessageTime = latestMessage.getTimeStamp();
                messageType = latestMessage.getType();
            }

            userConversationDisplays.add(new UserConversationDisplay(friendshipId, friendUserName, friendUserId,photoUrl, lastMessage, isByYou, messageStatus, lastMessageTime, messageType, null));
        }
        userConversationDisplays.sort(
                Comparator.comparing(
                        UserConversationDisplay::getLastMessageTime,
                        Comparator.nullsLast(Comparator.reverseOrder())
                )
        );

        return  userConversationDisplays;
    }


}
