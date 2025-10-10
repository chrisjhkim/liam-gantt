# 🔍 확인 필요 사항

## 🟥 긴급 (작동에 영향)

## 🟨 중요 (개선 필요)

### [2025-09-11] Spring Batch 배치 처리 대안
- **위치**: ADR-001-tech-stack.md:18
- **상황**: Spring Batch 제거 후 배치 처리 방식 선택
- **불확실한 부분**: 대신 어떤 방식으로 배치 처리할지 (Spring Scheduler vs Quartz vs 별도 서비스)
- **내가 한 선택**: 일단 Spring Batch만 제거하고 대안은 TODO로 남김
- **확인 필요**: 배치 처리가 필요한 기능들과 대안 방식 결정
- **우선순위**: 🟨중요

### [2025-09-11] batch-server 프로젝트 필요성
- **위치**: TODO 리스트
- **상황**: Spring Batch 제거로 batch-server 프로젝트 생성 필요성 검토
- **불확실한 부분**: 별도 batch-server 프로젝트가 여전히 필요한지
- **내가 한 선택**: 일단 TODO 리스트에 남겨둠
- **확인 필요**: api-server 내에서 스케줄링으로 처리할지, 별도 프로젝트로 갈지
- **우선순위**: 🟨중요

### [2025-09-11] Mobile 기술 스택 문서 정리
- **위치**: workspace/2-architecture/README.md:99-114, infrastructure/README.md:92-105
- **상황**: ADR-001에서 Mobile 관련 내용 제거했는데 architecture 문서들에는 남아있음
- **불확실한 부분**: Mobile 관련 내용을 완전 제거할지, 아니면 TBD로 표시할지
- **내가 한 선택**: 일단 현상 파악하고 TODO_CLARIFY에 기록
- **확인 필요**: Mobile 개발 시점과 architecture 문서 정리 방향
- **우선순위**: 🟨중요

### [2025-09-11] Deployment 전략 문서 보완
- **위치**: infrastructure/README.md:59-61, 128-155
- **상황**: ADR-001에 Wildfly vs jar배포 결정 사항 추가되었는데 infrastructure 문서에 반영 필요
- **불확실한 부분**: infrastructure 문서의 배포 전략 섹션을 어떻게 보완할지
- **내가 한 선택**: 현재 문서 상태 유지
- **확인 필요**: 운영팀 정책 기반 Wildfly 배포의 구체적인 장점과 제약사항
- **우선순위**: 🟨중요

## 🟦 나중에 (마이너)

---

## 해결된 항목 (DECISIONS.md로 이동 예정)