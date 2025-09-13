package com.liam.gantt.controller.api.v1;

import com.liam.gantt.dto.request.TaskDependencyRequestDto;
import com.liam.gantt.dto.response.GanttChartDto;
import com.liam.gantt.dto.response.TaskDependencyResponseDto;
import com.liam.gantt.service.GanttService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 간트 차트 관리 REST API 컨트롤러
 * 
 * @author Liam
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
@Slf4j
public class GanttController {

    private final GanttService ganttService;

    /**
     * 프로젝트의 간트 차트 데이터 조회
     */
    @GetMapping("/projects/{projectId}/gantt")
    public ResponseEntity<GanttChartDto> getGanttChart(@PathVariable @Positive Long projectId) {
        log.info("간트 차트 데이터 조회 요청 - projectId: {}", projectId);
        
        GanttChartDto ganttChart = ganttService.getGanttChart(projectId);
        
        log.info("간트 차트 데이터 조회 완료 - projectId: {}, 태스크 수: {}", 
                projectId, ganttChart.getTasks().size());
        return ResponseEntity.ok(ganttChart);
    }

    /**
     * 태스크 의존성 추가
     */
    @PostMapping("/dependencies")
    public ResponseEntity<TaskDependencyResponseDto> addDependency(
            @Valid @RequestBody TaskDependencyRequestDto requestDto) {
        log.info("태스크 의존성 추가 요청 - predecessor: {}, successor: {}", 
                requestDto.getPredecessorId(), requestDto.getSuccessorId());
        
        TaskDependencyResponseDto dependency = ganttService.addDependency(requestDto);
        
        log.info("태스크 의존성 추가 완료 - id: {}, predecessor: {}, successor: {}", 
                dependency.getId(), dependency.getPredecessorId(), dependency.getSuccessorId());
        return ResponseEntity.status(HttpStatus.CREATED).body(dependency);
    }

    /**
     * 태스크 의존성 제거
     */
    @DeleteMapping("/dependencies/{id}")
    public ResponseEntity<Void> removeDependency(@PathVariable @Positive Long id) {
        log.info("태스크 의존성 제거 요청 - id: {}", id);
        
        ganttService.removeDependency(id);
        
        log.info("태스크 의존성 제거 완료 - id: {}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 태스크의 모든 의존성 조회
     */
    @GetMapping("/tasks/{taskId}/dependencies")
    public ResponseEntity<List<TaskDependencyResponseDto>> getTaskDependencies(
            @PathVariable @Positive Long taskId) {
        log.info("태스크 의존성 조회 요청 - taskId: {}", taskId);
        
        List<TaskDependencyResponseDto> dependencies = ganttService.getTaskDependencies(taskId);
        
        log.info("태스크 의존성 조회 완료 - taskId: {}, 의존성 수: {}", taskId, dependencies.size());
        return ResponseEntity.ok(dependencies);
    }

    /**
     * 프로젝트의 모든 의존성 조회
     */
    @GetMapping("/projects/{projectId}/dependencies")
    public ResponseEntity<List<TaskDependencyResponseDto>> getProjectDependencies(
            @PathVariable @Positive Long projectId) {
        log.info("프로젝트 의존성 조회 요청 - projectId: {}", projectId);
        
        List<TaskDependencyResponseDto> dependencies = ganttService.getProjectDependencies(projectId);
        
        log.info("프로젝트 의존성 조회 완료 - projectId: {}, 의존성 수: {}", projectId, dependencies.size());
        return ResponseEntity.ok(dependencies);
    }

    /**
     * 임계 경로 계산
     */
    @GetMapping("/projects/{projectId}/critical-path")
    public ResponseEntity<List<Long>> calculateCriticalPath(@PathVariable @Positive Long projectId) {
        log.info("임계 경로 계산 요청 - projectId: {}", projectId);
        
        List<Long> criticalPath = ganttService.calculateCriticalPath(projectId);
        
        log.info("임계 경로 계산 완료 - projectId: {}, 임계 경로 태스크 수: {}", projectId, criticalPath.size());
        return ResponseEntity.ok(criticalPath);
    }

    /**
     * 순환 의존성 체크
     */
    @GetMapping("/dependencies/check-circular")
    public ResponseEntity<CircularDependencyCheckResult> checkCircularDependency(
            @RequestParam @Positive Long predecessorId,
            @RequestParam @Positive Long successorId) {
        log.info("순환 의존성 체크 요청 - predecessor: {}, successor: {}", predecessorId, successorId);
        
        boolean hasCircular = ganttService.hasCircularDependency(predecessorId, successorId);
        
        CircularDependencyCheckResult result = CircularDependencyCheckResult.builder()
                .hasCircularDependency(hasCircular)
                .predecessorId(predecessorId)
                .successorId(successorId)
                .message(hasCircular ? "순환 의존성이 존재합니다" : "순환 의존성이 없습니다")
                .build();
        
        log.info("순환 의존성 체크 완료 - predecessor: {}, successor: {}, 결과: {}", 
                predecessorId, successorId, hasCircular ? "순환 존재" : "순환 없음");
        return ResponseEntity.ok(result);
    }

    /**
     * 프로젝트 일정 재계산
     */
    @PostMapping("/projects/{projectId}/recalculate-schedule")
    public ResponseEntity<Void> recalculateProjectSchedule(@PathVariable @Positive Long projectId) {
        log.info("프로젝트 일정 재계산 요청 - projectId: {}", projectId);
        
        ganttService.recalculateProjectSchedule(projectId);
        
        log.info("프로젝트 일정 재계산 완료 - projectId: {}", projectId);
        return ResponseEntity.ok().build();
    }

    /**
     * 순환 의존성 체크 결과 DTO
     */
    public static class CircularDependencyCheckResult {
        private boolean hasCircularDependency;
        private Long predecessorId;
        private Long successorId;
        private String message;

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private CircularDependencyCheckResult result = new CircularDependencyCheckResult();

            public Builder hasCircularDependency(boolean hasCircularDependency) {
                result.hasCircularDependency = hasCircularDependency;
                return this;
            }

            public Builder predecessorId(Long predecessorId) {
                result.predecessorId = predecessorId;
                return this;
            }

            public Builder successorId(Long successorId) {
                result.successorId = successorId;
                return this;
            }

            public Builder message(String message) {
                result.message = message;
                return this;
            }

            public CircularDependencyCheckResult build() {
                return result;
            }
        }

        // Getters
        public boolean isHasCircularDependency() { return hasCircularDependency; }
        public Long getPredecessorId() { return predecessorId; }
        public Long getSuccessorId() { return successorId; }
        public String getMessage() { return message; }
    }
}