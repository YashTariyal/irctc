package com.irctc.booking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Indian Phone Number Validator
 * 
 * Validates Indian mobile numbers (10 digits starting with 6-9)
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class IndianPhoneValidator implements ConstraintValidator<ValidIndianPhone, String> {

    private static final Pattern INDIAN_PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
    
    @Override
    public void initialize(ValidIndianPhone constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        
        // Remove any non-digit characters
        String cleaned = phone.replaceAll("[^0-9]", "");
        
        // Check if it's a 10-digit number starting with 6-9
        return INDIAN_PHONE_PATTERN.matcher(cleaned).matches();
    }
}

