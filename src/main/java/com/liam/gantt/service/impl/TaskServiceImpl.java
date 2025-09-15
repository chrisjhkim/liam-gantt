package com.liam.gantt.service.impl;

import com.liam.gantt.dto.request.TaskRequestDto;
import com.liam.gantt.dto.response.TaskResponseDto;
import com.liam.gantt.entity.Project;
import com.liam.gantt.entity.Task;
import com.liam.gantt.entity.enums.TaskStatus;
import com.liam.gantt.exception.TaskNotFoundException;
import com.liam.gantt.exception.ProjectNotFoundException;
import com.liam.gantt.mapper.TaskMapper;
import com.liam.gantt.repository.ProjectRepository;
import com.liam.gantt.repository.TaskDependencyRepository;
import com.liam.gantt.repository.TaskRepository;
import com.liam.gantt.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 태스크 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TaskServiceImpl implements TaskService {
    
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TaskDependencyRepository dependencyRepository;
    private final TaskMapper taskMapper;
    
    @Override
    @Transactional
    public TaskResponseDto create(Long projectId, TaskRequestDto requestDto) {
        log.info("태스크 생성 시작: projectId={}, name={}", projectId, requestDto.getName());
        
        // 프로젝트 존재 확인
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("프로젝트를 찾을 수 없습니다: " + projectId));
        
        // 날짜 유효성 검증
        if (!requestDto.isValidDateRange()) {
            throw new IllegalArgumentException("종료일은 시작일보다 같거나 늦어야 합니다");
        }
        
        // 상위 태스크 확인 (있는 경우)
        Task parentTask = null;
        if (requestDto.getParentTaskId() != null) {
            parentTask = taskRepository.findById(requestDto.getParentTaskId())
                    .orElseThrow(() -> new TaskNotFoundException("상위 태스크를 찾을 수 없습니다: " + requestDto.getParentTaskId()));
            
            // 상위 태스크가 같은 프로젝트 소속인지 확인
            if (!parentTask.getProject().getId().equals(projectId)) {
                throw new IllegalArgumentException("상위 태스크는 같은 프로젝트에 속해야 합니다");
            }
        }
        
        // 엔티티 생성 (매퍼 사용)
        Task task = taskMapper.toEntity(requestDto);
        task.setProject(project);
        task.setParentTask(parentTask);
        
        Task savedTask = taskRepository.save(task);
        log.info("태스크 생성 완료: id={}, name={}", savedTask.getId(), savedTask.getName());
        
        return taskMapper.toResponseDto(savedTask);
    }
    
    @Override
    public TaskResponseDto findById(Long id) {
        log.debug("태스크 조회: id={}", id);
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("태스크를 찾을 수 없습니다: " + id));
        
        return taskMapper.toResponseDto(task);
    }
    
    @Override
    public List<TaskResponseDto> findByProjectId(Long projectId) {
        log.debug("프로젝트의 태스크 조회: projectId={}", projectId);
        
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return tasks.stream()
                .map(taskMapper::toResponseDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<TaskResponseDto> findByProjectIdWithPaging(Long projectId, Pageable pageable) {
        log.debug("프로젝트의 태스크 페이징 조회: projectId={}", projectId);
        
        Page<Task> tasks = taskRepository.findByProjectId(projectId, pageable);
        return tasks.map(taskMapper::toResponseDto);
    }
    
    @Override
    @Transactional
    public TaskResponseDto update(Long id, TaskRequestDto requestDto) {
        log.info("태스크 수정: id={}", id);
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("태스크를 찾을 수 없습니다: " + id));
        
        // 날짜 유효성 검증
        if (!requestDto.isValidDateRange()) {
            throw new IllegalArgumentException("종료일은 시작일보다 같거나 늦어야 합니다");
        }
        
        // 엔티티 업데이트 (매퍼 사용)
        taskMapper.updateEntity(task, requestDto);
        
        Task updatedTask = taskRepository.save(task);
        log.info("태스크 수정 완료: id={}", id);
        
        return taskMapper.toResponseDto(updatedTask);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        log.info("태스크 삭제: id={}", id);
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("태스크를 찾을 수 없습니다: " + id));
        
        // 하위 태스크가 있는 경우 삭제 불가
        if (!task.getSubTasks().isEmpty()) {
            throw new IllegalArgumentException("하위 태스크가 있는 태스크는 삭제할 수 없습니다");
        }
        
        // 의존성 제거
        dependencyRepository.deleteByPredecessorIdOrSuccessorId(id, id);
        
        taskRepository.deleteById(id);
        log.info("태스크 삭제 완료: id={}", id);
    }
    
    @Override
    @Transactional
    public TaskResponseDto updateProgress(Long id, BigDecimal progress) {
        log.info("태스크 진행률 업데이트: id={}, progress={}%", id, progress);
        
        if (progress.compareTo(BigDecimal.ZERO) < 0 || progress.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("진행률은 0-100 사이여야 합니다");
        }
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("태스크를 찾을 수 없습니다: " + id));
        
        task.setProgress(progress);
        updateTaskStatusByProgress(task);
        
        Task updatedTask = taskRepository.save(task);
        log.info("태스크 진행률 업데이트 완료: id={}, progress={}%", id, progress);
        
        return taskMapper.toResponseDto(updatedTask);
    }
    
    @Override
    @Transactional
    public TaskResponseDto updateStatus(Long id, TaskStatus status) {
        log.info("태스크 상태 변경: id={}, status={}", id, status);
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("태스크를 찾을 수 없습니다: " + id));
        
        task.setStatus(status);
        
        // 상태에 따른 진행률 자동 조정
        if (status == TaskStatus.COMPLETED) {
            task.setProgress(BigDecimal.valueOf(100));
        } else if (status == TaskStatus.NOT_STARTED) {
            task.setProgress(BigDecimal.ZERO);
        }
        
        Task updatedTask = taskRepository.save(task);
        log.info("태스크 상태 변경 완료: id={}, status={}", id, status);
        
        return taskMapper.toResponseDto(updatedTask);
    }
    
    @Override
    @Transactional
    public TaskResponseDto addSubTask(Long parentTaskId, TaskRequestDto requestDto) {
        log.info("하위 태스크 추가: parentTaskId={}", parentTaskId);
        
        Task parentTask = taskRepository.findById(parentTaskId)
                .orElseThrow(() -> new TaskNotFoundException("상위 태스크를 찾을 수 없습니다: " + parentTaskId));
        
        requestDto.setParentTaskId(parentTaskId);
        return create(parentTask.getProject().getId(), requestDto);
    }
    
    @Override
    public List<TaskResponseDto> findRootTasks(Long projectId) {
        log.debug("루트 태스크 조회: projectId={}", projectId);
        
        List<Task> tasks = taskRepository.findByProjectIdAndParentTaskIsNull(projectId);
        return buildTaskHierarchy(tasks);
    }
    
    @Override
    public List<TaskResponseDto> findTaskHierarchyByProjectId(Long projectId) {
        log.debug("태스크 계층구조 조회: projectId={}", projectId);
        
        List<Task> rootTasks = taskRepository.findByProjectIdAndParentTaskIsNull(projectId);
        return buildTaskHierarchy(rootTasks);
    }
    
    @Override
    public List<TaskResponseDto> findOverdueTasks(Long projectId) {
        log.debug("지연된 태스크 조회: projectId={}", projectId);
        
        List<Task> tasks = taskRepository.findOverdueTasks(projectId, LocalDate.now());
        return tasks.stream()
                .map(taskMapper::toResponseDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public TaskResponseDto moveTask(Long id, Integer dayOffset) {
        log.info("태스크 이동: id={}, dayOffset={}", id, dayOffset);
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("태스크를 찾을 수 없습니다: " + id));
        
        LocalDate newStartDate = task.getStartDate().plusDays(dayOffset);
        LocalDate newEndDate = task.getEndDate().plusDays(dayOffset);
        
        task.setStartDate(newStartDate);
        task.setEndDate(newEndDate);
        
        Task updatedTask = taskRepository.save(task);
        log.info("태스크 이동 완료: id={}, 새 기간={} ~ {}", id, newStartDate, newEndDate);
        
        return taskMapper.toResponseDto(updatedTask);
    }
    
    /**
     * 진행률에 따른 상태 자동 업데이트
     */
    @Override
    public TaskResponseDto findByIdWithDependencies(Long id) {
        log.debug("의존성 포함 태스크 조회: id={}", id);
        
        Task task = taskRepository.findByIdWithDependencies(id)
                .orElseThrow(() -> new TaskNotFoundException("태스크를 찾을 수 없습니다: " + id));
        
        return convertToDtoWithDependencies(task);
    }

    private void updateTaskStatusByProgress(Task task) {
        BigDecimal progress = task.getProgress();
        
        if (progress.compareTo(BigDecimal.ZERO) == 0) {
            task.setStatus(TaskStatus.NOT_STARTED);
        } else if (progress.compareTo(BigDecimal.valueOf(100)) == 0) {
            task.setStatus(TaskStatus.COMPLETED);
        } else {
            if (task.getStatus() == TaskStatus.NOT_STARTED || 
                task.getStatus() == TaskStatus.COMPLETED) {
                task.setStatus(TaskStatus.IN_PROGRESS);
            }
        }
    }
    
    /**
     * 태스크 계층구조 빌드
     */
    private List<TaskResponseDto> buildTaskHierarchy(List<Task> rootTasks) {
        List<TaskResponseDto> result = new ArrayList<>();
        
        for (Task rootTask : rootTasks) {
            TaskResponseDto dto = taskMapper.toResponseDto(rootTask);
            dto.setLevel(0);
            dto.setSubTasks(buildSubTaskHierarchy(rootTask.getSubTasks(), 1));
            result.add(dto);
        }
        
        return result;
    }
    
    /**
     * 하위 태스크 계층구조 빌드 (재귀)
     */
    private List<TaskResponseDto> buildSubTaskHierarchy(List<Task> subTasks, int level) {
        List<TaskResponseDto> result = new ArrayList<>();
        
        for (Task subTask : subTasks) {
            TaskResponseDto dto = taskMapper.toResponseDto(subTask);
            dto.setLevel(level);
            dto.setSubTasks(buildSubTaskHierarchy(subTask.getSubTasks(), level + 1));
            result.add(dto);
        }
        
        return result;
    }
    
    /**
     * Entity를 DTO로 변환
     */
    private TaskResponseDto convertToDto(Task task) {
        return TaskResponseDto.builder()
                .id(task.getId())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .parentTaskId(task.getParentTask() != null ? task.getParentTask().getId() : null)
                .name(task.getName())
                .description(task.getDescription())
                .startDate(task.getStartDate())
                .endDate(task.getEndDate())
                .duration(task.getDuration())
                .progress(task.getProgress())
                .status(task.getStatus())
                .level(task.getLevel())
                .isLeaf(task.isLeafTask())
                .isOverdue(task.isOverdue())
                .isCompleted(task.isCompleted())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
    
    /**
     * Entity를 DTO로 변환 (의존성 포함)
     */
    @Override
    public List<TaskResponseDto> search(Long projectId, String name, String status) {
        log.debug("태스크 검색: projectId={}, name={}, status={}", projectId, name, status);
        
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        
        return tasks.stream()
                .filter(task -> name == null || task.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(task -> status == null || task.getStatus().name().equals(status))
                .map(taskMapper::toResponseDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TaskResponseDto> findByProjectIdAndStatus(Long projectId, TaskStatus status) {
        log.debug("프로젝트 ID와 상태별 태스크 조회: projectId={}, status={}", projectId, status);
        
        List<Task> tasks = taskRepository.findByProjectIdAndStatus(projectId, status);
        return tasks.stream()
                .map(taskMapper::toResponseDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TaskResponseDto> searchByName(Long projectId, String keyword) {
        log.debug("프로젝트 내 태스크 이름으로 검색: projectId={}, keyword={}", projectId, keyword);
        
        List<Task> tasks = taskRepository.findByProjectIdAndNameContainingIgnoreCase(projectId, keyword);
        return tasks.stream()
                .map(taskMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponseDto> findByParentTaskId(Long parentTaskId) {
        log.debug("부모 태스크 ID로 하위 태스크 조회: parentTaskId={}", parentTaskId);

        List<Task> tasks = taskRepository.findByParentTaskId(parentTaskId);
        return tasks.stream()
                .map(taskMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private TaskResponseDto convertToDtoWithDependencies(Task task) {
        TaskResponseDto dto = taskMapper.toResponseDto(task);

        // 의존성 정보는 별도 서비스에서 처리
        // GanttService에서 관리

        return dto;
    }
}