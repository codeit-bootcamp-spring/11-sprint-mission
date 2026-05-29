# 1. 빌드 스테이지
# Amazon Corretto 17 이미지를 베이스 이미지로 사용
FROM amazoncorretto:17 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# 프로젝트(애플리케이션) 파일 복사
# 기존 : 전체 복사 -> 심화 : 캐시 최적화
#COPY . .
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Gradle 권한 설정
RUN chmod +x ./gradlew

# 의존성 캐시 레이어
RUN ./gradlew dependencies --no-daemon

# 소스코드 COPY
COPY src ./src

# Gradle Wrapper를 사용하여 애플리케이션 빌드(80 포트 노출)
RUN ./gradlew clean build -x test


# 2. 런타임 스테이지
# 경량화된 베이스 이미지를 사용
FROM amazoncorretto:17-alpine

WORKDIR /app


COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 80

# 환경변수 설정(프로젝트 정보)
#ENV PROJECT_NAME=discodeit
#ENV PROJECT_VERSION=1.2-M8

# 환경변수 설정(JVM)
ENV JVM_OPTS=""

# 애플리케이션 실행(환경변수 사용)
# [shell, command, "java JVM_OPTS(JVM기본값) -jar /app/build/libs/discodeit-1.2-M8.jar"]
#ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar app.jar"]
CMD ["sh", "-c", "java $JVM_OPTS -jar app.jar"]