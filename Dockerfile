FROM amazoncorretto:17 AS builder
WORKDIR /app

RUN yum install -y findutils && yum clean all

COPY build.gradle settings.gradle gradlew ./
COPY gradle/ gradle/

RUN ./gradlew dependencies --no-daemon

COPY src/ src/

RUN ./gradlew clean build -x test --no-daemon


FROM amazoncorretto:17-alpine AS runtime
WORKDIR /app
COPY --from=builder /app/build/libs/discodeit-*.jar app.jar

ENV JVM_OPTS="" \
    SERVER_PORT=8080

EXPOSE 8080

CMD ["sh", "-c", "java $JVM_OPTS -jar app.jar"]