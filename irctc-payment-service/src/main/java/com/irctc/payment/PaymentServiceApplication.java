package com.irctc.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * IRCTC Payment Service - Microservice for Payment Processing
 *
 * Responsibilities:
 * - Payment processing and validation
 * - Payment gateway integration
 * - Refund processing
 * - Payment history and tracking
 * - Integration with Booking service
 *
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableFeignClients
public class PaymentServiceApplication {

    public static void main(String[] args) {
        System.out.println("💳 Starting IRCTC Payment Service...");
        SpringApplication.run(PaymentServiceApplication.class, args);
        System.out.println("✅ IRCTC Payment Service started successfully!");
        System.out.println("💳 Port: 8094");
        System.out.println("📱 Service: Payment Processing & Management");
    }
}
