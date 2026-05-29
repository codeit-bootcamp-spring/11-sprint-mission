# 베이스 이미지
FROM amazoncorretto:17

# 작업 디렉토리
WORKDIR /app

# 프로젝트 파일을 컨테이너로 복사해라
COPY . .

# Gradle Wrapper가 프로젝트에 맞는 버전의 Gradle을 알아서 다운로드
RUN ./gradlew bootJar

# 포트 노출
EXPOSE 80

# 프로젝트 정보를 환경 변수 설정
ENV PROJECT_NAME=discodeit
ENV PROJECT_VERSION=1.2-M8

# 나중에 Dockerfile을 다시 수정하지 않고, 서버를 켤 때마다 자바 메모리 설정을 마음대로 바꿀 수 있게 하려고
ENV JVM_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar"]

# ENTRYPOINT : 컨테이너가 켜질 때 이 명령어를 실행
# "sh", "-c" : 리눅스 쉘(Shell)을 켜서 뒤에 오는 문자열을 명령어로서 해석해줘
# java : 자바 프로그램을 실행하는 명령어
#	$JVM_OPTS : 메모리를 얼마나 쓸지 적어두는 메모장
#	-jar : "나 압축 파일 실행할게!" 라는 신호
#	build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar : 실행할 파일의 정확한 주소와 이름