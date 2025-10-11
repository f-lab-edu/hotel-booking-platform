# --- 1단계: 빌드 스테이지 (Builder Stage) ---
FROM gradle:8.10.2-jdk17 AS builder

WORKDIR /build

COPY . .

RUN gradle :service:hotel-service:build -x test --no-daemon

# --- 2단계: 최종 스테이지 (Final Stage) ---
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=builder /build/service/hotel-service/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]