# ---- 빌드 스테이지 ----
FROM amazoncorretto:17 AS builder

WORKDIR /app

# 의존성 레이어를 소스와 분리: gradle 설정 파일 먼저 복사
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

# 의존성 다운로드만 수행 → 소스 변경 시 이 레이어는 캐시 재사용
RUN ./gradlew dependencies --no-daemon -q

# 소스 복사 후 빌드
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

# ---- 런타임 스테이지 ----
FROM amazoncorretto:17-headless

WORKDIR /app

ENV PROJECT_NAME=discodeit
ENV PROJECT_VERSION=1.2-M8
ENV JVM_OPTS=""

# 빌드 산출물 jar만 복사 (Gradle, JDK, 소스 제외)
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 80

ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar app.jar"]
