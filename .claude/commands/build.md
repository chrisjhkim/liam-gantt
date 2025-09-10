# Build Command

프로젝트를 빌드하고 JAR 파일을 생성합니다.

## Command
```bash
echo "🔨 Building Liam Gantt project..."
./gradlew clean build
if [ $? -eq 0 ]; then
    echo "✅ Build completed successfully!"
    echo "📦 JAR file created: build/libs/liam-gantt-*.jar"
else
    echo "❌ Build failed! Check the logs above."
    exit 1
fi
```

## Description
- 이전 빌드 산출물 정리 (clean)
- Java 소스 컴파일
- 테스트 실행
- JAR 파일 생성
- 의존성 검증

## Usage
Type `/build` in Claude Code to run this command.

## Prerequisites  
- Java 21 설치됨
- Gradle wrapper 존재 (./gradlew)