package com.irctc.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IRCTC Booking Service - Microservice for Ticket Booking
 *
 * Responsibilities:
 * - Ticket booking and reservation
 * - Booking management and cancellation
 * - Seat selection and allocation
 * - Booking history and status
 * - Integration with Train and Payment services
 *
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableFeignClients
@EnableAsync
@EnableScheduling
public class BookingServiceApplication {

    public static void main(String[] args) {
        System.out.println("🎫 Starting IRCTC Booking Service...");
        SpringApplication.run(BookingServiceApplication.class, args);
        System.out.println("✅ IRCTC Booking Service started successfully!");
        System.out.println("🎫 Port: 8093");
        System.out.println("📱 Service: Ticket Booking & Management");
    }
}
