package com.randomchat.chat_backend.controller;

import com.randomchat.chat_backend.model.Conversation;
import com.randomchat.chat_backend.service.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    // ✅ Create or Save a conversation
    @PostMapping
    public ResponseEntity<Conversation> createConversation(@RequestBody Conversation conversation) {
        Conversation savedConversation = conversationService.saveConversation(conversation);
        return ResponseEntity.ok(savedConversation);
    }

    // ✅ Get conversation by ID
    @GetMapping("/{id}")
    public ResponseEntity<Optional<Conversation>> getConversationById(@PathVariable Long id) {
        Optional<Conversation> conversation = conversationService.getConversationById(id);
        if (conversation.isPresent()) {
            return ResponseEntity.ok(conversation);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ Get all conversations for a specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Conversation>> getUserConversations(@PathVariable String userId) {
        List<Conversation> conversations = conversationService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    // ✅ End a conversation (set endedAt timestamp)
    @PutMapping("/{id}/end")
    public ResponseEntity<Void> endConversation(@PathVariable Long id) {
        conversationService.endConversation(id);
        return ResponseEntity.noContent().build();
    }
}
