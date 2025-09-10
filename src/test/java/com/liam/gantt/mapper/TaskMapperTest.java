package com.liam.gantt.mapper;

import com.liam.gantt.dto.request.TaskRequestDto;
import com.liam.gantt.dto.response.TaskResponseDto;
import com.liam.gantt.entity.Project;
import com.liam.gantt.entity.Task;
import com.liam.gantt.entity.enums.ProjectStatus;
import com.liam.gantt.entity.enums.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TaskMapper 단위 테스트
 * 
 * @author Liam
 * @since 1.0.0
 */
@DisplayName("TaskMapper 단위 테스트")
class TaskMapperTest {

    private TaskMapper taskMapper;
    private TaskDependencyMapper dependencyMapper;
    private Project testProject;
    private Task testTask;
    private Task parentTask;
    private TaskRequestDto testRequestDto;

    @BeforeEach
    void setUp() {
        dependencyMapper = mock(TaskDependencyMapper.class);
        taskMapper = new TaskMapper();
        // TaskDependencyMapper를 주입하기 위해 리플렉션 사용
        try {
            java.lang.reflect.Field field = TaskMapper.class.getDeclaredField("dependencyMapper");
            field.setAccessible(true);
            field.set(taskMapper, dependencyMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        testProject = Project.builder()
                .name("테스트 프로젝트")
                .description("테스트용 프로젝트")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 3, 31))
                .status(ProjectStatus.IN_PROGRESS)
                .build();
        testProject.setId(1L);

        parentTask = Task.builder()
                .project(testProject)
                .name("상위 태스크")
                .description("상위 태스크 설명")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 2, 15))
                .duration(45)
                .progress(BigDecimal.valueOf(50.0))
                .status(TaskStatus.IN_PROGRESS)
                .build();
        parentTask.setId(2L);

        testTask = Task.builder()
                .project(testProject)
                .parentTask(parentTask)
                .name("테스트 태스크")
                .description("테스트용 태스크입니다")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 15))
                .duration(15)
                .progress(BigDecimal.valueOf(75.5))
                .status(TaskStatus.IN_PROGRESS)
                .build();
        testTask.setId(3L);

        testRequestDto = TaskRequestDto.builder()
                .name("요청 태스크")
                .description("요청용 태스크입니다")
                .startDate(LocalDate.of(2024, 2, 1))
                .endDate(LocalDate.of(2024, 2, 10))
                .duration(10)
                .progress(BigDecimal.valueOf(30.0))
                .build();
    }

    @Nested
    @DisplayName("Entity to ResponseDto 변환 테스트")
    class EntityToResponseDtoTest {

        @Test
        @DisplayName("Task Entity를 TaskResponseDto로 변환 성공")
        void toResponseDto_Success() {
            // When
            TaskResponseDto result = taskMapper.toResponseDto(testTask);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testTask.getId());
            assertThat(result.getProjectId()).isEqualTo(testTask.getProject().getId());
            assertThat(result.getParentTaskId()).isEqualTo(testTask.getParentTask().getId());
            assertThat(result.getName()).isEqualTo(testTask.getName());
            assertThat(result.getDescription()).isEqualTo(testTask.getDescription());
            assertThat(result.getStartDate()).isEqualTo(testTask.getStartDate());
            assertThat(result.getEndDate()).isEqualTo(testTask.getEndDate());
            assertThat(result.getDuration()).isEqualTo(testTask.getDuration());
            assertThat(result.getProgress()).isEqualByComparingTo(testTask.getProgress());
            assertThat(result.getStatus()).isEqualTo(testTask.getStatus());
        }

        @Test
        @DisplayName("상위 태스크가 없는 Task Entity 변환 성공")
        void toResponseDto_NoParentTask_Success() {
            // Given
            Task rootTask = Task.builder()
                    .project(testProject)
                    .parentTask(null) // 상위 태스크 없음
                    .name("루트 태스크")
                    .description("최상위 태스크")
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 1, 10))
                    .duration(10)
                    .progress(BigDecimal.valueOf(0.0))
                    .status(TaskStatus.NOT_STARTED)
                    .build();
            rootTask.setId(3L);

            // When
            TaskResponseDto result = taskMapper.toResponseDto(rootTask);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(3L);
            assertThat(result.getProjectId()).isEqualTo(testProject.getId());
            assertThat(result.getParentTaskId()).isNull();
            assertThat(result.getName()).isEqualTo("루트 태스크");
        }

        @Test
        @DisplayName("null Entity를 변환 시 null 반환")
        void toResponseDto_NullEntity_ReturnsNull() {
            // When
            TaskResponseDto result = taskMapper.toResponseDto(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("필수 필드만 있는 Entity 변환 성공")
        void toResponseDto_MinimalFields_Success() {
            // Given
            Task minimalTask = Task.builder()
                    .project(testProject)
                    .name("최소 태스크")
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 1, 5))
                    .duration(5)
                    .progress(BigDecimal.valueOf(0.0))
                    .status(TaskStatus.NOT_STARTED)
                    .build();
            minimalTask.setId(4L);
            // description, parentTask는 null

            // When
            TaskResponseDto result = taskMapper.toResponseDto(minimalTask);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(4L);
            assertThat(result.getName()).isEqualTo("최소 태스크");
            assertThat(result.getDescription()).isNull();
            assertThat(result.getParentTaskId()).isNull();
        }
    }

    @Nested
    @DisplayName("RequestDto to Entity 변환 테스트")
    class RequestDtoToEntityTest {

        @Test
        @DisplayName("TaskRequestDto를 Task Entity로 변환 성공")
        void toEntity_Success() {
            // When
            Task result = taskMapper.toEntity(testRequestDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNull(); // 새로 생성되는 Entity이므로 ID는 null
            assertThat(result.getName()).isEqualTo(testRequestDto.getName());
            assertThat(result.getDescription()).isEqualTo(testRequestDto.getDescription());
            assertThat(result.getStartDate()).isEqualTo(testRequestDto.getStartDate());
            assertThat(result.getEndDate()).isEqualTo(testRequestDto.getEndDate());
            assertThat(result.getDuration()).isEqualTo(testRequestDto.getDuration());
            assertThat(result.getProgress()).isEqualByComparingTo(testRequestDto.getProgress());
            assertThat(result.getStatus()).isEqualTo(TaskStatus.NOT_STARTED); // 기본값
        }

        @Test
        @DisplayName("null RequestDto를 변환 시 null 반환")
        void toEntity_NullDto_ReturnsNull() {
            // When
            Task result = taskMapper.toEntity(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("필수 필드만 있는 RequestDto 변환 성공")
        void toEntity_MinimalFields_Success() {
            // Given
            TaskRequestDto minimalRequest = TaskRequestDto.builder()
                    .name("최소 요청")
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 1, 5))
                    .duration(5)
                    .build();
            // description, progress는 null

            // When
            Task result = taskMapper.toEntity(minimalRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("최소 요청");
            assertThat(result.getDescription()).isNull();
            assertThat(result.getProgress()).isEqualTo(BigDecimal.ZERO);
            assertThat(result.getStatus()).isEqualTo(TaskStatus.NOT_STARTED);
        }
    }

    @Nested
    @DisplayName("Entity 업데이트 테스트")
    class UpdateEntityTest {

        @Test
        @DisplayName("TaskRequestDto로 기존 Entity 업데이트 성공")
        void updateEntity_Success() {
            // Given
            Task existingTask = Task.builder()
                        .project(testProject)
                    .name("기존 태스크")
                    .description("기존 설명")
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 1, 10))
                    .duration(10)
                    .progress(BigDecimal.valueOf(25.0))
                    .status(TaskStatus.IN_PROGRESS)
                    .build();
            existingTask.setId(5L);

            TaskRequestDto updateRequest = TaskRequestDto.builder()
                    .name("수정된 태스크")
                    .description("수정된 설명")
                    .startDate(LocalDate.of(2024, 1, 5))
                    .endDate(LocalDate.of(2024, 1, 20))
                    .duration(15)
                    .progress(BigDecimal.valueOf(60.0))
                    .build();

            // When
            taskMapper.updateEntity(existingTask, updateRequest);

            // Then
            assertThat(existingTask.getId()).isEqualTo(5L); // ID는 유지
            assertThat(existingTask.getProject()).isEqualTo(testProject); // Project는 유지
            assertThat(existingTask.getName()).isEqualTo("수정된 태스크");
            assertThat(existingTask.getDescription()).isEqualTo("수정된 설명");
            assertThat(existingTask.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 5));
            assertThat(existingTask.getEndDate()).isEqualTo(LocalDate.of(2024, 1, 20));
            assertThat(existingTask.getDuration()).isEqualTo(15);
            assertThat(existingTask.getProgress()).isEqualByComparingTo(BigDecimal.valueOf(60.0));
            assertThat(existingTask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS); // 상태는 유지
        }

        @Test
        @DisplayName("일부 필드만 업데이트")
        void updateEntity_PartialUpdate_Success() {
            // Given
            Task existingTask = Task.builder()
                        .project(testProject)
                    .name("기존 태스크")
                    .description("기존 설명")
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 1, 10))
                    .duration(10)
                    .progress(BigDecimal.valueOf(25.0))
                    .status(TaskStatus.IN_PROGRESS)
                    .build();
            existingTask.setId(6L);

            TaskRequestDto updateRequest = TaskRequestDto.builder()
                    .name("수정된 이름만")
                    .description("기존 설명") // 동일한 값
                    .startDate(LocalDate.of(2024, 1, 1)) // 동일한 값
                    .endDate(LocalDate.of(2024, 1, 10)) // 동일한 값
                    .duration(10) // 동일한 값
                    .progress(BigDecimal.valueOf(50.0)) // 변경된 값
                    .build();

            // When
            taskMapper.updateEntity(existingTask, updateRequest);

            // Then
            assertThat(existingTask.getName()).isEqualTo("수정된 이름만");
            assertThat(existingTask.getProgress()).isEqualByComparingTo(BigDecimal.valueOf(50.0));
            assertThat(existingTask.getDescription()).isEqualTo("기존 설명");
            assertThat(existingTask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("null 값으로 업데이트 처리")
        void updateEntity_WithNullValues() {
            // Given
            Task existingTask = Task.builder()
                        .project(testProject)
                    .name("기존 태스크")
                    .description("기존 설명")
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 1, 10))
                    .duration(10)
                    .progress(BigDecimal.valueOf(25.0))
                    .status(TaskStatus.IN_PROGRESS)
                    .build();
            existingTask.setId(7L);

            TaskRequestDto updateRequest = TaskRequestDto.builder()
                    .name("수정된 태스크")
                    .description(null) // null 값
                    .startDate(LocalDate.of(2024, 1, 5))
                    .endDate(LocalDate.of(2024, 1, 15))
                    .duration(10)
                    .progress(null) // null 값
                    .build();

            // When
            taskMapper.updateEntity(existingTask, updateRequest);

            // Then
            assertThat(existingTask.getName()).isEqualTo("수정된 태스크");
            assertThat(existingTask.getDescription()).isNull();
            assertThat(existingTask.getProgress()).isEqualByComparingTo(BigDecimal.valueOf(25.0)); // null이어도 기존값 유지
            assertThat(existingTask.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 5));
        }
    }

    @Nested
    @DisplayName("BigDecimal 처리 테스트")
    class BigDecimalHandlingTest {

        @Test
        @DisplayName("진행률 정확한 변환 테스트")
        void progressMapping_Precision_Success() {
            // Given
            TaskRequestDto precisionRequest = TaskRequestDto.builder()
                    .name("정밀도 테스트")
                    .description("진행률 정밀도 테스트")
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 1, 10))
                    .duration(10)
                    .progress(BigDecimal.valueOf(33.333333))
                    .build();

            // When
            Task entity = taskMapper.toEntity(precisionRequest);
            TaskResponseDto responseDto = taskMapper.toResponseDto(entity);

            // Then
            assertThat(entity.getProgress()).isEqualByComparingTo(BigDecimal.valueOf(33.333333));
            assertThat(responseDto.getProgress()).isEqualByComparingTo(BigDecimal.valueOf(33.333333));
        }

        @Test
        @DisplayName("0과 100 경계값 처리 테스트")
        void progressMapping_BoundaryValues_Success() {
            // Given - 0% 진행률
            TaskRequestDto zeroProgressRequest = TaskRequestDto.builder()
                    .name("0% 태스크")
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 1, 10))
                    .duration(10)
                    .progress(BigDecimal.ZERO)
                    .build();

            // Given - 100% 진행률
            TaskRequestDto fullProgressRequest = TaskRequestDto.builder()
                    .name("100% 태스크")
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 1, 10))
                    .duration(10)
                    .progress(BigDecimal.valueOf(100.0))
                    .build();

            // When
            Task zeroEntity = taskMapper.toEntity(zeroProgressRequest);
            Task fullEntity = taskMapper.toEntity(fullProgressRequest);

            // Then
            assertThat(zeroEntity.getProgress()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(fullEntity.getProgress()).isEqualByComparingTo(BigDecimal.valueOf(100.0));
        }
    }

    @Nested
    @DisplayName("매핑 일관성 테스트")
    class MappingConsistencyTest {

        @Test
        @DisplayName("RequestDto -> Entity -> ResponseDto 변환 일관성")
        void roundTripConsistency_RequestDtoToEntityToResponseDto() {
            // Given
            TaskRequestDto originalRequest = testRequestDto;

            // When
            Task entity = taskMapper.toEntity(originalRequest);
            entity.setId(1L); // ID 설정
            entity.setProject(testProject); // Project 설정
            TaskResponseDto responseDto = taskMapper.toResponseDto(entity);

            // Then
            assertThat(responseDto.getName()).isEqualTo(originalRequest.getName());
            assertThat(responseDto.getDescription()).isEqualTo(originalRequest.getDescription());
            assertThat(responseDto.getStartDate()).isEqualTo(originalRequest.getStartDate());
            assertThat(responseDto.getEndDate()).isEqualTo(originalRequest.getEndDate());
            assertThat(responseDto.getDuration()).isEqualTo(originalRequest.getDuration());
            assertThat(responseDto.getProgress()).isEqualByComparingTo(originalRequest.getProgress());
            assertThat(responseDto.getId()).isEqualTo(1L);
            assertThat(responseDto.getProjectId()).isEqualTo(testProject.getId());
            assertThat(responseDto.getStatus()).isEqualTo(TaskStatus.NOT_STARTED); // 기본값
        }

        @Test
        @DisplayName("Entity 업데이트 후 일관성 검증")
        void updateConsistency_Success() {
            // Given
            Task originalTask = testTask;
            TaskRequestDto updateRequest = testRequestDto;

            // When
            taskMapper.updateEntity(originalTask, updateRequest);
            TaskResponseDto responseDto = taskMapper.toResponseDto(originalTask);

            // Then
            assertThat(responseDto.getName()).isEqualTo(updateRequest.getName());
            assertThat(responseDto.getDescription()).isEqualTo(updateRequest.getDescription());
            assertThat(responseDto.getStartDate()).isEqualTo(updateRequest.getStartDate());
            assertThat(responseDto.getEndDate()).isEqualTo(updateRequest.getEndDate());
            assertThat(responseDto.getDuration()).isEqualTo(updateRequest.getDuration());
            assertThat(responseDto.getProgress()).isEqualByComparingTo(updateRequest.getProgress());
            // ID, ProjectId, Status는 업데이트에서 유지됨
            assertThat(responseDto.getId()).isEqualTo(3L);
            assertThat(responseDto.getProjectId()).isEqualTo(testProject.getId());
            assertThat(responseDto.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS); // 기존 상태 유지
        }
    }

    @Nested
    @DisplayName("특수 케이스 테스트")
    class SpecialCasesTest {

        @Test
        @DisplayName("극값 데이터 처리 테스트")
        void handleExtremeValues() {
            // Given
            TaskRequestDto extremeRequest = TaskRequestDto.builder()
                    .name("극값 테스트 태스크")
                    .description("극값 데이터 처리 테스트")
                    .startDate(LocalDate.MIN)
                    .endDate(LocalDate.MAX)
                    .duration(Integer.MAX_VALUE)
                    .progress(BigDecimal.valueOf(99.99999999))
                    .build();

            // When
            Task entity = taskMapper.toEntity(extremeRequest);
            TaskResponseDto responseDto = taskMapper.toResponseDto(entity);

            // Then
            assertThat(entity.getStartDate()).isEqualTo(LocalDate.MIN);
            assertThat(entity.getEndDate()).isEqualTo(LocalDate.MAX);
            assertThat(entity.getDuration()).isEqualTo(Integer.MAX_VALUE);
            assertThat(responseDto.getStartDate()).isEqualTo(LocalDate.MIN);
            assertThat(responseDto.getEndDate()).isEqualTo(LocalDate.MAX);
            assertThat(responseDto.getDuration()).isEqualTo(Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("특수 문자 및 이모지 처리 테스트")
        void handleSpecialCharacters() {
            // Given
            String specialName = "태스크 @#$%^&*()_+-=[]{}|;':\",./<>?";
            String emojiDescription = "설명 with émojis 😀🎉🚀💻 and unicode ★☆♠♥♦♣";

            TaskRequestDto specialCharRequest = TaskRequestDto.builder()
                    .name(specialName)
                    .description(emojiDescription)
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 1, 10))
                    .duration(10)
                    .progress(BigDecimal.valueOf(50.0))
                    .build();

            // When
            Task entity = taskMapper.toEntity(specialCharRequest);
            TaskResponseDto responseDto = taskMapper.toResponseDto(entity);

            // Then
            assertThat(entity.getName()).isEqualTo(specialName);
            assertThat(entity.getDescription()).isEqualTo(emojiDescription);
            assertThat(responseDto.getName()).isEqualTo(specialName);
            assertThat(responseDto.getDescription()).isEqualTo(emojiDescription);
        }

        @Test
        @DisplayName("음수 기간 처리 테스트")
        void handleNegativeDuration() {
            // Given
            TaskRequestDto negativeDurationRequest = TaskRequestDto.builder()
                    .name("음수 기간 태스크")
                    .description("음수 기간 테스트")
                    .startDate(LocalDate.of(2024, 1, 10))
                    .endDate(LocalDate.of(2024, 1, 1))
                    .duration(-9) // 음수 기간
                    .progress(BigDecimal.valueOf(0.0))
                    .build();

            // When
            Task entity = taskMapper.toEntity(negativeDurationRequest);
            TaskResponseDto responseDto = taskMapper.toResponseDto(entity);

            // Then
            assertThat(entity.getDuration()).isEqualTo(-9);
            assertThat(responseDto.getDuration()).isEqualTo(-9);
        }
    }
}