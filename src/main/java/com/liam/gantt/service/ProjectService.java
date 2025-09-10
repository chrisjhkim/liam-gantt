package com.liam.gantt.service;

import com.liam.gantt.dto.request.ProjectRequestDto;
import com.liam.gantt.dto.response.ProjectResponseDto;
import com.liam.gantt.entity.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * 프로젝트 관련 비즈니스 로직 인터페이스
 */
public interface ProjectService {
    
    /**
     * 프로젝트 생성
     */
    ProjectResponseDto createProject(ProjectRequestDto requestDto);
    
    /**
     * 프로젝트 조회
     */
    ProjectResponseDto getProjectById(Long id);
    
    /**
     * 모든 프로젝트 조회 (페이징)
     */
    Page<ProjectResponseDto> getAllProjects(Pageable pageable);
    
    /**
     * 프로젝트 수정
     */
    ProjectResponseDto updateProject(Long id, ProjectRequestDto requestDto);
    
    /**
     * 프로젝트 삭제
     */
    void deleteProject(Long id);
    
    /**
     * 프로젝트명으로 검색
     */
    List<ProjectResponseDto> searchProjectsByName(String keyword);
    
    /**
     * 상태별 프로젝트 조회
     */
    List<ProjectResponseDto> getProjectsByStatus(ProjectStatus status);
    
    /**
     * 지연된 프로젝트 조회
     */
    List<ProjectResponseDto> getOverdueProjects();
    
    /**
     * 프로젝트 상태 변경
     */
    ProjectResponseDto updateProjectStatus(Long id, ProjectStatus status);
    
    /**
     * 날짜 범위로 프로젝트 조회
     */
    List<ProjectResponseDto> getProjectsByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * 프로젝트 진행률 계산 및 업데이트
     */
    ProjectResponseDto calculateProjectProgress(Long id);
    
    // 새로 추가된 메서드들
    List<ProjectResponseDto> findAll();
    Page<ProjectResponseDto> findAllWithPaging(Pageable pageable);
    ProjectResponseDto findById(Long id);
    ProjectResponseDto findByIdWithTasks(Long id);
    ProjectResponseDto create(ProjectRequestDto request);
    ProjectResponseDto update(Long id, ProjectRequestDto request);
    void delete(Long id);
    List<ProjectResponseDto> search(String name, String status);
    Page<ProjectResponseDto> searchWithPaging(String name, String status, Pageable pageable);
    long countAll();
    long countByStatus(String status);
}