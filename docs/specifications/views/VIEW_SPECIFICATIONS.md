# View Specifications Index
> Liam Gantt 프로젝트 View 명세서 메인 인덱스

## 📚 개요
이 문서는 모든 View(HTML 템플릿) 명세서의 인덱스입니다.
각 View가 필요로 하는 Model 데이터, 페이지 기능, Controller 매핑을 체계적으로 정리합니다.

## 🎯 목적
- **버그 예방**: Controller-View 간 데이터 불일치 방지
- **개발 효율**: 필요한 데이터와 기능 명확화
- **유지보수**: 변경 영향도 파악 용이
- **AI 지원**: Claude/Copilot 등 AI 도구의 정확한 코드 생성

## 📋 문서 구조

### [📖 작성 가이드라인](./VIEW_SPEC_GUIDELINES.md)
View 명세서 작성 방법과 규칙

### [🏗️ 프로젝트 관련 Views](./project-views.md)
- `/web/projects` - 프로젝트 목록
- `/web/projects/{id}` - 프로젝트 상세
- `/web/projects/new` - 프로젝트 생성
- `/web/projects/{id}/edit` - 프로젝트 수정

### [📝 태스크 관련 Views](./task-views.md)
- `/web/projects/{projectId}/tasks` - 태스크 목록
- `/web/tasks/{id}` - 태스크 상세
- `/web/projects/{projectId}/tasks/new` - 태스크 생성
- `/web/tasks/{id}/edit` - 태스크 수정

### [📊 간트차트 Views](./gantt-views.md)
- `/web/projects/{id}/gantt` - 간트차트 뷰

### [🏠 공통/홈 Views](./common-views.md)
- `/` - 홈/대시보드
- `/error` - 에러 페이지
- `layout/base` - 공통 레이아웃

## 🔍 빠른 참조

### Controller 클래스별 View 매핑

| Controller | View Files | 주요 기능 |
|-----------|------------|---------|
| `HomeController` | `home.html` | 대시보드, 최근 프로젝트 |
| `ProjectWebController` | `projects/*.html` | 프로젝트 CRUD |
| `TaskWebController` | `tasks/*.html` | 태스크 CRUD |
| `GanttWebController` | `gantt/*.html` | 간트차트 표시 |

### 주요 Model Attributes

#### 공통 속성
- `pageTitle` - 페이지 제목
- `pageIcon` - 페이지 아이콘 (Font Awesome)
- `activePage` - 활성 메뉴 표시
- `errorMessage` - 오류 메시지
- `successMessage` - 성공 메시지

#### 페이징 관련
- `Page<T>` 객체 사용 시:
  - `.content` - 실제 데이터 리스트
  - `.totalElements` - 전체 개수
  - `.totalPages` - 전체 페이지 수
  - `.number` - 현재 페이지 번호
  - `.first` - 첫 페이지 여부
  - `.last` - 마지막 페이지 여부

## ⚠️ 주요 이슈 및 해결

### 발견된 문제점들
1. **누락된 Model 속성**: Controller에서 View가 필요로 하는 데이터 미전달
2. **타입 불일치**: DTO 필드명과 View 참조명 불일치
3. **Null 처리**: Optional 값에 대한 안전한 처리 미흡
4. **페이징 데이터**: `Page` 객체 속성 접근 오류

### 해결 방안
- 각 View 명세서 참조하여 필수 Model 속성 확인
- Thymeleaf null-safe 연산자 사용 (`?:`, `?`)
- DTO 필드명 일관성 유지

## 🚀 사용 방법

### 개발자
1. 새 View 생성 시 먼저 명세 작성
2. Controller 개발 시 명세 참조하여 Model 설정
3. View 수정 시 명세 업데이트

### AI 도구 활용
```
"projects/list.html에 필요한 Model 데이터를
project-views.md 명세를 참조하여 Controller에 추가해줘"
```

## 📈 버전 관리

| 버전 | 날짜 | 변경사항 |
|-----|------|---------|
| 1.0 | 2024-01-13 | 초기 작성 |

## 🔗 관련 문서
- [Controller 개발 가이드](/src/main/java/com/liam/gantt/controller/CLAUDE.md)
- [Service 계층 가이드](/src/main/java/com/liam/gantt/service/CLAUDE.md)
- [API 명세서](/docs/specifications/API.md)