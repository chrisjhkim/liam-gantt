-- V006: Insert sample data for testing

-- 샘플 프로젝트 생성
INSERT INTO projects (id, name, description, start_date, end_date, status, created_at, updated_at) VALUES
(1, '웹사이트 리뉴얼 프로젝트', '기존 웹사이트를 최신 기술 스택으로 완전히 새롭게 구축하는 프로젝트입니다.', '2025-09-01', '2025-11-30', 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '모바일 앱 개발', '안드로이드 및 iOS용 모바일 애플리케이션 개발 프로젝트입니다.', '2025-10-01', '2025-12-31', 'PLANNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 웹사이트 리뉴얼 프로젝트의 태스크들
INSERT INTO tasks (id, project_id, parent_task_id, name, description, start_date, end_date, duration, progress, status, created_at, updated_at) VALUES
-- Phase 1: 기획 및 설계
(1, 1, NULL, '요구사항 분석', '고객 요구사항 수집 및 분석', '2025-09-01', '2025-09-07', 7, 100, 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, NULL, 'UI/UX 디자인', '웹사이트 사용자 인터페이스 및 경험 설계', '2025-09-08', '2025-09-21', 14, 85, 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, NULL, '데이터베이스 설계', '데이터베이스 스키마 및 구조 설계', '2025-09-15', '2025-09-28', 14, 70, 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Phase 2: 개발
(4, 1, NULL, '백엔드 개발', '서버 사이드 로직 및 API 개발', '2025-09-22', '2025-10-19', 28, 40, 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 1, NULL, '프론트엔드 개발', '클라이언트 사이드 UI 구현', '2025-10-01', '2025-10-28', 28, 20, 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 1, NULL, '데이터베이스 구축', '실제 데이터베이스 환경 구축 및 마이그레이션', '2025-10-15', '2025-10-21', 7, 0, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Phase 3: 테스트 및 배포
(7, 1, NULL, '통합 테스트', '전체 시스템 통합 테스트', '2025-10-22', '2025-11-04', 14, 0, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 1, NULL, '성능 최적화', '웹사이트 성능 튜닝 및 최적화', '2025-11-05', '2025-11-11', 7, 0, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 1, NULL, '사용자 테스트', '실제 사용자를 대상으로 한 베타 테스트', '2025-11-12', '2025-11-18', 7, 0, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 1, NULL, '배포 및 런칭', '운영 환경 배포 및 서비스 런칭', '2025-11-19', '2025-11-25', 7, 0, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 추가 세부 태스크들 (상위 태스크의 하위 태스크)
(11, 1, 2, '와이어프레임 작성', 'UI 구조 및 레이아웃 와이어프레임 작성', '2025-09-08', '2025-09-12', 5, 100, 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 1, 2, '비주얼 디자인', '실제 디자인 및 스타일 가이드 작성', '2025-09-13', '2025-09-21', 9, 70, 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 모바일 앱 개발 프로젝트의 태스크들
(13, 2, NULL, '시장 조사', '모바일 앱 시장 및 경쟁사 분석', '2025-10-01', '2025-10-07', 7, 0, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 2, NULL, '기능 명세서 작성', '앱의 핵심 기능 및 사용자 스토리 정의', '2025-10-08', '2025-10-14', 7, 0, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 2, NULL, '프로토타입 개발', 'MVP 버전의 프로토타입 개발', '2025-10-15', '2025-11-15', 32, 0, 'NOT_STARTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);