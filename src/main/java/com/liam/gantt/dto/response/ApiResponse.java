package com.liam.gantt.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 통일된 API 응답 래퍼 클래스
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ApiResponse<T> {
    
    @Builder.Default
    private String status = "success";
    
    private T data;
    
    private String message;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private ErrorDetails error;
    
    /**
     * 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 메시지와 함께 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .status("success")
                .data(data)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 에러 응답 생성
     */
    public static <T> ApiResponse<T> error(String errorCode, String errorMessage) {
        return ApiResponse.<T>builder()
                .status("error")
                .error(ErrorDetails.of(errorCode, errorMessage))
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 에러 상세 정보 내부 클래스
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorDetails {
        private String code;
        private String message;
        private String field;
        
        public static ErrorDetails of(String code, String message) {
            return ErrorDetails.builder()
                    .code(code)
                    .message(message)
                    .build();
        }
        
        public static ErrorDetails of(String code, String message, String field) {
            return ErrorDetails.builder()
                    .code(code)
                    .message(message)
                    .field(field)
                    .build();
        }
    }
}