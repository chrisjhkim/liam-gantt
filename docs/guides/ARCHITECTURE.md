# CLAUDE.md - Liam Gantt Chart Project

## Project Identity
**프로젝트명**: Liam Gantt Chart Web Application  
**도메인**: 프로젝트 관리 및 간트 차트 시각화  
**개발자**: 개인 프로젝트 (향후 팀 확장 예정)  
**버전**: 1.0.0-SNAPSHOT

## 🚀 Quick Start Commands
```bash
/build      # 프로젝트 빌드
/test       # 테스트 실행  
/clean      # 빌드 산출물 정리
/migrate    # DB 마이그레이션 실행
```

## 🛠 Technology Stack
### Backend
- **언어**: Java 21 (IntelliJ IDEA Built-in JDK)
- **프레임워크**: Spring Boot 3.5.5
- **빌드 도구**: Gradle 8.x (Kotlin DSL)
- **데이터베이스**: H2 (개발) → MariaDB (운영)
- **ORM**: Spring Data JPA + Hibernate
- **마이그레이션**: Flyway

### Frontend (단계적 전환)
- **Phase 1**: Thymeleaf + Bootstrap 5
- **Phase 2**: React + TypeScript (학습 후 전환)

### 개발 환경
- **IDE**: IntelliJ IDEA + Claude Code Plugin
- **버전 관리**: Git (GitHub Flow)
- **커밋 규칙**: Conventional Commits

## 📁 Project Architecture
```
src/main/java/com/liam/gantt/
├── controller/          # REST API + Web Controller
├── service/            # 비즈니스 로직
├── repository/         # 데이터 접근
├── entity/            # JPA 엔티티
├── dto/               # 데이터 전송 객체
└── config/            # 설정 클래스
```

## 🎯 MVP Features (1단계)
1. **프로젝트 관리**
   - 프로젝트 생성/수정/삭제
   - 프로젝트명, 시작일, 종료일 설정

2. **태스크 관리**
   - 태스크 생성/수정/삭제
   - WBS(Work Breakdown Structure) 구조

3. **간트 차트 조회**
   - 웹 브라우저에서 간트 차트 시각화
   - 프로젝트 일정 한눈에 보기

## 📊 Database Design
```sql
-- Core entities for MVP
projects: id, name, description, start_date, end_date, status
tasks: id, project_id, name, start_date, end_date, duration, progress
task_dependencies: id, predecessor_id, successor_id, type
```

## 🌐 API Design
**Base URL**: `/api/v1/`

### Project APIs
```
GET    /api/v1/projects        # 프로젝트 목록
POST   /api/v1/projects        # 프로젝트 생성
GET    /api/v1/projects/{id}   # 프로젝트 상세
PUT    /api/v1/projects/{id}   # 프로젝트 수정
DELETE /api/v1/projects/{id}   # 프로젝트 삭제
```

### Task APIs
```
GET    /api/v1/projects/{id}/tasks     # 프로젝트 태스크 목록
POST   /api/v1/projects/{id}/tasks     # 태스크 생성
PUT    /api/v1/tasks/{id}              # 태스크 수정
DELETE /api/v1/tasks/{id}              # 태스크 삭제
```

## 💻 Development Workflow
### Git Flow (GitHub Flow 기반)
1. `main` 브랜치에서 `feature/기능명` 브랜치 생성
2. 기능 개발 및 테스트
3. `/build` + `/test` 로 검증
4. `main`으로 merge

### Commit Convention
```
feat: 새로운 기능 추가
fix: 버그 수정  
docs: 문서 수정
style: 코드 포맷팅
refactor: 코드 리팩토링
test: 테스트 추가/수정
chore: 빌드/설정 변경
```

## 🔧 Development Guidelines
### Coding Standards
- **패키지 구조**: `com.liam.gantt.{layer}.{domain}`
- **클래스명**: PascalCase (ProjectService, TaskController)
- **메서드명**: camelCase (createProject, findTaskById)
- **상수**: UPPER_SNAKE_CASE (DEFAULT_PAGE_SIZE)
- **한국어 주석**: 비즈니스 로직 설명 시 허용

### API Response Format
```json
{
  "status": "success",
  "data": { ... },
  "message": "Operation completed successfully"
}
```

### Error Response Format
```json
{
  "status": "error", 
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "입력값이 올바르지 않습니다."
  }
}
```

## 📚 Layer-Specific Guidelines
상세한 개발 가이드는 각 계층별 CLAUDE.md 참조:
- @src/main/java/com/liam/gantt/controller/CLAUDE.md
- @src/main/java/com/liam/gantt/service/CLAUDE.md  
- @src/main/java/com/liam/gantt/repository/CLAUDE.md

## 🚀 Development Phases
### Phase 1: MVP 개발 (현재)
- [x] 프로젝트 기본 설정
- [ ] 핵심 엔티티 설계 (Project, Task)
- [ ] CRUD API 구현
- [ ] Thymeleaf 기반 기본 UI

### Phase 2: 기능 확장
- [ ] 태스크 의존성 관리
- [ ] 간트 차트 고급 시각화
- [ ] MariaDB 전환
- [ ] 사용자 인증

### Phase 3: React 전환
- [ ] React + TypeScript 학습
- [ ] Frontend 점진적 전환
- [ ] API 최적화

## ⚠️ Important Reminders
- **ALWAYS run `/test` before committing**
- **Use Conventional Commits for all commits**
- **Update database via Flyway migrations only**
- **Keep API responses consistent**
- **Document new features in relevant CLAUDE.md files**

## 🆘 Troubleshooting
- 빌드 실패 시: `/clean` 후 `/build` 재시도
- DB 이슈 시: `/migrate` 로 스키마 동기화
- 테스트 실패 시: 로그 확인 후 관련 테스트 수정
- IntelliJ 이슈 시: Gradle refresh + 프로젝트 reimport