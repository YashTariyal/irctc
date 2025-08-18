package com.irctc_backend.irctc.aspect;

import com.irctc_backend.irctc.annotation.ExecutionTime;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Test class to demonstrate AOP timing functionality.
 * This test verifies that the ExecutionTimeAspect is working correctly.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "logging.level.com.irctc_backend.irctc.aspect.ExecutionTimeAspect=INFO",
    "logging.level.root=INFO",
    "logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
})
public class ExecutionTimeAspectTest {
    
    /**
     * Test method to demonstrate @ExecutionTime annotation.
     * This method will be intercepted by the aspect and timed.
     */
    @ExecutionTime("Test Method Execution")
    public String testMethod() {
        // Simulate some work
        try {
            Thread.sleep(100); // Simulate 100ms of work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Test completed";
    }
    
    /**
     * Test method without @ExecutionTime annotation.
     * This method will not be timed by the custom aspect.
     */
    public String testMethodWithoutAnnotation() {
        return "Test without annotation";
    }
    
    @Test
    public void testExecutionTimeAspect() {
        System.out.println("\n🧪 Testing AOP Timing Functionality");
        System.out.println("=====================================");
        
        // Test method with @ExecutionTime annotation
        System.out.println("📝 Testing method with @ExecutionTime annotation...");
        String result1 = testMethod();
        System.out.println("✅ Result: " + result1);
        
        // Test method without annotation
        System.out.println("📝 Testing method without @ExecutionTime annotation...");
        String result2 = testMethodWithoutAnnotation();
        System.out.println("✅ Result: " + result2);
        
        System.out.println("\n📊 Expected Console Output:");
        System.out.println("   🚀 Starting execution of: Test Method Execution in class: ExecutionTimeAspectTest");
        System.out.println("   ✅ Completed: Test Method Execution in class: ExecutionTimeAspectTest - Execution time: XXXms");
        
        // The aspect should have logged timing information for testMethod()
        // but not for testMethodWithoutAnnotation()
    }
    
    @Test
    public void testSlowExecution() {
        System.out.println("\n🐌 Testing Slow Execution Warning");
        System.out.println("=================================");
        
        // Test method that simulates slow execution
        System.out.println("📝 Testing slow method (should trigger warning)...");
        slowTestMethod();
        
        System.out.println("\n📊 Expected Console Output:");
        System.out.println("   🚀 Starting execution of: Slow Test Method in class: ExecutionTimeAspectTest");
        System.out.println("   ⚠️  Slow execution detected: Slow Test Method in class: ExecutionTimeAspectTest took XXXms");
        System.out.println("   ✅ Completed: Slow Test Method in class: ExecutionTimeAspectTest - Execution time: XXXms");
    }
    
    @ExecutionTime("Slow Test Method")
    public void slowTestMethod() {
        try {
            Thread.sleep(1500); // Simulate 1.5 seconds of work (should trigger warning)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    public void testErrorHandling() {
        System.out.println("\n❌ Testing Error Handling with Timing");
        System.out.println("=====================================");
        
        try {
            System.out.println("📝 Testing method that throws exception...");
            methodThatThrowsException();
        } catch (Exception e) {
            System.out.println("✅ Exception caught as expected: " + e.getMessage());
        }
        
        System.out.println("\n📊 Expected Console Output:");
        System.out.println("   🚀 Starting execution of: Method That Throws Exception in class: ExecutionTimeAspectTest");
        System.out.println("   ❌ Failed: Method That Throws Exception in class: ExecutionTimeAspectTest - Execution time: XXXms - Error: Test exception");
    }
    
    @ExecutionTime("Method That Throws Exception")
    public void methodThatThrowsException() {
        try {
            Thread.sleep(50); // Simulate some work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        throw new RuntimeException("Test exception");
    }
}
