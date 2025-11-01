package com.irctc.booking.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for automatic audit logging.
 * The audit aspect will intercept methods annotated with @Auditable
 * and log audit information to the audit_logs table.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    /**
     * The type of entity being audited (e.g., "Booking", "Payment")
     */
    String entityType();
    
    /**
     * The action being performed (e.g., "CREATE", "UPDATE", "DELETE", "READ")
     */
    String action();
    
    /**
     * Whether to log the request body (default: true)
     */
    boolean logRequestBody() default true;
    
    /**
     * Whether to log the response body (default: false)
     */
    boolean logResponseBody() default false;
}

