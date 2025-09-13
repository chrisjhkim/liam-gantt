# 📚 Liam Gantt Chart 프로젝트 문서 가이드

## 📋 문서 구조

```
docs/
├── README.md                    # 이 파일 - 문서 구조 및 가이드라인
├── specifications/              # 기능 명세 및 요구사항
│   ├── FUNCTIONAL_SPECS.md     # 사용자 기능 명세서
│   ├── API.md                  # REST API 명세
│   └── DATABASE.md             # 데이터베이스 설계
├── analysis/                   # 분석 및 진단 리포트
│   ├── CURRENT_ISSUES.md       # 현재 이슈 분석
│   ├── DEBUG_REPORTS/          # 디버깅 리포트들
│   └── PERFORMANCE_ANALYSIS.md # 성능 분석 (필요시)
├── guides/                     # 개발 가이드
│   ├── ARCHITECTURE.md         # 전체 아키텍처 가이드
│   ├── DEVELOPMENT.md          # 개발 환경 설정
│   └── DEPLOYMENT.md           # 배포 가이드
└── CLAUDE.md                   # Claude Code 개발 가이드 (메인)
```

## 📏 문서 작성 가이드라인

### 📊 문서 크기 관리
- **⚠️ 주의 (1500줄 초과)**: 문서 분리 검토 필요
- **🚨 필수 (2000줄 초과)**: 문서를 반드시 분리해야 함

### 📝 문서 분리 방법
#### 1500줄 이상 문서 처리:
1. **기능별 분리**: 관련 기능끼리 그룹화
2. **계층별 분리**: Frontend/Backend, API/UI 등
3. **단계별 분리**: 설계/구현/테스트 등

#### 2000줄 초과 시 필수 작업:
1. **즉시 분리**: 더 이상 작성 금지
2. **인덱스 문서 생성**: 분리된 문서들의 목차 역할
3. **상호 참조 업데이트**: 링크 및 참조 정보 수정

### 🔗 문서 간 참조 방식
```markdown
# 절대 경로 (권장)
[API 명세서](./specifications/API.md)
[현재 이슈 분석](./analysis/CURRENT_ISSUES.md)

# 상대 경로 (같은 디렉토리 내)
[데이터베이스 설계](./DATABASE.md)
```

### 📂 문서 분류 기준

#### `specifications/` - 명세 및 설계
- 기능 요구사항 명세서
- API 인터페이스 문서
- 데이터베이스 스키마 설계
- UI/UX 디자인 가이드

#### `analysis/` - 분석 및 진단
- 현재 상태 분석 리포트
- 성능 분석 결과
- 버그 리포트 및 디버깅 로그
- 코드 품질 분석

#### `guides/` - 가이드 및 매뉴얼
- 개발 환경 설정 가이드
- 아키텍처 설명서
- 배포 매뉴얼
- 트러블슈팅 가이드

## 🔄 문서 관리 규칙

### 📅 정기 검토
- **월 1회**: 모든 문서 줄 수 체크
- **분기 1회**: 문서 구조 최적화 검토
- **연 2회**: 전체 문서 아키텍처 리뷰

### 📝 명명 규칙
- **대문자 + 언더스코어**: `FUNCTIONAL_SPECS.md`
- **카테고리 접두사**: `API_`, `DB_`, `UI_` 등 (필요시)
- **버전 관리**: `_v1.md`, `_v2.md` (주요 개정시)

### 🏷️ 문서 상태 라벨
```markdown
**상태**: 📝 작성중 | ✅ 완료 | 🔄 검토중 | ⚠️ 업데이트 필요
**최종 업데이트**: YYYY-MM-DD
**작성자**: [작성자명]
**검토자**: [검토자명]
```

## 🛠️ 실시간 문서 크기 모니터링

### ⚡ 문서 수정 시 필수 체크
**문서를 수정할 때마다 반드시 실행해야 할 명령어:**

```bash
# 현재 수정 중인 파일 크기 체크
wc -l docs/path/to/your/modified/file.md

# 전체 문서 크기 한눈에 보기
find docs/ -name "*.md" -exec wc -l {} + | sort -nr

# ⚠️ 1500줄 초과 파일 경고 (분리 검토 필요)
find docs/ -name "*.md" -exec wc -l {} + | awk '$1 > 1500 {print "⚠️  " $2 " : " $1 " lines - REVIEW NEEDED"}'

# 🚨 2000줄 초과 파일 에러 (필수 분리)
find docs/ -name "*.md" -exec wc -l {} + | awk '$1 > 2000 {print "🚨 " $2 " : " $1 " lines - MUST SPLIT NOW"}'
```

### 🔄 문서 수정 워크플로우
1. **문서 수정 후** → 줄 수 체크 실행
2. **1500줄 초과 시** → 분리 계획 즉시 수립
3. **2000줄 초과 시** → 커밋 전 필수 분리

### 🚨 Git Hook (필수)
**`.git/hooks/pre-commit` 파일에 추가:**
```bash
#!/bin/bash
echo "📏 문서 크기 체크 중..."

# 2000줄 초과 파일 검사
oversized_files=$(find docs/ -name "*.md" -exec wc -l {} + | awk '$1 > 2000 {print $2 " (" $1 " lines)"}')

if [ ! -z "$oversized_files" ]; then
    echo "🚨 Error: 다음 문서들이 2000줄을 초과했습니다:"
    echo "$oversized_files"
    echo ""
    echo "커밋하기 전에 반드시 문서를 분리해주세요!"
    echo "가이드라인: docs/README.md"
    exit 1
fi

# 1500줄 초과 파일 경고
warning_files=$(find docs/ -name "*.md" -exec wc -l {} + | awk '$1 > 1500 && $1 <= 2000 {print $2 " (" $1 " lines)"}')

if [ ! -z "$warning_files" ]; then
    echo "⚠️  Warning: 다음 문서들이 1500줄을 초과했습니다:"
    echo "$warning_files"
    echo "분리를 검토해주세요."
    echo ""
fi

echo "✅ 문서 크기 체크 완료"
```

## 📚 참고 자료
- [GitHub 문서 작성 가이드](https://docs.github.com/en/get-started/writing-on-github)
- [Markdown 스타일 가이드](https://github.com/markdownlint/markdownlint/blob/main/docs/RULES.md)
- [문서화 베스트 프랙티스](https://www.writethedocs.org/guide/writing/beginners-guide-to-docs/)

---

**💡 팁**: 이 README.md 파일 자체도 200줄을 넘지 않도록 관리하며, 필요시 세부 가이드는 별도 파일로 분리합니다.