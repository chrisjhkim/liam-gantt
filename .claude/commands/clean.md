# Clean Command

프로젝트의 빌드 산출물을 정리합니다.

## Command
```bash
echo "🧹 Cleaning Liam Gantt project..."
./gradlew clean
rm -rf .gradle/daemon
echo "✅ Cleanup completed!"
echo "🗑️ Removed: build/, .gradle/daemon/"
```

## Description
- build/ 디렉토리 제거
- 컴파일된 클래스 파일 삭제
- JAR/WAR 파일 삭제
- Gradle daemon 캐시 정리
- 임시 파일 제거

## Usage
Type `/clean` in Claude Code to run this command.

## When to Use
- 빌드 문제가 발생했을 때
- 의존성 변경 후
- 프로젝트 리셋이 필요할 때
- 디스크 공간 확보가 필요할 때

## After Clean
Clean 실행 후에는 `/build` 명령으로 다시 빌드 필요