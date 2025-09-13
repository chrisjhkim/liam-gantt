# Common & Home Views Specification
> 공통 레이아웃 및 홈 페이지 View 명세서

## 📋 Overview
홈 페이지, 공통 레이아웃, 에러 페이지 등 공통 View들의 명세입니다.

---

## home.html
> 메인 대시보드 페이지

### Controller Mapping
- **URL**: `/` 또는 `/web`
- **Method**: `GET`
- **Controller**: `HomeController#home`
- **Description**: 대시보드 및 프로젝트 요약 정보 표시

### Required Model Attributes

| Attribute Name | Type | Required | Default | Description |
|---------------|------|----------|---------|-------------|
| recentProjects | `List<ProjectResponseDto>` | Yes | empty | 최근 프로젝트 5개 |
| totalProjects | `Long` | Yes | 0 | 전체 프로젝트 수 |
| activeProjects | `Long` | Yes | 0 | 진행중 프로젝트 수 |
| completedProjects | `Long` | Yes | 0 | 완료된 프로젝트 수 |
| pageTitle | `String` | Yes | "대시보드" | 페이지 제목 |
| pageIcon | `String` | Yes | "fas fa-tachometer-alt" | 페이지 아이콘 |
| activePage | `String` | Yes | "home" | 활성 메뉴 표시용 |
| errorMessage | `String` | No | - | 오류 메시지 |

### Page Elements

#### Dashboard Cards
- **통계 카드**:
  - 전체 프로젝트 수
  - 진행중 프로젝트
  - 완료된 프로젝트
  - 이번 주 마감 태스크 (향후)

#### Recent Projects Section
- **프로젝트 카드**: 최근 5개 프로젝트
  - 프로젝트명
  - 진행률 바
  - 상태 배지
  - 시작일/종료일
  - 빠른 링크 (상세/간트차트)

#### Quick Actions
- **새 프로젝트**: `@{/web/projects/new}`
- **프로젝트 목록**: `@{/web/projects}`
- **내 태스크**: `@{/web/my-tasks}` (향후)
- **캘린더 뷰**: `@{/web/calendar}` (향후)

#### Charts (향후 구현)
- **진행률 차트**: Chart.js 도넛 차트
- **일정 타임라인**: 이번 주/월 일정
- **태스크 통계**: 상태별 분포

### Conditional Elements
- 프로젝트 없음: `${recentProjects.isEmpty()}`
- 오류 메시지: `${errorMessage != null}`

### Dependencies
- **Fragment**: `layout/base`
- **JavaScript**: Chart.js (향후)
- **CSS**: Bootstrap 5, Dashboard 스타일

---

## layout/base.html (Fragment)
> 공통 레이아웃 템플릿

### Fragment Definitions

#### head Fragment
```html
<head th:fragment="head">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${pageTitle} + ' - Liam Gantt'">Liam Gantt</title>
    <!-- Bootstrap CSS -->
    <!-- Font Awesome -->
    <!-- Custom CSS -->
</head>
```

#### nav Fragment
```html
<nav th:fragment="nav">
    <!-- 네비게이션 바 -->
</nav>
```

#### footer Fragment
```html
<footer th:fragment="footer">
    <!-- 푸터 -->
</footer>
```

#### scripts Fragment
```html
<div th:fragment="scripts">
    <!-- jQuery -->
    <!-- Bootstrap JS -->
    <!-- Custom JS -->
</div>
```

### Required Variables (From Including Page)
| Variable | Type | Required | Description |
|----------|------|----------|-------------|
| pageTitle | `String` | Yes | 페이지 제목 |
| pageIcon | `String` | No | 페이지 아이콘 |
| activePage | `String` | No | 활성 메뉴 |

### Navigation Structure
```
Liam Gantt (로고/홈)
├── 대시보드 (/)
├── 프로젝트 (/web/projects)
│   ├── 목록
│   └── 새 프로젝트
├── 태스크 (/web/tasks) - 향후
├── 캘린더 (/web/calendar) - 향후
└── 설정 (/web/settings) - 향후
```

### User Info Section (향후)
- 사용자 이름
- 프로필 이미지
- 로그아웃 링크

---

## error.html
> 에러 페이지

### Controller Mapping
- **URL**: `/error`
- **Method**: Any
- **Controller**: Spring Boot Default Error Handler
- **Description**: 에러 발생 시 표시되는 페이지

### Required Model Attributes

| Attribute Name | Type | Required | Default | Description |
|---------------|------|----------|---------|-------------|
| status | `Integer` | Yes | - | HTTP 상태 코드 |
| error | `String` | Yes | - | 에러 타입 |
| message | `String` | No | - | 에러 메시지 |
| timestamp | `Date` | Yes | - | 발생 시간 |
| path | `String` | Yes | - | 요청 경로 |

### Error Types
- **404**: 페이지를 찾을 수 없습니다
- **403**: 접근 권한이 없습니다
- **500**: 서버 오류가 발생했습니다
- **400**: 잘못된 요청입니다

### Page Elements
- **에러 코드 표시**: 큰 숫자로 상태 코드
- **에러 메시지**: 사용자 친화적 설명
- **홈으로 버튼**: `@{/}`
- **이전 페이지**: JavaScript `history.back()`

---

## fragments/alerts.html (Fragment)
> 알림 메시지 컴포넌트

### Fragment Definition
```html
<div th:fragment="alerts">
    <!-- Success Message -->
    <div th:if="${successMessage}" class="alert alert-success alert-dismissible fade show">
        <i class="fas fa-check-circle me-2"></i>
        <span th:text="${successMessage}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>

    <!-- Error Message -->
    <div th:if="${errorMessage}" class="alert alert-danger alert-dismissible fade show">
        <i class="fas fa-exclamation-circle me-2"></i>
        <span th:text="${errorMessage}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>

    <!-- Warning Message -->
    <div th:if="${warningMessage}" class="alert alert-warning alert-dismissible fade show">
        <i class="fas fa-exclamation-triangle me-2"></i>
        <span th:text="${warningMessage}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>

    <!-- Info Message -->
    <div th:if="${infoMessage}" class="alert alert-info alert-dismissible fade show">
        <i class="fas fa-info-circle me-2"></i>
        <span th:text="${infoMessage}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</div>
```

### Usage
```html
<div th:replace="fragments/alerts :: alerts"></div>
```

---

## fragments/pagination.html (Fragment)
> 페이지네이션 컴포넌트

### Fragment Definition
```html
<nav th:fragment="pagination(page, baseUrl)">
    <ul class="pagination justify-content-center">
        <!-- Previous -->
        <li class="page-item" th:classappend="${page.first} ? 'disabled'">
            <a class="page-link" th:href="@{${baseUrl}(page=${page.number - 1}, size=${page.size})}">
                이전
            </a>
        </li>

        <!-- Page Numbers -->
        <li class="page-item" th:each="i : ${#numbers.sequence(0, page.totalPages - 1)}"
            th:classappend="${i == page.number} ? 'active'">
            <a class="page-link" th:href="@{${baseUrl}(page=${i}, size=${page.size})}" th:text="${i + 1}">
            </a>
        </li>

        <!-- Next -->
        <li class="page-item" th:classappend="${page.last} ? 'disabled'">
            <a class="page-link" th:href="@{${baseUrl}(page=${page.number + 1}, size=${page.size})}">
                다음
            </a>
        </li>
    </ul>
</nav>
```

### Parameters
| Parameter | Type | Description |
|-----------|------|-------------|
| page | `Page<?>` | 페이지 객체 |
| baseUrl | `String` | 기본 URL 경로 |

---

## 🎨 공통 스타일 가이드

### 색상 변수
```css
:root {
    --primary: #007bff;
    --success: #28a745;
    --danger: #dc3545;
    --warning: #ffc107;
    --info: #17a2b8;
    --dark: #343a40;
    --light: #f8f9fa;
}
```

### 레이아웃 구조
```css
.main-wrapper {
    display: flex;
    min-height: 100vh;
}

.sidebar {
    width: 250px;
    background: var(--dark);
}

.content {
    flex: 1;
    padding: 20px;
}
```

### 반응형 브레이크포인트
- **xs**: < 576px (모바일)
- **sm**: ≥ 576px (태블릿)
- **md**: ≥ 768px (태블릿 가로)
- **lg**: ≥ 992px (데스크톱)
- **xl**: ≥ 1200px (대형 데스크톱)

## 🔍 공통 JavaScript 유틸리티

### AJAX 헬퍼
```javascript
// CSRF 토큰 포함 AJAX 요청
function ajaxRequest(url, method, data) {
    return $.ajax({
        url: url,
        method: method,
        data: JSON.stringify(data),
        contentType: 'application/json',
        headers: {
            'X-CSRF-TOKEN': $('meta[name="csrf-token"]').attr('content')
        }
    });
}
```

### 날짜 포맷터
```javascript
function formatDate(date) {
    return new Date(date).toLocaleDateString('ko-KR');
}
```

### 알림 표시
```javascript
function showAlert(message, type = 'success') {
    const alertHtml = `
        <div class="alert alert-${type} alert-dismissible fade show">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    $('#alert-container').html(alertHtml);
}
```

## 🔗 Related Files
- `HomeController` - `/src/main/java/com/liam/gantt/controller/HomeController.java`
- Base Layout - `/src/main/resources/templates/layout/base.html`
- Home Template - `/src/main/resources/templates/home.html`
- Error Template - `/src/main/resources/templates/error.html`