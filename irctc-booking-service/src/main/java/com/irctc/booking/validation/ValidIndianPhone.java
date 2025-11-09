package com.irctc.booking.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for Indian phone numbers
 * 
 * Validates that phone number is a valid 10-digit Indian mobile number
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IndianPhoneValidator.class)
@Documented
public @interface ValidIndianPhone {
    
    String message() default "Invalid Indian phone number. Must be a 10-digit number starting with 6-9.";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}

