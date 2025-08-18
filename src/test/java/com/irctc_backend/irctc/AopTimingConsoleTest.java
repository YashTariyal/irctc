package com.irctc_backend.irctc;

import com.irctc_backend.irctc.annotation.ExecutionTime;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Simple test to demonstrate AOP timing console output.
 * This test shows how the timing logs will appear in the console.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "logging.level.com.irctc_backend.irctc.aspect.ExecutionTimeAspect=INFO",
    "logging.level.root=INFO"
})
public class AopTimingConsoleTest {
    
    @Test
    public void demonstrateConsoleOutput() {
        System.out.println("\n🎯 AOP Timing Console Output Demonstration");
        System.out.println("==========================================");
        System.out.println();
        
        System.out.println("📋 When you run the application with: ./mvnw spring-boot:run");
        System.out.println("   You will see timing logs in the console like this:");
        System.out.println();
        
        System.out.println("🌐 API Request: GET /api/bookings/123 - getBookingById in class: BookingController");
        System.out.println("✅ API Response: GET /api/bookings/123 - getBookingById in class: BookingController - Execution time: 45ms");
        System.out.println();
        
        System.out.println("🚀 Starting execution of: Create Train Booking in class: BookingService");
        System.out.println("✅ Completed: Create Train Booking in class: BookingService - Execution time: 234ms");
        System.out.println();
        
        System.out.println("⚠️  Slow API detected: POST /api/bookings - createBooking in class: BookingController took 2500ms");
        System.out.println("⚠️  Slow execution detected: Create Train Booking in class: BookingService took 1500ms");
        System.out.println();
        
        System.out.println("❌ API Error: GET /api/bookings/999 - getBookingById in class: BookingController - Execution time: 12ms - Error: Booking not found");
        System.out.println();
        
        System.out.println("📊 Log Files Created:");
        System.out.println("   - logs/irctc-api.log (API timing logs)");
        System.out.println("   - logs/irctc-application.log (Application logs)");
        System.out.println("   - logs/irctc-json.log (Structured JSON logs)");
        System.out.println();
        
        // Demonstrate actual timing
        System.out.println("🧪 Testing actual timing now...");
        testMethod();
        
        System.out.println("✅ Console output demonstration completed!");
    }
    
    @ExecutionTime("Console Demo Method")
    public void testMethod() {
        try {
            Thread.sleep(200); // Simulate 200ms of work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
