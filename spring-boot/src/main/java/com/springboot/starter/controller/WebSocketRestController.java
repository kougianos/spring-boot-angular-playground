package com.springboot.starter.controller;

import com.springboot.starter.service.UserConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/websocket")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WebSocketRestController {

    private final UserConnectionService userConnectionService;

    @GetMapping("/connected-users")
    public ResponseEntity<Set<String>> getConnectedUsers() {
        return ResponseEntity.ok(userConnectionService.getConnectedUsers());
    }

    @GetMapping("/connection-info")
    public ResponseEntity<Map<String, Object>> getConnectionInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("connectedUsersCount", userConnectionService.getConnectedUsersCount());
        info.put("connectedUsers", userConnectionService.getConnectedUsers());
        info.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(info);
    }

    @GetMapping("/user/{username}/status")
    public ResponseEntity<Map<String, Object>> getUserStatus(@PathVariable String username) {
        Map<String, Object> status = new HashMap<>();
        status.put("username", username);
        status.put("connected", userConnectionService.isUserConnected(username));
        status.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(status);
    }
}
