# 미션9 작업 일지

> 매일 작업 마무리할 때 기록. `.gitignore`에 `*.md` 포함되어 있어 커밋 안 됨.

---

## 사용 방법

하루 작업 끝날 때 아래 템플릿을 복사해서 날짜 채워서 채워넣기.

```markdown
## YYYY-MM-DD (Day N)

### 오늘 한 일
-

### 트러블슈팅
| 문제 | 원인 | 해결 |
|---|---|---|
|  |  |  |

### 오늘 있던 질문
- Q:
  - A:

### 새로 배운 개념
-

### 참고한 문서/링크
-

### 내일 할 일
-

### 회고 (이번 레슨 어땠나?)
-
```

---

## 2026-06-22 (Day 1)

### 오늘 한 일
- `spring-boot-starter-security`, `spring-security-test` 의존성 추가
- `SecurityConfig` 클래스 생성, `http.build()`만 있는 가장 기본적인 `SecurityFilterChain` 등록
- dev 프로필에 `logging.level.org.springframework.security: trace` 설정
- 앱 실행해서 기본 필터 목록 확인 (아래 참고)
- 💾 커밋 1 완료 (`chore: spring security 의존성 추가 및 기본 SecurityFilterChain 등록`)

기본 필터 목록 (PR 첨부용):
```
DEBUG o.s.s.web.DefaultSecurityFilterChain - Will secure any request with filters:
DisableEncodeUrlFilter, WebAsyncManagerIntegrationFilter, SecurityContextHolderFilter,
HeaderWriterFilter, CorsFilter, CsrfFilter, LogoutFilter, UsernamePasswordAuthenticationFilter,
DefaultResourcesFilter, DefaultLoginPageGeneratingFilter, DefaultLogoutPageGeneratingFilter,
BasicAuthenticationFilter, RequestCacheAwareFilter, SecurityContextHolderAwareRequestFilter,
AnonymousAuthenticationFilter, ExceptionTranslationFilter, AuthorizationFilter
```

### 트러블슈팅
| 문제 | 원인 | 해결 |
|---|---|---|
| `bootRun` 진행률이 80%에서 안 움직임 | `bootRun`은 서버가 떠있는 동안 계속 EXECUTING 상태를 유지하는 장기 실행 태스크 — 정상 동작 | 정상이므로 무시, 서버 종료는 `Ctrl+C` |
| 앱 재실행 시 `Port 8080 was already in use` 에러 | 이전 `bootRun` 프로세스가 아직 살아있는 상태에서 중복 실행 | 기존 터미널 재사용, 중복 실행 안 하기 |
| `CLAUDE.md` 내용이 미션8 시절 내용으로 덮어써짐 | `git checkout -f`로 `origin/김관희` 체크아웃 시, `.gitignore`에 `*.md`를 추가해도 이미 커밋(추적)된 파일은 보호되지 않음 | Behavioral Guidelines 내용으로 복원, `mission8-checklist.md` 등 불필요한 옛 파일 삭제 |

### 오늘 있던 질문
- Q: `.gitignore`에 `*.md` 추가했는데 왜 `CLAUDE.md`가 덮어써졌나?
  - A: gitignore는 "새로 생기는 추적 안 된 파일"만 막아준다. 이미 커밋되어 추적 중인 파일은 `checkout -f`가 그대로 덮어씀. 막으려면 `git rm --cached`로 추적을 먼저 해제해야 함.
- Q: 필터 목록은 텍스트로 붙여넣으면 되나, 스크린샷이 필요한가?
  - A: 텍스트(코드블럭)로 충분. 스크린샷은 보통 UI 화면처럼 시각적 증거가 필요할 때만.

### 새로 배운 개념
- `http.build()`만 호출해도 Spring Security가 CSRF, formLogin, httpBasic, logout 등 기본 필터를 전부 자동 등록함 (직접 설정 안 해도 디폴트가 깔려있음)
- `UserDetailsService`를 직접 안 만들면 Spring Boot가 임시로 `InMemoryUserDetailsManager` + 랜덤 생성 비밀번호를 자동으로 만들어줌
- Gradle `bootRun`은 서버가 켜져있는 동안 계속 "EXECUTING" 상태를 유지하는 장기 실행(long-running) 태스크라서, 퍼센트/시간 표시가 멈춘 것처럼 보이는 게 정상

### 참고한 문서/링크
-

### 내일 할 일
- Day 1 마무리: CSRF 설정 (`CookieCsrfTokenRepository`, `SpaCsrfTokenRequestHandler`, `GET /api/auth/csrf-token`) → 커밋 2
- Day 2: 비밀번호 BCrypt 해싱(커밋 3) → formLogin 커스터마이징 전체(UserDetails/Service/SuccessHandler/FailureHandler + 기존 로그인 코드 제거, 커밋 4)

### 회고 (이번 레슨 어땠나?)
-

---

## 2026-06-23 (Day 2)

### 오늘 한 일
- Day 1 마무리: CSRF 설정 (`CookieCsrfTokenRepository.withHttpOnlyFalse()`, `SpaCsrfTokenRequestHandler`, `GET /api/auth/csrf-token`) — 💾 커밋 2 완료
  - `SecurityConfig`에 `@Configuration` 누락 발견 및 수정 (이게 없어서 우리가 작성한 시큐리티 설정이 전부 무시되고 Spring Boot 기본 체인이 대신 동작하고 있었음)
- Day 2: 회원가입/수정 비밀번호 `BCryptPasswordEncoder` 해싱 적용 — 💾 커밋 3 완료
  - `PasswordEncoder` 빈 등록(`SecurityConfig`)
  - `BasicUserService.create()`/`update()` 양쪽 다 해싱 적용 (원래 체크리스트는 회원가입만이었는데, 수정 시 비밀번호 변경도 평문으로 새는 구멍이라 범위 확장)
  - `BasicAuthService.login()` 비교 방식 `equals()` → `passwordEncoder.matches()`로 변경
  - `BasicUserServiceTest`에 `PasswordEncoder` mock 추가해서 테스트 그린 유지
- `./gradlew test` 전체를 처음 돌려서 우리 작업과 무관한 기존 버그 2개 발견 (별도 커밋 3-1로 분리 예정)
  - `build.gradle`: 테스트 소스셋에 Lombok `annotationProcessor` 미적용 → `AWSS3Test` 컴파일 에러
  - `src/test/resources/schema.sql`이 0바이트 빈 파일 → 테스트(H2) 컨텍스트 로딩 시 전체 Repository/통합 테스트 깨짐
- 남은 테스트 실패(69개)는 `@Configuration` 수정으로 시큐리티 필터가 실제로 걸리면서 인증/CSRF 없는 슬라이스·통합 테스트가 막히는 것 — Day 5에 이미 예정된 작업이라 지금 범위 아님으로 확인하고 넘어감
- `UserStatusRepositoryTest` 1개 실패는 `Instant` 정밀도 이슈로 별개의 기존 flaky 테스트로 판단(보류)
- 회원가입 API 응답 코드가 미션 스펙(200)과 실제 코드(201, `HttpStatus.CREATED`)가 다른 것 발견 — 논의 후 오늘은 손대지 않고 보류

### 트러블슈팅
| 문제 | 원인 | 해결 |
|---|---|---|
| `SpaCsrfTokenRequestHandler` 컴파일 에러 (`method reference not expected here`) | `CsrfTokenRequestHandler`의 SAM(단일 추상 메서드)은 `handle()`이라 `resolveCsrfTokenValue`를 메서드 레퍼런스로 그 타입에 끼워넣을 방법이 없음 | 삼항연산자+메서드레퍼런스 패턴을 버리고 if/else로 변경 |
| `StringUtils.hasText` 컴파일 에러 | `org.apache.commons.lang3.StringUtils`로 잘못 import (그 클래스엔 `hasText` 없음) | `org.springframework.util.StringUtils`로 교체 |
| BCrypt 적용 후 `BasicUserServiceTest`에서 NPE | `@InjectMocks`는 매칭되는 `@Mock`이 없는 생성자 필드엔 `null`을 주입 — 새로 추가된 `PasswordEncoder` 필드가 그 경우였음 | `@Mock PasswordEncoder` 추가 + `given(...).willReturn(...)` 스텁 추가 |
| `./gradlew test` 시 `AWSS3Test`에서 `lombok.extern.slf4j` 못 찾음 | `build.gradle`이 메인 소스셋에만 Lombok 적용, 테스트 소스셋엔 미적용 | `testCompileOnly`/`testAnnotationProcessor`에 Lombok 추가 |
| 전체 Repository/통합 테스트 컨텍스트 로딩 실패 | `src/test/resources/schema.sql`이 0바이트 빈 파일인데, H2(임베디드)라 Spring Boot가 자동 실행 시도하다 에러 | 빈 파일 삭제 (테스트는 `ddl-auto: create`라서 원래 필요 없는 파일) |
| 브라우저로 `csrf-token` 호출 시 화면이 안 넘어감 | 204 No Content 응답이라 브라우저가 navigate를 안 하는 정상 동작 | curl 또는 DevTools Network 탭으로 확인 |

### 오늘 있던 질문
- Q: 회원가입 비밀번호가 저장이 안 되는 거 아니냐?
  - A: 저장은 잘 되고 있었음. 문제는 평문으로 저장되고 있었던 것 — 그래서 BCrypt 해싱이 필요했음.
- Q: `schema.sql`의 역할이 뭐냐?
  - A: `ddl-auto: validate`인 메인 환경에서 실제 테이블 구조를 정의하는 DDL 소스. 테스트는 `ddl-auto: create`라서 Hibernate가 엔티티 보고 자동 생성하므로, 테스트용 `schema.sql`은 원래 불필요한 파일이었음.
- Q: 회원가입 응답을 201에서 200으로 바꾸면 코드가 개선되는 거냐?
  - A: 아니다. REST 관례상 리소스 생성 POST는 201이 더 정확한 코드. 200으로 바꾸는 건 미션 스펙 문서 글자에 맞추기 위함이고 코드 품질 개선은 아님.

### 새로 배운 개념
- BCrypt는 매번 다른 해시를 생성함(salt) → 그래서 로그인 비교는 `equals()` 대신 `matches()`를 써야 함
- `@Configuration`이 빠지면 `@Bean`이 등록 안 되고 Spring Boot 기본 시큐리티 체인이 대신 적용됨 — 기본 필터 목록이 똑같이 나와서 겉으로는 구분이 안 될 수 있음
- Gradle은 메인/테스트 소스셋의 의존성 설정(`compileOnly`/`annotationProcessor` vs `testCompileOnly`/`testAnnotationProcessor`)이 완전히 분리되어 있어서, 한쪽에만 선언하면 다른 쪽엔 적용 안 됨
- Mockito `@InjectMocks`는 매칭되는 `@Mock`이 없는 생성자 필드는 `null`로 주입함

### 참고한 문서/링크
-

### 내일 할 일
- Day 3: `GET /api/auth/me` API (커밋 5), 로그아웃 URL/`LogoutSuccessHandler` (커밋 6)
- Role 기반 권한 모델 도입(커밋 7) — `User` 엔티티에 `role` 추가 시 **메인** `schema.sql`에도 `role` 컬럼 반영 필요
- 사용자 권한 수정 API + ADMIN 계정 초기화 (커밋 8)
- 보류 중: 회원가입 응답 코드 200 vs 201 스펙 불일치 — 나중에 결정
- 별도 커밋으로 분리: `build.gradle` Lombok 테스트 적용 + 빈 `schema.sql` 삭제 (`fix:` 커밋)

### 회고 (이번 레슨 어땠나?)
-

---

## 2026-06-24 (Day 3)

### 오늘 한 일
- 미션 공식 요구사항 문서(로그인/CSRF 가이드)를 받아서, 그 스펙 순서·스타일 그대로 커밋4 진행
  - 스펙은 `LoginSuccessHandler`/`LoginFailureHandler`를 `@Component` 빈으로 만들고 `SecurityConfig`에 필드 주입해서 연결하는 방식 — 처음에 임의로 짰던 "new로 직접 생성" 버전은 버리고 이걸로 다시 감
- 신규 파일 (`security` 패키지): `DiscodeitUserDetails`, `DiscodeitUserDetailsService`, `LoginSuccessHandler`, `LoginFailureHandler`
- `SecurityConfig`: `formLogin(Customizer.withDefaults())` → `loginProcessingUrl("/api/auth/login")` → `successHandler`/`failureHandler` 연결까지 단계별로 진행
- 기존 로그인 코드 제거: `AuthController.login`/`AuthApi.login`(컨트롤러 계층) → `AuthService.login`/`BasicAuthService` 구현(서비스 계층) → `LoginRequest` DTO + 안 쓰게 된 `InvalidCredentialsException`/`UserNotFoundException.withUsername()` 정리(마지막 단계)
- 컴파일이 깨지는 테스트(`AuthControllerTest`, `AuthApiIntegrationTest`) 둘 다 로그인 전용 테스트만 있어서 빈 클래스로 정리 — Day3 me / Day5 role 작업 때 재작성 예정
- `./gradlew build`로 확인: 컴파일 성공, 테스트 61개 실패 — 전부 Day2 때부터 알고 있던 "시큐리티 필터가 걸려서 CSRF/인증 없는 슬라이스·통합 테스트가 막히는" 기존 이슈 + 기존 flaky 1개(`UserStatusRepositoryTest`)뿐, 오늘 작업으로 새로 생긴 실패는 없음
- 💾 커밋 4 완료 (`8e7a632f`, `feat: formLogin 기반 로그인 흐름으로 교체 (UserDetails/SuccessHandler/FailureHandler)`)
- 체크리스트의 "formLogin 기본값 활성화 후 추가된 필터 확인"(디버깅 확인용, PR 첨부 의무 아님)은 보류 — 필요 시 나중에 `bootRun`으로 확인
- `GET /api/auth/me` API 추가
  - `AuthController.me()`에서 `@AuthenticationPrincipal DiscodeitUserDetails principal`로 로그인 세션의 사용자 정보를 그대로 꺼내서 반환 (DB 재조회 없음)
  - `authorizeHttpRequests`가 아직 안 켜져있어서(Day4 예정) 비로그인 상태로 호출하면 `principal`이 null이라 500이 날 수 있음 — Day4에서 자연 해결될 예정이라 지금은 그대로 둠
  - 💾 커밋 5 완료 (`feat: 인증된 사용자 정보 조회 API(/api/auth/me) 추가`)

### 트러블슈팅
| 문제 | 원인 | 해결 |
|---|---|---|
| `LoginSuccessHandler`에서 `throws IOException, SerialException` 컴파일 에러 | `jakarta.servlet.ServletException` 대신 전혀 무관한 `javax.sql.rowset.serial.SerialException`을 잘못 import | import를 `jakarta.servlet.ServletException`으로 교체 |
| `LoginFailureHandler`에서 "추상 메서드 미구현" 에러 | `org.springframework.security.core.AuthenticationException` 대신 무관한 `javax.naming.AuthenticationException`을 잘못 import해서 파라미터 타입이 안 맞아 오버라이드가 성립 안 함 | import를 `org.springframework.security.core.AuthenticationException`으로 교체 |
| `AuthApi.java`에서 `@ApiResponse(value = {...})` 컴파일 에러 | `value` 속성은 `@ApiResponses`(복수형)에만 있는데 `@ApiResponse`(단수형)를 씀 | `@ApiResponses`로 교체 |

### 오늘 있던 질문
- Q: `loginProcessingUrl` 같은 formLogin 커스터마이징은 그냥 `.formLogin(...)`에 넣는 인자(람다)를 바꿔끼우는 거냐?
  - A: 맞음. `.formLogin()`은 인자를 하나(Customizer 람다)만 받음 — 처음엔 `Customizer.withDefaults()`였다가, 커스터마이징할 내용이 생기면 그 자리를 람다로 바꿔서 원하는 설정을 호출하는 방식.
- Q: `DiscodeitUserDetails`/`DiscodeitUserDetailsService`가 비밀번호를 저장하고 직접 검증(비교)하는 거냐?
  - A: 아니다. 저장된 비밀번호 해시를 DB에서 꺼내서 Spring한테 건네주는 역할만 하고, 실제 비교(`PasswordEncoder.matches()`)는 Spring의 `DaoAuthenticationProvider`가 내부적으로 자동 처리.
- Q: `LoginFailureHandler`를 `GlobalExceptionHandler`에 합쳐서 처리하면 안 되냐?
  - A: 안 됨. `GlobalExceptionHandler`(`@RestControllerAdvice`)는 `DispatcherServlet`(Spring MVC 영역)에서 발생한 예외만 잡는데, 로그인 인증 처리는 그보다 앞단인 서블릿 필터 체인에서 끝나버려서 컨트롤러까지 도달하지 않음 — 구조적으로 못 잡음. (Day4의 `AuthenticationEntryPoint`/`AccessDeniedHandler`도 같은 이유로 별도 컴포넌트로 만들어야 함)

### 새로 배운 개념
- `AuthenticationSuccessHandler`/`AuthenticationFailureHandler`는 서블릿 필터 체인 레벨이라, MVC 레벨인 `@RestControllerAdvice`가 못 잡는 영역의 응답을 직접 `HttpServletResponse`에 써야 함
- `UserDetailsService.loadUserByUsername()`에서 사용자를 못 찾으면, 도메인 예외(`UserNotFoundException`)가 아니라 Spring이 기대하는 `UsernameNotFoundException`(`AuthenticationException`의 하위 타입)을 던져야 `LoginFailureHandler`가 받을 수 있음
- 자바 표준 라이브러리/여러 프레임워크에 이름이 같은 무관한 클래스(`SerialException`, `javax.naming.AuthenticationException`)가 따로 존재해서, IDE 자동완성이 엉뚱한 걸 잡아줄 수 있음 — import 경로를 항상 직접 확인해야 함

### 참고한 문서/링크
- 미션 공식 요구사항 문서(로그인 및 CSRF 보안 설정 통합 가이드) — 사용자가 직접 붙여넣어줌

### 내일 할 일 (또는 오늘 이어서)
- 커밋6: 로그아웃 URL/`LogoutSuccessHandler`
- 커밋7: Role 기반 권한 모델 (`User` 엔티티 `role` 추가 시 메인 `schema.sql`도 같이 반영)
- 커밋8: 사용자 권한 수정 API + ADMIN 계정 초기화
- 보류: formLogin 필터 목록 확인(`bootRun`)

### 회고 (이번 레슨 어땠나?)
-

---

## 2026-06-25 (Day 4)

### 오늘 한 일
- 커밋6 완료: 로그아웃 URL(`/api/auth/logout`) + `HttpStatusReturningLogoutSuccessHandler`(204) + `deleteCookies("JSESSIONID")`
  - 설계서엔 있는데 체크리스트엔 빠져있던 `deleteCookies("JSESSIONID")`를 같이 보강
  - 별도로 발견: 커밋4에서 만든 `LoginFailureHandler`가 `SecurityConfig`에 연결이 안 되어 있던 버그(`formLogin`에 `successHandler`만 있고 `failureHandler` 없음) — 같은 커밋에 묶어서 같이 수정
  - `./gradlew build`: 컴파일 성공, 테스트 61개 실패 — Day3 때와 정확히 동일한 숫자/패턴(시큐리티 필터로 인증/CSRF 없는 슬라이스·통합 테스트 차단 + flaky `UserStatusRepositoryTest`), 오늘 변경으로 인한 새 회귀 없음 확인
- 커밋7 진행: Role 기반 권한 모델 도입
  - 공식 미션 요구사항 문서(인가-권한 정의)를 받아서 체크리스트 커밋7+8에 매핑 확인
  - 설계 결정: `DiscodeitUserDetails.getAuthorities()`에 권한 정보를 흘려보내는 방법으로 "UserDto에 role 추가" 대신 "DiscodeitUserDetails에 Role 필드를 직접 추가"를 선택 — `UserDto`는 record라서 필드 추가 시 `new UserDto(...)`를 직접 호출하는 테스트 5개가 컴파일 에러로 깨지는데, `DiscodeitUserDetailsService`가 이미 `User` 엔티티를 들고 있어서 `UserDto`를 안 거치고도 `user.getRole()`을 바로 넘길 수 있었음. `UserDto.role`은 커밋8(권한 수정 API 응답에 필요한 시점)으로 미룸
  - 신규/수정 파일: `entity/Role.java`(신규, enum), `entity/User.java`(`role` 필드+기본값 `Role.USER`), `schema.sql`(`role` 컬럼), `security/DiscodeitUserDetails.java`(`Role` 필드 + `getAuthorities()` 구현), `security/DiscodeitUserDetailsService.java`(생성자 호출부에 `user.getRole()` 추가)
  - 코드 자체는 완료. 다만 `./gradlew build`에서 `DiscodeitApplicationTests > contextLoads()`가 새로 깨짐(61→62) — `SchemaManagementException: missing column [role] in table [users]`. dev 프로필이 붙는 실제 Postgres(docker-compose)에 `ALTER TABLE users ADD COLUMN role ...`까지 직접 실행했는데도 동일 에러 재현되어 **미해결로 보류**, 다음에 이어서 확인하기로 함

### 트러블슈팅
| 문제 | 원인 | 해결 |
|---|---|---|
| `User.java`에 `role` 필드를 직접 타이핑하다가 `@OneToOne`/`@JoinColumn(profile_id)` 어노테이션이 잘못 붙음 | `profile` 필드 바로 위에 추가하면서 어노테이션이 한 줄씩 밀려 복사됨 | `@Enumerated(EnumType.STRING)` + `@Column(length=20, nullable=false)`로 교체 |
| `docker compose exec db psql ...` 실행 시 `failed to connect to the docker API` | Docker Desktop이 꺼져있었음 | Docker Desktop 켠 뒤 재시도 |
| `DiscodeitApplicationTests > contextLoads()`가 `SchemaManagementException: missing column [role]`로 깨짐 | dev Postgres는 `docker-entrypoint-initdb.d/schema.sql`이 볼륨 최초 1회만 실행 — 이미 떠있던 컨테이너엔 `schema.sql` 수정이 반영 안 됨, `ddl-auto: validate`라 자동 마이그레이션도 없음 | `ALTER TABLE users ADD COLUMN role varchar(20) NOT NULL DEFAULT 'USER';`를 컨테이너에 직접 실행 — **그런데도 동일 에러 재현, 미해결** (다음에 `\d users`로 실제 컬럼 존재 여부부터 재확인 필요. 포트 충돌 등으로 앱이 다른 DB에 붙고 있을 가능성도 의심) |

### 오늘 있던 질문
- Q: `deleteCookies("JSESSIONID")`가 다른 커밋(Day5 remember-me 등)에 이미 처리하기로 되어 있는 거 아니냐?
  - A: 아니다. 체크리스트에서 "JSESSIONID"가 나오는 곳은 Day5/커밋13의 remember-me 수동 테스트 시나리오 한 곳뿐이고 그건 무관한 내용 — 커밋6에서 단순 누락된 것으로 확인하고 보강함.
- Q: "당장 편한 것보다 궁극적 목표를 보고" UserDto에 role을 지금 추가하는 게 낫지 않냐?
  - A: 아니다. 어느 쪽이든 커밋8 시점엔 결과가 동일(`UserDto.role` 존재)해지는데, 지금 추가하면 Role/Auth와 무관한 `MessageControllerTest`/`ChannelControllerTest` 등까지 지금 당장 고쳐야 해서 범위가 새버림 — "나중"의 비용이 "지금"보다 훨씬 작은 케이스라 미루는 쪽이 맞음.

### 새로 배운 개념
- Postgres 공식 이미지의 `docker-entrypoint-initdb.d/*.sql`은 데이터 볼륨이 **비어있을 때 최초 1회만** 실행됨 — 이미 초기화된 컨테이너를 재시작/재빌드해도 다시 실행되지 않음 (볼륨을 지워야 재실행됨)
- `ddl-auto: validate`는 엔티티-DB 스키마 불일치를 검증만 하고 절대 자동으로 컬럼을 추가/변경하지 않음 — 운영/개발 DB처럼 한 번 초기화된 스키마를 바꾸려면 직접 `ALTER TABLE`을 실행해야 함(공식 미션 스펙이 `CREATE TABLE`과 `ALTER TABLE` 두 형태를 같이 준 이유)
- record(`UserDto`)에 필드를 추가하면, 그 record의 정식 생성자를 포지셔널 인자로 직접 호출하는 코드(주로 테스트)는 전부 컴파일 에러로 깨지지만, MapStruct처럼 필드명으로 매핑하는 매퍼는 영향 없음 — 그래서 production 코드(`UserMapper`)는 안전하고 테스트만 위험함
- `@RequiredArgsConstructor`는 `final` 필드를 추가하기만 해도 생성자 시그니처가 자동으로 바뀜 — 그 생성자를 호출하는 곳을 전부 찾아서 같이 고쳐야 함

### 참고한 문서/링크
- 공식 미션 요구사항 문서(인가 - 권한 정의) — 사용자가 직접 붙여넣어줌

### 내일 할 일 (또는 이어서)
- DB 이슈 재확인: `docker compose exec db psql -U discodeit_user -d discodeit -c "\d users"`로 실제 컬럼 존재 여부 확인, 포트 충돌 등 다른 원인 점검
- 커밋7 빌드 그린(또는 최소한 기존 61개 수준으로 복귀) 확인 후 git 커밋
- 커밋8: `PUT /api/auth/role` API + ADMIN 계정 초기화 + 이 시점에 `UserDto.role` 추가(테스트 5개 동시 수정 필요)

### 회고 (이번 레슨 어땠나?)
-

---

## 2026-06-26 (Day 5)

### 오늘 한 일
- DB 이슈 원인 확인 및 해결
  - `\d users`로 Docker 컨테이너 DB에 `role` 컬럼이 있음을 확인했는데도 `contextLoads()` 계속 실패
  - `netstat -ano | grep :5432` 결과 PID 두 개 발견 → `tasklist`로 확인하니 Windows 로컬 PostgreSQL 18(`postgres.exe`)이 5432 포트를 먼저 점유하고 있었음
  - Docker 컨테이너는 16.14이었지만 앱이 연결하는 DB는 18.3 → 로컬 PostgreSQL에 `role` 컬럼이 없어서 실패했던 것
  - `application-dev.yaml` 포트를 5433으로 변경, `.env` `POSTGRES_PORT=5433`으로 변경 → `bootRun` 정상 실행, `contextLoads()` 통과
  - 💾 추가 커밋: `chore: 개발 DB 포트를 5433으로 변경 (로컬 PostgreSQL 18 포트 충돌 방지)`
- 커밋8~12 완료 (Day 4까지 전부 완료)
  - 커밋8: `UserRoleUpdatedRequest`, `UserDto.role` 추가, 테스트 5개 수정, `updateRole` 서비스/컨트롤러, `AdminAccountInitializer`, 환경변수
  - 커밋9: `authorizeHttpRequests` + `@EnableMethodSecurity` + `RoleHierarchy`
  - 커밋10: `@PreAuthorize` (채널 생성/수정/삭제 → CHANNEL_MANAGER, 권한 수정 → ADMIN) + `AccessDeniedException` 403 처리 + `AuthenticationEntryPoint` 401 처리
  - 커밋11: `SessionRegistry` 기반 동시 로그인 차단(`maximumSessions(1)`) + `DiscodeitUserDetails.equals/hashCode` + 권한 변경 시 세션 무효화(`BasicAuthService.updateRole`)
  - 커밋12: `UserStatus` 전체 삭제 → `SessionRegistry` 기반 온라인 판정으로 전환 (파일 11개 삭제, 코드 10개 수정)
- 커밋12 누락 수정 + 커밋13 완료 (6/27 이어서)
  - `ReadStatusRepository.findAllByChannelIdWithUser`, `MessageRepository.findAllByChannelIdWithAuthor` — `JOIN FETCH .status` 라인 제거 (커밋12에서 `UserStatus` 삭제 시 누락됐던 것, bootRun 실패로 발견)
  - `SecurityConfig`: `UserDetailsService` 주입, `.rememberMe(...)` 블록 추가 (key, tokenValiditySeconds=2592000), `deleteCookies`에 `"remember-me"` 추가
  - 기동 후 필터 체인에 `RememberMeAuthenticationFilter` 등록 확인
  - 💾 커밋13: `feat: remember-me 로그인 유지 기능 추가`

### 트러블슈팅
| 문제 | 원인 | 해결 |
|---|---|---|
| DB에 `role` 컬럼 있는데 `contextLoads()` 계속 실패 | Windows 로컬 PostgreSQL 18이 5432 포트를 먼저 점유 → 앱이 Docker 컨테이너 대신 로컬 DB에 연결 | `application-dev.yaml` 포트를 5433으로 변경 |
| `AdminAccountInitializer`에서 `lombok.Value` 잘못 import | IDE 자동완성이 `org.springframework.beans.factory.annotation.Value` 대신 `lombok.Value` 선택 | import 직접 수정 |
| `SecurityConfig.roleHierarchy()` 메서드명과 `methodSecurityExpressionHandler` 파라미터명 충돌 | 같은 이름 → 파라미터가 메서드를 가림 | 파라미터명을 `hierarchy`로 변경 |
| `User.java` 수정 중 `profile` 필드의 `@OneToOne`이 함께 삭제됨 | `UserStatus`의 `@OneToOne`만 지워야 했는데 `profile`의 것도 같이 삭제 | `@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)` 복원 |
| `bootRun` 시 `ReadStatusRepository` / `MessageRepository` JPQL 검증 실패 | 커밋12에서 `UserStatus` 삭제 시 두 Repository의 `@Query`에 `JOIN FETCH .status`가 미처 제거되지 않음 | 두 쿼리에서 해당 라인 제거 |

### 오늘 있던 질문
-

### 새로 배운 개념
- `SessionRegistry`는 현재 로그인된 세션 목록을 메모리에 유지 → `maximumSessions(1)`으로 중복 로그인 차단, `expireNow()`로 특정 세션 강제 만료 가능
- `UserDetails.equals()/hashCode()`를 오버라이드해야 `SessionRegistry`가 같은 사용자의 세션을 올바르게 식별함
- `UserStatus.lastActiveAt` 기반 온라인 판정(5분 이내 활동 = 온라인)보다 `SessionRegistry` 기반(로그인 세션 존재 = 온라인)이 더 정확하고 서버 상태와 일치함
- `git rm` = 파일 삭제 + git 추적 해제 + 스테이징을 한 번에 처리

### 참고한 문서/링크
-

### 내일 할 일 (Day 5 나머지)
- 커밋14: SpEL 기반 리소스 소유자 검증 (사용자 본인/메시지 작성자)
- 커밋15: 테스트 정리 + e2e 수동 검증

### 회고 (이번 레슨 어땠나?)
-