package com.liam.gantt.service.impl;

import com.liam.gantt.dto.request.TaskDependencyRequestDto;
import com.liam.gantt.dto.response.GanttChartDto;
import com.liam.gantt.dto.response.ProjectResponseDto;
import com.liam.gantt.dto.response.TaskDependencyResponseDto;
import com.liam.gantt.dto.response.TaskResponseDto;
import com.liam.gantt.entity.Project;
import com.liam.gantt.entity.Task;
import com.liam.gantt.entity.TaskDependency;
import com.liam.gantt.exception.InvalidRequestException;
import com.liam.gantt.exception.ProjectNotFoundException;
import com.liam.gantt.exception.TaskNotFoundException;
import com.liam.gantt.repository.ProjectRepository;
import com.liam.gantt.repository.TaskDependencyRepository;
import com.liam.gantt.repository.TaskRepository;
import com.liam.gantt.service.GanttService;
import com.liam.gantt.service.ProjectService;
import com.liam.gantt.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 간트 차트 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GanttServiceImpl implements GanttService {
    
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskDependencyRepository dependencyRepository;
    private final ProjectService projectService;
    private final TaskService taskService;
    
    @Override
    public GanttChartDto getGanttChart(Long projectId) {
        log.info("간트 차트 데이터 조회: projectId={}", projectId);
        
        // 프로젝트 조회
        Project project = projectRepository.findByIdWithTasks(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("프로젝트를 찾을 수 없습니다: " + projectId));
        
        // 프로젝트 DTO 생성
        ProjectResponseDto projectDto = projectService.findById(projectId);
        
        // 태스크 계층구조 조회
        List<TaskResponseDto> tasks = taskService.findTaskHierarchyByProjectId(projectId);
        
        // 의존성 조회
        List<TaskDependencyResponseDto> dependencies = getProjectDependencies(projectId);
        
        // 타임라인 정보 생성
        GanttChartDto.TimelineInfo timeline = GanttChartDto.TimelineInfo.of(
                project.getStartDate(),
                project.getEndDate()
        );
        
        // 임계 경로 계산
        List<Long> criticalPathIds = calculateCriticalPath(projectId);
        List<TaskResponseDto> criticalPath = tasks.stream()
                .filter(task -> criticalPathIds.contains(task.getId()))
                .collect(Collectors.toList());
        
        return GanttChartDto.builder()
                .project(projectDto)
                .tasks(tasks)
                .dependencies(dependencies)
                .timeline(timeline)
                .criticalPath(criticalPath)
                .build();
    }
    
    @Override
    @Transactional
    public TaskDependencyResponseDto addDependency(TaskDependencyRequestDto requestDto) {
        log.info("태스크 의존성 추가: {} -> {}", 
                requestDto.getPredecessorId(), requestDto.getSuccessorId());
        
        // 자기 참조 체크
        if (requestDto.isSelfReference()) {
            throw new InvalidRequestException("태스크는 자기 자신에 의존할 수 없습니다");
        }
        
        // 태스크 존재 확인
        Task predecessor = taskRepository.findById(requestDto.getPredecessorId())
                .orElseThrow(() -> new TaskNotFoundException("선행 태스크를 찾을 수 없습니다: " + requestDto.getPredecessorId()));
        
        Task successor = taskRepository.findById(requestDto.getSuccessorId())
                .orElseThrow(() -> new TaskNotFoundException("후행 태스크를 찾을 수 없습니다: " + requestDto.getSuccessorId()));
        
        // 같은 프로젝트 소속인지 확인
        if (!predecessor.getProject().getId().equals(successor.getProject().getId())) {
            throw new InvalidRequestException("다른 프로젝트의 태스크 간에는 의존성을 설정할 수 없습니다");
        }
        
        // 중복 의존성 체크
        if (dependencyRepository.existsByPredecessorIdAndSuccessorId(
                requestDto.getPredecessorId(), requestDto.getSuccessorId())) {
            throw new InvalidRequestException("이미 존재하는 의존성입니다");
        }
        
        // 순환 의존성 체크
        if (hasCircularDependency(requestDto.getPredecessorId(), requestDto.getSuccessorId())) {
            throw new InvalidRequestException("순환 의존성이 발생합니다");
        }
        
        // 의존성 생성
        TaskDependency dependency = TaskDependency.builder()
                .predecessor(predecessor)
                .successor(successor)
                .dependencyType(requestDto.getDependencyType())
                .lagDays(requestDto.getLagDays())
                .build();
        
        TaskDependency savedDependency = dependencyRepository.save(dependency);
        log.info("태스크 의존성 추가 완료: id={}", savedDependency.getId());
        
        return convertToDto(savedDependency);
    }
    
    @Override
    @Transactional
    public void removeDependency(Long dependencyId) {
        log.info("태스크 의존성 제거: id={}", dependencyId);
        
        if (!dependencyRepository.existsById(dependencyId)) {
            throw new InvalidRequestException("태스크 의존성을 찾을 수 없습니다: " + dependencyId);
        }
        
        dependencyRepository.deleteById(dependencyId);
        log.info("태스크 의존성 제거 완료: id={}", dependencyId);
    }
    
    @Override
    public List<TaskDependencyResponseDto> getTaskDependencies(Long taskId) {
        log.debug("태스크 의존성 조회: taskId={}", taskId);
        
        List<TaskDependency> dependencies = dependencyRepository.findAllByTaskId(taskId);
        return dependencies.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TaskDependencyResponseDto> getProjectDependencies(Long projectId) {
        log.debug("프로젝트 의존성 조회: projectId={}", projectId);
        
        List<TaskDependency> dependencies = dependencyRepository.findByProjectId(projectId);
        return dependencies.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Long> calculateCriticalPath(Long projectId) {
        log.info("임계 경로 계산: projectId={}", projectId);
        
        // 간단한 구현 - 의존성이 있는 태스크들만 반환
        // 실제로는 CPM (Critical Path Method) 알고리즘 구현 필요
        List<Task> criticalTasks = taskRepository.findCriticalPathTasks(projectId);
        
        return criticalTasks.stream()
                .map(Task::getId)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean hasCircularDependency(Long predecessorId, Long successorId) {
        log.debug("순환 의존성 체크: {} -> {}", predecessorId, successorId);
        
        // successorId에서 도달 가능한 모든 후행 태스크 조회
        List<Long> reachableIds = dependencyRepository.findReachableSuccessorIds(successorId);
        
        // predecessorId가 도달 가능한 태스크에 포함되면 순환 의존성
        return reachableIds.contains(predecessorId);
    }
    
    @Override
    @Transactional
    public void recalculateProjectSchedule(Long projectId) {
        log.info("프로젝트 일정 재계산: projectId={}", projectId);
        
        // 프로젝트의 모든 태스크 조회
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        
        // 의존성 기반으로 태스크 일정 재계산
        // 실제 구현은 복잡한 스케줄링 알고리즘 필요
        // Forward Pass, Backward Pass 등
        
        log.info("프로젝트 일정 재계산 완료: projectId={}", projectId);
    }

    @Override
    public TaskDependencyResponseDto addTaskDependency(TaskDependencyRequestDto requestDto) {
        return addDependency(requestDto);
    }

    @Override
    public void removeTaskDependency(Long dependencyId) {
        removeDependency(dependencyId);
    }

    @Override
    @Transactional
    public TaskDependencyResponseDto updateTaskDependency(Long dependencyId, TaskDependencyRequestDto requestDto) {
        log.info("태스크 의존성 업데이트: id={}", dependencyId);

        TaskDependency dependency = dependencyRepository.findById(dependencyId)
                .orElseThrow(() -> new InvalidRequestException("태스크 의존성을 찾을 수 없습니다: " + dependencyId));

        // 유효성 검증
        if (requestDto.isSelfReference()) {
            throw new InvalidRequestException("태스크는 자기 자신에 의존할 수 없습니다");
        }

        // 순환 의존성 체크
        if (hasCircularDependency(requestDto.getPredecessorId(), requestDto.getSuccessorId())) {
            throw new InvalidRequestException("순환 의존성이 감지되었습니다");
        }

        // 태스크 조회
        Task predecessor = taskRepository.findById(requestDto.getPredecessorId())
                .orElseThrow(() -> new TaskNotFoundException("선행 태스크를 찾을 수 없습니다: " + requestDto.getPredecessorId()));
        Task successor = taskRepository.findById(requestDto.getSuccessorId())
                .orElseThrow(() -> new TaskNotFoundException("후행 태스크를 찾을 수 없습니다: " + requestDto.getSuccessorId()));

        // 의존성 업데이트
        dependency.setPredecessor(predecessor);
        dependency.setSuccessor(successor);
        dependency.setDependencyType(requestDto.getDependencyType());
        dependency.setLagDays(requestDto.getLagDays());

        log.info("태스크 의존성 업데이트 완료: id={}", dependencyId);
        return convertToDto(dependency);
    }

    @Override
    public GanttChartDto.Statistics calculateStatistics(Long projectId) {
        log.debug("프로젝트 통계 계산: projectId={}", projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("프로젝트를 찾을 수 없습니다: " + projectId));

        List<Task> tasks = taskRepository.findByProjectId(projectId);

        // 통계 계산
        int totalTasks = tasks.size();
        int completedTasks = (int) tasks.stream().filter(Task::isCompleted).count();
        int overdueTasks = (int) tasks.stream().filter(Task::isOverdue).count();

        return GanttChartDto.Statistics.builder()
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .inProgressTasks(totalTasks - completedTasks)
                .overdueTasks(overdueTasks)
                .completionRate(totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0.0)
                .build();
    }

    /**
     * Entity를 DTO로 변환
     */
    private TaskDependencyResponseDto convertToDto(TaskDependency dependency) {
        return TaskDependencyResponseDto.builder()
                .id(dependency.getId())
                .predecessorId(dependency.getPredecessor().getId())
                .predecessorName(dependency.getPredecessor().getName())
                .successorId(dependency.getSuccessor().getId())
                .successorName(dependency.getSuccessor().getName())
                .dependencyType(dependency.getDependencyType())
                .dependencyTypeCode(dependency.getDependencyType().getCode())
                .dependencyTypeDescription(dependency.getDependencyType().getDescription())
                .lagDays(dependency.getLagDays())
                .createdAt(dependency.getCreatedAt())
                .build();
    }
}