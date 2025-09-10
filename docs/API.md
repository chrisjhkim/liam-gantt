# API Design Document

Liam Gantt Chart Application REST API 설계 문서

## 🌐 API 개요
- **Base URL**: `http://localhost:8080/api/v1`
- **Content-Type**: `application/json`
- **Character Encoding**: UTF-8
- **API Version**: v1

## 📋 응답 형식
### 성공 응답
```json
{
  "status": "success",
  "data": {
    // 실제 데이터
  },
  "message": "Operation completed successfully",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 오류 응답
```json
{
  "status": "error",
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "입력값이 올바르지 않습니다.",
    "details": [
      {
        "field": "name",
        "message": "프로젝트명은 필수입니다."
      }
    ]
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## 🗂️ Project APIs

### 1. 프로젝트 목록 조회
```http
GET /api/v1/projects
```

**Parameters:**
- `page` (optional): 페이지 번호 (default: 0)
- `size` (optional): 페이지 크기 (default: 20)
- `sort` (optional): 정렬 기준 (default: "startDate,desc")

**Response:**
```json
{
  "status": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "웹사이트 리뉴얼 프로젝트",
        "description": "회사 웹사이트 전면 리뉴얼",
        "startDate": "2024-01-15",
        "endDate": "2024-03-15",
        "status": "IN_PROGRESS",
        "progress": 45.5,
        "taskCount": 12,
        "createdAt": "2024-01-10T09:00:00Z",
        "updatedAt": "2024-01-14T14:30:00Z"
      }
    ],
    "pageable": {
      "page": 0,
      "size": 20,
      "sort": "startDate,desc"
    },
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### 2. 프로젝트 상세 조회
```http
GET /api/v1/projects/{id}
```

**Response:**
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "name": "웹사이트 리뉴얼 프로젝트",
    "description": "회사 웹사이트 전면 리뉴얼",
    "startDate": "2024-01-15",
    "endDate": "2024-03-15",
    "status": "IN_PROGRESS",
    "progress": 45.5,
    "tasks": [
      {
        "id": 1,
        "name": "요구사항 분석",
        "startDate": "2024-01-15",
        "endDate": "2024-01-20",
        "progress": 100,
        "status": "COMPLETED"
      }
    ]
  }
}
```

### 3. 프로젝트 생성
```http
POST /api/v1/projects
```

**Request Body:**
```json
{
  "name": "새 프로젝트",
  "description": "프로젝트 설명",
  "startDate": "2024-02-01",
  "endDate": "2024-04-01"
}
```

**Response:**
```json
{
  "status": "success",
  "data": {
    "id": 2,
    "name": "새 프로젝트",
    "description": "프로젝트 설명",
    "startDate": "2024-02-01",
    "endDate": "2024-04-01",
    "status": "PLANNING",
    "progress": 0,
    "taskCount": 0,
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  }
}
```

### 4. 프로젝트 수정
```http
PUT /api/v1/projects/{id}
```

**Request Body:**
```json
{
  "name": "수정된 프로젝트명",
  "description": "수정된 설명",
  "startDate": "2024-02-01",
  "endDate": "2024-05-01"
}
```

### 5. 프로젝트 삭제
```http
DELETE /api/v1/projects/{id}
```

**Response:**
```json
{
  "status": "success",
  "message": "프로젝트가 성공적으로 삭제되었습니다."
}
```

## 📋 Task APIs

### 1. 프로젝트 태스크 목록 조회
```http
GET /api/v1/projects/{projectId}/tasks
```

**Response:**
```json
{
  "status": "success",
  "data": [
    {
      "id": 1,
      "name": "요구사항 분석",
      "description": "고객 요구사항 수집 및 분석",
      "startDate": "2024-01-15",
      "endDate": "2024-01-20",
      "duration": 5,
      "progress": 100,
      "status": "COMPLETED",
      "dependencies": [
        {
          "id": 2,
          "type": "FINISH_TO_START",
          "predecessorTaskId": 1,
          "successorTaskId": 2
        }
      ]
    }
  ]
}
```

### 2. 태스크 생성
```http
POST /api/v1/projects/{projectId}/tasks
```

**Request Body:**
```json
{
  "name": "UI 디자인",
  "description": "사용자 인터페이스 디자인 작업",
  "startDate": "2024-01-21",
  "endDate": "2024-01-30",
  "dependencies": [
    {
      "predecessorTaskId": 1,
      "type": "FINISH_TO_START"
    }
  ]
}
```

### 3. 태스크 수정
```http
PUT /api/v1/tasks/{id}
```

**Request Body:**
```json
{
  "name": "UI 디자인 수정",
  "progress": 75,
  "status": "IN_PROGRESS"
}
```

### 4. 태스크 삭제
```http
DELETE /api/v1/tasks/{id}
```

## 📊 Gantt Chart APIs

### 1. 간트 차트 데이터 조회
```http
GET /api/v1/projects/{projectId}/gantt
```

**Response:**
```json
{
  "status": "success",
  "data": {
    "project": {
      "id": 1,
      "name": "웹사이트 리뉴얼 프로젝트",
      "startDate": "2024-01-15",
      "endDate": "2024-03-15"
    },
    "tasks": [
      {
        "id": 1,
        "name": "요구사항 분석",
        "startDate": "2024-01-15",
        "endDate": "2024-01-20",
        "progress": 100,
        "status": "COMPLETED",
        "x": 0,
        "y": 0,
        "width": 5,
        "height": 20
      }
    ],
    "dependencies": [
      {
        "from": 1,
        "to": 2,
        "type": "FINISH_TO_START"
      }
    ],
    "timeline": {
      "startDate": "2024-01-15",
      "endDate": "2024-03-15",
      "totalDays": 60,
      "workingDays": 42
    }
  }
}
```

## 🔍 검색 APIs

### 1. 프로젝트 검색
```http
GET /api/v1/projects/search
```

**Parameters:**
- `name` (optional): 프로젝트명으로 검색
- `status` (optional): 상태별 필터링
- `startDate` (optional): 시작일 범위
- `endDate` (optional): 종료일 범위

**Example:**
```http
GET /api/v1/projects/search?name=웹사이트&status=IN_PROGRESS&startDate=2024-01-01
```

## 📈 통계 APIs

### 1. 프로젝트 통계
```http
GET /api/v1/statistics/projects
```

**Response:**
```json
{
  "status": "success",
  "data": {
    "totalProjects": 10,
    "activeProjects": 3,
    "completedProjects": 6,
    "overdueProjects": 1,
    "averageProgress": 67.5,
    "projectsByStatus": {
      "PLANNING": 2,
      "IN_PROGRESS": 3,
      "COMPLETED": 6,
      "ON_HOLD": 1
    }
  }
}
```

## 🚨 HTTP Status Codes

### 성공 응답
- `200 OK`: 조회, 수정 성공
- `201 Created`: 생성 성공  
- `204 No Content`: 삭제 성공

### 클라이언트 오류
- `400 Bad Request`: 잘못된 요청 데이터
- `404 Not Found`: 리소스를 찾을 수 없음
- `409 Conflict`: 중복된 리소스 (예: 프로젝트명)
- `422 Unprocessable Entity`: 유효하지 않은 비즈니스 로직

### 서버 오류
- `500 Internal Server Error`: 서버 내부 오류

## 🔧 에러 코드 정의

### 프로젝트 관련
- `PROJECT_NOT_FOUND`: 프로젝트를 찾을 수 없음
- `PROJECT_NAME_DUPLICATE`: 중복된 프로젝트명
- `PROJECT_DATE_INVALID`: 잘못된 프로젝트 날짜

### 태스크 관련
- `TASK_NOT_FOUND`: 태스크를 찾을 수 없음
- `TASK_DEPENDENCY_CYCLE`: 순환 의존성 감지
- `TASK_DATE_INVALID`: 잘못된 태스크 날짜

### 일반
- `VALIDATION_ERROR`: 입력값 검증 오류
- `INTERNAL_ERROR`: 서버 내부 오류

## 🔒 보안 (향후 구현)

### 인증 헤더
```http
Authorization: Bearer {JWT_TOKEN}
```

### 권한별 접근 제어
- **READ**: 프로젝트 조회
- **WRITE**: 프로젝트 생성, 수정
- **DELETE**: 프로젝트 삭제
- **ADMIN**: 모든 권한

## 📝 API 사용 예시

### JavaScript/Fetch 예시
```javascript
// 프로젝트 목록 조회
const response = await fetch('/api/v1/projects', {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json',
  }
});

const data = await response.json();
console.log(data.data.content); // 프로젝트 목록

// 프로젝트 생성
const newProject = await fetch('/api/v1/projects', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    name: '새 프로젝트',
    description: '프로젝트 설명',
    startDate: '2024-02-01',
    endDate: '2024-04-01'
  })
});

const created = await newProject.json();
console.log(created.data); // 생성된 프로젝트
```

## 📚 참고 자료
- RESTful API Design Best Practices
- HTTP Status Code 가이드
- JSON API Specification
- OpenAPI/Swagger Documentation