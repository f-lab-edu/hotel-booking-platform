# --- 1단계: 빌드 스테이지 (Builder Stage) ---
# Gradle과 JDK 17이 포함된 이미지를 빌더로 사용합니다.
FROM gradle:8.10.2-jdk17 AS builder

# 작업 디렉토리를 설정합니다.
WORKDIR /build

# 전체 프로젝트를 복사합니다. (빌드 컨텍스트: 프로젝트 루트)
COPY . .

# user-service를 빌드합니다 (테스트 제외)
RUN gradle :service:user-service:build -x test --no-daemon

# --- 2단계: 최종 스테이지 (Final Stage) ---
# 실제 실행에는 JRE만 필요하므로 더 가벼운 이미지를 사용합니다.
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# 빌더 스테이지에서 생성된 jar 파일만 최종 이미지로 복사합니다.
COPY --from=builder /build/service/user-service/build/libs/*.jar app.jar

# 애플리케이션 포트를 8080으로 노출합니다.
EXPOSE 8080

# 컨테이너 시작 시 애플리케이션을 실행합니다.
ENTRYPOINT ["java", "-jar", "app.jar"]