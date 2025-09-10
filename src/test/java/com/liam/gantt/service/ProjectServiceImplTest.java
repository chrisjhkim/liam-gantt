package com.liam.gantt.service;

import com.liam.gantt.dto.request.ProjectRequestDto;
import com.liam.gantt.dto.response.ProjectResponseDto;
import com.liam.gantt.entity.Project;
import com.liam.gantt.entity.enums.ProjectStatus;
import com.liam.gantt.exception.DuplicateProjectNameException;
import com.liam.gantt.exception.ProjectNotFoundException;
import com.liam.gantt.mapper.ProjectMapper;
import com.liam.gantt.repository.ProjectRepository;
import com.liam.gantt.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

/**
 * ProjectServiceImpl 단위 테스트
 * 
 * @author Liam
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService 단위 테스트")
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Project testProject;
    private ProjectRequestDto testRequestDto;
    private ProjectResponseDto testResponseDto;

    @BeforeEach
    void setUp() {
        testProject = Project.builder()
                .name("테스트 프로젝트")
                .description("테스트용 프로젝트입니다")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status(ProjectStatus.PLANNING)
                .build();

        testRequestDto = ProjectRequestDto.builder()
                .name("테스트 프로젝트")
                .description("테스트용 프로젝트입니다")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .build();

        testResponseDto = ProjectResponseDto.builder()
                .name("테스트 프로젝트")
                .description("테스트용 프로젝트입니다")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status(ProjectStatus.PLANNING)
                .taskCount(0)
                .progress(0.0)
                .build();
    }

    @Nested
    @DisplayName("프로젝트 생성 테스트")
    class CreateProjectTest {

        @Test
        @DisplayName("프로젝트 생성 성공")
        void createProject_Success() {
            // Given
            given(projectRepository.existsByName(testRequestDto.getName())).willReturn(false);
            given(projectMapper.toEntity(testRequestDto)).willReturn(testProject);
            given(projectRepository.save(testProject)).willReturn(testProject);
            given(projectMapper.toResponseDto(testProject)).willReturn(testResponseDto);

            // When
            ProjectResponseDto result = projectService.create(testRequestDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("테스트 프로젝트");
            assertThat(result.getStatus()).isEqualTo(ProjectStatus.PLANNING);

            verify(projectRepository).existsByName(testRequestDto.getName());
            verify(projectRepository).save(testProject);
            verify(projectMapper).toEntity(testRequestDto);
            verify(projectMapper).toResponseDto(testProject);
        }

        @Test
        @DisplayName("중복된 프로젝트명으로 생성 실패")
        void createProject_DuplicateName_ThrowsException() {
            // Given
            given(projectRepository.existsByName(testRequestDto.getName())).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> projectService.create(testRequestDto))
                    .isInstanceOf(DuplicateProjectNameException.class)
                    .hasMessageContaining("이미 존재하는 프로젝트명입니다");

            verify(projectRepository).existsByName(testRequestDto.getName());
            verify(projectRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("프로젝트 조회 테스트")
    class FindProjectTest {

        @Test
        @DisplayName("ID로 프로젝트 조회 성공")
        void findById_Success() {
            // Given
            Long projectId = 1L;
            given(projectRepository.findById(projectId)).willReturn(Optional.of(testProject));
            given(projectMapper.toResponseDto(testProject)).willReturn(testResponseDto);

            // When
            ProjectResponseDto result = projectService.findById(projectId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(projectId);
            assertThat(result.getName()).isEqualTo("테스트 프로젝트");

            verify(projectRepository).findById(projectId);
            verify(projectMapper).toResponseDto(testProject);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 예외 발생")
        void findById_NotFound_ThrowsException() {
            // Given
            Long projectId = 999L;
            given(projectRepository.findById(projectId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> projectService.findById(projectId))
                    .isInstanceOf(ProjectNotFoundException.class)
                    .hasMessageContaining("프로젝트를 찾을 수 없습니다");

            verify(projectRepository).findById(projectId);
            verify(projectMapper, never()).toResponseDto(any());
        }

        @Test
        @DisplayName("모든 프로젝트 조회 성공")
        void findAll_Success() {
            // Given
            List<Project> projects = Arrays.asList(testProject);
            List<ProjectResponseDto> expectedDtos = Arrays.asList(testResponseDto);
            
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
        @DisplayName("페이징으로 프로젝트 조회 성공")
        void findAllWithPaging_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
            Page<Project> projectPage = new PageImpl<>(Arrays.asList(testProject), pageable, 1);
            
            given(projectRepository.findAll(pageable)).willReturn(projectPage);
            given(projectMapper.toResponseDto(testProject)).willReturn(testResponseDto);

            // When
            Page<ProjectResponseDto> result = projectService.findAllWithPaging(pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("테스트 프로젝트");

            verify(projectRepository).findAll(pageable);
            verify(projectMapper).toResponseDto(testProject);
        }
    }

    @Nested
    @DisplayName("프로젝트 수정 테스트")
    class UpdateProjectTest {

        @Test
        @DisplayName("프로젝트 수정 성공")
        void updateProject_Success() {
            // Given
            Long projectId = 1L;
            ProjectRequestDto updateRequestDto = ProjectRequestDto.builder()
                    .name("수정된 프로젝트")
                    .description("수정된 설명")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(60))
                    .build();

            Project updatedProject = testProject.toBuilder()
                    .name("수정된 프로젝트")
                    .description("수정된 설명")
                    .endDate(LocalDate.now().plusDays(60))
                    .build();

            ProjectResponseDto updatedResponseDto = testResponseDto.toBuilder()
                    .name("수정된 프로젝트")
                    .description("수정된 설명")
                    .endDate(LocalDate.now().plusDays(60))
                    .build();

            given(projectRepository.findById(projectId)).willReturn(Optional.of(testProject));
            given(projectRepository.existsByNameAndIdNot(updateRequestDto.getName(), projectId)).willReturn(false);
            willDoNothing().given(projectMapper).updateEntity(testProject, updateRequestDto);
            given(projectMapper.toResponseDto(testProject)).willReturn(updatedResponseDto);

            // When
            ProjectResponseDto result = projectService.update(projectId, updateRequestDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("수정된 프로젝트");

            verify(projectRepository).findById(projectId);
            verify(projectRepository).existsByNameAndIdNot(updateRequestDto.getName(), projectId);
            verify(projectMapper).updateEntity(testProject, updateRequestDto);
            verify(projectMapper).toResponseDto(testProject);
        }

        @Test
        @DisplayName("존재하지 않는 프로젝트 수정 시 예외 발생")
        void updateProject_NotFound_ThrowsException() {
            // Given
            Long projectId = 999L;
            given(projectRepository.findById(projectId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> projectService.update(projectId, testRequestDto))
                    .isInstanceOf(ProjectNotFoundException.class)
                    .hasMessageContaining("프로젝트를 찾을 수 없습니다");

            verify(projectRepository).findById(projectId);
            verify(projectMapper, never()).updateEntity(any(), any());
        }
    }

    @Nested
    @DisplayName("프로젝트 삭제 테스트")
    class DeleteProjectTest {

        @Test
        @DisplayName("프로젝트 삭제 성공")
        void deleteProject_Success() {
            // Given
            Long projectId = 1L;
            given(projectRepository.existsById(projectId)).willReturn(true);
            willDoNothing().given(projectRepository).deleteById(projectId);

            // When
            projectService.delete(projectId);

            // Then
            verify(projectRepository).existsById(projectId);
            verify(projectRepository).deleteById(projectId);
        }

        @Test
        @DisplayName("존재하지 않는 프로젝트 삭제 시 예외 발생")
        void deleteProject_NotFound_ThrowsException() {
            // Given
            Long projectId = 999L;
            given(projectRepository.existsById(projectId)).willReturn(false);

            // When & Then
            assertThatThrownBy(() -> projectService.delete(projectId))
                    .isInstanceOf(ProjectNotFoundException.class)
                    .hasMessageContaining("프로젝트를 찾을 수 없습니다");

            verify(projectRepository).existsById(projectId);
            verify(projectRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("프로젝트 검색 테스트")
    class SearchProjectTest {

        @Test
        @DisplayName("이름으로 프로젝트 검색 성공")
        void searchByName_Success() {
            // Given
            String searchKeyword = "테스트";
            List<Project> projects = Arrays.asList(testProject);
            
            given(projectRepository.findByNameContainingIgnoreCase(searchKeyword)).willReturn(projects);
            given(projectMapper.toResponseDto(testProject)).willReturn(testResponseDto);

            // When
            List<ProjectResponseDto> result = projectService.search(searchKeyword, null);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).contains("테스트");

            verify(projectRepository).findByNameContainingIgnoreCase(searchKeyword);
            verify(projectMapper).toResponseDto(testProject);
        }

        @Test
        @DisplayName("상태로 프로젝트 검색 성공")
        void searchByStatus_Success() {
            // Given
            String status = "PLANNING";
            List<Project> projects = Arrays.asList(testProject);
            
            given(projectRepository.findByStatus(ProjectStatus.PLANNING)).willReturn(projects);
            given(projectMapper.toResponseDto(testProject)).willReturn(testResponseDto);

            // When
            List<ProjectResponseDto> result = projectService.search(null, status);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(ProjectStatus.PLANNING);

            verify(projectRepository).findByStatus(ProjectStatus.PLANNING);
            verify(projectMapper).toResponseDto(testProject);
        }
    }

    @Nested
    @DisplayName("프로젝트 통계 테스트")
    class ProjectStatisticsTest {

        @Test
        @DisplayName("전체 프로젝트 개수 조회 성공")
        void countAll_Success() {
            // Given
            long expectedCount = 5L;
            given(projectRepository.count()).willReturn(expectedCount);

            // When
            long result = projectService.countAll();

            // Then
            assertThat(result).isEqualTo(expectedCount);
            verify(projectRepository).count();
        }

        @Test
        @DisplayName("상태별 프로젝트 개수 조회 성공")
        void countByStatus_Success() {
            // Given
            String status = "IN_PROGRESS";
            long expectedCount = 3L;
            given(projectRepository.countByStatus(ProjectStatus.IN_PROGRESS)).willReturn(expectedCount);

            // When
            long result = projectService.countByStatus(status);

            // Then
            assertThat(result).isEqualTo(expectedCount);
            verify(projectRepository).countByStatus(ProjectStatus.IN_PROGRESS);
        }
    }
}