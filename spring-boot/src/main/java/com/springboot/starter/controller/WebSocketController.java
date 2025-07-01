package com.springboot.starter.controller;

import com.springboot.starter.model.websocket.ChatMessage;
import com.springboot.starter.model.websocket.UserConnectionEvent;
import com.springboot.starter.service.UserWSConnectionService;
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

    private final UserWSConnectionService userWSConnectionService;
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
        
        // Check if user is already connected to avoid duplicate JOIN messages
        boolean wasAlreadyConnected = userWSConnectionService.isUserConnected(username);
        
        userWSConnectionService.addUser(username);
        
        sendConnectedUsersList();
        
        // Only send JOIN message if user wasn't already connected
        if (!wasAlreadyConnected) {
            chatMessage.setType(ChatMessage.MessageType.JOIN);
            chatMessage.setContent(username + " joined the chat");
            chatMessage.setTimestamp(System.currentTimeMillis());
            
            log.info("User {} joined the chat", username);
            
            return chatMessage;
        } else {
            log.info("User {} reconnected (already in connected users)", username);
            
            // Return null to avoid broadcasting a duplicate JOIN message
            return null;
        }
    }

    private void sendConnectedUsersList() {
        UserConnectionEvent connectionEvent = new UserConnectionEvent();
        connectionEvent.setTimestamp(System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/users", userWSConnectionService.getConnectedUsers());
    }
}
