FROM amazoncorretto:17 AS builder

WORKDIR /app

RUN yum install -y findutils && yum clean all

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew clean build -x test --no-daemon

FROM amazoncorretto:17-alpine3.19 AS runtime

WORKDIR /app

ENV PROJECT_NAME=discodeit
ENV PROJECT_VERSION=3.0-M12
ENV JVM_OPTS=""

COPY --from=builder /app/build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar app.jar

EXPOSE 80

CMD ["sh", "-c", "java $JVM_OPTS -jar app.jar"]