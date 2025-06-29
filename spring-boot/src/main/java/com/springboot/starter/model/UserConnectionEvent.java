package com.springboot.starter.model;

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
