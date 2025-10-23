package com.irctc.swagger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * IRCTC Swagger Hub - Central API Documentation
 *
 * Responsibilities:
 * - Central Swagger UI for all microservices
 * - Service discovery integration
 * - API documentation aggregation
 * - Interactive API testing
 *
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@SpringBootApplication
public class SwaggerHubApplication {

    public static void main(String[] args) {
        System.out.println("📚 Starting IRCTC Swagger Hub...");
        SpringApplication.run(SwaggerHubApplication.class, args);
        System.out.println("✅ IRCTC Swagger Hub started successfully!");
        System.out.println("📚 Port: 8096");
        System.out.println("📱 Service: Central API Documentation");
    }
}
