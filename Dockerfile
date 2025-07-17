# -------------------------------------------------------------------
# 1단계: 빌드 스테이지 (빌드 도구 포함)
# Amazon Corretto 17 베이스 이미지 사용
FROM eclipse-temurin:17-jdk-alpine AS build

# 작업 디렉토리 설정
WORKDIR /app

# 프로젝트 정보를 환경 변수로 설정
ENV PROJECT_NAME=sb03-monew-team1
ENV PROJECT_VERSION=0.0.1-SNAPSHOT

# Gradle Wrapper와 설정 파일들을 먼저 복사 (Docker 레이어 캐싱 최적화)
COPY gradlew gradlew.bat build.gradle settings.gradle ./
COPY gradle/ gradle/

# 의존성만 먼저 다운로드 (캐시 활용)
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon || true

# 소스 코드 복사
COPY src/ src/

# 애플리케이션 빌드 (테스트 제외)
RUN ./gradlew build -x test --no-daemon

# -------------------------------------------------------------------
# 2단계: 런타임 스테이지 (경량 이미지)
FROM eclipse-temurin:17-jre-alpine AS runtime

# 작업 디렉토리 설정
WORKDIR /app

# curl 설치
RUN apk add --no-cache curl

# 프로젝트 정보를 환경 변수로 설정
ENV PROJECT_NAME=sb03-monew-team1
ENV PROJECT_VERSION=0.0.1-SNAPSHOT

# 빌드 결과물만 복사 (최종 JAR 파일만 포함)
COPY --from=build /app/build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar ./app.jar

# 기본 포트 노출 (실제 포트는 SERVER_PORT 환경변수로)
EXPOSE 80

# 애플리케이션 실행을 위한 ENTRYPOINT와 CMD 조합
ENTRYPOINT ["sh", "-c"]
CMD ["java ${JVM_OPTS} -jar app.jar"]

# (선택) 보안 강화를 위한 비루트 사용자 실행 예시
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 로그 디렉토리 생성 및 권한 부여
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app/logs

USER appuser
