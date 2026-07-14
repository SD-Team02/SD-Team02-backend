# 1단계: 빌드 전용 이미지 - 최종 이미지에는 남지 않음
FROM gradle:8.10-jdk17 AS build
WORKDIR /app

# 의존성 캐시 레이어 분리 (소스코드보다 먼저 복사해서 build.gradle 안 바뀌면 캐시 재사용)
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

COPY . .
RUN gradle clean build -x test --no-daemon

# 2단계: 실행 전용 이미지 - JRE + JAR만 포함, 소스코드/빌드도구 제외
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 헬스체크/타임존 등 최소 유틸만 필요시 추가 (지금은 생략)
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
