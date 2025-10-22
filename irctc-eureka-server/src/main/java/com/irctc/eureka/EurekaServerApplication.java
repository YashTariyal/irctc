package com.irctc.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * IRCTC Eureka Server - Service Discovery and Registration
 * 
 * Responsibilities:
 * - Service registration and discovery
 * - Health monitoring of registered services
 * - Load balancing information
 * - Service metadata management
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        System.out.println("🚀 Starting IRCTC Eureka Server...");
        SpringApplication.run(EurekaServerApplication.class, args);
        System.out.println("✅ IRCTC Eureka Server started successfully!");
        System.out.println("🔍 Port: 8761");
        System.out.println("📋 Dashboard: http://localhost:8761");
        System.out.println("🌐 Service Discovery: Central registry for all microservices");
    }
}
