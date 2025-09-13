# Gantt Chart Views Specification
> 간트차트 관련 View 명세서

## 📋 Overview
프로젝트 일정을 간트차트로 시각화하는 View들의 명세입니다.

---

## gantt/chart.html
> 간트차트 표시 페이지

### Controller Mapping
- **URL**: `/web/projects/{id}/gantt`
- **Method**: `GET`
- **Controller**: `GanttWebController#ganttChart`
- **Description**: 프로젝트의 태스크들을 간트차트로 시각화

### Required Model Attributes

| Attribute Name | Type | Required | Default | Description |
|---------------|------|----------|---------|-------------|
| ganttData | `GanttChartDto` | Yes | - | 간트차트 데이터 |
| projectId | `Long` | Yes | - | 프로젝트 ID |
| pageTitle | `String` | Yes | "${project.name} - 간트차트" | 페이지 제목 |
| pageIcon | `String` | Yes | "fas fa-chart-gantt" | 페이지 아이콘 |

#### GanttChartDto Structure
```java
{
  project: ProjectResponseDto,      // 프로젝트 정보
  tasks: List<TaskResponseDto>,     // 태스크 목록 (계층 구조)
  dependencies: List<TaskDependencyResponseDto>, // 의존성 정보
  timeline: TimelineInfo {          // 타임라인 정보
    startDate: LocalDate,           // 전체 시작일
    endDate: LocalDate,             // 전체 종료일
    totalDays: Long,                // 전체 기간
    workingDays: Long,              // 작업일수
    months: List<String>            // 월 표시용
  },
  statistics: Statistics {          // 통계 정보
    totalTasks: Integer,
    completedTasks: Integer,
    inProgressTasks: Integer,
    notStartedTasks: Integer,
    averageProgress: Double,
    criticalPathTasks: List<Long>  // Critical Path 태스크 ID
  }
}
```

### Page Elements

#### Chart Components
- **타임라인 헤더**: 년/월/일 표시
- **태스크 목록**: WBS 계층 구조
- **태스크 바**:
  - 위치: 시작일 기준
  - 길이: duration 기준
  - 색상: 상태별 (완료=초록, 진행중=파랑, 미시작=회색)
  - 진행률: 바 내부 채움
- **의존성 화살표**: 선행-후행 태스크 연결
- **오늘 표시선**: 현재 날짜 표시
- **마일스톤**: 기간 0인 태스크 다이아몬드 표시

#### Interactive Features
- **줌 인/아웃**: 일/주/월 단위 변경
- **드래그 앤 드롭**: 태스크 일정 조정 (향후)
- **호버 정보**: 태스크 상세 툴팁
- **클릭 이벤트**: 태스크 상세 페이지 이동
- **필터**: 상태별, 담당자별 필터링

#### Links & Navigation
- **프로젝트로 돌아가기**: `@{/web/projects/{id}(id=${projectId})}`
- **태스크 상세**: 클릭 시 `@{/web/tasks/{id}(id=${task.id})}`
- **PDF 내보내기**: JavaScript 기능
- **이미지 저장**: JavaScript 기능
- **전체화면**: JavaScript 기능

### JavaScript Data Binding
```javascript
// Thymeleaf에서 JavaScript로 데이터 전달
const ganttData = {
    project: /*[[${ganttData.project}]]*/ {},
    tasks: /*[[${ganttData.tasks}]]*/ [],
    dependencies: /*[[${ganttData.dependencies}]]*/ [],
    timeline: /*[[${ganttData.timeline}]]*/ {}
};

// 또는 JSON으로 직렬화
const ganttDataJson = /*[[${#strings.escapeJson(ganttData)}]]*/ '{}';
```

### Dependencies
- **Fragment**: `layout/base`
- **JavaScript Libraries**:
  - D3.js 또는 Chart.js (차트 렌더링)
  - DHTMLX Gantt (상용 라이브러리 옵션)
  - Frappe Gantt (오픈소스 옵션)
- **CSS**:
  - Bootstrap 5
  - 간트차트 전용 스타일

---

## gantt/components/task-bar.html (Fragment)
> 간트차트 태스크 바 컴포넌트

### Fragment Definition
```html
<div th:fragment="taskBar(task, timeline)">
    <!-- 태스크 바 렌더링 로직 -->
</div>
```

### Required Parameters
| Parameter | Type | Description |
|-----------|------|-------------|
| task | `TaskResponseDto` | 태스크 정보 |
| timeline | `TimelineInfo` | 타임라인 정보 |

### Calculated Values
- **X 위치**: `(task.startDate - timeline.startDate) * pixelsPerDay`
- **너비**: `task.duration * pixelsPerDay`
- **Y 위치**: `task.rowIndex * rowHeight`
- **진행률 너비**: `width * (task.progress / 100)`

---

## 🔍 렌더링 로직

### 날짜 계산
```javascript
// 픽셀 위치 계산
function calculatePosition(date, timelineStart, pixelsPerDay) {
    const daysDiff = Math.floor((date - timelineStart) / (1000 * 60 * 60 * 24));
    return daysDiff * pixelsPerDay;
}

// 태스크 바 너비 계산
function calculateWidth(startDate, endDate, pixelsPerDay) {
    const duration = Math.floor((endDate - startDate) / (1000 * 60 * 60 * 24)) + 1;
    return duration * pixelsPerDay;
}
```

### Critical Path 표시
```css
.task-bar.critical-path {
    border: 2px solid red;
    box-shadow: 0 0 5px rgba(255, 0, 0, 0.5);
}
```

### 의존성 화살표
```javascript
// SVG로 화살표 그리기
function drawDependency(fromTask, toTask) {
    const fromX = fromTask.x + fromTask.width;
    const fromY = fromTask.y + fromTask.height / 2;
    const toX = toTask.x;
    const toY = toTask.y + toTask.height / 2;

    // SVG path 생성
    return `M ${fromX} ${fromY} L ${toX} ${toY}`;
}
```

## 📊 성능 고려사항

### 대용량 데이터 처리
- **Virtual Scrolling**: 보이는 영역만 렌더링
- **Lazy Loading**: 스크롤 시 추가 로드
- **Canvas 렌더링**: DOM 대신 Canvas 사용 (1000+ 태스크)

### 최적화 기법
```javascript
// 디바운싱으로 렌더링 최적화
let renderTimeout;
function scheduleRender() {
    clearTimeout(renderTimeout);
    renderTimeout = setTimeout(render, 16); // 60fps
}

// 메모이제이션
const taskPositionCache = new Map();
```

## 🎨 스타일 가이드

### 색상 팔레트
| 상태 | 색상 | Hex |
|------|------|-----|
| COMPLETED | 초록 | #28a745 |
| IN_PROGRESS | 파랑 | #007bff |
| NOT_STARTED | 회색 | #6c757d |
| ON_HOLD | 노랑 | #ffc107 |
| CANCELLED | 빨강 | #dc3545 |
| CRITICAL_PATH | 진홍 | #ff0000 |

### 반응형 디자인
```css
/* 모바일 */
@media (max-width: 768px) {
    .gantt-chart {
        overflow-x: auto;
        min-width: 800px;
    }
}

/* 태블릿 */
@media (max-width: 1024px) {
    .gantt-timeline {
        font-size: 12px;
    }
}
```

## 🔗 Related Controllers
- `GanttWebController` - (현재 미구현, 향후 추가)
- `GanttService` - `/src/main/java/com/liam/gantt/service/GanttService.java`

## 📝 구현 상태
> ⚠️ **Note**: 간트차트 View는 현재 기본 구조만 준비되어 있으며,
> 실제 차트 렌더링 로직은 Phase 2에서 구현 예정입니다.

### 구현 완료
- [x] 데이터 구조 (GanttChartDto)
- [x] Service 계층 로직
- [x] API 엔드포인트

### 구현 예정
- [ ] GanttWebController
- [ ] gantt/chart.html 템플릿
- [ ] JavaScript 차트 라이브러리 통합
- [ ] 인터랙티브 기능
- [ ] PDF/이미지 내보내기