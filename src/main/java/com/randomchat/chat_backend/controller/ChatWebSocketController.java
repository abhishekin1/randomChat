package com.randomchat.chat_backend.controller;

import com.randomchat.chat_backend.model.Message;
import com.randomchat.chat_backend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService;

    @MessageMapping("/chat.send")
    public void handleChatMessage(@Payload Message message) {
        System.out.println("-----------------------i am inside websocket");
        messageService.saveMessage(message);

        messagingTemplate.convertAndSend("/user/" + message.getReceiverId()+ "/queue/messages", message);

    }
}
