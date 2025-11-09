package com.irctc.booking.versioning;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API Version Annotation
 * 
 * Marks controller methods or classes with API version information
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersion {
    
    /**
     * API version (e.g., "v1", "v2")
     */
    String value();
    
    /**
     * Whether this version is deprecated
     */
    boolean deprecated() default false;
    
    /**
     * Replacement version if deprecated
     */
    String replacement() default "";
}

