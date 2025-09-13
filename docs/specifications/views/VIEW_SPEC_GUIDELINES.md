# View Specification Guidelines
> View 명세 문서 작성 가이드라인

## 📋 개요
이 문서는 Liam Gantt 프로젝트의 View 명세서 작성 방법을 정의합니다.
Controller와 View 간의 데이터 계약을 명확히 하여 런타임 오류를 방지하고 개발 효율성을 높이는 것이 목표입니다.

## 🎯 작성 목적
1. **버그 방지**: Controller에서 누락된 Model 데이터로 인한 Thymeleaf 오류 방지
2. **개발 가이드**: 새로운 기능 추가 시 필요한 데이터 명확화
3. **유지보수**: View 수정 시 영향 범위 파악 용이
4. **AI 활용**: Claude 등 AI가 코드 수정 시 정확한 참조 가능

## 📝 문서 구조

### 1. 기본 형식
각 View 명세는 다음 구조를 따릅니다:

```markdown
## [View Name] (파일경로)

### Controller Mapping
- **URL**: 접근 경로
- **Method**: HTTP 메서드
- **Controller**: 컨트롤러 클래스#메서드명
- **Description**: 페이지 설명

### Required Model Attributes
| Attribute Name | Type | Required | Default | Description |
|---------------|------|----------|---------|-------------|
| attributeName | 타입 | Yes/No | 기본값 | 설명 |

### Page Elements
#### Links & Navigation
- 링크 목록과 목적지

#### Forms & Actions
- 폼과 액션 정보

#### Conditional Elements
- 조건부 표시 요소

### Dependencies
- 필요한 Fragment
- JavaScript 라이브러리
- CSS 요구사항
```

## 🔧 작성 규칙

### AI 친화적 작성
1. **구조화된 데이터**: 표, 리스트 형식 사용
2. **명확한 타입 정의**: Java 타입 명시
3. **필수/선택 구분**: Required 필드 명확히 표시
4. **경로 정보**: 파일 경로, URL 패턴 정확히 기재

### 가독성 고려
1. **시각적 구분**: 이모지, 구분선 활용
2. **계층 구조**: 들여쓰기로 관계 표현
3. **예시 포함**: 복잡한 데이터는 예시 제공
4. **간결한 설명**: 한 줄 설명 우선

## 📊 데이터 타입 표기법

### 기본 타입
- `String` - 문자열
- `Long` - ID 값
- `Integer` - 정수
- `Boolean` - true/false
- `LocalDate` - 날짜 (yyyy-MM-dd)
- `LocalDateTime` - 날짜시간

### 복합 타입
- `List<T>` - 리스트
- `Page<T>` - 페이징 데이터
- `Map<K,V>` - 맵
- DTO 클래스명 - 예: `ProjectResponseDto`

### Nullable 표기
- `String?` - null 가능
- `String!` - null 불가 (필수)

## 🔄 형식 변경 가능성

> **⚠️ 주의**: 현재 형식은 초기 버전입니다.
> 실제 사용 후 피드백에 따라 다음과 같은 변경이 있을 수 있습니다:
> - YAML/JSON 형식으로 전환
> - 자동 검증 도구 연동을 위한 형식 변경
> - 인간 친화적/AI 친화적 버전 분리
>
> 변경 시에는 이 가이드라인 문서를 먼저 업데이트합니다.

## 📁 파일 구조

```
docs/specifications/views/
├── VIEW_SPEC_GUIDELINES.md      # 이 문서 (작성 지침)
├── VIEW_SPECIFICATIONS.md       # 메인 인덱스
├── project-views.md            # 프로젝트 관련 View
├── task-views.md              # 태스크 관련 View
├── gantt-views.md             # 간트차트 관련 View
└── common-views.md            # 공통/홈 View
```

## ✅ 체크리스트

View 명세 작성 시 확인사항:
- [ ] Controller 메서드와 URL 매핑 정확성
- [ ] 모든 Model attribute 포함 여부
- [ ] Thymeleaf 표현식에서 참조하는 모든 필드
- [ ] 조건부 렌더링 요소의 조건 명시
- [ ] 페이지 이동 링크와 대상 URL
- [ ] 폼 전송 시 필요한 파라미터
- [ ] Fragment 의존성
- [ ] JavaScript가 필요로 하는 데이터

## 🔍 검증 방법

1. **수동 검증**: 실제 페이지 접속하여 오류 확인
2. **코드 리뷰**: Controller와 View 코드 대조
3. **AI 검증**: Claude에게 명세와 코드 일치 여부 확인 요청

## 📚 참고사항

- Thymeleaf 공식 문서: https://www.thymeleaf.org/
- Spring MVC Model: https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-ann-modelattrib
- 프로젝트 Controller 가이드: `/src/main/java/com/liam/gantt/controller/CLAUDE.md`