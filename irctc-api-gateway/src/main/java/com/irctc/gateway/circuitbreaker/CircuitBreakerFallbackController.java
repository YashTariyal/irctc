package com.irctc.gateway.circuitbreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Circuit Breaker Fallback Controller
 * 
 * Provides fallback responses when circuit breakers are open
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
public class CircuitBreakerFallbackController {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerFallbackController.class);
    
    /**
     * Default fallback handler
     */
    @RequestMapping("/fallback/default")
    public Mono<ResponseEntity<Map<String, Object>>> defaultFallback(ServerWebExchange exchange) {
        logger.warn("⚠️  Circuit breaker opened - default fallback triggered for path: {}", 
            exchange.getRequest().getURI().getPath());
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("message", "Service is temporarily unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now());
        response.put("path", exchange.getRequest().getURI().getPath());
        
        return Mono.just(ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(response));
    }
    
    /**
     * Booking service fallback
     */
    @RequestMapping("/fallback/booking")
    public Mono<ResponseEntity<Map<String, Object>>> bookingFallback(ServerWebExchange exchange) {
        logger.warn("⚠️  Booking service circuit breaker opened - fallback triggered");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "BOOKING_SERVICE_UNAVAILABLE");
        response.put("message", "Booking service is temporarily unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now());
        response.put("suggestion", "You can check your booking status later or contact customer support.");
        
        return Mono.just(ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(response));
    }
    
    /**
     * Payment service fallback
     */
    @RequestMapping("/fallback/payment")
    public Mono<ResponseEntity<Map<String, Object>>> paymentFallback(ServerWebExchange exchange) {
        logger.warn("⚠️  Payment service circuit breaker opened - fallback triggered");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "PAYMENT_SERVICE_UNAVAILABLE");
        response.put("message", "Payment service is temporarily unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now());
        response.put("suggestion", "Your payment has not been processed. Please retry after a few moments.");
        
        return Mono.just(ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(response));
    }
    
    /**
     * Train service fallback
     */
    @RequestMapping("/fallback/train")
    public Mono<ResponseEntity<Map<String, Object>>> trainFallback(ServerWebExchange exchange) {
        logger.warn("⚠️  Train service circuit breaker opened - fallback triggered");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "TRAIN_SERVICE_UNAVAILABLE");
        response.put("message", "Train service is temporarily unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now());
        response.put("suggestion", "You can try searching for trains again in a few moments.");
        
        return Mono.just(ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(response));
    }
    
    /**
     * User service fallback
     */
    @RequestMapping("/fallback/user")
    public Mono<ResponseEntity<Map<String, Object>>> userFallback(ServerWebExchange exchange) {
        logger.warn("⚠️  User service circuit breaker opened - fallback triggered");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "USER_SERVICE_UNAVAILABLE");
        response.put("message", "User service is temporarily unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now());
        response.put("suggestion", "Please try again in a few moments.");
        
        return Mono.just(ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(response));
    }
    
    /**
     * Notification service fallback
     */
    @RequestMapping("/fallback/notification")
    public Mono<ResponseEntity<Map<String, Object>>> notificationFallback(ServerWebExchange exchange) {
        logger.warn("⚠️  Notification service circuit breaker opened - fallback triggered");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "NOTIFICATION_SERVICE_UNAVAILABLE");
        response.put("message", "Notification service is temporarily unavailable.");
        response.put("timestamp", LocalDateTime.now());
        response.put("note", "Notifications may be delayed but will be sent once service recovers.");
        
        return Mono.just(ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(response));
    }
}

