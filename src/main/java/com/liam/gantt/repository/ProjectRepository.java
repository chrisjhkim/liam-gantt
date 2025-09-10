package com.liam.gantt.repository;

import com.liam.gantt.entity.Project;
import com.liam.gantt.entity.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 프로젝트 엔티티에 대한 데이터 액세스 인터페이스
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    /**
     * 프로젝트명으로 조회
     */
    Optional<Project> findByName(String name);
    
    /**
     * 프로젝트명 중복 체크
     */
    boolean existsByName(String name);
    
    /**
     * 프로젝트명에 특정 문자열이 포함된 프로젝트 검색
     */
    List<Project> findByNameContainingIgnoreCase(String keyword);
    
    /**
     * 상태별 프로젝트 조회
     */
    List<Project> findByStatus(ProjectStatus status);
    
    /**
     * 상태별 프로젝트 페이징 조회
     */
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);
    
    /**
     * 날짜 범위로 프로젝트 조회
     */
    List<Project> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 상태와 날짜 범위로 프로젝트 조회
     */
    List<Project> findByStatusAndStartDateBetween(
        ProjectStatus status, 
        LocalDate startDate, 
        LocalDate endDate
    );
    
    /**
     * 종료일이 지난 미완료 프로젝트 조회 (지연된 프로젝트)
     */
    @Query("SELECT p FROM Project p WHERE p.endDate < :currentDate " +
           "AND p.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Project> findOverdueProjects(@Param("currentDate") LocalDate currentDate);
    
    /**
     * 프로젝트와 연관된 태스크들을 함께 조회 (N+1 문제 방지)
     */
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.tasks WHERE p.id = :id")
    Optional<Project> findByIdWithTasks(@Param("id") Long id);
    
    /**
     * 활성 프로젝트 조회 (진행 중 또는 계획 중)
     */
    @Query("SELECT p FROM Project p WHERE p.status IN ('PLANNING', 'IN_PROGRESS') " +
           "ORDER BY p.startDate ASC")
    List<Project> findActiveProjects();
    
    /**
     * 프로젝트별 태스크 수 조회
     */
    @Query("SELECT p, COUNT(t) FROM Project p LEFT JOIN p.tasks t " +
           "GROUP BY p ORDER BY COUNT(t) DESC")
    List<Object[]> findProjectsWithTaskCount();
    
    /**
     * 특정 기간 내에 시작하는 프로젝트 조회
     */
    @Query("SELECT p FROM Project p WHERE p.startDate >= :fromDate " +
           "AND p.startDate <= :toDate ORDER BY p.startDate")
    List<Project> findUpcomingProjects(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );
    
    /**
     * 프로젝트 상태별 개수 조회
     */
    @Query("SELECT p.status, COUNT(p) FROM Project p GROUP BY p.status")
    List<Object[]> countByStatus();
    
    /**
     * 특정 상태의 프로젝트 개수 조회
     */
    long countByStatus(ProjectStatus status);
    
    /**
     * 특정 ID를 제외한 프로젝트명 중복 체크 (수정 시 사용)
     */
    boolean existsByNameAndIdNot(String name, Long id);
    
    /**
     * 종료일 이전의 프로젝트 조회
     */
    List<Project> findByEndDateBefore(LocalDate date);
    
    /**
     * 특정 날짜에 활성상태인 프로젝트 조회
     */
    List<Project> findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
        LocalDate startDate, LocalDate endDate, ProjectStatus status);
    
    /**
     * 이름과 상태로 대소문자 무관하게 검색
     */
    List<Project> findByNameContainingIgnoreCaseAndStatus(String name, ProjectStatus status);
}