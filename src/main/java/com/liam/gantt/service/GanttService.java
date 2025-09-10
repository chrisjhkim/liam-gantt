package com.liam.gantt.service;

import com.liam.gantt.dto.request.TaskDependencyRequestDto;
import com.liam.gantt.dto.response.GanttChartDto;
import com.liam.gantt.dto.response.TaskDependencyResponseDto;

import java.util.List;

/**
 * 간트 차트 관련 비즈니스 로직 인터페이스
 */
public interface GanttService {
    
    /**
     * 프로젝트의 간트 차트 데이터 조회
     */
    GanttChartDto getGanttChart(Long projectId);
    
    /**
     * 태스크 의존성 추가
     */
    TaskDependencyResponseDto addDependency(TaskDependencyRequestDto requestDto);
    
    /**
     * 태스크 의존성 제거
     */
    void removeDependency(Long dependencyId);
    
    /**
     * 태스크의 모든 의존성 조회
     */
    List<TaskDependencyResponseDto> getTaskDependencies(Long taskId);
    
    /**
     * 프로젝트의 모든 의존성 조회
     */
    List<TaskDependencyResponseDto> getProjectDependencies(Long projectId);
    
    /**
     * 임계 경로 계산
     */
    List<Long> calculateCriticalPath(Long projectId);
    
    /**
     * 순환 의존성 체크
     */
    boolean hasCircularDependency(Long predecessorId, Long successorId);
    
    /**
     * 프로젝트 일정 재계산
     */
    void recalculateProjectSchedule(Long projectId);
}