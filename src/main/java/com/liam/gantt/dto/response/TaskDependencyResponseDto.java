package com.liam.gantt.dto.response;

import com.liam.gantt.entity.enums.DependencyType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 태스크 의존성 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TaskDependencyResponseDto {
    
    private Long id;
    private Long predecessorId;
    private String predecessorName;
    private Long successorId;
    private String successorName;
    private DependencyType dependencyType;
    private String dependencyTypeCode;
    private String dependencyTypeDescription;
    private Integer lagDays;
    private LocalDateTime createdAt;
    
    /**
     * 간트 차트용 간단한 응답
     */
    public static TaskDependencyResponseDto forGanttChart(
            Long predecessorId,
            Long successorId,
            DependencyType type) {
        return TaskDependencyResponseDto.builder()
                .predecessorId(predecessorId)
                .successorId(successorId)
                .dependencyType(type)
                .dependencyTypeCode(type.getCode())
                .build();
    }
}