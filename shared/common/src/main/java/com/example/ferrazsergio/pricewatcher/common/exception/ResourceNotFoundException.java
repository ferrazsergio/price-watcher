package com.example.ferrazsergio.pricewatcher.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception for resource not found scenarios
 */
public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
    
    public ResourceNotFoundException(String resourceType, Object id) {
        super(String.format("%s with id '%s' not found", resourceType, id), 
              "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}