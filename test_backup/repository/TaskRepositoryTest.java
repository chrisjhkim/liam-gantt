package com.liam.gantt.repository;

import com.liam.gantt.entity.Project;
import com.liam.gantt.entity.Task;
import com.liam.gantt.entity.enums.ProjectStatus;
import com.liam.gantt.entity.enums.TaskStatus;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * TaskRepository 통합 테스트
 * 
 * @author Liam
 * @since 1.0.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TaskRepository 통합 테스트")
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private Project testProject;
    private Task task1;
    private Task task2;
    private Task task3;
    private Task parentTask;
    private Task subTask;

    @BeforeEach
    void setUp() {
        // 테스트 프로젝트 생성
        testProject = Project.builder()
                .name("테스트 프로젝트")
                .description("태스크 테스트용 프로젝트")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 3, 31))
                .status(ProjectStatus.IN_PROGRESS)
                .build();

        entityManager.persistAndFlush(testProject);

        // 테스트 태스크들 생성
        task1 = Task.builder()
                .project(testProject)
                .name("요구사항 분석")
                .description("프로젝트 요구사항 수집 및 분석")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 10))
                .duration(10)
                .progress(BigDecimal.valueOf(100.0))
                .status(TaskStatus.COMPLETED)
                .build();

        task2 = Task.builder()
                .project(testProject)
                .name("시스템 설계")
                .description("시스템 아키텍처 및 상세 설계")
                .startDate(LocalDate.of(2024, 1, 11))
                .endDate(LocalDate.of(2024, 1, 25))
                .duration(15)
                .progress(BigDecimal.valueOf(75.0))
                .status(TaskStatus.IN_PROGRESS)
                .build();

        task3 = Task.builder()
                .project(testProject)
                .name("개발 구현")
                .description("시스템 개발 및 구현")
                .startDate(LocalDate.of(2024, 1, 26))
                .endDate(LocalDate.of(2024, 2, 20))
                .duration(25)
                .progress(BigDecimal.valueOf(0.0))
                .status(TaskStatus.NOT_STARTED)
                .build();

        // 상위-하위 태스크 관계
        parentTask = Task.builder()
                .project(testProject)
                .name("전체 테스트")
                .description("통합 테스트 및 단위 테스트")
                .startDate(LocalDate.of(2024, 2, 21))
                .endDate(LocalDate.of(2024, 3, 10))
                .duration(18)
                .progress(BigDecimal.valueOf(30.0))
                .status(TaskStatus.IN_PROGRESS)
                .build();

        entityManager.persistAndFlush(task1);
        entityManager.persistAndFlush(task2);
        entityManager.persistAndFlush(task3);
        entityManager.persistAndFlush(parentTask);

        subTask = Task.builder()
                .project(testProject)
                .parentTask(parentTask)
                .name("단위 테스트")
                .description("개별 모듈 단위 테스트")
                .startDate(LocalDate.of(2024, 2, 21))
                .endDate(LocalDate.of(2024, 3, 1))
                .duration(9)
                .progress(BigDecimal.valueOf(50.0))
                .status(TaskStatus.IN_PROGRESS)
                .build();

        entityManager.persistAndFlush(subTask);
        entityManager.clear();
    }

    @Nested
    @DisplayName("기본 CRUD 테스트")
    class BasicCrudTest {

        @Test
        @DisplayName("태스크 저장 및 조회 테스트")
        void saveAndFind_Success() {
            // Given
            Task newTask = Task.builder()
                    .project(testProject)
                    .name("새로운 태스크")
                    .description("테스트용 새 태스크")
                    .startDate(LocalDate.of(2024, 3, 11))
                    .endDate(LocalDate.of(2024, 3, 20))
                    .duration(10)
                    .progress(BigDecimal.valueOf(0.0))
                    .status(TaskStatus.NOT_STARTED)
                    .build();

            // When
            Task savedTask = taskRepository.save(newTask);
            Optional<Task> foundTask = taskRepository.findById(savedTask.getId());

            // Then
            assertThat(savedTask.getId()).isNotNull();
            assertThat(foundTask).isPresent();
            assertThat(foundTask.get().getName()).isEqualTo("새로운 태스크");
            assertThat(foundTask.get().getProject().getId()).isEqualTo(testProject.getId());
        }

        @Test
        @DisplayName("태스크 삭제 테스트")
        void delete_Success() {
            // Given
            Long taskId = task1.getId();

            // When
            taskRepository.deleteById(taskId);
            Optional<Task> deletedTask = taskRepository.findById(taskId);

            // Then
            assertThat(deletedTask).isNotPresent();
        }

        @Test
        @DisplayName("태스크 수정 테스트")
        void update_Success() {
            // Given
            Task task = taskRepository.findById(task2.getId()).orElseThrow();
            BigDecimal newProgress = BigDecimal.valueOf(90.0);
            TaskStatus newStatus = TaskStatus.COMPLETED;

            // When
            task.setProgress(newProgress);
            task.setStatus(newStatus);
            Task updatedTask = taskRepository.save(task);

            // Then
            assertThat(updatedTask.getProgress()).isEqualByComparingTo(newProgress);
            assertThat(updatedTask.getStatus()).isEqualTo(newStatus);
        }
    }

    @Nested
    @DisplayName("프로젝트별 태스크 조회 테스트")
    class FindByProjectTest {

        @Test
        @DisplayName("프로젝트 ID로 태스크 목록 조회 테스트")
        void findByProjectId_Success() {
            // When
            List<Task> tasks = taskRepository.findByProjectId(testProject.getId());

            // Then
            assertThat(tasks).hasSize(5); // task1, task2, task3, parentTask, subTask
            assertThat(tasks).extracting(Task::getName)
                    .contains("요구사항 분석", "시스템 설계", "개발 구현", "전체 테스트", "단위 테스트");
        }

        @Test
        @DisplayName("프로젝트 ID로 시작일 오름차순 태스크 조회 테스트")
        void findByProjectIdOrderByStartDateAsc_Success() {
            // When
            List<Task> tasks = taskRepository.findByProjectIdOrderByStartDateAsc(testProject.getId());

            // Then
            assertThat(tasks).hasSize(5);
            assertThat(tasks.get(0).getName()).isEqualTo("요구사항 분석"); // 1월 1일
            assertThat(tasks.get(1).getName()).isEqualTo("시스템 설계"); // 1월 11일
            assertThat(tasks.get(2).getName()).isEqualTo("개발 구현"); // 1월 26일
            assertThat(tasks.get(3).getName()).isEqualTo("전체 테스트"); // 2월 21일
            assertThat(tasks.get(4).getName()).isEqualTo("단위 테스트"); // 2월 21일 (같은 날)
        }

        @Test
        @DisplayName("프로젝트별 태스크 페이징 조회 테스트")
        void findByProjectIdWithPaging_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 3, Sort.by("startDate"));

            // When
            Page<Task> taskPage = taskRepository.findByProjectId(testProject.getId(), pageable);

            // Then
            assertThat(taskPage.getContent()).hasSize(3);
            assertThat(taskPage.getTotalElements()).isEqualTo(5);
            assertThat(taskPage.getTotalPages()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("상태별 태스크 조회 테스트")
    class FindByStatusTest {

        @Test
        @DisplayName("프로젝트와 상태별 태스크 조회 테스트")
        void findByProjectIdAndStatus_Success() {
            // When
            List<Task> completedTasks = taskRepository.findByProjectIdAndStatus(testProject.getId(), TaskStatus.COMPLETED);
            List<Task> inProgressTasks = taskRepository.findByProjectIdAndStatus(testProject.getId(), TaskStatus.IN_PROGRESS);
            List<Task> notStartedTasks = taskRepository.findByProjectIdAndStatus(testProject.getId(), TaskStatus.NOT_STARTED);

            // Then
            assertThat(completedTasks).hasSize(1);
            assertThat(completedTasks.get(0).getName()).isEqualTo("요구사항 분석");

            assertThat(inProgressTasks).hasSize(3);
            assertThat(inProgressTasks).extracting(Task::getName)
                    .contains("시스템 설계", "전체 테스트", "단위 테스트");

            assertThat(notStartedTasks).hasSize(1);
            assertThat(notStartedTasks.get(0).getName()).isEqualTo("개발 구현");
        }
    }

    @Nested
    @DisplayName("계층 구조 태스크 조회 테스트")
    class HierarchyTest {

        @Test
        @DisplayName("상위 태스크별 하위 태스크 조회 테스트")
        void findByParentTaskId_Success() {
            // When
            List<Task> subTasks = taskRepository.findByParentTaskId(parentTask.getId());

            // Then
            assertThat(subTasks).hasSize(1);
            assertThat(subTasks.get(0).getName()).isEqualTo("단위 테스트");
            assertThat(subTasks.get(0).getParentTask().getId()).isEqualTo(parentTask.getId());
        }

        @Test
        @DisplayName("루트 태스크 조회 테스트")
        void findByProjectIdAndParentTaskIsNull_Success() {
            // When
            List<Task> rootTasks = taskRepository.findByProjectIdAndParentTaskIsNull(testProject.getId());

            // Then
            assertThat(rootTasks).hasSize(4); // task1, task2, task3, parentTask (subTask 제외)
            assertThat(rootTasks).extracting(Task::getName)
                    .contains("요구사항 분석", "시스템 설계", "개발 구현", "전체 테스트")
                    .doesNotContain("단위 테스트");
        }
    }

    @Nested
    @DisplayName("검색 기능 테스트")
    class SearchTest {

        @Test
        @DisplayName("태스크명으로 검색 테스트")
        void findByProjectIdAndNameContainingIgnoreCase_Success() {
            // When
            List<Task> testTasks = taskRepository.findByProjectIdAndNameContainingIgnoreCase(
                    testProject.getId(), "테스트");
            List<Task> systemTasks = taskRepository.findByProjectIdAndNameContainingIgnoreCase(
                    testProject.getId(), "시스템");

            // Then
            assertThat(testTasks).hasSize(2);
            assertThat(testTasks).extracting(Task::getName)
                    .contains("전체 테스트", "단위 테스트");

            assertThat(systemTasks).hasSize(1);
            assertThat(systemTasks.get(0).getName()).isEqualTo("시스템 설계");
        }

        @Test
        @DisplayName("날짜 범위로 태스크 검색 테스트")
        void findByStartDateBetween_Success() {
            // Given
            LocalDate fromDate = LocalDate.of(2024, 1, 1);
            LocalDate toDate = LocalDate.of(2024, 1, 31);

            // When
            List<Task> januaryTasks = taskRepository.findByStartDateBetween(fromDate, toDate);

            // Then
            assertThat(januaryTasks).hasSize(3);
            assertThat(januaryTasks).extracting(Task::getName)
                    .contains("요구사항 분석", "시스템 설계", "개발 구현");
        }
    }

    @Nested
    @DisplayName("커스텀 쿼리 테스트")
    class CustomQueryTest {

        @Test
        @DisplayName("지연된 태스크 조회 테스트")
        void findOverdueTasks_Success() {
            // Given
            LocalDate currentDate = LocalDate.of(2024, 1, 30);

            // When
            List<Task> overdueTasks = taskRepository.findOverdueTasks(testProject.getId(), currentDate);

            // Then
            // 종료일이 1/25인 task2(시스템 설계)가 IN_PROGRESS 상태이므로 지연됨
            assertThat(overdueTasks).hasSize(1);
            assertThat(overdueTasks.get(0).getName()).isEqualTo("시스템 설계");
            assertThat(overdueTasks.get(0).getEndDate()).isBefore(currentDate);
            assertThat(overdueTasks.get(0).getStatus()).isNotIn(TaskStatus.COMPLETED, TaskStatus.CANCELLED);
        }

        @Test
        @DisplayName("진행률 범위로 태스크 조회 테스트")
        void findByProgressRange_Success() {
            // Given
            BigDecimal minProgress = BigDecimal.valueOf(50.0);
            BigDecimal maxProgress = BigDecimal.valueOf(100.0);

            // When
            List<Task> tasks = taskRepository.findByProgressRange(testProject.getId(), minProgress, maxProgress);

            // Then
            assertThat(tasks).hasSize(3);
            assertThat(tasks).extracting(Task::getName)
                    .contains("요구사항 분석", "시스템 설계", "단위 테스트");
        }

        @Test
        @DisplayName("프로젝트별 태스크 개수 조회 테스트")
        void countByProjectId_Success() {
            // When
            long taskCount = taskRepository.countByProjectId(testProject.getId());

            // Then
            assertThat(taskCount).isEqualTo(5);
        }

        @Test
        @DisplayName("완료된 태스크 개수 조회 테스트")
        void countCompletedTasksByProjectId_Success() {
            // When
            long completedCount = taskRepository.countCompletedTasksByProjectId(testProject.getId());

            // Then
            assertThat(completedCount).isEqualTo(1); // task1(요구사항 분석)만 완료
        }
    }

    @Nested
    @DisplayName("진행률 업데이트 테스트")
    class ProgressUpdateTest {

        @Test
        @DisplayName("태스크 진행률 업데이트 테스트")
        void updateTaskProgress_Success() {
            // Given
            Long taskId = task3.getId();
            BigDecimal newProgress = BigDecimal.valueOf(25.0);
            TaskStatus newStatus = TaskStatus.IN_PROGRESS;

            // When
            int updatedCount = taskRepository.updateTaskProgress(taskId, newProgress, newStatus);

            // Then
            assertThat(updatedCount).isEqualTo(1);

            Task updatedTask = taskRepository.findById(taskId).orElseThrow();
            assertThat(updatedTask.getProgress()).isEqualByComparingTo(newProgress);
            assertThat(updatedTask.getStatus()).isEqualTo(newStatus);
        }
    }

    @Nested
    @DisplayName("데이터 일관성 테스트")
    class DataIntegrityTest {

        @Test
        @DisplayName("태스크-프로젝트 관계 일관성 테스트")
        void taskProjectRelationship_Success() {
            // When
            Task task = taskRepository.findById(task1.getId()).orElseThrow();

            // Then
            assertThat(task.getProject()).isNotNull();
            assertThat(task.getProject().getId()).isEqualTo(testProject.getId());
            assertThat(task.getProject().getName()).isEqualTo("테스트 프로젝트");
        }

        @Test
        @DisplayName("상위-하위 태스크 관계 일관성 테스트")
        void parentChildTaskRelationship_Success() {
            // When
            Task child = taskRepository.findById(subTask.getId()).orElseThrow();
            Task parent = taskRepository.findById(parentTask.getId()).orElseThrow();

            // Then
            assertThat(child.getParentTask()).isNotNull();
            assertThat(child.getParentTask().getId()).isEqualTo(parent.getId());
        }

        @Test
        @DisplayName("날짜 제약 조건 검증 테스트")
        void dateConstraintValidation_Test() {
            // Given
            Task invalidDateTask = Task.builder()
                    .project(testProject)
                    .name("잘못된 날짜 태스크")
                    .description("종료일이 시작일보다 빠른 태스크")
                    .startDate(LocalDate.of(2024, 3, 1))
                    .endDate(LocalDate.of(2024, 2, 1)) // 시작일보다 빠름
                    .duration(10)
                    .progress(BigDecimal.valueOf(0.0))
                    .status(TaskStatus.NOT_STARTED)
                    .build();

            // When & Then
            assertThatThrownBy(() -> {
                taskRepository.saveAndFlush(invalidDateTask);
            }).isInstanceOf(Exception.class);
        }
    }
}