# 빌드 스테이지
FROM amazoncorretto:17 AS builder

# 작업 디렉토리 설정
WORKDIR /app

RUN yum install -y findutils

# 프로젝트 파일 복사
COPY gradle ./gradle
COPY gradlew ./gradlew

# 의존성 파일 복사
COPY build.gradle settings.gradle ./

# 의존성 다운로드
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY src ./src
RUN ./gradlew clean build -x test --no-daemon


# 런타임 스테이지
FROM amazoncorretto:17-alpine3.21

# 작업 디렉토리 설정
WORKDIR /app

# 환경 변수 설정
ENV PROJECT_NAME=discodeit \
    PROJECT_VERSION=3.0-M12 \
    JVM_OPTS=""

# 빌드 스테이지에서 JAR 파일 복사
COPY --from=builder /app/build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar ./

# 포트 노출
EXPOSE 8080

# JAR 실행
ENTRYPOINT ["sh", "-c", "java ${JVM_OPTS} -jar ${PROJECT_NAME}-${PROJECT_VERSION}.jar"]