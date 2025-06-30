package com.springboot.starter.model.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserConnectionEvent {
    private String username;
    private boolean connected;
    private long timestamp;
}
