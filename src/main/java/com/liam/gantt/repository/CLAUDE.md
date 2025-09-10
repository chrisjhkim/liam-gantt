# Repository Layer Guidelines

데이터 액세스를 담당하는 Repository 계층 개발 가이드

## 🎯 Repository 역할
- 데이터베이스 액세스 추상화
- CRUD 기본 기능 제공
- 복잡한 쿼리 작성 (JPQL, Native SQL)
- 트랜잭션과 영속성 컨텍스트 관리
- 데이터 일관성 보장

## 📋 Coding Standards
### 기본 Repository 인터페이스
```java
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    // 기본 CRUD는 JpaRepository에서 제공
    // findById, save, delete, findAll 등
    
    // 커스텀 쿼리 메서드
    List<Project> findByNameContaining(String name);
    
    Optional<Project> findByName(String name);
    
    boolean existsByName(String name);
    
    List<Project> findByStatusAndStartDateBetween(
            ProjectStatus status, 
            LocalDate startDate, 
            LocalDate endDate
    );
    
    @Query("SELECT p FROM Project p WHERE p.endDate < :date")
    List<Project> findOverdueProjects(@Param("date") LocalDate date);
}
```

## 🔍 Query Method 명명 규칙
### Spring Data JPA 메서드명 규칙
```java
// 기본 패턴: findBy + 필드명 + 조건
findByName(String name)                    // WHERE name = ?
findByNameContaining(String name)          // WHERE name LIKE %?%
findByNameStartingWith(String prefix)      // WHERE name LIKE ?%
findByNameEndingWith(String suffix)        // WHERE name LIKE %?

// 복합 조건
findByNameAndStatus(String name, ProjectStatus status)  // WHERE name = ? AND status = ?
findByNameOrDescription(String name, String desc)       // WHERE name = ? OR description = ?

// 정렬
findByStatusOrderByStartDateAsc(ProjectStatus status)
findByNameContainingOrderByNameDesc(String name)

// 날짜 비교
findByStartDateBefore(LocalDate date)      // WHERE start_date < ?
findByStartDateAfter(LocalDate date)       // WHERE start_date > ?
findByStartDateBetween(LocalDate start, LocalDate end)  // WHERE start_date BETWEEN ? AND ?

// Null 체크
findByDescriptionIsNull()                  // WHERE description IS NULL
findByDescriptionIsNotNull()               // WHERE description IS NOT NULL

// 컬렉션
findByTasksIsEmpty()                       // LEFT JOIN tasks WHERE tasks.id IS NULL
```

## 📝 JPQL 쿼리 작성
### @Query 애노테이션 사용
```java
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    // 간단한 JPQL
    @Query("SELECT p FROM Project p WHERE p.name = :name")
    Optional<Project> findByProjectName(@Param("name") String name);
    
    // 조인 쿼리
    @Query("SELECT p FROM Project p JOIN FETCH p.tasks WHERE p.id = :id")
    Optional<Project> findByIdWithTasks(@Param("id") Long id);
    
    // 집계 함수
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status")
    long countByStatus(@Param("status") ProjectStatus status);
    
    // 복잡한 조건문
    @Query("""
        SELECT p FROM Project p 
        WHERE (:name IS NULL OR p.name LIKE %:name%) 
        AND (:status IS NULL OR p.status = :status)
        AND p.startDate >= :fromDate
        """)
    List<Project> findProjectsByCriteria(
        @Param("name") String name,
        @Param("status") ProjectStatus status,
        @Param("fromDate") LocalDate fromDate
    );
    
    // DTO 프로젝션
    @Query("""
        SELECT new com.liam.gantt.dto.ProjectSummaryDto(
            p.id, p.name, p.status, COUNT(t)
        )
        FROM Project p LEFT JOIN p.tasks t 
        GROUP BY p.id, p.name, p.status
        """)
    List<ProjectSummaryDto> findProjectSummaries();
}
```

## 🔧 Native SQL 쿼리
### 복잡한 쿼리나 데이터베이스 특화 기능 사용시
```java
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // Native SQL with pagination
    @Query(value = """
        SELECT t.* FROM tasks t 
        JOIN projects p ON t.project_id = p.id 
        WHERE p.status = :status 
        AND t.start_date BETWEEN :startDate AND :endDate
        ORDER BY t.start_date
        """, 
        countQuery = """
        SELECT COUNT(t.id) FROM tasks t 
        JOIN projects p ON t.project_id = p.id 
        WHERE p.status = :status 
        AND t.start_date BETWEEN :startDate AND :endDate
        """,
        nativeQuery = true)
    Page<Task> findTasksByProjectStatusAndDateRange(
        @Param("status") String status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );
    
    // 복잡한 통계 쿼리
    @Query(value = """
        SELECT 
            p.name as project_name,
            COUNT(t.id) as task_count,
            AVG(t.progress) as avg_progress,
            SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_tasks
        FROM projects p 
        LEFT JOIN tasks t ON p.id = t.project_id 
        GROUP BY p.id, p.name
        ORDER BY avg_progress DESC
        """, nativeQuery = true)
    List<Object[]> getProjectStatistics();
}
```

## 🔄 Custom Repository 구현
### 복잡한 동적 쿼리를 위한 커스텀 구현
```java
// 1. 커스텀 인터페이스 정의
public interface ProjectRepositoryCustom {
    List<Project> findByCriteria(ProjectSearchCriteria criteria);
    Page<Project> findByComplexCriteria(ProjectSearchCriteria criteria, Pageable pageable);
}

// 2. 커스텀 구현 클래스 (접미사 Impl 필수)
@Repository
@RequiredArgsConstructor
public class ProjectRepositoryImpl implements ProjectRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public List<Project> findByCriteria(ProjectSearchCriteria criteria) {
        QProject project = QProject.project;
        QTask task = QTask.task;
        
        BooleanBuilder builder = new BooleanBuilder();
        
        // 동적 조건 추가
        if (StringUtils.hasText(criteria.getName())) {
            builder.and(project.name.containsIgnoreCase(criteria.getName()));
        }
        
        if (criteria.getStatus() != null) {
            builder.and(project.status.eq(criteria.getStatus()));
        }
        
        if (criteria.getStartDate() != null) {
            builder.and(project.startDate.goe(criteria.getStartDate()));
        }
        
        if (criteria.getEndDate() != null) {
            builder.and(project.endDate.loe(criteria.getEndDate()));
        }
        
        return queryFactory
            .selectFrom(project)
            .leftJoin(project.tasks, task).fetchJoin()
            .where(builder)
            .orderBy(project.startDate.desc())
            .fetch();
    }
}

// 3. 메인 Repository에서 상속
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, ProjectRepositoryCustom {
    // 기본 메서드들과 커스텀 메서드들을 함께 사용 가능
}
```

## 📊 Specifications 패턴
### JPA Criteria API를 사용한 동적 쿼리
```java
// 1. Specification 클래스 작성
public class ProjectSpecifications {
    
    public static Specification<Project> hasName(String name) {
        return (root, query, criteriaBuilder) -> 
            name == null ? null : criteriaBuilder.like(
                criteriaBuilder.lower(root.get("name")), 
                "%" + name.toLowerCase() + "%"
            );
    }
    
    public static Specification<Project> hasStatus(ProjectStatus status) {
        return (root, query, criteriaBuilder) -> 
            status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }
    
    public static Specification<Project> startDateBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) return null;
            
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get("startDate"), startDate, endDate);
            } else if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), startDate);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), endDate);
            }
        };
    }
}

// 2. Repository에서 JpaSpecificationExecutor 상속
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    // 기본 메서드들
}

// 3. Service에서 Specification 조합 사용
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    
    public Page<ProjectResponseDto> searchProjects(ProjectSearchCriteria criteria, Pageable pageable) {
        Specification<Project> spec = Specification.where(null);
        
        spec = spec.and(ProjectSpecifications.hasName(criteria.getName()));
        spec = spec.and(ProjectSpecifications.hasStatus(criteria.getStatus()));
        spec = spec.and(ProjectSpecifications.startDateBetween(criteria.getStartDate(), criteria.getEndDate()));
        
        Page<Project> projects = projectRepository.findAll(spec, pageable);
        return projects.map(projectMapper::toResponseDto);
    }
}
```

## 🚀 성능 최적화
### N+1 문제 해결
```java
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    // Fetch Join 사용
    @Query("SELECT p FROM Project p JOIN FETCH p.tasks WHERE p.id = :id")
    Optional<Project> findByIdWithTasks(@Param("id") Long id);
    
    // EntityGraph 사용
    @EntityGraph(attributePaths = {"tasks", "tasks.dependencies"})
    @Query("SELECT p FROM Project p WHERE p.id = :id")
    Optional<Project> findByIdWithTasksAndDependencies(@Param("id") Long id);
    
    // 배치 조회
    @Query("SELECT p FROM Project p JOIN FETCH p.tasks WHERE p.id IN :ids")
    List<Project> findByIdsWithTasks(@Param("ids") List<Long> ids);
}
```

### 페이징과 정렬 최적화
```java
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // 커버링 인덱스 활용
    @Query(value = """
        SELECT t.id, t.name, t.start_date, t.end_date, t.status 
        FROM tasks t 
        WHERE t.project_id = :projectId 
        ORDER BY t.start_date
        """, nativeQuery = true)
    Page<TaskProjection> findTasksByProjectIdOptimized(
        @Param("projectId") Long projectId, 
        Pageable pageable
    );
    
    // Count 쿼리 최적화
    @Query(value = "SELECT t FROM Task t WHERE t.project.id = :projectId",
           countQuery = "SELECT COUNT(t.id) FROM Task t WHERE t.project.id = :projectId")
    Page<Task> findByProjectId(@Param("projectId") Long projectId, Pageable pageable);
}
```

## 🗄️ 배치 처리
### 대량 데이터 처리를 위한 배치 쿼리
```java
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // 배치 업데이트
    @Modifying
    @Query("UPDATE Task t SET t.status = :newStatus WHERE t.project.id = :projectId")
    int updateTaskStatusByProjectId(@Param("projectId") Long projectId, @Param("newStatus") TaskStatus newStatus);
    
    // 배치 삭제
    @Modifying
    @Query("DELETE FROM Task t WHERE t.project.id = :projectId")
    int deleteTasksByProjectId(@Param("projectId") Long projectId);
    
    // 조건부 배치 처리
    @Modifying
    @Query("""
        UPDATE Task t SET t.progress = 100, t.status = 'COMPLETED' 
        WHERE t.endDate < :date AND t.status != 'COMPLETED'
        """)
    int markOverdueTasksAsCompleted(@Param("date") LocalDate date);
}
```

## 🧪 Repository 테스트
### @DataJpaTest를 사용한 테스트
```java
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class ProjectRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Test
    @DisplayName("프로젝트명으로 조회 테스트")
    void findByName_Success() {
        // Given
        Project project = Project.builder()
                .name("테스트 프로젝트")
                .description("테스트용 프로젝트입니다")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status(ProjectStatus.IN_PROGRESS)
                .build();
        
        entityManager.persistAndFlush(project);
        
        // When
        Optional<Project> found = projectRepository.findByName("테스트 프로젝트");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("테스트 프로젝트");
        assertThat(found.get().getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
    }
    
    @Test
    @DisplayName("커스텀 쿼리 테스트")
    void findOverdueProjects_Success() {
        // Given
        Project overdueProject = Project.builder()
                .name("지연된 프로젝트")
                .startDate(LocalDate.now().minusDays(60))
                .endDate(LocalDate.now().minusDays(1))
                .status(ProjectStatus.IN_PROGRESS)
                .build();
        
        entityManager.persistAndFlush(overdueProject);
        
        // When
        List<Project> overdueProjects = projectRepository.findOverdueProjects(LocalDate.now());
        
        // Then
        assertThat(overdueProjects).hasSize(1);
        assertThat(overdueProjects.get(0).getName()).isEqualTo("지연된 프로젝트");
    }
}
```

## ⚠️ 주의사항
1. **@Modifying 쿼리는 @Transactional과 함께 사용**
2. **N+1 문제 방지를 위한 적극적인 Fetch Join 활용**
3. **페이징 쿼리에서 Count 쿼리 최적화 고려**
4. **Native SQL 사용 시 데이터베이스 종속성 주의**
5. **배치 처리 시 메모리 사용량 모니터링**
6. **복잡한 쿼리는 QueryDSL이나 Specifications 활용**

## 📚 참고 자료
- Spring Data JPA Reference Documentation
- QueryDSL Reference Guide  
- JPA Performance Tuning
- Database Index Design Principles