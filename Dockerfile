# Stage 1: Builder (빌드 전용)
FROM amazoncorretto:17 AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x ./gradlew

RUN ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew bootJar -x test --no-daemon

# Stage 2: Runtime (실제 배포용)
FROM amazoncorretto:17-alpine

WORKDIR /app

ENV PROJECT_NAME=discodeit
ENV PROJECT_VERSION=3.0-M12

ENV JVM_OPTS=""

COPY --from=builder /app/build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar ./app.jar

EXPOSE 80

# 애플리케이션 실행 명령어 설정 (환경 변수를 읽기 위해 sh -c 사용)
ENTRYPOINT ["sh", "-c", "java ${JVM_OPTS} -jar app.jar"]