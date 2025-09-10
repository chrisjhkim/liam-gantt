package com.liam.gantt.entity.enums;

/**
 * 프로젝트 상태를 나타내는 Enum
 */
public enum ProjectStatus {
    PLANNING("계획 중"),           // 프로젝트 계획 단계
    IN_PROGRESS("진행 중"),        // 프로젝트 진행 중
    COMPLETED("완료"),             // 프로젝트 완료
    ON_HOLD("보류"),               // 프로젝트 일시 중단
    CANCELLED("취소");             // 프로젝트 취소
    
    private final String description;
    
    ProjectStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}