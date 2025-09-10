package com.liam.gantt.exception;

/**
 * 비즈니스 로직 예외 기본 클래스
 */
public class BusinessException extends RuntimeException {
    
    private String errorCode;
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}