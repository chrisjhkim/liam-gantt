package com.liam.gantt.exception;

/**
 * 중복된 리소스가 존재할 때 발생하는 예외
 */
public class DuplicateResourceException extends BusinessException {
    
    public DuplicateResourceException(String message) {
        super("DUPLICATE_RESOURCE", message);
    }
    
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super("DUPLICATE_RESOURCE", 
              String.format("%s already exists with %s : '%s'", resourceName, fieldName, fieldValue));
    }
}