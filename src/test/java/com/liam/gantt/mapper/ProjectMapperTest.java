package com.liam.gantt.mapper;

import com.liam.gantt.dto.request.ProjectRequestDto;
import com.liam.gantt.dto.response.ProjectResponseDto;
import com.liam.gantt.entity.Project;
import com.liam.gantt.entity.enums.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ProjectMapper 단위 테스트
 * 
 * @author Liam
 * @since 1.0.0
 */
@DisplayName("ProjectMapper 단위 테스트")
class ProjectMapperTest {

    private ProjectMapper projectMapper;
    private TaskMapper taskMapper;
    private Project testProject;
    private ProjectRequestDto testRequestDto;
    private ProjectResponseDto testResponseDto;

    @BeforeEach
    void setUp() {
        taskMapper = mock(TaskMapper.class);
        projectMapper = new ProjectMapper();
        // TaskMapper를 주입하기 위해 리플렉션 사용
        try {
            java.lang.reflect.Field field = ProjectMapper.class.getDeclaredField("taskMapper");
            field.setAccessible(true);
            field.set(projectMapper, taskMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        testProject = Project.builder()
                .name("테스트 프로젝트")
                .description("테스트용 프로젝트입니다")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 3, 31))
                .status(ProjectStatus.IN_PROGRESS)
                .build();

        testRequestDto = ProjectRequestDto.builder()
                .name("요청 프로젝트")
                .description("요청용 프로젝트입니다")
                .startDate(LocalDate.of(2024, 2, 1))
                .endDate(LocalDate.of(2024, 4, 30))
                .build();

        testResponseDto = ProjectResponseDto.builder()
                .id(1L)
                .name("응답 프로젝트")
                .description("응답용 프로젝트입니다")
                .startDate(LocalDate.of(2024, 3, 1))
                .endDate(LocalDate.of(2024, 5, 31))
                .status(ProjectStatus.COMPLETED)
                .taskCount(5)
                .progress(100.0)
                .build();
    }

    @Nested
    @DisplayName("Entity to ResponseDto 변환 테스트")
    class EntityToResponseDtoTest {

        @Test
        @DisplayName("Project Entity를 ProjectResponseDto로 변환 성공")
        void toResponseDto_Success() {
            // When
            ProjectResponseDto result = projectMapper.toResponseDto(testProject);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testProject.getId());
            assertThat(result.getName()).isEqualTo(testProject.getName());
            assertThat(result.getDescription()).isEqualTo(testProject.getDescription());
            assertThat(result.getStartDate()).isEqualTo(testProject.getStartDate());
            assertThat(result.getEndDate()).isEqualTo(testProject.getEndDate());
            assertThat(result.getStatus()).isEqualTo(testProject.getStatus());
        }

        @Test
        @DisplayName("null Entity를 변환 시 null 반환")
        void toResponseDto_NullEntity_ReturnsNull() {
            // When
            ProjectResponseDto result = projectMapper.toResponseDto(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("필수 필드만 있는 Entity 변환 성공")
        void toResponseDto_MinimalFields_Success() {
            // Given
            Project minimalProject = Project.builder()
                    .name("최소 프로젝트")
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 1, 31))
                    .status(ProjectStatus.PLANNING)
                    .build();
            // description은 null

            // When
            ProjectResponseDto result = projectMapper.toResponseDto(minimalProject);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNull(); // ID는 GeneratedValue이므로 테스트에서는 null
            assertThat(result.getName()).isEqualTo("최소 프로젝트");
            assertThat(result.getDescription()).isNull();
            assertThat(result.getStatus()).isEqualTo(ProjectStatus.PLANNING);
        }
    }

    @Nested
    @DisplayName("RequestDto to Entity 변환 테스트")
    class RequestDtoToEntityTest {

        @Test
        @DisplayName("ProjectRequestDto를 Project Entity로 변환 성공")
        void toEntity_Success() {
            // When
            Project result = projectMapper.toEntity(testRequestDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNull(); // 새로 생성되는 Entity이므로 ID는 null
            assertThat(result.getName()).isEqualTo(testRequestDto.getName());
            assertThat(result.getDescription()).isEqualTo(testRequestDto.getDescription());
            assertThat(result.getStartDate()).isEqualTo(testRequestDto.getStartDate());
            assertThat(result.getEndDate()).isEqualTo(testRequestDto.getEndDate());
            assertThat(result.getStatus()).isEqualTo(ProjectStatus.PLANNING); // 기본값
        }

        @Test
        @DisplayName("null RequestDto를 변환 시 null 반환")
        void toEntity_NullDto_ReturnsNull() {
            // When
            Project result = projectMapper.toEntity(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("필수 필드만 있는 RequestDto 변환 성공")
        void toEntity_MinimalFields_Success() {
            // Given
            ProjectRequestDto minimalRequest = ProjectRequestDto.builder()
                    .name("최소 요청")
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 1, 31))
                    .build();
            // description은 null

            // When
            Project result = projectMapper.toEntity(minimalRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("최소 요청");
            assertThat(result.getDescription()).isNull();
            assertThat(result.getStatus()).isEqualTo(ProjectStatus.PLANNING);
        }
    }

    @Nested
    @DisplayName("Entity 업데이트 테스트")
    class UpdateEntityTest {

        @Test
        @DisplayName("ProjectRequestDto로 기존 Entity 업데이트 성공")
        void updateEntity_Success() {
            // Given
            Project existingProject = Project.builder()
                    .name("기존 프로젝트")
                    .description("기존 설명")
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 2, 29))
                    .status(ProjectStatus.IN_PROGRESS)
                    .build();
            // ID 설정 (BaseEntity 필드)
            existingProject.setId(1L);

            ProjectRequestDto updateRequest = ProjectRequestDto.builder()
                    .name("수정된 프로젝트")
                    .description("수정된 설명")
                    .startDate(LocalDate.of(2024, 2, 1))
                    .endDate(LocalDate.of(2024, 4, 30))
                    .build();

            // When
            projectMapper.updateEntity(existingProject, updateRequest);

            // Then
            assertThat(existingProject.getId()).isEqualTo(1L); // ID는 유지
            assertThat(existingProject.getName()).isEqualTo("수정된 프로젝트");
            assertThat(existingProject.getDescription()).isEqualTo("수정된 설명");
            assertThat(existingProject.getStartDate()).isEqualTo(LocalDate.of(2024, 2, 1));
            assertThat(existingProject.getEndDate()).isEqualTo(LocalDate.of(2024, 4, 30));
            assertThat(existingProject.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS); // 상태는 유지
        }

        @Test
        @DisplayName("일부 필드만 업데이트")
        void updateEntity_PartialUpdate_Success() {
            // Given
            Project existingProject = Project.builder()
                    .name("기존 프로젝트")
                    .description("기존 설명")
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 2, 29))
                    .status(ProjectStatus.IN_PROGRESS)
                    .build();

            ProjectRequestDto updateRequest = ProjectRequestDto.builder()
                    .name("수정된 이름만")
                    .description("기존 설명") // 동일한 값
                    .startDate(LocalDate.of(2024, 1, 1)) // 동일한 값
                    .endDate(LocalDate.of(2024, 2, 29)) // 동일한 값
                    .build();

            // When
            projectMapper.updateEntity(existingProject, updateRequest);

            // Then
            assertThat(existingProject.getName()).isEqualTo("수정된 이름만");
            assertThat(existingProject.getDescription()).isEqualTo("기존 설명");
            assertThat(existingProject.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("null 값으로 업데이트 처리")
        void updateEntity_WithNullValues() {
            // Given
            Project existingProject = Project.builder()
                    .name("기존 프로젝트")
                    .description("기존 설명")
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 2, 29))
                    .status(ProjectStatus.IN_PROGRESS)
                    .build();

            // RequestDto에서 description이 null인 경우
            ProjectRequestDto updateRequest = ProjectRequestDto.builder()
                    .name("수정된 프로젝트")
                    .description(null) // null 값
                    .startDate(LocalDate.of(2024, 2, 1))
                    .endDate(LocalDate.of(2024, 4, 30))
                    .build();

            // When
            projectMapper.updateEntity(existingProject, updateRequest);

            // Then
            assertThat(existingProject.getName()).isEqualTo("수정된 프로젝트");
            assertThat(existingProject.getDescription()).isNull();
            assertThat(existingProject.getStartDate()).isEqualTo(LocalDate.of(2024, 2, 1));
            assertThat(existingProject.getEndDate()).isEqualTo(LocalDate.of(2024, 4, 30));
        }
    }

    @Nested
    @DisplayName("매핑 일관성 테스트")
    class MappingConsistencyTest {

        @Test
        @DisplayName("Entity -> ResponseDto -> Entity 변환 일관성")
        void roundTripConsistency_EntityToResponseDtoToEntity() {
            // Given
            Project originalProject = testProject;

            // When
            ProjectResponseDto responseDto = projectMapper.toResponseDto(originalProject);
            // ResponseDto에서 다시 Entity로 직접 변환하는 메서드는 없으므로
            // RequestDto로 변환한 후 Entity로 변환
            ProjectRequestDto requestDto = ProjectRequestDto.builder()
                    .name(responseDto.getName())
                    .description(responseDto.getDescription())
                    .startDate(responseDto.getStartDate())
                    .endDate(responseDto.getEndDate())
                    .build();
            Project resultProject = projectMapper.toEntity(requestDto);

            // Then
            assertThat(resultProject.getName()).isEqualTo(originalProject.getName());
            assertThat(resultProject.getDescription()).isEqualTo(originalProject.getDescription());
            assertThat(resultProject.getStartDate()).isEqualTo(originalProject.getStartDate());
            assertThat(resultProject.getEndDate()).isEqualTo(originalProject.getEndDate());
            // ID와 상태는 변환 과정에서 다를 수 있음
        }

        @Test
        @DisplayName("RequestDto -> Entity -> ResponseDto 변환 일관성")
        void roundTripConsistency_RequestDtoToEntityToResponseDto() {
            // Given
            ProjectRequestDto originalRequest = testRequestDto;

            // When
            Project entity = projectMapper.toEntity(originalRequest);
            entity.setId(1L); // ID 설정 (실제로는 DB에서 자동 생성)
            ProjectResponseDto responseDto = projectMapper.toResponseDto(entity);

            // Then
            assertThat(responseDto.getName()).isEqualTo(originalRequest.getName());
            assertThat(responseDto.getDescription()).isEqualTo(originalRequest.getDescription());
            assertThat(responseDto.getStartDate()).isEqualTo(originalRequest.getStartDate());
            assertThat(responseDto.getEndDate()).isEqualTo(originalRequest.getEndDate());
            assertThat(responseDto.getId()).isEqualTo(1L);
            assertThat(responseDto.getStatus()).isEqualTo(ProjectStatus.PLANNING); // 기본값
        }
    }

    @Nested
    @DisplayName("특수 케이스 테스트")
    class SpecialCasesTest {

        @Test
        @DisplayName("극값 날짜 처리 테스트")
        void handleExtremeDates() {
            // Given
            ProjectRequestDto extremeDateRequest = ProjectRequestDto.builder()
                    .name("극값 날짜 프로젝트")
                    .description("극값 날짜 테스트")
                    .startDate(LocalDate.MIN)
                    .endDate(LocalDate.MAX)
                    .build();

            // When
            Project entity = projectMapper.toEntity(extremeDateRequest);
            ProjectResponseDto responseDto = projectMapper.toResponseDto(entity);

            // Then
            assertThat(entity.getStartDate()).isEqualTo(LocalDate.MIN);
            assertThat(entity.getEndDate()).isEqualTo(LocalDate.MAX);
            assertThat(responseDto.getStartDate()).isEqualTo(LocalDate.MIN);
            assertThat(responseDto.getEndDate()).isEqualTo(LocalDate.MAX);
        }

        @Test
        @DisplayName("긴 문자열 처리 테스트")
        void handleLongStrings() {
            // Given
            String longName = "매우".repeat(100) + " 긴 프로젝트명";
            String longDescription = "매우".repeat(500) + " 긴 설명";

            ProjectRequestDto longStringRequest = ProjectRequestDto.builder()
                    .name(longName)
                    .description(longDescription)
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 12, 31))
                    .build();

            // When
            Project entity = projectMapper.toEntity(longStringRequest);
            ProjectResponseDto responseDto = projectMapper.toResponseDto(entity);

            // Then
            assertThat(entity.getName()).isEqualTo(longName);
            assertThat(entity.getDescription()).isEqualTo(longDescription);
            assertThat(responseDto.getName()).isEqualTo(longName);
            assertThat(responseDto.getDescription()).isEqualTo(longDescription);
        }

        @Test
        @DisplayName("특수 문자 처리 테스트")
        void handleSpecialCharacters() {
            // Given
            String specialName = "프로젝트 @#$%^&*()_+-=[]{}|;':\",./<>?";
            String specialDescription = "설명 with émojis 😀🎉 and unicode ★☆♠♥♦♣";

            ProjectRequestDto specialCharRequest = ProjectRequestDto.builder()
                    .name(specialName)
                    .description(specialDescription)
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 12, 31))
                    .build();

            // When
            Project entity = projectMapper.toEntity(specialCharRequest);
            ProjectResponseDto responseDto = projectMapper.toResponseDto(entity);

            // Then
            assertThat(entity.getName()).isEqualTo(specialName);
            assertThat(entity.getDescription()).isEqualTo(specialDescription);
            assertThat(responseDto.getName()).isEqualTo(specialName);
            assertThat(responseDto.getDescription()).isEqualTo(specialDescription);
        }
    }
}