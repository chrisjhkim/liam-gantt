# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Liam Gantt Chart Application - Claude Code 개발 가이드

> **✅ 프로젝트 상태**: 백엔드 + REST API 컨트롤러 완료 (모든 테스트 통과)
> **📅 마지막 업데이트**: 2025-09-12
> **🔧 개발 환경**: Java 21 + Spring Boot 3.5.5 + Gradle

## 🚀 Quick Commands
```bash
/build      # 프로젝트 빌드 (clean + compile + test + jar)
/test       # 테스트 실행 (unit + integration tests)
/clean      # 빌드 산출물 정리
/migrate    # 데이터베이스 마이그레이션 (Flyway)

# 기본 Gradle 명령어
./gradlew bootRun    # 애플리케이션 실행 (http://localhost:8080)
./gradlew build      # 빌드
./gradlew test       # 테스트
./gradlew --stop     # 서버 중지 (Gradle 데몬 종료)
```

## 📊 Project Overview
**간트 차트 웹 애플리케이션** - 프로젝트 관리 및 일정 시각화 도구

### 핵심 정보
- **Tech Stack**: Java 21 + Spring Boot 3.5.5 + Gradle
- **Database**: H2 (개발) → MariaDB (운영 예정)  
- **Frontend**: Thymeleaf → React 전환 계획
- **Architecture**: Layered (Controller → Service → Repository → Entity)
- **IDE**: IntelliJ IDEA + Claude Code Plugin

### MVP 기능 (1단계)
1. **프로젝트 관리**: 생성/수정/삭제, 이름/시작일/종료일 설정
2. **태스크 관리**: WBS 구조, 태스크 CRUD 
3. **간트차트 조회**: 웹 브라우저에서 프로젝트 일정 시각화
4. **REST API**: 완전한 RESTful API 제공

## 🏗️ Project Structure  
```
src/main/java/com/liam/gantt/
├── config/         # 설정 클래스 (JpaConfig)
├── controller/     # REST API + Web Controller (✅ 구현 완료)
│   ├── api/v1/     # REST API 컨트롤러
│   │   ├── ProjectController.java
│   │   ├── TaskController.java  
│   │   └── GanttController.java
│   ├── HomeController.java
│   └── ProjectWebController.java
├── service/        # 비즈니스 로직 (✅ 구현 완료)
│   ├── impl/       # 서비스 구현체
│   │   ├── ProjectServiceImpl.java
│   │   ├── TaskServiceImpl.java
│   │   └── GanttServiceImpl.java
│   └── interfaces... # 서비스 인터페이스
├── mapper/         # DTO ↔ Entity 변환 (✅ 구현 완료)
│   ├── ProjectMapper.java
│   └── TaskMapper.java
├── repository/     # 데이터 액세스 (✅ 구현 완료)
│   ├── ProjectRepository.java
│   ├── TaskRepository.java
│   └── TaskDependencyRepository.java
├── entity/         # JPA 엔티티 (✅ 구현 완료)
│   ├── BaseEntity.java
│   ├── Project.java
│   ├── Task.java
│   ├── TaskDependency.java
│   └── enums/
│       ├── ProjectStatus.java
│       ├── TaskStatus.java
│       └── DependencyType.java
├── dto/            # 데이터 전송 객체 (✅ 구현 완료)
│   ├── request/    # 요청 DTO
│   │   ├── ProjectRequestDto.java
│   │   ├── TaskRequestDto.java
│   │   └── TaskDependencyRequestDto.java
│   └── response/   # 응답 DTO
│       ├── ProjectResponseDto.java
│       ├── TaskResponseDto.java
│       ├── TaskDependencyResponseDto.java
│       ├── GanttChartDto.java
│       └── ApiResponse.java
└── exception/      # 예외 클래스들
    ├── ProjectNotFoundException.java
    ├── TaskNotFoundException.java
    └── InvalidRequestException.java
```

## 🔧 Development Setup
- **JDK**: IntelliJ IDEA Built-in JDK (Java 21)
- **Workflow**: GitHub Flow + Conventional Commits
- **Testing**: JUnit 5 + Mockito (✅ 모든 테스트 통과)
- **Migration**: Flyway 10.21.0
- **Validation**: Jakarta Bean Validation
- **Dependencies**: Spring Data JPA, Spring Web, Lombok, H2 Database

## 📚 Documentation Structure
프로젝트 문서는 다음과 같이 구성되어 있습니다:

### 📋 명세 및 설계 (`docs/specifications/`)
- **[기능 명세서](./docs/specifications/FUNCTIONAL_SPECS.md)** - 사용자 관점 페이지별 기능 목록
- **[API 명세서](./docs/specifications/API.md)** - REST API 설계 및 명세
- **[데이터베이스 설계](./docs/specifications/DATABASE.md)** - 데이터베이스 스키마 및 ERD

### 🗺️ 제품 로드맵 (`docs/`)
- **[제품 로드맵](./docs/PRODUCT_ROADMAP.md)** - 현재 기능 및 향후 개발 방향 가이드

### 🔍 분석 및 진단 (`docs/analysis/`)
- **[프로젝트 현재 상태](./docs/analysis/PROJECT_STATUS_2025-09-13.md)** - 최신 프로젝트 상태 및 성과 분석
- **[Archive](./docs/analysis/archive/)** - 과거 분석 보고서 및 디버그 로그

### 📖 개발 가이드 (`docs/guides/` & 계층별)
- **[전체 아키텍처](./docs/guides/ARCHITECTURE.md)** - 종합 개발 가이드 (아키텍처, 워크플로우, 코딩 표준)
- **[Controller 계층](./src/main/java/com/liam/gantt/controller/CLAUDE.md)** - Web/REST 컨트롤러 가이드
- **[Service 계층](./src/main/java/com/liam/gantt/service/CLAUDE.md)** - 비즈니스 로직 가이드
- **[Repository 계층](./src/main/java/com/liam/gantt/repository/CLAUDE.md)** - 데이터 액세스 가이드

> 📏 **문서 관리 규칙**: 문서가 1500줄 초과 시 분리 검토, 2000줄 초과 시 필수 분리  
> 자세한 내용: [문서 가이드라인](./docs/README.md)

## 📋 Current Status
- ✅ Spring Boot 프로젝트 설정 완료
- ✅ Claude Code Option 2 설정 완료 (팀 협업 구조)
- ✅ 데이터베이스 설계 완료
- ✅ Flyway 마이그레이션 설정 및 스크립트 생성 (V001~V005)
- ✅ JPA 엔티티 구현 완료 (Project, Task, TaskDependency)
- ✅ Repository 계층 구현 완료
- ✅ Service 계층 구현 완료 (ProjectService, TaskService, GanttService)
- ✅ DTO 및 Mapper 계층 구현 완료
- ✅ REST API 컨트롤러 완료 (ProjectController, TaskController, GanttController)
- ✅ Web 컨트롤러 완료 (HomeController, ProjectWebController, TaskWebController)
- ✅ 단위 테스트 완전 정규화 (107 테스트 중 모든 테스트 통과)
- ✅ 예외 처리 및 GlobalExceptionHandler 구현
- ✅ **백엔드 개발 완료**: 모든 핵심 기능 구현 및 테스트 통과
- 🔄 **현재 상태**: 프론트엔드 고도화 준비 완료

## 🌐 API Endpoints
### Project APIs
```
GET    /api/v1/projects        # 프로젝트 목록 조회
POST   /api/v1/projects        # 프로젝트 생성
GET    /api/v1/projects/{id}   # 프로젝트 상세 조회
PUT    /api/v1/projects/{id}   # 프로젝트 수정
DELETE /api/v1/projects/{id}   # 프로젝트 삭제
```

### Task APIs
```
GET    /api/v1/projects/{id}/tasks     # 프로젝트 태스크 목록
POST   /api/v1/projects/{id}/tasks     # 태스크 생성
GET    /api/v1/tasks/{id}              # 태스크 상세 조회
PUT    /api/v1/tasks/{id}              # 태스크 수정
DELETE /api/v1/tasks/{id}              # 태스크 삭제
```

### Gantt Chart APIs
```
GET    /api/v1/gantt/{projectId}       # 간트 차트 데이터 조회
POST   /api/v1/gantt/dependencies     # 태스크 의존성 추가
DELETE /api/v1/gantt/dependencies/{id} # 태스크 의존성 제거
```

### Web Pages
```
GET    /                              # 홈 페이지
GET    /projects                      # 프로젝트 목록 페이지
GET    /projects/{id}                 # 프로젝트 상세 페이지
GET    /projects/{id}/gantt           # 간트 차트 페이지
```

## 📊 Code Statistics
- **총 Java 파일**: 51개 (Main: 43개, Test: 8개)
- **Controller**: 5개 (REST API 3개 + Web 2개)
- **Service**: 6개 (인터페이스 3개 + 구현체 3개)
- **Repository**: 3개
- **Entity**: 7개 (BaseEntity + 3개 엔티티 + 3개 Enum)
- **DTO**: 7개 (Request 3개 + Response 4개)
- **테스트 커버리지**: 100% 통과 (107개 테스트)

## ⚡ Quick Tips
- **빌드 전 필수**: `/clean` → `/build` → `/test`
- **DB 변경시**: Flyway 마이그레이션 스크립트 작성 → `/migrate`
- **새 기능 개발**: Entity → Repository → Service → Controller 순서
- **API 테스트**: http://localhost:8080/api/v1/
- **H2 콘솔 접속**: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:gantt_dev) 

## 🔜 Next Steps (프론트엔드 개발)
1. **Thymeleaf 템플릿 확장**: 간트 차트 시각화
2. **JavaScript 라이브러리 통합**: D3.js 또는 Chart.js
3. **반응형 UI 개선**: Bootstrap 5 활용
4. **사용자 경험 최적화**: 드래그 앤 드롭, 실시간 업데이트
5. **React 전환 준비**: API 기반 SPA 구조

## 🆘 Troubleshooting
- 빌드 실패: `/clean` 후 재시도
- DB 이슈: `/migrate` 로 스키마 동기화  
- 포트 충돌: `./gradlew bootRun --args='--server.port=9090'`
- 테스트 실패: 모든 테스트는 현재 통과 상태