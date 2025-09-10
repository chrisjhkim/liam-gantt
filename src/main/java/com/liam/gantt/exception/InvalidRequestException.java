package com.liam.gantt.exception;

/**
 * 잘못된 요청 데이터가 들어왔을 때 발생하는 예외
 */
public class InvalidRequestException extends BusinessException {
    
    public InvalidRequestException(String message) {
        super("INVALID_REQUEST", message);
    }
    
    public InvalidRequestException(String fieldName, String reason) {
        super("INVALID_REQUEST", 
              String.format("Invalid value for field '%s': %s", fieldName, reason));
    }
}