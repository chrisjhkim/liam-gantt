package com.liam.gantt.exception;

public class DuplicateProjectNameException extends RuntimeException {
    
    public DuplicateProjectNameException(String message) {
        super(message);
    }
    
    public DuplicateProjectNameException(String message, String projectName) {
        super(message + ": " + projectName);
    }
}