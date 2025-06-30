package com.springboot.starter.controller;

import com.springboot.starter.service.UserWSConnectionService;
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

    private final UserWSConnectionService userWSConnectionService;

    @GetMapping("/connected-users")
    public ResponseEntity<Set<String>> getConnectedUsers() {
        return ResponseEntity.ok(userWSConnectionService.getConnectedUsers());
    }

    @GetMapping("/connection-info")
    public ResponseEntity<Map<String, Object>> getConnectionInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("connectedUsersCount", userWSConnectionService.getConnectedUsersCount());
        info.put("connectedUsers", userWSConnectionService.getConnectedUsers());
        info.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(info);
    }

    @GetMapping("/user/{username}/status")
    public ResponseEntity<Map<String, Object>> getUserStatus(@PathVariable String username) {
        Map<String, Object> status = new HashMap<>();
        status.put("username", username);
        status.put("connected", userWSConnectionService.isUserConnected(username));
        status.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(status);
    }
}
