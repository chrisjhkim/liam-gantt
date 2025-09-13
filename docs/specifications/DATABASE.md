# Database Design Document

Liam Gantt Chart Application 데이터베이스 설계 문서

## 🗄️ 데이터베이스 개요
- **개발 환경**: H2 Database (In-Memory)
- **운영 환경**: MariaDB 10.5+
- **마이그레이션 도구**: Flyway
- **ORM**: Spring Data JPA + Hibernate

## 📊 ERD (Entity Relationship Diagram)
```
┌─────────────────┐       ┌─────────────────┐
│    Projects     │       │     Tasks       │
├─────────────────┤       ├─────────────────┤
│ id (PK)         │◄─────►│ id (PK)         │
│ name            │   1:N │ project_id (FK) │
│ description     │       │ name            │
│ start_date      │       │ description     │
│ end_date        │       │ start_date      │
│ status          │       │ end_date        │
│ created_at      │       │ duration        │
│ updated_at      │       │ progress        │
└─────────────────┘       │ status          │
                          │ parent_task_id  │
                          │ created_at      │
                          │ updated_at      │
                          └─────────────────┘
                                   │
                                   │ N:N
                                   ▼
                          ┌─────────────────┐
                          │ TaskDependencies│
                          ├─────────────────┤
                          │ id (PK)         │
                          │ predecessor_id  │
                          │ successor_id    │
                          │ dependency_type │
                          │ created_at      │
                          └─────────────────┘
```

## 📋 테이블 구조

### 1. Projects (프로젝트)
```sql
CREATE TABLE projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_project_dates CHECK (end_date >= start_date),
    INDEX idx_projects_status (status),
    INDEX idx_projects_dates (start_date, end_date),
    INDEX idx_projects_name (name)
);
```

**컬럼 설명:**
- `id`: 프로젝트 고유 식별자
- `name`: 프로젝트명 (최대 200자, 고유값)
- `description`: 프로젝트 설명 (TEXT 타입)
- `start_date`: 프로젝트 시작일
- `end_date`: 프로젝트 종료일
- `status`: 프로젝트 상태 (PLANNING, IN_PROGRESS, COMPLETED, ON_HOLD, CANCELLED)
- `created_at`: 생성일시
- `updated_at`: 최종 수정일시

**제약 조건:**
- `end_date`는 `start_date`보다 같거나 늦어야 함
- 프로젝트명은 고유해야 함

### 2. Tasks (태스크)
```sql
CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    parent_task_id BIGINT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    duration INT NOT NULL,
    progress DECIMAL(5,2) DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_task_id) REFERENCES tasks(id) ON DELETE SET NULL,
    
    CONSTRAINT chk_task_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_task_progress CHECK (progress >= 0 AND progress <= 100),
    CONSTRAINT chk_task_duration CHECK (duration > 0),
    
    INDEX idx_tasks_project (project_id),
    INDEX idx_tasks_parent (parent_task_id),
    INDEX idx_tasks_dates (start_date, end_date),
    INDEX idx_tasks_status (status)
);
```

**컬럼 설명:**
- `id`: 태스크 고유 식별자
- `project_id`: 소속 프로젝트 ID (외래키)
- `parent_task_id`: 상위 태스크 ID (계층 구조 지원)
- `name`: 태스크명 (최대 200자)
- `description`: 태스크 설명
- `start_date`: 태스크 시작일
- `end_date`: 태스크 종료일
- `duration`: 작업 기간 (일 단위)
- `progress`: 진행률 (0.00 ~ 100.00)
- `status`: 태스크 상태 (NOT_STARTED, IN_PROGRESS, COMPLETED, ON_HOLD, CANCELLED)

**제약 조건:**
- 진행률은 0~100% 사이
- 작업 기간은 양수
- `end_date`는 `start_date`보다 같거나 늦어야 함

### 3. Task_Dependencies (태스크 의존성)
```sql
CREATE TABLE task_dependencies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    predecessor_id BIGINT NOT NULL,
    successor_id BIGINT NOT NULL,
    dependency_type VARCHAR(20) NOT NULL DEFAULT 'FINISH_TO_START',
    lag_days INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (predecessor_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (successor_id) REFERENCES tasks(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_task_dependency (predecessor_id, successor_id),
    CONSTRAINT chk_no_self_dependency CHECK (predecessor_id != successor_id),
    
    INDEX idx_dependencies_predecessor (predecessor_id),
    INDEX idx_dependencies_successor (successor_id)
);
```

**컬럼 설명:**
- `id`: 의존성 관계 고유 식별자
- `predecessor_id`: 선행 태스크 ID
- `successor_id`: 후행 태스크 ID
- `dependency_type`: 의존성 유형
  - `FINISH_TO_START` (FS): 선행 작업 완료 → 후행 작업 시작
  - `START_TO_START` (SS): 선행 작업 시작 → 후행 작업 시작
  - `FINISH_TO_FINISH` (FF): 선행 작업 완료 → 후행 작업 완료
  - `START_TO_FINISH` (SF): 선행 작업 시작 → 후행 작업 완료
- `lag_days`: 지연 기간 (일 단위, 음수 가능)

**제약 조건:**
- 동일한 태스크 간 중복 의존성 방지
- 자기 자신에 대한 의존성 방지

## 🔍 인덱스 전략
### 성능 최적화를 위한 인덱스
```sql
-- 프로젝트 검색용
CREATE INDEX idx_projects_name_status ON projects(name, status);
CREATE INDEX idx_projects_date_range ON projects(start_date, end_date, status);

-- 태스크 검색용
CREATE INDEX idx_tasks_project_status ON tasks(project_id, status);
CREATE INDEX idx_tasks_date_progress ON tasks(start_date, end_date, progress);

-- 의존성 검색용 (순환 의존성 체크)
CREATE INDEX idx_dependencies_both ON task_dependencies(predecessor_id, successor_id);
```

## 📈 뷰 (Views)
### 1. 프로젝트 요약 뷰
```sql
CREATE VIEW project_summary AS
SELECT 
    p.id,
    p.name,
    p.description,
    p.start_date,
    p.end_date,
    p.status,
    COUNT(t.id) as task_count,
    COALESCE(AVG(t.progress), 0) as avg_progress,
    SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_tasks,
    SUM(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as active_tasks
FROM projects p
LEFT JOIN tasks t ON p.id = t.project_id
GROUP BY p.id, p.name, p.description, p.start_date, p.end_date, p.status;
```

### 2. 태스크 계층 뷰 (재귀 CTE)
```sql
WITH RECURSIVE task_hierarchy AS (
    -- 루트 태스크들
    SELECT 
        id,
        project_id,
        parent_task_id,
        name,
        0 as level,
        CAST(id AS CHAR(1000)) as path
    FROM tasks 
    WHERE parent_task_id IS NULL
    
    UNION ALL
    
    -- 하위 태스크들
    SELECT 
        t.id,
        t.project_id,
        t.parent_task_id,
        t.name,
        th.level + 1,
        CONCAT(th.path, '->', t.id)
    FROM tasks t
    INNER JOIN task_hierarchy th ON t.parent_task_id = th.id
)
SELECT * FROM task_hierarchy;
```

## 🔧 Flyway 마이그레이션 스크립트
### V001__Create_projects_table.sql
```sql
CREATE TABLE projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_project_dates CHECK (end_date >= start_date)
);

CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_dates ON projects(start_date, end_date);
CREATE INDEX idx_projects_name ON projects(name);
```

### V002__Create_tasks_table.sql
```sql
CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    parent_task_id BIGINT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    duration INT NOT NULL,
    progress DECIMAL(5,2) DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_task_id) REFERENCES tasks(id) ON DELETE SET NULL,
    
    CONSTRAINT chk_task_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_task_progress CHECK (progress >= 0 AND progress <= 100),
    CONSTRAINT chk_task_duration CHECK (duration > 0)
);

CREATE INDEX idx_tasks_project ON tasks(project_id);
CREATE INDEX idx_tasks_parent ON tasks(parent_task_id);
CREATE INDEX idx_tasks_dates ON tasks(start_date, end_date);
CREATE INDEX idx_tasks_status ON tasks(status);
```

### V003__Create_task_dependencies_table.sql
```sql
CREATE TABLE task_dependencies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    predecessor_id BIGINT NOT NULL,
    successor_id BIGINT NOT NULL,
    dependency_type VARCHAR(20) NOT NULL DEFAULT 'FINISH_TO_START',
    lag_days INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (predecessor_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (successor_id) REFERENCES tasks(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_task_dependency (predecessor_id, successor_id),
    CONSTRAINT chk_no_self_dependency CHECK (predecessor_id != successor_id)
);

CREATE INDEX idx_dependencies_predecessor ON task_dependencies(predecessor_id);
CREATE INDEX idx_dependencies_successor ON task_dependencies(successor_id);
```

## 🧪 샘플 데이터
### 개발/테스트용 데이터 삽입
```sql
-- 프로젝트 데이터
INSERT INTO projects (name, description, start_date, end_date, status) VALUES
('웹사이트 리뉴얼', '회사 웹사이트 전면 리뉴얼 프로젝트', '2024-01-15', '2024-03-15', 'IN_PROGRESS'),
('모바일 앱 개발', 'iOS/Android 모바일 앱 신규 개발', '2024-02-01', '2024-05-01', 'PLANNING');

-- 태스크 데이터
INSERT INTO tasks (project_id, name, description, start_date, end_date, duration, progress, status) VALUES
(1, '요구사항 분석', '고객 요구사항 수집 및 분석', '2024-01-15', '2024-01-20', 5, 100.00, 'COMPLETED'),
(1, 'UI/UX 디자인', '사용자 인터페이스 설계', '2024-01-21', '2024-02-05', 10, 75.00, 'IN_PROGRESS'),
(1, '프론트엔드 개발', 'HTML/CSS/JavaScript 개발', '2024-02-06', '2024-02-25', 15, 0.00, 'NOT_STARTED');

-- 의존성 데이터
INSERT INTO task_dependencies (predecessor_id, successor_id, dependency_type) VALUES
(1, 2, 'FINISH_TO_START'),
(2, 3, 'FINISH_TO_START');
```

## 📊 성능 모니터링 쿼리
### 슬로우 쿼리 분석
```sql
-- 프로젝트별 태스크 수 조회 (성능 테스트)
SELECT 
    p.name,
    COUNT(t.id) as task_count,
    AVG(t.progress) as avg_progress
FROM projects p
LEFT JOIN tasks t ON p.id = t.project_id
GROUP BY p.id, p.name
ORDER BY task_count DESC;

-- 복잡한 의존성 체인 조회
WITH RECURSIVE dependency_chain AS (
    SELECT predecessor_id, successor_id, 1 as depth
    FROM task_dependencies
    WHERE predecessor_id = ?
    
    UNION ALL
    
    SELECT dc.predecessor_id, td.successor_id, dc.depth + 1
    FROM dependency_chain dc
    JOIN task_dependencies td ON dc.successor_id = td.predecessor_id
    WHERE dc.depth < 10
)
SELECT * FROM dependency_chain;
```

## 🔒 데이터베이스 보안
### 사용자 권한 관리 (운영 환경)
```sql
-- 애플리케이션용 사용자 생성
CREATE USER 'gantt_user'@'localhost' IDENTIFIED BY 'secure_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON gantt.* TO 'gantt_user'@'localhost';

-- 읽기 전용 사용자 (리포트용)
CREATE USER 'gantt_reader'@'localhost' IDENTIFIED BY 'reader_password';
GRANT SELECT ON gantt.* TO 'gantt_reader'@'localhost';
```

## 📚 참고 자료
- Flyway Migration Guide
- MariaDB Performance Tuning
- JPA/Hibernate Best Practices
- Database Index Design Principles