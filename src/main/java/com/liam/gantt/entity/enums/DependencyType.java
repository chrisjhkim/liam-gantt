package com.liam.gantt.entity.enums;

/**
 * 태스크 의존성 유형을 나타내는 Enum
 */
public enum DependencyType {
    FINISH_TO_START("FS", "완료-시작"),      // 선행 작업 완료 후 후행 작업 시작
    START_TO_START("SS", "시작-시작"),       // 선행 작업 시작과 동시에 후행 작업 시작
    FINISH_TO_FINISH("FF", "완료-완료"),     // 선행 작업 완료와 동시에 후행 작업 완료
    START_TO_FINISH("SF", "시작-완료");      // 선행 작업 시작 후 후행 작업 완료
    
    private final String code;
    private final String description;
    
    DependencyType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}