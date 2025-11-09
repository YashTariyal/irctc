package com.irctc.booking.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for distributed locking
 * 
 * Use this annotation on methods that require distributed locking
 * to prevent concurrent execution across service instances.
 * 
 * Example:
 * <pre>
 * {@code
 * @DistributedLock(key = "booking:#{#trainId}:#{#journeyDate}", timeout = 30)
 * public Booking createBooking(Long trainId, LocalDate journeyDate, ...) {
 *     // Critical operation that needs locking
 * }
 * }
 * </pre>
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    
    /**
     * Lock key expression (SpEL supported)
     * Examples:
     * - "booking:#{#trainId}"
     * - "payment:#{#bookingId}"
     * - "seat:#{#trainId}:#{#date}"
     */
    String key();
    
    /**
     * Lock timeout in seconds
     * Default: 30 seconds
     */
    long timeout() default 30;
    
    /**
     * Maximum wait time in seconds to acquire lock
     * Default: 5 seconds
     */
    long waitTime() default 5;
    
    /**
     * Whether to throw exception if lock cannot be acquired
     * Default: true
     */
    boolean throwOnFailure() default true;
    
    /**
     * Error message when lock acquisition fails
     */
    String errorMessage() default "Failed to acquire distributed lock";
}

