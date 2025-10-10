package com.irctc_backend.irctc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(IrctcApplication.class);

    /**
     * Main method to start the IRCTC application
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            logger.info("🚀 Starting IRCTC Backend Application...");
            
            SpringApplication app = new SpringApplication(IrctcApplication.class);
            
            // Set custom application properties
            app.setBannerMode(org.springframework.boot.Banner.Mode.CONSOLE);
            app.setLogStartupInfo(true);
            
            // Add shutdown hook for graceful shutdown
            app.setRegisterShutdownHook(true);
            
            // Start the application
            var context = app.run(args);
            
            // Display startup information
            displayStartupInfo(context);
            
            logger.info("✅ IRCTC Backend Application started successfully!");
            
        } catch (Exception e) {
            logger.error("❌ Failed to start IRCTC application: {}", e.getMessage(), e);
            System.err.println("❌ Failed to start IRCTC application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Display application startup information
     */
    private static void displayStartupInfo(org.springframework.context.ConfigurableApplicationContext context) {
        String port = context.getEnvironment().getProperty("server.port", "8082");
        String profile = String.join(",", context.getEnvironment().getActiveProfiles());
        if (profile.isEmpty()) {
            profile = "default";
        }
        
        logger.info("Application startup information:");
        logger.info("🌐 Server Port: {}", port);
        logger.info("🔧 Active Profile(s): {}", profile);
        logger.info("📊 Dashboard: http://localhost:{}/dashboard", port);
        logger.info("📚 API Docs: http://localhost:{}/swagger-ui.html", port);
        logger.info("🔍 Health Check: http://localhost:{}/actuator/health", port);
        logger.info("📈 Metrics: http://localhost:{}/actuator/metrics", port);
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🚂 IRCTC Backend Application started successfully!");
        System.out.println("=".repeat(60));
        System.out.println("🌐 Server Port: " + port);
        System.out.println("🔧 Active Profile(s): " + profile);
        System.out.println("📊 Dashboard: http://localhost:" + port + "/dashboard");
        System.out.println("📚 API Docs: http://localhost:" + port + "/swagger-ui.html");
        System.out.println("🔍 Health Check: http://localhost:" + port + "/actuator/health");
        System.out.println("📈 Metrics: http://localhost:" + port + "/actuator/metrics");
        System.out.println("=".repeat(60) + "\n");
    }
}