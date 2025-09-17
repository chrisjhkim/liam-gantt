package com.liam.gantt.controller;

import com.liam.gantt.dto.response.ProjectResponseDto;
import com.liam.gantt.dto.response.TaskDependencyResponseDto;
import com.liam.gantt.dto.response.TaskResponseDto;
import com.liam.gantt.entity.enums.DependencyType;
import com.liam.gantt.entity.enums.ProjectStatus;
import com.liam.gantt.entity.enums.TaskStatus;
import com.liam.gantt.service.ProjectService;
import com.liam.gantt.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(TaskWebController.class)
@DisplayName("TaskWebController 테스트")
class TaskWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private ProjectService projectService;

    private ProjectResponseDto testProject;
    private TaskResponseDto testTask;
    private TaskResponseDto subtask1;
    private TaskResponseDto subtask2;
    private List<TaskDependencyResponseDto> dependencies;

    @BeforeEach
    void setUp() {
        // 테스트용 프로젝트 생성
        testProject = ProjectResponseDto.builder()
                .id(1L)
                .name("테스트 프로젝트")
                .description("테스트 프로젝트 설명")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .status(ProjectStatus.IN_PROGRESS)
                .build();

        // 테스트용 의존성 생성
        dependencies = Arrays.asList(
                TaskDependencyResponseDto.builder()
                        .id(1L)
                        .predecessorId(2L)
                        .successorId(1L)
                        .dependencyType(DependencyType.FINISH_TO_START)
                        .lagDays(0)
                        .build()
        );

        // 테스트용 메인 태스크 생성
        testTask = TaskResponseDto.builder()
                .id(1L)
                .projectId(1L)
                .name("메인 태스크")
                .description("메인 태스크 설명")
                .startDate(LocalDate.of(2025, 1, 15))
                .endDate(LocalDate.of(2025, 2, 15))
                .duration(31)
                .progress(BigDecimal.valueOf(50))
                .status(TaskStatus.IN_PROGRESS)
                .dependencies(dependencies)
                .build();

        // 테스트용 하위 태스크들 생성
        subtask1 = TaskResponseDto.builder()
                .id(2L)
                .projectId(1L)
                .parentTaskId(1L)
                .name("하위 태스크 1")
                .description("하위 태스크 1 설명")
                .startDate(LocalDate.of(2025, 1, 20))
                .endDate(LocalDate.of(2025, 1, 31))
                .duration(11)
                .progress(BigDecimal.valueOf(75))
                .status(TaskStatus.IN_PROGRESS)
                .build();

        subtask2 = TaskResponseDto.builder()
                .id(3L)
                .projectId(1L)
                .parentTaskId(1L)
                .name("하위 태스크 2")
                .description("하위 태스크 2 설명")
                .startDate(LocalDate.of(2025, 2, 1))
                .endDate(LocalDate.of(2025, 2, 10))
                .duration(9)
                .progress(BigDecimal.valueOf(25))
                .status(TaskStatus.IN_PROGRESS)
                .build();
    }

    @Test
    @DisplayName("태스크 상세 페이지 - 하위 태스크와 의존성 포함")
    void taskDetail_WithSubtasksAndDependencies_Success() throws Exception {
        // Given
        Long taskId = 1L;
        List<TaskResponseDto> subtasks = Arrays.asList(subtask1, subtask2);

        given(taskService.findByIdWithDependencies(taskId)).willReturn(testTask);
        given(projectService.findById(testTask.getProjectId())).willReturn(testProject);
        given(taskService.findByParentTaskId(taskId)).willReturn(subtasks);

        // When & Then
        mockMvc.perform(get("/web/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/detail"))
                .andExpect(model().attribute("task", testTask))
                .andExpect(model().attribute("project", testProject))
                .andExpect(model().attribute("subtasks", subtasks))
                .andExpect(model().attribute("dependencies", dependencies))
                .andExpect(model().attribute("pageTitle", testTask.getName()))
                .andExpect(model().attribute("pageIcon", "fas fa-tasks"));

        // Verify
        verify(taskService).findByIdWithDependencies(taskId);
        verify(projectService).findById(testTask.getProjectId());
        verify(taskService).findByParentTaskId(taskId);
    }

    @Test
    @DisplayName("태스크 상세 페이지 - 하위 태스크 없음")
    void taskDetail_WithoutSubtasks_Success() throws Exception {
        // Given
        Long taskId = 1L;
        List<TaskResponseDto> emptySubtasks = Collections.emptyList();

        given(taskService.findByIdWithDependencies(taskId)).willReturn(testTask);
        given(projectService.findById(testTask.getProjectId())).willReturn(testProject);
        given(taskService.findByParentTaskId(taskId)).willReturn(emptySubtasks);

        // When & Then
        mockMvc.perform(get("/web/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/detail"))
                .andExpect(model().attribute("task", testTask))
                .andExpect(model().attribute("project", testProject))
                .andExpect(model().attribute("subtasks", emptySubtasks))
                .andExpect(model().attribute("dependencies", dependencies));

        // Verify
        verify(taskService).findByIdWithDependencies(taskId);
        verify(projectService).findById(testTask.getProjectId());
        verify(taskService).findByParentTaskId(taskId);
    }

    @Test
    @DisplayName("태스크 상세 페이지 - 의존성 없음")
    void taskDetail_WithoutDependencies_Success() throws Exception {
        // Given
        Long taskId = 1L;
        TaskResponseDto taskWithoutDeps = TaskResponseDto.builder()
                .id(1L)
                .projectId(1L)
                .name("의존성 없는 태스크")
                .description("의존성이 없는 태스크")
                .startDate(LocalDate.of(2025, 1, 15))
                .endDate(LocalDate.of(2025, 2, 15))
                .duration(31)
                .progress(BigDecimal.valueOf(50))
                .status(TaskStatus.IN_PROGRESS)
                .dependencies(Collections.emptyList())
                .build();

        given(taskService.findByIdWithDependencies(taskId)).willReturn(taskWithoutDeps);
        given(projectService.findById(taskWithoutDeps.getProjectId())).willReturn(testProject);
        given(taskService.findByParentTaskId(taskId)).willReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/web/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/detail"))
                .andExpect(model().attribute("task", taskWithoutDeps))
                .andExpect(model().attribute("dependencies", Collections.emptyList()));
    }

    @Test
    @DisplayName("태스크 상세 페이지 - 태스크 없음 에러")
    void taskDetail_TaskNotFound_Error() throws Exception {
        // Given
        Long taskId = 999L;
        given(taskService.findByIdWithDependencies(taskId))
                .willThrow(new RuntimeException("태스크를 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/web/tasks/{id}", taskId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/projects"))
                .andExpect(flash().attribute("errorMessage", "태스크를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("새 태스크 생성 폼 - availableParentTasks 속성 확인")
    void newTaskForm_WithAvailableParentTasks_Success() throws Exception {
        // Given
        Long projectId = 1L;
        List<TaskResponseDto> availableTasks = Arrays.asList(testTask);

        given(projectService.findById(projectId)).willReturn(testProject);
        given(taskService.findByProjectId(projectId)).willReturn(availableTasks);

        // When & Then
        mockMvc.perform(get("/web/projects/{projectId}/tasks/new", projectId))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/form"))
                .andExpect(model().attributeExists("task"))
                .andExpect(model().attribute("project", testProject))
                .andExpect(model().attribute("availableParentTasks", availableTasks))
                .andExpect(model().attribute("pageTitle", "새 태스크 - " + testProject.getName()))
                .andExpect(model().attribute("pageIcon", "fas fa-plus"));

        // Verify
        verify(projectService).findById(projectId);
        verify(taskService).findByProjectId(projectId);
    }

    @Test
    @DisplayName("태스크 수정 폼 - availableParentTasks 속성 확인")
    void editTaskForm_WithAvailableParentTasks_Success() throws Exception {
        // Given
        Long taskId = 1L;
        List<TaskResponseDto> availableTasks = Arrays.asList(subtask1, subtask2);

        given(taskService.findByIdWithDependencies(taskId)).willReturn(testTask);
        given(projectService.findById(testTask.getProjectId())).willReturn(testProject);
        given(taskService.findByProjectId(testTask.getProjectId())).willReturn(
                Arrays.asList(testTask, subtask1, subtask2)
        );

        // When & Then
        mockMvc.perform(get("/web/tasks/{id}/edit", taskId))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/form"))
                .andExpect(model().attributeExists("task"))
                .andExpect(model().attribute("taskId", taskId))
                .andExpect(model().attribute("project", testProject))
                .andExpect(model().attribute("availableParentTasks", hasSize(2)))
                .andExpect(model().attribute("pageTitle", "태스크 수정: " + testTask.getName()))
                .andExpect(model().attribute("pageIcon", "fas fa-edit"));

        // Verify
        verify(taskService).findByIdWithDependencies(taskId);
        verify(projectService).findById(testTask.getProjectId());
        verify(taskService).findByProjectId(testTask.getProjectId());
    }
}