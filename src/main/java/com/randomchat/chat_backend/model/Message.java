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
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                      // Unique message ID

    private Long conversationId;          // ID of the conversation
    private String senderId;                // ID of the sender
    private String message;               // Message content
    private String objectUri;             // URI for media (if any)
    private Long referredMessageId;       // ID of the referred message
    private String reactEmojiSender;    // ID of the user reacting with an emoji
    private String reactEmojiReceiver;
    private LocalDateTime timeStamp;      // Message sent time
    @Enumerated(EnumType.STRING)
    private Enums.MessageStatus status;         // SENT, DELIVERED, SEEN
    @Enumerated(EnumType.STRING)
    private Enums.MessageType type;

}
