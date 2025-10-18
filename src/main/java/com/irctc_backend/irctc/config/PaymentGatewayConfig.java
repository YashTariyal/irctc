package com.irctc_backend.irctc.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Payment Gateway Configuration
 * 
 * This configuration class sets up payment gateway clients and configurations
 * for the IRCTC booking system. It supports multiple payment gateways including
 * Razorpay for seamless payment processing.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Configuration
public class PaymentGatewayConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewayConfig.class);
    
    @Value("${payment.razorpay.key-id:}")
    private String razorpayKeyId;
    
    @Value("${payment.razorpay.key-secret:}")
    private String razorpayKeySecret;
    
    @Value("${payment.razorpay.webhook-secret:}")
    private String razorpayWebhookSecret;
    
    @Value("${payment.gateway.timeout:30000}")
    private int gatewayTimeout;
    
    @Value("${payment.gateway.retry-attempts:3}")
    private int retryAttempts;
    
    /**
     * Configure Razorpay client for payment processing
     * 
     * @return RazorpayClient instance
     * @throws RazorpayException if client initialization fails
     */
    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        if (razorpayKeyId == null || razorpayKeyId.isEmpty() || 
            razorpayKeySecret == null || razorpayKeySecret.isEmpty()) {
            logger.warn("Razorpay credentials not configured. Payment gateway will not be available.");
            return null;
        }
        
        logger.info("Initializing Razorpay client with key ID: {}", razorpayKeyId);
        return new RazorpayClient(razorpayKeyId, razorpayKeySecret);
    }
    
    /**
     * Configure WebClient for HTTP requests to payment gateways
     * 
     * @return WebClient instance with timeout configuration
     */
    @Bean
    public WebClient paymentWebClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }
    
    /**
     * Get Razorpay key ID for frontend integration
     * 
     * @return Razorpay key ID
     */
    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }
    
    /**
     * Get Razorpay webhook secret for signature verification
     * 
     * @return Razorpay webhook secret
     */
    public String getRazorpayWebhookSecret() {
        return razorpayWebhookSecret;
    }
    
    /**
     * Get gateway timeout configuration
     * 
     * @return timeout in milliseconds
     */
    public int getGatewayTimeout() {
        return gatewayTimeout;
    }
    
    /**
     * Get retry attempts configuration
     * 
     * @return number of retry attempts
     */
    public int getRetryAttempts() {
        return retryAttempts;
    }
}
