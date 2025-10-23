package com.irctc.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * IRCTC Notification Service - Microservice for Notifications
 *
 * Responsibilities:
 * - Email notifications
 * - SMS notifications
 * - Push notifications
 * - Notification templates
 * - Notification history and tracking
 *
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableFeignClients
public class NotificationServiceApplication {

    public static void main(String[] args) {
        System.out.println("📧 Starting IRCTC Notification Service...");
        SpringApplication.run(NotificationServiceApplication.class, args);
        System.out.println("✅ IRCTC Notification Service started successfully!");
        System.out.println("📧 Port: 8095");
        System.out.println("📱 Service: Notifications & Alerts");
    }
}
