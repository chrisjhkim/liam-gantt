package com.liam.gantt.service;

import com.liam.gantt.entity.Project;
import com.liam.gantt.entity.Task;
import com.liam.gantt.entity.enums.ProjectStatus;
import com.liam.gantt.entity.enums.TaskStatus;
import com.liam.gantt.exception.ProjectNotFoundException;
import com.liam.gantt.repository.ProjectRepository;
import com.liam.gantt.repository.TaskRepository;
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
                .id(1L)
                .name("테스트 프로젝트")
                .description("간트차트 테스트용 프로젝트")
                .startDate(projectStartDate)
                .endDate(projectEndDate)
                .status(ProjectStatus.IN_PROGRESS)
                .build();

        Task task1 = Task.builder()
                .id(1L)
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
                .id(2L)
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
                .id(3L)
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
            given(projectRepository.findById(projectId)).willReturn(Optional.of(testProject));
            given(taskRepository.findByProjectIdOrderByStartDateAsc(projectId)).willReturn(testTasks);

            // When
            Map<String, Object> result = ganttService.getGanttChartData(projectId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).containsKeys("project", "tasks", "timeline");

            // 프로젝트 정보 검증
            @SuppressWarnings("unchecked")
            Map<String, Object> projectInfo = (Map<String, Object>) result.get("project");
            assertThat(projectInfo.get("id")).isEqualTo(1L);
            assertThat(projectInfo.get("name")).isEqualTo("테스트 프로젝트");
            assertThat(projectInfo.get("startDate")).isEqualTo(projectStartDate);
            assertThat(projectInfo.get("endDate")).isEqualTo(projectEndDate);

            // 태스크 정보 검증
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) result.get("tasks");
            assertThat(tasks).hasSize(3);
            assertThat(tasks.get(0).get("name")).isEqualTo("기획 단계");
            assertThat(tasks.get(1).get("name")).isEqualTo("개발 단계");
            assertThat(tasks.get(2).get("name")).isEqualTo("테스트 단계");

            // 타임라인 정보 검증
            @SuppressWarnings("unchecked")
            Map<String, Object> timeline = (Map<String, Object>) result.get("timeline");
            assertThat(timeline.get("startDate")).isEqualTo(projectStartDate);
            assertThat(timeline.get("endDate")).isEqualTo(projectEndDate);
            assertThat(timeline.get("totalDays")).isEqualTo(ChronoUnit.DAYS.between(projectStartDate, projectEndDate) + 1);

            verify(projectRepository).findById(projectId);
            verify(taskRepository).findByProjectIdOrderByStartDateAsc(projectId);
        }

        @Test
        @DisplayName("존재하지 않는 프로젝트 조회 시 예외 발생")
        void getGanttChartData_ProjectNotFound_ThrowsException() {
            // Given
            Long projectId = 999L;
            given(projectRepository.findById(projectId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> ganttService.getGanttChartData(projectId))
                    .isInstanceOf(ProjectNotFoundException.class)
                    .hasMessageContaining("프로젝트를 찾을 수 없습니다");

            verify(projectRepository).findById(projectId);
        }

        @Test
        @DisplayName("태스크가 없는 프로젝트 조회 성공")
        void getGanttChartData_NoTasks_Success() {
            // Given
            Long projectId = 1L;
            given(projectRepository.findById(projectId)).willReturn(Optional.of(testProject));
            given(taskRepository.findByProjectIdOrderByStartDateAsc(projectId)).willReturn(Arrays.asList());

            // When
            Map<String, Object> result = ganttService.getGanttChartData(projectId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).containsKeys("project", "tasks", "timeline");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) result.get("tasks");
            assertThat(tasks).isEmpty();

            verify(projectRepository).findById(projectId);
            verify(taskRepository).findByProjectIdOrderByStartDateAsc(projectId);
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
            given(projectRepository.findById(projectId)).willReturn(Optional.of(testProject));
            given(taskRepository.findByProjectIdOrderByStartDateAsc(projectId)).willReturn(testTasks);

            // When
            Map<String, Object> result = ganttService.getGanttChartData(projectId);

            // Then
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) result.get("tasks");

            // 첫 번째 태스크 (기획 단계) 검증
            Map<String, Object> task1 = tasks.get(0);
            assertThat(task1.get("startOffset")).isEqualTo(0); // 프로젝트 시작일과 동일
            assertThat(task1.get("width")).isEqualTo(5); // 5일 기간

            // 두 번째 태스크 (개발 단계) 검증
            Map<String, Object> task2 = tasks.get(1);
            assertThat(task2.get("startOffset")).isEqualTo(5); // 6일차 시작
            assertThat(task2.get("width")).isEqualTo(15); // 15일 기간

            // 세 번째 태스크 (테스트 단계) 검증
            Map<String, Object> task3 = tasks.get(2);
            assertThat(task3.get("startOffset")).isEqualTo(20); // 21일차 시작
            assertThat(task3.get("width")).isEqualTo(11); // 11일 기간
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
            given(projectRepository.findById(projectId)).willReturn(Optional.of(testProject));
            given(taskRepository.findByProjectIdOrderByStartDateAsc(projectId)).willReturn(testTasks);

            // When
            Map<String, Object> result = ganttService.getGanttChartData(projectId);

            // Then
            @SuppressWarnings("unchecked")
            Map<String, Object> projectInfo = (Map<String, Object>) result.get("project");
            
            // 진행률 계산: (100 + 60 + 0) / 3 = 53.33%
            Double expectedProgress = (100.0 + 60.0 + 0.0) / 3.0;
            Double actualProgress = (Double) projectInfo.get("progress");
            
            assertThat(actualProgress).isCloseTo(expectedProgress, within(0.01));
        }

        @Test
        @DisplayName("완료된 태스크 개수 계산 검증")
        void calculateCompletedTasks_Success() {
            // Given
            Long projectId = 1L;
            given(projectRepository.findById(projectId)).willReturn(Optional.of(testProject));
            given(taskRepository.findByProjectIdOrderByStartDateAsc(projectId)).willReturn(testTasks);

            // When
            Map<String, Object> result = ganttService.getGanttChartData(projectId);

            // Then
            @SuppressWarnings("unchecked")
            Map<String, Object> projectInfo = (Map<String, Object>) result.get("project");
            
            assertThat(projectInfo.get("totalTasks")).isEqualTo(3);
            assertThat(projectInfo.get("completedTasks")).isEqualTo(1); // COMPLETED 상태인 태스크 1개
            assertThat(projectInfo.get("inProgressTasks")).isEqualTo(1); // IN_PROGRESS 상태인 태스크 1개
            assertThat(projectInfo.get("notStartedTasks")).isEqualTo(1); // NOT_STARTED 상태인 태스크 1개
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
            given(projectRepository.findById(projectId)).willReturn(Optional.of(testProject));
            given(taskRepository.findByProjectIdOrderByStartDateAsc(projectId)).willReturn(testTasks);

            // When
            Map<String, Object> result = ganttService.getGanttChartData(projectId);

            // Then
            @SuppressWarnings("unchecked")
            Map<String, Object> timeline = (Map<String, Object>) result.get("timeline");

            assertThat(timeline.get("startDate")).isEqualTo(projectStartDate);
            assertThat(timeline.get("endDate")).isEqualTo(projectEndDate);
            
            // 2024년 1월 1일부터 31일까지 = 31일
            long expectedTotalDays = ChronoUnit.DAYS.between(projectStartDate, projectEndDate) + 1;
            assertThat(timeline.get("totalDays")).isEqualTo(expectedTotalDays);

            // 주말 제외한 작업일 계산 (대략적으로 22일 정도)
            Integer workingDays = (Integer) timeline.get("workingDays");
            assertThat(workingDays).isBetween(20, 25); // 주말 고려한 작업일
        }

        @Test
        @DisplayName("주말 계산 로직 검증")
        void calculateWorkingDays_Success() {
            // Given
            Long projectId = 1L;
            given(projectRepository.findById(projectId)).willReturn(Optional.of(testProject));
            given(taskRepository.findByProjectIdOrderByStartDateAsc(projectId)).willReturn(testTasks);

            // When
            Map<String, Object> result = ganttService.getGanttChartData(projectId);

            // Then
            @SuppressWarnings("unchecked")
            Map<String, Object> timeline = (Map<String, Object>) result.get("timeline");
            
            Integer totalDays = (Integer) timeline.get("totalDays");
            Integer workingDays = (Integer) timeline.get("workingDays");
            Integer weekendDays = (Integer) timeline.get("weekendDays");
            
            // 전체 일수 = 작업일 + 주말 일수
            assertThat(totalDays).isEqualTo(workingDays + weekendDays);
            
            // 주말은 전체 기간의 약 28% 정도 (2/7)
            double weekendRatio = (double) weekendDays / totalDays;
            assertThat(weekendRatio).isBetween(0.2, 0.35);
        }
    }
}