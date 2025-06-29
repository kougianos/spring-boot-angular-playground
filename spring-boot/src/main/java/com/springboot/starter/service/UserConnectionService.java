package com.springboot.starter.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserConnectionService {
    
    private final Set<String> connectedUsers = ConcurrentHashMap.newKeySet();
    
    public void addUser(String username) {
        connectedUsers.add(username);
    }
    
    public void removeUser(String username) {
        connectedUsers.remove(username);
    }
    
    public Set<String> getConnectedUsers() {
        return Set.copyOf(connectedUsers);
    }
    
    public boolean isUserConnected(String username) {
        return connectedUsers.contains(username);
    }
    
    public int getConnectedUsersCount() {
        return connectedUsers.size();
    }
}
