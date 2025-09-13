# Project Views Specification
> 프로젝트 관련 View 명세서

## 📋 Overview
프로젝트 CRUD 기능을 담당하는 View들의 명세입니다.

---

## projects/list.html
> 프로젝트 목록 페이지

### Controller Mapping
- **URL**: `/web/projects`
- **Method**: `GET`
- **Controller**: `ProjectWebController#projectList`
- **Description**: 프로젝트 목록을 페이징하여 표시

### Required Model Attributes

| Attribute Name | Type | Required | Default | Description |
|---------------|------|----------|---------|-------------|
| projects | `Page<ProjectResponseDto>` | Yes | - | 프로젝트 페이징 데이터 |
| pageTitle | `String` | Yes | "프로젝트 관리" | 페이지 제목 |
| pageIcon | `String` | Yes | "fas fa-project-diagram" | 페이지 아이콘 |

#### ProjectResponseDto Fields Used
- `id` - 프로젝트 ID
- `name` - 프로젝트명
- `description` - 설명
- `startDate` - 시작일
- `endDate` - 종료일
- `status` - 상태 (enum)
- `averageProgress` - 평균 진행률 (nullable)
- `taskCount` - 태스크 수 (nullable)

#### Page Object Properties Used
- `content` - 실제 프로젝트 리스트
- `hasContent()` - 컨텐츠 존재 여부
- `totalPages` - 전체 페이지 수
- `number` - 현재 페이지 번호 (0-based)
- `size` - 페이지 크기
- `first` - 첫 페이지 여부
- `last` - 마지막 페이지 여부

### Page Elements

#### Links & Navigation
- **새 프로젝트**: `@{/web/projects/new}` → 프로젝트 생성 폼
- **상세보기**: `@{/web/projects/{id}(id=${project.id})}` → 프로젝트 상세
- **간트차트**: `@{/web/projects/{id}/gantt(id=${project.id})}` → 간트차트
- **수정**: `@{/web/projects/{id}/edit(id=${project.id})}` → 프로젝트 수정
- **페이징**: `@{/web/projects(page=${i}, size=${projects.size})}` → 페이지 이동

#### Forms & Actions
- **검색 폼**: `GET /web/projects`
  - `search` - 프로젝트명 검색어
  - `status` - 상태 필터
  - `sort` - 정렬 기준
- **삭제 폼**: `POST /web/projects/{id}` (Modal)
  - `_method=delete` - HTTP DELETE 시뮬레이션

#### Conditional Elements
- 프로젝트 없을 때: `${projects == null or !projects.hasContent()}`
- 페이징 표시: `${projects != null and projects.totalPages > 1}`
- 상태별 배지 색상: `${project.status.name()}`에 따라 분기

### Dependencies
- **Fragment**: `layout/base`
- **JavaScript**: Bootstrap Modal (삭제 확인)
- **CSS**: Bootstrap 5

---

## projects/detail.html
> 프로젝트 상세 페이지

### Controller Mapping
- **URL**: `/web/projects/{id}`
- **Method**: `GET`
- **Controller**: `ProjectWebController#projectDetail`
- **Description**: 프로젝트 상세 정보와 태스크 목록 표시

### Required Model Attributes

| Attribute Name | Type | Required | Default | Description |
|---------------|------|----------|---------|-------------|
| project | `ProjectResponseDto` | Yes | - | 프로젝트 정보 |
| tasks | `Page<TaskResponseDto>` | Yes | - | 태스크 목록 (미리보기) |
| completedTaskCount | `Long` | Yes | 0 | 완료된 태스크 수 |
| inProgressTaskCount | `Long` | Yes | 0 | 진행중 태스크 수 |
| notStartedTaskCount | `Long` | Yes | 0 | 시작전 태스크 수 |
| elapsedDays | `Long` | Yes | 0 | 경과 일수 |
| remainingDays | `Long` | Yes | - | 남은 일수 |
| pageTitle | `String` | Yes | project.name | 페이지 제목 |
| pageIcon | `String` | Yes | "fas fa-project-diagram" | 페이지 아이콘 |

#### ProjectResponseDto Fields Used
- 모든 필드 + `durationInDays` (프로젝트 기간)

#### TaskResponseDto Fields Used
- `id`, `name`, `status`, `progress`, `startDate`, `endDate`

### Page Elements

#### Links & Navigation
- **프로젝트 수정**: `@{/web/projects/{id}/edit(id=${project.id})}`
- **간트차트 보기**: `@{/web/projects/{id}/gantt(id=${project.id})}`
- **태스크 추가**: `@{/web/projects/{id}/tasks/new(id=${project.id})}`
- **태스크 상세**: `@{/web/tasks/{id}(id=${task.id})}`
- **전체 태스크**: `@{/web/projects/{id}/tasks(id=${project.id})}`
- **목록으로**: `@{/web/projects}`

#### Forms & Actions
- **프로젝트 삭제**: `POST /web/projects/{id}/delete`
- **태스크 삭제**: Modal 통한 삭제 (JavaScript)

#### Conditional Elements
- 태스크 없음 표시: `${tasks.content.isEmpty()}`
- 진행률 표시: `${project.durationInDays ?: 0}` null 체크
- 상태별 색상: `${project.status.name()}`에 따라 분기

### Dependencies
- **Fragment**: `layout/base`
- **JavaScript**: Chart.js (진행률 차트), Bootstrap Modal
- **CSS**: Bootstrap 5

---

## projects/form.html
> 프로젝트 생성/수정 폼

### Controller Mapping

#### 생성 폼
- **URL**: `/web/projects/new`
- **Method**: `GET`
- **Controller**: `ProjectWebController#newProjectForm`

#### 수정 폼
- **URL**: `/web/projects/{id}/edit`
- **Method**: `GET`
- **Controller**: `ProjectWebController#editProjectForm`

#### 생성 처리
- **URL**: `/web/projects`
- **Method**: `POST`
- **Controller**: `ProjectWebController#createProject`

#### 수정 처리
- **URL**: `/web/projects/{id}`
- **Method**: `POST`
- **Controller**: `ProjectWebController#updateProject`

### Required Model Attributes

| Attribute Name | Type | Required | Default | Description |
|---------------|------|----------|---------|-------------|
| project | `ProjectRequestDto` | Yes | - | 프로젝트 폼 데이터 |
| projectId | `Long` | No | null | 수정 시 프로젝트 ID |
| pageTitle | `String` | Yes | "새 프로젝트" or "프로젝트 수정" | 페이지 제목 |
| pageIcon | `String` | Yes | "fas fa-plus" or "fas fa-edit" | 페이지 아이콘 |
| errorMessage | `String` | No | - | 오류 메시지 |

#### ProjectRequestDto Fields
- `name` - 프로젝트명 (required, max=200)
- `description` - 설명 (optional)
- `startDate` - 시작일 (required)
- `endDate` - 종료일 (required)
- `status` - 상태 (required, enum)

### Page Elements

#### Forms & Actions
- **프로젝트 폼**:
  - 생성: `POST @{/web/projects}`
  - 수정: `POST @{/web/projects/{id}(id=${projectId})}`
- **유효성 검증**: Bean Validation (`@Valid`)
  - Field errors: `th:errors="*{fieldName}"`

#### Conditional Elements
- 수정 모드: `${projectId != null}`
- 에러 표시: `${#fields.hasErrors('fieldName')}`
- 에러 메시지: `${errorMessage != null}`

### Dependencies
- **Fragment**: `layout/base`
- **JavaScript**: 날짜 유효성 검증
- **CSS**: Bootstrap 5 Forms

---

## 🔍 공통 이슈 및 해결

### 발견된 문제점
1. **averageProgress null 처리**: `${project.averageProgress ?: 0}`
2. **durationInDays 누락**: ProjectMapper에서 계산 추가 필요
3. **taskCount null 처리**: `${project.taskCount ?: 0}`
4. **Page 객체 접근**: `${projects}` → `${projects.content}`

### Thymeleaf 안전 처리
```html
<!-- Null-safe 연산자 사용 -->
${value ?: defaultValue}

<!-- 조건부 렌더링 -->
th:if="${condition}"

<!-- Safe navigation -->
${object?.property}
```

## 📊 Request Parameters

### 목록 페이지 파라미터
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| search | String | No | 검색어 |
| status | String | No | 상태 필터 |
| sort | String | No | 정렬 기준 (예: "startDate,desc") |
| page | Integer | No | 페이지 번호 (0-based) |
| size | Integer | No | 페이지 크기 (기본 10) |

## 🔗 Related Controllers
- `ProjectWebController` - `/src/main/java/com/liam/gantt/controller/ProjectWebController.java`
- `ProjectService` - `/src/main/java/com/liam/gantt/service/ProjectService.java`