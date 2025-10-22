package com.irctc.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * IRCTC User Service - Microservice for User Management
 * 
 * Responsibilities:
 * - User authentication and authorization
 * - User profile management
 * - Two-factor authentication (2FA)
 * - Password policy enforcement
 * - User security and access control
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        System.out.println("🚀 Starting IRCTC User Service...");
        SpringApplication.run(UserServiceApplication.class, args);
        System.out.println("✅ IRCTC User Service started successfully!");
        System.out.println("🔐 Port: 8081");
        System.out.println("📱 Service: User Management & Authentication");
    }
}
