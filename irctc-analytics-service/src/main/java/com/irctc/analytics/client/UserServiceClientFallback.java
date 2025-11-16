package com.irctc.analytics.client;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fallback implementation for User Service Client
 */
@Component
public class UserServiceClientFallback implements UserServiceClient {
    
    @Override
    public List<UserDTO> getAllUsers() {
        return new ArrayList<>();
    }
    
    @Override
    public UserDTO getUserById(Long userId) {
        return new UserDTO();
    }
    
    @Override
    public Map<String, Object> getUserStatistics() {
        return new HashMap<>();
    }
}

