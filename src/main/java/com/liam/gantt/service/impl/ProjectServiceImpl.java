package com.liam.gantt.service.impl;

import com.liam.gantt.dto.request.ProjectRequestDto;
import com.liam.gantt.dto.response.ProjectResponseDto;
import com.liam.gantt.entity.Project;
import com.liam.gantt.entity.enums.ProjectStatus;
import com.liam.gantt.exception.DuplicateResourceException;
import com.liam.gantt.exception.InvalidRequestException;
import com.liam.gantt.exception.ResourceNotFoundException;
import com.liam.gantt.repository.ProjectRepository;
import com.liam.gantt.repository.TaskRepository;
import com.liam.gantt.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 프로젝트 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProjectServiceImpl implements ProjectService {
    
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    
    @Override
    @Transactional
    public ProjectResponseDto create(ProjectRequestDto requestDto) {
        log.info("프로젝트 생성 시작: {}", requestDto.getName());
        
        // 중복 체크
        if (projectRepository.existsByName(requestDto.getName())) {
            throw new DuplicateResourceException("Project", "name", requestDto.getName());
        }
        
        // 날짜 유효성 검증
        if (!requestDto.isValidDateRange()) {
            throw new InvalidRequestException("종료일은 시작일보다 같거나 늦어야 합니다");
        }
        
        // 엔티티 생성
        Project project = Project.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .status(ProjectStatus.PLANNING)
                .build();
        
        Project savedProject = projectRepository.save(project);
        log.info("프로젝트 생성 완료: id={}, name={}", savedProject.getId(), savedProject.getName());
        
        return convertToDto(savedProject);
    }
    
    @Override
    public ProjectResponseDto findById(Long id) {
        log.debug("프로젝트 조회: id={}", id);
        
        Project project = projectRepository.findByIdWithTasks(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        
        return convertToDto(project);
    }
    
    @Override
    public Page<ProjectResponseDto> findAllWithPaging(Pageable pageable) {
        log.debug("모든 프로젝트 페이징 조회");
        
        Page<Project> projects = projectRepository.findAll(pageable);
        return projects.map(this::convertToDto);
    }
    
    @Override
    @Transactional
    public ProjectResponseDto update(Long id, ProjectRequestDto requestDto) {
        log.info("프로젝트 수정: id={}", id);
        
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        
        // 프로젝트명 중복 체크 (자기 자신 제외)
        if (!project.getName().equals(requestDto.getName()) && 
            projectRepository.existsByName(requestDto.getName())) {
            throw new DuplicateResourceException("Project", "name", requestDto.getName());
        }
        
        // 날짜 유효성 검증
        if (!requestDto.isValidDateRange()) {
            throw new InvalidRequestException("종료일은 시작일보다 같거나 늦어야 합니다");
        }
        
        // 엔티티 업데이트
        project.setName(requestDto.getName());
        project.setDescription(requestDto.getDescription());
        project.setStartDate(requestDto.getStartDate());
        project.setEndDate(requestDto.getEndDate());
        
        Project updatedProject = projectRepository.save(project);
        log.info("프로젝트 수정 완료: id={}", id);
        
        return convertToDto(updatedProject);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        log.info("프로젝트 삭제: id={}", id);
        
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project", "id", id);
        }
        
        projectRepository.deleteById(id);
        log.info("프로젝트 삭제 완료: id={}", id);
    }
    
    @Override
    public List<ProjectResponseDto> findAll() {
        log.debug("모든 프로젝트 조회");

        List<Project> projects = projectRepository.findAll();
        return projects.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectResponseDto findByIdWithTasks(Long id) {
        log.debug("태스크 포함 프로젝트 조회: id={}", id);

        Project project = projectRepository.findByIdWithTasks(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        return convertToDto(project);
    }

    @Override
    public List<ProjectResponseDto> searchProjectsByName(String keyword) {
        log.debug("프로젝트명 검색: keyword={}", keyword);
        
        List<Project> projects = projectRepository.findByNameContainingIgnoreCase(keyword);
        return projects.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ProjectResponseDto> getProjectsByStatus(ProjectStatus status) {
        log.debug("상태별 프로젝트 조회: status={}", status);
        
        List<Project> projects = projectRepository.findByStatus(status);
        return projects.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ProjectResponseDto> search(String name, String status) {
        log.debug("프로젝트 검색: name={}, status={}", name, status);

        List<Project> projects = projectRepository.findAll();
        return projects.stream()
                .filter(p -> name == null || p.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(p -> status == null || p.getStatus().name().equals(status))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProjectResponseDto> searchWithPaging(String name, String status, Pageable pageable) {
        log.debug("페이징된 프로젝트 검색: name={}, status={}", name, status);

        Page<Project> projects = projectRepository.findAll(pageable);
        return projects.map(this::convertToDto);
    }

    @Override
    public long countAll() {
        return projectRepository.count();
    }

    @Override
    public long countByStatus(String status) {
        try {
            ProjectStatus projectStatus = ProjectStatus.valueOf(status);
            return projectRepository.countByStatus(projectStatus);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    @Override
    public List<ProjectResponseDto> getOverdueProjects() {
        log.debug("지연된 프로젝트 조회");
        
        List<Project> projects = projectRepository.findOverdueProjects(LocalDate.now());
        return projects.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ProjectResponseDto updateProjectStatus(Long id, ProjectStatus status) {
        log.info("프로젝트 상태 변경: id={}, status={}", id, status);
        
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        
        project.setStatus(status);
        Project updatedProject = projectRepository.save(project);
        
        log.info("프로젝트 상태 변경 완료: id={}, status={}", id, status);
        return convertToDto(updatedProject);
    }
    
    @Override
    public List<ProjectResponseDto> getProjectsByDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("날짜 범위로 프로젝트 조회: {} ~ {}", startDate, endDate);
        
        List<Project> projects = projectRepository.findByStartDateBetween(startDate, endDate);
        return projects.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ProjectResponseDto calculateProjectProgress(Long id) {
        log.info("프로젝트 진행률 계산: id={}", id);
        
        Project project = projectRepository.findByIdWithTasks(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        
        Double progress = project.calculateProgress();
        
        // 진행률에 따른 상태 자동 업데이트
        if (progress >= 100.0 && project.getStatus() != ProjectStatus.COMPLETED) {
            project.setStatus(ProjectStatus.COMPLETED);
            projectRepository.save(project);
        } else if (progress > 0 && progress < 100 && project.getStatus() == ProjectStatus.PLANNING) {
            project.setStatus(ProjectStatus.IN_PROGRESS);
            projectRepository.save(project);
        }
        
        ProjectResponseDto responseDto = convertToDto(project);
        responseDto.setProgress(progress);
        
        log.info("프로젝트 진행률 계산 완료: id={}, progress={}%", id, progress);
        return responseDto;
    }
    
    // 누락된 메서드들 구현
    
    @Override
    public Page<ProjectResponseDto> getAllProjects(Pageable pageable) {
        return findAllWithPaging(pageable); // 기존의 findAllWithPaging 메서드 호출
    }
    
    @Override
    public ProjectResponseDto getProjectById(Long id) {
        return findById(id); // 기존의 findById 메서드 호출
    }
    
    @Override
    @Transactional
    public ProjectResponseDto createProject(ProjectRequestDto requestDto) {
        return create(requestDto); // 기존의 create 메서드 호출
    }
    
    @Override
    @Transactional
    public ProjectResponseDto updateProject(Long id, ProjectRequestDto requestDto) {
        return update(id, requestDto); // 기존의 update 메서드 호출
    }
    
    @Override
    public void deleteProject(Long id) {
        delete(id); // 기존의 delete 메서드 호출
    }

    /**
     * Entity를 DTO로 변환
     */
    private ProjectResponseDto convertToDto(Project project) {
        long taskCount = project.getTasks() != null ? project.getTasks().size() : 0;
        
        return ProjectResponseDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus())
                .progress(project.calculateProgress())
                .taskCount((int) taskCount)
                .durationInDays(project.getDurationInDays())
                .isOverdue(project.isOverdue())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}