package com.randomchat.chat_backend.model;

import com.randomchat.chat_backend.Enums;
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

    private String user1Id;      // First user in the conversation
    private String user2Id;      // Second user in the conversation

    private LocalDateTime startedAt;    // Conversation start timestamp
    private LocalDateTime endedAt;      // (Optional) End timestamp

    @Enumerated(EnumType.STRING)
    private Enums.TypingStatus typing;        // Who is typing: USER1, USER2, BOTH, NO
}
