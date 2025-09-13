package com.liam.gantt.controller.api.v1;

import com.liam.gantt.dto.request.TaskRequestDto;
import com.liam.gantt.dto.response.TaskResponseDto;
import com.liam.gantt.entity.enums.TaskStatus;
import com.liam.gantt.service.TaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 태스크 관리 REST API 컨트롤러
 * 
 * @author Liam
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
@Slf4j
public class TaskController {

    private final TaskService taskService;

    /**
     * 프로젝트별 태스크 목록 조회
     */
    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<List<TaskResponseDto>> getTasksByProject(@PathVariable @Positive Long projectId) {
        log.info("프로젝트 태스크 목록 조회 요청 - projectId: {}", projectId);
        
        List<TaskResponseDto> tasks = taskService.findByProjectId(projectId);
        
        log.info("프로젝트 태스크 목록 조회 완료 - projectId: {}, 태스크 수: {}", projectId, tasks.size());
        return ResponseEntity.ok(tasks);
    }

    /**
     * 프로젝트별 태스크 목록 조회 (페이징)
     */
    @GetMapping("/projects/{projectId}/tasks/paged")
    public ResponseEntity<Page<TaskResponseDto>> getTasksByProjectWithPaging(
            @PathVariable @Positive Long projectId,
            @PageableDefault(size = 20, sort = "startDate") Pageable pageable) {
        log.info("프로젝트 태스크 페이징 조회 요청 - projectId: {}, page: {}, size: {}", 
                projectId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<TaskResponseDto> tasks = taskService.findByProjectIdWithPaging(projectId, pageable);
        
        log.info("프로젝트 태스크 페이징 조회 완료 - projectId: {}, 총 {}개, 현재 페이지 {}개", 
                projectId, tasks.getTotalElements(), tasks.getNumberOfElements());
        return ResponseEntity.ok(tasks);
    }

    /**
     * 태스크 상세 조회
     */
    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskResponseDto> getTask(@PathVariable @Positive Long id) {
        log.info("태스크 상세 조회 요청 - id: {}", id);
        
        TaskResponseDto task = taskService.findById(id);
        
        log.info("태스크 상세 조회 완료 - id: {}, name: {}", task.getId(), task.getName());
        return ResponseEntity.ok(task);
    }

    /**
     * 의존성 포함 태스크 상세 조회
     */
    @GetMapping("/tasks/{id}/with-dependencies")
    public ResponseEntity<TaskResponseDto> getTaskWithDependencies(@PathVariable @Positive Long id) {
        log.info("의존성 포함 태스크 조회 요청 - id: {}", id);
        
        TaskResponseDto task = taskService.findByIdWithDependencies(id);
        
        log.info("의존성 포함 태스크 조회 완료 - id: {}, name: {}", task.getId(), task.getName());
        return ResponseEntity.ok(task);
    }

    /**
     * 태스크 생성
     */
    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<TaskResponseDto> createTask(
            @PathVariable @Positive Long projectId,
            @Valid @RequestBody TaskRequestDto request) {
        log.info("태스크 생성 요청 - projectId: {}, name: {}", projectId, request.getName());
        
        TaskResponseDto createdTask = taskService.create(projectId, request);
        
        log.info("태스크 생성 완료 - id: {}, name: {}", createdTask.getId(), createdTask.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    /**
     * 태스크 수정
     */
    @PutMapping("/tasks/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable @Positive Long id,
            @Valid @RequestBody TaskRequestDto request) {
        log.info("태스크 수정 요청 - id: {}, name: {}", id, request.getName());
        
        TaskResponseDto updatedTask = taskService.update(id, request);
        
        log.info("태스크 수정 완료 - id: {}, name: {}", updatedTask.getId(), updatedTask.getName());
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * 태스크 삭제
     */
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable @Positive Long id) {
        log.info("태스크 삭제 요청 - id: {}", id);
        
        taskService.delete(id);
        
        log.info("태스크 삭제 완료 - id: {}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 태스크 진행률 업데이트
     */
    @PatchMapping("/tasks/{id}/progress")
    public ResponseEntity<TaskResponseDto> updateTaskProgress(
            @PathVariable @Positive Long id,
            @RequestParam BigDecimal progress) {
        log.info("태스크 진행률 업데이트 요청 - id: {}, progress: {}%", id, progress);
        
        TaskResponseDto updatedTask = taskService.updateProgress(id, progress);
        
        log.info("태스크 진행률 업데이트 완료 - id: {}, progress: {}%", updatedTask.getId(), updatedTask.getProgress());
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * 태스크 상태 변경
     */
    @PatchMapping("/tasks/{id}/status")
    public ResponseEntity<TaskResponseDto> updateTaskStatus(
            @PathVariable @Positive Long id,
            @RequestParam TaskStatus status) {
        log.info("태스크 상태 변경 요청 - id: {}, status: {}", id, status);
        
        TaskResponseDto updatedTask = taskService.updateStatus(id, status);
        
        log.info("태스크 상태 변경 완료 - id: {}, status: {}", updatedTask.getId(), updatedTask.getStatus());
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * 하위 태스크 추가
     */
    @PostMapping("/tasks/{parentTaskId}/subtasks")
    public ResponseEntity<TaskResponseDto> addSubTask(
            @PathVariable @Positive Long parentTaskId,
            @Valid @RequestBody TaskRequestDto request) {
        log.info("하위 태스크 추가 요청 - parentTaskId: {}, name: {}", parentTaskId, request.getName());
        
        TaskResponseDto createdSubTask = taskService.addSubTask(parentTaskId, request);
        
        log.info("하위 태스크 추가 완료 - id: {}, name: {}, parentId: {}", 
                createdSubTask.getId(), createdSubTask.getName(), parentTaskId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSubTask);
    }

    /**
     * 프로젝트 루트 태스크 목록 조회
     */
    @GetMapping("/projects/{projectId}/tasks/roots")
    public ResponseEntity<List<TaskResponseDto>> getRootTasks(@PathVariable @Positive Long projectId) {
        log.info("루트 태스크 목록 조회 요청 - projectId: {}", projectId);
        
        List<TaskResponseDto> rootTasks = taskService.findRootTasks(projectId);
        
        log.info("루트 태스크 목록 조회 완료 - projectId: {}, 루트 태스크 수: {}", projectId, rootTasks.size());
        return ResponseEntity.ok(rootTasks);
    }

    /**
     * 프로젝트 태스크 계층구조 조회
     */
    @GetMapping("/projects/{projectId}/tasks/hierarchy")
    public ResponseEntity<List<TaskResponseDto>> getTaskHierarchy(@PathVariable @Positive Long projectId) {
        log.info("태스크 계층구조 조회 요청 - projectId: {}", projectId);
        
        List<TaskResponseDto> taskHierarchy = taskService.findTaskHierarchyByProjectId(projectId);
        
        log.info("태스크 계층구조 조회 완료 - projectId: {}, 전체 태스크 수: {}", projectId, taskHierarchy.size());
        return ResponseEntity.ok(taskHierarchy);
    }

    /**
     * 지연된 태스크 조회
     */
    @GetMapping("/projects/{projectId}/tasks/overdue")
    public ResponseEntity<List<TaskResponseDto>> getOverdueTasks(@PathVariable @Positive Long projectId) {
        log.info("지연된 태스크 조회 요청 - projectId: {}", projectId);
        
        List<TaskResponseDto> overdueTasks = taskService.findOverdueTasks(projectId);
        
        log.info("지연된 태스크 조회 완료 - projectId: {}, 지연된 태스크 수: {}", projectId, overdueTasks.size());
        return ResponseEntity.ok(overdueTasks);
    }

    /**
     * 태스크 이동 (날짜 조정)
     */
    @PatchMapping("/tasks/{id}/move")
    public ResponseEntity<TaskResponseDto> moveTask(
            @PathVariable @Positive Long id,
            @RequestParam Integer dayOffset) {
        log.info("태스크 이동 요청 - id: {}, dayOffset: {}", id, dayOffset);
        
        TaskResponseDto movedTask = taskService.moveTask(id, dayOffset);
        
        log.info("태스크 이동 완료 - id: {}, 새 시작일: {}, 새 종료일: {}", 
                movedTask.getId(), movedTask.getStartDate(), movedTask.getEndDate());
        return ResponseEntity.ok(movedTask);
    }

    /**
     * 태스크 검색
     */
    @GetMapping("/projects/{projectId}/tasks/search")
    public ResponseEntity<List<TaskResponseDto>> searchTasks(
            @PathVariable @Positive Long projectId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {
        log.info("태스크 검색 요청 - projectId: {}, name: {}, status: {}", projectId, name, status);
        
        List<TaskResponseDto> tasks = taskService.search(projectId, name, status);
        
        log.info("태스크 검색 완료 - projectId: {}, 검색 결과: {}개", projectId, tasks.size());
        return ResponseEntity.ok(tasks);
    }

    /**
     * 상태별 태스크 조회
     */
    @GetMapping("/projects/{projectId}/tasks/status/{status}")
    public ResponseEntity<List<TaskResponseDto>> getTasksByStatus(
            @PathVariable @Positive Long projectId,
            @PathVariable TaskStatus status) {
        log.info("상태별 태스크 조회 요청 - projectId: {}, status: {}", projectId, status);
        
        List<TaskResponseDto> tasks = taskService.findByProjectIdAndStatus(projectId, status);
        
        log.info("상태별 태스크 조회 완료 - projectId: {}, status: {}, 태스크 수: {}", 
                projectId, status, tasks.size());
        return ResponseEntity.ok(tasks);
    }

    /**
     * 이름으로 태스크 검색
     */
    @GetMapping("/projects/{projectId}/tasks/search-by-name")
    public ResponseEntity<List<TaskResponseDto>> searchTasksByName(
            @PathVariable @Positive Long projectId,
            @RequestParam String keyword) {
        log.info("이름으로 태스크 검색 요청 - projectId: {}, keyword: {}", projectId, keyword);
        
        List<TaskResponseDto> tasks = taskService.searchByName(projectId, keyword);
        
        log.info("이름으로 태스크 검색 완료 - projectId: {}, keyword: {}, 검색 결과: {}개", 
                projectId, keyword, tasks.size());
        return ResponseEntity.ok(tasks);
    }
}