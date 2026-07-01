# 빌드 스테이지
FROM amazoncorretto:17 AS builder

WORKDIR /app

#설정 파일 복사
COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle settings.gradle
COPY build.gradle build.gradle

# 의존성
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

#소스코드
COPY src src
RUN ./gradlew bootJar --no-daemon

FROM amazoncorretto:17-alpine

WORKDIR /app

#위에서 빌드한 파일만 가져오기

COPY --from=builder /app/build/libs/*.jar app.jar

ENV APP_NAME=app

ENV JVM_OPTS=""

EXPOSE 80

ENTRYPOINT ["sh", "-c", "java ${JVM_OPTS} -jar ${APP_NAME}.jar"]
