# 기본 요구사항

## 1. 프로파일 기반 설정 관리
- [X] 개발(dev), 운영(prod) 환경 프로파일 구성
  - [X] `application-dev.yaml`, `application-prod.yaml` 생성
  - [X] 프로파일별 설정값 분리
      - [X] 데이터베이스 연결 정보 (URL, Username, Password)
      - [X] 서버 포트 (Server Port)

## 2. 로그 관리
- [X] Lombok `@Slf4j` 활용 로깅 구성
- [X] `application.yaml` 기본 로깅 레벨 설정 (default: `info`)
- [X] 프로파일별 로깅 레벨 설정
    - [X] SQL 로그 레벨 유지
    - [X] 프로젝트 로그: 개발(`debug`), 운영(`info`)
- [X] `logback-spring.xml` 설정 파일 구성
    - [X] 로그 패턴 커스터마이징: `%d{yy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %-36logger{36} - %msg%n`
    - [X] 콘솔 & 파일 동시 기록 설정
    - [X] 저장 경로: `{프로젝트 루트}/.logs`
    - [X] 일자별 롤링(Rolling) 및 30일 보관 설정
- [ ] 주요 레이어 및 메소드 로깅 추가 (ERROR, WARN, INFO, DEBUG)
    - [ ] 사용자/채널/메시지 (생성, 수정, 삭제)
    - [ ] 파일 업로드/다운로드

## 3. 예외 처리 고도화
- [X] 패키지 구성: `com.sprint.mission.discodeit.exception[.{도메인}]`
- [X] `ErrorCode` Enum 구현 (상태코드, 예외코드, 메시지 정의)
- [X] 최상위 예외 `DiscodeitException` 정의 (details 속성 포함)
- [X] 도메인별 메인 예외 클래스 정의 (`UserException`, `ChannelException` 등)
- [X] 구체적인 예외 클래스 정의 (`UserNotFoundException` 등)
- [X] 기존 예외(NoSuchElement 등)를 커스텀 예외로 대체
- [X] 일관된 응답을 위한 `ErrorResponse` 클래스 정의
- [X] `@RestControllerAdvice` 기반 전역 예외 핸들러 구현

## 4. 유효성 검사 (Validation)
- [X] Spring Validation 의존성 추가
- [X] Request DTO 제약 조건 어노테이션 추가 (`@NotBlank`, `@Size`, `@Email` 등)
- [X] 컨트롤러 메소드 파라미터에 `@Valid` 적용
- [X] `MethodArgumentNotValidException` 전역 핸들러 처리 및 상세 메시지 반환

## 5. Actuator 설정
- [X] Spring Boot Actuator 의존성 추가
- [X] 엔드포인트 활성화: `health`, `info`, `metrics`, `loggers`
- [X] `info` 엔드포인트 커스텀 정보 추가
    - [X] 앱 이름, 버전, Java/Spring Boot 버전
    - [X] 데이터소스, JPA ddl-auto, Storage 설정, Multipart 설정 정보
- [X] Spring Boot 서버를 실행 후 각종 정보를 확인해보세요.
    - [X] `/actuator/info`
    - [X] `/actuator/metrics`
    - [X] `/actuator/health`
    - [X] `/actuator/loggers`

## 6. 테스트 코드 작성

### 단위 테스트 (Service Layer)
- [X] Mockito & BDDMockito 활용 의존성 모의화
- [X] 핵심 메소드별 성공/실패 케이스(최소 2개) 작성
    - [X] `UserService`: create(O), update(O), delete(X)
    - [X] `ChannelService`: create(private), update(X), delete(O), findByUserId(X), findAllByUserId(O)
    - [X] `MessageService`: create(X), update(X), delete(X), findByChannelId(X), findAllByChannelId(O)

### 슬라이스 테스트 (Slice Test)
- [X] **Repository 테스트**
    - [X] `@DataJpaTest` 및 `application-test.yaml` 구성
    - [X] H2 인메모리 DB (PostgreSQL 호환 모드) 설정
    - [X] `@EnableJpaAuditing` 추가
    - [ ] 주요 쿼리/페이징/정렬 메소드 성공/실패 케이스 작성
- [ ] **Controller 테스트**
    - [ ] `@WebMvcTest` 및 `MockMvc` 활용
    - [ ] 서비스 레이어 Mocking 및 JSON 응답 검증
    - [ ] 주요 컨트롤러별 성공/실패 케이스 작성

### 통합 테스트 (Integration Test)
- [X] `@SpringBootTest` 기반 애플리케이션 컨텍스트 로드
- [X] 테스트용 프로파일 및 H2 DB 환경 구성 => PostgreSQL로 변경
- [X] 주요 API 엔드포인트 테스트 (사용자/채널/메시지 관련 API)
- [X] `@Transactional`을 이용한 테스트 독립성 확보

---

# 심화 요구사항

## 1. MDC를 활용한 로깅 고도화
- [X] 요청 ID, 요청 URL, 요청 방식 등의 정보를 MDC에 추가하는 인터셉터를 구현하세요.
  - [X] 클래스명: `MDCLoggingInterceptor`
  - [X] 패키지명: `com.**.discodeit.config`
  - [X] 요청 ID는 랜덤한 문자열로 생성합니다. (UUID)
  - [X] 요청 ID는 응답 헤더에 포함시켜 더 많은 분석이 가능하도록 합니다. (헤더 이름: `Discodeit-Request-ID`)
- [X] `WebMvcConfigurer`를 통해 `MDCLoggingInterceptor`를 등록하세요.
  - [X] 클래스명: `WebMvcConfig`
  - [X] 패키지명: `com.**.discodeit.config`
- [X] Logback 패턴에 MDC 값을 포함시키세요.
  - 로그 출력 예시:
    ```text
    # 패턴
    {년}-{월}-{일} {시}:{분}:{초}:{밀리초} [{스레드명}] {로그 레벨(5글자로 맞춤)} {로거 이름(최대 36글자)} [{MDC:요청ID} | {MDC:요청 메소드} | {MDC:요청 URL}] - {로그 메시지}{줄바꿈}

    # 예시
    25-01-01 10:33:55.740 [main] DEBUG o.s.api.AbstractOpenApiResource [827cbc0b | GET | /v3/api-docs] - Init duration for springdoc-openapi is: 216 ms
    ```

## 2. Spring Boot Admin을 활용한 메트릭 가시화
- [X] Spring Boot Admin 서버를 구현할 모듈을 생성하세요.
  - IntelliJ 화면 참고 (모듈 정보는 다음과 같습니다. 의존성)
- [X] admin 모듈의 메인 클래스에 `@EnableAdminServer` 어노테이션을 추가하고, 서버는 9090번 포트로 설정합니다.
    ```java
    import de.codecentric.boot.admin.server.config.EnableAdminServer;
    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;

    @SpringBootApplication
    @EnableAdminServer
    public class AdminApplication {
        public static void main(String[] args) {
            SpringApplication.run(AdminApplication.class, args);
        }
    }
    ```
    ```yaml
    # application.yaml
    spring:
      application:
        name: admin
    server:
      port: 9090
    ```
- [X] admin 서버 실행 후 `localhost:9090/applications` 에 접속해봅니다.
- [X] discodeit 프로젝트에 Spring Boot Admin Client를 적용합니다.
- [X] 의존성을 추가합니다.
    ```gradle
    dependencies {
        ...
        implementation 'de.codecentric:spring-boot-admin-starter-client:3.4.5'
    }
    ```
- [X] admin 서버에 등록될 수 있도록 설정 정보를 추가합니다.
    ```yaml
    # application.yml
    spring:
      application:
        name: discodeit
        ...
      boot:
        admin:
          client:
            instance:
              name: discodeit
    ...

    # application-dev.yml
    spring:
      application:
        name: discodeit
        ...
      boot:
        admin:
          client:
            url: http://localhost:9090
    ...

    # application-prod.yml
    spring:
      application:
        name: discodeit
        ...
      boot:
        admin:
          client:
            url: ${SPRING_BOOT_ADMIN_CLIENT_URL}
    ...
    ```
- [X] discodeit 서버를 실행하고, admin 대시보드에 discodeit 인스턴스가 추가되었는지 확인합니다.
- [X] admin 대시보드 화면을 조작해보면서 각종 메트릭 정보를 확인해보세요.
  - 주요 API의 요청 횟수, 응답시간 등
  - 서비스 정보

## 3. 테스트 커버리지 관리
- [X] JaCoCo 플러그인을 추가하세요.
    ```gradle
    plugins {
        id 'jacoco'
    }

    test {
        finalizedBy jacocoTestReport
    }

    jacocoTestReport {
        dependsOn test
        reports {
            xml.required = true
            html.required = true
        }
    }
    ```
- [X] 테스트 실행 후 생성된 리포트를 분석해보세요.
  - 리포트는 `build/reports/jacoco` 경로에서 확인할 수 있습니다.
- [ ] `com.sprint.mission.discodeit.service.basic` 패키지에 대해서 60% 이상의 코드 커버리지를 달성하세요.
