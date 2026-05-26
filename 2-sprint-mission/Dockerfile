# 베이스 이미지 설정
FROM amazoncorretto:17

# 작업 디렉토리 설정
WORKDIR /app

# 프로젝트 파일 복사
COPY gradle ./gradle
COPY gradlew ./gradlew

# 의존성 파일 복사
COPY build.gradle settings.gradle ./

# 의존성 다운로드
RUN ./gradlew dependencies

# 소스 코드 복사 및 빌드
COPY src ./src

# Gradle Wrapper를 사용하여 빌드
RUN ./gradlew build -x test

# 포트 노출
EXPOSE 80

# 환경 변수 설정
ENV PROJECT_NAME=discodeit
ENV PROJECT_VERSION=1.2-M8
ENV JVM_OPTS=""

# JAR 실행
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar"]