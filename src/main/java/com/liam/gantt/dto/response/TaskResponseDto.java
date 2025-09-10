package com.liam.gantt.dto.response;

import com.liam.gantt.entity.enums.TaskStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 태스크 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class TaskResponseDto {
    
    private Long id;
    private Long projectId;
    private String projectName;
    private Long parentTaskId;
    private String parentTaskName;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer duration;
    private BigDecimal progress;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 계층 구조 정보
    private Integer level;
    private Boolean isLeaf;
    @Builder.Default
    private List<TaskResponseDto> subTasks = new ArrayList<>();
    
    // 의존성 정보
    @Builder.Default
    private List<TaskDependencyResponseDto> dependencies = new ArrayList<>();
    
    // 계산된 필드들
    private Boolean isOverdue;
    private Boolean isCompleted;
    
    /**
     * 간트 차트용 간단한 응답
     */
    public static TaskResponseDto forGanttChart(
            Long id, 
            String name, 
            LocalDate startDate, 
            LocalDate endDate, 
            BigDecimal progress) {
        return TaskResponseDto.builder()
                .id(id)
                .name(name)
                .startDate(startDate)
                .endDate(endDate)
                .progress(progress)
                .build();
    }
}