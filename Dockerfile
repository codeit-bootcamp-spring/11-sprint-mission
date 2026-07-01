# =========================================================
# Build Stage
# ---------------------------------------------------------
# 애플리케이션을 실행 가능한 Spring Boot jar로 빌드하는 단계임
# Amazon Corretto 17 Alpine 이미지를 사용해서 기본 이미지 크기 줄임
# =========================================================
FROM amazoncorretto:17-alpine AS build

# 컨테이너 내부 작업 디렉터리를 /app으로 설정함
WORKDIR /app

# Gradle Wrapper와 Gradle 설정 파일을 먼저 복사함
# 소스 코드보다 먼저 복사해서 Docker layer cache를 더 잘 활용하기 위함
COPY gradlew gradlew.bat settings.gradle build.gradle ./

# Gradle Wrapper 실행에 필요한 wrapper 파일 복사함
COPY gradle ./gradle

# Linux 컨테이너에서 gradlew 실행 가능하도록 권한 부여함
RUN chmod +x ./gradlew

# 애플리케이션 소스 코드를 복사함
# 소스 코드 변경 시 이 단계부터 다시 빌드됨
COPY src ./src

# Gradle Wrapper로 Spring Boot 실행 jar 생성함
# Docker 이미지 빌드 단계에서는 빠른 빌드를 위해 테스트 제외함
# --no-daemon은 컨테이너 빌드 환경에서 Gradle 데몬을 남기지 않기 위함
RUN ./gradlew clean bootJar -x test --no-daemon

# =========================================================
# Runtime Stage
# ---------------------------------------------------------
# 빌드 결과물인 jar 파일만 포함해서 실행하는 단계임
# 빌드 도구, Gradle 캐시, 소스 코드를 최종 이미지에 포함하지 않음
# =========================================================
FROM amazoncorretto:17-alpine

# 컨테이너 내부 실행 디렉터리를 /app으로 설정함
WORKDIR /app

# jar 파일 이름을 구성하기 위한 build argument임
# docker build 시 별도 값을 주지 않으면 기본값 사용함
ARG PROJECT_NAME=discodeit
ARG PROJECT_VERSION=1.2-M8

# 런타임에서도 프로젝트 정보를 환경 변수로 확인할 수 있게 설정함
ENV PROJECT_NAME=${PROJECT_NAME}
ENV PROJECT_VERSION=${PROJECT_VERSION}

# JVM 실행 옵션을 외부에서 주입할 수 있도록 환경 변수로 둠
# 예: -Xmx512m -Xms256m
ENV JVM_OPTS=""

# build stage에서 생성된 jar 파일만 runtime stage로 복사함
# 최종 실행 파일명은 app.jar로 고정해서 ENTRYPOINT를 단순하게 유지함
COPY --from=build /app/build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar ./app.jar

# 컨테이너가 80 포트를 사용한다는 것을 명시함
# 실제 서버 포트는 application-prod.yaml의 server.port=80 설정을 사용함
EXPOSE 80

# 컨테이너 시작 시 Spring Boot 애플리케이션 실행함
# sh -c를 사용해야 JVM_OPTS 환경 변수가 정상 해석됨
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar /app/app.jar"]