package com.liam.gantt.service.impl;

import com.liam.gantt.dto.request.ProjectRequestDto;
import com.liam.gantt.dto.response.ProjectResponseDto;
import com.liam.gantt.entity.Project;
import com.liam.gantt.entity.Task;
import com.liam.gantt.entity.enums.ProjectStatus;
import com.liam.gantt.entity.enums.TaskStatus;
import com.liam.gantt.exception.ProjectNotFoundException;
import com.liam.gantt.mapper.ProjectMapper;
import com.liam.gantt.repository.ProjectRepository;
import com.liam.gantt.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectServiceImpl 단위 테스트")
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Project testProject;
    private ProjectRequestDto testRequestDto;
    private ProjectResponseDto testResponseDto;
    private List<Task> testTasks;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        testProject = Project.builder()
                .id(1L)
                .name("테스트 프로젝트")
                .description("테스트 프로젝트 설명")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .status(ProjectStatus.IN_PROGRESS)
                .build();

        testRequestDto = ProjectRequestDto.builder()
                .name("테스트 프로젝트")
                .description("테스트 프로젝트 설명")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .status(ProjectStatus.IN_PROGRESS)
                .build();

        testResponseDto = ProjectResponseDto.builder()
                .id(1L)
                .name("테스트 프로젝트")
                .description("테스트 프로젝트 설명")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .status(ProjectStatus.IN_PROGRESS)
                .build();

        Task task1 = Task.builder()
                .id(1L)
                .name("태스크 1")
                .progress(BigDecimal.valueOf(100))
                .status(TaskStatus.COMPLETED)
                .build();

        Task task2 = Task.builder()
                .id(2L)
                .name("태스크 2")
                .progress(BigDecimal.valueOf(50))
                .status(TaskStatus.IN_PROGRESS)
                .build();

        testTasks = Arrays.asList(task1, task2);
    }

    @Test
    @DisplayName("프로젝트 생성 - 성공")
    void createProject_Success() {
        // Given
        given(projectMapper.toEntity(testRequestDto)).willReturn(testProject);
        given(projectRepository.save(any(Project.class))).willReturn(testProject);
        given(projectMapper.toResponseDto(testProject)).willReturn(testResponseDto);

        // When
        ProjectResponseDto result = projectService.create(testRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("테스트 프로젝트");

        verify(projectMapper).toEntity(testRequestDto);
        verify(projectRepository).save(any(Project.class));
        verify(projectMapper).toResponseDto(testProject);
    }

    @Test
    @DisplayName("프로젝트 ID로 조회 - 성공")
    void findById_Success() {
        // Given
        given(projectRepository.findById(1L)).willReturn(Optional.of(testProject));
        given(projectMapper.toResponseDto(testProject)).willReturn(testResponseDto);

        // When
        ProjectResponseDto result = projectService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("테스트 프로젝트");

        verify(projectRepository).findById(1L);
        verify(projectMapper).toResponseDto(testProject);
    }

    @Test
    @DisplayName("프로젝트 ID로 조회 - 실패 (존재하지 않는 ID)")
    void findById_NotFound() {
        // Given
        given(projectRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectService.findById(999L))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining("프로젝트를 찾을 수 없습니다");

        verify(projectRepository).findById(999L);
        verify(projectMapper, never()).toResponseDto(any());
    }

    @Test
    @DisplayName("모든 프로젝트 조회")
    void findAll_Success() {
        // Given
        List<Project> projects = Arrays.asList(testProject);
        given(projectRepository.findAll()).willReturn(projects);
        given(projectMapper.toResponseDto(testProject)).willReturn(testResponseDto);

        // When
        List<ProjectResponseDto> result = projectService.findAll();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("테스트 프로젝트");

        verify(projectRepository).findAll();
        verify(projectMapper).toResponseDto(testProject);
    }

    @Test
    @DisplayName("프로젝트 페이징 조회")
    void findAllWithPaging_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> projectPage = new PageImpl<>(Arrays.asList(testProject));
        given(projectRepository.findAll(pageable)).willReturn(projectPage);
        given(projectMapper.toResponseDto(testProject)).willReturn(testResponseDto);

        // When
        Page<ProjectResponseDto> result = projectService.findAllWithPaging(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("테스트 프로젝트");

        verify(projectRepository).findAll(pageable);
        verify(projectMapper).toResponseDto(testProject);
    }

    @Test
    @DisplayName("프로젝트 수정 - 성공")
    void updateProject_Success() {
        // Given
        ProjectRequestDto updateRequest = ProjectRequestDto.builder()
                .name("수정된 프로젝트")
                .description("수정된 설명")
                .startDate(LocalDate.of(2025, 2, 1))
                .endDate(LocalDate.of(2025, 11, 30))
                .status(ProjectStatus.COMPLETED)
                .build();

        Project updatedProject = Project.builder()
                .id(1L)
                .name("수정된 프로젝트")
                .description("수정된 설명")
                .startDate(LocalDate.of(2025, 2, 1))
                .endDate(LocalDate.of(2025, 11, 30))
                .status(ProjectStatus.COMPLETED)
                .build();

        ProjectResponseDto updatedResponseDto = ProjectResponseDto.builder()
                .id(1L)
                .name("수정된 프로젝트")
                .description("수정된 설명")
                .startDate(LocalDate.of(2025, 2, 1))
                .endDate(LocalDate.of(2025, 11, 30))
                .status(ProjectStatus.COMPLETED)
                .build();

        given(projectRepository.findById(1L)).willReturn(Optional.of(testProject));
        given(projectRepository.save(any(Project.class))).willReturn(updatedProject);
        given(projectMapper.toResponseDto(any(Project.class))).willReturn(updatedResponseDto);

        // When
        ProjectResponseDto result = projectService.update(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("수정된 프로젝트");
        assertThat(result.getStatus()).isEqualTo(ProjectStatus.COMPLETED);

        verify(projectRepository).findById(1L);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("프로젝트 삭제 - 성공")
    void deleteProject_Success() {
        // Given
        given(projectRepository.existsById(1L)).willReturn(true);
        doNothing().when(projectRepository).deleteById(1L);

        // When
        projectService.delete(1L);

        // Then
        verify(projectRepository).existsById(1L);
        verify(projectRepository).deleteById(1L);
    }

    @Test
    @DisplayName("프로젝트 삭제 - 실패 (존재하지 않는 ID)")
    void deleteProject_NotFound() {
        // Given
        given(projectRepository.existsById(999L)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> projectService.delete(999L))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining("프로젝트를 찾을 수 없습니다");

        verify(projectRepository).existsById(999L);
        verify(projectRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("프로젝트 검색 - 이름과 상태로")
    void search_Success() {
        // Given
        given(projectRepository.findAll()).willReturn(Arrays.asList(testProject));
        given(projectMapper.toResponseDto(testProject)).willReturn(testResponseDto);

        // When
        List<ProjectResponseDto> result = projectService.search("테스트", "IN_PROGRESS");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("테스트 프로젝트");

        verify(projectRepository).findAll();
    }

    @Test
    @DisplayName("프로젝트 검색 - 이름만으로")
    void searchByNameOnly_Success() {
        // Given
        given(projectRepository.findAll()).willReturn(Arrays.asList(testProject));
        given(projectMapper.toResponseDto(testProject)).willReturn(testResponseDto);

        // When
        List<ProjectResponseDto> result = projectService.search("테스트", null);

        // Then
        assertThat(result).hasSize(1);

        verify(projectRepository).findAll();
    }

    @Test
    @DisplayName("프로젝트 전체 개수 조회")
    void countAll_Success() {
        // Given
        given(projectRepository.count()).willReturn(5L);

        // When
        long count = projectService.countAll();

        // Then
        assertThat(count).isEqualTo(5L);

        verify(projectRepository).count();
    }

    @Test
    @DisplayName("상태별 프로젝트 개수 조회")
    void countByStatus_Success() {
        // Given
        given(projectRepository.findAll()).willReturn(Arrays.asList(testProject));

        // When
        long count = projectService.countByStatus("IN_PROGRESS");

        // Then
        assertThat(count).isEqualTo(1L);

        verify(projectRepository).findAll();
    }

    @Test
    @DisplayName("프로젝트와 태스크 함께 조회")
    void findByIdWithTasks_Success() {
        // Given
        testProject.setTasks(testTasks);
        given(projectRepository.findById(1L)).willReturn(Optional.of(testProject));
        given(taskRepository.findByProjectId(1L)).willReturn(testTasks);
        given(projectMapper.toResponseDto(testProject)).willReturn(testResponseDto);

        // When
        ProjectResponseDto result = projectService.findByIdWithTasks(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProjectId(1L);
    }

    @Test
    @DisplayName("프로젝트 진행률 계산")
    void calculateProjectProgress_Success() {
        // Given
        testProject.setTasks(testTasks);
        ProjectResponseDto responseWithProgress = ProjectResponseDto.builder()
                .id(1L)
                .name("테스트 프로젝트")
                .averageProgress(BigDecimal.valueOf(75)) // (100 + 50) / 2
                .build();

        given(projectRepository.findById(1L)).willReturn(Optional.of(testProject));
        given(taskRepository.findByProjectId(1L)).willReturn(testTasks);
        given(projectRepository.save(any(Project.class))).willReturn(testProject);
        given(projectMapper.toResponseDto(any(Project.class))).willReturn(responseWithProgress);

        // When
        ProjectResponseDto result = projectService.calculateProjectProgress(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAverageProgress()).isEqualTo(BigDecimal.valueOf(75));

        verify(projectRepository).findById(1L);
        verify(taskRepository).findByProjectId(1L);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("지연된 프로젝트 조회")
    void getOverdueProjects_Success() {
        // Given
        Project overdueProject = Project.builder()
                .id(2L)
                .name("지연된 프로젝트")
                .endDate(LocalDate.now().minusDays(1))
                .status(ProjectStatus.IN_PROGRESS)
                .build();

        given(projectRepository.findAll()).willReturn(Arrays.asList(overdueProject));
        given(projectMapper.toResponseDto(overdueProject)).willReturn(
                ProjectResponseDto.builder()
                        .id(2L)
                        .name("지연된 프로젝트")
                        .endDate(LocalDate.now().minusDays(1))
                        .status(ProjectStatus.IN_PROGRESS)
                        .build()
        );

        // When
        List<ProjectResponseDto> result = projectService.getOverdueProjects();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("지연된 프로젝트");

        verify(projectRepository).findAll();
    }

    @Test
    @DisplayName("날짜 범위로 프로젝트 조회")
    void getProjectsByDateRange_Success() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 30);

        given(projectRepository.findAll()).willReturn(Arrays.asList(testProject));
        given(projectMapper.toResponseDto(testProject)).willReturn(testResponseDto);

        // When
        List<ProjectResponseDto> result = projectService.getProjectsByDateRange(startDate, endDate);

        // Then
        assertThat(result).hasSize(1);

        verify(projectRepository).findAll();
    }

    @Test
    @DisplayName("프로젝트 상태 업데이트")
    void updateProjectStatus_Success() {
        // Given
        given(projectRepository.findById(1L)).willReturn(Optional.of(testProject));
        given(projectRepository.save(any(Project.class))).willReturn(testProject);
        given(projectMapper.toResponseDto(any(Project.class))).willReturn(
                ProjectResponseDto.builder()
                        .id(1L)
                        .name("테스트 프로젝트")
                        .status(ProjectStatus.COMPLETED)
                        .build()
        );

        // When
        ProjectResponseDto result = projectService.updateProjectStatus(1L, ProjectStatus.COMPLETED);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ProjectStatus.COMPLETED);

        verify(projectRepository).findById(1L);
        verify(projectRepository).save(any(Project.class));
    }
}