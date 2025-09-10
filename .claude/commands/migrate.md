# Database Migration Command

Flyway를 사용하여 데이터베이스 마이그레이션을 실행합니다.

## Command
```bash
echo "🗄️ Running database migration..."
./gradlew flywayInfo
echo "📋 Current migration status shown above"
echo ""
echo "🚀 Applying pending migrations..."
./gradlew flywayMigrate
if [ $? -eq 0 ]; then
    echo "✅ Database migration completed!"
    ./gradlew flywayInfo
else
    echo "❌ Migration failed! Check the logs above."
    exit 1
fi
```

## Description
- 현재 DB 마이그레이션 상태 확인
- 대기 중인 마이그레이션 스크립트 실행
- 마이그레이션 히스토리 업데이트
- 스키마 버전 관리

## Usage
Type `/migrate` in Claude Code to run this command.

## Prerequisites
- Flyway plugin이 build.gradle.kts에 설정됨
- 마이그레이션 스크립트가 src/main/resources/db/migration/에 존재
- 데이터베이스 연결 정보가 올바르게 설정됨

## Migration Files Location
```
src/main/resources/db/migration/
├── V001__Create_projects_table.sql
├── V002__Create_tasks_table.sql
└── V003__Create_task_dependencies_table.sql
```

## Development Workflow
1. 새로운 마이그레이션 스크립트 작성
2. `/migrate` 명령으로 적용
3. 변경사항 테스트
4. Git에 커밋