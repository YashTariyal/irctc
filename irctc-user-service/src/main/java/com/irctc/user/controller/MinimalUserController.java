package com.irctc.user.controller;

import com.irctc.user.entity.SimpleUser;
import com.irctc.user.repository.SimpleUserRepository;
import com.irctc.user.service.EventPublisherService;
import com.irctc.user.service.SimpleUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class MinimalUserController {

    @Autowired
    private SimpleUserRepository userRepository;
    
    @Autowired
    private SimpleUserService userService;
    
    @Autowired
    private EventPublisherService eventPublisherService;
    
    @Autowired
    private com.irctc.user.service.ReferralService referralService;
    
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping
    public ResponseEntity<List<SimpleUser>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimpleUser> getUserById(@PathVariable Long id) {
        SimpleUser user = userService.getUserById(id)
                .orElseThrow(() -> new com.irctc.user.exception.EntityNotFoundException("User", id));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<SimpleUser> getUserByUsername(@PathVariable String username) {
        SimpleUser user = userService.getUserByUsername(username)
                .orElseThrow(() -> new com.irctc.user.exception.EntityNotFoundException("User", username));
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<SimpleUser> createUser(@RequestBody SimpleUser user) {
        SimpleUser newUser = userService.createUser(user);
        return ResponseEntity.ok(newUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SimpleUser> updateUser(@PathVariable Long id, @RequestBody SimpleUser user) {
        SimpleUser updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ===== AUTHENTICATION APIs =====
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> userData,
                                          @RequestParam(value = "referralCode", required = false) String referralCode) {
        try {
            // Validate required fields
            if (!userData.containsKey("username") || !userData.containsKey("password") || 
                !userData.containsKey("email") || !userData.containsKey("firstName") || 
                !userData.containsKey("lastName")) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }
            
            // Check if username already exists
            if (userRepository.findByUsername(userData.get("username")).isPresent()) {
                return ResponseEntity.badRequest().body("Username already exists");
            }
            
            // Check if email already exists
            if (userRepository.findByEmail(userData.get("email")).isPresent()) {
                return ResponseEntity.badRequest().body("Email already exists");
            }
            
            // Create new user
            SimpleUser user = new SimpleUser();
            user.setUsername(userData.get("username"));
            user.setPassword(passwordEncoder.encode(userData.get("password")));
            user.setEmail(userData.get("email"));
            user.setFirstName(userData.get("firstName"));
            user.setLastName(userData.get("lastName"));
            user.setPhoneNumber(userData.getOrDefault("phoneNumber", ""));
            user.setRoles("USER");
            
            SimpleUser savedUser = userRepository.save(user);
            referralService.handlePostRegistration(savedUser, referralCode);
            
            // Publish user registered event
            eventPublisherService.publishUserRegistered(
                savedUser.getId(), 
                savedUser.getUsername(), 
                savedUser.getEmail(), 
                savedUser.getFirstName(), 
                savedUser.getLastName()
            );
            
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginData) {
        try {
            String username = loginData.get("username");
            String password = loginData.get("password");
            
            if (username == null || password == null) {
                return ResponseEntity.badRequest().body("Username and password required");
            }
            
            Optional<SimpleUser> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid credentials");
            }
            
            SimpleUser user = userOpt.get();
            if (!passwordEncoder.matches(password, user.getPassword())) {
                return ResponseEntity.badRequest().body("Invalid credentials");
            }
            
            // Create response with user info (excluding password)
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("roles", user.getRoles());
            response.put("message", "Login successful");
            
            // Publish user login event
            eventPublisherService.publishUserLogin(
                user.getId(), 
                user.getUsername(), 
                "127.0.0.1", // In production, get from request
                "IRCTC-Web-Client" // In production, get from request headers
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }
    
    // ===== ADVANCED USER APIs =====
    
    @GetMapping("/active")
    public ResponseEntity<List<SimpleUser>> getActiveUsers() {
        // For now, return all users (in real implementation, filter by status)
        return ResponseEntity.ok(userRepository.findAll());
    }
    
    @GetMapping("/verified")
    public ResponseEntity<List<SimpleUser>> getVerifiedUsers() {
        // For now, return all users (in real implementation, filter by verification status)
        return ResponseEntity.ok(userRepository.findAll());
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<SimpleUser> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search/name")
    public ResponseEntity<List<SimpleUser>> searchUsersByName(@RequestParam String name) {
        List<SimpleUser> users = userRepository.findAll().stream()
                .filter(user -> user.getFirstName().toLowerCase().contains(name.toLowerCase()) ||
                               user.getLastName().toLowerCase().contains(name.toLowerCase()))
                .toList();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/search/email")
    public ResponseEntity<List<SimpleUser>> searchUsersByEmail(@RequestParam String email) {
        List<SimpleUser> users = userRepository.findAll().stream()
                .filter(user -> user.getEmail().toLowerCase().contains(email.toLowerCase()))
                .toList();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/role/{role}")
    public ResponseEntity<List<SimpleUser>> getUsersByRole(@PathVariable String role) {
        List<SimpleUser> users = userRepository.findAll().stream()
                .filter(user -> user.getRoles() != null && user.getRoles().equals(role))
                .toList();
        return ResponseEntity.ok(users);
    }
    
    @PutMapping("/{id}/role")
    public ResponseEntity<SimpleUser> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        SimpleUser user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setRoles(role);
        SimpleUser updatedUser = userRepository.save(user);
        return ResponseEntity.ok(updatedUser);
    }
    
    @PutMapping("/{id}/verify")
    public ResponseEntity<SimpleUser> verifyUser(@PathVariable Long id) {
        SimpleUser user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        // In real implementation, set verification status
        SimpleUser updatedUser = userRepository.save(user);
        return ResponseEntity.ok(updatedUser);
    }
    
    @PutMapping("/{id}/activate")
    public ResponseEntity<SimpleUser> activateUser(@PathVariable Long id) {
        SimpleUser user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        // In real implementation, set active status
        SimpleUser updatedUser = userRepository.save(user);
        return ResponseEntity.ok(updatedUser);
    }
    
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<SimpleUser> deactivateUser(@PathVariable Long id) {
        SimpleUser user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        // In real implementation, set inactive status
        SimpleUser updatedUser = userRepository.save(user);
        return ResponseEntity.ok(updatedUser);
    }
}
