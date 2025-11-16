package com.irctc.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * IRCTC Analytics Service - Revenue Analytics Dashboard
 *
 * Responsibilities:
 * - Revenue trends analysis (daily, weekly, monthly)
 * - Booking analytics (trends, cancellation rates, refund analytics)
 * - Route performance analysis (most profitable routes, popular routes)
 * - User segmentation analysis
 * - Revenue and booking forecasting
 *
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableFeignClients
@EnableCaching
public class AnalyticsServiceApplication {

    public static void main(String[] args) {
        System.out.println("📊 Starting IRCTC Analytics Service...");
        SpringApplication.run(AnalyticsServiceApplication.class, args);
        System.out.println("✅ IRCTC Analytics Service started successfully!");
        System.out.println("📊 Port: 8096");
        System.out.println("📱 Service: Revenue Analytics Dashboard");
    }
}

