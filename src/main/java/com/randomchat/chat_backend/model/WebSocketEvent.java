package com.randomchat.chat_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketEvent<T> {
    private String type; // "MESSAGE", "TYPING", "ONLINE_STATUS"
    private T payload;
}
