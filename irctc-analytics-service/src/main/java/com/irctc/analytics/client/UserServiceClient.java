package com.irctc.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

/**
 * Feign client for User Service integration
 */
@FeignClient(name = "irctc-user-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {
    
    /**
     * Get all users
     */
    @GetMapping("/api/users")
    List<UserDTO> getAllUsers();
    
    /**
     * Get user by ID
     */
    @GetMapping("/api/users/{userId}")
    UserDTO getUserById(@PathVariable Long userId);
    
    /**
     * Get user statistics
     */
    @GetMapping("/api/users/statistics")
    Map<String, Object> getUserStatistics();
    
    /**
     * User DTO
     */
    class UserDTO {
        private Long id;
        private String username;
        private String email;
        private String role;
        private Boolean isActive;
        private String registrationDate;
        private Integer totalBookings;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        public String getRegistrationDate() { return registrationDate; }
        public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
        public Integer getTotalBookings() { return totalBookings; }
        public void setTotalBookings(Integer totalBookings) { this.totalBookings = totalBookings; }
    }
}

