package com.randomchat.chat_backend;

public class Enums {

    public enum MessageStatus {
        SENT,
        DELIVERED,
        SEEN,
        DELETED
    }

    public enum MessageType {
        TEXT,
        IMAGE,
        VIDEO,
        AUDIO
    }

    public enum TypingStatus {
        USER1,      // First user is typing
        USER2,      // Second user is typing
        BOTH,       // Both are typing
        NO          // No one is typing
    }

    public enum WebSocketEventType {
        MESSAGE,
        TYPING,
        ONLINE_STATUS,
        CONVERSATION_DTO
    }
}
