package com.randomchat.chat_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlineStatusMessage {
    private String senderId;
    private String conversationId;
    private boolean online;
}