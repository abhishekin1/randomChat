package com.randomchat.chat_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypingStatusDTO {
    private String senderId;
    private String conversationId;
    private boolean typing;
}
