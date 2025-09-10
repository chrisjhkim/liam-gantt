package com.liam.gantt.exception;

public class ProjectNotFoundException extends RuntimeException {
    
    public ProjectNotFoundException(String message) {
        super(message);
    }
    
    public ProjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ProjectNotFoundException(Long projectId) {
        super("프로젝트를 찾을 수 없습니다. ID: " + projectId);
    }
}