package com.irctc.user.service;

import com.irctc.user.entity.SimpleUser;
import com.irctc.user.repository.SimpleUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * User Service with Caching Support
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class SimpleUserService {

    @Autowired
    private SimpleUserRepository userRepository;

    @Cacheable(value = "users", key = "#id", unless = "#result.isEmpty()")
    public Optional<SimpleUser> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Cacheable(value = "users-by-email", key = "#email", unless = "#result.isEmpty()")
    public Optional<SimpleUser> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Cacheable(value = "users", key = "#username", unless = "#result.isEmpty()")
    public Optional<SimpleUser> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<SimpleUser> getAllUsers() {
        return userRepository.findAll();
    }

    @CacheEvict(value = {"users", "users-by-email"}, allEntries = false)
    public SimpleUser createUser(SimpleUser user) {
        return userRepository.save(user);
    }

    @CacheEvict(value = {"users", "users-by-email"}, key = "#id", allEntries = false)
    public SimpleUser updateUser(Long id, SimpleUser user) {
        SimpleUser existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        
        return userRepository.save(existingUser);
    }

    @CacheEvict(value = {"users", "users-by-email"}, key = "#id", allEntries = false)
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}

