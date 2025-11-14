package com.irctc.payment.webhook;

import com.irctc.payment.service.SimplePaymentService;
import com.irctc.payment.webhook.service.WebhookVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Controller for handling payment gateway webhooks
 */
@RestController
@RequestMapping("/api/payments/webhooks")
public class WebhookController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    
    @Autowired(required = false)
    private WebhookVerificationService webhookVerificationService;
    
    @Autowired
    private SimplePaymentService paymentService;
    
    /**
     * Razorpay webhook endpoint
     */
    @PostMapping("/razorpay")
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature,
            HttpServletRequest request) {
        
        logger.info("Received Razorpay webhook: {}", payload);
        
        try {
            // Verify webhook signature
            if (webhookVerificationService != null && signature != null) {
                // Read request body as string for signature verification
                java.util.Scanner scanner = new java.util.Scanner(request.getInputStream()).useDelimiter("\\A");
                String requestBody = scanner.hasNext() ? scanner.next() : "";
                scanner.close();
                
                boolean isValid = webhookVerificationService.verifyRazorpaySignature(requestBody, signature);
                if (!isValid) {
                    logger.warn("Invalid Razorpay webhook signature");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
                }
            }
            
            // Process webhook
            String event = (String) payload.get("event");
            Map<String, Object> paymentData = (Map<String, Object>) payload.get("payload");
            
            if ("payment.captured".equals(event) || "payment.authorized".equals(event)) {
                Map<String, Object> paymentEntity = (Map<String, Object>) paymentData.get("payment");
                Map<String, Object> entity = (Map<String, Object>) paymentEntity.get("entity");
                String gatewayTransactionId = (String) entity.get("id");
                updatePaymentStatus(gatewayTransactionId, "COMPLETED");
            } else if ("payment.failed".equals(event)) {
                Map<String, Object> paymentEntity = (Map<String, Object>) paymentData.get("payment");
                Map<String, Object> entity = (Map<String, Object>) paymentEntity.get("entity");
                String gatewayTransactionId = (String) entity.get("id");
                updatePaymentStatus(gatewayTransactionId, "FAILED");
            }
            
            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            logger.error("Error processing Razorpay webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
        }
    }
    
    /**
     * Stripe webhook endpoint
     */
    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signature,
            HttpServletRequest request) {
        
        logger.info("Received Stripe webhook");
        
        try {
            // Verify webhook signature
            if (webhookVerificationService != null) {
                boolean isValid = webhookVerificationService.verifyStripeSignature(payload, signature);
                if (!isValid) {
                    logger.warn("Invalid Stripe webhook signature");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
                }
            }
            
            // Process webhook (Stripe sends JSON)
            // Parse payload and update payment status
            
            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            logger.error("Error processing Stripe webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
        }
    }
    
    /**
     * PayU webhook endpoint
     */
    @PostMapping("/payu")
    public ResponseEntity<String> handlePayUWebhook(
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {
        
        logger.info("Received PayU webhook: {}", payload);
        
        try {
            // PayU webhook verification
            if (webhookVerificationService != null) {
                boolean isValid = webhookVerificationService.verifyPayUSignature(payload);
                if (!isValid) {
                    logger.warn("Invalid PayU webhook signature");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
                }
            }
            
            // Process webhook
            String status = (String) payload.get("status");
            String gatewayTransactionId = (String) payload.get("txnid");
            
            if ("success".equalsIgnoreCase(status)) {
                updatePaymentStatus(gatewayTransactionId, "COMPLETED");
            } else if ("failure".equalsIgnoreCase(status)) {
                updatePaymentStatus(gatewayTransactionId, "FAILED");
            }
            
            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            logger.error("Error processing PayU webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
        }
    }
    
    private void updatePaymentStatus(String gatewayTransactionId, String status) {
        try {
            // Find payment by gateway transaction ID and update status
            paymentService.getPaymentByTransactionId(gatewayTransactionId)
                .ifPresent(payment -> {
                    payment.setStatus(status);
                    paymentService.processPayment(payment); // Re-save with updated status
                    logger.info("Updated payment status: {} -> {}", gatewayTransactionId, status);
                });
        } catch (Exception e) {
            logger.error("Error updating payment status: {}", e.getMessage(), e);
        }
    }
}

