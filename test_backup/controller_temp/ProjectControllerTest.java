package com.liam.gantt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liam.gantt.dto.request.ProjectRequestDto;
import com.liam.gantt.dto.response.ProjectResponseDto;
import com.liam.gantt.entity.enums.ProjectStatus;
import com.liam.gantt.exception.DuplicateProjectNameException;
import com.liam.gantt.exception.ProjectNotFoundException;
import com.liam.gantt.service.ProjectService;
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
 * ProjectController 단위 테스트
 * 
 * @author Liam
 * @since 1.0.0
 */
@WebMvcTest(ProjectController.class)
@DisplayName("ProjectController 단위 테스트")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    private ProjectRequestDto testRequestDto;
    private ProjectResponseDto testResponseDto;

    @BeforeEach
    void setUp() {
        testRequestDto = ProjectRequestDto.builder()
                .name("테스트 프로젝트")
                .description("테스트용 프로젝트입니다")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 3, 31))
                .build();

        testResponseDto = ProjectResponseDto.builder()
                .id(1L)
                .name("테스트 프로젝트")
                .description("테스트용 프로젝트입니다")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 3, 31))
                .status(ProjectStatus.PLANNING)
                .taskCount(0)
                .progress(0.0)
                .build();
    }

    @Nested
    @DisplayName("프로젝트 조회 API 테스트")
    class GetProjectsTest {

        @Test
        @DisplayName("전체 프로젝트 목록 조회 성공")
        void getAllProjects_Success() throws Exception {
            // Given
            List<ProjectResponseDto> projects = Arrays.asList(testResponseDto);
            given(projectService.findAll()).willReturn(projects);

            // When & Then
            mockMvc.perform(get("/api/v1/projects")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("테스트 프로젝트"))
                    .andExpect(jsonPath("$[0].status").value("PLANNING"));
        }

        @Test
        @DisplayName("페이징으로 프로젝트 목록 조회 성공")
        void getProjectsWithPaging_Success() throws Exception {
            // Given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
            Page<ProjectResponseDto> projectPage = new PageImpl<>(Arrays.asList(testResponseDto), pageable, 1);
            given(projectService.findAllWithPaging(any(Pageable.class))).willReturn(projectPage);

            // When & Then
            mockMvc.perform(get("/api/v1/projects/paged")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "name")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].name").value("테스트 프로젝트"))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1));
        }

        @Test
        @DisplayName("ID로 프로젝트 상세 조회 성공")
        void getProjectById_Success() throws Exception {
            // Given
            Long projectId = 1L;
            given(projectService.findById(projectId)).willReturn(testResponseDto);

            // When & Then
            mockMvc.perform(get("/api/v1/projects/{id}", projectId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("테스트 프로젝트"))
                    .andExpect(jsonPath("$.description").value("테스트용 프로젝트입니다"));
        }

        @Test
        @DisplayName("존재하지 않는 프로젝트 조회 시 404 에러")
        void getProjectById_NotFound() throws Exception {
            // Given
            Long projectId = 999L;
            given(projectService.findById(projectId))
                    .willThrow(new ProjectNotFoundException("프로젝트를 찾을 수 없습니다: " + projectId));

            // When & Then
            mockMvc.perform(get("/api/v1/projects/{id}", projectId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("프로젝트 생성 API 테스트")
    class CreateProjectTest {

        @Test
        @DisplayName("프로젝트 생성 성공")
        void createProject_Success() throws Exception {
            // Given
            given(projectService.create(any(ProjectRequestDto.class))).willReturn(testResponseDto);

            // When & Then
            mockMvc.perform(post("/api/v1/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequestDto)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("테스트 프로젝트"))
                    .andExpect(jsonPath("$.status").value("PLANNING"));
        }

        @Test
        @DisplayName("중복된 프로젝트명으로 생성 시 409 에러")
        void createProject_DuplicateName() throws Exception {
            // Given
            given(projectService.create(any(ProjectRequestDto.class)))
                    .willThrow(new DuplicateProjectNameException("이미 존재하는 프로젝트명입니다: " + testRequestDto.getName()));

            // When & Then
            mockMvc.perform(post("/api/v1/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequestDto)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("잘못된 요청 데이터로 생성 시 400 에러")
        void createProject_InvalidRequest() throws Exception {
            // Given
            ProjectRequestDto invalidRequest = ProjectRequestDto.builder()
                    .name("") // 빈 이름
                    .startDate(LocalDate.of(2024, 3, 31))
                    .endDate(LocalDate.of(2024, 1, 1)) // 시작일보다 빠른 종료일
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("프로젝트 수정 API 테스트")
    class UpdateProjectTest {

        @Test
        @DisplayName("프로젝트 수정 성공")
        void updateProject_Success() throws Exception {
            // Given
            Long projectId = 1L;
            ProjectResponseDto updatedResponse = testResponseDto.toBuilder()
                    .name("수정된 프로젝트")
                    .description("수정된 설명")
                    .build();

            given(projectService.update(eq(projectId), any(ProjectRequestDto.class))).willReturn(updatedResponse);

            // When & Then
            mockMvc.perform(put("/api/v1/projects/{id}", projectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequestDto)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("수정된 프로젝트"));
        }

        @Test
        @DisplayName("존재하지 않는 프로젝트 수정 시 404 에러")
        void updateProject_NotFound() throws Exception {
            // Given
            Long projectId = 999L;
            given(projectService.update(eq(projectId), any(ProjectRequestDto.class)))
                    .willThrow(new ProjectNotFoundException("프로젝트를 찾을 수 없습니다: " + projectId));

            // When & Then
            mockMvc.perform(put("/api/v1/projects/{id}", projectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequestDto)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("프로젝트 삭제 API 테스트")
    class DeleteProjectTest {

        @Test
        @DisplayName("프로젝트 삭제 성공")
        void deleteProject_Success() throws Exception {
            // Given
            Long projectId = 1L;
            willDoNothing().given(projectService).delete(projectId);

            // When & Then
            mockMvc.perform(delete("/api/v1/projects/{id}", projectId))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("존재하지 않는 프로젝트 삭제 시 404 에러")
        void deleteProject_NotFound() throws Exception {
            // Given
            Long projectId = 999L;
            willThrow(new ProjectNotFoundException("프로젝트를 찾을 수 없습니다: " + projectId))
                    .given(projectService).delete(projectId);

            // When & Then
            mockMvc.perform(delete("/api/v1/projects/{id}", projectId))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("프로젝트 검색 API 테스트")
    class SearchProjectsTest {

        @Test
        @DisplayName("이름으로 프로젝트 검색 성공")
        void searchProjects_ByName_Success() throws Exception {
            // Given
            String searchName = "테스트";
            List<ProjectResponseDto> searchResults = Arrays.asList(testResponseDto);
            given(projectService.search(searchName, null)).willReturn(searchResults);

            // When & Then
            mockMvc.perform(get("/api/v1/projects/search")
                            .param("name", searchName)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].name").value("테스트 프로젝트"));
        }

        @Test
        @DisplayName("상태로 프로젝트 검색 성공")
        void searchProjects_ByStatus_Success() throws Exception {
            // Given
            String status = "PLANNING";
            List<ProjectResponseDto> searchResults = Arrays.asList(testResponseDto);
            given(projectService.search(null, status)).willReturn(searchResults);

            // When & Then
            mockMvc.perform(get("/api/v1/projects/search")
                            .param("status", status)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].status").value("PLANNING"));
        }

        @Test
        @DisplayName("페이징으로 프로젝트 검색 성공")
        void searchProjectsWithPaging_Success() throws Exception {
            // Given
            String searchName = "테스트";
            Pageable pageable = PageRequest.of(0, 10);
            Page<ProjectResponseDto> searchResults = new PageImpl<>(Arrays.asList(testResponseDto), pageable, 1);
            given(projectService.searchWithPaging(eq(searchName), eq(null), any(Pageable.class))).willReturn(searchResults);

            // When & Then
            mockMvc.perform(get("/api/v1/projects/search/paged")
                            .param("name", searchName)
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].name").value("테스트 프로젝트"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
    }
}