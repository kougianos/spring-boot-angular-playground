package com.springboot.starter.controller;

import com.springboot.starter.model.ChatMessage;
import com.springboot.starter.model.UserConnectionEvent;
import com.springboot.starter.service.UserConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final UserConnectionService userConnectionService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(System.currentTimeMillis());
        log.info("Received message from {}: {}", chatMessage.getSender(), chatMessage.getContent());
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String username = chatMessage.getSender();
        
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("username", username);
        
        userConnectionService.addUser(username);
        
        chatMessage.setType(ChatMessage.MessageType.JOIN);
        chatMessage.setContent(username + " joined the chat");
        chatMessage.setTimestamp(System.currentTimeMillis());
        
        log.info("User {} joined the chat", username);
        
        sendConnectedUsersList();
        
        return chatMessage;
    }

    private void sendConnectedUsersList() {
        UserConnectionEvent connectionEvent = new UserConnectionEvent();
        connectionEvent.setTimestamp(System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/users", userConnectionService.getConnectedUsers());
    }
}
