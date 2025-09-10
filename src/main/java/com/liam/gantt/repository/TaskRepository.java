package com.liam.gantt.repository;

import com.liam.gantt.entity.Task;
import com.liam.gantt.entity.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 태스크 엔티티에 대한 데이터 액세스 인터페이스
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    /**
     * 프로젝트별 태스크 조회
     */
    List<Task> findByProjectId(Long projectId);
    
    /**
     * 프로젝트별 태스크 조회 (시작일 오름차순)
     */
    List<Task> findByProjectIdOrderByStartDateAsc(Long projectId);
    
    /**
     * 프로젝트별 태스크 페이징 조회
     */
    Page<Task> findByProjectId(Long projectId, Pageable pageable);
    
    /**
     * 프로젝트와 상태별 태스크 조회
     */
    List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status);
    
    /**
     * 상위 태스크별 하위 태스크 조회
     */
    List<Task> findByParentTaskId(Long parentTaskId);
    
    /**
     * 루트 태스크 조회 (상위 태스크가 없는 태스크)
     */
    List<Task> findByProjectIdAndParentTaskIsNull(Long projectId);
    
    /**
     * 태스크명으로 검색
     */
    List<Task> findByProjectIdAndNameContainingIgnoreCase(Long projectId, String keyword);
    
    /**
     * 날짜 범위로 태스크 조회
     */
    List<Task> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 프로젝트의 지연된 태스크 조회
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId " +
           "AND t.endDate < :currentDate AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findOverdueTasks(
        @Param("projectId") Long projectId,
        @Param("currentDate") LocalDate currentDate
    );
    
    /**
     * 태스크와 의존성 관계를 함께 조회 (N+1 문제 방지)
     */
    @Query("SELECT DISTINCT t FROM Task t " +
           "LEFT JOIN FETCH t.predecessorDependencies " +
           "LEFT JOIN FETCH t.successorDependencies " +
           "WHERE t.id = :id")
    Optional<Task> findByIdWithDependencies(@Param("id") Long id);
    
    /**
     * 프로젝트의 간트 차트 데이터 조회 (모든 관계 포함)
     */
    @Query("SELECT DISTINCT t FROM Task t " +
           "LEFT JOIN FETCH t.subTasks " +
           "LEFT JOIN FETCH t.predecessorDependencies pd " +
           "LEFT JOIN FETCH pd.predecessor " +
           "WHERE t.project.id = :projectId AND t.parentTask IS NULL " +
           "ORDER BY t.startDate")
    List<Task> findGanttChartData(@Param("projectId") Long projectId);
    
    /**
     * 진행률 범위로 태스크 조회
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId " +
           "AND t.progress >= :minProgress AND t.progress <= :maxProgress")
    List<Task> findByProgressRange(
        @Param("projectId") Long projectId,
        @Param("minProgress") BigDecimal minProgress,
        @Param("maxProgress") BigDecimal maxProgress
    );
    
    /**
     * 임계 경로 태스크 조회 (의존성이 있는 태스크들)
     */
    @Query("SELECT DISTINCT t FROM Task t " +
           "WHERE t.project.id = :projectId " +
           "AND (EXISTS (SELECT 1 FROM TaskDependency td WHERE td.predecessor = t) " +
           "OR EXISTS (SELECT 1 FROM TaskDependency td WHERE td.successor = t))")
    List<Task> findCriticalPathTasks(@Param("projectId") Long projectId);
    
    /**
     * 프로젝트별 태스크 진행률 업데이트
     */
    @Modifying
    @Query("UPDATE Task t SET t.progress = :progress, t.status = :status " +
           "WHERE t.id = :taskId")
    int updateTaskProgress(
        @Param("taskId") Long taskId,
        @Param("progress") BigDecimal progress,
        @Param("status") TaskStatus status
    );
    
    /**
     * 프로젝트의 전체 태스크 수 조회
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId")
    long countByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 프로젝트의 완료된 태스크 수 조회
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId " +
           "AND t.status = 'COMPLETED'")
    long countCompletedTasksByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 태스크 계층 구조 조회 (재귀 쿼리 시뮬레이션)
     */
    @Query(value = "WITH RECURSIVE task_hierarchy AS (" +
           "  SELECT t.*, 0 as level FROM tasks t " +
           "  WHERE t.project_id = :projectId AND t.parent_task_id IS NULL " +
           "  UNION ALL " +
           "  SELECT t.*, th.level + 1 FROM tasks t " +
           "  INNER JOIN task_hierarchy th ON t.parent_task_id = th.id " +
           ") SELECT * FROM task_hierarchy ORDER BY level, start_date",
           nativeQuery = true)
    List<Task> findTaskHierarchy(@Param("projectId") Long projectId);
}