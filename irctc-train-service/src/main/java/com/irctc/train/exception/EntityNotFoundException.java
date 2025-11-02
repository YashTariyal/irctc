package com.irctc.train.exception;

public class EntityNotFoundException extends RuntimeException {
    private final String errorCode = "ENTITY_NOT_FOUND";
    
    public EntityNotFoundException(String message) {
        super(message);
    }
    
    public EntityNotFoundException(String entityName, Long id) {
        super(String.format("%s with id %d not found", entityName, id));
    }
}

