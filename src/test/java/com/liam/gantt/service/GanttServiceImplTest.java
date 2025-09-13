package com.liam.gantt.service;

import com.liam.gantt.dto.response.GanttChartDto;
import com.liam.gantt.dto.response.ProjectResponseDto;
import com.liam.gantt.dto.response.TaskResponseDto;
import com.liam.gantt.entity.Project;
import com.liam.gantt.entity.Task;
import com.liam.gantt.entity.enums.ProjectStatus;
import com.liam.gantt.entity.enums.TaskStatus;
import com.liam.gantt.exception.ProjectNotFoundException;
import com.liam.gantt.repository.ProjectRepository;
import com.liam.gantt.repository.TaskRepository;
import com.liam.gantt.repository.TaskDependencyRepository;
import com.liam.gantt.service.ProjectService;
import com.liam.gantt.service.TaskService;
import com.liam.gantt.service.impl.GanttServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * GanttServiceImpl 단위 테스트
 * 
 * @author Liam
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GanttService 단위 테스트")
class GanttServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private TaskDependencyRepository dependencyRepository;
    
    @Mock
    private ProjectService projectService;
    
    @Mock
    private TaskService taskService;

    @InjectMocks
    private GanttServiceImpl ganttService;

    private Project testProject;
    private List<Task> testTasks;
    private LocalDate projectStartDate;
    private LocalDate projectEndDate;

    @BeforeEach
    void setUp() {
        projectStartDate = LocalDate.of(2024, 1, 1);
        projectEndDate = LocalDate.of(2024, 1, 31);

        testProject = Project.builder()
                .name("테스트 프로젝트")
                .description("간트차트 테스트용 프로젝트")
                .startDate(projectStartDate)
                .endDate(projectEndDate)
                .status(ProjectStatus.IN_PROGRESS)
                .build();

        Task task1 = Task.builder()
                .project(testProject)
                .name("기획 단계")
                .description("프로젝트 기획 및 요구사항 분석")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 5))
                .duration(5)
                .progress(BigDecimal.valueOf(100.0))
                .status(TaskStatus.COMPLETED)
                .build();

        Task task2 = Task.builder()
                .project(testProject)
                .name("개발 단계")
                .description("시스템 개발 및 구현")
                .startDate(LocalDate.of(2024, 1, 6))
                .endDate(LocalDate.of(2024, 1, 20))
                .duration(15)
                .progress(BigDecimal.valueOf(60.0))
                .status(TaskStatus.IN_PROGRESS)
                .build();

        Task task3 = Task.builder()
                .project(testProject)
                .name("테스트 단계")
                .description("시스템 테스트 및 검증")
                .startDate(LocalDate.of(2024, 1, 21))
                .endDate(LocalDate.of(2024, 1, 31))
                .duration(11)
                .progress(BigDecimal.valueOf(0.0))
                .status(TaskStatus.NOT_STARTED)
                .build();

        testTasks = Arrays.asList(task1, task2, task3);
    }

    @Nested
    @DisplayName("간트차트 데이터 조회 테스트")
    class GetGanttChartDataTest {

        @Test
        @DisplayName("간트차트 데이터 조회 성공")
        void getGanttChartData_Success() {
            // Given
            Long projectId = 1L;
            testProject.setId(projectId);
            
            ProjectResponseDto mockProjectDto = ProjectResponseDto.builder()
                    .id(projectId)
                    .name("테스트 프로젝트")
                    .startDate(projectStartDate)
                    .endDate(projectEndDate)
                    .status(ProjectStatus.IN_PROGRESS)
                    .build();
                    
            List<TaskResponseDto> mockTaskDtos = List.of(
                TaskResponseDto.builder()
                    .id(1L)
                    .name("기획 단계")
                    .status(TaskStatus.COMPLETED)
                    .build(),
                TaskResponseDto.builder()
                    .id(2L)
                    .name("개발 단계")
                    .status(TaskStatus.IN_PROGRESS)
                    .build(),
                TaskResponseDto.builder()
                    .id(3L)
                    .name("테스트 단계")
                    .status(TaskStatus.NOT_STARTED)
                    .build()
            );
            
            given(projectRepository.findByIdWithTasks(projectId)).willReturn(Optional.of(testProject));
            given(projectService.findById(projectId)).willReturn(mockProjectDto);
            given(taskService.findTaskHierarchyByProjectId(projectId)).willReturn(mockTaskDtos);
            given(dependencyRepository.findByProjectId(projectId)).willReturn(List.of());
            given(taskRepository.findCriticalPathTasks(projectId)).willReturn(List.of());

            // When
            GanttChartDto result = ganttService.getGanttChart(projectId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getProject()).isNotNull();
            assertThat(result.getTasks()).isNotNull();
            assertThat(result.getTimeline()).isNotNull();

            // 프로젝트 정보 검증
            ProjectResponseDto projectInfo = result.getProject();
            assertThat(projectInfo.getName()).isEqualTo("테스트 프로젝트");
            assertThat(projectInfo.getStartDate()).isEqualTo(projectStartDate);
            assertThat(projectInfo.getEndDate()).isEqualTo(projectEndDate);

            // 태스크 정보 검증
            List<TaskResponseDto> tasks = result.getTasks();
            assertThat(tasks).hasSize(3);
            assertThat(tasks.get(0).getName()).isEqualTo("기획 단계");
            assertThat(tasks.get(1).getName()).isEqualTo("개발 단계");
            assertThat(tasks.get(2).getName()).isEqualTo("테스트 단계");

            verify(projectRepository).findByIdWithTasks(projectId);
            verify(projectService).findById(projectId);
            verify(taskService).findTaskHierarchyByProjectId(projectId);
        }

        @Test
        @DisplayName("존재하지 않는 프로젝트 조회 시 예외 발생")
        void getGanttChartData_ProjectNotFound_ThrowsException() {
            // Given
            Long projectId = 999L;
            given(projectRepository.findByIdWithTasks(projectId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> ganttService.getGanttChart(projectId))
                    .isInstanceOf(ProjectNotFoundException.class)
                    .hasMessageContaining("프로젝트를 찾을 수 없습니다");

            verify(projectRepository).findByIdWithTasks(projectId);
        }

        @Test
        @DisplayName("태스크가 없는 프로젝트 조회 성공")
        void getGanttChartData_NoTasks_Success() {
            // Given
            Long projectId = 1L;
            testProject.setId(projectId);
            
            ProjectResponseDto mockProjectDto = ProjectResponseDto.builder()
                    .id(projectId)
                    .name("테스트 프로젝트")
                    .startDate(projectStartDate)
                    .endDate(projectEndDate)
                    .status(ProjectStatus.IN_PROGRESS)
                    .build();
            
            given(projectRepository.findByIdWithTasks(projectId)).willReturn(Optional.of(testProject));
            given(projectService.findById(projectId)).willReturn(mockProjectDto);
            given(taskService.findTaskHierarchyByProjectId(projectId)).willReturn(List.of());
            given(dependencyRepository.findByProjectId(projectId)).willReturn(List.of());
            given(taskRepository.findCriticalPathTasks(projectId)).willReturn(List.of());

            // When
            GanttChartDto result = ganttService.getGanttChart(projectId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getProject()).isNotNull();
            assertThat(result.getTasks()).isNotNull();
            assertThat(result.getTimeline()).isNotNull();

            List<TaskResponseDto> tasks = result.getTasks();
            assertThat(tasks).isEmpty();

            verify(projectRepository).findByIdWithTasks(projectId);
            verify(projectService).findById(projectId);
            verify(taskService).findTaskHierarchyByProjectId(projectId);
        }
    }

    @Nested
    @DisplayName("태스크 위치 계산 테스트")
    class CalculateTaskPositionTest {

        @Test
        @DisplayName("태스크 위치 및 크기 계산 검증")
        void calculateTaskPosition_Success() {
            // Given
            Long projectId = 1L;
            testProject.setId(projectId);
            
            ProjectResponseDto mockProjectDto = ProjectResponseDto.builder()
                    .id(projectId)
                    .name("테스트 프로젝트")
                    .startDate(projectStartDate)
                    .endDate(projectEndDate)
                    .status(ProjectStatus.IN_PROGRESS)
                    .build();
            
            List<TaskResponseDto> mockTaskDtos = List.of(
                TaskResponseDto.builder()
                    .id(1L)
                    .name("기획 단계")
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 1, 5))
                    .status(TaskStatus.COMPLETED)
                    .build(),
                TaskResponseDto.builder()
                    .id(2L)
                    .name("개발 단계")
                    .startDate(LocalDate.of(2024, 1, 6))
                    .endDate(LocalDate.of(2024, 1, 20))
                    .status(TaskStatus.IN_PROGRESS)
                    .build(),
                TaskResponseDto.builder()
                    .id(3L)
                    .name("테스트 단계")
                    .startDate(LocalDate.of(2024, 1, 21))
                    .endDate(LocalDate.of(2024, 1, 31))
                    .status(TaskStatus.NOT_STARTED)
                    .build()
            );
            
            given(projectRepository.findByIdWithTasks(projectId)).willReturn(Optional.of(testProject));
            given(projectService.findById(projectId)).willReturn(mockProjectDto);
            given(taskService.findTaskHierarchyByProjectId(projectId)).willReturn(mockTaskDtos);
            given(dependencyRepository.findByProjectId(projectId)).willReturn(List.of());
            given(taskRepository.findCriticalPathTasks(projectId)).willReturn(List.of());

            // When
            GanttChartDto result = ganttService.getGanttChart(projectId);

            // Then
            List<TaskResponseDto> tasks = result.getTasks();
            assertThat(tasks).hasSize(3);

            // 첫 번째 태스크 (기획 단계) 검증
            TaskResponseDto task1 = tasks.get(0);
            assertThat(task1.getName()).isEqualTo("기획 단계");
            assertThat(task1.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
            assertThat(task1.getEndDate()).isEqualTo(LocalDate.of(2024, 1, 5));

            // 두 번째 태스크 (개발 단계) 검증
            TaskResponseDto task2 = tasks.get(1);
            assertThat(task2.getName()).isEqualTo("개발 단계");
            assertThat(task2.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 6));
            assertThat(task2.getEndDate()).isEqualTo(LocalDate.of(2024, 1, 20));

            // 세 번째 태스크 (테스트 단계) 검증
            TaskResponseDto task3 = tasks.get(2);
            assertThat(task3.getName()).isEqualTo("테스트 단계");
            assertThat(task3.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 21));
            assertThat(task3.getEndDate()).isEqualTo(LocalDate.of(2024, 1, 31));
        }
    }

    @Nested
    @DisplayName("프로젝트 진행률 계산 테스트")
    class CalculateProjectProgressTest {

        @Test
        @DisplayName("프로젝트 전체 진행률 계산 검증")
        void calculateProjectProgress_Success() {
            // Given
            Long projectId = 1L;
            testProject.setId(projectId);
            
            // 진행률 계산: (100 + 60 + 0) / 3 = 53.33%
            Double expectedProgress = (100.0 + 60.0 + 0.0) / 3.0;
            
            ProjectResponseDto mockProjectDto = ProjectResponseDto.builder()
                    .id(projectId)
                    .name("테스트 프로젝트")
                    .startDate(projectStartDate)
                    .endDate(projectEndDate)
                    .status(ProjectStatus.IN_PROGRESS)
                    .progress(expectedProgress)
                    .build();
            
            List<TaskResponseDto> mockTaskDtos = List.of(
                TaskResponseDto.builder()
                    .id(1L)
                    .name("기획 단계")
                    .progress(BigDecimal.valueOf(100.0))
                    .status(TaskStatus.COMPLETED)
                    .build(),
                TaskResponseDto.builder()
                    .id(2L)
                    .name("개발 단계")
                    .progress(BigDecimal.valueOf(60.0))
                    .status(TaskStatus.IN_PROGRESS)
                    .build(),
                TaskResponseDto.builder()
                    .id(3L)
                    .name("테스트 단계")
                    .progress(BigDecimal.valueOf(0.0))
                    .status(TaskStatus.NOT_STARTED)
                    .build()
            );
            
            given(projectRepository.findByIdWithTasks(projectId)).willReturn(Optional.of(testProject));
            given(projectService.findById(projectId)).willReturn(mockProjectDto);
            given(taskService.findTaskHierarchyByProjectId(projectId)).willReturn(mockTaskDtos);
            given(dependencyRepository.findByProjectId(projectId)).willReturn(List.of());
            given(taskRepository.findCriticalPathTasks(projectId)).willReturn(List.of());

            // When
            GanttChartDto result = ganttService.getGanttChart(projectId);

            // Then
            ProjectResponseDto projectInfo = result.getProject();
            Double actualProgress = projectInfo.getProgress();
            
            assertThat(actualProgress).isCloseTo(expectedProgress, within(0.01));
        }

        @Test
        @DisplayName("완료된 태스크 개수 계산 검증")
        void calculateCompletedTasks_Success() {
            // Given
            Long projectId = 1L;
            testProject.setId(projectId);
            
            ProjectResponseDto mockProjectDto = ProjectResponseDto.builder()
                    .id(projectId)
                    .name("테스트 프로젝트")
                    .startDate(projectStartDate)
                    .endDate(projectEndDate)
                    .status(ProjectStatus.IN_PROGRESS)
                    .build();
            
            List<TaskResponseDto> mockTaskDtos = List.of(
                TaskResponseDto.builder()
                    .id(1L)
                    .name("기획 단계")
                    .status(TaskStatus.COMPLETED)
                    .build(),
                TaskResponseDto.builder()
                    .id(2L)
                    .name("개발 단계")
                    .status(TaskStatus.IN_PROGRESS)
                    .build(),
                TaskResponseDto.builder()
                    .id(3L)
                    .name("테스트 단계")
                    .status(TaskStatus.NOT_STARTED)
                    .build()
            );
            
            given(projectRepository.findByIdWithTasks(projectId)).willReturn(Optional.of(testProject));
            given(projectService.findById(projectId)).willReturn(mockProjectDto);
            given(taskService.findTaskHierarchyByProjectId(projectId)).willReturn(mockTaskDtos);
            given(dependencyRepository.findByProjectId(projectId)).willReturn(List.of());
            given(taskRepository.findCriticalPathTasks(projectId)).willReturn(List.of());

            // When
            GanttChartDto result = ganttService.getGanttChart(projectId);

            // Then
            assertThat(result.getTasks()).hasSize(3);
            long completedTasks = result.getTasks().stream()
                    .mapToLong(task -> TaskStatus.COMPLETED.equals(task.getStatus()) ? 1 : 0)
                    .sum();
            assertThat(completedTasks).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("타임라인 계산 테스트")
    class CalculateTimelineTest {

        @Test
        @DisplayName("프로젝트 타임라인 정보 계산 검증")
        void calculateTimeline_Success() {
            // Given
            Long projectId = 1L;
            testProject.setId(projectId);
            
            ProjectResponseDto mockProjectDto = ProjectResponseDto.builder()
                    .id(projectId)
                    .name("테스트 프로젝트")
                    .startDate(projectStartDate)
                    .endDate(projectEndDate)
                    .status(ProjectStatus.IN_PROGRESS)
                    .build();
            
            given(projectRepository.findByIdWithTasks(projectId)).willReturn(Optional.of(testProject));
            given(projectService.findById(projectId)).willReturn(mockProjectDto);
            given(taskService.findTaskHierarchyByProjectId(projectId)).willReturn(List.of());
            given(dependencyRepository.findByProjectId(projectId)).willReturn(List.of());
            given(taskRepository.findCriticalPathTasks(projectId)).willReturn(List.of());

            // When
            GanttChartDto result = ganttService.getGanttChart(projectId);

            // Then
            GanttChartDto.TimelineInfo timeline = result.getTimeline();
            assertThat(timeline).isNotNull();
            assertThat(timeline.getStartDate()).isEqualTo(projectStartDate);
            assertThat(timeline.getEndDate()).isEqualTo(projectEndDate);
            
            // 2024년 1월 1일부터 31일까지 = 31일
            long expectedTotalDays = ChronoUnit.DAYS.between(projectStartDate, projectEndDate) + 1;
            assertThat(timeline.getTotalDays()).isEqualTo(expectedTotalDays);
        }

        @Test
        @DisplayName("주말 계산 로직 검증")
        void calculateWorkingDays_Success() {
            // Given
            Long projectId = 1L;
            testProject.setId(projectId);
            
            ProjectResponseDto mockProjectDto = ProjectResponseDto.builder()
                    .id(projectId)
                    .name("테스트 프로젝트")
                    .startDate(projectStartDate)
                    .endDate(projectEndDate)
                    .status(ProjectStatus.IN_PROGRESS)
                    .build();
            
            given(projectRepository.findByIdWithTasks(projectId)).willReturn(Optional.of(testProject));
            given(projectService.findById(projectId)).willReturn(mockProjectDto);
            given(taskService.findTaskHierarchyByProjectId(projectId)).willReturn(List.of());
            given(dependencyRepository.findByProjectId(projectId)).willReturn(List.of());
            given(taskRepository.findCriticalPathTasks(projectId)).willReturn(List.of());

            // When
            GanttChartDto result = ganttService.getGanttChart(projectId);

            // Then
            GanttChartDto.TimelineInfo timeline = result.getTimeline();
            assertThat(timeline).isNotNull();
            
            Long totalDays = timeline.getTotalDays();
            Long workingDays = timeline.getWorkingDays();
            
            // 전체 일수는 작업일보다 크거나 같아야 함
            assertThat(totalDays).isGreaterThanOrEqualTo(workingDays);
            
            // 주말은 전체 기간의 약 20-35% 정도 (2/7 ≈ 28.6%)
            if (totalDays > 0 && workingDays != null) {
                double workingRatio = (double) workingDays / totalDays;
                assertThat(workingRatio).isBetween(0.65, 1.0); // 작업일 비율
            }
        }
    }
}