package com.liam.gantt.entity.enums;

/**
 * 태스크 상태를 나타내는 Enum
 */
public enum TaskStatus {
    NOT_STARTED("시작 전"),        // 아직 시작하지 않은 태스크
    IN_PROGRESS("진행 중"),        // 현재 진행 중인 태스크
    COMPLETED("완료"),             // 완료된 태스크
    ON_HOLD("보류"),               // 일시 중단된 태스크
    CANCELLED("취소");             // 취소된 태스크
    
    private final String description;
    
    TaskStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}