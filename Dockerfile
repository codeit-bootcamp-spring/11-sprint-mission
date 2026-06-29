FROM gradle:8.8-jdk17 AS builder

WORKDIR /build

COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./
RUN ./gradlew --no-daemon dependencies --configuration runtimeClasspath

COPY src/ src/
RUN ./gradlew --no-daemon bootJar -x test

FROM amazoncorretto:17

WORKDIR /app

ENV PROJECT_NAME=discodeit \
    PROJECT_VERSION=1.2-M8 \
    JVM_OPTS=""

COPY --from=builder /build/build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar app.jar

EXPOSE 80

ENTRYPOINT ["sh", "-lc", "exec java $JVM_OPTS -jar app.jar"]