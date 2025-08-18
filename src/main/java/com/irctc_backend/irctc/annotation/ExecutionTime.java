package com.irctc_backend.irctc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to mark methods for execution time measurement.
 * This annotation can be applied to methods that need to be timed.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecutionTime {
    /**
     * Optional description for the timed operation.
     * @return description of the operation being timed
     */
    String value() default "";
}
