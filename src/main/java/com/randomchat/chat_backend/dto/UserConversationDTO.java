package com.randomchat.chat_backend.dto;

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
public class UserConversationDTO {
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

    public UserConversationDTO(Long conversationId, String friendUserName, String friendUserId, String photoUrl) {
        this.conversationId = conversationId;
        this.friendUserName = friendUserName;
        this.friendUserId = friendUserId;
        this.photoUrl = photoUrl;
    }
}
