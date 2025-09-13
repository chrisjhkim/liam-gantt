package com.liam.gantt.controller.api.v1;

import com.liam.gantt.dto.request.ProjectRequestDto;
import com.liam.gantt.dto.response.ProjectResponseDto;
import com.liam.gantt.service.ProjectService;
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

import java.util.List;

/**
 * 프로젝트 관리 REST API 컨트롤러
 * 
 * @author Liam
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ProjectController {
    
    private final ProjectService projectService;

    /**
     * 프로젝트 목록 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<Page<ProjectResponseDto>> getAllProjects(
            @PageableDefault(size = 20, sort = "startDate") Pageable pageable) {
        log.info("프로젝트 목록 조회 요청 - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        Page<ProjectResponseDto> projects = projectService.findAllWithPaging(pageable);
        
        log.info("프로젝트 목록 조회 완료 - 총 {}개, 현재 페이지 {}개", 
                projects.getTotalElements(), projects.getNumberOfElements());
                
        return ResponseEntity.ok(projects);
    }

    /**
     * 프로젝트 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> getProject(@PathVariable @Positive Long id) {
        log.info("프로젝트 상세 조회 요청 - id: {}", id);
        
        ProjectResponseDto project = projectService.findById(id);
        
        log.info("프로젝트 상세 조회 완료 - id: {}, name: {}", project.getId(), project.getName());
        return ResponseEntity.ok(project);
    }

    /**
     * 프로젝트 생성
     */
    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(@Valid @RequestBody ProjectRequestDto request) {
        log.info("프로젝트 생성 요청 - name: {}", request.getName());
        
        ProjectResponseDto createdProject = projectService.create(request);
        
        log.info("프로젝트 생성 완료 - id: {}, name: {}", createdProject.getId(), createdProject.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    /**
     * 프로젝트 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> updateProject(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ProjectRequestDto request) {
        log.info("프로젝트 수정 요청 - id: {}, name: {}", id, request.getName());
        
        ProjectResponseDto updatedProject = projectService.update(id, request);
        
        log.info("프로젝트 수정 완료 - id: {}, name: {}", updatedProject.getId(), updatedProject.getName());
        return ResponseEntity.ok(updatedProject);
    }

    /**
     * 프로젝트 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable @Positive Long id) {
        log.info("프로젝트 삭제 요청 - id: {}", id);
        
        projectService.delete(id);
        
        log.info("프로젝트 삭제 완료 - id: {}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 프로젝트 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProjectResponseDto>> searchProjects(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {
        log.info("프로젝트 검색 요청 - name: {}, status: {}", name, status);
        
        List<ProjectResponseDto> projects = projectService.search(name, status);
        
        log.info("프로젝트 검색 완료 - 검색 결과 {}개", projects.size());
        return ResponseEntity.ok(projects);
    }

    /**
     * 프로젝트 전체 목록 조회 (페이징 없음)
     */
    @GetMapping("/all")
    public ResponseEntity<List<ProjectResponseDto>> getAllProjectsWithoutPaging() {
        log.info("전체 프로젝트 목록 조회 요청");
        
        List<ProjectResponseDto> projects = projectService.findAll();
        
        log.info("전체 프로젝트 목록 조회 완료 - 총 {}개", projects.size());
        return ResponseEntity.ok(projects);
    }
    
    /**
     * 프로젝트 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<ProjectStatistics> getProjectStatistics() {
        log.info("프로젝트 통계 조회 요청");
        
        long totalCount = projectService.countAll();
        long planningCount = projectService.countByStatus("PLANNING");
        long inProgressCount = projectService.countByStatus("IN_PROGRESS");
        long completedCount = projectService.countByStatus("COMPLETED");
        long onHoldCount = projectService.countByStatus("ON_HOLD");
        long cancelledCount = projectService.countByStatus("CANCELLED");
        
        ProjectStatistics statistics = ProjectStatistics.builder()
                .totalProjects(totalCount)
                .planningProjects(planningCount)
                .inProgressProjects(inProgressCount)
                .completedProjects(completedCount)
                .onHoldProjects(onHoldCount)
                .cancelledProjects(cancelledCount)
                .build();
        
        log.info("프로젝트 통계 조회 완료 - 전체: {}개", totalCount);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 프로젝트 통계 DTO
     */
    public static class ProjectStatistics {
        private long totalProjects;
        private long planningProjects;
        private long inProgressProjects;
        private long completedProjects;
        private long onHoldProjects;
        private long cancelledProjects;

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private ProjectStatistics statistics = new ProjectStatistics();

            public Builder totalProjects(long totalProjects) {
                statistics.totalProjects = totalProjects;
                return this;
            }

            public Builder planningProjects(long planningProjects) {
                statistics.planningProjects = planningProjects;
                return this;
            }

            public Builder inProgressProjects(long inProgressProjects) {
                statistics.inProgressProjects = inProgressProjects;
                return this;
            }

            public Builder completedProjects(long completedProjects) {
                statistics.completedProjects = completedProjects;
                return this;
            }

            public Builder onHoldProjects(long onHoldProjects) {
                statistics.onHoldProjects = onHoldProjects;
                return this;
            }

            public Builder cancelledProjects(long cancelledProjects) {
                statistics.cancelledProjects = cancelledProjects;
                return this;
            }

            public ProjectStatistics build() {
                return statistics;
            }
        }

        // Getters
        public long getTotalProjects() { return totalProjects; }
        public long getPlanningProjects() { return planningProjects; }
        public long getInProgressProjects() { return inProgressProjects; }
        public long getCompletedProjects() { return completedProjects; }
        public long getOnHoldProjects() { return onHoldProjects; }
        public long getCancelledProjects() { return cancelledProjects; }
    }
}