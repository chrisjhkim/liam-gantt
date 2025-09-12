-- V005: Insert sample data for testing and development

-- Sample projects
INSERT INTO projects (name, description, start_date, end_date, status, created_at, updated_at) VALUES
('웹사이트 리뉴얼 프로젝트', '회사 웹사이트 전면 리뉴얼 및 모던화 작업', '2025-01-01', '2025-04-30', 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('모바일 앱 개발', 'iOS/Android 하이브리드 모바일 앱 신규 개발', '2025-02-01', '2025-07-31', 'PLANNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('데이터 마이그레이션', '레거시 시스템에서 신규 플랫폼으로 데이터 이관', '2025-01-15', '2025-03-15', 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('사용자 교육 프로그램', '신규 시스템 도입을 위한 직원 교육 프로그램 운영', '2025-04-01', '2025-05-31', 'PLANNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Sample tasks for project 1 (웹사이트 리뉴얼)
INSERT INTO tasks (project_id, name, description, start_date, end_date, duration, progress, status, created_at, updated_at) VALUES
-- Phase 1: 분석 및 설계 (완료)
(1, '요구사항 분석', '고객 및 내부 이해관계자 요구사항 수집 및 분석', '2025-01-01', '2025-01-10', 10, 100.00, 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'UI/UX 설계', '와이어프레임 및 프로토타입 설계', '2025-01-11', '2025-01-25', 15, 100.00, 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '시스템 아키텍처 설계', '기술 스택 선정 및 시스템 구조 설계', '2025-01-15', '2025-01-30', 16, 100.00, 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Phase 2: 개발 (진행중)
(1, '프론트엔드 개발', 'React 기반 사용자 인터페이스 개발', '2025-01-31', '2025-03-15', 44, 65.00, 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '백엔드 API 개발', 'Spring Boot 기반 REST API 개발', '2025-02-01', '2025-03-10', 38, 75.00, 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '데이터베이스 구축', 'MariaDB 스키마 설계 및 구축', '2025-02-05', '2025-02-20', 16, 90.00, 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Phase 3: 테스트 (예정)
(1, '단위 테스트', '각 컴포넌트별 단위 테스트 작성 및 실행', '2025-03-11', '2025-03-25', 15, 0.00, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '통합 테스트', '시스템 전체 통합 테스트', '2025-03-21', '2025-04-05', 16, 0.00, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '사용자 승인 테스트', '고객사 사용자 승인 테스트 및 피드백 수집', '2025-04-01', '2025-04-15', 15, 0.00, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Phase 4: 배포 (예정)
(1, '운영 환경 구축', '운영 서버 환경 설정 및 배포 자동화', '2025-04-10', '2025-04-20', 11, 0.00, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '데이터 마이그레이션', '기존 데이터 신규 시스템으로 마이그레이션', '2025-04-16', '2025-04-25', 10, 0.00, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '최종 배포 및 모니터링', '라이브 서비스 배포 및 초기 운영 모니터링', '2025-04-26', '2025-04-30', 5, 0.00, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Sample tasks for project 2 (모바일 앱 개발)
INSERT INTO tasks (project_id, name, description, start_date, end_date, duration, progress, status, created_at, updated_at) VALUES
(2, '시장 조사 및 경쟁 분석', '타겟 시장 분석 및 경쟁사 앱 벤치마킹', '2025-02-01', '2025-02-14', 14, 0.00, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'MVP 기능 정의', '최소 기능 제품(MVP) 스코프 정의', '2025-02-10', '2025-02-24', 15, 0.00, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '디자인 시스템 구축', 'UI/UX 디자인 가이드라인 및 컴포넌트 라이브러리', '2025-02-25', '2025-03-15', 19, 0.00, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'React Native 개발', '크로스플랫폼 모바일 앱 개발', '2025-03-16', '2025-05-30', 76, 0.00, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '네이티브 모듈 개발', '플랫폼별 특화 기능 네이티브 모듈 개발', '2025-04-01', '2025-05-15', 45, 0.00, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '앱 스토어 등록', 'iOS App Store 및 Google Play Store 등록', '2025-06-01', '2025-06-30', 30, 0.00, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Sample tasks for project 3 (데이터 마이그레이션)
INSERT INTO tasks (project_id, name, description, start_date, end_date, duration, progress, status, created_at, updated_at) VALUES
(3, '데이터 분석', '기존 시스템 데이터 구조 및 품질 분석', '2025-01-15', '2025-01-25', 11, 100.00, 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'ETL 프로세스 설계', '데이터 추출, 변환, 로딩 프로세스 설계', '2025-01-26', '2025-02-05', 11, 80.00, 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '데이터 정제 및 변환', '데이터 품질 개선 및 신규 스키마에 맞게 변환', '2025-02-06', '2025-02-25', 20, 30.00, 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '테스트 마이그레이션', '소량 데이터로 마이그레이션 프로세스 검증', '2025-02-26', '2025-03-05', 8, 0.00, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '전체 마이그레이션 실행', '전체 데이터 마이그레이션 실행', '2025-03-06', '2025-03-10', 5, 0.00, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '데이터 검증', '마이그레이션된 데이터 정합성 검증', '2025-03-11', '2025-03-15', 5, 0.00, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task dependencies for project 1 (웹사이트 리뉴얼)
-- Get task IDs first (assuming sequential IDs starting from 1)
INSERT INTO task_dependencies (predecessor_id, successor_id, dependency_type, created_at) VALUES
-- Phase dependencies
(1, 2, 'FINISH_TO_START', CURRENT_TIMESTAMP), -- 요구사항 분석 → UI/UX 설계
(1, 3, 'FINISH_TO_START', CURRENT_TIMESTAMP), -- 요구사항 분석 → 시스템 아키텍처 설계
(2, 4, 'FINISH_TO_START', CURRENT_TIMESTAMP), -- UI/UX 설계 → 프론트엔드 개발
(3, 5, 'FINISH_TO_START', CURRENT_TIMESTAMP), -- 시스템 아키텍처 설계 → 백엔드 API 개발
(3, 6, 'FINISH_TO_START', CURRENT_TIMESTAMP), -- 시스템 아키텍처 설계 → 데이터베이스 구축
(5, 7, 'FINISH_TO_START', CURRENT_TIMESTAMP), -- 백엔드 API 개발 → 단위 테스트
(4, 8, 'FINISH_TO_START', CURRENT_TIMESTAMP), -- 프론트엔드 개발 → 통합 테스트
(7, 8, 'FINISH_TO_START', CURRENT_TIMESTAMP), -- 단위 테스트 → 통합 테스트
(8, 9, 'FINISH_TO_START', CURRENT_TIMESTAMP), -- 통합 테스트 → 사용자 승인 테스트
(9, 10, 'FINISH_TO_START', CURRENT_TIMESTAMP), -- 사용자 승인 테스트 → 운영 환경 구축
(6, 11, 'FINISH_TO_START', CURRENT_TIMESTAMP), -- 데이터베이스 구축 → 데이터 마이그레이션
(10, 12, 'FINISH_TO_START', CURRENT_TIMESTAMP), -- 운영 환경 구축 → 최종 배포
(11, 12, 'FINISH_TO_START', CURRENT_TIMESTAMP); -- 데이터 마이그레이션 → 최종 배포

-- Task dependencies for project 2 (모바일 앱)
INSERT INTO task_dependencies (predecessor_id, successor_id, dependency_type, created_at) VALUES
(13, 14, 'FINISH_TO_START', CURRENT_TIMESTAMP), -- 시장 조사 → MVP 기능 정의
(14, 15, 'FINISH_TO_START', CURRENT_TIMESTAMP), -- MVP 기능 정의 → 디자인 시스템 구축
(15, 16, 'FINISH_TO_START', CURRENT_TIMESTAMP), -- 디자인 시스템 → React Native 개발
(16, 17, 'START_TO_START', CURRENT_TIMESTAMP),  -- React Native 개발 || 네이티브 모듈 개발 (병렬)
(16, 18, 'FINISH_TO_START', CURRENT_TIMESTAMP), -- React Native 개발 → 앱 스토어 등록
(17, 18, 'FINISH_TO_START', CURRENT_TIMESTAMP); -- 네이티브 모듈 개발 → 앱 스토어 등록

-- Task dependencies for project 3 (데이터 마이그레이션)
INSERT INTO task_dependencies (predecessor_id, successor_id, dependency_type, created_at) VALUES
(19, 20, 'FINISH_TO_START', CURRENT_TIMESTAMP), -- 데이터 분석 → ETL 프로세스 설계
(20, 21, 'FINISH_TO_START', CURRENT_TIMESTAMP), -- ETL 프로세스 설계 → 데이터 정제 및 변환
(21, 22, 'FINISH_TO_START', CURRENT_TIMESTAMP), -- 데이터 정제 및 변환 → 테스트 마이그레이션
(22, 23, 'FINISH_TO_START', CURRENT_TIMESTAMP), -- 테스트 마이그레이션 → 전체 마이그레이션 실행
(23, 24, 'FINISH_TO_START', CURRENT_TIMESTAMP); -- 전체 마이그레이션 실행 → 데이터 검증