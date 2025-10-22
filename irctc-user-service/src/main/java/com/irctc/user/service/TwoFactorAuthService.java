package com.irctc.user.service;

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
 * Two-Factor Authentication Service for IRCTC User Service
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
            otpStorage.put(cacheKey, otpData);
            logger.info("Stored OTP with key: {} in in-memory storage", cacheKey);

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
            OtpData otpData = otpStorage.get(cacheKey);
            logger.info("Looking for OTP with key: {}, found: {}", cacheKey, otpData != null ? "YES" : "NO");
            logger.info("Available keys in storage: {}", otpStorage.keySet());

            if (otpData == null) {
                return new OtpResult(false, "OTP not found or expired", null);
            }

            // Check if OTP is expired
            if (LocalDateTime.now().isAfter(otpData.getExpiryTime())) {
                otpStorage.remove(cacheKey);
                return new OtpResult(false, "OTP has expired", null);
            }

            // Check if too many attempts
            if (otpData.getAttempts() >= MAX_OTP_ATTEMPTS) {
                otpStorage.remove(cacheKey);
                return new OtpResult(false, "Too many incorrect attempts. OTP invalidated.", null);
            }

            // Verify OTP
            if (otpData.getOtp().equals(inputOtp)) {
                // OTP is correct
                otpStorage.remove(cacheKey);
                clearFailedAttempts(userId);
                logger.info("OTP verified successfully for user: {}", userId);
                return new OtpResult(true, "OTP verified successfully", null);
            } else {
                // Increment attempts
                otpData.incrementAttempts();
                otpStorage.put(cacheKey, otpData);

                int remainingAttempts = MAX_OTP_ATTEMPTS - otpData.getAttempts();
                if (remainingAttempts <= 0) {
                    otpStorage.remove(cacheKey);
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
     * Resend OTP
     */
    public OtpResult resendOtp(String userId, String phoneNumber, String email) {
        logger.info("Resending OTP for user: {}", userId);
        // Invalidate any existing OTP for the user
        String cacheKey = "otp:" + userId;
        otpStorage.remove(cacheKey);
        return generateOtp(userId, phoneNumber, email);
    }

    /**
     * Generate secure OTP
     */
    private String generateSecureOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10)); // 0-9
        }
        return otp.toString();
    }

    /**
     * Simulate sending OTP via SMS
     */
    private boolean sendOtpViaSms(String phoneNumber, String otp) {
        // In production, integrate with an SMS gateway like Twilio, MessageBird, etc.
        logger.info("SMS OTP sent to {}: {}", phoneNumber, otp);
        return true;
    }

    /**
     * Simulate sending OTP via Email
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
        String limitKey = "otp:limit:" + userId;
        Integer attempts = failedAttempts.get(limitKey);

        if (attempts != null && attempts >= 5) { // Max 5 OTPs per hour
            return true;
        }

        // Increment counter
        int currentAttempts = (attempts != null ? attempts : 0) + 1;
        failedAttempts.put(limitKey, currentAttempts);

        return false;
    }

    /**
     * Clear failed attempts for user
     */
    private void clearFailedAttempts(String userId) {
        String attemptsKey = "failed:attempts:" + userId;
        failedAttempts.remove(attemptsKey);
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
