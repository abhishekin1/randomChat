package com.randomchat.chat_backend.controller;

import com.randomchat.chat_backend.model.Message;
import com.randomchat.chat_backend.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    // ✅ Save a message
    @PostMapping
    public ResponseEntity<Message> saveMessage(@RequestBody Message message) {
        Message savedMessage = messageService.saveMessage(message);
        return ResponseEntity.ok(savedMessage);
    }

    // ✅ Get all messages in a conversation (sorted by timestamp)
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<Message>> getMessagesInConversation(@PathVariable Long conversationId) {
        List<Message> messages = messageService.getMessagesInConversation(conversationId);
        return ResponseEntity.ok(messages);
    }

    // ✅ Get all messages sent by a user
    @GetMapping("/sender/{senderId}")
    public ResponseEntity<List<Message>> getMessagesBySender(@PathVariable String senderId) {
        List<Message> messages = messageService.getMessagesBySender(senderId);
        return ResponseEntity.ok(messages);
    }

    // ✅ Get messages by status (e.g., unread, delivered)
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Message>> getMessagesByStatus(@PathVariable String status) {
        List<Message> messages = messageService.getMessagesByStatus(status);
        return ResponseEntity.ok(messages);
    }

    // ✅ Get messages referring to another message (for replies)
    @GetMapping("/referred/{referredMessageId}")
    public ResponseEntity<List<Message>> getMessagesByReferredMessage(@PathVariable Long referredMessageId) {
        List<Message> messages = messageService.getMessagesByReferredMessage(referredMessageId);
        return ResponseEntity.ok(messages);
    }

    // ✅ Get the latest message in a conversation
    @GetMapping("/latest/{conversationId}")
    public ResponseEntity<Optional<Message>> getLatestMessageInConversation(@PathVariable Long conversationId) {
        Optional<Message> latestMessage = messageService.getLatestMessageInConversation(conversationId);
        return ResponseEntity.ok(latestMessage);
    }

    // ✅ Delete a message by ID
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        messageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }
}
