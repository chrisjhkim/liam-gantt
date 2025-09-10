package com.liam.gantt.controller.api.v1;

import com.liam.gantt.dto.request.TaskDependencyRequestDto;
import com.liam.gantt.dto.response.ApiResponse;
import com.liam.gantt.dto.response.GanttChartDto;
import com.liam.gantt.dto.response.TaskDependencyResponseDto;
import com.liam.gantt.service.GanttService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 간트 차트 REST API Controller
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class GanttController {
    
    private final GanttService ganttService;
    
    /**
     * 프로젝트의 간트 차트 데이터 조회
     */
    @GetMapping("/projects/{projectId}/gantt")
    public ResponseEntity<ApiResponse<GanttChartDto>> getGanttChart(@PathVariable Long projectId) {
        log.info("간트 차트 데이터 조회 요청: projectId={}", projectId);
        
        GanttChartDto ganttChart = ganttService.getGanttChart(projectId);
        
        return ResponseEntity.ok(ApiResponse.success(ganttChart));
    }
    
    /**
     * 태스크 의존성 추가
     */
    @PostMapping("/dependencies")
    public ResponseEntity<ApiResponse<TaskDependencyResponseDto>> addDependency(
            @Valid @RequestBody TaskDependencyRequestDto requestDto) {
        log.info("태스크 의존성 추가 요청: {} -> {}", 
                requestDto.getPredecessorId(), requestDto.getSuccessorId());
        
        TaskDependencyResponseDto dependency = ganttService.addDependency(requestDto);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(dependency, "의존성이 성공적으로 추가되었습니다"));
    }
    
    /**
     * 태스크 의존성 제거
     */
    @DeleteMapping("/dependencies/{id}")
    public ResponseEntity<ApiResponse<Void>> removeDependency(@PathVariable Long id) {
        log.info("태스크 의존성 제거 요청: id={}", id);
        
        ganttService.removeDependency(id);
        
        return ResponseEntity.ok(ApiResponse.success(null, "의존성이 성공적으로 제거되었습니다"));
    }
    
    /**
     * 태스크의 모든 의존성 조회
     */
    @GetMapping("/tasks/{taskId}/dependencies")
    public ResponseEntity<ApiResponse<List<TaskDependencyResponseDto>>> getTaskDependencies(
            @PathVariable Long taskId) {
        log.info("태스크 의존성 조회 요청: taskId={}", taskId);
        
        List<TaskDependencyResponseDto> dependencies = ganttService.getTaskDependencies(taskId);
        
        return ResponseEntity.ok(ApiResponse.success(dependencies));
    }
    
    /**
     * 프로젝트의 모든 의존성 조회
     */
    @GetMapping("/projects/{projectId}/dependencies")
    public ResponseEntity<ApiResponse<List<TaskDependencyResponseDto>>> getProjectDependencies(
            @PathVariable Long projectId) {
        log.info("프로젝트 의존성 조회 요청: projectId={}", projectId);
        
        List<TaskDependencyResponseDto> dependencies = ganttService.getProjectDependencies(projectId);
        
        return ResponseEntity.ok(ApiResponse.success(dependencies));
    }
    
    /**
     * 임계 경로 계산
     */
    @GetMapping("/projects/{projectId}/critical-path")
    public ResponseEntity<ApiResponse<List<Long>>> calculateCriticalPath(@PathVariable Long projectId) {
        log.info("임계 경로 계산 요청: projectId={}", projectId);
        
        List<Long> criticalPath = ganttService.calculateCriticalPath(projectId);
        
        return ResponseEntity.ok(ApiResponse.success(criticalPath, "임계 경로가 계산되었습니다"));
    }
    
    /**
     * 순환 의존성 체크
     */
    @GetMapping("/dependencies/check-circular")
    public ResponseEntity<ApiResponse<Boolean>> checkCircularDependency(
            @RequestParam Long predecessorId,
            @RequestParam Long successorId) {
        log.info("순환 의존성 체크 요청: {} -> {}", predecessorId, successorId);
        
        boolean hasCircular = ganttService.hasCircularDependency(predecessorId, successorId);
        
        return ResponseEntity.ok(ApiResponse.success(
                hasCircular, 
                hasCircular ? "순환 의존성이 존재합니다" : "순환 의존성이 없습니다"
        ));
    }
    
    /**
     * 프로젝트 일정 재계산
     */
    @PostMapping("/projects/{projectId}/recalculate-schedule")
    public ResponseEntity<ApiResponse<Void>> recalculateProjectSchedule(@PathVariable Long projectId) {
        log.info("프로젝트 일정 재계산 요청: projectId={}", projectId);
        
        ganttService.recalculateProjectSchedule(projectId);
        
        return ResponseEntity.ok(ApiResponse.success(null, "프로젝트 일정이 재계산되었습니다"));
    }
}