package com.liam.gantt.exception;

import com.liam.gantt.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProjectNotFoundException(ProjectNotFoundException ex) {
        log.warn("프로젝트 조회 실패: {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("PROJECT_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleTaskNotFoundException(TaskNotFoundException ex) {
        log.warn("태스크 조회 실패: {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("TASK_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DuplicateProjectNameException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateProjectNameException(DuplicateProjectNameException ex) {
        log.warn("중복된 프로젝트명: {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("PROJECT_NAME_DUPLICATE", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidProjectDateException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidProjectDateException(InvalidProjectDateException ex) {
        log.warn("잘못된 프로젝트 날짜: {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("PROJECT_DATE_INVALID", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("입력값 검증 실패: {}", ex.getMessage());

        String fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ApiResponse<Void> response = ApiResponse.error("VALIDATION_ERROR", "입력값이 올바르지 않습니다: " + fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException ex) {
        log.warn("바인딩 오류: {}", ex.getMessage());

        String fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ApiResponse<Void> response = ApiResponse.error("BINDING_ERROR", "요청 데이터 바인딩에 실패했습니다: " + fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.warn("타입 불일치 오류: {}", ex.getMessage());
        
        String message = String.format("잘못된 파라미터 타입입니다. '%s'는 %s 타입이어야 합니다.", 
                ex.getName(), ex.getRequiredType().getSimpleName());
        
        ApiResponse<Void> response = ApiResponse.error("TYPE_MISMATCH", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("데이터 무결성 위반: {}", ex.getMessage());
        
        String message = "데이터 제약 조건을 위반했습니다.";
        if (ex.getMessage().contains("Duplicate entry")) {
            message = "중복된 데이터입니다.";
        } else if (ex.getMessage().contains("foreign key constraint")) {
            message = "참조 무결성 제약을 위반했습니다.";
        }
        
        ApiResponse<Void> response = ApiResponse.error("DATA_INTEGRITY_VIOLATION", message);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("잘못된 인자: {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("INVALID_ARGUMENT", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("예상치 못한 오류 발생: {}", ex.getMessage(), ex);
        
        ApiResponse<Void> response = ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}