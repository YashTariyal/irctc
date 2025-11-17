package com.irctc.train;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IRCTC Train Service - Microservice for Train Management
 *
 * Responsibilities:
 * - Train information and schedules
 * - Train search and filtering
 * - Route management
 * - Seat availability
 * - Train status and updates
 *
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.irctc.train", "com.irctc.external.railways", "com.irctc.external.weather", "com.irctc.external.maps"})
@EnableFeignClients
@EnableScheduling
public class TrainServiceApplication {

    public static void main(String[] args) {
        System.out.println("🚂 Starting IRCTC Train Service...");
        SpringApplication.run(TrainServiceApplication.class, args);
        System.out.println("✅ IRCTC Train Service started successfully!");
        System.out.println("🚂 Port: 8092");
        System.out.println("📱 Service: Train Management & Search");
    }
}
