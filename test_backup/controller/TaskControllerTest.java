package com.liam.gantt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liam.gantt.dto.request.TaskRequestDto;
import com.liam.gantt.dto.response.TaskResponseDto;
import com.liam.gantt.entity.enums.TaskStatus;
import com.liam.gantt.exception.ProjectNotFoundException;
import com.liam.gantt.exception.TaskNotFoundException;
import com.liam.gantt.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TaskController 단위 테스트
 * 
 * @author Liam
 * @since 1.0.0
 */
@WebMvcTest(TaskController.class)
@DisplayName("TaskController 단위 테스트")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    private TaskRequestDto testRequestDto;
    private TaskResponseDto testResponseDto;
    private Long projectId = 1L;

    @BeforeEach
    void setUp() {
        testRequestDto = TaskRequestDto.builder()
                .name("테스트 태스크")
                .description("테스트용 태스크입니다")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 5))
                .duration(5)
                .progress(BigDecimal.valueOf(30.0))
                .build();

        testResponseDto = TaskResponseDto.builder()
                .id(1L)
                .projectId(projectId)
                .name("테스트 태스크")
                .description("테스트용 태스크입니다")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 5))
                .duration(5)
                .progress(BigDecimal.valueOf(30.0))
                .status(TaskStatus.IN_PROGRESS)
                .build();
    }

    @Nested
    @DisplayName("태스크 조회 API 테스트")
    class GetTasksTest {

        @Test
        @DisplayName("프로젝트별 태스크 목록 조회 성공")
        void getTasksByProject_Success() throws Exception {
            // Given
            List<TaskResponseDto> tasks = Arrays.asList(testResponseDto);
            given(taskService.findByProjectId(projectId)).willReturn(tasks);

            // When & Then
            mockMvc.perform(get("/api/v1/projects/{projectId}/tasks", projectId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("테스트 태스크"))
                    .andExpect(jsonPath("$[0].projectId").value(projectId));
        }

        @Test
        @DisplayName("프로젝트별 태스크 페이징 조회 성공")
        void getTasksByProjectWithPaging_Success() throws Exception {
            // Given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("startDate"));
            Page<TaskResponseDto> taskPage = new PageImpl<>(Arrays.asList(testResponseDto), pageable, 1);
            given(taskService.findByProjectIdWithPaging(eq(projectId), any(Pageable.class))).willReturn(taskPage);

            // When & Then
            mockMvc.perform(get("/api/v1/projects/{projectId}/tasks/paged", projectId)
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "startDate")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].name").value("테스트 태스크"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("ID로 태스크 상세 조회 성공")
        void getTaskById_Success() throws Exception {
            // Given
            Long taskId = 1L;
            given(taskService.findById(taskId)).willReturn(testResponseDto);

            // When & Then
            mockMvc.perform(get("/api/v1/tasks/{id}", taskId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("테스트 태스크"))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        }

        @Test
        @DisplayName("존재하지 않는 태스크 조회 시 404 에러")
        void getTaskById_NotFound() throws Exception {
            // Given
            Long taskId = 999L;
            given(taskService.findById(taskId))
                    .willThrow(new TaskNotFoundException("태스크를 찾을 수 없습니다: " + taskId));

            // When & Then
            mockMvc.perform(get("/api/v1/tasks/{id}", taskId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("태스크 생성 API 테스트")
    class CreateTaskTest {

        @Test
        @DisplayName("태스크 생성 성공")
        void createTask_Success() throws Exception {
            // Given
            given(taskService.create(eq(projectId), any(TaskRequestDto.class))).willReturn(testResponseDto);

            // When & Then
            mockMvc.perform(post("/api/v1/projects/{projectId}/tasks", projectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequestDto)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("테스트 태스크"))
                    .andExpect(jsonPath("$.projectId").value(projectId));
        }

        @Test
        @DisplayName("존재하지 않는 프로젝트에 태스크 생성 시 404 에러")
        void createTask_ProjectNotFound() throws Exception {
            // Given
            Long invalidProjectId = 999L;
            given(taskService.create(eq(invalidProjectId), any(TaskRequestDto.class)))
                    .willThrow(new ProjectNotFoundException("프로젝트를 찾을 수 없습니다: " + invalidProjectId));

            // When & Then
            mockMvc.perform(post("/api/v1/projects/{projectId}/tasks", invalidProjectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequestDto)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("잘못된 요청 데이터로 생성 시 400 에러")
        void createTask_InvalidRequest() throws Exception {
            // Given
            TaskRequestDto invalidRequest = TaskRequestDto.builder()
                    .name("") // 빈 이름
                    .startDate(LocalDate.of(2024, 1, 5))
                    .endDate(LocalDate.of(2024, 1, 1)) // 시작일보다 빠른 종료일
                    .duration(-1) // 음수 기간
                    .progress(BigDecimal.valueOf(150.0)) // 100을 초과하는 진행률
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/projects/{projectId}/tasks", projectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("태스크 수정 API 테스트")
    class UpdateTaskTest {

        @Test
        @DisplayName("태스크 수정 성공")
        void updateTask_Success() throws Exception {
            // Given
            Long taskId = 1L;
            TaskResponseDto updatedResponse = testResponseDto.toBuilder()
                    .name("수정된 태스크")
                    .description("수정된 설명")
                    .progress(BigDecimal.valueOf(75.0))
                    .build();

            given(taskService.update(eq(taskId), any(TaskRequestDto.class))).willReturn(updatedResponse);

            // When & Then
            mockMvc.perform(put("/api/v1/tasks/{id}", taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequestDto)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("수정된 태스크"))
                    .andExpect(jsonPath("$.progress").value(75.0));
        }

        @Test
        @DisplayName("존재하지 않는 태스크 수정 시 404 에러")
        void updateTask_NotFound() throws Exception {
            // Given
            Long taskId = 999L;
            given(taskService.update(eq(taskId), any(TaskRequestDto.class)))
                    .willThrow(new TaskNotFoundException("태스크를 찾을 수 없습니다: " + taskId));

            // When & Then
            mockMvc.perform(put("/api/v1/tasks/{id}", taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequestDto)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("태스크 삭제 API 테스트")
    class DeleteTaskTest {

        @Test
        @DisplayName("태스크 삭제 성공")
        void deleteTask_Success() throws Exception {
            // Given
            Long taskId = 1L;
            willDoNothing().given(taskService).delete(taskId);

            // When & Then
            mockMvc.perform(delete("/api/v1/tasks/{id}", taskId))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("존재하지 않는 태스크 삭제 시 404 에러")
        void deleteTask_NotFound() throws Exception {
            // Given
            Long taskId = 999L;
            willThrow(new TaskNotFoundException("태스크를 찾을 수 없습니다: " + taskId))
                    .given(taskService).delete(taskId);

            // When & Then
            mockMvc.perform(delete("/api/v1/tasks/{id}", taskId))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("태스크 상태 및 진행률 API 테스트")
    class TaskProgressTest {

        @Test
        @DisplayName("태스크 상태 변경 성공")
        void updateTaskStatus_Success() throws Exception {
            // Given
            Long taskId = 1L;
            TaskStatus newStatus = TaskStatus.COMPLETED;
            TaskResponseDto updatedResponse = testResponseDto.toBuilder()
                    .status(newStatus)
                    .progress(BigDecimal.valueOf(100.0))
                    .build();

            given(taskService.updateStatus(taskId, newStatus)).willReturn(updatedResponse);

            // When & Then
            mockMvc.perform(patch("/api/v1/tasks/{id}/status", taskId)
                            .param("status", newStatus.name())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.progress").value(100.0));
        }

        @Test
        @DisplayName("태스크 진행률 업데이트 성공")
        void updateTaskProgress_Success() throws Exception {
            // Given
            Long taskId = 1L;
            BigDecimal newProgress = BigDecimal.valueOf(80.0);
            TaskStatus newStatus = TaskStatus.IN_PROGRESS;
            TaskResponseDto updatedResponse = testResponseDto.toBuilder()
                    .progress(newProgress)
                    .status(newStatus)
                    .build();

            given(taskService.updateProgress(taskId, newProgress, newStatus)).willReturn(updatedResponse);

            // When & Then
            mockMvc.perform(patch("/api/v1/tasks/{id}/progress", taskId)
                            .param("progress", "80.0")
                            .param("status", newStatus.name())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.progress").value(80.0))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        }
    }

    @Nested
    @DisplayName("태스크 검색 API 테스트")
    class SearchTasksTest {

        @Test
        @DisplayName("상태별 태스크 조회 성공")
        void getTasksByStatus_Success() throws Exception {
            // Given
            TaskStatus status = TaskStatus.IN_PROGRESS;
            List<TaskResponseDto> tasks = Arrays.asList(testResponseDto);
            given(taskService.findByProjectIdAndStatus(projectId, status)).willReturn(tasks);

            // When & Then
            mockMvc.perform(get("/api/v1/projects/{projectId}/tasks/status/{status}", projectId, status.name())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));
        }

        @Test
        @DisplayName("이름으로 태스크 검색 성공")
        void searchTasksByName_Success() throws Exception {
            // Given
            String keyword = "테스트";
            List<TaskResponseDto> tasks = Arrays.asList(testResponseDto);
            given(taskService.searchByName(projectId, keyword)).willReturn(tasks);

            // When & Then
            mockMvc.perform(get("/api/v1/projects/{projectId}/tasks/search", projectId)
                            .param("keyword", keyword)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].name").value("테스트 태스크"));
        }
    }
}