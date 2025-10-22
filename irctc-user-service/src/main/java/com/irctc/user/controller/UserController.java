package com.irctc.user.controller;

import com.irctc.user.entity.User;
import com.irctc.user.service.UserService;
import com.irctc.user.service.TwoFactorAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * User Controller for IRCTC User Service
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TwoFactorAuthService twoFactorAuthService;
    
    /**
     * Create a new user
     */
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.findById(#id).get().username == authentication.name")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Get user by username
     */
    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.name")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userService.findByUsername(username);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Update user information
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.findById(#id).get().username == authentication.name")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody User userDetails) {
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Change user password
     */
    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasRole('ADMIN') or @userService.findById(#id).get().username == authentication.name")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody PasswordChangeRequest request) {
        try {
            boolean success = userService.changePassword(id, request.getOldPassword(), request.getNewPassword());
            if (success) {
                return ResponseEntity.ok(new SuccessResponse("Password changed successfully"));
            } else {
                return ResponseEntity.badRequest().body(new ErrorResponse("Invalid old password"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Toggle user account status
     */
    @PostMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id, @RequestBody StatusToggleRequest request) {
        try {
            userService.toggleUserStatus(id, request.isEnabled());
            return ResponseEntity.ok(new SuccessResponse("User status updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Toggle 2FA for user
     */
    @PostMapping("/{id}/toggle-2fa")
    @PreAuthorize("hasRole('ADMIN') or @userService.findById(#id).get().username == authentication.name")
    public ResponseEntity<?> toggleTwoFactorAuth(@PathVariable Long id, @RequestBody TwoFactorToggleRequest request) {
        try {
            userService.toggleTwoFactorAuth(id, request.isEnabled());
            return ResponseEntity.ok(new SuccessResponse("2FA status updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Generate 2FA OTP
     */
    @PostMapping("/{id}/generate-otp")
    @PreAuthorize("hasRole('ADMIN') or @userService.findById(#id).get().username == authentication.name")
    public ResponseEntity<?> generateOtp(@PathVariable Long id) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            TwoFactorAuthService.OtpResult result = twoFactorAuthService.generateOtp(
                user.getId().toString(),
                user.getPhoneNumber(),
                user.getEmail()
            );
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(new SuccessResponse(result.getMessage()));
            } else {
                return ResponseEntity.badRequest().body(new ErrorResponse(result.getMessage()));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Verify 2FA OTP
     */
    @PostMapping("/{id}/verify-otp")
    @PreAuthorize("hasRole('ADMIN') or @userService.findById(#id).get().username == authentication.name")
    public ResponseEntity<?> verifyOtp(@PathVariable Long id, @RequestBody OtpVerificationRequest request) {
        try {
            TwoFactorAuthService.OtpResult result = twoFactorAuthService.verifyOtp(
                id.toString(),
                request.getOtp()
            );
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(new SuccessResponse("OTP verified successfully"));
            } else {
                return ResponseEntity.badRequest().body(new ErrorResponse(result.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("OTP verification failed"));
        }
    }
    
    /**
     * Get all users (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * Get users by role (Admin only)
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable User.Role role) {
        List<User> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }
    
    /**
     * Delete user (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new SuccessResponse("User deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // DTOs for request/response
    public static class PasswordChangeRequest {
        private String oldPassword;
        private String newPassword;
        
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
    
    public static class StatusToggleRequest {
        private boolean enabled;
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
    
    public static class TwoFactorToggleRequest {
        private boolean enabled;
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
    
    public static class OtpVerificationRequest {
        private String otp;
        
        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
    }
    
    public static class SuccessResponse {
        private String message;
        
        public SuccessResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
    
    public static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) { this.error = error; }
        public String getError() { return error; }
    }
}
