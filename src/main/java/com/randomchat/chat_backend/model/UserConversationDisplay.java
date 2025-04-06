package com.randomchat.chat_backend.model;

import com.randomchat.chat_backend.Enums;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserConversationDisplay {
    private Long conversationId;
    private String friendUserName;
    private String friendUserId;
    private String photoUrl;
    private String lastMessage;
    private Boolean isByYou;
    @Enumerated(EnumType.STRING)
    private Enums.MessageStatus messageStatus;
    private LocalDateTime lastMessageTime;
    @Enumerated(EnumType.STRING)
    private Enums.MessageType messageType;
    private Boolean isTyping;
}
