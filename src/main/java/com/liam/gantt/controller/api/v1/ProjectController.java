package com.liam.gantt.controller.api.v1;

import com.liam.gantt.dto.request.ProjectRequestDto;
import com.liam.gantt.dto.response.ApiResponse;
import com.liam.gantt.dto.response.ProjectResponseDto;
import com.liam.gantt.entity.enums.ProjectStatus;
import com.liam.gantt.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 프로젝트 REST API Controller
 */
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {
    
    private final ProjectService projectService;
    
    /**
     * 프로젝트 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponseDto>> createProject(
            @Valid @RequestBody ProjectRequestDto requestDto) {
        log.info("프로젝트 생성 요청: {}", requestDto.getName());
        
        ProjectResponseDto project = projectService.createProject(requestDto);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(project, "프로젝트가 성공적으로 생성되었습니다"));
    }
    
    /**
     * 프로젝트 단건 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> getProject(@PathVariable Long id) {
        log.info("프로젝트 조회 요청: id={}", id);
        
        ProjectResponseDto project = projectService.getProjectById(id);
        
        return ResponseEntity.ok(ApiResponse.success(project));
    }
    
    /**
     * 모든 프로젝트 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProjectResponseDto>>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        log.info("프로젝트 목록 조회 요청: page={}, size={}", page, size);
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ProjectResponseDto> projects = projectService.getAllProjects(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(projects));
    }
    
    /**
     * 프로젝트 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequestDto requestDto) {
        log.info("프로젝트 수정 요청: id={}", id);
        
        ProjectResponseDto project = projectService.updateProject(id, requestDto);
        
        return ResponseEntity.ok(ApiResponse.success(project, "프로젝트가 성공적으로 수정되었습니다"));
    }
    
    /**
     * 프로젝트 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        log.info("프로젝트 삭제 요청: id={}", id);
        
        projectService.deleteProject(id);
        
        return ResponseEntity.ok(ApiResponse.success(null, "프로젝트가 성공적으로 삭제되었습니다"));
    }
    
    /**
     * 프로젝트명으로 검색
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProjectResponseDto>>> searchProjects(
            @RequestParam String keyword) {
        log.info("프로젝트 검색 요청: keyword={}", keyword);
        
        List<ProjectResponseDto> projects = projectService.searchProjectsByName(keyword);
        
        return ResponseEntity.ok(ApiResponse.success(projects));
    }
    
    /**
     * 상태별 프로젝트 조회
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<ProjectResponseDto>>> getProjectsByStatus(
            @PathVariable ProjectStatus status) {
        log.info("상태별 프로젝트 조회 요청: status={}", status);
        
        List<ProjectResponseDto> projects = projectService.getProjectsByStatus(status);
        
        return ResponseEntity.ok(ApiResponse.success(projects));
    }
    
    /**
     * 지연된 프로젝트 조회
     */
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<ProjectResponseDto>>> getOverdueProjects() {
        log.info("지연된 프로젝트 조회 요청");
        
        List<ProjectResponseDto> projects = projectService.getOverdueProjects();
        
        return ResponseEntity.ok(ApiResponse.success(projects));
    }
    
    /**
     * 프로젝트 상태 변경
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> updateProjectStatus(
            @PathVariable Long id,
            @RequestParam ProjectStatus status) {
        log.info("프로젝트 상태 변경 요청: id={}, status={}", id, status);
        
        ProjectResponseDto project = projectService.updateProjectStatus(id, status);
        
        return ResponseEntity.ok(ApiResponse.success(project, "프로젝트 상태가 변경되었습니다"));
    }
    
    /**
     * 날짜 범위로 프로젝트 조회
     */
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<ProjectResponseDto>>> getProjectsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("날짜 범위 프로젝트 조회 요청: {} ~ {}", startDate, endDate);
        
        List<ProjectResponseDto> projects = projectService.getProjectsByDateRange(startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(projects));
    }
    
    /**
     * 프로젝트 진행률 계산
     */
    @PostMapping("/{id}/calculate-progress")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> calculateProjectProgress(@PathVariable Long id) {
        log.info("프로젝트 진행률 계산 요청: id={}", id);
        
        ProjectResponseDto project = projectService.calculateProjectProgress(id);
        
        return ResponseEntity.ok(ApiResponse.success(project, "프로젝트 진행률이 계산되었습니다"));
    }
}