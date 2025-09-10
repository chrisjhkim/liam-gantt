# Liam Gantt Chart Application 📊

간트 차트 기반 프로젝트 관리 웹 애플리케이션

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-green.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 🚀 프로젝트 소개

> **🔄 프로젝트 상태**: MVP 개발 중 (백엔드 90% 완료, 프론트엔드 개발 예정)  
> **📅 마지막 업데이트**: 2025-01-15  
> **🔧 개발 환경**: Java 21 + Spring Boot 3.5.5 + Gradle + H2 Database  

Liam Gantt는 프로젝트와 태스크를 체계적으로 관리하고, 직관적인 간트 차트로 시각화할 수 있는 웹 애플리케이션입니다. 
개인 또는 팀 프로젝트의 일정 계획, 진행률 추적, 태스크 간 의존성 관리를 효율적으로 수행할 수 있습니다.

### 주요 기능 ✨

- **프로젝트 관리**: 프로젝트 생성, 수정, 삭제 및 상태 추적
- **태스크 관리**: 계층적 태스크 구조 및 의존성 관리
- **간트 차트**: 실시간 간트 차트 시각화 및 진행률 표시
- **진행률 추적**: 프로젝트 및 태스크별 상세 진행률 모니터링
- **검색 및 필터링**: 프로젝트/태스크 검색 및 다양한 조건별 필터링
- **반응형 디자인**: 모바일, 태블릿, 데스크톱 지원

## 🛠 기술 스택

### Backend
- **Java 21** - 최신 LTS 버전
- **Spring Boot 3.5.5** - 웹 프레임워크
- **Spring Data JPA** - 데이터 액세스
- **Spring Validation** - 입력값 검증
- **H2 Database** - 개발 환경 (MariaDB 운영 예정)
- **Flyway** - 데이터베이스 마이그레이션
- **MapStruct** - 객체 매핑
- **Lombok** - 보일러플레이트 코드 제거

### Frontend
- **Thymeleaf** - 서버 사이드 템플릿 엔진
- **Bootstrap 5** - UI 프레임워크
- **Chart.js** - 간트 차트 시각화
- **JavaScript ES6+** - 클라이언트 사이드 로직

### Development & Testing
- **Gradle** - 빌드 도구
- **JUnit 5** - 단위 테스트
- **Mockito** - 모킹 프레임워크
- **Spring Boot Test** - 통합 테스트
- **SLF4J + Logback** - 로깅

## 📁 프로젝트 구조

```
src/main/java/com/liam/gantt/
├── controller/          # REST API 및 웹 컨트롤러
│   ├── api/v1/         # REST API 컨트롤러
│   └── web/            # 웹 페이지 컨트롤러
├── service/            # 비즈니스 로직
│   └── impl/           # 서비스 구현체
├── repository/         # 데이터 액세스 계층
├── entity/             # JPA 엔티티
│   └── enums/          # 열거형
├── dto/                # 데이터 전송 객체
│   ├── request/        # 요청 DTO
│   └── response/       # 응답 DTO
├── mapper/             # 매퍼 인터페이스
├── exception/          # 예외 클래스
└── config/             # 설정 클래스

src/main/resources/
├── templates/          # Thymeleaf 템플릿
├── static/             # 정적 리소스 (CSS, JS, 이미지)
├── db/migration/       # Flyway 마이그레이션 스크립트
└── application.yml     # 애플리케이션 설정
```

## 🚀 시작하기

### 사전 요구사항
- Java 21 이상
- Git

### 설치 및 실행

1. **프로젝트 클론**
   ```bash
   git clone https://github.com/liam/liam-gantt.git
   cd liam-gantt
   ```

2. **애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```

3. **웹 브라우저에서 접속**
   ```
   http://localhost:8080
   ```

### 빌드 및 테스트

```bash
# 전체 빌드 및 테스트
./gradlew build

# 테스트만 실행
./gradlew test

# 빌드 산출물 정리
./gradlew clean
```

## 📖 API 문서

### 주요 REST API 엔드포인트

#### 프로젝트 API
```http
GET    /api/v1/projects           # 프로젝트 목록 조회
POST   /api/v1/projects           # 프로젝트 생성
GET    /api/v1/projects/{id}      # 프로젝트 상세 조회
PUT    /api/v1/projects/{id}      # 프로젝트 수정
DELETE /api/v1/projects/{id}      # 프로젝트 삭제
```

#### 태스크 API
```http
GET    /api/v1/projects/{id}/tasks     # 프로젝트 태스크 목록
POST   /api/v1/projects/{id}/tasks     # 태스크 생성
GET    /api/v1/tasks/{id}              # 태스크 상세 조회
PUT    /api/v1/tasks/{id}              # 태스크 수정
DELETE /api/v1/tasks/{id}              # 태스크 삭제
```

#### 간트 차트 API
```http
GET    /api/v1/projects/{id}/gantt     # 간트 차트 데이터 조회
```

자세한 API 명세는 [API 문서](docs/API.md)를 참조하세요.

## 🗄️ 데이터베이스

애플리케이션은 H2 인메모리 데이터베이스를 사용하여 개발 환경에서 쉽게 실행할 수 있습니다.

### 마이그레이션 상태
- ✅ **V001**: Projects 테이블 생성
- ✅ **V002**: Tasks 테이블 생성  
- ✅ **V003**: Task Dependencies 테이블 생성
- ✅ **V004**: Task Dependencies 테이블에 updated_at 컬럼 추가

### H2 콘솔 접속
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:gantt_dev`
- Username: `sa`
- Password: (비어있음)

자세한 데이터베이스 설계는 [데이터베이스 문서](docs/DATABASE.md)를 참조하세요.

## 📋 현재 구현 상태

### ✅ 완료된 기능
- **데이터베이스 설계**: Flyway 마이그레이션 (V001~V004) 완료
- **엔티티 계층**: Project, Task, TaskDependency 엔티티 및 Enum 완료
- **Repository 계층**: Spring Data JPA Repository 완료
- **Service 계층**: 비즈니스 로직 및 트랜잭션 관리 완료
- **Controller 계층**: REST API Controller 완료
- **DTO 계층**: Request/Response DTO 및 매퍼 완료
- **예외 처리**: GlobalExceptionHandler 및 커스텀 예외 완료

### 🔄 현재 작업 중
- **통합 테스트**: Repository 및 Service 계층 테스트 작성
- **API 검증**: REST API 엔드포인트 테스트

### 📅 다음 단계 (예정)
- **웹 UI 개발**: Thymeleaf 템플릿 및 Bootstrap 5 스타일링
- **간트 차트 시각화**: Chart.js를 활용한 간트 차트 구현
- **사용자 인증**: Spring Security 기반 인증 시스템
- **React 전환**: 점진적 프론트엔드 모던화

## 🧪 테스트

프로젝트는 포괄적인 테스트 커버리지를 제공합니다:

- **단위 테스트**: Service, Controller, Mapper 계층
- **통합 테스트**: Repository 계층 (JPA)
- **API 테스트**: REST API 엔드포인트

```bash
# 모든 테스트 실행
./gradlew test

# 테스트 리포트 확인
./gradlew test --continuous
```

## 📚 개발 가이드

### 코딩 컨벤션
- **Java**: Google Java Style Guide 준수
- **패키지 구조**: 기능별 계층화 아키텍처
- **커밋 메시지**: Conventional Commits 규칙 준수

### 개발 워크플로우
1. 기능별 브랜치 생성 (`feature/기능명`)
2. 개발 및 테스트 작성
3. 코드 리뷰 후 메인 브랜치 병합

자세한 가이드는 각 계층별 문서를 참조하세요:
- [Controller 가이드](src/main/java/com/liam/gantt/controller/CLAUDE.md)
- [Service 가이드](src/main/java/com/liam/gantt/service/CLAUDE.md)
- [Repository 가이드](src/main/java/com/liam/gantt/repository/CLAUDE.md)

## 🚀 배포

### JAR 파일 생성
```bash
./gradlew bootJar
```

### Docker 실행 (향후 지원 예정)
```bash
docker build -t liam-gantt .
docker run -p 8080:8080 liam-gantt
```

## 🤝 기여하기

1. 프로젝트 포크
2. 기능 브랜치 생성 (`git checkout -b feature/AmazingFeature`)
3. 변경사항 커밋 (`git commit -m 'Add some AmazingFeature'`)
4. 브랜치에 푸시 (`git push origin feature/AmazingFeature`)
5. Pull Request 생성

## 📝 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 📞 연락처

Liam - liam@example.com

프로젝트 링크: [https://github.com/liam/liam-gantt](https://github.com/liam/liam-gantt)

## 📈 로드맵

### v1.0 MVP (2025 Q1) - 현재 진행 중
- [x] ~~데이터베이스 설계 및 마이그레이션~~
- [x] ~~백엔드 REST API 구현~~
- [ ] 웹 UI 및 간트 차트 시각화
- [ ] 기본 CRUD 기능 완성
- [ ] 초기 배포 환경 구성

### v1.1 (2025 Q2)
- [ ] 사용자 인증 및 권한 관리
- [ ] 프로젝트 공유 및 협업 기능
- [ ] 태스크 댓글 및 첨부파일
- [ ] 고급 필터링 및 검색

### v1.2 (2025 Q3)
- [ ] React 기반 프론트엔드 전환
- [ ] 실시간 알림 기능
- [ ] 모바일 반응형 최적화
- [ ] PWA 지원

### v2.0 (2025 Q4)
- [ ] 고급 리포팅 기능
- [ ] 프로젝트 템플릿
- [ ] 외부 도구 연동 (Slack, Jira)
- [ ] 모바일 앱 개발

---

⭐ 이 프로젝트가 도움이 되었다면 별표를 눌러주세요!