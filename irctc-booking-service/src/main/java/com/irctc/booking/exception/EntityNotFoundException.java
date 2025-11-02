package com.irctc.booking.exception;

/**
 * Exception thrown when an entity is not found
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class EntityNotFoundException extends CustomException {
    
    public EntityNotFoundException(String message) {
        super(message, "ENTITY_NOT_FOUND");
    }
    
    public EntityNotFoundException(String entityName, Long id) {
        super(String.format("%s with id %d not found", entityName, id), "ENTITY_NOT_FOUND");
    }
    
    public EntityNotFoundException(String entityName, String identifier) {
        super(String.format("%s with identifier %s not found", entityName, identifier), "ENTITY_NOT_FOUND");
    }
}

