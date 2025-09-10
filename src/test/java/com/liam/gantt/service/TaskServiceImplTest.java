package com.liam.gantt.service;

import com.liam.gantt.dto.request.TaskRequestDto;
import com.liam.gantt.dto.response.TaskResponseDto;
import com.liam.gantt.entity.Project;
import com.liam.gantt.entity.Task;
import com.liam.gantt.entity.enums.ProjectStatus;
import com.liam.gantt.entity.enums.TaskStatus;
import com.liam.gantt.exception.ProjectNotFoundException;
import com.liam.gantt.exception.TaskNotFoundException;
import com.liam.gantt.mapper.TaskMapper;
import com.liam.gantt.repository.ProjectRepository;
import com.liam.gantt.repository.TaskRepository;
import com.liam.gantt.repository.TaskDependencyRepository;
import com.liam.gantt.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
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
 * TaskServiceImpl 단위 테스트
 * 
 * @author Liam
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService 단위 테스트")
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private TaskDependencyRepository dependencyRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Project testProject;
    private Task testTask;
    private TaskRequestDto testRequestDto;
    private TaskResponseDto testResponseDto;

    @BeforeEach
    void setUp() {
        testProject = Project.builder()
                .name("테스트 프로젝트")
                .description("테스트용 프로젝트")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status(ProjectStatus.IN_PROGRESS)
                .build();
        testProject.setId(1L);

        testTask = Task.builder()
                .project(testProject)
                .name("테스트 태스크")
                .description("테스트용 태스크")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(5))
                .duration(5)
                .progress(BigDecimal.valueOf(30.0))
                .status(TaskStatus.IN_PROGRESS)
                .build();
        testTask.setId(1L);

        testRequestDto = TaskRequestDto.builder()
                .name("테스트 태스크")
                .description("테스트용 태스크")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(5))
                .duration(5)
                .progress(BigDecimal.valueOf(30.0))
                .build();

        testResponseDto = TaskResponseDto.builder()
                .id(1L)
                .projectId(1L)
                .name("테스트 태스크")
                .description("테스트용 태스크")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(5))
                .duration(5)
                .progress(BigDecimal.valueOf(30.0))
                .status(TaskStatus.IN_PROGRESS)
                .build();
    }

    @Nested
    @DisplayName("태스크 생성 테스트")
    class CreateTaskTest {

        @Test
        @DisplayName("태스크 생성 성공")
        void createTask_Success() {
            // Given
            Long projectId = 1L;
            given(projectRepository.findById(projectId)).willReturn(Optional.of(testProject));
            given(taskMapper.toEntity(testRequestDto)).willReturn(testTask);
            given(taskRepository.save(testTask)).willReturn(testTask);
            given(taskMapper.toResponseDto(testTask)).willReturn(testResponseDto);

            // When
            TaskResponseDto result = taskService.create(projectId, testRequestDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("테스트 태스크");
            assertThat(result.getProjectId()).isEqualTo(projectId);
            assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);

            verify(projectRepository).findById(projectId);
            verify(taskRepository).save(testTask);
            verify(taskMapper).toEntity(testRequestDto);
            verify(taskMapper).toResponseDto(testTask);
        }

        @Test
        @DisplayName("존재하지 않는 프로젝트에 태스크 생성 시 예외 발생")
        void createTask_ProjectNotFound_ThrowsException() {
            // Given
            Long projectId = 999L;
            given(projectRepository.findById(projectId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> taskService.create(projectId, testRequestDto))
                    .isInstanceOf(ProjectNotFoundException.class)
                    .hasMessageContaining("프로젝트를 찾을 수 없습니다");

            verify(projectRepository).findById(projectId);
            verify(taskRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("태스크 조회 테스트")
    class FindTaskTest {

        @Test
        @DisplayName("ID로 태스크 조회 성공")
        void findById_Success() {
            // Given
            Long taskId = 1L;
            given(taskRepository.findById(taskId)).willReturn(Optional.of(testTask));
            given(taskMapper.toResponseDto(testTask)).willReturn(testResponseDto);

            // When
            TaskResponseDto result = taskService.findById(taskId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(taskId);
            assertThat(result.getName()).isEqualTo("테스트 태스크");

            verify(taskRepository).findById(taskId);
            verify(taskMapper).toResponseDto(testTask);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 예외 발생")
        void findById_NotFound_ThrowsException() {
            // Given
            Long taskId = 999L;
            given(taskRepository.findById(taskId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> taskService.findById(taskId))
                    .isInstanceOf(TaskNotFoundException.class)
                    .hasMessageContaining("태스크를 찾을 수 없습니다");

            verify(taskRepository).findById(taskId);
            verify(taskMapper, never()).toResponseDto(any());
        }

        @Test
        @DisplayName("프로젝트별 태스크 목록 조회 성공")
        void findByProjectId_Success() {
            // Given
            Long projectId = 1L;
            List<Task> tasks = Arrays.asList(testTask);
            
            given(taskRepository.findByProjectId(projectId)).willReturn(tasks);
            given(taskMapper.toResponseDto(testTask)).willReturn(testResponseDto);

            // When
            List<TaskResponseDto> result = taskService.findByProjectId(projectId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getProjectId()).isEqualTo(projectId);
            assertThat(result.get(0).getName()).isEqualTo("테스트 태스크");

            verify(taskRepository).findByProjectId(projectId);
            verify(taskMapper).toResponseDto(testTask);
        }

        @Test
        @DisplayName("프로젝트별 태스크 페이징 조회 성공")
        void findByProjectIdWithPaging_Success() {
            // Given
            Long projectId = 1L;
            Pageable pageable = PageRequest.of(0, 10, Sort.by("startDate"));
            Page<Task> taskPage = new PageImpl<>(Arrays.asList(testTask), pageable, 1);
            
            given(taskRepository.findByProjectId(projectId, pageable)).willReturn(taskPage);
            given(taskMapper.toResponseDto(testTask)).willReturn(testResponseDto);

            // When
            Page<TaskResponseDto> result = taskService.findByProjectIdWithPaging(projectId, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getProjectId()).isEqualTo(projectId);

            verify(taskRepository).findByProjectId(projectId, pageable);
            verify(taskMapper).toResponseDto(testTask);
        }
    }

    @Nested
    @DisplayName("태스크 수정 테스트")
    class UpdateTaskTest {

        @Test
        @DisplayName("태스크 수정 성공")
        void updateTask_Success() {
            // Given
            Long taskId = 1L;
            TaskRequestDto updateRequestDto = TaskRequestDto.builder()
                    .name("수정된 태스크")
                    .description("수정된 설명")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(7))
                    .duration(7)
                    .progress(BigDecimal.valueOf(50.0))
                    .build();

            TaskResponseDto updatedResponseDto = testResponseDto.toBuilder()
                    .name("수정된 태스크")
                    .description("수정된 설명")
                    .endDate(LocalDate.now().plusDays(7))
                    .duration(7)
                    .progress(BigDecimal.valueOf(50.0))
                    .build();

            given(taskRepository.findById(taskId)).willReturn(Optional.of(testTask));
            willDoNothing().given(taskMapper).updateEntity(testTask, updateRequestDto);
            given(taskRepository.save(testTask)).willReturn(testTask);
            given(taskMapper.toResponseDto(testTask)).willReturn(updatedResponseDto);

            // When
            TaskResponseDto result = taskService.update(taskId, updateRequestDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("수정된 태스크");
            assertThat(result.getDuration()).isEqualTo(7);

            verify(taskRepository).findById(taskId);
            verify(taskMapper).updateEntity(testTask, updateRequestDto);
            verify(taskRepository).save(testTask);
            verify(taskMapper).toResponseDto(testTask);
        }

        @Test
        @DisplayName("존재하지 않는 태스크 수정 시 예외 발생")
        void updateTask_NotFound_ThrowsException() {
            // Given
            Long taskId = 999L;
            given(taskRepository.findById(taskId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> taskService.update(taskId, testRequestDto))
                    .isInstanceOf(TaskNotFoundException.class)
                    .hasMessageContaining("태스크를 찾을 수 없습니다");

            verify(taskRepository).findById(taskId);
            verify(taskMapper, never()).updateEntity(any(), any());
        }
    }

    @Nested
    @DisplayName("태스크 삭제 테스트")
    class DeleteTaskTest {

        @Test
        @DisplayName("태스크 삭제 성공")
        void deleteTask_Success() {
            // Given
            Long taskId = 1L;
            given(taskRepository.findById(taskId)).willReturn(Optional.of(testTask));
            willDoNothing().given(dependencyRepository).deleteByPredecessorIdOrSuccessorId(taskId, taskId);
            willDoNothing().given(taskRepository).deleteById(taskId);

            // When
            taskService.delete(taskId);

            // Then
            verify(taskRepository).findById(taskId);
            verify(dependencyRepository).deleteByPredecessorIdOrSuccessorId(taskId, taskId);
            verify(taskRepository).deleteById(taskId);
        }

        @Test
        @DisplayName("존재하지 않는 태스크 삭제 시 예외 발생")
        void deleteTask_NotFound_ThrowsException() {
            // Given
            Long taskId = 999L;
            given(taskRepository.findById(taskId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> taskService.delete(taskId))
                    .isInstanceOf(TaskNotFoundException.class)
                    .hasMessageContaining("태스크를 찾을 수 없습니다");

            verify(taskRepository).findById(taskId);
            verify(taskRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("태스크 상태 및 진행률 테스트")
    class TaskProgressTest {

        @Test
        @DisplayName("태스크 진행률 업데이트 성공")
        void updateTaskProgress_Success() {
            // Given
            Long taskId = 1L;
            BigDecimal newProgress = BigDecimal.valueOf(75.0);
            TaskStatus newStatus = TaskStatus.IN_PROGRESS;

            given(taskRepository.findById(taskId)).willReturn(Optional.of(testTask));
            given(taskRepository.save(testTask)).willReturn(testTask);
            given(taskMapper.toResponseDto(testTask)).willReturn(testResponseDto);

            // When
            TaskResponseDto result = taskService.updateProgress(taskId, newProgress);

            // Then
            assertThat(result).isNotNull();
            verify(taskRepository).findById(taskId);
            verify(taskRepository).save(testTask);
        }

        @Test
        @DisplayName("태스크 상태 변경 성공")
        void updateTaskStatus_Success() {
            // Given
            Long taskId = 1L;
            TaskStatus newStatus = TaskStatus.COMPLETED;

            given(taskRepository.findById(taskId)).willReturn(Optional.of(testTask));
            given(taskRepository.save(testTask)).willReturn(testTask);
            given(taskMapper.toResponseDto(testTask)).willReturn(testResponseDto);

            // When
            TaskResponseDto result = taskService.updateStatus(taskId, newStatus);

            // Then
            assertThat(result).isNotNull();
            verify(taskRepository).findById(taskId);
            verify(taskRepository).save(testTask);
        }
    }

    @Nested
    @DisplayName("태스크 검색 및 필터링 테스트")
    class SearchTaskTest {

        @Test
        @DisplayName("상태별 태스크 조회 성공")
        void findByStatus_Success() {
            // Given
            Long projectId = 1L;
            TaskStatus status = TaskStatus.IN_PROGRESS;
            List<Task> tasks = Arrays.asList(testTask);

            given(taskRepository.findByProjectIdAndStatus(projectId, status)).willReturn(tasks);
            given(taskMapper.toResponseDto(testTask)).willReturn(testResponseDto);

            // When
            List<TaskResponseDto> result = taskService.findByProjectIdAndStatus(projectId, status);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(status);

            verify(taskRepository).findByProjectIdAndStatus(projectId, status);
            verify(taskMapper).toResponseDto(testTask);
        }

        @Test
        @DisplayName("이름으로 태스크 검색 성공")
        void searchByName_Success() {
            // Given
            Long projectId = 1L;
            String keyword = "테스트";
            List<Task> tasks = Arrays.asList(testTask);

            given(taskRepository.findByProjectIdAndNameContainingIgnoreCase(projectId, keyword)).willReturn(tasks);
            given(taskMapper.toResponseDto(testTask)).willReturn(testResponseDto);

            // When
            List<TaskResponseDto> result = taskService.searchByName(projectId, keyword);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).contains("테스트");

            verify(taskRepository).findByProjectIdAndNameContainingIgnoreCase(projectId, keyword);
            verify(taskMapper).toResponseDto(testTask);
        }
    }
}