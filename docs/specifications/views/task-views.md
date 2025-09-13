# Task Views Specification
> 태스크 관련 View 명세서

## 📋 Overview
태스크 CRUD 기능을 담당하는 View들의 명세입니다.

---

## tasks/list.html
> 프로젝트별 태스크 목록 페이지

### Controller Mapping
- **URL**: `/web/projects/{projectId}/tasks`
- **Method**: `GET`
- **Controller**: `TaskWebController#taskList`
- **Description**: 특정 프로젝트의 태스크 목록을 페이징하여 표시

### Required Model Attributes

| Attribute Name | Type | Required | Default | Description |
|---------------|------|----------|---------|-------------|
| project | `ProjectResponseDto` | Yes | - | 프로젝트 정보 |
| tasks | `Page<TaskResponseDto>` | Yes | - | 태스크 페이징 데이터 |
| pageTitle | `String` | Yes | "${project.name} - 태스크 관리" | 페이지 제목 |
| pageIcon | `String` | Yes | "fas fa-tasks" | 페이지 아이콘 |

#### TaskResponseDto Fields Used
- `id` - 태스크 ID
- `name` - 태스크명
- `description` - 설명
- `startDate` - 시작일
- `endDate` - 종료일
- `duration` - 기간
- `progress` - 진행률
- `status` - 상태 (enum)
- `parentTaskId` - 부모 태스크 ID (nullable)
- `level` - 계층 레벨 (WBS용)

#### Page Object Properties Used
- `content` - 실제 태스크 리스트
- `hasContent()` - 컨텐츠 존재 여부
- `totalElements` - 전체 태스크 수
- `totalPages` - 전체 페이지 수
- `number` - 현재 페이지 번호
- `size` - 페이지 크기
- `first` - 첫 페이지 여부
- `last` - 마지막 페이지 여부

### Page Elements

#### Links & Navigation
- **새 태스크**: `@{/web/projects/{projectId}/tasks/new(projectId=${project.id})}`
- **태스크 상세**: `@{/web/tasks/{id}(id=${task.id})}`
- **태스크 수정**: `@{/web/tasks/{id}/edit(id=${task.id})}`
- **프로젝트로 돌아가기**: `@{/web/projects/{id}(id=${project.id})}`
- **페이징**: `@{/web/projects/{projectId}/tasks(projectId=${project.id}, page=${i}, size=${tasks.size})}`

#### Forms & Actions
- **검색/필터 폼**: `GET /web/projects/{projectId}/tasks`
  - `search` - 태스크명 검색
  - `status` - 상태 필터
  - `parentTaskId` - 부모 태스크 필터
- **태스크 삭제**: Modal 통한 삭제 확인

#### Conditional Elements
- 태스크 없음: `${!tasks.hasContent()}`
- 계층 표시: `${task.level}` 기반 들여쓰기
- 부모 태스크 표시: `${task.parentTaskId != null}`
- 상태별 배지: `${task.status.name()}`에 따라 색상

### Dependencies
- **Fragment**: `layout/base`
- **JavaScript**: Bootstrap Modal, WBS 트리 표시
- **CSS**: Bootstrap 5, 계층 구조 스타일

---

## tasks/detail.html
> 태스크 상세 페이지

### Controller Mapping
- **URL**: `/web/tasks/{id}`
- **Method**: `GET`
- **Controller**: `TaskWebController#taskDetail`
- **Description**: 태스크 상세 정보 표시

### Required Model Attributes

| Attribute Name | Type | Required | Default | Description |
|---------------|------|----------|---------|-------------|
| task | `TaskResponseDto` | Yes | - | 태스크 정보 |
| project | `ProjectResponseDto` | Yes | - | 소속 프로젝트 정보 |
| subtasks | `List<TaskResponseDto>` | No | empty | 하위 태스크 목록 |
| dependencies | `List<TaskDependencyResponseDto>` | No | empty | 의존성 목록 |
| pageTitle | `String` | Yes | task.name | 페이지 제목 |
| pageIcon | `String` | Yes | "fas fa-tasks" | 페이지 아이콘 |

#### TaskResponseDto Additional Fields
- `assignee` - 담당자 (향후 추가 예정)
- `priority` - 우선순위 (향후 추가 예정)
- `tags` - 태그 목록 (향후 추가 예정)

### Page Elements

#### Links & Navigation
- **태스크 수정**: `@{/web/tasks/{id}/edit(id=${task.id})}`
- **프로젝트로**: `@{/web/projects/{id}(id=${project.id})}`
- **태스크 목록**: `@{/web/projects/{projectId}/tasks(projectId=${project.id})}`
- **하위 태스크 추가**: `@{/web/projects/{projectId}/tasks/new(projectId=${project.id}, parentTaskId=${task.id})}`

#### Forms & Actions
- **태스크 삭제**: `POST /web/tasks/{id}/delete`
- **진행률 업데이트**: AJAX (향후 구현)
- **상태 변경**: AJAX (향후 구현)

#### Conditional Elements
- 하위 태스크 표시: `${subtasks != null && !subtasks.isEmpty()}`
- 의존성 표시: `${dependencies != null && !dependencies.isEmpty()}`
- 진행률 색상: `${task.progress}` 값에 따라

### Dependencies
- **Fragment**: `layout/base`
- **JavaScript**: 진행률 슬라이더
- **CSS**: Bootstrap 5

---

## tasks/form.html
> 태스크 생성/수정 폼

### Controller Mapping

#### 생성 폼
- **URL**: `/web/projects/{projectId}/tasks/new`
- **Method**: `GET`
- **Controller**: `TaskWebController#newTaskForm`

#### 수정 폼
- **URL**: `/web/tasks/{id}/edit`
- **Method**: `GET`
- **Controller**: `TaskWebController#editTaskForm`

#### 생성 처리
- **URL**: `/web/projects/{projectId}/tasks`
- **Method**: `POST`
- **Controller**: `TaskWebController#createTask`

#### 수정 처리
- **URL**: `/web/tasks/{id}`
- **Method**: `POST`
- **Controller**: `TaskWebController#updateTask`

### Required Model Attributes

| Attribute Name | Type | Required | Default | Description |
|---------------|------|----------|---------|-------------|
| task | `TaskRequestDto` | Yes | - | 태스크 폼 데이터 |
| project | `ProjectResponseDto` | Yes | - | 소속 프로젝트 정보 |
| taskId | `Long` | No | null | 수정 시 태스크 ID |
| availableParentTasks | `List<TaskResponseDto>` | Yes | empty | 선택 가능한 부모 태스크 목록 |
| pageTitle | `String` | Yes | "새 태스크" or "태스크 수정" | 페이지 제목 |
| pageIcon | `String` | Yes | "fas fa-plus" or "fas fa-edit" | 페이지 아이콘 |
| errorMessage | `String` | No | - | 오류 메시지 |

#### TaskRequestDto Fields
- `projectId` - 프로젝트 ID (hidden)
- `parentTaskId` - 부모 태스크 ID (optional)
- `name` - 태스크명 (required, max=200)
- `description` - 설명 (optional)
- `startDate` - 시작일 (required)
- `endDate` - 종료일 (required)
- `duration` - 기간 (자동 계산)
- `progress` - 진행률 (0-100, default=0)
- `status` - 상태 (required, enum)

### Page Elements

#### Forms & Actions
- **태스크 폼**:
  - 생성: `POST @{/web/projects/{projectId}/tasks(projectId=${project.id})}`
  - 수정: `POST @{/web/tasks/{id}(id=${taskId})}`
- **날짜 계산**: JavaScript로 duration 자동 계산
- **유효성 검증**:
  - 시작일 ≤ 종료일
  - 프로젝트 기간 내 포함
  - 부모 태스크 기간 내 포함

#### Conditional Elements
- 수정 모드: `${taskId != null}`
- 부모 태스크 선택: `${!availableParentTasks.isEmpty()}`
- 에러 표시: `${#fields.hasErrors('fieldName')}`

### Dependencies
- **Fragment**: `layout/base`
- **JavaScript**:
  - 날짜 계산 로직
  - 기간 자동 계산
  - 진행률 슬라이더
- **CSS**: Bootstrap 5 Forms

---

## 🔍 공통 이슈 및 해결

### 발견된 문제점
1. **부모-자식 관계**: 계층 구조 표시 로직 필요
2. **날짜 제약**: 프로젝트 기간 벗어나는 태스크 방지
3. **진행률 계산**: 하위 태스크 진행률 반영
4. **의존성 관리**: 선행 태스크 완료 체크

### 계층 구조 표시
```html
<!-- WBS 계층 들여쓰기 -->
<td th:style="'padding-left: ' + (${task.level ?: 0} * 20) + 'px'">
    <span th:if="${task.parentTaskId != null}">└</span>
    <span th:text="${task.name}"></span>
</td>
```

### 상태 전이 규칙
- `NOT_STARTED` → `IN_PROGRESS` → `COMPLETED`
- `IN_PROGRESS` → `ON_HOLD` → `IN_PROGRESS`
- 언제든 `CANCELLED` 가능

## 📊 Request Parameters

### 태스크 목록 파라미터
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| projectId | Long | Yes | 프로젝트 ID (path) |
| search | String | No | 검색어 |
| status | String | No | 상태 필터 |
| parentTaskId | Long | No | 부모 태스크 필터 |
| page | Integer | No | 페이지 번호 |
| size | Integer | No | 페이지 크기 |

### 태스크 생성 파라미터
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| projectId | Long | Yes | 프로젝트 ID (path) |
| parentTaskId | Long | No | 부모 태스크 ID (query) |

## 🔗 Related Controllers
- `TaskWebController` - `/src/main/java/com/liam/gantt/controller/TaskWebController.java`
- `TaskService` - `/src/main/java/com/liam/gantt/service/TaskService.java`
- `ProjectService` - `/src/main/java/com/liam/gantt/service/ProjectService.java`