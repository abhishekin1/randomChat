package com.randomchat.chat_backend.service;

import com.randomchat.chat_backend.Enums;
import com.randomchat.chat_backend.model.Conversation;
import com.randomchat.chat_backend.model.Message;
import com.randomchat.chat_backend.model.User;
import com.randomchat.chat_backend.model.UserConversationDisplay;
import com.randomchat.chat_backend.repository.ConversationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    public List<UserConversationDisplay> getUserConversations(String userId) {
        List<Conversation> conversations = conversationRepository.findByUserId(userId);
        List<UserConversationDisplay> userConversationDisplays = new ArrayList<>();
        for(Conversation c :  conversations) {
            long conversationId = c.getId();

            User friendUser = Objects.equals(c.getUser1Id(), userId) ?  userService.getUserByDeviceId(c.getUser2Id()).get() : userService.getUserByDeviceId(c.getUser1Id()).get();
            String friendUserId = friendUser.getUsername();
            String friendUserName = friendUser.getName();
            String photoUrl = friendUser.getPhotoUrl();

            Message latestMessage = messageService.getLatestMessageInConversation(conversationId).get();
            String lastMessage = latestMessage.getMessage();
            Boolean isByYou = latestMessage.getSenderId().equals(userId);
            Enums.MessageStatus messageStatus = latestMessage.getStatus();
            LocalDateTime lastMessageTime = latestMessage.getTimeStamp();
            Enums.MessageType messageType = latestMessage.getType();
            Boolean isTyping = Enums.TypingStatus.BOTH.equals(c.getTyping())
                                || (Enums.TypingStatus.USER1.equals(c.getTyping()) && friendUserId.equals(c.getUser1Id()))
                                || (Enums.TypingStatus.USER2.equals(c.getTyping()) && friendUserId.equals(c.getUser2Id()));

            userConversationDisplays.add(new UserConversationDisplay(conversationId, friendUserName, friendUserId,photoUrl, lastMessage, isByYou, messageStatus, lastMessageTime, messageType, isTyping));
        }
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
}
