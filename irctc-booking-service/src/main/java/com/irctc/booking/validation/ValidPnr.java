package com.irctc.booking.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for PNR numbers
 * 
 * Validates that PNR number follows IRCTC format (10 alphanumeric characters)
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PnrValidator.class)
@Documented
public @interface ValidPnr {
    
    String message() default "Invalid PNR number format. PNR must be 10 alphanumeric characters.";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}

