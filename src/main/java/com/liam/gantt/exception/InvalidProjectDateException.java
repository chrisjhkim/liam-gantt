package com.liam.gantt.exception;

public class InvalidProjectDateException extends RuntimeException {
    
    public InvalidProjectDateException(String message) {
        super(message);
    }
    
    public InvalidProjectDateException(String message, Throwable cause) {
        super(message, cause);
    }
}