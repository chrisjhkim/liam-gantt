package com.liam.gantt.repository;

import com.liam.gantt.entity.Project;
import com.liam.gantt.entity.enums.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * ProjectRepository 통합 테스트
 * 
 * @author Liam
 * @since 1.0.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProjectRepository 통합 테스트")
class ProjectRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProjectRepository projectRepository;

    private Project testProject1;
    private Project testProject2;
    private Project testProject3;

    @BeforeEach
    void setUp() {
        testProject1 = Project.builder()
                .name("웹사이트 리뉴얼 프로젝트")
                .description("회사 웹사이트 전면 리뉴얼")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 3, 31))
                .status(ProjectStatus.IN_PROGRESS)
                .build();

        testProject2 = Project.builder()
                .name("모바일 앱 개발")
                .description("iOS/Android 앱 신규 개발")
                .startDate(LocalDate.of(2024, 2, 1))
                .endDate(LocalDate.of(2024, 6, 30))
                .status(ProjectStatus.PLANNING)
                .build();

        testProject3 = Project.builder()
                .name("시스템 유지보수")
                .description("기존 시스템 유지보수 및 개선")
                .startDate(LocalDate.of(2023, 12, 1))
                .endDate(LocalDate.of(2024, 1, 15))
                .status(ProjectStatus.COMPLETED)
                .build();

        // 테스트 데이터 저장
        entityManager.persistAndFlush(testProject1);
        entityManager.persistAndFlush(testProject2);
        entityManager.persistAndFlush(testProject3);
        entityManager.clear();
    }

    @Nested
    @DisplayName("기본 CRUD 테스트")
    class BasicCrudTest {

        @Test
        @DisplayName("프로젝트 저장 및 조회 테스트")
        void saveAndFind_Success() {
            // Given
            Project newProject = Project.builder()
                    .name("새로운 프로젝트")
                    .description("테스트용 새 프로젝트")
                    .startDate(LocalDate.of(2024, 4, 1))
                    .endDate(LocalDate.of(2024, 7, 31))
                    .status(ProjectStatus.PLANNING)
                    .build();

            // When
            Project savedProject = projectRepository.save(newProject);
            Optional<Project> foundProject = projectRepository.findById(savedProject.getId());

            // Then
            assertThat(savedProject.getId()).isNotNull();
            assertThat(foundProject).isPresent();
            assertThat(foundProject.get().getName()).isEqualTo("새로운 프로젝트");
            assertThat(foundProject.get().getStatus()).isEqualTo(ProjectStatus.PLANNING);
        }

        @Test
        @DisplayName("전체 프로젝트 조회 테스트")
        void findAll_Success() {
            // When
            List<Project> projects = projectRepository.findAll();

            // Then
            assertThat(projects).hasSize(3);
            assertThat(projects).extracting(Project::getName)
                    .contains("웹사이트 리뉴얼 프로젝트", "모바일 앱 개발", "시스템 유지보수");
        }

        @Test
        @DisplayName("프로젝트 삭제 테스트")
        void delete_Success() {
            // Given
            Long projectId = testProject1.getId();

            // When
            projectRepository.deleteById(projectId);
            Optional<Project> deletedProject = projectRepository.findById(projectId);

            // Then
            assertThat(deletedProject).isNotPresent();
            assertThat(projectRepository.findAll()).hasSize(2);
        }

        @Test
        @DisplayName("프로젝트 수정 테스트")
        void update_Success() {
            // Given
            Project project = projectRepository.findById(testProject1.getId()).orElseThrow();
            String newName = "수정된 프로젝트명";
            ProjectStatus newStatus = ProjectStatus.ON_HOLD;

            // When
            project.setName(newName);
            project.setStatus(newStatus);
            Project updatedProject = projectRepository.save(project);

            // Then
            assertThat(updatedProject.getName()).isEqualTo(newName);
            assertThat(updatedProject.getStatus()).isEqualTo(newStatus);
        }
    }

    @Nested
    @DisplayName("커스텀 쿼리 메서드 테스트")
    class CustomQueryTest {

        @Test
        @DisplayName("프로젝트명으로 조회 테스트")
        void findByName_Success() {
            // When
            Optional<Project> found = projectRepository.findByName("웹사이트 리뉴얼 프로젝트");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getDescription()).isEqualTo("회사 웹사이트 전면 리뉴얼");
            assertThat(found.get().getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("프로젝트명 존재 여부 확인 테스트")
        void existsByName_Success() {
            // When & Then
            assertThat(projectRepository.existsByName("웹사이트 리뉴얼 프로젝트")).isTrue();
            assertThat(projectRepository.existsByName("존재하지 않는 프로젝트")).isFalse();
        }

        @Test
        @DisplayName("프로젝트명 포함 검색 테스트")
        void findByNameContainingIgnoreCase_Success() {
            // When
            List<Project> webProjects = projectRepository.findByNameContainingIgnoreCase("웹사이트");
            List<Project> appProjects = projectRepository.findByNameContainingIgnoreCase("앱");

            // Then
            assertThat(webProjects).hasSize(1);
            assertThat(webProjects.get(0).getName()).contains("웹사이트");
            
            assertThat(appProjects).hasSize(1);
            assertThat(appProjects.get(0).getName()).contains("앱");
        }

        @Test
        @DisplayName("상태별 프로젝트 조회 테스트")
        void findByStatus_Success() {
            // When
            List<Project> planningProjects = projectRepository.findByStatus(ProjectStatus.PLANNING);
            List<Project> inProgressProjects = projectRepository.findByStatus(ProjectStatus.IN_PROGRESS);
            List<Project> completedProjects = projectRepository.findByStatus(ProjectStatus.COMPLETED);

            // Then
            assertThat(planningProjects).hasSize(1);
            assertThat(planningProjects.get(0).getName()).isEqualTo("모바일 앱 개발");

            assertThat(inProgressProjects).hasSize(1);
            assertThat(inProgressProjects.get(0).getName()).isEqualTo("웹사이트 리뉴얼 프로젝트");

            assertThat(completedProjects).hasSize(1);
            assertThat(completedProjects.get(0).getName()).isEqualTo("시스템 유지보수");
        }

        @Test
        @DisplayName("상태별 프로젝트 개수 조회 테스트")
        void countByStatus_Success() {
            // When & Then
            assertThat(projectRepository.countByStatus(ProjectStatus.PLANNING)).isEqualTo(1);
            assertThat(projectRepository.countByStatus(ProjectStatus.IN_PROGRESS)).isEqualTo(1);
            assertThat(projectRepository.countByStatus(ProjectStatus.COMPLETED)).isEqualTo(1);
            assertThat(projectRepository.countByStatus(ProjectStatus.ON_HOLD)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("날짜 범위 검색 테스트")
    class DateRangeTest {

        @Test
        @DisplayName("시작일 기준 프로젝트 조회 테스트")
        void findByStartDateBetween_Success() {
            // Given
            LocalDate fromDate = LocalDate.of(2024, 1, 1);
            LocalDate toDate = LocalDate.of(2024, 2, 28);

            // When
            List<Project> projects = projectRepository.findByStartDateBetween(fromDate, toDate);

            // Then
            assertThat(projects).hasSize(2);
            assertThat(projects).extracting(Project::getName)
                    .contains("웹사이트 리뉴얼 프로젝트", "모바일 앱 개발");
        }

        @Test
        @DisplayName("종료일 기준 프로젝트 조회 테스트")
        void findByEndDateBefore_Success() {
            // Given
            LocalDate criteriaDate = LocalDate.of(2024, 2, 1);

            // When
            List<Project> projects = projectRepository.findByEndDateBefore(criteriaDate);

            // Then
            assertThat(projects).hasSize(1);
            assertThat(projects.get(0).getName()).isEqualTo("시스템 유지보수");
        }

        @Test
        @DisplayName("진행 중인 프로젝트 조회 테스트")
        void findActiveProjects_Success() {
            // Given
            LocalDate currentDate = LocalDate.of(2024, 1, 15);

            // When
            List<Project> activeProjects = projectRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
                    currentDate, currentDate, ProjectStatus.IN_PROGRESS);

            // Then
            assertThat(activeProjects).hasSize(1);
            assertThat(activeProjects.get(0).getName()).isEqualTo("웹사이트 리뉴얼 프로젝트");
        }
    }

    @Nested
    @DisplayName("페이징 및 정렬 테스트")
    class PagingAndSortingTest {

        @Test
        @DisplayName("페이징 조회 테스트")
        void findAllWithPaging_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 2, Sort.by("name"));

            // When
            Page<Project> projectPage = projectRepository.findAll(pageable);

            // Then
            assertThat(projectPage.getContent()).hasSize(2);
            assertThat(projectPage.getTotalElements()).isEqualTo(3);
            assertThat(projectPage.getTotalPages()).isEqualTo(2);
            assertThat(projectPage.isFirst()).isTrue();
        }

        @Test
        @DisplayName("이름순 정렬 테스트")
        void findAllSortedByName_Success() {
            // Given
            Sort sort = Sort.by(Sort.Direction.ASC, "name");

            // When
            List<Project> sortedProjects = projectRepository.findAll(sort);

            // Then
            assertThat(sortedProjects).hasSize(3);
            assertThat(sortedProjects.get(0).getName()).isEqualTo("모바일 앱 개발");
            assertThat(sortedProjects.get(1).getName()).isEqualTo("시스템 유지보수");
            assertThat(sortedProjects.get(2).getName()).isEqualTo("웹사이트 리뉴얼 프로젝트");
        }

        @Test
        @DisplayName("시작일순 정렬 테스트")
        void findAllSortedByStartDate_Success() {
            // Given
            Sort sort = Sort.by(Sort.Direction.ASC, "startDate");

            // When
            List<Project> sortedProjects = projectRepository.findAll(sort);

            // Then
            assertThat(sortedProjects).hasSize(3);
            assertThat(sortedProjects.get(0).getName()).isEqualTo("시스템 유지보수"); // 2023-12-01
            assertThat(sortedProjects.get(1).getName()).isEqualTo("웹사이트 리뉴얼 프로젝트"); // 2024-01-01
            assertThat(sortedProjects.get(2).getName()).isEqualTo("모바일 앱 개발"); // 2024-02-01
        }
    }

    @Nested
    @DisplayName("복잡한 검색 조건 테스트")
    class ComplexSearchTest {

        @Test
        @DisplayName("이름과 상태 조건 검색 테스트")
        void findByNameContainingAndStatus_Success() {
            // When
            List<Project> results = projectRepository.findByNameContainingIgnoreCaseAndStatus(
                    "프로젝트", ProjectStatus.IN_PROGRESS);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getName()).isEqualTo("웹사이트 리뉴얼 프로젝트");
        }

        @Test
        @DisplayName("상태와 날짜 범위 조건 검색 테스트")
        void findByStatusAndDateRange_Success() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 31);

            // When
            List<Project> results = projectRepository.findByStatusAndStartDateBetween(
                    ProjectStatus.PLANNING, startDate, endDate);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getName()).isEqualTo("모바일 앱 개발");
        }
    }

    @Nested
    @DisplayName("ID 및 이름 제외 검색 테스트")
    class ExclusionSearchTest {

        @Test
        @DisplayName("특정 ID 제외하고 이름 중복 확인 테스트")
        void existsByNameAndIdNot_Success() {
            // Given
            Long excludeId = testProject1.getId();
            String existingName = "웹사이트 리뉴얼 프로젝트";
            String newName = "완전히 새로운 프로젝트명";

            // When & Then
            // 기존 이름이지만 같은 ID면 중복 아님
            assertThat(projectRepository.existsByNameAndIdNot(existingName, excludeId)).isFalse();
            
            // 기존 이름이고 다른 ID면 중복임
            assertThat(projectRepository.existsByNameAndIdNot(existingName, 999L)).isTrue();
            
            // 새로운 이름이면 중복 아님
            assertThat(projectRepository.existsByNameAndIdNot(newName, 999L)).isFalse();
        }
    }

    @Nested
    @DisplayName("데이터 일관성 테스트")
    class DataIntegrityTest {

        @Test
        @DisplayName("프로젝트명 유니크 제약 조건 테스트")
        void uniqueNameConstraint_Test() {
            // Given
            Project duplicateProject = Project.builder()
                    .name("웹사이트 리뉴얼 프로젝트") // 이미 존재하는 이름
                    .description("중복된 이름의 프로젝트")
                    .startDate(LocalDate.of(2024, 5, 1))
                    .endDate(LocalDate.of(2024, 8, 31))
                    .status(ProjectStatus.PLANNING)
                    .build();

            // When & Then
            assertThatThrownBy(() -> {
                projectRepository.saveAndFlush(duplicateProject);
            }).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("날짜 제약 조건 검증 테스트")
        void dateConstraintValidation_Test() {
            // Given
            Project invalidDateProject = Project.builder()
                    .name("잘못된 날짜 프로젝트")
                    .description("종료일이 시작일보다 빠른 프로젝트")
                    .startDate(LocalDate.of(2024, 6, 1))
                    .endDate(LocalDate.of(2024, 3, 1)) // 시작일보다 빠름
                    .status(ProjectStatus.PLANNING)
                    .build();

            // When & Then
            assertThatThrownBy(() -> {
                projectRepository.saveAndFlush(invalidDateProject);
            }).isInstanceOf(Exception.class);
        }
    }
}