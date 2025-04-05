package com.randomchat.chat_backend.service;

import com.randomchat.chat_backend.model.Message;
import com.randomchat.chat_backend.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    // ✅ Save a message
    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    // ✅ Get all messages in a conversation (sorted by timestamp)
    public List<Message> getMessagesInConversation(Long conversationId) {
        return messageRepository.findByConversationIdOrderByTimeStampAsc(conversationId);
    }

    // ✅ Get all messages sent by a user
    public List<Message> getMessagesBySender(String senderId) {
        return messageRepository.findBySenderId(senderId);
    }


    // ✅ Get messages by status (e.g., unread, delivered)
    public List<Message> getMessagesByStatus(String status) {
        return messageRepository.findByStatus(status);
    }

    // ✅ Get messages referring to another message (for replies)
    public List<Message> getMessagesByReferredMessage(Long referredMessageId) {
        return messageRepository.findByReferredMessageId(referredMessageId);
    }


    // ✅ Get the latest message in a conversation
    public Optional<Message> getLatestMessageInConversation(Long conversationId) {
        return messageRepository.findTopByConversationIdOrderByTimeStampDesc(conversationId);
    }

    // ✅ Delete a message by ID
    public void deleteMessage(Long messageId) {
        messageRepository.deleteById(messageId);
    }
}
