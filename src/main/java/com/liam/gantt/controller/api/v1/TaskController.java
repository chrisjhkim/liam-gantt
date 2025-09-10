package com.liam.gantt.controller.api.v1;

import com.liam.gantt.dto.request.TaskRequestDto;
import com.liam.gantt.dto.response.ApiResponse;
import com.liam.gantt.dto.response.TaskResponseDto;
import com.liam.gantt.entity.enums.TaskStatus;
import com.liam.gantt.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 태스크 REST API Controller
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class TaskController {
    
    private final TaskService taskService;
    
    /**
     * 프로젝트에 태스크 생성
     */
    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<ApiResponse<TaskResponseDto>> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody TaskRequestDto requestDto) {
        log.info("태스크 생성 요청: projectId={}, name={}", projectId, requestDto.getName());
        
        TaskResponseDto task = taskService.create(projectId, requestDto);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(task, "태스크가 성공적으로 생성되었습니다"));
    }
    
    /**
     * 태스크 단건 조회
     */
    @GetMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<TaskResponseDto>> getTask(@PathVariable Long id) {
        log.info("태스크 조회 요청: id={}", id);
        
        TaskResponseDto task = taskService.findById(id);
        
        return ResponseEntity.ok(ApiResponse.success(task));
    }
    
    /**
     * 프로젝트의 모든 태스크 조회
     */
    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<ApiResponse<List<TaskResponseDto>>> getProjectTasks(
            @PathVariable Long projectId) {
        log.info("프로젝트 태스크 목록 조회 요청: projectId={}", projectId);
        
        List<TaskResponseDto> tasks = taskService.findByProjectId(projectId);
        
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }
    
    /**
     * 프로젝트의 태스크 페이징 조회
     */
    @GetMapping("/projects/{projectId}/tasks/paged")
    public ResponseEntity<ApiResponse<Page<TaskResponseDto>>> getProjectTasksPaged(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        log.info("프로젝트 태스크 페이징 조회 요청: projectId={}, page={}, size={}", 
                projectId, page, size);
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<TaskResponseDto> tasks = taskService.findByProjectIdWithPaging(projectId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }
    
    /**
     * 태스크 수정
     */
    @PutMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<TaskResponseDto>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequestDto requestDto) {
        log.info("태스크 수정 요청: id={}", id);
        
        TaskResponseDto task = taskService.update(id, requestDto);
        
        return ResponseEntity.ok(ApiResponse.success(task, "태스크가 성공적으로 수정되었습니다"));
    }
    
    /**
     * 태스크 삭제
     */
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        log.info("태스크 삭제 요청: id={}", id);
        
        taskService.delete(id);
        
        return ResponseEntity.ok(ApiResponse.success(null, "태스크가 성공적으로 삭제되었습니다"));
    }
    
    /**
     * 태스크 진행률 업데이트
     */
    @PatchMapping("/tasks/{id}/progress")
    public ResponseEntity<ApiResponse<TaskResponseDto>> updateTaskProgress(
            @PathVariable Long id,
            @RequestParam BigDecimal progress) {
        log.info("태스크 진행률 업데이트 요청: id={}, progress={}%", id, progress);
        
        TaskResponseDto task = taskService.updateProgress(id, progress);
        
        return ResponseEntity.ok(ApiResponse.success(task, "태스크 진행률이 업데이트되었습니다"));
    }
    
    /**
     * 태스크 상태 변경
     */
    @PatchMapping("/tasks/{id}/status")
    public ResponseEntity<ApiResponse<TaskResponseDto>> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus status) {
        log.info("태스크 상태 변경 요청: id={}, status={}", id, status);
        
        TaskResponseDto task = taskService.updateStatus(id, status);
        
        return ResponseEntity.ok(ApiResponse.success(task, "태스크 상태가 변경되었습니다"));
    }
    
    /**
     * 하위 태스크 추가
     */
    @PostMapping("/tasks/{parentTaskId}/subtasks")
    public ResponseEntity<ApiResponse<TaskResponseDto>> addSubTask(
            @PathVariable Long parentTaskId,
            @Valid @RequestBody TaskRequestDto requestDto) {
        log.info("하위 태스크 추가 요청: parentTaskId={}", parentTaskId);
        
        TaskResponseDto task = taskService.addSubTask(parentTaskId, requestDto);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(task, "하위 태스크가 성공적으로 추가되었습니다"));
    }
    
    /**
     * 프로젝트의 루트 태스크 조회
     */
    @GetMapping("/projects/{projectId}/tasks/root")
    public ResponseEntity<ApiResponse<List<TaskResponseDto>>> getRootTasks(
            @PathVariable Long projectId) {
        log.info("루트 태스크 조회 요청: projectId={}", projectId);
        
        List<TaskResponseDto> tasks = taskService.findRootTasks(projectId);
        
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }
    
    /**
     * 프로젝트의 태스크 계층구조 조회
     */
    @GetMapping("/projects/{projectId}/tasks/hierarchy")
    public ResponseEntity<ApiResponse<List<TaskResponseDto>>> getTaskHierarchy(
            @PathVariable Long projectId) {
        log.info("태스크 계층구조 조회 요청: projectId={}", projectId);
        
        List<TaskResponseDto> tasks = taskService.findTaskHierarchyByProjectId(projectId);
        
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }
    
    /**
     * 지연된 태스크 조회
     */
    @GetMapping("/projects/{projectId}/tasks/overdue")
    public ResponseEntity<ApiResponse<List<TaskResponseDto>>> getOverdueTasks(
            @PathVariable Long projectId) {
        log.info("지연된 태스크 조회 요청: projectId={}", projectId);
        
        List<TaskResponseDto> tasks = taskService.findOverdueTasks(projectId);
        
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }
    
    /**
     * 태스크 이동 (날짜 변경)
     */
    @PatchMapping("/tasks/{id}/move")
    public ResponseEntity<ApiResponse<TaskResponseDto>> moveTask(
            @PathVariable Long id,
            @RequestParam Integer dayOffset) {
        log.info("태스크 이동 요청: id={}, dayOffset={}", id, dayOffset);
        
        TaskResponseDto task = taskService.moveTask(id, dayOffset);
        
        return ResponseEntity.ok(ApiResponse.success(task, "태스크가 이동되었습니다"));
    }
}