# Controller Layer Guidelines

Spring Boot REST API 및 Web Controller 개발 가이드

## 🎯 Controller 역할
- HTTP 요청/응답 처리
- 입력값 검증 (Bean Validation)
- 비즈니스 로직은 Service에 위임
- 적절한 HTTP 상태 코드 반환

## 📋 Coding Standards
### REST Controller 기본 구조
```java
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ProjectController {

    private final ProjectService projectService;
    
    @GetMapping
    public ResponseEntity<List<ProjectResponseDto>> getAllProjects() {
        List<ProjectResponseDto> projects = projectService.findAll();
        return ResponseEntity.ok(projects);
    }
    
    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(
            @Valid @RequestBody ProjectRequestDto request) {
        ProjectResponseDto created = projectService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
```

### Web Controller (Thymeleaf) 구조
```java
@Controller
@RequestMapping("/web/projects")
@RequiredArgsConstructor
public class ProjectWebController {

    private final ProjectService projectService;
    
    @GetMapping
    public String projectList(Model model) {
        model.addAttribute("projects", projectService.findAll());
        return "projects/list"; // templates/projects/list.html
    }
    
    @GetMapping("/new")
    public String newProjectForm(Model model) {
        model.addAttribute("project", new ProjectRequestDto());
        return "projects/form";
    }
}
```

## 🔧 필수 Annotations
- `@RestController` - REST API용
- `@Controller` - 웹 페이지용 (Thymeleaf)
- `@RequestMapping` - 기본 경로 설정
- `@RequiredArgsConstructor` - 의존성 주입 (Lombok)
- `@Validated` - 클래스 레벨 검증
- `@Slf4j` - 로깅 (Lombok)

## 📝 HTTP Method Mapping
```java
@GetMapping           // 조회 (READ)
@PostMapping          // 생성 (CREATE)  
@PutMapping           // 전체 수정 (UPDATE)
@PatchMapping         // 부분 수정 (PARTIAL UPDATE)
@DeleteMapping        // 삭제 (DELETE)
```

## ✅ 입력값 검증
### Request DTO 검증
```java
@PostMapping
public ResponseEntity<ProjectResponseDto> createProject(
        @Valid @RequestBody ProjectRequestDto request) {
    // @Valid가 자동으로 DTO 필드 검증 수행
    // 검증 실패 시 MethodArgumentNotValidException 발생
}
```

### Path Variable 검증
```java
@GetMapping("/{id}")
public ResponseEntity<ProjectResponseDto> getProject(
        @PathVariable @Positive Long id) {
    // @Positive로 양수 검증
}
```

## 📊 HTTP Status Code 가이드
```java
// 성공 응답
return ResponseEntity.ok(data);                    // 200 OK
return ResponseEntity.status(HttpStatus.CREATED)   // 201 Created
        .body(createdData);
return ResponseEntity.noContent().build();        // 204 No Content

// 클라이언트 오류
throw new IllegalArgumentException("Bad Request"); // 400 Bad Request
throw new EntityNotFoundException("Not Found");    // 404 Not Found
throw new DuplicateKeyException("Conflict");      // 409 Conflict

// 서버 오류는 GlobalExceptionHandler에서 처리
```

## 🔍 응답 데이터 구조
### 성공 응답
```java
// 단일 객체
public ResponseEntity<ProjectResponseDto> getProject(@PathVariable Long id)

// 리스트
public ResponseEntity<List<ProjectResponseDto>> getAllProjects()

// 페이징
public ResponseEntity<Page<ProjectResponseDto>> getProjects(Pageable pageable)
```

### 통일된 응답 래퍼 (선택사항)
```java
public class ApiResponse<T> {
    private String status;
    private T data;
    private String message;
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data, null);
    }
}
```

## 🚨 Exception Handling
Controller에서는 비즈니스 예외를 throw하고, GlobalExceptionHandler에서 처리:

```java
@PostMapping
public ResponseEntity<ProjectResponseDto> createProject(@Valid @RequestBody ProjectRequestDto request) {
    try {
        ProjectResponseDto created = projectService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    } catch (DuplicateProjectException e) {
        // GlobalExceptionHandler가 자동으로 처리
        throw e; 
    }
}
```

## 📋 로깅 가이드
```java
@Slf4j
public class ProjectController {
    
    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(@Valid @RequestBody ProjectRequestDto request) {
        log.info("프로젝트 생성 요청: {}", request.getName());
        
        ProjectResponseDto created = projectService.create(request);
        
        log.info("프로젝트 생성 완료: id={}, name={}", created.getId(), created.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
```

## 🌐 URL 패턴 가이드
### REST API 패턴
```
GET    /api/v1/projects           # 프로젝트 목록
POST   /api/v1/projects           # 프로젝트 생성
GET    /api/v1/projects/{id}      # 프로젝트 상세
PUT    /api/v1/projects/{id}      # 프로젝트 수정
DELETE /api/v1/projects/{id}      # 프로젝트 삭제

# 중첩 리소스
GET    /api/v1/projects/{id}/tasks    # 프로젝트의 태스크 목록
POST   /api/v1/projects/{id}/tasks    # 프로젝트에 태스크 추가
```

### Web Controller 패턴
```
GET    /web/projects              # 프로젝트 목록 페이지
GET    /web/projects/new          # 프로젝트 생성 폼
POST   /web/projects              # 프로젝트 생성 처리
GET    /web/projects/{id}         # 프로젝트 상세 페이지
GET    /web/projects/{id}/edit    # 프로젝트 수정 폼
POST   /web/projects/{id}/edit    # 프로젝트 수정 처리
```

## 🎨 Thymeleaf 연동 (Phase 1)
```java
@Controller
@RequestMapping("/web/gantt")
public class GanttWebController {
    
    @GetMapping("/{projectId}")
    public String ganttChart(@PathVariable Long projectId, Model model) {
        // 간트 차트 데이터 조회
        GanttChartDto ganttData = ganttService.getGanttChart(projectId);
        
        // Thymeleaf 템플릿에 전달
        model.addAttribute("ganttData", ganttData);
        model.addAttribute("projectId", projectId);
        
        return "gantt/chart"; // templates/gantt/chart.html
    }
}
```

## ⚠️ 주의사항
1. **Controller에 비즈니스 로직 금지** - Service에 위임
2. **적절한 HTTP 상태 코드 사용**
3. **입력값 검증 필수** - `@Valid` 사용
4. **일관된 응답 구조 유지**
5. **로깅으로 요청/응답 추적**
6. **예외는 GlobalExceptionHandler에 위임**

## 📚 참고 자료
- Spring Boot REST API Best Practices
- HTTP Status Code 가이드
- Bean Validation 사용법
- Thymeleaf 템플릿 엔진