FROM amazoncorretto:17 AS builder

WORKDIR /app

COPY . .

RUN chmod +x gradlew \
    && ./gradlew clean bootJar -x test --no-daemon

FROM amazoncorretto:17

WORKDIR /app

COPY --from=builder /app/build/libs/discodeit-3.0-M12.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
