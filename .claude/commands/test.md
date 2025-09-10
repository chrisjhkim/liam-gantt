# Test Command

프로젝트의 모든 테스트를 실행합니다.

## Command
```bash
echo "🧪 Running tests for Liam Gantt project..."
./gradlew test --info
if [ $? -eq 0 ]; then
    echo "✅ All tests passed!"
    echo "📊 Test report: build/reports/tests/test/index.html"
else
    echo "❌ Some tests failed! Check the logs above."
    exit 1
fi
```

## Description
- Unit 테스트 실행
- Integration 테스트 실행
- 테스트 리포트 생성
- 커버리지 측정 (추후 JaCoCo 추가)

## Usage
Type `/test` in Claude Code to run this command.

## Test Categories
- **Unit Tests**: Service, Repository 계층 단위 테스트
- **Integration Tests**: Spring Boot 통합 테스트
- **API Tests**: Controller 테스트 (MockMvc)

## Test Report
테스트 완료 후 `build/reports/tests/test/index.html`에서 상세 리포트 확인 가능