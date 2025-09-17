# React 마이그레이션 가이드

## 개요

현재 Thymeleaf + JavaScript 기반의 간트차트 애플리케이션을 React SPA로 마이그레이션하기 위한 단계별 가이드입니다.

## 현재 상태 분석

### ✅ 완료된 준비 작업
1. **Core Module 분리**: `gantt-core.js`로 비즈니스 로직 모듈화
2. **React 컴포넌트 구조 설계**: 컴포넌트별 역할 분리
3. **상태 관리 패턴**: 중앙화된 상태 관리 구조
4. **API 인터페이스**: RESTful API 완전 구현
5. **반응형 디자인**: 모바일 친화적 UI 완성

### 🔧 기술 스택 준비
- **Backend**: Spring Boot + REST API ✅
- **Frontend**: Thymeleaf → React (마이그레이션 대상)
- **Build**: Gradle → Webpack/Vite 추가 필요
- **State Management**: 기본 React State → Redux Toolkit 권장
- **UI Library**: Bootstrap 5 → React Bootstrap 전환

## 마이그레이션 로드맵

### Phase 1: 개발 환경 설정 (1-2주)

#### 1.1 Frontend 빌드 환경 구축
```bash
# 프로젝트 루트에 frontend 디렉토리 생성
mkdir frontend
cd frontend

# React 앱 초기화
npx create-react-app . --template typescript
# 또는 Vite 사용 (권장)
npm create vite@latest . -- --template react-ts

# 필수 의존성 추가
npm install @reduxjs/toolkit react-redux
npm install react-bootstrap bootstrap
npm install d3 @types/d3
npm install axios react-router-dom
npm install @testing-library/react @testing-library/jest-dom
```

#### 1.2 Gradle 빌드 통합
```kotlin
// build.gradle.kts에 추가
tasks.register<Exec>("buildFrontend") {
    workingDir("frontend")
    commandLine("npm", "run", "build")
}

tasks.named("processResources") {
    dependsOn("buildFrontend")
}

tasks.register<Copy>("copyFrontendBuild") {
    from("frontend/dist")
    into("src/main/resources/static")
}
```

### Phase 2: Core 컴포넌트 마이그레이션 (2-3주)

#### 2.1 상태 관리 설정
```typescript
// store/ganttSlice.ts
import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

export const loadProjectData = createAsyncThunk(
  'gantt/loadProjectData',
  async (projectId: number) => {
    const response = await fetch(`/api/v1/projects/${projectId}`);
    return response.json();
  }
);

const ganttSlice = createSlice({
  name: 'gantt',
  initialState: {
    projectData: null,
    tasksData: [],
    filteredTasks: [],
    filters: { search: '', status: '', progress: '', dateFrom: '', dateTo: '' },
    currentView: 'basic',
    loading: false,
    error: null
  },
  reducers: {
    setFilters: (state, action) => {
      state.filters = action.payload;
    },
    setCurrentView: (state, action) => {
      state.currentView = action.payload;
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(loadProjectData.pending, (state) => {
        state.loading = true;
      })
      .addCase(loadProjectData.fulfilled, (state, action) => {
        state.loading = false;
        state.projectData = action.payload.project;
        state.tasksData = action.payload.tasks;
        state.filteredTasks = action.payload.tasks;
      });
  }
});
```

#### 2.2 핵심 컴포넌트 구현 순서
1. `GanttChart` (메인 컨테이너)
2. `GanttHeader` (헤더 및 네비게이션)
3. `GanttStatistics` (통계 표시)
4. `GanttControls` (뷰 전환 및 필터)
5. `BasicGanttChart` (기본 간트차트)
6. `D3GanttChart` (고급 시각화)
7. `TaskDetailModal` (태스크 상세 모달)

### Phase 3: 고급 기능 구현 (2-3주)

#### 3.1 D3.js React 통합
```typescript
// components/D3GanttChart.tsx
import React, { useEffect, useRef } from 'react';
import * as d3 from 'd3';

const D3GanttChart: React.FC<Props> = ({ tasks, projectData }) => {
  const svgRef = useRef<SVGSVGElement>(null);

  useEffect(() => {
    if (!svgRef.current || !tasks.length) return;

    const svg = d3.select(svgRef.current);
    // D3 차트 구현...

    return () => {
      svg.selectAll("*").remove();
    };
  }, [tasks, projectData]);

  return <svg ref={svgRef} className="d3-gantt-container" />;
};
```

#### 3.2 고급 기능들
- 드래그 앤 드롭으로 태스크 일정 조정
- 실시간 진행률 업데이트
- 태스크 의존성 시각화
- 확대/축소 및 팬 기능
- 키보드 단축키 지원

### Phase 4: 통합 및 최적화 (1-2주)

#### 4.1 성능 최적화
```typescript
// React.memo로 불필요한 리렌더링 방지
const TaskItem = React.memo(({ task }: { task: Task }) => {
  return <div className="task-item">{task.name}</div>;
});

// useMemo로 계산 결과 캐싱
const statistics = useMemo(() => {
  return calculateStatistics(filteredTasks);
}, [filteredTasks]);

// useCallback으로 함수 재생성 방지
const handleFilterChange = useCallback((filterType: string, value: string) => {
  dispatch(setFilters({ ...filters, [filterType]: value }));
}, [filters, dispatch]);
```

#### 4.2 테스트 구현
```typescript
// __tests__/GanttChart.test.tsx
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { store } from '../store';
import GanttChart from '../components/GanttChart';

test('renders gantt chart with project data', () => {
  render(
    <Provider store={store}>
      <GanttChart projectId={1} />
    </Provider>
  );

  expect(screen.getByText('간트차트:')).toBeInTheDocument();
});
```

## 마이그레이션 체크리스트

### ✅ 사전 준비
- [ ] React 개발 환경 설정
- [ ] TypeScript 설정
- [ ] Redux Toolkit 설정
- [ ] 라우팅 설정 (React Router)
- [ ] UI 라이브러리 설정 (React Bootstrap)

### 🔄 컴포넌트 마이그레이션
- [ ] 메인 간트차트 컨테이너
- [ ] 통계 대시보드
- [ ] 필터 및 검색 기능
- [ ] 기본 간트차트 뷰
- [ ] D3.js 고급 시각화
- [ ] 태스크 상세 모달
- [ ] 반응형 레이아웃

### 🧪 테스트 및 검증
- [ ] 단위 테스트 (컴포넌트별)
- [ ] 통합 테스트 (API 연동)
- [ ] E2E 테스트 (사용자 시나리오)
- [ ] 성능 테스트
- [ ] 크로스 브라우저 테스트
- [ ] 모바일 반응형 테스트

### 🚀 배포 및 운영
- [ ] 프로덕션 빌드 최적화
- [ ] 번들 크기 최적화
- [ ] PWA 설정 (선택사항)
- [ ] CI/CD 파이프라인 설정
- [ ] 모니터링 및 에러 추적

## 기술적 고려사항

### 1. 상태 관리
- **Redux Toolkit** 사용으로 복잡한 상태 관리 간소화
- **RTK Query**로 API 호출 및 캐싱 최적화
- **개발자 도구** 활용으로 디버깅 향상

### 2. 성능 최적화
- **Code Splitting**: 라우트별 번들 분리
- **Lazy Loading**: 컴포넌트 지연 로딩
- **Virtualization**: 대량 태스크 렌더링 최적화
- **Memoization**: 계산 결과 캐싱

### 3. 접근성 (A11y)
- 키보드 네비게이션 지원
- 스크린 리더 지원 (ARIA 레이블)
- 고대비 모드 지원
- 포커스 관리

### 4. 국제화 (i18n)
- React i18n 라이브러리 통합
- 다국어 날짜/시간 형식
- RTL 언어 지원 고려

## 예상 일정

| Phase | 기간 | 주요 작업 |
|-------|------|----------|
| Phase 1 | 1-2주 | 개발 환경 설정, 기본 구조 |
| Phase 2 | 2-3주 | 핵심 컴포넌트 마이그레이션 |
| Phase 3 | 2-3주 | 고급 기능 구현 |
| Phase 4 | 1-2주 | 최적화 및 테스트 |
| **총 예상 기간** | **6-10주** | |

## 위험 요소 및 대응

### 1. D3.js React 통합 복잡성
- **대응**: useRef와 useEffect 활용한 안전한 DOM 조작
- **대안**: React D3 래퍼 라이브러리 고려

### 2. 성능 이슈
- **대응**: React DevTools Profiler로 성능 모니터링
- **대안**: Virtual scrolling, 페이지네이션 적용

### 3. 기존 기능 누락
- **대응**: 기능별 체크리스트 작성 및 단계별 검증
- **대안**: 점진적 마이그레이션 (Hybrid 방식)

## 마이그레이션 후 이점

1. **개발 생산성 향상**: 컴포넌트 재사용성, 타입 안정성
2. **사용자 경험 개선**: SPA의 빠른 페이지 전환
3. **유지보수성 향상**: 명확한 컴포넌트 구조
4. **확장성**: 새로운 기능 추가 용이성
5. **생태계 활용**: React 생태계의 풍부한 라이브러리

## 결론

현재 구현된 간트차트는 React 마이그레이션을 위한 견고한 기반을 제공합니다. 모듈화된 Core 클래스와 잘 정의된 API 인터페이스를 통해 안전하고 체계적인 마이그레이션이 가능할 것으로 예상됩니다.