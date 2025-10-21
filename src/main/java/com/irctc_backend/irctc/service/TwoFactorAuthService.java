package com.irctc_backend.irctc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Two-Factor Authentication Service
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class TwoFactorAuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(TwoFactorAuthService.class);
    
    // OTP configuration
    private static final int OTP_LENGTH = 6;
    private static final int OTP_VALIDITY_MINUTES = 5;
    private static final int MAX_OTP_ATTEMPTS = 3;
    
    // Store OTPs temporarily (in production, use Redis or database)
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    
    // Store failed attempts
    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    
    @Autowired(required = false)
    private CacheService cacheService;
    
    /**
     * Generate and send OTP
     */
    public OtpResult generateOtp(String userId, String phoneNumber, String email) {
        try {
            // Check if user has exceeded OTP generation limit
            if (hasExceededOtpLimit(userId)) {
                return new OtpResult(false, "Too many OTP requests. Please try again later.", null);
            }
            
            // Generate OTP
            String otp = generateSecureOtp();
            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES);
            
            // Store OTP data
            OtpData otpData = new OtpData(otp, expiryTime, 0);
            String cacheKey = "otp:" + userId;
            
            if (cacheService != null) {
                cacheService.put(cacheKey, otpData, OTP_VALIDITY_MINUTES * 60);
            } else {
                // Use in-memory storage when cache service is not available
                otpStorage.put(cacheKey, otpData);
                logger.info("Stored OTP with key: {} in in-memory storage", cacheKey);
            }
            
            // Send OTP via SMS and Email (mock implementation)
            boolean smsSent = sendOtpViaSms(phoneNumber, otp);
            boolean emailSent = sendOtpViaEmail(email, otp);
            
            if (smsSent || emailSent) {
                logger.info("OTP generated and sent for user: {}", userId);
                return new OtpResult(true, "OTP sent successfully", null);
            } else {
                return new OtpResult(false, "Failed to send OTP", null);
            }
            
        } catch (Exception e) {
            logger.error("Error generating OTP for user: {}", userId, e);
            return new OtpResult(false, "Failed to generate OTP", null);
        }
    }
    
    /**
     * Verify OTP
     */
    public OtpResult verifyOtp(String userId, String inputOtp) {
        try {
            String cacheKey = "otp:" + userId;
            Optional<OtpData> cachedOtp;
            
            if (cacheService != null) {
                cachedOtp = cacheService.get(cacheKey, OtpData.class);
            } else {
                // Use in-memory storage when cache service is not available
                OtpData otpData = otpStorage.get(cacheKey);
                logger.info("Looking for OTP with key: {}, found: {}", cacheKey, otpData != null ? "YES" : "NO");
                logger.info("Available keys in storage: {}", otpStorage.keySet());
                cachedOtp = Optional.ofNullable(otpData);
            }
            
            if (cachedOtp.isEmpty()) {
                return new OtpResult(false, "OTP not found or expired", null);
            }
            
            OtpData otpData = cachedOtp.get();
            
            // Check if OTP is expired
            if (LocalDateTime.now().isAfter(otpData.getExpiryTime())) {
                if (cacheService != null) {
                    cacheService.delete(cacheKey);
                } else {
                    otpStorage.remove(cacheKey);
                }
                return new OtpResult(false, "OTP has expired", null);
            }
            
            // Check if too many attempts
            if (otpData.getAttempts() >= MAX_OTP_ATTEMPTS) {
                if (cacheService != null) {
                    cacheService.delete(cacheKey);
                } else {
                    otpStorage.remove(cacheKey);
                }
                return new OtpResult(false, "Too many incorrect attempts. OTP invalidated.", null);
            }
            
            // Verify OTP
            if (otpData.getOtp().equals(inputOtp)) {
                // OTP is correct
                if (cacheService != null) {
                    cacheService.delete(cacheKey);
                } else {
                    otpStorage.remove(cacheKey);
                }
                clearFailedAttempts(userId);
                logger.info("OTP verified successfully for user: {}", userId);
                return new OtpResult(true, "OTP verified successfully", null);
            } else {
                // Increment attempts
                otpData.incrementAttempts();
                if (cacheService != null) {
                    cacheService.put(cacheKey, otpData, OTP_VALIDITY_MINUTES * 60);
                } else {
                    otpStorage.put(cacheKey, otpData);
                }
                
                int remainingAttempts = MAX_OTP_ATTEMPTS - otpData.getAttempts();
                if (remainingAttempts <= 0) {
                    if (cacheService != null) {
                        cacheService.delete(cacheKey);
                    } else {
                        otpStorage.remove(cacheKey);
                    }
                    return new OtpResult(false, "Too many incorrect attempts. OTP invalidated.", null);
                }
                
                return new OtpResult(false, 
                    String.format("Invalid OTP. %d attempts remaining.", remainingAttempts), null);
            }
            
        } catch (Exception e) {
            logger.error("Error verifying OTP for user: {}", userId, e);
            return new OtpResult(false, "Failed to verify OTP", null);
        }
    }
    
    /**
     * Check if 2FA is enabled for user
     */
    public boolean isTwoFactorEnabled(String userId) {
        String cacheKey = "2fa:enabled:" + userId;
        Optional<Boolean> enabled = cacheService.get(cacheKey, Boolean.class);
        return enabled.orElse(false);
    }
    
    /**
     * Enable 2FA for user
     */
    public boolean enableTwoFactor(String userId) {
        try {
            String cacheKey = "2fa:enabled:" + userId;
            cacheService.put(cacheKey, true, 86400); // 24 hours
            logger.info("2FA enabled for user: {}", userId);
            return true;
        } catch (Exception e) {
            logger.error("Error enabling 2FA for user: {}", userId, e);
            return false;
        }
    }
    
    /**
     * Disable 2FA for user
     */
    public boolean disableTwoFactor(String userId) {
        try {
            String cacheKey = "2fa:enabled:" + userId;
            cacheService.delete(cacheKey);
            logger.info("2FA disabled for user: {}", userId);
            return true;
        } catch (Exception e) {
            logger.error("Error disabling 2FA for user: {}", userId, e);
            return false;
        }
    }
    
    /**
     * Generate secure OTP
     */
    private String generateSecureOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }
    
    /**
     * Send OTP via SMS (mock implementation)
     */
    private boolean sendOtpViaSms(String phoneNumber, String otp) {
        try {
            // In production, integrate with SMS service like Twilio, AWS SNS, etc.
            logger.info("SMS OTP sent to {}: {}", phoneNumber, otp);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send SMS OTP to {}", phoneNumber, e);
            return false;
        }
    }
    
    /**
     * Send OTP via Email (mock implementation)
     */
    private boolean sendOtpViaEmail(String email, String otp) {
        try {
            // In production, integrate with email service like SendGrid, AWS SES, etc.
            logger.info("Email OTP sent to {}: {}", email, otp);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send email OTP to {}", email, e);
            return false;
        }
    }
    
    /**
     * Check if user has exceeded OTP generation limit
     */
    private boolean hasExceededOtpLimit(String userId) {
        if (cacheService == null) {
            return false; // No cache service, allow OTP generation
        }
        
        String limitKey = "otp:limit:" + userId;
        Optional<Integer> attempts = cacheService.get(limitKey, Integer.class);
        
        if (attempts.isPresent() && attempts.get() >= 5) { // Max 5 OTPs per hour
            return true;
        }
        
        // Increment counter
        int currentAttempts = attempts.orElse(0) + 1;
        cacheService.put(limitKey, currentAttempts, 3600); // 1 hour TTL
        
        return false;
    }
    
    /**
     * Clear failed attempts for user
     */
    private void clearFailedAttempts(String userId) {
        if (cacheService == null) {
            return; // No cache service, nothing to clear
        }
        
        String attemptsKey = "failed:attempts:" + userId;
        cacheService.delete(attemptsKey);
    }
    
    /**
     * OTP Data class
     */
    public static class OtpData {
        private String otp;
        private LocalDateTime expiryTime;
        private int attempts;
        
        public OtpData(String otp, LocalDateTime expiryTime, int attempts) {
            this.otp = otp;
            this.expiryTime = expiryTime;
            this.attempts = attempts;
        }
        
        public String getOtp() {
            return otp;
        }
        
        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }
        
        public int getAttempts() {
            return attempts;
        }
        
        public void incrementAttempts() {
            this.attempts++;
        }
    }
    
    /**
     * OTP Result class
     */
    public static class OtpResult {
        private final boolean success;
        private final String message;
        private final String token;
        
        public OtpResult(boolean success, String message, String token) {
            this.success = success;
            this.message = message;
            this.token = token;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getToken() {
            return token;
        }
    }
}
