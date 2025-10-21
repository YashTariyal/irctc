package com.irctc_backend.irctc.controller;

import com.irctc_backend.irctc.service.PasswordPolicyService;
import com.irctc_backend.irctc.service.TwoFactorAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Security Controller for password policies and 2FA
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/security")
@CrossOrigin(origins = "*")
public class SecurityController {
    
    @Autowired
    private PasswordPolicyService passwordPolicyService;
    
    @Autowired
    private TwoFactorAuthService twoFactorAuthService;
    
    /**
     * Validate password strength
     */
    @PostMapping("/validate-password")
    public ResponseEntity<Map<String, Object>> validatePassword(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        
        PasswordPolicyService.PasswordValidationResult result = passwordPolicyService.validatePassword(password);
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", result.isValid());
        response.put("errors", result.getErrors());
        response.put("warnings", result.getWarnings());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Generate secure password suggestion
     */
    @GetMapping("/generate-password")
    public ResponseEntity<Map<String, String>> generatePassword() {
        String securePassword = passwordPolicyService.generateSecurePassword();
        
        Map<String, String> response = new HashMap<>();
        response.put("password", securePassword);
        response.put("message", "Generated secure password");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Generate OTP for 2FA
     */
    @PostMapping("/generate-otp")
    public ResponseEntity<Map<String, Object>> generateOtp(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String phoneNumber = request.get("phoneNumber");
        String email = request.get("email");
        
        TwoFactorAuthService.OtpResult result = twoFactorAuthService.generateOtp(userId, phoneNumber, email);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Verify OTP for 2FA
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String otp = request.get("otp");
        
        TwoFactorAuthService.OtpResult result = twoFactorAuthService.verifyOtp(userId, otp);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());
        response.put("token", result.getToken());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if 2FA is enabled
     */
    @GetMapping("/2fa/status/{userId}")
    public ResponseEntity<Map<String, Boolean>> getTwoFactorStatus(@PathVariable String userId) {
        boolean enabled = twoFactorAuthService.isTwoFactorEnabled(userId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("enabled", enabled);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Enable 2FA
     */
    @PostMapping("/2fa/enable/{userId}")
    public ResponseEntity<Map<String, Object>> enableTwoFactor(@PathVariable String userId) {
        boolean success = twoFactorAuthService.enableTwoFactor(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "2FA enabled successfully" : "Failed to enable 2FA");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Disable 2FA
     */
    @PostMapping("/2fa/disable/{userId}")
    public ResponseEntity<Map<String, Object>> disableTwoFactor(@PathVariable String userId) {
        boolean success = twoFactorAuthService.disableTwoFactor(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "2FA disabled successfully" : "Failed to disable 2FA");
        
        return ResponseEntity.ok(response);
    }
}
