package com.liam.gantt.repository;

import com.liam.gantt.entity.TaskDependency;
import com.liam.gantt.entity.enums.DependencyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 태스크 의존성 엔티티에 대한 데이터 액세스 인터페이스
 */
@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {
    
    /**
     * 선행 태스크별 의존성 조회
     */
    List<TaskDependency> findByPredecessorId(Long predecessorId);
    
    /**
     * 후행 태스크별 의존성 조회
     */
    List<TaskDependency> findBySuccessorId(Long successorId);
    
    /**
     * 특정 태스크 쌍의 의존성 조회
     */
    Optional<TaskDependency> findByPredecessorIdAndSuccessorId(
        Long predecessorId, 
        Long successorId
    );
    
    /**
     * 의존성 유형별 조회
     */
    List<TaskDependency> findByDependencyType(DependencyType dependencyType);
    
    /**
     * 프로젝트의 모든 의존성 조회
     */
    @Query("SELECT td FROM TaskDependency td " +
           "JOIN td.predecessor p " +
           "WHERE p.project.id = :projectId")
    List<TaskDependency> findByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 특정 태스크와 관련된 모든 의존성 조회 (선행 + 후행)
     */
    @Query("SELECT td FROM TaskDependency td " +
           "WHERE td.predecessor.id = :taskId OR td.successor.id = :taskId")
    List<TaskDependency> findAllByTaskId(@Param("taskId") Long taskId);
    
    /**
     * 순환 의존성 체크를 위한 경로 조회
     * 특정 태스크에서 시작하여 도달 가능한 모든 후행 태스크들
     */
    @Query(value = "WITH RECURSIVE dependency_chain AS (" +
           "  SELECT successor_id, predecessor_id FROM task_dependencies " +
           "  WHERE predecessor_id = :taskId " +
           "  UNION ALL " +
           "  SELECT td.successor_id, td.predecessor_id FROM task_dependencies td " +
           "  INNER JOIN dependency_chain dc ON td.predecessor_id = dc.successor_id " +
           ") SELECT DISTINCT successor_id FROM dependency_chain",
           nativeQuery = true)
    List<Long> findReachableSuccessorIds(@Param("taskId") Long taskId);
    
    /**
     * 중복 의존성 체크
     */
    boolean existsByPredecessorIdAndSuccessorId(Long predecessorId, Long successorId);
    
    /**
     * 프로젝트의 의존성 개수 조회
     */
    @Query("SELECT COUNT(td) FROM TaskDependency td " +
           "JOIN td.predecessor p " +
           "WHERE p.project.id = :projectId")
    long countByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 태스크의 직접 선행 태스크들 조회
     */
    @Query("SELECT td.predecessor FROM TaskDependency td " +
           "WHERE td.successor.id = :taskId")
    List<Long> findDirectPredecessorIds(@Param("taskId") Long taskId);
    
    /**
     * 태스크의 직접 후행 태스크들 조회
     */
    @Query("SELECT td.successor FROM TaskDependency td " +
           "WHERE td.predecessor.id = :taskId")
    List<Long> findDirectSuccessorIds(@Param("taskId") Long taskId);
    
    /**
     * 의존성 체인의 최대 깊이 조회 (임계 경로 계산용)
     */
    @Query(value = "WITH RECURSIVE dependency_depth AS (" +
           "  SELECT predecessor_id, successor_id, 1 as depth " +
           "  FROM task_dependencies WHERE predecessor_id = :startTaskId " +
           "  UNION ALL " +
           "  SELECT td.predecessor_id, td.successor_id, dd.depth + 1 " +
           "  FROM task_dependencies td " +
           "  INNER JOIN dependency_depth dd ON td.predecessor_id = dd.successor_id " +
           "  WHERE dd.depth < 100 " +  // 무한 루프 방지
           ") SELECT MAX(depth) FROM dependency_depth",
           nativeQuery = true)
    Integer findMaxDependencyDepth(@Param("startTaskId") Long startTaskId);
    
    /**
     * 태스크 삭제 시 관련 의존성 삭제
     */
    void deleteByPredecessorIdOrSuccessorId(Long predecessorId, Long successorId);
}