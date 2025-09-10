package com.liam.gantt.dto.response;

import com.liam.gantt.entity.enums.ProjectStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 프로젝트 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class ProjectResponseDto {
    
    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatus status;
    private Double progress;
    private Integer taskCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 계산된 필드들
    private Long durationInDays;
    private List<TaskResponseDto> tasks;
    private Double averageProgress;
    private Boolean isOverdue;
    
    /**
     * 간단한 응답용 정적 팩토리 메서드
     */
    public static ProjectResponseDto simple(Long id, String name, ProjectStatus status) {
        return ProjectResponseDto.builder()
                .id(id)
                .name(name)
                .status(status)
                .build();
    }
}