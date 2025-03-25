package com.randomchat.chat_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long user1Id;      // First user in the conversation
    private Long user2Id;      // Second user in the conversation

    private LocalDateTime startedAt;    // Conversation start timestamp
    private LocalDateTime endedAt;      // (Optional) End timestamp

    @Enumerated(EnumType.STRING)
    private TypingStatus typing;        // Who is typing: USER1, USER2, BOTH, NO

    private Long lastMessageId;         // ID of the last message

    // Embedded Typing Enum
    public enum TypingStatus {
        USER1,      // First user is typing
        USER2,      // Second user is typing
        BOTH,       // Both are typing
        NO          // No one is typing
    }
}
