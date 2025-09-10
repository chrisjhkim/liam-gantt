package com.liam.gantt.exception;

public class TaskNotFoundException extends RuntimeException {
    
    public TaskNotFoundException(String message) {
        super(message);
    }
    
    public TaskNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public TaskNotFoundException(Long taskId) {
        super("태스크를 찾을 수 없습니다. ID: " + taskId);
    }
}