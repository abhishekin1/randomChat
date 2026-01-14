package com.randomchat.chat_backend.dto;

import com.randomchat.chat_backend.Enums;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketEventDTO<T> {
    private Enums.WebSocketEventType type; // MESSAGE, TYPING, ONLINE_STATUS
    private T payload;
}
