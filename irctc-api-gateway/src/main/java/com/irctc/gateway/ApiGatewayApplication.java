package com.irctc.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * IRCTC API Gateway - Central entry point for all microservices
 * 
 * Responsibilities:
 * - Route requests to appropriate microservices
 * - Load balancing across service instances
 * - Circuit breaker for fault tolerance
 * - Authentication and authorization
 * - Rate limiting and throttling
 * - Request/response transformation
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        System.out.println("🚀 Starting IRCTC API Gateway...");
        SpringApplication.run(ApiGatewayApplication.class, args);
        System.out.println("✅ IRCTC API Gateway started successfully!");
        System.out.println("🌐 Port: 8080");
        System.out.println("🔀 Gateway: Central routing for all microservices");
    }
}
