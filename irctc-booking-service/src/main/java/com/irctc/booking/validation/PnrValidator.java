package com.irctc.booking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * PNR Validator
 * 
 * Validates PNR number format (10 alphanumeric characters)
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class PnrValidator implements ConstraintValidator<ValidPnr, String> {

    private static final Pattern PNR_PATTERN = Pattern.compile("^[A-Z0-9]{10}$");
    
    @Override
    public void initialize(ValidPnr constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String pnr, ConstraintValidatorContext context) {
        if (pnr == null || pnr.isEmpty()) {
            return false;
        }
        
        return PNR_PATTERN.matcher(pnr.toUpperCase()).matches();
    }
}

