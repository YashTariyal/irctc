package com.irctc.payment.webhook.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

/**
 * Service for verifying webhook signatures from payment gateways
 */
@Service
public class WebhookVerificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(WebhookVerificationService.class);
    
    @Value("${payment.gateway.razorpay.webhook-secret:}")
    private String razorpayWebhookSecret;
    
    @Value("${payment.gateway.stripe.webhook-secret:}")
    private String stripeWebhookSecret;
    
    @Value("${payment.gateway.payu.merchant-salt:}")
    private String payuMerchantSalt;
    
    /**
     * Verify Razorpay webhook signature
     */
    public boolean verifyRazorpaySignature(String payload, String signature) {
        if (razorpayWebhookSecret == null || razorpayWebhookSecret.isEmpty()) {
            logger.warn("Razorpay webhook secret not configured, skipping verification");
            return true; // Allow if not configured
        }
        
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                razorpayWebhookSecret.getBytes(StandardCharsets.UTF_8), 
                "HmacSHA256"
            );
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = bytesToHex(hash);
            
            return calculatedSignature.equals(signature);
        } catch (Exception e) {
            logger.error("Error verifying Razorpay signature: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Verify Stripe webhook signature
     */
    public boolean verifyStripeSignature(String payload, String signature) {
        if (stripeWebhookSecret == null || stripeWebhookSecret.isEmpty()) {
            logger.warn("Stripe webhook secret not configured, skipping verification");
            return true;
        }
        
        try {
            // Stripe uses HMAC SHA256
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                stripeWebhookSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = bytesToHex(hash);
            
            // Stripe signature format: timestamp,signature
            if (signature != null && signature.contains(",")) {
                String[] parts = signature.split(",");
                if (parts.length >= 2) {
                    return calculatedSignature.equals(parts[1]);
                }
            }
            
            return false;
        } catch (Exception e) {
            logger.error("Error verifying Stripe signature: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Verify PayU webhook signature
     */
    public boolean verifyPayUSignature(Map<String, Object> payload) {
        if (payuMerchantSalt == null || payuMerchantSalt.isEmpty()) {
            logger.warn("PayU merchant salt not configured, skipping verification");
            return true;
        }
        
        try {
            // PayU signature verification logic
            String hashString = (String) payload.get("hash");
            if (hashString == null) {
                return false;
            }
            
            // Reconstruct hash
            String txnid = (String) payload.get("txnid");
            String amount = (String) payload.get("amount");
            String productinfo = (String) payload.get("productinfo");
            String firstname = (String) payload.get("firstname");
            String email = (String) payload.get("email");
            String status = (String) payload.get("status");
            
            String hashData = payuMerchantSalt + "|" + status + "|||||||||||" + email + "|" + 
                firstname + "|" + productinfo + "|" + amount + "|" + txnid;
            
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] hashBytes = md.digest(hashData.getBytes(StandardCharsets.UTF_8));
            String calculatedHash = bytesToHex(hashBytes);
            
            return calculatedHash.equalsIgnoreCase(hashString);
        } catch (Exception e) {
            logger.error("Error verifying PayU signature: {}", e.getMessage(), e);
            return false;
        }
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}

