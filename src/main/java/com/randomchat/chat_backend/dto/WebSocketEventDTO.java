package com.randomchat.chat_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketEventDTO<T> {
    private String type; // "MESSAGE", "TYPING", "ONLINE_STATUS"
    private T payload;
}
