package com.liam.gantt.service.impl;

import com.liam.gantt.dto.request.TaskRequestDto;
import com.liam.gantt.dto.response.TaskDependencyResponseDto;
import com.liam.gantt.dto.response.TaskResponseDto;
import com.liam.gantt.entity.Project;
import com.liam.gantt.entity.Task;
import com.liam.gantt.entity.TaskDependency;
import com.liam.gantt.entity.enums.DependencyType;
import com.liam.gantt.entity.enums.ProjectStatus;
import com.liam.gantt.entity.enums.TaskStatus;
import com.liam.gantt.exception.ProjectNotFoundException;
import com.liam.gantt.exception.TaskNotFoundException;
import com.liam.gantt.mapper.TaskMapper;
import com.liam.gantt.repository.ProjectRepository;
import com.liam.gantt.repository.TaskDependencyRepository;
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
@DisplayName("TaskServiceImpl 단위 테스트")
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskDependencyRepository taskDependencyRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Project testProject;
    private Task testTask;
    private Task parentTask;
    private Task childTask;
    private TaskRequestDto testRequestDto;
    private TaskResponseDto testResponseDto;
    private TaskDependency testDependency;

    @BeforeEach
    void setUp() {
        // 테스트 프로젝트
        testProject = Project.builder()
                .id(1L)
                .name("테스트 프로젝트")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .status(ProjectStatus.IN_PROGRESS)
                .build();

        // 테스트 태스크
        testTask = Task.builder()
                .id(1L)
                .project(testProject)
                .name("테스트 태스크")
                .description("테스트 태스크 설명")
                .startDate(LocalDate.of(2025, 1, 15))
                .endDate(LocalDate.of(2025, 2, 15))
                .duration(31)
                .progress(BigDecimal.valueOf(50))
                .status(TaskStatus.IN_PROGRESS)
                .build();

        // 부모 태스크
        parentTask = Task.builder()
                .id(2L)
                .project(testProject)
                .name("부모 태스크")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 3, 31))
                .duration(90)
                .progress(BigDecimal.ZERO)
                .status(TaskStatus.NOT_STARTED)
                .build();

        // 자식 태스크
        childTask = Task.builder()
                .id(3L)
                .project(testProject)
                .parentTask(testTask)
                .name("자식 태스크")
                .startDate(LocalDate.of(2025, 1, 20))
                .endDate(LocalDate.of(2025, 2, 10))
                .duration(21)
                .progress(BigDecimal.valueOf(25))
                .status(TaskStatus.IN_PROGRESS)
                .build();

        // 테스트 요청 DTO
        testRequestDto = TaskRequestDto.builder()
                .name("테스트 태스크")
                .description("테스트 태스크 설명")
                .startDate(LocalDate.of(2025, 1, 15))
                .endDate(LocalDate.of(2025, 2, 15))
                .duration(31)
                .progress(BigDecimal.valueOf(50))
                .status(TaskStatus.IN_PROGRESS)
                .build();

        // 테스트 응답 DTO
        testResponseDto = TaskResponseDto.builder()
                .id(1L)
                .projectId(1L)
                .name("테스트 태스크")
                .description("테스트 태스크 설명")
                .startDate(LocalDate.of(2025, 1, 15))
                .endDate(LocalDate.of(2025, 2, 15))
                .duration(31)
                .progress(BigDecimal.valueOf(50))
                .status(TaskStatus.IN_PROGRESS)
                .build();

        // 테스트 의존성
        testDependency = TaskDependency.builder()
                .id(1L)
                .predecessor(parentTask)
                .successor(testTask)
                .dependencyType(DependencyType.FINISH_TO_START)
                .lagDays(0)
                .build();
    }

    @Test
    @DisplayName("태스크 생성 - 성공")
    void createTask_Success() {
        // Given
        given(projectRepository.findById(1L)).willReturn(Optional.of(testProject));
        given(taskMapper.toEntity(testRequestDto)).willReturn(testTask);
        given(taskRepository.save(any(Task.class))).willReturn(testTask);
        given(taskMapper.toResponseDto(testTask)).willReturn(testResponseDto);

        // When
        TaskResponseDto result = taskService.create(1L, testRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("테스트 태스크");

        verify(projectRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
        verify(taskMapper).toResponseDto(testTask);
    }

    @Test
    @DisplayName("태스크 생성 - 실패 (프로젝트 없음)")
    void createTask_ProjectNotFound() {
        // Given
        given(projectRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.create(999L, testRequestDto))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining("프로젝트를 찾을 수 없습니다");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("태스크 ID로 조회 - 성공")
    void findById_Success() {
        // Given
        given(taskRepository.findById(1L)).willReturn(Optional.of(testTask));
        given(taskMapper.toResponseDto(testTask)).willReturn(testResponseDto);

        // When
        TaskResponseDto result = taskService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("테스트 태스크");

        verify(taskRepository).findById(1L);
        verify(taskMapper).toResponseDto(testTask);
    }

    @Test
    @DisplayName("태스크 ID로 조회 - 실패 (존재하지 않음)")
    void findById_NotFound() {
        // Given
        given(taskRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.findById(999L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("태스크를 찾을 수 없습니다");

        verify(taskRepository).findById(999L);
    }

    @Test
    @DisplayName("프로젝트별 태스크 목록 조회")
    void findByProjectId_Success() {
        // Given
        List<Task> tasks = Arrays.asList(testTask, childTask);
        given(taskRepository.findByProjectId(1L)).willReturn(tasks);
        given(taskMapper.toResponseDto(testTask)).willReturn(testResponseDto);
        given(taskMapper.toResponseDto(childTask)).willReturn(
                TaskResponseDto.builder()
                        .id(3L)
                        .projectId(1L)
                        .parentTaskId(1L)
                        .name("자식 태스크")
                        .build()
        );

        // When
        List<TaskResponseDto> result = taskService.findByProjectId(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("테스트 태스크");
        assertThat(result.get(1).getName()).isEqualTo("자식 태스크");

        verify(taskRepository).findByProjectId(1L);
    }

    @Test
    @DisplayName("프로젝트별 태스크 페이징 조회")
    void findByProjectIdWithPaging_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(Arrays.asList(testTask));
        given(taskRepository.findByProjectId(1L, pageable)).willReturn(taskPage);
        given(taskMapper.toResponseDto(testTask)).willReturn(testResponseDto);

        // When
        Page<TaskResponseDto> result = taskService.findByProjectIdWithPaging(1L, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("테스트 태스크");

        verify(taskRepository).findByProjectId(1L, pageable);
    }

    @Test
    @DisplayName("부모 태스크 ID로 하위 태스크 조회")
    void findByParentTaskId_Success() {
        // Given
        List<Task> childTasks = Arrays.asList(childTask);
        given(taskRepository.findByParentTaskId(1L)).willReturn(childTasks);
        given(taskMapper.toResponseDto(childTask)).willReturn(
                TaskResponseDto.builder()
                        .id(3L)
                        .parentTaskId(1L)
                        .name("자식 태스크")
                        .build()
        );

        // When
        List<TaskResponseDto> result = taskService.findByParentTaskId(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getParentTaskId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("자식 태스크");

        verify(taskRepository).findByParentTaskId(1L);
    }

    @Test
    @DisplayName("태스크 수정 - 성공")
    void updateTask_Success() {
        // Given
        TaskRequestDto updateRequest = TaskRequestDto.builder()
                .name("수정된 태스크")
                .description("수정된 설명")
                .startDate(LocalDate.of(2025, 2, 1))
                .endDate(LocalDate.of(2025, 3, 1))
                .progress(BigDecimal.valueOf(75))
                .status(TaskStatus.IN_PROGRESS)
                .build();

        given(taskRepository.findById(1L)).willReturn(Optional.of(testTask));
        given(taskRepository.save(any(Task.class))).willReturn(testTask);
        given(taskMapper.toResponseDto(any(Task.class))).willReturn(
                TaskResponseDto.builder()
                        .id(1L)
                        .name("수정된 태스크")
                        .progress(BigDecimal.valueOf(75))
                        .build()
        );

        // When
        TaskResponseDto result = taskService.update(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("수정된 태스크");
        assertThat(result.getProgress()).isEqualTo(BigDecimal.valueOf(75));

        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("태스크 삭제 - 성공")
    void deleteTask_Success() {
        // Given
        given(taskRepository.existsById(1L)).willReturn(true);
        doNothing().when(taskRepository).deleteById(1L);

        // When
        taskService.delete(1L);

        // Then
        verify(taskRepository).existsById(1L);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    @DisplayName("태스크 진행률 업데이트")
    void updateProgress_Success() {
        // Given
        BigDecimal newProgress = BigDecimal.valueOf(100);
        given(taskRepository.findById(1L)).willReturn(Optional.of(testTask));
        given(taskRepository.save(any(Task.class))).willReturn(testTask);
        given(taskMapper.toResponseDto(any(Task.class))).willReturn(
                TaskResponseDto.builder()
                        .id(1L)
                        .progress(newProgress)
                        .status(TaskStatus.COMPLETED)
                        .build()
        );

        // When
        TaskResponseDto result = taskService.updateProgress(1L, newProgress);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProgress()).isEqualTo(newProgress);
        assertThat(result.getStatus()).isEqualTo(TaskStatus.COMPLETED);

        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("태스크 상태 변경")
    void updateStatus_Success() {
        // Given
        TaskStatus newStatus = TaskStatus.COMPLETED;
        given(taskRepository.findById(1L)).willReturn(Optional.of(testTask));
        given(taskRepository.save(any(Task.class))).willReturn(testTask);
        given(taskMapper.toResponseDto(any(Task.class))).willReturn(
                TaskResponseDto.builder()
                        .id(1L)
                        .status(newStatus)
                        .build()
        );

        // When
        TaskResponseDto result = taskService.updateStatus(1L, newStatus);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(newStatus);

        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("의존성 포함 태스크 조회")
    void findByIdWithDependencies_Success() {
        // Given
        testTask.setPredecessorDependencies(Arrays.asList(testDependency));
        given(taskRepository.findByIdWithDependencies(1L)).willReturn(Optional.of(testTask));
        given(taskMapper.toResponseDto(testTask)).willReturn(testResponseDto);

        // When
        TaskResponseDto result = taskService.findByIdWithDependencies(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(taskRepository).findByIdWithDependencies(1L);
    }

    @Test
    @DisplayName("루트 태스크 조회")
    void findRootTasks_Success() {
        // Given
        List<Task> rootTasks = Arrays.asList(parentTask);
        given(taskRepository.findByProjectIdAndParentTaskIsNull(1L)).willReturn(rootTasks);
        given(taskMapper.toResponseDto(parentTask)).willReturn(
                TaskResponseDto.builder()
                        .id(2L)
                        .name("부모 태스크")
                        .parentTaskId(null)
                        .build()
        );

        // When
        List<TaskResponseDto> result = taskService.findRootTasks(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getParentTaskId()).isNull();

        verify(taskRepository).findByProjectIdAndParentTaskIsNull(1L);
    }

    @Test
    @DisplayName("지연된 태스크 조회")
    void findOverdueTasks_Success() {
        // Given
        Task overdueTask = Task.builder()
                .id(4L)
                .project(testProject)
                .name("지연된 태스크")
                .endDate(LocalDate.now().minusDays(1))
                .status(TaskStatus.IN_PROGRESS)
                .build();

        given(taskRepository.findOverdueTasks(eq(1L), any(LocalDate.class)))
                .willReturn(Arrays.asList(overdueTask));
        given(taskMapper.toResponseDto(overdueTask)).willReturn(
                TaskResponseDto.builder()
                        .id(4L)
                        .name("지연된 태스크")
                        .endDate(LocalDate.now().minusDays(1))
                        .status(TaskStatus.IN_PROGRESS)
                        .build()
        );

        // When
        List<TaskResponseDto> result = taskService.findOverdueTasks(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("지연된 태스크");

        verify(taskRepository).findOverdueTasks(eq(1L), any(LocalDate.class));
    }

    @Test
    @DisplayName("태스크 검색 - 이름과 상태")
    void search_Success() {
        // Given
        given(taskRepository.findByProjectId(1L)).willReturn(Arrays.asList(testTask));
        given(taskMapper.toResponseDto(testTask)).willReturn(testResponseDto);

        // When
        List<TaskResponseDto> result = taskService.search(1L, "테스트", "IN_PROGRESS");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("테스트 태스크");

        verify(taskRepository).findByProjectId(1L);
    }

    @Test
    @DisplayName("프로젝트 ID와 상태별 태스크 조회")
    void findByProjectIdAndStatus_Success() {
        // Given
        given(taskRepository.findByProjectIdAndStatus(1L, TaskStatus.IN_PROGRESS))
                .willReturn(Arrays.asList(testTask));
        given(taskMapper.toResponseDto(testTask)).willReturn(testResponseDto);

        // When
        List<TaskResponseDto> result = taskService.findByProjectIdAndStatus(1L, TaskStatus.IN_PROGRESS);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);

        verify(taskRepository).findByProjectIdAndStatus(1L, TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("태스크 이름으로 검색")
    void searchByName_Success() {
        // Given
        given(taskRepository.findByProjectIdAndNameContainingIgnoreCase(1L, "테스트"))
                .willReturn(Arrays.asList(testTask));
        given(taskMapper.toResponseDto(testTask)).willReturn(testResponseDto);

        // When
        List<TaskResponseDto> result = taskService.searchByName(1L, "테스트");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).contains("테스트");

        verify(taskRepository).findByProjectIdAndNameContainingIgnoreCase(1L, "테스트");
    }

    @Test
    @DisplayName("태스크 이동 - 날짜 오프셋 적용")
    void moveTask_Success() {
        // Given
        Integer dayOffset = 7;
        given(taskRepository.findById(1L)).willReturn(Optional.of(testTask));
        given(taskRepository.save(any(Task.class))).willReturn(testTask);
        given(taskMapper.toResponseDto(any(Task.class))).willReturn(
                TaskResponseDto.builder()
                        .id(1L)
                        .startDate(LocalDate.of(2025, 1, 22))
                        .endDate(LocalDate.of(2025, 2, 22))
                        .build()
        );

        // When
        TaskResponseDto result = taskService.moveTask(1L, dayOffset);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2025, 1, 22));

        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("하위 태스크 추가")
    void addSubTask_Success() {
        // Given
        TaskRequestDto subTaskRequest = TaskRequestDto.builder()
                .name("새 하위 태스크")
                .startDate(LocalDate.of(2025, 2, 1))
                .endDate(LocalDate.of(2025, 2, 10))
                .build();

        given(taskRepository.findById(1L)).willReturn(Optional.of(testTask));
        given(projectRepository.findById(1L)).willReturn(Optional.of(testProject));
        given(taskMapper.toEntity(subTaskRequest)).willReturn(childTask);
        given(taskRepository.save(any(Task.class))).willReturn(childTask);
        given(taskMapper.toResponseDto(childTask)).willReturn(
                TaskResponseDto.builder()
                        .id(3L)
                        .parentTaskId(1L)
                        .name("새 하위 태스크")
                        .build()
        );

        // When
        TaskResponseDto result = taskService.addSubTask(1L, subTaskRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getParentTaskId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("새 하위 태스크");

        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }
}