package com.irctc.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.irctc.external.notification.TwilioSmsService;

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
@ComponentScan(basePackages = {"com.irctc.notification", "com.irctc.external.notification"}, 
               excludeFilters = {
                   @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.irctc.external.railways.*"),
                   @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.irctc.external.weather.*"),
                   @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.irctc.external.maps.*"),
                   @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.irctc.external.notification.TwilioSmsService.class})
               })
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
