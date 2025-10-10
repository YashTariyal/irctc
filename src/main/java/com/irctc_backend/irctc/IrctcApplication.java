package com.irctc_backend.irctc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * IRCTC Backend Application - Main Spring Boot Application Class
 * 
 * This application provides a comprehensive railway booking system with the following features:
 * - Train and station management
 * - Passenger booking and reservation
 * - Payment processing
 * - Real-time notifications via Kafka
 * - Dashboard with analytics
 * - Service discovery support
 * - Caching for performance optimization
 * - AOP for cross-cutting concerns
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@EnableConfigurationProperties
public class IrctcApplication {

    /**
     * Main method to start the IRCTC application
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            SpringApplication app = new SpringApplication(IrctcApplication.class);
            
            // Set custom application properties
            app.setBannerMode(org.springframework.boot.Banner.Mode.CONSOLE);
            app.setLogStartupInfo(true);
            
            // Start the application
            app.run(args);
            
            System.out.println("🚂 IRCTC Backend Application started successfully!");
            System.out.println("📊 Dashboard available at: http://localhost:8082/dashboard");
            System.out.println("📚 API Documentation: http://localhost:8082/swagger-ui.html");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to start IRCTC application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}