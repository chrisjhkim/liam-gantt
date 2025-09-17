package com.liam.gantt.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 간트 차트 데이터 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class GanttChartDto {
    
    private ProjectResponseDto project;
    
    @Builder.Default
    private List<TaskResponseDto> tasks = new ArrayList<>();
    
    @Builder.Default
    private List<TaskDependencyResponseDto> dependencies = new ArrayList<>();
    
    private TimelineInfo timeline;
    
    @Builder.Default
    private List<TaskResponseDto> criticalPath = new ArrayList<>();
    
    /**
     * 타임라인 정보 내부 클래스
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimelineInfo {
        private LocalDate startDate;
        private LocalDate endDate;
        private Long totalDays;
        private Long workingDays;
        private LocalDate currentDate;
        
        public static TimelineInfo of(LocalDate start, LocalDate end) {
            long totalDays = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
            return TimelineInfo.builder()
                    .startDate(start)
                    .endDate(end)
                    .totalDays(totalDays)
                    .workingDays(totalDays) // 주말 제외 로직은 추후 구현
                    .currentDate(LocalDate.now())
                    .build();
        }
    }

    /**
     * 프로젝트 통계 정보 내부 클래스
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Statistics {
        private int totalTasks;
        private int completedTasks;
        private int inProgressTasks;
        private int overdueTasks;
        private double completionRate;
    }
}