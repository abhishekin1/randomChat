package com.randomchat.chat_backend.service;

import com.randomchat.chat_backend.model.Conversation;
import com.randomchat.chat_backend.repository.ConversationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;

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
    public List<Conversation> getUserConversations(String userId) {
        return conversationRepository.findByUserId(userId);
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
