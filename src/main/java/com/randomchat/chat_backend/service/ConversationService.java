package com.randomchat.chat_backend.service;

import com.randomchat.chat_backend.Enums;
import com.randomchat.chat_backend.dto.UserConversationDTO;
import com.randomchat.chat_backend.model.Conversation;
import com.randomchat.chat_backend.model.Message;
import com.randomchat.chat_backend.model.User;
import com.randomchat.chat_backend.repository.ConversationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private MessageService messageService;

    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    // ✅ Create or Save a conversation
    public Conversation saveConversation(Conversation conversation) {
        conversation.setStartedAt(LocalDateTime.now());
        return conversationRepository.save(conversation);
    }

    // ✅ Get conversation by ID
    public Optional<Conversation> getConversationById(Long id) {
        return conversationRepository.findById(id);
    }

    // ✅ Get all conversations by user (either as user1 or user2)
    public List<UserConversationDTO> getUserConversations(String userId) {
        List<Conversation> conversations = conversationRepository.findByUserId(userId);
        
        if (conversations.isEmpty()) {
            return new ArrayList<>();
        }

        // Collect friend IDs
        List<String> friendIds = conversations.stream()
                .map(c -> Objects.equals(c.getUser1Id(), userId) ? c.getUser2Id() : c.getUser1Id())
                .distinct()
                .toList();

        // Fetch users in batch
        Map<String, User> userMap = userService.getUsersByIds(friendIds).stream()
                .collect(Collectors.toMap(User::getUsername, Function.identity()));

        List<UserConversationDTO> userConversationDisplays = new ArrayList<>();
        for(Conversation c :  conversations) {
            long conversationId = c.getId();
            String friendId = Objects.equals(c.getUser1Id(), userId) ? c.getUser2Id() : c.getUser1Id();
            User friendUser = userMap.get(friendId);
            
            if (friendUser == null) continue;

            String friendUserId = friendUser.getUsername();
            String friendUserName = friendUser.getName();
            String photoUrl = friendUser.getPhotoUrl();

            Optional<Message> latestMessageOpt = messageService.getLatestMessageInConversation(conversationId);
            String lastMessage = null;
            Boolean isByYou = null;
            Enums.MessageStatus messageStatus = null;
            LocalDateTime lastMessageTime = null;
            Enums.MessageType messageType = null;

            if (latestMessageOpt.isPresent()) {
                Message latestMessage = latestMessageOpt.get();
                lastMessage = latestMessage.getMessage();
                isByYou = latestMessage.getSenderId().equals(userId);
                messageStatus = latestMessage.getStatus();
                lastMessageTime = latestMessage.getTimeStamp();
                messageType = latestMessage.getType();
            }

            Boolean isTyping = Enums.TypingStatus.BOTH.equals(c.getTyping())
                                || (Enums.TypingStatus.USER1.equals(c.getTyping()) && friendUserId.equals(c.getUser1Id()))
                                || (Enums.TypingStatus.USER2.equals(c.getTyping()) && friendUserId.equals(c.getUser2Id()));

            userConversationDisplays.add(new UserConversationDTO(conversationId, friendUserName, friendUserId,photoUrl, lastMessage, isByYou, messageStatus, lastMessageTime, messageType));
        }
        
        userConversationDisplays.sort(
                Comparator.comparing(
                        UserConversationDTO::getLastMessageTime,
                        Comparator.nullsLast(Comparator.reverseOrder())
                )
        );

        return  userConversationDisplays;
    }

    // ✅ End a conversation by setting the endedAt timestamp
    public void endConversation(Long id) {
        Optional<Conversation> optionalConversation = conversationRepository.findById(id);

        if (optionalConversation.isPresent()) {
            Conversation conversation = optionalConversation.get();
            conversation.setEndedAt(LocalDateTime.now());
            conversationRepository.save(conversation);
        }
    }

    public Optional<Conversation> getConversationBetween(String user1Id, String user2Id) {
        return conversationRepository.findBetweenUsers(user1Id, user2Id);
    }
}
