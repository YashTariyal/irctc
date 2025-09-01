package com.irctc_backend.irctc.service;

import com.irctc_backend.irctc.entity.User;
import com.irctc_backend.irctc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @CacheEvict(value = {"user-sessions", "stations"}, allEntries = true)
    public User registerUser(User user) {
        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Check if phone number already exists
        if (user.getPhoneNumber() != null && userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists");
        }
        
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set default values
        user.setIsVerified(false);
        user.setIsActive(true);
        user.setRole(User.UserRole.USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    @Cacheable(value = "user-sessions", key = "#username")
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Cacheable(value = "user-sessions", key = "#email")
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Cacheable(value = "user-sessions", key = "#id")
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    @Cacheable(value = "user-sessions", key = "'all-users'")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Cacheable(value = "user-sessions", key = "'active-users'")
    public List<User> getActiveUsers() {
        return userRepository.findByIsActive(true);
    }
    
    @Cacheable(value = "user-sessions", key = "#role")
    public List<User> getUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role);
    }
    
    @Cacheable(value = "user-sessions", key = "'active-verified-users'")
    public List<User> getActiveVerifiedUsers() {
        return userRepository.findActiveVerifiedUsers();
    }
    
    @Cacheable(value = "user-sessions", key = "#name")
    public List<User> searchUsersByName(String name) {
        return userRepository.findByNameContaining(name);
    }
    
    @Cacheable(value = "user-sessions", key = "#email")
    public List<User> searchUsersByEmail(String email) {
        return userRepository.findByEmailContaining(email);
    }
    
    @CacheEvict(value = "user-sessions", allEntries = true)
    public User updateUser(User user) {
        User existingUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update fields
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        existingUser.setDateOfBirth(user.getDateOfBirth());
        existingUser.setGender(user.getGender());
        existingUser.setIdProofType(user.getIdProofType());
        existingUser.setIdProofNumber(user.getIdProofNumber());
        existingUser.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(existingUser);
    }
    
    @CacheEvict(value = "user-sessions", allEntries = true)
    public User updateUserRole(Long userId, User.UserRole role) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    @CacheEvict(value = "user-sessions", allEntries = true)
    public User verifyUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setIsVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    @CacheEvict(value = "user-sessions", allEntries = true)
    public User deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    @CacheEvict(value = "user-sessions", allEntries = true)
    public User activateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    public boolean authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return passwordEncoder.matches(password, user.getPassword()) && user.getIsActive();
        }
        return false;
    }
    
    @CacheEvict(value = "user-sessions", allEntries = true)
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
} 