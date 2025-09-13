# 🔍 Liam Gantt Chart Application 디버깅 리포트

**생성 일시**: 2025-09-12 23:29  
**애플리케이션 포트**: 8080  
**Spring Boot 버전**: 3.5.5  
**Java 버전**: 21

## 📊 페이지별 상태 점검 결과

### ✅ 정상 작동하는 페이지 (HTTP 200)

1. **홈페이지**
   - **URL**: `/` 또는 `http://localhost:8080/`
   - **상태**: ✅ 200 OK
   - **컨트롤러**: `HomeController`
   - **템플릿**: `home.html`
   - **기능**: 대시보드, 프로젝트 통계, 최근 프로젝트 목록

2. **프로젝트 목록 페이지**
   - **URL**: `/web/projects`
   - **상태**: ✅ 200 OK
   - **컨트롤러**: `ProjectWebController.projectList()`
   - **템플릿**: `projects/list.html`
   - **기능**: 프로젝트 검색, 필터링, 페이징

3. **특정 프로젝트 간트차트 페이지**
   - **URL**: `/web/gantt/{projectId}` (예: `/web/gantt/1`)
   - **상태**: ✅ 200 OK
   - **컨트롤러**: `GanttWebController.ganttChart()`
   - **템플릿**: `gantt/chart.html`
   - **기능**: 특정 프로젝트의 간트차트 시각화

4. **REST API - 프로젝트 목록**
   - **URL**: `/api/v1/projects`
   - **상태**: ✅ 200 OK
   - **컨트롤러**: REST API 컨트롤러
   - **응답**: JSON 형식 프로젝트 데이터

### ❌ 문제가 있는 페이지 (HTTP 500)

1. **프로젝트 상세 페이지**
   - **URL**: `/web/projects/{id}` (예: `/web/projects/1`)
   - **상태**: ❌ 500 Internal Server Error
   - **컨트롤러**: `ProjectWebController.projectDetail()`
   - **템플릿**: `projects/detail.html` ✅ (생성됨)
   - **추정 원인**: 
     - `ProjectService.findByIdWithTasks()` 메서드 실행 시 에러
     - `ProjectMapper`에서 `TaskMapper` 의존성 문제
     - tasks 필드 매핑 과정에서 순환 의존성 또는 null pointer exception

2. **간트차트 프로젝트 선택 페이지**
   - **URL**: `/web/gantt` (프로젝트 ID 없이 호출)
   - **상태**: ❌ 500 Internal Server Error
   - **컨트롤러**: `GanttWebController` - `@GetMapping` 메서드 **누락**
   - **템플릿**: `gantt/list.html` ✅ (존재함)
   - **추정 원인**: 
     - `/web/gantt` 경로에 대한 매핑 메서드가 없음
     - `GanttWebController`에는 `@GetMapping("/{projectId}")`만 존재
     - 프로젝트 목록을 보여주는 `@GetMapping` 메서드 필요

## 🗂️ 템플릿 파일 현황

### ✅ 존재하는 템플릿
- `src/main/resources/templates/home.html` - 홈페이지
- `src/main/resources/templates/projects/list.html` - 프로젝트 목록
- `src/main/resources/templates/projects/form.html` - 프로젝트 생성/수정 폼
- `src/main/resources/templates/projects/detail.html` - 프로젝트 상세 (✅ 신규 생성됨)
- `src/main/resources/templates/gantt/list.html` - 간트차트 프로젝트 선택
- `src/main/resources/templates/gantt/chart.html` - 간트차트 시각화
- `src/main/resources/templates/layout/base.html` - 공통 레이아웃

### 🔧 컨트롤러 메서드 현황

#### HomeController
- ✅ `@GetMapping("/")` - 홈페이지

#### ProjectWebController
- ✅ `@GetMapping` - 프로젝트 목록 (정상 작동)
- ❌ `@GetMapping("/{id}")` - 프로젝트 상세 (500 에러)
- ✅ `@GetMapping("/new")` - 새 프로젝트 폼
- ✅ `@PostMapping` - 프로젝트 생성
- ✅ `@GetMapping("/{id}/edit")` - 프로젝트 수정 폼
- ✅ `@PostMapping("/{id}")` - 프로젝트 수정
- ✅ `@PostMapping("/{id}/delete")` - 프로젝트 삭제

#### GanttWebController  
- ❌ **누락**: `@GetMapping` - 간트차트 프로젝트 목록 (필요함)
- ✅ `@GetMapping("/{projectId}")` - 특정 프로젝트 간트차트 (정상 작동)
- ✅ `@GetMapping("/{projectId}/data")` - 간트차트 데이터 API (정상 작동)

## 🧩 데이터베이스 현황

### 샘플 데이터
- **프로젝트**: 4개 (ID: 1-4)
  1. 웹사이트 리뉴얼 프로젝트 (진행중)
  2. 모바일 앱 개발 (계획)
  3. 데이터 마이그레이션 (진행중) 
  4. 사용자 교육 프로그램 (계획)
- **태스크**: 각 프로젝트별로 여러 개 존재
- **의존성**: 태스크 간 의존성 관계 설정됨

### REST API 테스트 결과
- ✅ `GET /api/v1/projects` - 프로젝트 목록 조회 성공
- ✅ `GET /api/v1/projects/1` - 개별 프로젝트 조회 성공  
- ❌ `GET /api/v1/projects/1/with-tasks` - 태스크 포함 조회 시 에러

## 🚨 주요 문제 및 해결 방안

### 1. `/web/gantt` 페이지 500 에러
**문제**: GanttWebController에 `@GetMapping` 메서드 누락  
**해결 방안**:
```java
@GetMapping
public String ganttList(Model model) {
    List<ProjectResponseDto> projects = projectService.findAll();
    model.addAttribute("projects", projects);
    return "gantt/list";
}
```

### 2. `/web/projects/{id}` 페이지 500 에러  
**문제**: `ProjectService.findByIdWithTasks()` 실행 시 매핑 에러  
**해결 방안**:
- ProjectMapper에서 TaskMapper 순환 의존성 해결
- 별도의 `toResponseDtoWithTasks()` 메서드 구현
- 또는 `findById()` 사용 후 별도로 태스크 조회

### 3. TaskMapper 순환 의존성
**문제**: ProjectMapper ↔ TaskMapper 간 순환 참조  
**해결 방안**: 
- 지연 초기화 (`@Lazy`) 사용
- 매핑 로직 분리
- 별도의 헬퍼 클래스 생성

## 🎯 우선순위별 수정 사항

### 🔥 즉시 수정 필요 (Critical)
1. **GanttWebController에 `@GetMapping` 메서드 추가** - `/web/gantt` 페이지 활성화
2. **ProjectWebController 프로젝트 상세 페이지 에러 수정** - 500 에러 해결

### 🔶 중간 우선순위 (Medium)  
1. TaskMapper 순환 의존성 해결
2. 에러 페이지 템플릿 개선
3. 로깅 레벨 조정으로 디버깅 정보 수집

### 🔵 낮은 우선순위 (Low)
1. UI/UX 개선
2. 성능 최적화  
3. 테스트 코드 보강

## 📈 전체 애플리케이션 상태

**전체 기능 중 작동률**: 약 75%
- ✅ 홈페이지: 100%
- ✅ 프로젝트 목록: 100%
- ❌ 프로젝트 상세: 0% (500 에러)
- ✅ 개별 간트차트: 100% 
- ❌ 간트차트 목록: 0% (500 에러)
- ✅ REST API: 80% (일부 엔드포인트 에러)

**핵심 기능 동작 여부**:
- ✅ 프로젝트 조회/목록
- ✅ 개별 간트차트 표시
- ❌ 프로젝트 상세 정보
- ❌ 간트차트 프로젝트 선택

**결론**: 기본적인 애플리케이션 구조와 대부분의 기능은 정상 작동하며, 2개의 주요 엔드포인트 수정으로 완전한 동작이 가능할 것으로 판단됩니다.