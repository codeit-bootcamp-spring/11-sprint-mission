# ════ Stage 1: builder ════════════════════════════════
FROM amazoncorretto:17 AS builder
WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle/ gradle/

RUN ./gradlew dependencies --no-daemon

COPY src/ src/
RUN ./gradlew bootJar -x test --no-daemon

# ════ Stage 2: runtime ════════════════════════════════
FROM amazoncorretto:17-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/discodeit-2.2-M11.jar app.jar

EXPOSE 80

ENV JVM_OPTS=""

CMD ["sh", "-c", "java $JVM_OPTS -jar app.jar"]
