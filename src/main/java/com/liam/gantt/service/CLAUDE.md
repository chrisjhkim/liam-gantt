# Service Layer Guidelines

비즈니스 로직을 담당하는 Service 계층 개발 가이드

## 🎯 Service 역할
- 비즈니스 로직 구현
- 트랜잭션 관리 (@Transactional)
- 데이터 검증 및 변환
- Repository 계층 호출
- 복잡한 도메인 규칙 처리

## 📋 Coding Standards
### Service 기본 구조
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    
    public List<ProjectResponseDto> findAll() {
        log.debug("모든 프로젝트 조회 시작");
        
        List<Project> projects = projectRepository.findAll();
        return projects.stream()
                .map(projectMapper::toResponseDto)
                .toList();
    }
    
    @Transactional
    public ProjectResponseDto create(ProjectRequestDto request) {
        log.info("프로젝트 생성 시작: {}", request.getName());
        
        // 비즈니스 규칙 검증
        validateProjectRequest(request);
        
        // Entity 생성 및 저장
        Project project = projectMapper.toEntity(request);
        Project savedProject = projectRepository.save(project);
        
        log.info("프로젝트 생성 완료: id={}", savedProject.getId());
        return projectMapper.toResponseDto(savedProject);
    }
    
    private void validateProjectRequest(ProjectRequestDto request) {
        // 비즈니스 규칙 검증 로직
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new InvalidProjectDateException("종료일은 시작일보다 늦어야 합니다.");
        }
        
        if (projectRepository.existsByName(request.getName())) {
            throw new DuplicateProjectNameException("이미 존재하는 프로젝트명입니다.");
        }
    }
}
```

## 🔧 필수 Annotations
- `@Service` - Spring Service Bean 등록
- `@RequiredArgsConstructor` - 의존성 주입 (Lombok)
- `@Transactional` - 트랜잭션 관리
- `@Slf4j` - 로깅 (Lombok)

## ⚡ 트랜잭션 관리
### 기본 설정
```java
@Service
@Transactional(readOnly = true) // 클래스 레벨에 읽기 전용 설정
public class ProjectService {
    
    // 읽기 전용 메서드들 (기본적으로 readOnly = true)
    public ProjectResponseDto findById(Long id) { ... }
    public List<ProjectResponseDto> findAll() { ... }
    
    // 쓰기 작업은 메서드 레벨에서 @Transactional 오버라이드
    @Transactional
    public ProjectResponseDto create(ProjectRequestDto request) { ... }
    
    @Transactional
    public ProjectResponseDto update(Long id, ProjectRequestDto request) { ... }
    
    @Transactional
    public void delete(Long id) { ... }
}
```

### 트랜잭션 전파 설정
```java
@Transactional(propagation = Propagation.REQUIRED)     // 기본값
@Transactional(propagation = Propagation.REQUIRES_NEW) // 새 트랜잭션
@Transactional(propagation = Propagation.SUPPORTS)     // 기존 트랜잭션 지원
```

## 🔍 비즈니스 로직 패턴
### CRUD 기본 패턴
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TaskMapper taskMapper;
    
    // 조회 (READ)
    public TaskResponseDto findById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("태스크를 찾을 수 없습니다: " + id));
        return taskMapper.toResponseDto(task);
    }
    
    // 생성 (CREATE)
    @Transactional
    public TaskResponseDto create(Long projectId, TaskRequestDto request) {
        // 1. 연관 엔티티 검증
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("프로젝트를 찾을 수 없습니다: " + projectId));
        
        // 2. 비즈니스 규칙 검증
        validateTaskRequest(request, project);
        
        // 3. 엔티티 생성 및 저장
        Task task = taskMapper.toEntity(request);
        task.setProject(project);
        Task savedTask = taskRepository.save(task);
        
        // 4. 후처리 로직 (필요시)
        updateProjectSchedule(project);
        
        return taskMapper.toResponseDto(savedTask);
    }
    
    // 수정 (UPDATE)
    @Transactional
    public TaskResponseDto update(Long id, TaskRequestDto request) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("태스크를 찾을 수 없습니다: " + id));
        
        // 비즈니스 규칙 검증
        validateTaskUpdate(existingTask, request);
        
        // 엔티티 업데이트
        taskMapper.updateEntity(existingTask, request);
        
        return taskMapper.toResponseDto(existingTask);
    }
    
    // 삭제 (DELETE)
    @Transactional
    public void delete(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("태스크를 찾을 수 없습니다: " + id));
        
        // 삭제 전 검증 (의존성 체크 등)
        validateTaskDeletion(task);
        
        taskRepository.delete(task);
        
        // 후처리 (프로젝트 일정 재계산 등)
        updateProjectSchedule(task.getProject());
    }
}
```

## 🧪 비즈니스 규칙 검증
### 검증 로직 분리
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProjectService {
    
    @Transactional
    public ProjectResponseDto create(ProjectRequestDto request) {
        // 검증 로직을 private 메서드로 분리
        validateProjectRequest(request);
        
        Project project = projectMapper.toEntity(request);
        Project savedProject = projectRepository.save(project);
        
        return projectMapper.toResponseDto(savedProject);
    }
    
    private void validateProjectRequest(ProjectRequestDto request) {
        // 날짜 검증
        validateProjectDates(request.getStartDate(), request.getEndDate());
        
        // 중복 검증
        validateProjectNameUniqueness(request.getName());
        
        // 비즈니스 규칙 검증
        validateBusinessRules(request);
    }
    
    private void validateProjectDates(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new InvalidProjectDateException(
                "프로젝트 종료일({})은 시작일({})보다 늦어야 합니다.", 
                endDate, startDate
            );
        }
        
        if (startDate.isBefore(LocalDate.now())) {
            throw new InvalidProjectDateException(
                "프로젝트 시작일은 과거일 수 없습니다: {}", startDate
            );
        }
    }
    
    private void validateProjectNameUniqueness(String name) {
        if (projectRepository.existsByName(name)) {
            throw new DuplicateProjectNameException(
                "이미 존재하는 프로젝트명입니다: {}", name
            );
        }
    }
}
```

## 🔄 Entity ↔ DTO 변환
### Mapper 패턴 사용
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper; // MapStruct 또는 ModelMapper
    
    public ProjectResponseDto findById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("프로젝트를 찾을 수 없습니다: " + id));
        
        // Mapper를 통한 변환
        return projectMapper.toResponseDto(project);
    }
    
    @Transactional
    public ProjectResponseDto create(ProjectRequestDto request) {
        // DTO -> Entity 변환
        Project project = projectMapper.toEntity(request);
        
        Project savedProject = projectRepository.save(project);
        
        // Entity -> DTO 변환
        return projectMapper.toResponseDto(savedProject);
    }
}
```

## 📊 페이징 및 정렬
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    
    public Page<ProjectResponseDto> findAll(Pageable pageable) {
        Page<Project> projects = projectRepository.findAll(pageable);
        
        return projects.map(projectMapper::toResponseDto);
    }
    
    public List<ProjectResponseDto> findByStatus(ProjectStatus status, Sort sort) {
        List<Project> projects = projectRepository.findByStatus(status, sort);
        
        return projects.stream()
                .map(projectMapper::toResponseDto)
                .toList();
    }
}
```

## 🔍 검색 및 필터링
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {
    
    public List<ProjectResponseDto> searchProjects(ProjectSearchCriteria criteria) {
        log.debug("프로젝트 검색: {}", criteria);
        
        // Repository에서 동적 쿼리 또는 Specification 사용
        List<Project> projects = projectRepository.findByCriteria(criteria);
        
        return projects.stream()
                .map(projectMapper::toResponseDto)
                .toList();
    }
}
```

## 🚨 예외 처리
### Service 계층에서 발생시킬 예외들
```java
// 비즈니스 예외들
throw new ProjectNotFoundException("프로젝트를 찾을 수 없습니다: " + id);
throw new DuplicateProjectNameException("이미 존재하는 프로젝트명입니다: " + name);
throw new InvalidProjectDateException("잘못된 프로젝트 날짜입니다");
throw new TaskDependencyException("순환 의존성이 감지되었습니다");

// 검증 예외들
throw new IllegalArgumentException("필수 파라미터가 누락되었습니다");
throw new IllegalStateException("현재 상태에서는 해당 작업을 수행할 수 없습니다");
```

## 📋 로깅 가이드
```java
@Slf4j
public class ProjectService {
    
    @Transactional
    public ProjectResponseDto create(ProjectRequestDto request) {
        log.info("프로젝트 생성 시작 - name: {}, startDate: {}", 
                request.getName(), request.getStartDate());
        
        try {
            // 비즈니스 로직 수행
            Project savedProject = projectRepository.save(project);
            
            log.info("프로젝트 생성 완료 - id: {}, name: {}", 
                    savedProject.getId(), savedProject.getName());
                    
            return projectMapper.toResponseDto(savedProject);
            
        } catch (Exception e) {
            log.error("프로젝트 생성 실패 - name: {}, error: {}", 
                     request.getName(), e.getMessage(), e);
            throw e;
        }
    }
}
```

## 🧩 복잡한 비즈니스 로직 예시
### 간트 차트 스케줄 계산
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GanttService {
    
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskDependencyRepository dependencyRepository;
    
    public GanttChartDto calculateSchedule(Long projectId) {
        log.debug("간트 차트 스케줄 계산 시작 - projectId: {}", projectId);
        
        // 1. 프로젝트와 태스크들 조회
        Project project = projectRepository.findByIdWithTasks(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("프로젝트를 찾을 수 없습니다: " + projectId));
        
        List<Task> tasks = project.getTasks();
        List<TaskDependency> dependencies = dependencyRepository.findByProjectId(projectId);
        
        // 2. 의존성 그래프 생성
        DependencyGraph dependencyGraph = buildDependencyGraph(tasks, dependencies);
        
        // 3. 임계 경로 계산
        List<Task> criticalPath = calculateCriticalPath(dependencyGraph);
        
        // 4. 각 태스크의 시작/종료 시간 계산
        Map<Long, TaskSchedule> taskSchedules = calculateTaskSchedules(dependencyGraph);
        
        // 5. 간트 차트 데이터 생성
        return GanttChartDto.builder()
                .project(projectMapper.toDto(project))
                .tasks(mapTasksWithSchedules(tasks, taskSchedules))
                .criticalPath(mapTasksToDto(criticalPath))
                .build();
    }
    
    private DependencyGraph buildDependencyGraph(List<Task> tasks, List<TaskDependency> dependencies) {
        // 복잡한 의존성 그래프 구축 로직
        // 순환 의존성 검사 포함
    }
    
    private List<Task> calculateCriticalPath(DependencyGraph graph) {
        // 임계 경로 계산 알고리즘 (CPM - Critical Path Method)
    }
}
```

## ⚠️ 주의사항
1. **비즈니스 로직만 처리** - HTTP 관련 로직은 Controller에
2. **트랜잭션 경계 명확히 설정**
3. **적절한 예외 발생으로 오류 상황 명시**
4. **로깅으로 비즈니스 흐름 추적**
5. **복잡한 로직은 private 메서드로 분리**
6. **Entity 직접 반환 금지** - 항상 DTO 변환

## 📚 참고 자료
- Spring Transaction Management
- 도메인 주도 설계 (DDD)
- Clean Architecture 원칙