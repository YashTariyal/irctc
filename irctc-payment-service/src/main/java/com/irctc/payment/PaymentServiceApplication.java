package com.irctc.payment;

import com.irctc.payment.service.RefundPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

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
@EnableKafka
@EnableAsync
@EnableScheduling
public class PaymentServiceApplication implements CommandLineRunner {

    @Autowired(required = false)
    private RefundPolicyService refundPolicyService;

    public static void main(String[] args) {
        System.out.println("💳 Starting IRCTC Payment Service...");
        SpringApplication.run(PaymentServiceApplication.class, args);
        System.out.println("✅ IRCTC Payment Service started successfully!");
        System.out.println("💳 Port: 8094");
        System.out.println("📱 Service: Payment Processing & Management");
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize default refund policies on startup
        if (refundPolicyService != null) {
            refundPolicyService.initializeDefaultPolicies();
        }
    }
}
