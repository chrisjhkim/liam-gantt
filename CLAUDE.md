# CLAUDE.md

Liam Gantt Chart Application - Claude Code 개발 가이드

> **✅ 프로젝트 상태**: 개발 완료 (테스트 및 문서화 완료)
> **📅 마지막 업데이트**: 2025-01-09
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

## 🏗️ Project Structure  
```
src/main/java/com/liam/gantt/
├── config/         # 설정 클래스 (JpaConfig)
├── controller/     # REST API + Web Controller (개발 예정)
├── service/        # 비즈니스 로직 (개발 중)
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
└── dto/            # 데이터 전송 객체 (개발 예정)
```

## 🔧 Development Setup
- **JDK**: IntelliJ IDEA Built-in JDK (Java 21)
- **Workflow**: GitHub Flow + Conventional Commits
- **Testing**: JUnit 5 + Mockito
- **Migration**: Flyway 10.21.0
- **Validation**: Jakarta Bean Validation
- **Dependencies**: Spring Data JPA, Spring Web, Lombok, H2 Database

## 📚 Detailed Documentation
상세한 개발 가이드는 다음 문서들을 참조하세요:

- **@docs/CLAUDE.md** - 종합 개발 가이드 (아키텍처, 워크플로우, 코딩 표준)
- **@docs/API.md** - REST API 설계 및 명세
- **@docs/DATABASE.md** - 데이터베이스 설계 및 스키마
- **@src/main/java/com/liam/gantt/controller/CLAUDE.md** - Controller 계층 가이드
- **@src/main/java/com/liam/gantt/service/CLAUDE.md** - Service 계층 가이드  
- **@src/main/java/com/liam/gantt/repository/CLAUDE.md** - Repository 계층 가이드

## 📋 Current Status
- ✅ Spring Boot 프로젝트 설정 완료
- ✅ Claude Code Option 2 설정 완료 (팀 협업 구조)
- ✅ 데이터베이스 설계 완료
- ✅ Flyway 마이그레이션 설정 및 스크립트 생성 (V001~V003)
- ✅ JPA 엔티티 구현 완료 (Project, Task, TaskDependency)
- ✅ Repository 계층 구현 완료
- ✅ Service 계층 구현 완료
- 🔄 **현재 작업**: DTO 및 Controller 구현 중

## ⚡ Quick Tips
- **빌드 전 필수**: `/clean` → `/build` → `/test`
- **DB 변경시**: Flyway 마이그레이션 스크립트 작성 → `/migrate`
- **새 기능 개발**: Entity → Repository → Service → Controller 순서
- **API 테스트**: http://localhost:8080/api/v1/
- **H2 콘솔 접속**: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:gantt_dev) 

## 🆘 Troubleshooting
- 빌드 실패: `/clean` 후 재시도
- DB 이슈: `/migrate` 로 스키마 동기화  
- 포트 충돌: `./gradlew bootRun --args='--server.port=9090'`